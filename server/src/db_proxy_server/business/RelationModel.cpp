/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：RelationModel.cpp
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#include <vector>

#include "../DBPool.h"
#include "RelationModel.h"
#include "MessageModel.h"
#include "GroupMessageModel.h"
#include "UserModel.h"
using namespace std;

CRelationModel* CRelationModel::m_pInstance = NULL;

CRelationModel::CRelationModel()
{

}

CRelationModel::~CRelationModel()
{

}

CRelationModel* CRelationModel::getInstance()
{
	if (!m_pInstance) {
		m_pInstance = new CRelationModel();
	}

	return m_pInstance;
}

/**
 *  获取会话关系ID
 *  对于群组，必须把nUserBId设置为群ID
 *
 *  @param nUserAId  <#nUserAId description#>
 *  @param nUserBId  <#nUserBId description#>
 *  @param bAdd      <#bAdd description#>
 *  @param nStatus 0 获取未被删除会话，1获取所有。
 */
uint32_t CRelationModel::getRelationId(string nUserAId, string nUserBId, bool bAdd)
{
    log("debug");
    uint32_t nRelationId = INVALID_VALUE;
    if (nUserAId == "" || nUserBId == "") {
        log("invalied user id:%s->%s", nUserAId.c_str(), nUserBId.c_str());
        return nRelationId;
    }
    CDBManager* pDBManager = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
    if (pDBConn)
    {
        string nBigId = nUserAId > nUserBId ? nUserAId : nUserBId;
        string nSmallId = nUserAId > nUserBId ? nUserBId : nUserAId;
        string strSql = "select id from IMRelationShip where smallId='" + (nSmallId) + "' and bigId='"+ (nBigId) + "' and status = 0";
        
        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
        if (pResultSet)
        {
            while (pResultSet->Next())
            {
                nRelationId = pResultSet->GetInt("id");
            }
            delete pResultSet;
        }
        else
        {
            log("there is no result for sql:%s", strSql.c_str());
        }
        pDBManager->RelDBConn(pDBConn);
        if (nRelationId == INVALID_VALUE && bAdd)
        {
        	//这里可能是用于临时对话，这样对addRelation的修改可能有问题
        	//先注掉
            //nRelationId = addRelation(nUserAId, nUserBId, RELATION_ACTION_ADD_FOLLOW);
        }
    }
    else
    {
        log("no db connection for teamtalk_slave");
    }
    return nRelationId;
}


uint32_t CRelationModel::addRelation(string nFromUserId, string nToUserId, uint32_t tag)
{
	bool bRet = false;
	string nBigId = nFromUserId > nToUserId ? nFromUserId : nToUserId;
	string nSmallId = nFromUserId > nToUserId ? nToUserId : nFromUserId;

	uint32_t status = -1, status2 = -2;
    uint32_t nRelationId = INVALID_VALUE;
    CDBManager* pDBManager = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
    if (pDBConn)
    {
        uint32_t nTimeNow = (uint32_t)time(NULL);
        string strSql = "select id,status from IMRelationShip where smallId='" + (nSmallId) + "' and bigId='"+ (nBigId)+"'";
        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
        if(pResultSet && pResultSet->Next())
        {
            nRelationId = pResultSet->GetInt("id");
            status = pResultSet->GetInt("status");
            delete pResultSet;

            //a.已是好友 status不变
            if(tag == RELATION_ACTION_ADD_FRIEND){
            	status2 = RELATION_TYPE_FRIEND;
            }else if (tag == RELATION_ACTION_ADD_FOLLOW){
            	if((status == RELATION_TYPE_FOLLOW_BIG && nSmallId == nToUserId) || (status == RELATION_TYPE_FOLLOW_SMALL && nBigId == nToUserId)){
            		status2 = RELATION_TYPE_FOLLOW_EACH_OTHER;
            	}
            }
            if(-2 != status2 && status2 != status){
            	strSql = "update IMRelationShip set status=" + int2string(status2) + ", updated=" + int2string(nTimeNow) + " where id=" + int2string(nRelationId);
				bRet = pDBConn->ExecuteUpdate(strSql.c_str());
				if(!bRet)
				{
					nRelationId = INVALID_VALUE;
				}
				log("has relation ship set %u", status2);
            }

        }
        else
        {
        	if(tag == RELATION_ACTION_ADD_FRIEND){
        	    status2 = RELATION_TYPE_FRIEND;
        	}else if (tag == RELATION_ACTION_ADD_FOLLOW){
        	    if(nFromUserId ==  nSmallId){
        	        status2 = RELATION_TYPE_FOLLOW_BIG;
        	    }else{
        	    	status2 = RELATION_TYPE_FOLLOW_SMALL;
        	    }
        	}
            strSql = "insert into IMRelationShip (`smallId`,`bigId`,`status`,`created`,`updated`) values(?,?,?,?,?)";
            // 必须在释放连接前delete CPrepareStatement对象，否则有可能多个线程操作mysql对象，会crash
            CPrepareStatement* stmt = new CPrepareStatement();
            if (stmt->Init(pDBConn->GetMysql(), strSql))
            {
                uint32_t index = 0;
                stmt->SetParam(index++, nSmallId);
                stmt->SetParam(index++, nBigId);
                stmt->SetParam(index++, status2);
                stmt->SetParam(index++, nTimeNow);
                stmt->SetParam(index++, nTimeNow);
                bRet = stmt->ExecuteUpdate();
                if (bRet)
                {
                    nRelationId = pDBConn->GetInsertId();
                }
                else
                {
                    log("insert message failed. %s", strSql.c_str());
                }
            }
            if(nRelationId != INVALID_VALUE)
            {
                // 初始化msgId
                if(tag == RELATION_ACTION_ADD_FRIEND && !CMessageModel::getInstance()->resetMsgId(nRelationId))
                {
                    log("reset msgId failed. smallId=%s, bigId=%s.", nSmallId.c_str(), nBigId.c_str());
                }
            }
            delete stmt;
        }

        pDBManager->RelDBConn(pDBConn);

        //更新 fans count
		if(-2 != status2 && status2 != status && tag == RELATION_ACTION_ADD_FOLLOW){
			CUserModel::getInstance()->updateFansCnt(nToUserId);
		}
    }
    else
    {
        log("no db connection for teamtalk_master");
    }
    return nRelationId;
}

