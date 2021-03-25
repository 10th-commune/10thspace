/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：MessageModel.cpp
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#include <map>
#include <set>

#include "../DBPool.h"
#include "../CachePool.h"
#include "MessageModel.h"
#include "AudioModel.h"
#include "SessionModel.h"
#include "RelationModel.h"
#include "SystemMsgModel.h"
#include "json/json.h"

using namespace std;

CSystemMsgModel* CSystemMsgModel::m_pInstance = NULL;

CSystemMsgModel::CSystemMsgModel()
{

}

CSystemMsgModel::~CSystemMsgModel()
{

}

CSystemMsgModel* CSystemMsgModel::getInstance()
{
	if (!m_pInstance) {
		m_pInstance = new CSystemMsgModel();
	}

	return m_pInstance;
}

void CSystemMsgModel::getSysMsgData(string user_id, IM::System::SysMsgType type, uint32_t msg_cnt,
		IM::System::IMGetSysMsgDataRsp& msgResp)
{
	CDBManager* pDBManager = CDBManager::getInstance();
	CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
	if (pDBConn)
	{
		//string strTableName = "IMMessage_" + int2string(nRelateId % 8);
		string strTableName = "IMSysMsg_0";
		string strSql;

		strSql = "select "+ strTableName + ".* from " + strTableName +
						" where toId=" + (user_id);

		if(type == IM::System::SYS_MSG_BUDDY){
			strSql += " and (type=";
			strSql += int2string(IM::System::ADD_FRIEND_REQUEST);
			strSql += " or type=";
			strSql += int2string(IM::System::ADD_FRIEND_AGREE);
			strSql += " or type=";
			strSql += int2string(IM::System::ADD_FRIEND_DISAGREE);
			strSql += ")";
		}else if(type == IM::System::SYS_MSG_GROUP){
			strSql += " and (type=";
			strSql += int2string(IM::System::ADD_GROUP_REQUEST);
			strSql += " or type=";
			strSql += int2string(IM::System::ADD_GROUP_AGREE);
			strSql += " or type=";
			strSql += int2string(IM::System::ADD_GROUP_DISAGREE);
			strSql += ")";
		}else if(type == IM::System::SYS_MSG_SYSTEM){
			strSql += " and (type=";
			strSql += int2string(IM::System::PUSH_SYSTEM_MSG);
			strSql += " or type=";
			strSql += int2string(IM::System::PUSH_INVITE_ADD_FRIEND_MSG);
			strSql += " or type=";
			strSql += int2string(IM::System::PUSH_INVITE_ADD_GROUP_MSG);
			strSql += ")";
		}

		strSql += " order by created desc, id desc limit ";
		strSql += int2string(msg_cnt);

		/*if (nMsgId == 0) {
			//第一次取时
			strSql = "select "+ strTableName + ".* from " + strTableName + " where toId= " + int2string(nUserId) + " and fromId=IMUser.id order by created desc, id desc limit " + int2string(nMsgCnt);
		}else{
			//客户端上传取到的最后一条消息，服务端将所有没取的一次返回。
			strSql = "select " + strTableName + ".* from " + strTableName + " where toId= " + int2string(nUserId) + " and msgId <=" + int2string(nMsgId)+ " and fromId=IMUser.id order by created desc, id desc limit " + int2string(nMsgCnt);
		}*/
		CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
		if (pResultSet)
		{
			while (pResultSet->Next())
			{
				IM::System::IMSysMsgData *pData = msgResp.add_msg_list();
				pData->set_from_id(pResultSet->GetString("fromId"));
				pData->set_to_id(pResultSet->GetString("toId"));
				IM::System::SysMsgOper nMsgType = IM::System::SysMsgOper(pResultSet->GetInt("type"));
				pData->set_type(nMsgType);
				pData->set_attach_data(pResultSet->GetString("content"));
			}
			delete pResultSet;
		}
		else
		{
			log("no result set: %s", strSql.c_str());
		}
		pDBManager->RelDBConn(pDBConn);
	}
	else
	{
		log("no db connection for teamtalk_slave");
	}

}


