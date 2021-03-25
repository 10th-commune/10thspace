/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：RelationModel.h
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#ifndef RELATION_SHIP_H_
#define RELATION_SHIP_H_

#include <list>

#include "util.h"
#include "ImPduBase.h"
#include "IM.BaseDefine.pb.h"

using namespace std;

class CRelationModel {
public:
	virtual ~CRelationModel();

	static CRelationModel* getInstance();
    uint32_t getRelationId(string nUserAId, string nUserBId, bool bAdd);
    bool updateRelation(uint32_t nRelationId, uint32_t nUpdateTime);
    bool removeRelation(uint32_t nRelationId);
    
    uint32_t addRelation(string nSmallId, string nBigId, uint32_t tag);

    bool delFriend(string nUserId, string nFriendId);
    bool delFollowUser(string nUserId, string nFriendId);
private:
	CRelationModel();

private:
	static CRelationModel*	m_pInstance;
};
#endif
