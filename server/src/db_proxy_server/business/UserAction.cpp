/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：UserAction.cpp
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#include <list>
#include <map>

#include "../ProxyConn.h"
#include "../DBPool.h"
#include "../SyncCenter.h"
#include "public_define.h"
#include "UserModel.h"
#include "IM.Login.pb.h"
#include "IM.Buddy.pb.h"
#include "IM.BaseDefine.pb.h"
#include "IM.Server.pb.h"
#include "RelationModel.h"
#include "SystemMsgModel.h"
//#include "json/json.h"
#include "GroupModel.h"

namespace DB_PROXY {

    void getUserInfo(CImPdu* pPdu, uint32_t conn_uuid)
    {
        IM::Buddy::IMUsersInfoReq msg;
        IM::Buddy::IMUsersInfoRsp msgResp;
        if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
        {
            CImPdu* pPduRes = new CImPdu;
            
            string from_user_id = msg.user_id();
            uint32_t userCount = msg.user_id_list_size();
            std::list<string> idList;
            for(uint32_t i = 0; i < userCount;++i) {
    			idList.push_back(msg.user_id_list(i));
            }
            std::list<IM::BaseDefine::UserInfo> lsUser;
            CUserModel::getInstance()->getUsers(idList, lsUser);
            msgResp.set_user_id(from_user_id);
            for(list<IM::BaseDefine::UserInfo>::iterator it=lsUser.begin();
                it!=lsUser.end(); ++it)
            {
                IM::BaseDefine::UserInfo* pUser = msgResp.add_user_info_list();
    //            *pUser = *it;
             
                pUser->set_user_id(it->user_id());
                pUser->set_user_gender(it->user_gender());
                pUser->set_user_nick_name(it->user_nick_name());
                pUser->set_avatar_url(it->avatar_url());

                pUser->set_sign_info(it->sign_info());
                pUser->set_department_id(it->department_id());
                pUser->set_email(it->email());
                pUser->set_user_real_name(it->user_real_name());
                pUser->set_user_tel("");    //用户电话隐藏
                pUser->set_user_domain(it->user_domain());
                pUser->set_status(it->status());
                pUser->set_fans_cnt(it->fans_cnt());
            }
            log("userId=%s, userCnt=%u", from_user_id.c_str(), userCount);
            msgResp.set_attach_data(msg.attach_data());
            pPduRes->SetPBMsg(&msgResp);
            pPduRes->SetSeqNum(pPdu->GetSeqNum());
            pPduRes->SetServiceId(IM::BaseDefine::SID_BUDDY_LIST);
            pPduRes->SetCommandId(IM::BaseDefine::CID_BUDDY_LIST_USER_INFO_RESPONSE);
            CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
        }
        else
        {
            log("parse pb failed");
        }
    }
    
    void getChangedUser(CImPdu* pPdu, uint32_t conn_uuid)
    {
        IM::Buddy::IMAllUserReq msg;
        IM::Buddy::IMAllUserRsp msgResp;
        if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
        {
            CImPdu* pPduRes = new CImPdu;
            
            string nUserId = msg.user_id();
            uint32_t nLastTime = msg.latest_update_time();

            //update time不能再用统一的方式处理了
            //uint32_t nLastUpdate = CSyncCenter::getInstance()->getLastUpdate();
            uint32_t nLastUpdate = 0;

            CUserModel::getInstance()->getUsers2(nUserId, nLastTime, msgResp);

            msgResp.set_user_id(nUserId);
            msgResp.set_latest_update_time(nLastTime);

            log("userId=%s,nLastUpdate=%u, last_time=%u, userCnt=%u", nUserId.c_str(), nLastUpdate, nLastTime, msgResp.user_list_size());
            msgResp.set_attach_data(msg.attach_data());
            pPduRes->SetPBMsg(&msgResp);
            pPduRes->SetSeqNum(pPdu->GetSeqNum());
            pPduRes->SetServiceId(IM::BaseDefine::SID_BUDDY_LIST);
            pPduRes->SetCommandId(IM::BaseDefine::CID_BUDDY_LIST_ALL_USER_RESPONSE);
            CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
        }
        else
        {
            log("parse pb failed");
        }
    }
    
