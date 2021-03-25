/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：GroupAction.cpp
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#include "../ProxyConn.h"
#include "GroupAction.h"
#include "GroupModel.h"
#include "IM.Group.pb.h"
#include "IM.BaseDefine.pb.h"
#include "public_define.h"
#include "IM.Server.pb.h"

namespace DB_PROXY {
    
    /**
     *  创建群组
     *
     *  @param pPdu      收到的packet包指针
     *  @param conn_uuid 该包过来的socket 描述符
     */
    void createGroup(CImPdu* pPdu, uint32_t conn_uuid)
    {
        IM::Group::IMGroupCreateReq msg;
        IM::Group::IMGroupCreateRsp msgResp;
        if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
        {
            CImPdu* pPduRes = new CImPdu;
            
            string nUserId = msg.user_id();
            string strGroupName = msg.group_name();
            IM::BaseDefine::GroupType nGroupType = msg.group_type();
            if(IM::BaseDefine::GroupType_IsValid(nGroupType))
            {
                string strGroupAvatar = msg.group_avatar();
                set<string> setMember;
                uint32_t nMemberCnt = msg.member_id_list_size();
                for(uint32_t i=0; i<nMemberCnt; ++i)
                {
                    string nUserId = msg.member_id_list(i);
                    setMember.insert(nUserId);
                }
                log("createGroup.%s create %s, userCnt=%u", nUserId.c_str(), strGroupName.c_str(), setMember.size());
                
                string nGroupId = CGroupModel::getInstance()->createGroup(nUserId, strGroupName, strGroupAvatar, nGroupType, setMember);
                msgResp.set_user_id(nUserId);
                msgResp.set_group_name(strGroupName);
                for(auto it=setMember.begin(); it!=setMember.end();++it)
                {
                    msgResp.add_user_id_list(*it);
                }
                if(nGroupId != "")
                {
                    msgResp.set_result_code(0);
                    msgResp.set_group_id(nGroupId);
                }
                else
                {
                    msgResp.set_result_code(1);
                }
                
                
                log("createGroup.%s create %s, userCnt=%u, result:%d", nUserId.c_str(), strGroupName.c_str(), setMember.size(), msgResp.result_code());
                
                msgResp.set_attach_data(msg.attach_data());
                pPduRes->SetPBMsg(&msgResp);
                pPduRes->SetSeqNum(pPdu->GetSeqNum());
                pPduRes->SetServiceId(IM::BaseDefine::SID_GROUP);
                pPduRes->SetCommandId(IM::BaseDefine::CID_GROUP_CREATE_RESPONSE);
                CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
            }
            else
            {
                log("invalid group type.userId=%s, groupType=%u, groupName=%s", nUserId.c_str(), nGroupType, strGroupName.c_str());
            }
        }
        else
        {
            log("parse pb failed");
        }
    }
    
    /**
     *  获取正式群列表
     *
     *  @param pPdu      收到的packet包指针
     *  @param conn_uuid 该包过来的socket 描述符
     */
    void getNormalGroupList(CImPdu* pPdu, uint32_t conn_uuid)
    {
        IM::Group::IMNormalGroupListReq msg;
        IM::Group::IMNormalGroupListRsp msgResp;
        if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
        {
            CImPdu* pPduRes = new CImPdu;
            
            string nUserId = msg.user_id();
            
            list<IM::BaseDefine::GroupVersionInfo> lsGroup;
            CGroupModel::getInstance()->getUserGroup(nUserId, lsGroup, IM::BaseDefine::GROUP_TYPE_NORMAL);
            msgResp.set_user_id(nUserId);
            for(auto it=lsGroup.begin(); it!=lsGroup.end(); ++it)
            {
                IM::BaseDefine::GroupVersionInfo* pGroupVersion = msgResp.add_group_version_list();
                pGroupVersion->set_group_id(it->group_id());
                pGroupVersion->set_version(it->version());
            }
            
            log("getNormalGroupList. userId=%s, count=%d", nUserId.c_str(), msgResp.group_version_list_size());
            
            msgResp.set_attach_data(msg.attach_data());
            pPduRes->SetPBMsg(&msgResp);
            pPduRes->SetSeqNum(pPdu->GetSeqNum());
            pPduRes->SetServiceId(IM::BaseDefine::SID_GROUP);
            pPduRes->SetCommandId(IM::BaseDefine::CID_GROUP_NORMAL_LIST_RESPONSE);
            CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
        }
        else
        {
            log("parse pb failed");
        }
    }
    