/*void CSystemMsgModel::getSystemMsg(uint32_t nUserId, uint32_t nMsgId,
                               uint32_t nMsgCnt, list<IM::BaseDefine::MsgInfo>& lsMsg)
{
	CDBManager* pDBManager = CDBManager::getInstance();
	CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
	if (pDBConn)
	{
		//string strTableName = "IMMessage_" + int2string(nRelateId % 8);
		string strTableName = "IMSysMsg_0";
		string strSql;

		if (nMsgId == 0) {
			//第一次取时
			strSql = "select "+ strTableName + ".* from " + strTableName + " where toId= " + int2string(nUserId) + " and fromId=IMUser.id order by created desc, id desc limit " + int2string(nMsgCnt);
		}
		else
		{
			//客户端上传取到的最后一条消息，服务端将所有没取的一次返回。
			strSql = "select " + strTableName + ".* from " + strTableName + " where toId= " + int2string(nUserId) + " and msgId <=" + int2string(nMsgId)+ " and fromId=IMUser.id order by created desc, id desc limit " + int2string(nMsgCnt);
		}
		CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
		if (pResultSet)
		{
			while (pResultSet->Next())
			{
				IM::BaseDefine::MsgInfo cMsg;
				cMsg.set_msg_id(pResultSet->GetInt("msgId"));
				cMsg.set_from_session_id(pResultSet->GetInt("fromId"));
				cMsg.set_create_time(pResultSet->GetInt("created"));
				IM::BaseDefine::MsgType nMsgType = IM::BaseDefine::MsgType(pResultSet->GetInt("type"));
				if(IM::BaseDefine::MsgType_IsValid(nMsgType))
				{
					cMsg.set_msg_type(nMsgType);

					Json::Value json_obj;

					json_obj["user_nick_name"] = pResultSet->GetString("nick");
					json_obj["avatar_url"] = pResultSet->GetString("avatar");
					json_obj["addition_msg"] = pResultSet->GetString("content");
					cMsg.set_msg_data(json_obj.toStyledString().c_str());
					lsMsg.push_back(cMsg);
				}
				else
				{
					log("invalid msgType. userId=%u, msgId=%u, msgCnt=%u, msgType=%u", nUserId, nMsgId, nMsgCnt, nMsgType);
				}
			}
			delete pResultSet;
		}
		else
		{
			log("no result set: %s", strSql.c_str());
		}
		pDBManager->RelDBConn(pDBConn);
		if (!lsMsg.empty())
		{
			CAudioModel::getInstance()->readAudios(lsMsg);
		}
	}
	else
	{
		log("no db connection for teamtalk_slave");
	}

}*/

/*
 * IMMessage 分表
 * AddFriendShip()
 * if nFromId or nToId is ShopEmployee
 * GetShopId
 * Insert into IMMessage_ShopId%8
 */