	//cita add:只搜索在线用户
	void searcOnlinehUser(CImPdu* pPdu, uint32_t conn_uuid)
	{
		IM::Server::IMGetOnlineUserInfoReq msg;
		IM::Buddy::IMGetALLOnlineUserRsp msgResp;
		if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
		{
			log("search useid = %s",msg.user_id().c_str());
			CImPdu* pPduRes = new CImPdu;
			string nReqId = msg.user_id();
			uint32_t userCount = msg.user_id_list_size();
			std::list<string> idList;
            for(uint32_t i = 0; i < userCount;++i) {
    			idList.push_back(msg.user_id_list(i));
				log("online user = %s",msg.user_id_list(i).c_str());
            }
            std::list<IM::BaseDefine::UserInfo> lsUser;
            CUserModel::getInstance()->getUsers(idList, lsUser);
			msgResp.set_user_id(msg.user_id());
			log("detail user count = %d",lsUser.size());
			for (list<IM::BaseDefine::UserInfo>::iterator it=lsUser.begin();
				 it!=lsUser.end(); ++it) {
				IM::BaseDefine::UserInfo* pUser = msgResp.add_user_list();

				pUser->set_user_id(it->user_id());
				pUser->set_user_gender(it->user_gender());
				pUser->set_user_nick_name(it->user_nick_name());
				pUser->set_avatar_url(it->avatar_url());
				pUser->set_sign_info(it->sign_info());
				pUser->set_department_id(it->department_id());
				pUser->set_email(it->email());
				pUser->set_user_real_name(it->user_real_name());
				pUser->set_user_tel("");    //用户电话隐藏
				pUser->set_user_domain(it->user_domain());
				pUser->set_status(it->status());
				pUser->set_fans_cnt(it->fans_cnt());
			}
			log("msg user count = %d",msgResp.user_list_size());

			msgResp.set_user_id(nReqId);
			msgResp.set_attach_data(msg.attach_data());
			pPduRes->SetPBMsg(&msgResp);
			pPduRes->SetSeqNum(pPdu->GetSeqNum());
			pPduRes->SetServiceId(IM::BaseDefine::SID_BUDDY_LIST);
			pPduRes->SetCommandId(IM::BaseDefine::CID_BUDDY_LIST_GET_ALL_ONLINE_USER_INFO_RESPONSE);
			CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
		}
		else
		{
			log("parse pb failed");
		}
	}


    void searchUser(CImPdu* pPdu, uint32_t conn_uuid)
	{
		IM::Buddy::IMSearchUserReq msg;
		IM::Buddy::IMSearchUserRsp msgResp;
		if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
		{
			CImPdu* pPduRes = new CImPdu;

			list<IM::BaseDefine::UserInfo> lsUsers;
			string nReqId = msg.user_id();
			string search_user_name = msg.search_user_name();

			CUserModel::getInstance()->getUsersByNickOrPhone(search_user_name, lsUsers);
			//msgResp.set_search_user_id(nReqId);
			for (list<IM::BaseDefine::UserInfo>::iterator it=lsUsers.begin();
				 it!=lsUsers.end(); ++it) {
				IM::BaseDefine::UserInfo* pUser = msgResp.add_search_user_list();

				pUser->set_user_id(it->user_id());
				pUser->set_user_gender(it->user_gender());
				pUser->set_user_nick_name(it->user_nick_name());
				pUser->set_avatar_url(it->avatar_url());
				pUser->set_sign_info(it->sign_info());
				pUser->set_department_id(it->department_id());
				pUser->set_email(it->email());
				pUser->set_user_real_name(it->user_real_name());
				pUser->set_user_tel("");    //用户电话隐藏
				pUser->set_user_domain(it->user_domain());
				pUser->set_status(it->status());
				pUser->set_fans_cnt(it->fans_cnt());
			}
			//log("userId=%u,nLastUpdate=%u, last_time=%u, userCnt=%u", nReqId,nLastUpdate, nLastTime, msgResp.user_list_size());
			msgResp.set_user_id(nReqId);
			msgResp.set_attach_data(msg.attach_data());
			pPduRes->SetPBMsg(&msgResp);
			pPduRes->SetSeqNum(pPdu->GetSeqNum());
			pPduRes->SetServiceId(IM::BaseDefine::SID_BUDDY_LIST);
			pPduRes->SetCommandId(IM::BaseDefine::CID_BUDDY_LIST_SEARCH_USER_RESPONSE);
			CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
		}
		else
		{
			log("parse pb failed");
		}
	}

