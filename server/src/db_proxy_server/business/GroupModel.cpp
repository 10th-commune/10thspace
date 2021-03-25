/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：GroupModel.cpp
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#include "../DBPool.h"
#include "../CachePool.h"

#include "GroupModel.h"
#include "ImPduBase.h"
#include "Common.h"
#include "AudioModel.h"
#include "UserModel.h"
#include "GroupMessageModel.h"
#include "public_define.h"
#include "SessionModel.h"
#include "InterLogin.h"

CGroupModel* CGroupModel::m_pInstance = NULL;

/**
 *  <#Description#>
 */
CGroupModel::CGroupModel()
{
    
}

CGroupModel::~CGroupModel()
{
    
}

CGroupModel* CGroupModel::getInstance()
{
    if (!m_pInstance) {
        m_pInstance = new CGroupModel();
    }
    return m_pInstance;
}

/**
 *  创建群
 *
 *  @param nUserId        创建者
 *  @param strGroupName   群名
 *  @param strGroupAvatar 群头像
 *  @param nGroupType     群类型1,固定群;2,临时群
 *  @param setMember      群成员列表，为了踢出重复的userId，使用set存储
 *
 *  @return 成功返回群Id，失败返回0;
 */
string CGroupModel::createGroup(string nUserId, const string& strGroupName, const string& strGroupAvatar, uint32_t nGroupType, set<string>& setMember)
{
    string nGroupId = "";
    do {
        if(strGroupName.empty()) {
            break;
        }
        if (setMember.empty()) {
            break;
        }
        // remove repeat user
        
        
        //insert IMGroup
        if(!insertNewGroup(nUserId, strGroupName, strGroupAvatar, nGroupType, (uint32_t)setMember.size(), nGroupId)) {
            break;
        }
        bool bRet = CGroupMessageModel::getInstance()->resetMsgId(nGroupId);
        if(!bRet)
        {
            log("reset msgId failed. groupId=%s", nGroupId.c_str());
        }
        
        //insert IMGroupMember
        clearGroupMember(nGroupId);
        insertNewMember(nGroupId, setMember);
        
    } while (false);
    
    return nGroupId;
}

bool CGroupModel::removeGroup(string nUserId, string nGroupId, list<string>& lsCurUserId)
{
    bool bRet = false;
    CDBManager* pDBManager = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
    set<string> setGroupUsers;
    if(pDBConn)
    {
        string strSql = "select creator from IMGroup where id='"+(nGroupId)+"'";
        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
        if(pResultSet)
        {
            string nCreator;
            while (pResultSet->Next()) {
                nCreator = pResultSet->GetString("creator");
            }
            
            if("" == nCreator || nCreator == nUserId)
            {
                //设置群组不可用。
                strSql = "update IMGroup set status=0 where id='"+(nGroupId)+"'";
                bRet = pDBConn->ExecuteUpdate(strSql.c_str());
            }
            delete  pResultSet;
        }
        
        if (bRet) {
            strSql = "select userId from IMGroupMember where groupId='"+(nGroupId)+"'";
            CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
            if(pResultSet)
            {
                while (pResultSet->Next()) {
                    string nId = pResultSet->GetString("userId");
                    setGroupUsers.insert(nId);
                }
                delete pResultSet;
            }
        }
        pDBManager->RelDBConn(pDBConn);
    }
    
    if(bRet)
    {
        bRet = removeMember(nGroupId, setGroupUsers, lsCurUserId);
    }
    
    return bRet;
}


void CGroupModel::getUserGroup(string nUserId, list<IM::BaseDefine::GroupVersionInfo>& lsGroup, uint32_t nGroupType)
{
    list<string> lsGroupId;
    getUserGroupIds(nUserId, lsGroupId,0);
    if(lsGroupId.size() != 0)
    {
        getGroupVersion(lsGroupId, lsGroup, nGroupType);
    }
}


