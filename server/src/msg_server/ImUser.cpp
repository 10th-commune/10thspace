/*
 * ImUser.cpp
 *
 *  Created on: 2014年4月16日
 *      Author: ziteng
 *  Brief:
 *  	a map from user_id to userInfo and connection list
 */

#include "ImUser.h"
#include "MsgConn.h"
#include "RouteServConn.h"
#include "IM.Server.pb.h"
#include "IM.Login.pb.h"
#include "IM.System.pb.h"
#include "util.h"
using namespace ::IM::BaseDefine;

CImUser::CImUser(string user_id)
{
    m_user_id = user_id;
    m_bValidate = false;
    m_user_updated = false;
    m_pc_login_status = IM::BaseDefine::USER_STATUS_OFFLINE;
}

CImUser::~CImUser()
{

}

CMsgConn* CImUser::GetUnValidateMsgConn(uint32_t handle)
{
    for (set<CMsgConn*>::iterator it = m_unvalidate_conn_set.begin(); it != m_unvalidate_conn_set.end(); it++)
    {
        CMsgConn* pConn = *it;
        if (pConn->GetHandle() == handle) {
            return pConn;
        }
    }
    
    return NULL;
}

CMsgConn* CImUser::GetMsgConn(uint32_t handle)
{
    CMsgConn* pMsgConn = NULL;
    map<uint32_t, CMsgConn*>::iterator it = m_conn_map.find(handle);
    if (it != m_conn_map.end()) {
        pMsgConn = it->second;
    }
    return pMsgConn;
}

void CImUser::ValidateMsgConn(uint32_t handle, CMsgConn* pMsgConn)
{
    AddMsgConn(handle, pMsgConn);
    DelUnValidateMsgConn(pMsgConn);
}


user_conn_t CImUser::GetUserConn()
{
    uint32_t conn_cnt = 0;
    for (map<uint32_t, CMsgConn*>::iterator it = m_conn_map.begin(); it != m_conn_map.end(); it++)
    {
        CMsgConn* pConn = it->second;
        if (pConn->IsOpen()) {
            conn_cnt++;
        }
    }
    
    user_conn_t user_cnt = {m_user_id, conn_cnt};
    return user_cnt;
}

void CImUser::BroadcastPdu(CImPdu* pPdu, CMsgConn* pFromConn)
{
    for (map<uint32_t, CMsgConn*>::iterator it = m_conn_map.begin(); it != m_conn_map.end(); it++)
    {
        CMsgConn* pConn = it->second;
        if (pConn != pFromConn) {
            pConn->SendPdu(pPdu);
        }
    }
}

void CImUser::BroadcastPduWithOutMobile(CImPdu *pPdu, CMsgConn* pFromConn)
{
    for (map<uint32_t, CMsgConn*>::iterator it = m_conn_map.begin(); it != m_conn_map.end(); it++)
    {
        CMsgConn* pConn = it->second;
        if (pConn != pFromConn && CHECK_CLIENT_TYPE_PC(pConn->GetClientType())) {
            pConn->SendPdu(pPdu);
        }
    }
}

void CImUser::BroadcastPduToMobile(CImPdu* pPdu, CMsgConn* pFromConn)
{
    for (map<uint32_t, CMsgConn*>::iterator it = m_conn_map.begin(); it != m_conn_map.end(); it++)
    {
        CMsgConn* pConn = it->second;
        if (pConn != pFromConn && CHECK_CLIENT_TYPE_MOBILE(pConn->GetClientType())) {
            pConn->SendPdu(pPdu);
        }
    }
}


void CImUser::BroadcastClientMsgData(CImPdu* pPdu, uint32_t msg_id, CMsgConn* pFromConn, string from_id)
{
    for (map<uint32_t, CMsgConn*>::iterator it = m_conn_map.begin(); it != m_conn_map.end(); it++)
    {
        CMsgConn* pConn = it->second;
        if (pConn != pFromConn) {
            pConn->SendPdu(pPdu);
            pConn->AddToSendList(msg_id, from_id);
        }
    }
}