    void delFriend(CImPdu* pPdu, uint32_t conn_uuid)
	{
		IM::Buddy::IMDelFriendReq msg;
		IM::Buddy::IMDelFriendRsp msgResp;
		if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
		{
			CImPdu* pPduRes = new CImPdu;

			string nUserId = msg.user_id();
			string nFriendId = msg.friend_id();

			//需要在这还验证下用户是否存在
			//存在给被加用户发一个请求成为好友消息
			uint32_t result_code = 0;

			log("%s del friend %s", nUserId.c_str(), nFriendId.c_str());

			if(!CRelationModel::getInstance()->delFriend(nUserId, nFriendId)){
				result_code = 1;
			}

			//删除好友，不需要通知对方吧
			//CSystemMsgModel* pMsgModel = CSystemMsgModel::getInstance();
			//pMsgModel->sendSystemMsg(nUserId, nFriendId,
			//		IM::BaseDefine::MSG_TYPE_ADD_FRIEND, nCreateTime, 0, json_str, 0);
			
			msgResp.set_result_code(result_code); //0:成功 1:删除失败
			msgResp.set_user_id(nUserId);
			msgResp.set_friend_id(nFriendId);
			msgResp.set_attach_data(msg.attach_data());
			pPduRes->SetPBMsg(&msgResp);
			pPduRes->SetSeqNum(pPdu->GetSeqNum());
			pPduRes->SetServiceId(IM::BaseDefine::SID_BUDDY_LIST);
			pPduRes->SetCommandId(IM::BaseDefine::CID_BUDDY_LIST_DEL_FRIEND_RESPONSE);
			CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
		}
		else
		{
			log("parse pb failed");
		}
	}

    void followUser(CImPdu* pPdu, uint32_t conn_uuid)
	{
		IM::Buddy::IMFollowUserReq msg;
		IM::Buddy::IMFollowUserRsp msgResp;
		if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
		{
			CImPdu* pPduRes = new CImPdu;

			list<IM::BaseDefine::UserInfo> lsUsers;
			string nUserId = msg.user_id();
			string nFriendId = msg.friend_id();

			DBUserInfo_t cUser;
			CUserModel* pUserModel = CUserModel::getInstance();

			if(nUserId == nFriendId || !pUserModel->getUserById(nFriendId, cUser)){
				msgResp.set_result_code(1);
			}else{
				//加之前还是要验证用户是否存在
				CRelationModel::getInstance()->addRelation(nUserId, nFriendId, RELATION_ACTION_ADD_FOLLOW);
				msgResp.set_result_code(0); //0:成功 1:失败
			}

			log("user=%s follow friend=%s result %u", nUserId.c_str(), nFriendId.c_str(), msgResp.result_code());

			msgResp.set_user_id(nUserId);
			msgResp.set_attach_data(msg.attach_data());
			pPduRes->SetPBMsg(&msgResp);
			pPduRes->SetSeqNum(pPdu->GetSeqNum());
			pPduRes->SetServiceId(IM::BaseDefine::SID_BUDDY_LIST);
			pPduRes->SetCommandId(IM::BaseDefine::CID_BUDDY_LIST_FOLLOW_USER_RESPONSE);
			CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
		}
		else
		{
			log("parse pb failed");
		}
	}