void CGroupModel::getGroupInfo(map<string,IM::BaseDefine::GroupVersionInfo>& mapGroupId, list<IM::BaseDefine::GroupInfo>& lsGroupInfo)
{
   if (!mapGroupId.empty())
    {
        CDBManager* pDBManager = CDBManager::getInstance();
        CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
        if (pDBConn)
        {
            string strClause;
            bool bFirst = true;
            for(auto it=mapGroupId.begin(); it!=mapGroupId.end(); ++it)
            {
                if(bFirst)
                {
                    bFirst = false;
                    strClause = (it->first);
                }
                else
                {
                    strClause += ("','" + (it->first));
                }
            }
            string strSql = "select * from IMGroup where id in ('" + strClause  + "') order by updated desc";
            CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
            if(pResultSet)
            {
                while (pResultSet->Next()) {
                    string nGroupId = pResultSet->GetString("id");
                    uint32_t nVersion = pResultSet->GetInt("version");
               if(mapGroupId[nGroupId].version() < nVersion)
                    {
                        IM::BaseDefine::GroupInfo cGroupInfo;
                        cGroupInfo.set_group_id(nGroupId);
                        cGroupInfo.set_version(nVersion);
                        cGroupInfo.set_group_name(pResultSet->GetString("name"));
                        cGroupInfo.set_group_avatar(pResultSet->GetString("avatar"));
                        IM::BaseDefine::GroupType nGroupType = IM::BaseDefine::GroupType(pResultSet->GetInt("type"));
                        if(IM::BaseDefine::GroupType_IsValid(nGroupType))
                        {
                            cGroupInfo.set_group_type(nGroupType);
                            cGroupInfo.set_group_creator_id(pResultSet->GetString("creator"));
                            lsGroupInfo.push_back(cGroupInfo);
                        }
                        else
                        {
                            log("invalid groupType. groupId=%s, groupType=%u", nGroupId.c_str(), nGroupType);
                        }
                    }
                }
                delete pResultSet;
            }
            else
            {
                log("no result set for sql:%s", strSql.c_str());
            }
            pDBManager->RelDBConn(pDBConn);
       if(!lsGroupInfo.empty())
            {
                fillGroupMember(lsGroupInfo);
            }
        }
        else
        {
            log("no db connection for teamtalk_slave");
        }
    }
    else
    {
        log("no ids in map");
    }
}

bool CGroupModel::modifyGroupMember(string nUserId, string nGroupId,
		IM::BaseDefine::GroupModifyType nType, set<string>& setUserId,
		list<string>& lsCurUserId, uint32_t quit)
{
    bool bRet = false;
    auto iter = setUserId.begin();
    if(hasModifyPermission(nUserId, nGroupId, nType) ||
    		(quit == 0x22 && setUserId.size() == 1 && *iter == nUserId))
    {
        switch (nType) {
            case IM::BaseDefine::GROUP_MODIFY_TYPE_ADD:
                bRet = addMember(nGroupId, setUserId, lsCurUserId);
                break;
            case IM::BaseDefine::GROUP_MODIFY_TYPE_DEL:
                bRet = removeMember(nGroupId, setUserId, lsCurUserId);
                removeSession(nGroupId, setUserId);
                break;
            default:
                log("unknown while");
        }
        //if modify group member success, need to inc the group version and clear the user count;
        if(bRet)
        {
            incGroupVersion(nGroupId);
            for (auto it=setUserId.begin(); it!=setUserId.end(); ++it) {
                string nUserId=*it;
                CUserModel::getInstance()->clearUserCounter(nUserId, nGroupId, IM::BaseDefine::SESSION_TYPE_GROUP);
            }
        }
    }
    else
    {
        log("user:%s has no permission to modify group:%s", nUserId.c_str(), nGroupId.c_str());
    }    return bRet;
}

bool CGroupModel::insertNewGroup(string nUserId, const string& strGroupName, const string& strGroupAvatar, uint32_t nGroupType, uint32_t nMemberCnt, string& nGroupId)
{
    bool bRet = false;
    nGroupId = INVALID_VALUE;
    CDBManager* pDBManager = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
    if (pDBConn)
    {
        string strSql = "insert into IMGroup(`name`, `avatar`, `creator`, `type`,`userCnt`, `status`, `version`, `lastChated`, `updated`, `created`) "\
        "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        CPrepareStatement* pStmt = new CPrepareStatement();
        if (pStmt->Init(pDBConn->GetMysql(), strSql))
        {
            uint32_t nCreated = (uint32_t)time(NULL);
            uint32_t index = 0;
            uint32_t nStatus = 0;
            uint32_t nVersion = 1;
            uint32_t nLastChat = 0;
            pStmt->SetParam(index++, strGroupName);
            pStmt->SetParam(index++, strGroupAvatar);
            pStmt->SetParam(index++, nUserId);
            pStmt->SetParam(index++, nGroupType);
            pStmt->SetParam(index++, nMemberCnt);
            pStmt->SetParam(index++, nStatus);
            pStmt->SetParam(index++, nVersion);
            pStmt->SetParam(index++, nLastChat);
            pStmt->SetParam(index++, nCreated);
            pStmt->SetParam(index++, nCreated);
            
            bRet = pStmt->ExecuteUpdate();
            if(bRet) {
                strSql = "select id from IMGroup order by created desc limit 1";
                CResultSet *pSet = pDBConn->ExecuteQuery(strSql.c_str());
                if(pSet)
                {
                    while(pSet->Next())
                        nGroupId = pSet->GetString("id");
                    delete pSet;
                }
            }
        }
        delete pStmt;
        pDBManager->RelDBConn(pDBConn);
    }
    else
    {
        log("no db connection for teamtalk_master");
    }
    return bRet;
}