bool CSystemMsgModel::sendSystemMsg(string nFromId, string nToId,
		IM::System::SysMsgOper oper_type, uint32_t nMsgId,
		string& strMsgContent, uint32_t nStatus)
{
    bool bRet = false;

	CDBManager* pDBManager = CDBManager::getInstance();
	CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
	if (pDBConn)
    {
		uint32_t nNow = (uint32_t)time(NULL);

        //string strTableName = "IMSysMsg_" + int2string(nToId % 8);
		string strTableName = "IMSysMsg_0";
        string strSql = "insert into " + strTableName + " (`fromId`, `toId`, `msgId`, `content`, `status`, `type`, `created`, `updated`) values(?, ?, ?, ?, ?, ?, ?, ?)";
        // 必须在释放连接前delete CPrepareStatement对象，否则有可能多个线程操作mysql对象，会crash
        CPrepareStatement* pStmt = new CPrepareStatement();
        if (pStmt->Init(pDBConn->GetMysql(), strSql))
        {
            uint32_t index = 0;
            uint32_t operType = oper_type;
            //pStmt->SetParam(index++, 0);
            pStmt->SetParam(index++, nFromId);
            pStmt->SetParam(index++, nToId);
            pStmt->SetParam(index++, nMsgId);
            pStmt->SetParam(index++, strMsgContent);
            pStmt->SetParam(index++, nStatus);
            pStmt->SetParam(index++, operType);
            pStmt->SetParam(index++, nNow);
            pStmt->SetParam(index++, nNow);
            bRet = pStmt->ExecuteUpdate();

            log("insert system message");
        }
        delete pStmt;
        pDBManager->RelDBConn(pDBConn);
        if (bRet)
        {
        	if(oper_type == IM::System::ADD_FRIEND_REQUEST || oper_type == IM::System::ADD_FRIEND_AGREE ||
        			oper_type == IM::System::ADD_FRIEND_DISAGREE){
        		incMsgCount(nToId, IM::System::SYS_MSG_BUDDY);
        	}else if(oper_type == IM::System::ADD_GROUP_REQUEST || oper_type == IM::System::ADD_GROUP_AGREE ||
        			oper_type == IM::System::ADD_GROUP_DISAGREE){
        		incMsgCount(nToId, IM::System::SYS_MSG_GROUP);
        	}else{
        		incMsgCount(nToId, IM::System::SYS_MSG_SYSTEM);
        	}
            log("inc sys msg count");
        }
        else
        {
            log("insert message failed: %s", strSql.c_str());
        }
	}
    else
    {
        log("no db connection for teamtalk_master");
    }
	return bRet;
}

void CSystemMsgModel::incMsgCount(string nToId, IM::System::SysMsgType type)
{
	string strKey;
	if(type == IM::System::SYS_MSG_BUDDY){
		strKey = "unread_buddy_";
	}else if(type == IM::System::SYS_MSG_SYSTEM){
		strKey = "unread_system_";
	}else if(type == IM::System::SYS_MSG_GROUP){
		strKey = "unread_group_";
	}else{
		log("type error");
		return;
	}

	CacheManager* pCacheManager = CacheManager::getInstance();
	// increase message count
	CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
	if (pCacheConn) {
		pCacheConn->incrBy(strKey + (nToId), 1);
		pCacheManager->RelCacheConn(pCacheConn);
	} else {
		log("no cache connection to increase unread_sys count: %s", nToId.c_str());
	}
}

bool CSystemMsgModel::getUnreadSysMsgCount(string nUserId, IM::System::IMSysMsgUnreadCntRsp &msgResp)
{
	CacheManager* pCacheManager = CacheManager::getInstance();
	CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
    if (pCacheConn)
    {
    	IM::System::UnreadSysMsgCnt *pCnt = NULL;
		string strKey = "unread_buddy_" + (nUserId);
		pCnt = msgResp.add_unread_list();
		pCnt->set_type(IM::System::SYS_MSG_BUDDY);
		pCnt->set_count(atoi(pCacheConn->get(strKey).c_str()));

		strKey = "unread_group_" + (nUserId);
		pCnt = msgResp.add_unread_list();
		pCnt->set_type(IM::System::SYS_MSG_GROUP);
		pCnt->set_count(atoi(pCacheConn->get(strKey).c_str()));

		strKey = "unread_system_" + (nUserId);
		pCnt = msgResp.add_unread_list();
		pCnt->set_type(IM::System::SYS_MSG_SYSTEM);
		pCnt->set_count(atoi(pCacheConn->get(strKey).c_str()));

        pCacheManager->RelCacheConn(pCacheConn);
    }
    else
    {
        log("no cache connection for unread");
        return false;
    }
    return true;
}