//好像没有使用
bool CRelationModel::updateRelation(uint32_t nRelationId, uint32_t nUpdateTime)
{
    bool bRet = false;
    CDBManager* pDBManager = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
    if (pDBConn)
    {
        string strSql = "update IMRelationShip set `updated`="+int2string(nUpdateTime) + " where id="+int2string(nRelationId);
        bRet = pDBConn->ExecuteUpdate(strSql.c_str());
        pDBManager->RelDBConn(pDBConn);
    }
    else
    {
        log("no db connection for teamtalk_master");
    }
    return bRet;
}

//好像没有使用
bool CRelationModel::removeRelation(uint32_t nRelationId)
{
    bool bRet = false;
    CDBManager* pDBManager = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
    if (pDBConn)
    {
        uint32_t nNow = (uint32_t) time(NULL);
        string strSql = "update IMRelationShip set status = 1, updated="+int2string(nNow)+" where id=" + int2string(nRelationId);
        bRet = pDBConn->ExecuteUpdate(strSql.c_str());
        pDBManager->RelDBConn(pDBConn);
    }
    else
    {
        log("no db connection for teamtalk_master");
    }
    return bRet;
}

bool CRelationModel::delFriend(string nUserId, string nFriendId)
{
	bool bRet = false;
	string nBigId = nUserId > nFriendId ? nUserId : nFriendId;
	string nSmallId = nUserId > nFriendId ? nFriendId : nUserId;

	CDBManager* pDBManager = CDBManager::getInstance();
	CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
	if (pDBConn)
	{
		uint32_t nNow = (uint32_t) time(NULL);
		string strSql = "delete from IMRelationShip where smallId='" + (nSmallId) +
				"' and bigId='" + (nBigId) + "' and status=" + int2string(RELATION_TYPE_FRIEND);
		bRet = pDBConn->ExecuteDelete(strSql.c_str());
		pDBManager->RelDBConn(pDBConn);
	}
	else
	{
		log("no db connection for teamtalk_master");
	}
	return bRet;

}

bool CRelationModel::delFollowUser(string nUserId, string nFriendId)
{
	bool bRet = false;
	string nBigId = nUserId > nFriendId ? nUserId : nFriendId;
	string nSmallId = nUserId > nFriendId ? nFriendId : nUserId;
	uint32_t nRelationId = 0;
	string strSql2 = "";

	CDBManager* pDBManager = CDBManager::getInstance();
	CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
	if (pDBConn)
	{
		uint32_t status = -1;
		string strSql = "select * from IMRelationShip where smallId='" + (nSmallId) + "' and bigId='"+ (nBigId)+"'";
		CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
		if(pResultSet && pResultSet->Next())
		{
			nRelationId = pResultSet->GetInt("id");
			status = pResultSet->GetInt("status");
			delete pResultSet;

			if((status == RELATION_TYPE_FOLLOW_BIG && nBigId == nFriendId) ||
					(status == RELATION_TYPE_FOLLOW_SMALL && nSmallId == nFriendId)){
				strSql2 = "delete from IMRelationShip where id=" + (nRelationId);
			}

			if(strSql2 == "" && status == RELATION_TYPE_FOLLOW_EACH_OTHER){
				if(nFriendId == nSmallId){
					strSql2 = "update IMRelationShip set status=" + int2string(RELATION_TYPE_FOLLOW_BIG) +
							" where id=" + int2string(nRelationId);
				}else{
					strSql2 = "update IMRelationShip set status=" + int2string(RELATION_TYPE_FOLLOW_SMALL) +
							" where id=" + int2string(nRelationId);
				}
			}

			if(strSql2 != ""){
				bRet = pDBConn->ExecuteUpdate(strSql2.c_str());
			}else{
				log("no match operator userid:%s friendid:%s",nUserId.c_str(), nFriendId.c_str());
			}
		}else{
			log("no relationship userid:%s friendid:%s",nUserId.c_str(), nFriendId.c_str());
		}

		pDBManager->RelDBConn(pDBConn);

		if(bRet){
		    CUserModel::getInstance()->updateFansCnt(nFriendId);
		}
	}
	else
	{
		log("no db connection for teamtalk_master");
	}
	return bRet;

}