bool CGroupModel::insertNewMember(string nGroupId, set<string>& setUsers)
{
    bool bRet = false;
    uint32_t nUserCnt = (uint32_t)setUsers.size();
    if(nGroupId != "" &&  nUserCnt > 0)
    {
        CDBManager* pDBManager = CDBManager::getInstance();
        CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
        if (pDBConn)
        {
            uint32_t nCreated = (uint32_t)time(NULL);
            // 获取 已经存在群里的用户
            string strClause;
            bool bFirst = true;
            for (auto it=setUsers.begin(); it!=setUsers.end(); ++it)
            {
                if(bFirst)
                {
                    bFirst = false;
                    strClause = (*it);
                }
                else
                {
                    strClause += ("','" + (*it));
                }
            }
            string strSql = "select userId from IMGroupMember where groupId='" + (nGroupId) + "' and userId in ('" + strClause + "')";
            CResultSet* pResult = pDBConn->ExecuteQuery(strSql.c_str());
            set<string> setHasUser;
            if(pResult)
            {
                while (pResult->Next()) {
                    setHasUser.insert(pResult->GetString("userId"));
                }
                delete pResult;
            }
            else
            {
                log("no result for sql:%s", strSql.c_str());
            }
            pDBManager->RelDBConn(pDBConn);
            
            pDBConn = pDBManager->GetDBConn("teamtalk_master");
            if (pDBConn)
            {
                CacheManager* pCacheManager = CacheManager::getInstance();
                CacheConn* pCacheConn = pCacheManager->GetCacheConn("group_member");
                if (pCacheConn)
                {
                    // 设置已经存在群中人的状态
                    if (!setHasUser.empty())
                    {
                        strClause.clear();
                        bFirst = true;
                        for (auto it=setHasUser.begin(); it!=setHasUser.end(); ++it) {
                            if(bFirst)
                            {
                                bFirst = false;
                                strClause = (*it);
                            }
                            else
                            {
                                strClause += ("','" + (*it));
                            }
                        }
                        
                        strSql = "update IMGroupMember set status=0, updated="+int2string(nCreated)+" where groupId='" + (nGroupId) + "' and userId in ('" + strClause + "')";
                        pDBConn->ExecuteUpdate(strSql.c_str());
                    }
                    strSql = "insert into IMGroupMember(`groupId`, `userId`, `status`, `created`, `updated`) values\
                    (?,?,?,?,?)";
                    
                    //插入新成员
                    auto it = setUsers.begin();
                    uint32_t nStatus = 0;
                    uint32_t nIncMemberCnt = 0;
                    for (;it != setUsers.end();)
                    {
                        string nUserId = *it;
                        if(setHasUser.find(nUserId) == setHasUser.end())
                        {
                            CPrepareStatement* pStmt = new CPrepareStatement();
                            if (pStmt->Init(pDBConn->GetMysql(), strSql))
                            {
                                uint32_t index = 0;
                                pStmt->SetParam(index++, nGroupId);
                                pStmt->SetParam(index++, nUserId);
                                pStmt->SetParam(index++, nStatus);
                                pStmt->SetParam(index++, nCreated);
                                pStmt->SetParam(index++, nCreated);
                                pStmt->ExecuteUpdate();
                                ++nIncMemberCnt;
                                delete pStmt;
                            }
                            else
                            {
                                setUsers.erase(it++);
                                delete pStmt;
                                continue;
                            }
                        }
                        ++it;
                    }
                    if(nIncMemberCnt != 0)
                    {
                        strSql = "update IMGroup set userCnt=userCnt+" + int2string(nIncMemberCnt) + " where id='"+(nGroupId)+"'";
                        pDBConn->ExecuteUpdate(strSql.c_str());
                    }
                    
                    //更新一份到redis中
                    string strKey = "group_member_"+(nGroupId);
                    for(auto it = setUsers.begin(); it!=setUsers.end(); ++it)
                    {
                        pCacheConn->hset(strKey, (*it), int2string(nCreated));
                    }
                    pCacheManager->RelCacheConn(pCacheConn);
                    bRet = true;
                }
                else
                {
                    log("no cache connection");
                }
                pDBManager->RelDBConn(pDBConn);
            }
            else
            {
                log("no db connection for teamtalk_master");
            }
        }
        else
        {
            log("no db connection for teamtalk_slave");
        }
    }
    return bRet;
}

