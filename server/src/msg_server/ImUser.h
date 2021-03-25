/*
 * ImUser.h
 *
 *  Created on: 2014年4月16日
 *      Author: ziteng
 */

#ifndef IMUSER_H_
#define IMUSER_H_

#include "imconn.h"
#include "public_define.h"
#include "IM.System.pb.h"
#include "IM.Buddy.pb.h"
#define MAX_ONLINE_FRIEND_CNT		100	//通知好友状态通知的最多个数

class CMsgConn;

class CImUser
{
public:
    CImUser(string pubkey);
    ~CImUser();
    
    void SetUserId(string user_id) { m_user_id = user_id; }
    string GetUserId() { return m_user_id; }
    string GetLoginName() { return m_login_name; }
    void SetNickName(string nick_name) { m_nick_name = nick_name; }
    string GetNickName() { return m_nick_name; }
    bool IsValidate() { return m_bValidate; }
    void SetValidated() { m_bValidate = true; }
    uint32_t GetPCLoginStatus() { return m_pc_login_status; }
    void SetPCLoginStatus(uint32_t pc_login_status) { m_pc_login_status = pc_login_status; }
    
    //同一个用户可能保存着多个msg_conn连接
    user_conn_t GetUserConn();      
    bool IsMsgConnEmpty() { return m_conn_map.empty(); }
    void AddMsgConn(uint32_t handle, CMsgConn* pMsgConn) { m_conn_map[handle] = pMsgConn; }
    void DelMsgConn(uint32_t handle) { m_conn_map.erase(handle); }
    CMsgConn* GetMsgConn(uint32_t handle);
    void ValidateMsgConn(uint32_t handle, CMsgConn* pMsgConn);
    void AddUnValidateMsgConn(CMsgConn* pMsgConn) { m_unvalidate_conn_set.insert(pMsgConn); }
    void DelUnValidateMsgConn(CMsgConn* pMsgConn) { m_unvalidate_conn_set.erase(pMsgConn); }
    CMsgConn* GetUnValidateMsgConn(uint32_t handle);
    map<uint32_t, CMsgConn*>& GetMsgConnMap() { return m_conn_map; }

    //广播到从不同设备登录的用户
    void BroadcastPdu(CImPdu* pPdu, CMsgConn* pFromConn = NULL);
    void BroadcastPduWithOutMobile(CImPdu* pPdu, CMsgConn* pFromConn = NULL);
    void BroadcastPduToMobile(CImPdu* pPdu, CMsgConn* pFromConn = NULL);
    void BroadcastClientMsgData(CImPdu* pPdu, uint32_t msg_id, CMsgConn* pFromConn = NULL, string from_id = 0);
    void BroadcastData(void* buff, uint32_t len, CMsgConn* pFromConn = NULL);
        
    //踢下线操作    
    void HandleKickUser(CMsgConn* pConn, uint32_t reason);
    bool KickOutSameClientType(uint32_t client_type, uint32_t reason, CMsgConn* pFromConn = NULL);
    uint32_t GetClientTypeFlag();

    //cita add:
    void SetUserAddress(string address){m_user_address = address;}
    string GetUserAddress(){return m_user_address;}

private:
    string			m_login_name;       //登录名-保留
    string          m_nick_name;        //昵称
    string          m_user_address;     //用户地址-暂时与btc地址一致
    string          m_user_id;      //用户公钥
    bool 			m_user_updated;
    uint32_t        m_pc_login_status;  // pc client login状态，1: on 0: off
    bool 			m_bValidate;
    map<uint32_t /* handle */, CMsgConn*>	m_conn_map; //socket句柄与CMsgConn关联
    set<CMsgConn*> m_unvalidate_conn_set; //cita delete
};

typedef map<string /* user_id */, CImUser*> ImUserMap_t;

class CImUserManager
{
public:
    CImUserManager() {}
    ~CImUserManager();
    
    static CImUserManager* GetInstance();
    CMsgConn* GetMsgConnByHandle(string user_id, uint32_t handle);
    
    CImUser* GetImUserById(string user_id);//cita add
    bool AddImUserById(string user_id,CImUser *pUser);//cita add
    void RemoveImUserById(string user_id);//cita add
    void GetAllUser(list<CImUser *> & userList);//cita add
    void RemoveImUser(CImUser* pUser);
    
    void RemoveAll();
    void GetOnlineUserInfo(list<user_stat_t>* online_user_info);
    void GetUserConnCnt(list<user_conn_t>* user_conn_list, uint32_t& total_conn_cnt);
    void BroadcastPdu(CImPdu* pdu, uint32_t client_type_flag);
    void GetOnlineRecommendList(uint32_t page, uint32_t page_size, IM::Buddy::IMRecommendListRsp &resp);

public:
    ImUserMap_t m_im_user_map;
};

void get_online_user_info(list<user_stat_t>* online_user_info);

#endif /* IMUSER_H_ */