    void delFollowUser(CImPdu* pPdu, uint32_t conn_uuid)
	{
		IM::Buddy::IMDelFollowUserReq msg;
		IM::Buddy::IMDelFollowUserRsp msgResp;
		if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
		{
			CImPdu* pPduRes = new CImPdu;

			string nUserId = msg.user_id();
			string nFriendId = msg.friend_id();

			uint32_t result_code = 0;
			//加之前还是要验证用户是否存在
			if(!CRelationModel::getInstance()->delFollowUser(nUserId, nFriendId)){
				result_code = 1;
				log("%s, %s failed", nUserId.c_str(), nFriendId.c_str());
			}

			msgResp.set_result_code(result_code); //0:成功 1:失败
			msgResp.set_user_id(nUserId);
			msgResp.set_friend_id(nFriendId);
			msgResp.set_attach_data(msg.attach_data());
			pPduRes->SetPBMsg(&msgResp);
			pPduRes->SetSeqNum(pPdu->GetSeqNum());
			pPduRes->SetServiceId(IM::BaseDefine::SID_BUDDY_LIST);
			pPduRes->SetCommandId(IM::BaseDefine::CID_BUDDY_LIST_DEL_FOLLOW_USER_RESPONSE);
			CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
		}
		else
		{
			log("parse pb failed");
		}
	}

    void changeAvatar(CImPdu* pPdu, uint32_t conn_uuid)
    {
    	IM::Buddy::IMChangeAvatarReq msg;
		IM::Buddy::IMChangeAvatarRsp msgResp;
		if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
		{
			CImPdu* pPduRes = new CImPdu;

			string nUserId = msg.user_id();
			string avatar_url = msg.avatar_url();

			CUserModel::getInstance()->changeAvatar(nUserId, avatar_url);

			//log("userId=%u,nLastUpdate=%u, last_time=%u, userCnt=%u", nReqId,nLastUpdate, nLastTime, msgResp.user_list_size());
			msgResp.set_result_code(0); //0:成功 1:已经添加了 2:此用户不存在
			msgResp.set_user_id(nUserId);
			msgResp.set_attach_data(msg.attach_data());
			pPduRes->SetPBMsg(&msgResp);
			pPduRes->SetSeqNum(pPdu->GetSeqNum());
			pPduRes->SetServiceId(IM::BaseDefine::SID_BUDDY_LIST);
			pPduRes->SetCommandId(IM::BaseDefine::CID_BUDDY_LIST_CHANGE_AVATAR_RESPONSE);
			CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
		}
		else
		{
			log("parse pb failed");
		}
    }
 