void CGroupModel::getUserGroupIds(string nUserId, list<string>& lsGroupId, uint32_t nLimited)
{
    CDBManager* pDBManager = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
    if(pDBConn)
    {
        string strSql ;
        if (nLimited != 0) {
            strSql = "select groupId from IMGroupMember where userId='" + (nUserId) + "' and status = 0 order by updated desc, id desc limit " + int2string(nLimited);
        }
        else
        {
            strSql = "select groupId from IMGroupMember where userId='" + (nUserId) + "' and status = 0 order by updated desc, id desc";
        }
        
        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
        if(pResultSet)
        {
            while(pResultSet->Next())
            {
                string nGroupId = pResultSet->GetString("groupId");
                lsGroupId.push_back(nGroupId);
            }
            delete pResultSet;
        }
        else
        {
            log("no result set for sql:%s", strSql.c_str());
        }
        pDBManager->RelDBConn(pDBConn);
    }
    else
    {
        log("no db connection for teamtalk_slave");
    }
}

void CGroupModel::getGroupVersion(list<string> &lsGroupId, list<IM::BaseDefine::GroupVersionInfo> &lsGroup, uint32_t nGroupType)
{
    if(!lsGroupId.empty())
    {
        CDBManager* pDBManager = CDBManager::getInstance();
        CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
        if(pDBConn)
        {
            string strClause;
            bool bFirst = true;
            for(list<string>::iterator it=lsGroupId.begin(); it!=lsGroupId.end(); ++it)
            {
                if(bFirst)
                {
                    bFirst = false;
                    strClause = (*it);
                }
                else
                {
                    strClause += ("','" + (*it));
                }
            }
            
            string strSql = "select id,version from IMGroup where id in ('" +  strClause  + "')";
            if(0 != nGroupType)
            {
                strSql += " and type="+int2string(nGroupType);
            }
            strSql += " order by updated desc";
            
            CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
            if(pResultSet)
            {
                while(pResultSet->Next())
                {
                    IM::BaseDefine::GroupVersionInfo group;
                    group.set_group_id(pResultSet->GetString("id"));
                    group.set_version(pResultSet->GetInt("version"));
                    lsGroup.push_back(group);
                }
                delete pResultSet;
            }
            else
            {
                log("no result set for sql:%s", strSql.c_str());
            }
            pDBManager->RelDBConn(pDBConn);
        }
        else
        {
            log("no db connection for teamtalk_slave");
        }
    }
    else
    {
        log("group ids is empty");
    }
}

bool CGroupModel::isInGroup(string nUserId, string nGroupId)
{
    bool bRet = false;
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("group_member");
    if (pCacheConn)
    {
        string strKey = "group_member_" + (nGroupId);
        string strField = (nUserId);
        string strValue = pCacheConn->hget(strKey, strField);
        pCacheManager->RelCacheConn(pCacheConn);
        if(!strValue.empty())
        {
            bRet = true;
        }
    }
    else
    {
        log("no cache connection for group_member");
    }
    return bRet;
}

string CGroupModel::getGroupOwner(string nGroupId)
{
	string nRet ;
	CDBManager* pDBManager = CDBManager::getInstance();
	CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
	if(pDBConn)
	{
		string strSql = "select creator from IMGroup where id="+ (nGroupId);
		CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
		if(pResultSet)
		{
			while (pResultSet->Next())
			{
				nRet = pResultSet->GetString("creator");
			}
		}
		delete pResultSet;

		pDBManager->RelDBConn(pDBConn);
	}
	return nRet;

}

bool CGroupModel::hasModifyPermission(string nUserId, string nGroupId, IM::BaseDefine::GroupModifyType nType)
{
    if(nUserId == "") {
        return true;
    }
    
    bool bRet = false;
    CDBManager* pDBManager = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
    if(pDBConn)
    {
        string strSql = "select creator, type from IMGroup where id='"+ (nGroupId)+"'";
        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
        if(pResultSet)
        {
            while (pResultSet->Next())
            {
                string nCreator = pResultSet->GetString("creator");
                IM::BaseDefine::GroupType nGroupType = IM::BaseDefine::GroupType(pResultSet->GetInt("type"));
                if(IM::BaseDefine::GroupType_IsValid(nGroupType))
                {
                    if((IM::BaseDefine::GROUP_TYPE_TMP == nGroupType || IM::BaseDefine::GROUP_TYPE_ACTIVE == nGroupType)&& IM::BaseDefine::GROUP_MODIFY_TYPE_ADD == nType)
                    {
                        bRet = true;
                        break;
                    }
                    else
                    {
                        if(nCreator == nUserId)
                        {
                            bRet = true;
                            break;
                        }
                    }
                }
            }
            delete pResultSet;
        }
        else
        {
            log("no result for sql:%s", strSql.c_str());
        }
        pDBManager->RelDBConn(pDBConn);
    }
    else
    {
        log("no db connection for teamtalk_slave");
    }
    return bRet;
}

