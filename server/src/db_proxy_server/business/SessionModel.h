/*================================================================
*     Copyright (c) 2015年 lanhu. All rights reserved.
*   
*   文件名称：SessionModel.h
*   创 建 者：Zhang Yuanhao
*   邮    箱：bluefoxah@gmail.com
*   创建日期：2015年03月16日
*   描    述：
*
#pragma once
================================================================*/
#ifndef __SESSIONMODEL_H__
#define __SESSIONMODEL_H__

#include "ImPduBase.h"
#include "IM.BaseDefine.pb.h"

class CSessionModel
{
public:
    static CSessionModel* getInstance();
    ~CSessionModel() {}

    void getRecentSession(string userId, uint32_t lastTime, list<IM::BaseDefine::ContactSessionInfo>& lsContact);
    string getSessionId(string nUserId, string nPeerId, uint32_t nType, bool isAll);
    bool updateSession(string nSessionId, uint32_t nUpdateTime);
    bool removeSession(string nSessionId);
    string addSession(string nUserId, string nPeerId, uint32_t nType);
    
private:
    CSessionModel() {};
    void fillSessionMsg(string nUserId, list<IM::BaseDefine::ContactSessionInfo>& lsContact);
private:
    static CSessionModel* m_pInstance;
};

#endif /*defined(__SESSIONMODEL_H__) */