    void changeUserSignInfo(CImPdu* pPdu, uint32_t conn_uuid) {
	    IM::Buddy::IMChangeSignInfoReq req;
		IM::Buddy::IMChangeSignInfoRsp resp;
		if(req.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength())) {
			string user_id = req.user_id();
	 	    const string& sign_info = req.sign_info();

			bool result = CUserModel::getInstance()->updateUserSignInfo(user_id, sign_info);

			resp.set_user_id(user_id);
			resp.set_result_code(result ? 0 : 1);
			if (result) {
				resp.set_sign_info(sign_info);
				log("changeUserSignInfo sucess, user_id=%s, sign_info=%s", user_id.c_str(), sign_info.c_str());
			} else {
				log("changeUserSignInfo false, user_id=%s, sign_info=%s", user_id.c_str(), sign_info.c_str());
			}

			CImPdu* pdu_resp = new CImPdu();
			resp.set_attach_data(req.attach_data());
			pdu_resp->SetPBMsg(&resp);
			pdu_resp->SetSeqNum(pPdu->GetSeqNum());
			pdu_resp->SetServiceId(IM::BaseDefine::SID_BUDDY_LIST);
			pdu_resp->SetCommandId(IM::BaseDefine::CID_BUDDY_LIST_CHANGE_SIGN_INFO_RESPONSE);
			CProxyConn::AddResponsePdu(conn_uuid, pdu_resp);

		} else {
			log("changeUserSignInfo: IMChangeSignInfoReq ParseFromArray failed!!!");
		}
    }

    void updateUserInfo(CImPdu* pPdu, uint32_t conn_uuid)
    {
    	IM::Buddy::IMUpdateUsersInfoReq req;
		IM::Buddy::IMUpdateUsersInfoRsp resp;
		if(req.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength())) {
			string user_id = req.user_id();
			IM::BaseDefine::UserInfo user_info = req.user_info();

			bool result = CUserModel::getInstance()->updateUser(user_id, user_info);

			uint32_t result_code = 1;
			resp.set_user_id(user_id);
			if (result) {
				result_code = 0;
				log("sucess, user_id=%s", user_id.c_str());
			} else {
				log("false, user_id=%s ", user_id.c_str());
			}

			resp.set_result_code(result_code);
			CImPdu* pdu_resp = new CImPdu();
			resp.set_attach_data(req.attach_data());
			pdu_resp->SetPBMsg(&resp);
			pdu_resp->SetSeqNum(pPdu->GetSeqNum());
			pdu_resp->SetServiceId(IM::BaseDefine::SID_BUDDY_LIST);
			pdu_resp->SetCommandId(IM::BaseDefine::CID_BUDDY_LIST_UPDATE_USER_INFO_RESPONSE);
			CProxyConn::AddResponsePdu(conn_uuid, pdu_resp);

		} else {
			log("changeUserSignInfo: IMChangeSignInfoReq ParseFromArray failed!!!");
		}

    }

    void doPushShield(CImPdu* pPdu, uint32_t conn_uuid) {
        IM::Login::IMPushShieldReq req;
        IM::Login::IMPushShieldRsp resp;
        if(req.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength())) {
            string user_id = req.user_id();
            uint32_t shield_status = req.shield_status();
            // const string& sign_info = req.sign_info();
            
            bool result = CUserModel::getInstance()->updatePushShield(user_id, shield_status);
            
            resp.set_user_id(user_id);
            resp.set_result_code(result ? 0 : 1);
            if (result) {
                resp.set_shield_status(shield_status);
                log("doPushShield sucess, user_id=%s, shield_status=%u", user_id.c_str(), shield_status);
            } else {
                log("doPushShield false, user_id=%s, shield_status=%u", user_id.c_str(), shield_status);
            }
            
            
            CImPdu* pdu_resp = new CImPdu();
            resp.set_attach_data(req.attach_data());
            pdu_resp->SetPBMsg(&resp);
            pdu_resp->SetSeqNum(pPdu->GetSeqNum());
            pdu_resp->SetServiceId(IM::BaseDefine::SID_LOGIN);
            pdu_resp->SetCommandId(IM::BaseDefine::CID_LOGIN_RES_PUSH_SHIELD);
            CProxyConn::AddResponsePdu(conn_uuid, pdu_resp);
            
        } else {
            log("doPushShield: IMPushShieldReq ParseFromArray failed!!!");
        }
    }
    
    void doQueryPushShield(CImPdu* pPdu, uint32_t conn_uuid) {
        IM::Login::IMQueryPushShieldReq req;
        IM::Login::IMQueryPushShieldRsp resp;
        if(req.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength())) {
            string user_id = req.user_id();
            uint32_t shield_status = 0;
            
            bool result = CUserModel::getInstance()->getPushShield(user_id, &shield_status);
            
            resp.set_user_id(user_id);
            resp.set_result_code(result ? 0 : 1);
            if (result) {
                resp.set_shield_status(shield_status);
                log("doQueryPushShield sucess, user_id=%s, shield_status=%u", user_id.c_str(), shield_status);
            } else {
                log("doQueryPushShield false, user_id=%s", user_id.c_str());
            }
            
            
            CImPdu* pdu_resp = new CImPdu();
            resp.set_attach_data(req.attach_data());
            pdu_resp->SetPBMsg(&resp);
            pdu_resp->SetSeqNum(pPdu->GetSeqNum());
            pdu_resp->SetServiceId(IM::BaseDefine::SID_LOGIN);
            pdu_resp->SetCommandId(IM::BaseDefine::CID_LOGIN_RES_QUERY_PUSH_SHIELD);
            CProxyConn::AddResponsePdu(conn_uuid, pdu_resp);
        } else {
            log("doQueryPushShield: IMQueryPushShieldReq ParseFromArray failed!!!");
        }
    }

};