bool CGroupModel::addMember(string nGroupId, set<string> &setUser, list<string>& lsCurUserId)
{
    // 去掉已经存在的用户ID
    removeRepeatUser(nGroupId, setUser);
    bool bRet = insertNewMember(nGroupId, setUser);
    getGroupUser(nGroupId,lsCurUserId);
    return bRet;
}

bool CGroupModel::removeMember(string nGroupId, set<string> &setUser, list<string>& lsCurUserId)
{
    if(setUser.size() <= 0)
    {
        return true;
    }
    bool bRet = false;
    CDBManager* pDBManager = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
    if(pDBConn)
    {
        CacheManager* pCacheManager = CacheManager::getInstance();
        CacheConn* pCacheConn = pCacheManager->GetCacheConn("group_member");
        if (pCacheConn)
        {
            string strClause ;
            bool bFirst = true;
            for(auto it= setUser.begin(); it!=setUser.end();++it)
            {
                if (bFirst) {
                    bFirst = false;
                    strClause = (*it);
                }
                else
                {
                    strClause += ("','" + (*it));
                }
            }
            string strSql = "update IMGroupMember set status=1 where  groupId ='" + (nGroupId) + "' and userId in('" + strClause + "')";
            pDBConn->ExecuteUpdate(strSql.c_str());
            
            //从redis中删除成员
            string strKey = "group_member_"+ (nGroupId);
            for (auto it=setUser.begin(); it!=setUser.end(); ++it) {
                string strField = (*it);
                pCacheConn->hdel(strKey, strField);
            }
            pCacheManager->RelCacheConn(pCacheConn);
            bRet = true;
        }
        else
        {
            log("no cache connection");
        }
        pDBManager->RelDBConn(pDBConn);
        if (bRet)
        {
            getGroupUser(nGroupId,lsCurUserId);
        }
    }
    else
    {
        log("no db connection for teamtalk_master");
    }
    return bRet;
}

void CGroupModel::removeRepeatUser(string nGroupId, set<string> &setUser)
{
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("group_member");
    if (pCacheConn)
    {
        string strKey = "group_member_"+(nGroupId);
        for (auto it=setUser.begin(); it!=setUser.end();) {
            string strField = (*it);
            string strValue = pCacheConn->hget(strKey, strField);
            pCacheManager->RelCacheConn(pCacheConn);
            if(!strValue.empty())
            {
                setUser.erase(it++);
            }
            else
            {
                ++it;
            }
        }
    }
    else
    {
        log("no cache connection for group_member");
    }
}

bool CGroupModel::setPush(string nUserId, string nGroupId, uint32_t nType, uint32_t nStatus)
{
    bool bRet = false;
    if(!isInGroup(nUserId, nGroupId))
    {
        log("user:%s is not in group:%s", nUserId.c_str(), nGroupId.c_str());
        return bRet;;
    }
    
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("group_set");
    if(pCacheConn)
    {
        string strGroupKey = "group_set_" + (nGroupId);
        string strField = (nUserId) + "_" + int2string(nType);
        int nRet = pCacheConn->hset(strGroupKey, strField, int2string(nStatus));
        pCacheManager->RelCacheConn(pCacheConn);
        if(nRet != -1)
        {
            bRet = true;
        }
    }
    else
    {
        log("no cache connection for group_set");
    }
    return bRet;
}