    /**
     *  获取群信息
     *
     *  @param pPdu      收到的packet包指针
     *  @param conn_uuid 该包过来的socket 描述符
     */
    void getGroupInfo(CImPdu* pPdu, uint32_t conn_uuid)
    {
        IM::Group::IMGroupInfoListReq msg;
        IM::Group::IMGroupInfoListRsp msgResp;
        if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
        {
            CImPdu* pPduRes = new CImPdu;
            string nUserId = msg.user_id();
            uint32_t nGroupCnt = msg.group_version_list_size();
            
            map<string, IM::BaseDefine::GroupVersionInfo> mapGroupId;
            for(uint32_t i=0; i<nGroupCnt; ++i)
            {
                IM::BaseDefine::GroupVersionInfo groupInfo = msg.group_version_list(i);
                if(CGroupModel::getInstance()->isValidateGroupId(groupInfo.group_id()))
                {
                    mapGroupId[groupInfo.group_id()] = groupInfo;
                }
            }
            list<IM::BaseDefine::GroupInfo> lsGroupInfo;
            CGroupModel::getInstance()->getGroupInfo(mapGroupId, lsGroupInfo);
            
            msgResp.set_user_id(nUserId);
            for(auto it=lsGroupInfo.begin(); it!=lsGroupInfo.end(); ++it)
            {
                IM::BaseDefine::GroupInfo* pGroupInfo = msgResp.add_group_info_list();
    //            *pGroupInfo = *it;
                pGroupInfo->set_group_id(it->group_id());
                pGroupInfo->set_version(it->version());
                pGroupInfo->set_group_name(it->group_name());
                pGroupInfo->set_group_avatar(it->group_avatar());
                pGroupInfo->set_group_creator_id(it->group_creator_id());
                pGroupInfo->set_group_type(it->group_type());
                pGroupInfo->set_shield_status(it->shield_status());
                uint32_t nGroupMemberCnt = it->group_member_list_size();
                for (uint32_t i=0; i<nGroupMemberCnt; ++i) {
                    string userId = it->group_member_list(i);
                    pGroupInfo->add_group_member_list(userId);
                }

                list<IM::BaseDefine::UserInfo> lUserInfo;
                CGroupModel::getInstance()->getGroupMemberInfoFromDB(it->group_id(),lUserInfo);
                for(auto & info:lUserInfo)
                {
                    IM::BaseDefine::UserInfo *pInfo = pGroupInfo->add_group_member_users();
                    *pInfo = info;
                }
            }
            
            log("userId=%s, requestCount=%u", nUserId.c_str(), nGroupCnt);
            
            msgResp.set_attach_data(msg.attach_data());
            pPduRes->SetPBMsg(&msgResp);
            pPduRes->SetSeqNum(pPdu->GetSeqNum());
            pPduRes->SetServiceId(IM::BaseDefine::SID_GROUP);
            pPduRes->SetCommandId(IM::BaseDefine::CID_GROUP_INFO_RESPONSE);
            CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
        }
        else
        {
            log("parse pb failed");
        }
    }
    /**
     *  修改群成员，增加或删除
     *
     *  @param pPdu      收到的packet包指针
     *  @param conn_uuid 该包过来的socket 描述符
     */
    void modifyMember(CImPdu* pPdu, uint32_t conn_uuid)
    {
        IM::Group::IMGroupChangeMemberReq msg;
        IM::Group::IMGroupChangeMemberRsp msgResp;
        if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
        {
            string nUserId = msg.user_id();
            string nGroupId = msg.group_id();
            IM::BaseDefine::GroupModifyType nType = msg.change_type();
            if (IM::BaseDefine::GroupModifyType_IsValid(nType) &&
                CGroupModel::getInstance()->isValidateGroupId(nGroupId)) {
                
                CImPdu* pPduRes = new CImPdu;
                
                uint32_t nQuit = msg.quit();
                uint32_t nCnt = msg.member_id_list_size();
                set<string> setUserId;
                for(uint32_t i=0; i<nCnt;++i)
                {
                    setUserId.insert(msg.member_id_list(i));
                }
                list<string> lsCurUserId;
                bool bRet = CGroupModel::getInstance()->modifyGroupMember(nUserId, nGroupId,
                		nType, setUserId, lsCurUserId, nQuit);
                msgResp.set_user_id(nUserId);
                msgResp.set_group_id(nGroupId);
                msgResp.set_change_type(nType);
                msgResp.set_result_code(bRet?0:1);
                if(bRet)
                {
                    for(auto it=setUserId.begin(); it!=setUserId.end(); ++it)
                    {
                        msgResp.add_chg_user_id_list(*it);
                    }
                    
                    for(auto it=lsCurUserId.begin(); it!=lsCurUserId.end(); ++it)
                    {
                        msgResp.add_cur_user_id_list(*it);
                    }
                }
                log("userId=%s, groupId=%s, result=%u, changeCount:%u, currentCount=%u",nUserId.c_str(), nGroupId.c_str(),  bRet?0:1, msgResp.chg_user_id_list_size(), msgResp.cur_user_id_list_size());
                msgResp.set_attach_data(msg.attach_data());
                pPduRes->SetPBMsg(&msgResp);
                pPduRes->SetSeqNum(pPdu->GetSeqNum());
                pPduRes->SetServiceId(IM::BaseDefine::SID_GROUP);
                pPduRes->SetCommandId(IM::BaseDefine::CID_GROUP_CHANGE_MEMBER_RESPONSE);
                CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
            }
            else
            {
                log("invalid groupModifyType or groupId. userId=%s, groupId=%s, groupModifyType=%u", nUserId.c_str(), nGroupId.c_str(), nType);
            }
            
        }
        else
        {
            log("parse pb failed");
        }
    }
    
