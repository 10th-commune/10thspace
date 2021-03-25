/*================================================================
*     Copyright (c) 2015年 lanhu. All rights reserved.
*   
*   文件名称：InternalAuth.h
*   创 建 者：Zhang Yuanhao
*   邮    箱：bluefoxah@gmail.com
*   创建日期：2015年03月09日
*   描    述：内部数据库验证策略
*
#pragma once
================================================================*/
#ifndef __INTERLOGIN_H__
#define __INTERLOGIN_H__

#include "LoginStrategy.h"

class CInterLoginStrategy :public CLoginStrategy
{
public:

    virtual bool doLogin(const std::string& pubkey, const std::string& strReferralCode, IM::BaseDefine::UserInfo &user);
    bool insertUser(const std::string & pubkey);
    bool getUserReferralCode(const std::string & pubkey,std::string & code);
    bool insertReferralRelation(const std::string& pubkey, const std::string& strReferralCode);
    bool addReferralCode(const std::string & pubkey,const std::string & code);
    bool getReferralCode(const std::string & pubkey, std::string & code);
};

#endif /*defined(__INTERLOGIN_H__) */