void CImUser::BroadcastData(void *buff, uint32_t len, CMsgConn* pFromConn)
{
    if(!buff)
        return;
    for (map<uint32_t, CMsgConn*>::iterator it = m_conn_map.begin(); it != m_conn_map.end(); it++)
    {
        CMsgConn* pConn = it->second;
        
        if(pConn == NULL)
            continue;
        
        if (pConn != pFromConn) {
            pConn->Send(buff, len);
        }
    }
}

//只支持一个msg server
void CImUser::HandleKickUser(CMsgConn* pConn, uint32_t reason)
{
    map<uint32_t, CMsgConn*>::iterator it = m_conn_map.find(pConn->GetHandle());
    if (it != m_conn_map.end()) {
        CMsgConn* pConn = it->second;
        if(pConn) {
            log("kick service user, user_id=%s.", m_user_id.c_str());
            IM::Login::IMKickUser msg;
            msg.set_user_id(m_user_id);
            msg.set_kick_reason((::IM::BaseDefine::KickReasonType)reason);
            CImPdu pdu;
            pdu.SetPBMsg(&msg);
            pdu.SetServiceId(SID_LOGIN);
            pdu.SetCommandId(CID_LOGIN_KICK_USER);
            pConn->SetKickOff();//wystan modify for kickout same user 200628
            pConn->SendPdu(&pdu);
			//pConn->SetKickOff();//wystan modify for kickout same user 200628
            //pConn->Close();
        }
    }
}

// 只支持一个WINDOWS/MAC客户端登陆,或者一个ios/android登录
bool CImUser::KickOutSameClientType(uint32_t client_type, uint32_t reason, CMsgConn* pFromConn)
{
    for (map<uint32_t, CMsgConn*>::iterator it = m_conn_map.begin(); it != m_conn_map.end(); it++)
    {
        CMsgConn* pMsgConn = it->second;
        
        //16进制位移计算
        if ((((pMsgConn->GetClientType() ^ client_type) >> 4) == 0) && (pMsgConn != pFromConn)) {
            HandleKickUser(pMsgConn, reason);
            break;
        }
    }
    return true;
}

uint32_t CImUser::GetClientTypeFlag()
{
    uint32_t client_type_flag = 0x00;
    map<uint32_t, CMsgConn*>::iterator it = m_conn_map.begin();
    for (; it != m_conn_map.end(); it++)
    {
        CMsgConn* pConn = it->second;
        uint32_t client_type = pConn->GetClientType();
        if (CHECK_CLIENT_TYPE_PC(client_type))
        {
            client_type_flag |= CLIENT_TYPE_FLAG_PC;
        }
        else if (CHECK_CLIENT_TYPE_MOBILE(client_type))
        {
            client_type_flag |= CLIENT_TYPE_FLAG_MOBILE;
        }
    }
    return client_type_flag;
}


CImUserManager::~CImUserManager()
{
    RemoveAll();
}

CImUserManager* CImUserManager::GetInstance()
{
    static CImUserManager s_manager;
    return &s_manager;
}


void CImUserManager::GetOnlineRecommendList(uint32_t page, uint32_t page_size, IM::Buddy::IMRecommendListRsp &resp)
{
	uint32_t start = 0;
	if(page * page_size > m_im_user_map.size()){
		start = m_im_user_map.size();
	}

	uint32_t end = page_size * (page + 1) > m_im_user_map.size() ? m_im_user_map.size() : page * (page_size + 1);

	ImUserMap_t::iterator it = m_im_user_map.begin();
	uint32_t i = 0;
	for(;i < start; i++){
		it++;
	}

	CImUser *pUser = NULL;
	for(i= start; i<end; i++, it++){
		resp.add_recommend_list(it->first);
		pUser = it->second;
		resp.add_recommend_nick_list(pUser->GetNickName());
		log("recommend user %s", pUser->GetNickName().c_str());
	}

	log("start:%u end:%u m_im_user_map.size:%u", start, end, m_im_user_map.size());
}