    /**
     *  设置群组信息推送，屏蔽或者取消屏蔽
     *
     *  @param pPdu      收到的packet包指针
     *  @param conn_uuid 该包过来的socket 描述符
     */
    void setGroupPush(CImPdu* pPdu, uint32_t conn_uuid)
    {
        IM::Group::IMGroupShieldReq msg;
        IM::Group::IMGroupShieldRsp msgResp;
        if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
        {
            string nUserId = msg.user_id();
            string nGroupId = msg.group_id();
            uint32_t nStatus = msg.shield_status();
            if(CGroupModel::getInstance()->isValidateGroupId(nGroupId))
            {
                
                CImPdu* pPduRes = new CImPdu;
                bool bRet = CGroupModel::getInstance()->setPush(nUserId, nGroupId, IM_GROUP_SETTING_PUSH, nStatus);
                
                msgResp.set_user_id(nUserId);
                msgResp.set_group_id(nGroupId);
                msgResp.set_result_code(bRet?0:1);
            
                log("userId=%s, groupId=%s, result=%u", nUserId.c_str(), nGroupId.c_str(), msgResp.result_code());
                
                msgResp.set_attach_data(msg.attach_data());
                pPduRes->SetPBMsg(&msgResp);
                pPduRes->SetSeqNum(pPdu->GetSeqNum());
                pPduRes->SetServiceId(IM::BaseDefine::SID_GROUP);
                pPduRes->SetCommandId(IM::BaseDefine::CID_GROUP_SHIELD_GROUP_RESPONSE);
                CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
            }
            else
            {
                log("Invalid group.userId=%s, groupId=%s", nUserId.c_str(), nGroupId.c_str());
            }
        }
        else
        {
            log("parse pb failed");
        }
    }
    
