#include "../ProxyConn.h"
#include "SystemMsg.h"
#include "public_define.h"
#include "UserModel.h"
#include "SystemMsgModel.h"
#include "RelationModel.h"
#include "json/json.h"
#include "GroupModel.h"
#include "IM.BaseDefine.pb.h"
#include "IM.System.pb.h"

namespace DB_PROXY {

	void sendSysMsg(CImPdu* pPdu, uint32_t conn_uuid)
	{
		IM::System::IMSendSysMsgReq msg;
		IM::System::IMSendSysMsgRsp msgResp;
		if(!msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
		{
			log("parse pb failed");
			return;
		}

		CImPdu* pPduRes = new CImPdu;

		IM::System::IMSysMsgData msgData = msg.sys_msg();

		if(msgData.type() == IM::System::ADD_FRIEND_REQUEST){
			addFriend(msg, msgResp);

		}else if(msgData.type() == IM::System::ADD_GROUP_REQUEST){
			addGroup(msg, msgResp);

		}else if(msgData.type() == IM::System::ADD_FRIEND_AGREE){
			agreeAddFriend(msg, msgResp);

		}else if(msgData.type() == IM::System::ADD_GROUP_AGREE){
			agreeAddGroup(msg, msgResp);

		}

		msgResp.set_attach_data(msg.attach_data());
		pPduRes->SetPBMsg(&msgResp);
		pPduRes->SetSeqNum(pPdu->GetSeqNum());
		pPduRes->SetServiceId(IM::BaseDefine::SID_SYS_MSG);
		pPduRes->SetCommandId(IM::BaseDefine::CID_SYS_MSG_SEND_DATA_RESPONSE);
		CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
	}

	void addFriend(IM::System::IMSendSysMsgReq &msg, IM::System::IMSendSysMsgRsp &msgResp)
	{
		string nUserId = msg.user_id();
		IM::System::IMSysMsgData msgData = msg.sys_msg();
		string nFriendId = msgData.to_id();
		log("%s add friend %s", nUserId.c_str(), nFriendId.c_str());

		uint32_t nRetCode = 0;

		DBUserInfo_t cUser;
		CUserModel* pUserModel = CUserModel::getInstance();
		if(!pUserModel->getUserById(nFriendId, cUser)){
			//要加的用户不存在
			nRetCode = 2;
		}

		std::string jsonStr = "";

		if(nRetCode == 0){
			if(pUserModel->getUserById(nUserId, cUser)){
				Json::Value jsonObj;

				jsonObj["nick_name"] = cUser.strNick.c_str();
				jsonObj["avatar_url"] = cUser.strAvatar.c_str();
				jsonObj["addition_msg"] = msgData.attach_data();
				jsonStr = jsonObj.toStyledString();

				CSystemMsgModel* pSysModel = CSystemMsgModel::getInstance();
				pSysModel->sendSystemMsg(nUserId, nFriendId, IM::System::ADD_FRIEND_REQUEST, 0, jsonStr, 0);
			}else{
				nRetCode = 3;
			}
		}

		//log("userId=%u,nLastUpdate=%u, last_time=%u, userCnt=%u", nReqId,nLastUpdate, nLastTime, msgResp.user_list_size());
		msgResp.set_user_id(nUserId);
		msgResp.set_result_code(nRetCode); //0:成功 1:此用户不存在  2:已经添加了 3:出错
		IM::System::IMSysMsgData *pSysMsg = msgResp.mutable_sys_msg();
		pSysMsg->set_from_id(nUserId);
		pSysMsg->set_to_id(nFriendId);
		pSysMsg->set_type(msgData.type());
		pSysMsg->set_attach_data(jsonStr);

	}

	void agreeAddFriend(IM::System::IMSendSysMsgReq &msg, IM::System::IMSendSysMsgRsp &msgResp)
	{
		string nUserId = msg.user_id();
		IM::System::IMSysMsgData msgData = msg.sys_msg();
		string nFriendId = msgData.to_id();
		IM::System::SysMsgOper nAgree = msgData.type();

		log("%s agree %s add friend", nUserId.c_str(), nFriendId.c_str());

		uint32_t nRetCode = 3;

		DBUserInfo_t cUser;
		string jsonStr = "";

		CUserModel* pUserModel = CUserModel::getInstance();
		if(!pUserModel->getUserById(nUserId, cUser)){
			nRetCode = 1;

		}else if(nAgree == IM::System::ADD_FRIEND_AGREE){
			//同意添加为好友
			CRelationModel::getInstance()->addRelation(nUserId, nFriendId, RELATION_ACTION_ADD_FRIEND);

			Json::Value jsonObj;

			jsonObj["nick_name"] = cUser.strNick.c_str();
			jsonObj["avatar_url"] = cUser.strAvatar.c_str();
			jsonObj["addition_msg"] = msgData.attach_data();
			jsonStr = jsonObj.toStyledString();

			CSystemMsgModel* pSysModel = CSystemMsgModel::getInstance();
			pSysModel->sendSystemMsg(nUserId, nFriendId, nAgree, 0, jsonStr, 0);

			nRetCode = 0;
		}

		//
		msgResp.set_user_id(nUserId);
		msgResp.set_result_code(nRetCode); //0:同意加好友 1:此用户不存在  2:已经添加了 3:出错
		IM::System::IMSysMsgData *pSysMsg = msgResp.mutable_sys_msg();
		pSysMsg->set_from_id(nUserId);
		pSysMsg->set_to_id(nFriendId);
		pSysMsg->set_type(msgData.type());
		pSysMsg->set_attach_data(jsonStr);

	}

	void addGroup(IM::System::IMSendSysMsgReq &msg, IM::System::IMSendSysMsgRsp &msgResp)
	{
		uint32_t nRetCode = 0;
		string nGroupOwnerId;
		string nUserId = msg.user_id();
		IM::System::IMSysMsgData msgData = msg.sys_msg();
		string nGroupId = msgData.to_id();

		log("%s add group %s", nUserId.c_str(), nGroupId.c_str());

		if("" == (nGroupOwnerId = CGroupModel::getInstance()->getGroupOwner(nGroupId))){
			//group id不存在
			nRetCode = 1;
		}

		std::string jsonStr = "";

		DBUserInfo_t cUser;
		CUserModel* pUserModel = CUserModel::getInstance();
		if(nRetCode == 0){
			if(pUserModel->getUserById(nUserId, cUser)){

				Json::Value jsonObj;

				jsonObj["group_id"] = nGroupId.c_str();
				jsonObj["nick_name"] = cUser.strNick.c_str();
				jsonObj["avatar_url"] = cUser.strAvatar.c_str();
				jsonObj["addition_msg"] = msgData.attach_data();
				jsonStr = jsonObj.toStyledString();

				CSystemMsgModel* pSysModel = CSystemMsgModel::getInstance();
				pSysModel->sendSystemMsg(nUserId, nGroupOwnerId, IM::System::ADD_GROUP_REQUEST, 0, jsonStr, 0);
			}else{
				nRetCode = 2;
			}
		}

		//log("userId=%u,nLastUpdate=%u, last_time=%u, userCnt=%u", nReqId,nLastUpdate, nLastTime, msgResp.user_list_size());
		msgResp.set_user_id(nUserId);
		msgResp.set_result_code(nRetCode); //0:成功 1:已经添加了 2:此用户不存在
		IM::System::IMSysMsgData *pSysMsg = msgResp.mutable_sys_msg();
		pSysMsg->set_from_id(nUserId);
		pSysMsg->set_to_id(nGroupOwnerId);
		pSysMsg->set_type(msgData.type());
		pSysMsg->set_attach_data(jsonStr);
	}

	void agreeAddGroup(IM::System::IMSendSysMsgReq &msg, IM::System::IMSendSysMsgRsp &msgResp)
	{
		string nUserId = msg.user_id();
		IM::System::IMSysMsgData msgData = msg.sys_msg();
		string nGroupId = msgData.from_id();
		string nNewMemberId = msgData.to_id();
		IM::System::SysMsgOper nAgree = msgData.type();

		uint32_t nRetCode = 3;

		log("%s agree %s add to group %s", nUserId.c_str(), nNewMemberId.c_str(), nGroupId.c_str());

		DBUserInfo_t cUser;
		string jsonStr = "";

		if("" == CGroupModel::getInstance()->getGroupOwner(nUserId)){
			//group id不存在
			nRetCode = 1;
		}

		CUserModel* pUserModel = CUserModel::getInstance();
		if(!pUserModel->getUserById(nUserId, cUser)){
			nRetCode = 1;

		}else if(nAgree == IM::System::ADD_GROUP_AGREE){
			//同意添加进群
			set<string> setUserId;
			setUserId.insert(nNewMemberId);
			list<string> lsCurUserId;
			CGroupModel::getInstance()->modifyGroupMember(nUserId, nGroupId,
					IM::BaseDefine::GROUP_MODIFY_TYPE_ADD, setUserId, lsCurUserId, 0);

			Json::Value jsonObj;

			jsonObj["group_id"] = nGroupId;
			jsonObj["nick_name"] = cUser.strNick.c_str();
			jsonObj["avatar_url"] = cUser.strAvatar.c_str();
			jsonObj["addition_msg"] = msgData.attach_data();
			jsonStr = jsonObj.toStyledString();

			CSystemMsgModel* pSysModel = CSystemMsgModel::getInstance();
			pSysModel->sendSystemMsg(nUserId, nNewMemberId, nAgree, 0, jsonStr, 0);

			nRetCode = 0;
		}

		//log("userId=%u,nLastUpdate=%u, last_time=%u, userCnt=%u", nReqId,nLastUpdate, nLastTime, msgResp.user_list_size());
		msgResp.set_user_id(nUserId);
		msgResp.set_result_code(nRetCode); //0:同意加好友 1:此用户不存在  2:已经添加了 3:出错
		IM::System::IMSysMsgData *pSysMsg = msgResp.mutable_sys_msg();
		pSysMsg->set_from_id(nUserId);
		pSysMsg->set_to_id(nNewMemberId);
		pSysMsg->set_type(msgData.type());
		pSysMsg->set_attach_data(jsonStr);
	}

	void getUnreadSysMsgData(CImPdu* pPdu, uint32_t conn_uuid)
	{
		IM::System::IMGetSysMsgDataReq msg;
		IM::System::IMGetSysMsgDataRsp msgResp;
		if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
		{
			CImPdu* pPduRes = new CImPdu;

			string nUserId = msg.user_id();
			IM::System::SysMsgType msgType = msg.type();
			uint32_t cnt = msg.msg_cnt();

			CSystemMsgModel::getInstance()->getSysMsgData(nUserId, msgType, cnt, msgResp);

			msgResp.set_user_id(nUserId);
			msgResp.set_attach_data(msg.attach_data());
			pPduRes->SetPBMsg(&msgResp);
			pPduRes->SetSeqNum(pPdu->GetSeqNum());
			pPduRes->SetServiceId(IM::BaseDefine::SID_SYS_MSG);
			pPduRes->SetCommandId(IM::BaseDefine::CID_SYS_MSG_GET_DATA_RESPONSE);
			CProxyConn::AddResponsePdu(conn_uuid, pPduRes);
		}
		else
		{
			//log("parse pb failed");
		}
	}

	void getUnreadSysMsgCnt(CImPdu* pPdu, uint32_t conn_uuid)
	{
		IM::System::IMSysMsgUnreadCntReq msg;
		IM::System::IMSysMsgUnreadCntRsp msgResp;
		if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
		{
			CImPdu* pPduResp = new CImPdu;

			string nUserId = msg.user_id();
			uint32_t nTotalCnt = 0;
			log("getUnreadSysMsgCount");
			CSystemMsgModel::getInstance()->getUnreadSysMsgCount(nUserId, msgResp);

			msgResp.set_user_id(nUserId);
			msgResp.set_attach_data(msg.attach_data());
			pPduResp->SetPBMsg(&msgResp);
			pPduResp->SetSeqNum(pPdu->GetSeqNum());
			pPduResp->SetServiceId(IM::BaseDefine::SID_SYS_MSG);
			pPduResp->SetCommandId(IM::BaseDefine::CID_SYS_MSG_GET_UNREAD_CNT_RESPONSE);
			CProxyConn::AddResponsePdu(conn_uuid, pPduResp);
			log("CID_SYS_MSG_GET_UNREAD_CNT_RESPONSE pdu");

		}
		else
		{
			log("parse pb failed");
		}
	}

	void clearSysMsgCounter(CImPdu* pPdu, uint32_t conn_uuid)
	{
		IM::System::IMSysMsgReadDataAck msg;
		if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
		{
			string nUserId = msg.user_id();
			CSystemMsgModel::getInstance()->clearSysMsgCounter(nUserId, msg.type());
		}
		else
		{
			log("parse pb failed");
		}
	}

	void getStudyTime(CImPdu* pPdu, uint32_t conn_uuid)
	{
		IM::System::IMSysGetStudyTimeReq msg;
		IM::System::IMSysGetStudyTimeRsp msgResp;
		if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
		{
			IM::System::StatisticsType sType = msg.statistics_type();
			string sParam = msg.statistics_param();
			uint32_t nPage = msg.page();
			uint32_t nPageSize = msg.page_size();

			list<string> lsIds;

			uint32_t userCount = msg.target_user_id_list_size();
			std::list<string> idList;
			for(uint32_t i = 0; i < userCount; ++i) {
				idList.push_back(msg.target_user_id_list(i));
			}

			CImPdu* pPduResp = new CImPdu;

			CSystemMsgModel::getInstance()->getStudyTime(idList, sType, sParam, nPage, nPageSize, msgResp);

			msgResp.set_user_id(msg.user_id());
			msgResp.set_attach_data(msg.attach_data());
			pPduResp->SetPBMsg(&msgResp);
			pPduResp->SetSeqNum(pPdu->GetSeqNum());
			pPduResp->SetServiceId(IM::BaseDefine::SID_SYS_MSG);
			pPduResp->SetCommandId(IM::BaseDefine::CID_SYS_GET_STUDY_TIME_RESPONSE);
			CProxyConn::AddResponsePdu(conn_uuid, pPduResp);

		}
		else
		{
			log("parse pb failed");
		}

	}

};