/*void CSystemMsgModel::getUnReadCntAll(uint32_t nUserId, uint32_t &nTotalCnt)
{
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
    if (pCacheConn)
    {
        map<string, string> mapUnread;
        string strKey = "unread_sys_" + int2string(nUserId);
        bool bRet = pCacheConn->hgetAll(strKey, mapUnread);
        pCacheManager->RelCacheConn(pCacheConn);
        
        if(bRet)
        {
            for (auto it = mapUnread.begin(); it != mapUnread.end(); it++) {
                nTotalCnt += atoi(it->second.c_str());
            }
        }
        else
        {
            log("hgetall %s failed!", strKey.c_str());
        }
    }
    else
    {
        log("no cache connection for unread");
    }
}*/

void CSystemMsgModel::clearSysMsgCounter(string nUserId, IM::System::SysMsgType type)
{
	string strKey;
	if(type == IM::System::SYS_MSG_BUDDY){
		strKey = "unread_buddy_";
	}else if(type == IM::System::SYS_MSG_SYSTEM){
		strKey = "unread_system_";
	}else if(type == IM::System::SYS_MSG_GROUP){
		strKey = "unread_group_";
	}else{
		log("type error");
		return;
	}

	CacheManager* pCacheManager = CacheManager::getInstance();
	CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");

	if (pCacheConn)
	{
		if(type == IM::System::SYS_MSG_BUDDY || type == IM::System::SYS_MSG_GROUP){
			pCacheConn->del("unread_group_"+(nUserId));
			pCacheConn->del("unread_buddy_"+(nUserId));
		}else{
			strKey += (nUserId);
			bool nRet = pCacheConn->del(strKey);
			if(!nRet)
			{
				log("del failed %s", nUserId.c_str());
			}
		}

		pCacheManager->RelCacheConn(pCacheConn);
	}
	else
	{
		log("no cache connection for sys unread");
	}
}

void CSystemMsgModel::getStudyTime(list<string> lsIds, IM::System::StatisticsType stype, string sparam,
			uint32_t page, uint32_t page_size, IM::System::IMSysGetStudyTimeRsp& resp)
{
	CDBManager* pDBManager = CDBManager::getInstance();
	CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
	if (pDBConn)
	{
		string strTableName = "StudyTimeRecord";//"IMMessage_" + int2string(nRelateId % 8);

		string strClause;
		bool bFirst = true;
		for (auto it = lsIds.begin(); it!=lsIds.end(); ++it)
		{
			if(bFirst)
			{
				bFirst = false;
				strClause += (*it);
			}
			else
			{
				strClause += ("," + (*it));
			}
		}

		string strSql = "";
		if(lsIds.size() != 0){
			strSql = "select "+ strTableName + ".*,nick,avatar from " + strTableName +
				",IMUser where user_id=IMUser.id and user_id in(" + strClause + ")";
		}else{
			strSql = "select "+ strTableName + ".*,nick,avatar from " + strTableName +
				",IMUser where user_id=IMUser.id ";
		}

		if(stype == IM::System::DAY){
			strSql += " and date_format(create_time,'%Y-%m-%d')='" + sparam + "' ";

		}else if(stype == IM::System::MONTH){
			strSql += " and date_format(create_time,'%Y-%m')='" + sparam + "' ";

		}else if(stype == IM::System::YEAR){
			strSql += " and date_format(create_time,'%Y')='" + sparam + "' ";

		}

		strSql += " order by create_time,user_id desc limit "+ int2string(page*page_size)+","+int2string(page_size);

		log("%s",strSql.c_str());

		CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
		if (pResultSet)
		{
			while (pResultSet->Next())
			{
				IM::System::StudyTimeInfo* pInfo = resp.add_studytime_list();
				pInfo->set_user_id(pResultSet->GetString("user_id"));
				pInfo->set_nick_name(pResultSet->GetString("nick"));
				pInfo->set_avatar_url(pResultSet->GetString("avatar"));
				pInfo->set_start(pResultSet->GetInt("start"));
				pInfo->set_duration(pResultSet->GetInt("duration"));
			}
			delete pResultSet;
		}
		else
		{
			log("no result set: %s", strSql.c_str());
		}
		pDBManager->RelDBConn(pDBConn);
	}
	else
	{
		log("no db connection for teamtalk_slave");
	}
}