    /**
     *  获取一个群的推送设置
     *
     *  @param pPdu      收到的packet包指针
     *  @param conn_uuid 该包过来的socket 描述符
     */
    void getGroupPush(CImPdu* pPdu, uint32_t conn_uuid)
    {
        IM::Server::IMGroupGetShieldReq msg;
        IM::Server::IMGroupGetShieldRsp msgResp;
        if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
        {
            string nGroupId = msg.group_id();
            uint32_t nUserCnt = msg.user_id_size();
            if(CGroupModel::getInstance()->isValidateGroupId(nGroupId))
            {
                CImPdu* pPduRes = new CImPdu;
                list<string> lsUser;
                for(uint32_t i=0; i<nUserCnt; ++i)
                {
                    lsUser.push_back(msg.user_id(i));
                }
                list<IM::BaseDefine::ShieldStatus> lsPush;
                CGroupModel::getInstance()->getPush(nGroupId, lsUser, lsPush);
                
                msgResp.set_group_id(nGroupId);
                for (auto it=lsPush.begin(); it!=lsPush.end(); ++it) {
                    IM::BaseDefine::ShieldStatus* pStatus = msgResp.add_shield_status_list();
        //            *pStatus = *it;
                    pStatus->set_user_id(it->user_id());
                    pStatus->set_group_id(it->group_id());
                    pStatus->set_shield_status(it->shield_status());
                }
                
                log("groupId=%s, count=%u", nGroupId.c_str(), nUserCnt);
                
                msgResp.set_attach_data(msg.attach_data());
                pPduRes->SetPBMsg(&msgResp);
                pPduRes->SetSeqNum(pPdu->GetSeqNum());
                pPduRes->SetServiceId(IM::BaseDefine::SID_OTHER);
                pPduRes->SetCommandId(IM::BaseDefine::CID_OTHER_GET_SHIELD_RSP);
                CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
            }
            else
            {
                log("Invalid groupId. nGroupId=%s", nGroupId.c_str());
            }
        }
        else
        {
            log("parse pb failed");
        }
    }

    //cita add:获取推荐群信息
    void getRecommandGroupListInfo(CImPdu *pPdu, uint32_t conn_uuid)
    {
        IM::Group::IMRecommandGroupListReq msg;
        IM::Group::IMRecommandGroupListRsp msgResp;
        if(msg.ParseFromArray(pPdu->GetBodyData(),pPdu->GetBodyLength()))
        {
            std::string user_id = msg.user_id();
            std::list<IM::BaseDefine::GroupInfo> groupList;
            bool bRet = CGroupModel::getInstance()->getRecommandGroupList(groupList);
            if(bRet)
            {
                for(auto & info : groupList)
                {
                    IM::BaseDefine::GroupInfo * newInfo = msgResp.add_group_info_list();
                    *newInfo = info;
                }
            }

            msgResp.set_user_id(user_id);
            msgResp.set_attach_data(msg.attach_data());
            CImPdu * rspPdu = new CImPdu();
            rspPdu->SetPBMsg(&msgResp);
            rspPdu->SetSeqNum(pPdu->GetSeqNum());
            rspPdu->SetServiceId(IM::BaseDefine::SID_GROUP);
            rspPdu->SetCommandId(IM::BaseDefine::CID_GROUP_RECOMMAND_LIST_INFO_RESPONSE);
            CProxyConn::AddResponsePdu(conn_uuid, rspPdu);
        }
    }

}