void CGroupModel::getPush(string nGroupId, list<string>& lsUser, list<IM::BaseDefine::ShieldStatus>& lsPush)
{
    if (lsUser.empty()) {
        return;
    }
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("group_set");
    if(pCacheConn)
    {
        string strGroupKey = "group_set_" + (nGroupId);
        map<string, string> mapResult;
        bool bRet = pCacheConn->hgetAll(strGroupKey, mapResult);
        pCacheManager->RelCacheConn(pCacheConn);
        if(bRet)
        {
            for(auto it=lsUser.begin(); it!=lsUser.end(); ++it)
            {
                string strField = (*it) + "_" + int2string(IM_GROUP_SETTING_PUSH);
                auto itResult = mapResult.find(strField);
                IM::BaseDefine::ShieldStatus status;
                status.set_group_id(nGroupId);
                status.set_user_id(*it);
                if(itResult != mapResult.end())
                {
                    status.set_shield_status(string2int(itResult->second));
                }
                else
                {
                    status.set_shield_status(0);
                }
                lsPush.push_back(status);
            }
        }
        else
        {
            log("hgetall %s failed!", strGroupKey.c_str());
        }
    }
    else
    {
        log("no cache connection for group_set");
    }
}

void CGroupModel::getGroupUser(string nGroupId, list<string> &lsUserId)
{
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("group_member");
    if (pCacheConn)
    {
        string strKey = "group_member_" + (nGroupId);
        map<string, string> mapAllUser;
        bool bRet = pCacheConn->hgetAll(strKey, mapAllUser);
        pCacheManager->RelCacheConn(pCacheConn);
        if(bRet)
        {
            for (auto it=mapAllUser.begin(); it!=mapAllUser.end(); ++it) {
                string nUserId = (it->first);
                lsUserId.push_back(nUserId);
            }
        }
        else
        {
            log("hgetall %s failed!", strKey.c_str());
        }
    }
    else
    {
        log("no cache connection for group_member");
    }
}

void CGroupModel::updateGroupChat(string nGroupId)
{
    CDBManager* pDBManager = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
    if(pDBConn)
    { 
        uint32_t nNow = (uint32_t)time(NULL);
        string strSql = "update IMGroup set lastChated=" + int2string(nNow) + " where id='" + (nGroupId)+"'";
        pDBConn->ExecuteUpdate(strSql.c_str());
        pDBManager->RelDBConn(pDBConn);
    }
    else
    {
        log("no db connection for teamtalk_master");
    }
}

//bool CGroupModel::isValidateGroupId(uint32_t nGroupId)
//{
//    bool bRet = false;
//    CDBManager* pDBManager = CDBManager::getInstance();
//    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
//    if(pDBConn)
//    {
//        string strSql = "select id from IMGroup where id=" + int2string(nGroupId)+" and status=0";
//        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
//        if(pResultSet && pResultSet->Next())
//        {
//            bRet =  true;
//            delete pResultSet;
//        }
//        pDBManager->RelDBConn(pDBConn);
//    }
//    else
//    {
//        log("no db connection for teamtalk_slave");
//    }
//    return bRet;
//}

bool CGroupModel::isValidateGroupId(string nGroupId)
{
    bool bRet = false;
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("group_member");
    if(pCacheConn)
    {
        string strKey = "group_member_"+(nGroupId);
        bRet = pCacheConn->isExists(strKey);
        pCacheManager->RelCacheConn(pCacheConn);
    }
    return bRet;
}


void CGroupModel::removeSession(string nGroupId, const set<string> &setUser)
{
    for(auto it=setUser.begin(); it!=setUser.end(); ++it)
    {
        string nUserId=*it;
        string nSessionId = CSessionModel::getInstance()->getSessionId(nUserId, nGroupId, IM::BaseDefine::SESSION_TYPE_GROUP, false);
        CSessionModel::getInstance()->removeSession(nSessionId);
    }
}

bool CGroupModel::incGroupVersion(string nGroupId)
{
    bool bRet = false;
    CDBManager* pDBManager = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
    if(pDBConn)
    {
        string strSql = "update IMGroup set version=version+1 where id='"+(nGroupId)+"'";
        if(pDBConn->ExecuteUpdate(strSql.c_str()))
        {
            bRet = true;
        }
        pDBManager->RelDBConn(pDBConn);
    }
    else
    {
        log("no db connection for teamtalk_master");
    }
    return  bRet;
}


void CGroupModel::fillGroupMember(list<IM::BaseDefine::GroupInfo>& lsGroups)
{
    for (auto it=lsGroups.begin(); it!=lsGroups.end(); ++it) {
        list<string> lsUserIds;
        string nGroupId = it->group_id();
        getGroupUser(nGroupId, lsUserIds);
        for(auto itUserId=lsUserIds.begin(); itUserId!=lsUserIds.end(); ++itUserId)
        {
            it->add_group_member_list(*itUserId);
        }
    }
}

