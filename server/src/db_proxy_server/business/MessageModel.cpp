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
#include "Base64.h"

using namespace std;

CMessageModel* CMessageModel::m_pInstance = NULL;
extern string strAudioEnc;

CMessageModel::CMessageModel()
{

}

CMessageModel::~CMessageModel()
{

}

CMessageModel* CMessageModel::getInstance()
{
	if (!m_pInstance) {
		m_pInstance = new CMessageModel();
	}

	return m_pInstance;
}

void CMessageModel::getMessage(string nUserId, string nPeerId, uint32_t nMsgId,
                               uint32_t nMsgCnt, list<IM::BaseDefine::MsgInfo>& lsMsg)
{
    uint32_t nRelateId = CRelationModel::getInstance()->getRelationId(nUserId, nPeerId, false);
	if (nRelateId != INVALID_VALUE)
    {
        CDBManager* pDBManager = CDBManager::getInstance();
        CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
        if (pDBConn)
        {
            string strTableName = "IMMessage_" + int2string(0);
            string strSql;
            if (nMsgId == 0) {
                strSql = "select * from " + strTableName + " force index (idx_relateId_status_created) where relateId= " + int2string(nRelateId) + " and status = 0 order by created desc, id desc limit " + int2string(nMsgCnt);
            }
            else
            {
                strSql = "select * from " + strTableName + " force index (idx_relateId_status_created) where relateId= " + int2string(nRelateId) + " and status = 0 and msgId <=" + int2string(nMsgId)+ " order by created desc, id desc limit " + int2string(nMsgCnt);
            }
            CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
            if (pResultSet)
            {
                while (pResultSet->Next())
                {
                    IM::BaseDefine::MsgInfo cMsg;
                    cMsg.set_msg_id(pResultSet->GetInt("msgId"));
                    cMsg.set_from_session_id(pResultSet->GetString("fromId"));
                    cMsg.set_create_time(pResultSet->GetInt("created"));
                    IM::BaseDefine::MsgType nMsgType = IM::BaseDefine::MsgType(pResultSet->GetInt("type"));
                    if(IM::BaseDefine::MsgType_IsValid(nMsgType))
                    {
                        cMsg.set_msg_type(nMsgType);
                        cMsg.set_msg_data(pResultSet->GetString("content"));
                        lsMsg.push_back(cMsg);
                    }
                    else
                    {
                        log("invalid msgType. userId=%s, peerId=%s, msgId=%u, msgCnt=%u, msgType=%u", nUserId.c_str(), nPeerId.c_str(), nMsgId, nMsgCnt, nMsgType);
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
	}
    else
    {
        log("no relation between %s and %s", nUserId.c_str(), nPeerId.c_str());
    }
}

/*
 * IMMessage 分表
 * AddFriendShip()
 * if nFromId or nToId is ShopEmployee
 * GetShopId
 * Insert into IMMessage_ShopId%8
 */
bool CMessageModel::sendMessage(uint32_t nRelateId, string nFromId, string nToId, IM::BaseDefine::MsgType nMsgType, uint32_t nCreateTime, uint32_t nMsgId, string& strMsgContent,const string & strSigData)
{
    bool bRet =false;
    if (nFromId == "" || nToId == "") {
        log("invalied userId.%s->%s", nFromId.c_str(), nToId.c_str());
        return bRet;
    }

	CDBManager* pDBManager = CDBManager::getInstance();
	CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
	if (pDBConn)
    {
        string strTableName = "IMMessage_" + int2string(0);
        string strSql = "insert into " + strTableName +
        		" (`relateId`, `fromId`, `toId`, `msgId`, `content`, `status`, `type`, `created`, `updated`,`contentSig`) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // 必须在释放连接前delete CPrepareStatement对象，否则有可能多个线程操作mysql对象，会crash
        CPrepareStatement* pStmt = new CPrepareStatement();
        std::string strHexSigData = HexStr(strSigData);
        if (pStmt->Init(pDBConn->GetMysql(), strSql))
        {
            uint32_t nStatus = 0;
            uint32_t nType = nMsgType;
            uint32_t index = 0;
            pStmt->SetParam(index++, nRelateId);
            pStmt->SetParam(index++, nFromId);
            pStmt->SetParam(index++, nToId);
            pStmt->SetParam(index++, nMsgId);
            pStmt->SetParam(index++, strMsgContent);
            pStmt->SetParam(index++, nStatus);
            pStmt->SetParam(index++, nType);
            pStmt->SetParam(index++, nCreateTime);
            pStmt->SetParam(index++, nCreateTime);
			pStmt->SetParam(index++, strHexSigData);
            bRet = pStmt->ExecuteUpdate();
        }
        delete pStmt;
        pDBManager->RelDBConn(pDBConn);
        if (bRet)
        {
            uint32_t nNow = (uint32_t) time(NULL);
            incMsgCount(nFromId, nToId);
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

bool CMessageModel::sendAudioMessage(uint32_t nRelateId, string nFromId, string nToId, IM::BaseDefine::MsgType nMsgType, uint32_t nCreateTime, uint32_t nMsgId, const char* pMsgContent, uint32_t nMsgLen)
{
	if (nMsgLen <= 4) {
		return false;
	}

	CAudioModel* pAudioModel = CAudioModel::getInstance();
	int nAudioId = pAudioModel->saveAudioInfo(nFromId, nToId, nCreateTime, pMsgContent, nMsgLen);

	bool bRet = true;
	if (nAudioId != -1) {
		string strMsg = int2string(nAudioId);
		bRet = sendMessage(nRelateId, nFromId, nToId, nMsgType, nCreateTime, nMsgId, strMsg,"");
	} else {
		bRet = false;
	}

	return bRet;
}

void CMessageModel::incMsgCount(string nFromId, string nToId)
{
	CacheManager* pCacheManager = CacheManager::getInstance();
	// increase message count
	CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
	if (pCacheConn) {
		pCacheConn->hincrBy("unread_" + (nToId), (nFromId), 1);
		pCacheManager->RelCacheConn(pCacheConn);
		
		log("increase unread count: %s->%s", nFromId.c_str(), nToId.c_str());//wystan add for debug 200611
	} else {
		log("no cache connection to increase unread count: %s->%s", nFromId.c_str(), nToId.c_str());
	}
}

void CMessageModel::getUnreadMsgCount(string nUserId, uint32_t &nTotalCnt, list<IM::BaseDefine::UnreadInfo>& lsUnreadCount)
{
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
    if (pCacheConn)
    {
        map<string, string> mapUnread;
        string strKey = "unread_" + (nUserId);
        bool bRet = pCacheConn->hgetAll(strKey, mapUnread);
        pCacheManager->RelCacheConn(pCacheConn);
        if(bRet)
        {
            IM::BaseDefine::UnreadInfo cUnreadInfo;
            for (auto it = mapUnread.begin(); it != mapUnread.end(); it++) {
            	//存的不是session_id吧，是from_id
                cUnreadInfo.set_session_id((it->first.c_str()));
                cUnreadInfo.set_unread_cnt(atoi(it->second.c_str()));
                cUnreadInfo.set_session_type(IM::BaseDefine::SESSION_TYPE_SINGLE);
                uint32_t nMsgId = 0;
                string strMsgData;
                IM::BaseDefine::MsgType nMsgType;
                getLastMsg(cUnreadInfo.session_id(), nUserId, nMsgId, strMsgData, nMsgType);
                if(IM::BaseDefine::MsgType_IsValid(nMsgType))
                {
                    cUnreadInfo.set_latest_msg_id(nMsgId);
                    cUnreadInfo.set_latest_msg_data(strMsgData);
                    cUnreadInfo.set_latest_msg_type(nMsgType);
                    cUnreadInfo.set_latest_msg_from_user_id(cUnreadInfo.session_id());
                    lsUnreadCount.push_back(cUnreadInfo);
                    nTotalCnt += cUnreadInfo.unread_cnt();
                }
                else
                {
                    log("invalid msgType. userId=%s, peerId=%s, msgType=%u", nUserId.c_str(), cUnreadInfo.session_id().c_str(), nMsgType);
                }
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
}

uint32_t CMessageModel::getMsgId(uint32_t nRelateId)
{
    uint32_t nMsgId = 0;
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
    if(pCacheConn)
    {
        string strKey = "msg_id_" + int2string(nRelateId);
        nMsgId = pCacheConn->incrBy(strKey, 1);
        pCacheManager->RelCacheConn(pCacheConn);
    }
    return nMsgId;
}

//好像client没有使用了
/**
 *  <#Description#>
 *
 *  @param nFromId    <#nFromId description#>
 *  @param nToId      <#nToId description#>
 *  @param nMsgId     <#nMsgId description#>
 *  @param strMsgData <#strMsgData description#>
 *  @param nMsgType   <#nMsgType description#>
 *  @param nStatus    0获取未被删除的，1获取所有的，默认获取未被删除的
 */
void CMessageModel::getLastMsg(string nFromId, string nToId, uint32_t& nMsgId, string& strMsgData, IM::BaseDefine::MsgType& nMsgType, uint32_t nStatus)
{
    uint32_t nRelateId = CRelationModel::getInstance()->getRelationId(nFromId, nToId, false);
    
    if (nRelateId != INVALID_VALUE)
    {
        CDBManager* pDBManager = CDBManager::getInstance();
        CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
        if (pDBConn)
        {
            string strTableName = "IMMessage_" + int2string(0);
            string strSql = "select msgId,type,content,contentSig from " + strTableName + " force index (idx_relateId_status_created) where relateId= " + int2string(nRelateId) + " and status = 0 order by created desc, id desc limit 1";
            CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
            if (pResultSet)
            {
                while (pResultSet->Next())
                {
                    nMsgId = pResultSet->GetInt("msgId");

                    nMsgType = IM::BaseDefine::MsgType(pResultSet->GetInt("type"));
                    if (nMsgType == IM::BaseDefine::MSG_TYPE_SINGLE_AUDIO)
                    {
                        // "[语音]"加密后的字符串
                        strMsgData = strAudioEnc;
                    }
                    else
                    {
                        strMsgData = pResultSet->GetString("content");
                    }
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
            log("no db connection_slave");
        }
    }
    else
    {
        log("no relation between %s and %s", nFromId.c_str(), nToId.c_str());
    }
}

void CMessageModel::getUnReadCntAll(string nUserId, uint32_t &nTotalCnt)
{
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
    if (pCacheConn)
    {
        map<string, string> mapUnread;
        string strKey = "unread_" + (nUserId);
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
}

void CMessageModel::getMsgByMsgId(string nUserId, string nPeerId, const list<uint32_t> &lsMsgId, list<IM::BaseDefine::MsgInfo> &lsMsg)
{
    if(lsMsgId.empty())
    {
        return ;
    }
    uint32_t nRelateId = CRelationModel::getInstance()->getRelationId(nUserId, nPeerId, false);

    if(nRelateId == INVALID_VALUE)
    {
        log("invalid relation id between %s and %s", nUserId.c_str(), nPeerId.c_str());
        return;
    }

    CDBManager* pDBManager = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
    if (pDBConn)
    {
        string strTableName = "IMMessage_" + int2string(0);
        string strClause ;
        bool bFirst = true;
        for(auto it= lsMsgId.begin(); it!=lsMsgId.end();++it)
        {
            if (bFirst) {
                bFirst = false;
                strClause = int2string(*it);
            }
            else
            {
                strClause += ("," + int2string(*it));
            }
        }

        string strSql = "select * from " + strTableName + " where relateId=" + int2string(nRelateId) + "  and status=0 and msgId in (" + strClause + ") order by created desc, id desc limit 100";
        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
        if (pResultSet)
        {
            while (pResultSet->Next())
            {
                IM::BaseDefine::MsgInfo msg;
                msg.set_msg_id(pResultSet->GetInt("msgId"));
                msg.set_from_session_id(pResultSet->GetString("fromId"));
                msg.set_create_time(pResultSet->GetInt("created"));
                IM::BaseDefine::MsgType nMsgType = IM::BaseDefine::MsgType(pResultSet->GetInt("type"));
                if(IM::BaseDefine::MsgType_IsValid(nMsgType))
                {
                    msg.set_msg_type(nMsgType);
                    msg.set_msg_data(pResultSet->GetString("content"));
                    lsMsg.push_back(msg);
                }
                else
                {
                    log("invalid msgType. userId=%s, peerId=%s msgType=%u, msgId=%u", nUserId.c_str(), nPeerId.c_str(), nMsgType, msg.msg_id());
                }
            }
            delete pResultSet;
        }
        else
        {
            log("no result set for sql:%s", strSql.c_str());
        }
        pDBManager->RelDBConn(pDBConn);
        if(!lsMsg.empty())
        {
            CAudioModel::getInstance()->readAudios(lsMsg);
        }
    }
    else
    {
        log("no db connection for teamtalk_slave");
    }
}

bool CMessageModel::resetMsgId(uint32_t nRelateId)
{
    bool bRet = false;
    uint32_t nMsgId = 0;
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
    if(pCacheConn)
    {
        string strKey = "msg_id_" + int2string(nRelateId);
        string strValue = "0";
        string strReply = pCacheConn->set(strKey, strValue);
        if(strReply == strValue)
        {
            bRet = true;
        }
        pCacheManager->RelCacheConn(pCacheConn);
    }
    return bRet;
}
