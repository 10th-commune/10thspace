/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：GroupModel.h
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#ifndef __IM_GROUP_MODEL__
#define __IM_GROUP_MODEL__

#include <stdio.h>
#include <iostream>
#include <string>
#include <list>
#include <map>
#include <set>

#include "MessageModel.h"
#include "IM.BaseDefine.pb.h"

using namespace std;

const uint32_t MAX_UNREAD_COUNT = 100;

const std::string  g_SuperCreateId      = "vFdNZ5MzbE2KYtGcgKxVqsh7CSGj4yKhkbm89ULeKwzv";
const std::string  g_SuperNickName      = "Official Person";
const std::string  g_SuperGroupName     = "Official Group";

class CGroupModel {
public:
    virtual ~CGroupModel();
    static CGroupModel* getInstance();
    
public:
    string createGroup(string nUserId, const string& strGroupName, const string& strGroupAvatar, uint32_t nGroupType, set<string>& setMember);
    bool removeGroup(string nUserId, string nGroupId, list<string>& lsCurUserId);
    void getUserGroup(string nUserId, list<IM::BaseDefine::GroupVersionInfo>& lsGroup, uint32_t nGroupType);
    void getUserGroupIds(string nUserId, list<string>& lsGroupId, uint32_t nLimited = 100);
    void getGroupInfo(map<string,IM::BaseDefine::GroupVersionInfo>& mapGroupId, list<IM::BaseDefine::GroupInfo>& lsGroupInfo);
    bool setPush(string nUserId, string nGroupId, uint32_t nType, uint32_t nStatus);
    void getPush(string nGroupId, list<string>& lsUser, list<IM::BaseDefine::ShieldStatus>& lsPush);
    bool modifyGroupMember(string nUserId, string nGroupId, IM::BaseDefine::GroupModifyType nType, set<string>& setUserId,
                           list<string>& lsCurUserId, uint32_t quit);
    void getGroupUser(string nGroupId, list<string>& lsUserId);
    bool isInGroup(string nUserId, string nGroupId);
    void updateGroupChat(string nGroupId);
    bool isValidateGroupId(string nGroupId);
    uint32_t getUserJoinTime(string nGroupId, string nUserId);
    string getGroupOwner(string nGroupId);

    //cita add:获取推荐群列表
    bool getRecommandGroupList(list<IM::BaseDefine::GroupInfo>& lsGroupInfo);
    bool getGroupMemberFromDB(const std::string groupId,list<std::string> & members);
    bool getGroupMemberInfoFromDB(const std::string groupId,list<IM::BaseDefine::UserInfo>& lsUsers);
    bool createSuperGroup(std::string & groupId);
    bool insertSuperGroupMember(const std::string & nUserId);
    bool removeSuperGroupMember(const std::string & nUserId);
private:
    CGroupModel();
    
    bool insertNewGroup(string reqUserId, const string& groupName, const string& groupAvatar, uint32_t groupType, uint32_t memCnt, string& groupId);
    bool insertNewMember(string nGroupId,set<string>& setUsers);
        string GenerateGroupAvatar(string groupId);
    void getGroupVersion(list<string>&lsGroupId, list<IM::BaseDefine::GroupVersionInfo>& lsGroup, uint32_t nGroupType);
    bool hasModifyPermission(string nUserId, string nGroupId, IM::BaseDefine::GroupModifyType nType);
    bool addMember(string nGroupId, set<string>& setUser,list<string>& lsCurUserId);
    bool removeMember(string nGroupId, set<string>& setUser,list<string>& lsCurUserId);
    void removeRepeatUser(string nGroupId, set<string>& setUser);
    void removeSession(string nGroupId, const set<string>& lsUser);
    bool incGroupVersion(string nGroupId);
    void clearGroupMember(string nGroupId);
    
    void fillGroupMember(list<IM::BaseDefine::GroupInfo>& lsGroups);
        
private:
    static CGroupModel*	m_pInstance;
    std::string m_superGroupId;
};

#endif /* defined(__IM_GROUP_MODEL__) */