uint32_t CGroupModel::getUserJoinTime(string nGroupId, string nUserId)
{
    uint32_t nTime = 0;
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("group_member");
    if (pCacheConn)
    {
        string strKey = "group_member_" + (nGroupId);
        string strField = (nUserId);
        string strValue = pCacheConn->hget(strKey, strField);
        pCacheManager->RelCacheConn(pCacheConn);
        if (!strValue.empty()) {
            nTime = string2int(strValue);
        }
    }
    else
    {
        log("no cache connection for group_member");
    }
    return  nTime;
}

void CGroupModel::clearGroupMember(string nGroupId)
{
    CDBManager* pDBManager = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
    if(pDBConn)
    {
        string strSql = "delete from IMGroupMember where groupId='"+(nGroupId)+"'";
        pDBConn->ExecuteUpdate(strSql.c_str());
        pDBManager->RelDBConn(pDBConn);
    }
    else
    {
        log("no db connection for teamtalk_master");
    }
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("group_member");
    if(pCacheConn)
    {
        string strKey = "group_member_" + (nGroupId);
        map<string, string> mapRet;
        bool bRet = pCacheConn->hgetAll(strKey, mapRet);
        if(bRet)
        {
            for(auto it=mapRet.begin(); it!=mapRet.end(); ++it)
            {
                pCacheConn->hdel(strKey, it->first);
            }
        }
        else
        {
            log("hgetall %s failed", strKey.c_str());
        }
        pCacheManager->RelCacheConn(pCacheConn);
    }
    else
    {
        log("no cache connection for group_member");
    }
}


//cita add:获取推荐群列表
bool CGroupModel::getRecommandGroupList(list<IM::BaseDefine::GroupInfo>& lsGroupInfo)
{
    bool ret = false;
    do{
        CDBManager *pDBManager = CDBManager::getInstance();
        CDBConn * pDBConn = pDBManager->GetDBConn("teamtalk_master");
        if(pDBConn)
        {
            int group_type = (int)IM::BaseDefine::GroupType::GROUP_TYPE_ACTIVE;
            string strSql = "select * from IMGroup where type="+std::to_string(group_type);
            CResultSet *pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
            if(pResultSet)
            {
                while(pResultSet->Next())
                {
                    string nGroupId = pResultSet->GetString("id");
                    uint32_t nVersion = pResultSet->GetInt("version");
                    IM::BaseDefine::GroupInfo cGroupInfo;
                    cGroupInfo.set_group_id(nGroupId);
                    cGroupInfo.set_version(nVersion);
                    cGroupInfo.set_group_name(pResultSet->GetString("name"));
                    cGroupInfo.set_group_avatar(pResultSet->GetString("avatar"));
                    IM::BaseDefine::GroupType nGroupType = IM::BaseDefine::GroupType(pResultSet->GetInt("type"));

                    std::list<IM::BaseDefine::UserInfo> users;
                    getGroupMemberInfoFromDB(nGroupId,users);
                    for(const auto & iUser:users)
                    {
                       IM::BaseDefine::UserInfo *pUser = cGroupInfo.add_group_member_users();
                        pUser->set_user_id(iUser.user_id());
                        pUser->set_user_gender(iUser.user_gender());
                        pUser->set_user_nick_name(iUser.user_nick_name());
                        pUser->set_user_domain(iUser.user_domain());
                        pUser->set_user_real_name(iUser.user_real_name());
                        pUser->set_user_tel(iUser.user_tel());    //用户电话隐藏
                        pUser->set_email(iUser.email());
                        pUser->set_avatar_url(iUser.avatar_url());
                        pUser->set_sign_info(iUser.sign_info());
                        pUser->set_fans_cnt(iUser.fans_cnt());

                        pUser->set_department_id(iUser.department_id());
                        pUser->set_status(iUser.status());
                        pUser->set_updated(iUser.updated());

                        pUser->set_relation(iUser.relation());
                    }
                    if(IM::BaseDefine::GroupType_IsValid(nGroupType))
                    {
                        cGroupInfo.set_group_type(nGroupType);
                        cGroupInfo.set_group_creator_id(pResultSet->GetString("creator"));
                        lsGroupInfo.push_back(cGroupInfo);
                    }
                    else
                    {
                        log("invalid groupType. groupId=%s, groupType=%u", nGroupId.c_str(), nGroupType);
                    }
                }
                delete pResultSet;
            }
            pDBManager->RelDBConn(pDBConn);
            pDBConn = nullptr;
            ret = true;
        }
    }while(0);
    return ret;
}

