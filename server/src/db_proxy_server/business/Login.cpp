/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：Login.cpp
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#include <list>
#include "../ProxyConn.h"
#include "../HttpClient.h"
#include "../SyncCenter.h"
#include "Login.h"
#include "UserModel.h"
#include "TokenValidator.h"
#include "json/json.h"
#include "Common.h"
#include "IM.Server.pb.h"
#include "Base64.h"
#include "InterLogin.h"
#include "ExterLogin.h"
#include "GroupModel.h"

CInterLoginStrategy g_loginStrategy;

namespace DB_PROXY {
    
void doLogin(CImPdu* pPdu, uint32_t conn_uuid)
{
    CImPdu* pPduResp = new CImPdu;
    IM::Server::IMValidateReq msg;
    IM::Server::IMValidateRsp msgResp;
    string userId;
    if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
    {
        string pubkey = msg.user_name();
        userId = pubkey;
        string referral_code = msg.password();
        msgResp.set_user_name(pubkey);
        msgResp.set_attach_data(msg.attach_data());
        log("%s request login.", pubkey.c_str());
        IM::BaseDefine::UserInfo cUser;
        if(g_loginStrategy.doLogin(pubkey, referral_code, cUser))
        {
            IM::BaseDefine::UserInfo* pUser = msgResp.mutable_user_info();
            pUser->set_user_id(pubkey);
            pUser->set_user_gender(cUser.user_gender());
            pUser->set_department_id(cUser.department_id());
            pUser->set_user_nick_name(cUser.user_nick_name());
            pUser->set_user_domain(cUser.user_domain());
            pUser->set_avatar_url(cUser.avatar_url());
            pUser->set_email(cUser.email());
            pUser->set_user_tel(cUser.user_tel());
            pUser->set_user_real_name(cUser.user_real_name());
            pUser->set_status(0);
            pUser->set_sign_info(cUser.sign_info());
            pUser->set_referral_code(cUser.referral_code());
            msgResp.set_result_code(0);
            msgResp.set_result_string("成功");
        }
    }
    else
    {
        msgResp.set_result_code(2);
        msgResp.set_result_string("服务端内部错误");
    }
    
  
    //cita: 用户登录的时候创建超级群，如果已经创建则不再创建
    std::string superGroupId;
    bool ret = CGroupModel::getInstance()->createSuperGroup(superGroupId);
    if(ret)
    {
        CGroupModel::getInstance()->insertSuperGroupMember(userId);
        map<string, IM::BaseDefine::GroupVersionInfo> mapGroupId;
        IM::BaseDefine::GroupVersionInfo groupInfo;
        groupInfo.set_group_id(superGroupId);
        groupInfo.set_version(0);
        mapGroupId[superGroupId] = groupInfo;
        list<IM::BaseDefine::GroupInfo> lsGroupInfo;
        CGroupModel::getInstance()->getGroupInfo(mapGroupId, lsGroupInfo);
        if(lsGroupInfo.size() > 0)
        {
            list<IM::BaseDefine::UserInfo> lsUser;
            CGroupModel::getInstance()->getGroupMemberInfoFromDB(superGroupId,lsUser);
            for(auto & user:lsUser)
            {
                IM::BaseDefine::UserInfo *pUser = lsGroupInfo.front().add_group_member_users();
                *pUser = user;
            }
            IM::BaseDefine::GroupInfo *pInfo = msgResp.mutable_public_group_info();
            *pInfo = lsGroupInfo.front();
            log("create supergroup memsize=%d id=%s",lsUser.size(),superGroupId.c_str());
        }
        else
            log("can't found %s group",superGroupId.c_str());

    }
    pPduResp->SetPBMsg(&msgResp);
    pPduResp->SetSeqNum(pPdu->GetSeqNum());
    pPduResp->SetServiceId(IM::BaseDefine::SID_OTHER);
    pPduResp->SetCommandId(IM::BaseDefine::CID_OTHER_VALIDATE_RSP);
    CProxyConn::AddResponsePdu(conn_uuid, pPduResp);
}

};