CMsgConn* CImUserManager::GetMsgConnByHandle(string user_id, uint32_t handle)
{
    CMsgConn* pMsgConn = NULL;
    CImUser* pImUser = GetImUserById(user_id);
    if (pImUser) {
        pMsgConn = pImUser->GetMsgConn(handle);
    }
    return pMsgConn;
}


void CImUserManager::RemoveImUser(CImUser *pUser)
{
    if (pUser != NULL) {
        RemoveImUserById(pUser->GetUserId());//cita add
        delete pUser;
        pUser = NULL;
    }
}

void CImUserManager::RemoveAll()
{
    m_im_user_map.clear();
}

void CImUserManager::GetOnlineUserInfo(list<user_stat_t>* online_user_info)
{
    user_stat_t status;
    CImUser* pImUser = NULL;
    //m_im_user_map_by_pubkey : onilne user map
    for (ImUserMap_t::iterator it = m_im_user_map.begin(); it != m_im_user_map.end(); it++) {
        pImUser = (CImUser*)it->second;
        if (pImUser->IsValidate()) {
            map<uint32_t, CMsgConn*>& ConnMap = pImUser->GetMsgConnMap();
            for (map<uint32_t, CMsgConn*>::iterator it = ConnMap.begin(); it != ConnMap.end(); it++)
            {
                CMsgConn* pConn = it->second;
                if (pConn->IsOpen())
                {
                    status.user_id = pImUser->GetUserId();
                    status.client_type = pConn->GetClientType();
                    status.status = pConn->GetOnlineStatus();
                    online_user_info->push_back(status);
                }
            }
        }
    }
}

void CImUserManager::GetUserConnCnt(list<user_conn_t>* user_conn_list, uint32_t& total_conn_cnt)
{
    total_conn_cnt = 0;
    CImUser* pImUser = NULL;
    for (ImUserMap_t::iterator it = m_im_user_map.begin(); it != m_im_user_map.end(); it++)
    {
        pImUser = (CImUser*)it->second;
        if (pImUser->IsValidate())
        {
            user_conn_t user_conn_cnt = pImUser->GetUserConn();
            user_conn_list->push_back(user_conn_cnt);
            total_conn_cnt += user_conn_cnt.conn_cnt;
        }
    }
}

void CImUserManager::BroadcastPdu(CImPdu* pdu, uint32_t client_type_flag)
{
    CImUser* pImUser = NULL;
    for (ImUserMap_t::iterator it = m_im_user_map.begin(); it != m_im_user_map.end(); it++)
    {
        pImUser = (CImUser*)it->second;
        if (pImUser->IsValidate())
        {
            switch (client_type_flag) {
                case CLIENT_TYPE_FLAG_PC:
                    pImUser->BroadcastPduWithOutMobile(pdu);
                    break;
                case CLIENT_TYPE_FLAG_MOBILE:
                    pImUser->BroadcastPduToMobile(pdu);
                    break;
                case CLIENT_TYPE_FLAG_BOTH:
                    pImUser->BroadcastPdu(pdu);
                    break;
                default:
                    break;
            }
        }
    }
}


CImUser* CImUserManager::GetImUserById(string user_id)
{
    CImUser * pUser = NULL;
    ImUserMap_t::iterator it = m_im_user_map.find(user_id);
    if (it != m_im_user_map.end()) {
        pUser = it->second;
    }
    return pUser;
}


bool CImUserManager::AddImUserById(string user_id,CImUser *pUser)//cita add
{
    bool bRet = false;
    if (GetImUserById(user_id) == NULL) {
        m_im_user_map[user_id] = pUser;
        bRet = true;
    }
    return bRet;
}

void CImUserManager::RemoveImUserById(string user_id)//cita add
{
    m_im_user_map.erase(user_id);
}

void CImUserManager::GetAllUser(list<CImUser *> & userList)
{
    for (ImUserMap_t::iterator it = m_im_user_map.begin(); it != m_im_user_map.end(); it++)
    {
        userList.push_back(it->second);
    }
}