//cita add:获取群成员
bool CGroupModel::getGroupMemberFromDB(const std::string groupId,list<std::string> & members)
{
    bool bRet = false;
    do{
        CDBManager *pDBManager = CDBManager::getInstance();
        CDBConn * pDBConn = pDBManager->GetDBConn("teamtalk_master");
        if(pDBConn)
        {
            string strSql = "select * from IMGroupMember where groupId='"+groupId+"' and status=0";
            CResultSet *pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
            if(pResultSet)
            {
                while(pResultSet->Next())
                {
                    members.push_back(pResultSet->GetString("userId"));
                }
                delete pResultSet;
            }
            pDBManager->RelDBConn(pDBConn);
            bRet = true;
        }
        
    }while(0);

    return bRet;
}


 bool CGroupModel::getGroupMemberInfoFromDB(const std::string groupId,list<IM::BaseDefine::UserInfo>& lsUsers)
 {
    bool bRet = false;
    do{
        CDBManager *pDBManager = CDBManager::getInstance();
        CDBConn * pDBConn = pDBManager->GetDBConn("teamtalk_master");
        if(pDBConn)
        {
            list<std::string> members;
            string strSql = "select * from IMGroupMember where groupId='"+groupId+"' and status=0";
            CResultSet *pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
            if(pResultSet)
            {
                while(pResultSet->Next())
                {
                    members.push_back(pResultSet->GetString("userId"));
                }
                delete pResultSet;
            }
            pDBManager->RelDBConn(pDBConn);
            bRet = true;
            if(members.size() > 0)
                CUserModel::getInstance()->getUsers(members,lsUsers);
        }
        
    }while(0);

    return bRet;
 }


static bool superGroupIsCreate(const std::string & createId)
{
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("group_member");
    bool bRet = false;
    if(pCacheConn)
    {
        std::string key = createId;
        bRet = pCacheConn->isExists(key);
        std::string value = "true";
        if(!bRet)
            pCacheConn->set(std::string(createId),value);
        pCacheManager->RelCacheConn(pCacheConn);
    }
    return bRet;
}

static bool updateSuperGroup(const std::string & createId,const std::string & groupId)
{
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("group_member");
    bool bRet = false;
    if(pCacheConn)
    {
        std::string strGroupId = groupId;
        pCacheConn->set(std::string(createId),strGroupId);
        pCacheManager->RelCacheConn(pCacheConn);
    }
    return bRet;
}

static std::string getSuperGroupId()
{
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("group_member");
    std:;string groupId;
    if(pCacheConn)
    {
        groupId = pCacheConn->get(std::string(g_SuperCreateId));
        pCacheManager->RelCacheConn(pCacheConn);
    }
    return groupId;
}


bool CGroupModel::createSuperGroup(std::string & groupId)
{
    bool ret = false;
    do{
        if(!superGroupIsCreate(g_SuperCreateId))
        {
            CDBManager *pDBManager = CDBManager::getInstance();
            CDBConn * pDBConn = pDBManager->GetDBConn("teamtalk_master");
            if(pDBConn)
            {
                CInterLoginStrategy loginStrategy;
                loginStrategy.insertUser(g_SuperCreateId);
                std::string sql = "select id from IMUser";
                CResultSet *pSet = pDBConn->ExecuteQuery(sql.c_str());
                std::set<std::string> allUsers;
                if(pSet)
                {
                    while(pSet->Next())
                        allUsers.insert(pSet->GetString("id"));
                    delete pSet;
                }
                pDBManager->RelDBConn(pDBConn);

                if(allUsers.size() > 0)
                {
                    groupId = this->createGroup(g_SuperCreateId,g_SuperGroupName,"",IM::BaseDefine::GroupType::GROUP_TYPE_ACTIVE,allUsers);
                    updateSuperGroup(g_SuperCreateId,groupId);
                    log("super group id=%s",groupId.c_str());
                }
            }
        }
        else
        {
            groupId = getSuperGroupId();
            log("super group is already existed!");
        }
        ret = true;
    }while(0);
    return ret; 
}

bool CGroupModel::insertSuperGroupMember(const std::string & nUserId)
{
    bool bRet = false;
    std::string strGroupId = getSuperGroupId();
    std::set<std::string> uset;
    uset.insert(nUserId);
    if(!strGroupId.empty())
       bRet = insertNewMember(strGroupId,uset);
    return bRet;
}

bool CGroupModel::removeSuperGroupMember(const std::string & nUserId)
{
    bool bRet = false;
    std:;string strGroupId = getSuperGroupId();
    if(!strGroupId.empty())
    {
        std::set<std::string> uset;
        uset.insert(nUserId);
        std::list<std::string> curList;
        bRet = removeMember(strGroupId,uset,curList);
    }
    return bRet;
}