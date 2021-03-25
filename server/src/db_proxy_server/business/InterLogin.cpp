/*================================================================
*     Copyright (c) 2015年 lanhu. All rights reserved.
*   
*   文件名称：InterLogin.cpp
*   创 建 者：Zhang Yuanhao
*   邮    箱：bluefoxah@gmail.com
*   创建日期：2015年03月09日
*   描    述：
*
================================================================*/
#include "InterLogin.h"
#include "../DBPool.h"
#include "EncDec.h"
#include <time.h>

#define DEF_REFERRAL_CODE_SIZE 6 //推荐码7位，第一位默认是0


bool CInterLoginStrategy::insertUser(const std::string & pubkey)
{

    if(pubkey.empty())
        return false;

    bool ret = false;
    CDBManager* pDBManger = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManger->GetDBConn("teamtalk_slave");
    if (pDBConn) {
        string strSql = "INSERT INTO IMUser (id) VALUES('"+pubkey+"')";
        ret = pDBConn->ExecuteUpdate(strSql.c_str());
        log("%s",strSql.c_str());
        pDBManger->RelDBConn(pDBConn);
    }
    return ret;
}

static std::string intCodeToString(const int & referralCode)
{
    std::string strCode = std::to_string(referralCode);
    int size =strCode.length();
    std::string strTemplate(DEF_REFERRAL_CODE_SIZE-size,'0');
    strTemplate += strCode;
    return strTemplate;
}

bool CInterLoginStrategy::getUserReferralCode(const std::string & pubkey,std::string & code)
{
    if(pubkey.empty())
        return false;
    bool ret = false;
    CDBManager* pDBManger = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManger->GetDBConn("teamtalk_slave");
    int referral_code = -1;
    if (pDBConn) {
        string strSql = "select id from IMReferralCode where userId='"+pubkey+"'";
        CResultSet *pSet = pDBConn->ExecuteQuery(strSql.c_str());
        if(pSet)
        {
            while(pSet->Next())
                referral_code = pSet->GetInt("id");
            delete pSet;
            pSet = nullptr;
        }

        if(referral_code < 0)
        {
             string strSql = "insert into IMReferralCode set userId='"+pubkey+"'";
             ret = pDBConn->ExecuteUpdate(strSql.c_str());
             if(ret)
                referral_code = pDBConn->GetInsertId();
             log("%s",strSql.c_str());
        }
        code = intCodeToString(referral_code);

        log("%s",code.c_str());
        pDBManger->RelDBConn(pDBConn);
    }
    return ret;
}

bool CInterLoginStrategy::insertReferralRelation(const std::string& pubkey, const std::string& strReferralCode)
{
    bool ret = false;
    CDBManager* pDBManger = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManger->GetDBConn("teamtalk_slave");
    if (pDBConn && !pubkey.empty()) {
        string strSql = "select id from IMRecommand where userId='"+pubkey+"'";
        log("%s",strSql.c_str());
        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
        if(pResultSet)
        {
            ret = pResultSet->Next();
            delete pResultSet;
        }

        if(!ret)
        {
            addReferralCode(pubkey,strReferralCode);
        }
        pDBManger->RelDBConn(pDBConn);
    }
    return ret;
}


bool CInterLoginStrategy::doLogin(const std::string &pubkey, const std::string &strReferralCode, IM::BaseDefine::UserInfo& user)
{
    bool bRet = false;
    CDBManager* pDBManger = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManger->GetDBConn("teamtalk_slave");
    if (pDBConn) {
        string strSql = "select * from IMUser where id='" + pubkey + "' and status=0";
        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
        if(pResultSet)
        {
            string strResult, strSalt;
            string nId;
            uint32_t nGender, nDeptId, nStatus;
            string strNick, strAvatar, strEmail, strRealName, strTel, strDomain,strSignInfo,referral_code;
            while (pResultSet->Next()) {
                nId = pResultSet->GetString("id");
                strResult = pResultSet->GetString("password");
                strSalt = pResultSet->GetString("salt");
                strNick = pResultSet->GetString("nick");
                nGender = pResultSet->GetInt("sex");
                strRealName = pResultSet->GetString("name");
                strDomain = pResultSet->GetString("domain");
                strTel = pResultSet->GetString("phone");
                strEmail = pResultSet->GetString("email");
                strAvatar = pResultSet->GetString("avatar");
                nDeptId = pResultSet->GetInt("departId");
                nStatus = pResultSet->GetInt("status");
                strSignInfo = pResultSet->GetString("sign_info");
            }

            //用户不存在，插入新用户
            if(nId.empty())
            {
                insertUser(pubkey);
            }
           
            getUserReferralCode(pubkey,referral_code);
            
            if(!strReferralCode.empty())
                insertReferralRelation(pubkey,strReferralCode);
            //返回用户信息
            bRet = true;
            user.set_user_id(pubkey);
            user.set_user_nick_name(strNick);
            user.set_user_gender(nGender);
            user.set_user_real_name(strRealName);
            user.set_user_domain(strDomain);
            user.set_user_tel(strTel);
            user.set_email(strEmail);
            user.set_avatar_url(strAvatar);
            user.set_department_id(nDeptId);
            user.set_status(nStatus);
            user.set_sign_info(strSignInfo);
            user.set_referral_code(referral_code);
            
            delete  pResultSet;
        }
        pDBManger->RelDBConn(pDBConn);
    }
    return bRet;
}


bool CInterLoginStrategy::addReferralCode(const std::string & pubkey,const std::string & code)
{

    if(pubkey.empty() || code.empty())
        return false;

    bool ret = false;
    CDBManager* pDBManger = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManger->GetDBConn("teamtalk_slave");
    if (pDBConn) {
      
        string strSql = "INSERT INTO IMRecommand (referralCode,userId) values('" + code + "','" + pubkey + "')";
        ret = pDBConn->ExecuteUpdate(strSql.c_str());
        log("%s",strSql.c_str());
        pDBManger->RelDBConn(pDBConn);
    }
    return ret;
}

bool CInterLoginStrategy::getReferralCode(const std::string & pubkey, std::string & code)
{
    if(pubkey.empty() || code.empty())
        return false;

    bool ret = false;
    CDBManager* pDBManger = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManger->GetDBConn("teamtalk_slave");
    if (pDBConn && !pubkey.empty()) {
      
        string strSql = "select referralCode from IMUser where id='"+pubkey+"'";
        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
        if(pResultSet)
        {
            while(pResultSet->Next())
            {
                code = pResultSet->GetString("referralCode");
                ret = true;
                break;
            }
            delete pResultSet;
        }
        log("%s",strSql.c_str());
        pDBManger->RelDBConn(pDBConn);
    }
    return ret;
}
