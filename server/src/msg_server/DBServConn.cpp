/*
 * DBServConn.cpp
 *
 *  Created on: 2013-7-8
 *      Author: ziteng@mogujie.com
 */

#include "EncDec.h"
#include "DBServConn.h"
#include "MsgConn.h"
#include "RouteServConn.h"
#include "GroupChat.h"
#include "FileHandler.h"
#include "PushServConn.h"
#include "ImUser.h"
#include "security.h"
#include "AttachData.h"
#include "jsonxx.h"
#include "IM.BaseDefine.pb.h"
#include "IM.Other.pb.h"
#include "IM.Buddy.pb.h"
#include "IM.Login.pb.h"
#include "IM.Group.pb.h"
#include "IM.Message.pb.h"
#include "IM.Server.pb.h"
#include "IM.Blog.pb.h"
#include "ImPduBase.h"
#include "public_define.h"
//#include "EncDec.h"
//#include "json/json.h"
//#include "base_config.h"
using namespace IM::BaseDefine;

static ConnMap_t g_db_server_conn_map;

static serv_info_t* g_db_server_list = NULL;
static uint32_t		g_db_server_count = 0;			// 到DBServer的总连接数
static uint32_t		g_db_server_login_count = 0;	// 到进行登录处理的DBServer的总连接数
static CGroupChat*	s_group_chat = NULL;
static CFileHandler* s_file_handler = NULL;


extern CAes *pAes;

static void db_server_conn_timer_callback(void* callback_data, uint8_t msg, uint32_t handle, void* pParam)
{
	ConnMap_t::iterator it_old;
	CDBServConn* pConn = NULL;
	uint64_t cur_time = get_tick_count();

	for (ConnMap_t::iterator it = g_db_server_conn_map.begin(); it != g_db_server_conn_map.end(); ) {
		it_old = it;
		it++;

		pConn = (CDBServConn*)it_old->second;
		if (pConn->IsOpen()) {
			pConn->OnTimer(cur_time);
		}
	}

	// reconnect DB Storage Server
	// will reconnect in 4s, 8s, 16s, 32s, 64s, 4s 8s ...
	serv_check_reconnect<CDBServConn>(g_db_server_list, g_db_server_count);
}

void init_db_serv_conn(serv_info_t* server_list, uint32_t server_count, uint32_t concur_conn_cnt)
{
	g_db_server_list = server_list;
	g_db_server_count = server_count;

	uint32_t total_db_instance = server_count / concur_conn_cnt;
	g_db_server_login_count = (total_db_instance / 2) * concur_conn_cnt;
	log("DB server connection index for login business: [0, %u), for other business: [%u, %u) ",g_db_server_login_count, g_db_server_login_count, g_db_server_count);

	serv_init<CDBServConn>(g_db_server_list, g_db_server_count);

	netlib_register_timer(db_server_conn_timer_callback, NULL, 1000);
	s_group_chat = CGroupChat::GetInstance();
	s_file_handler = CFileHandler::getInstance();
}

// get a random db server connection in the range [start_pos, stop_pos)
static CDBServConn* get_db_server_conn_in_range(uint32_t start_pos, uint32_t stop_pos)
{
	uint32_t i = 0;
	CDBServConn* pDbConn = NULL;

	// determine if there is a valid DB server connection
	for (i = start_pos; i < stop_pos; i++) {
		pDbConn = (CDBServConn*)g_db_server_list[i].serv_conn;
		if (pDbConn && pDbConn->IsOpen()) {
			break;
		}
	}

	// no valid DB server connection
	if (i == stop_pos) {
		return NULL;
	}

	// return a random valid DB server connection
	while (true) {
		int i = rand() % (stop_pos - start_pos) + start_pos;
		pDbConn = (CDBServConn*)g_db_server_list[i].serv_conn;
		if (pDbConn && pDbConn->IsOpen()) {
			break;
		}
	}

	return pDbConn;
}

CDBServConn* get_db_serv_conn_for_login()
{
	// 先获取login业务的实例，没有就去获取其他业务流程的实例
	CDBServConn* pDBConn = get_db_server_conn_in_range(0, g_db_server_login_count);
	if (!pDBConn) {
		pDBConn = get_db_server_conn_in_range(g_db_server_login_count, g_db_server_count);
	}

	return pDBConn;
}

CDBServConn* get_db_serv_conn()
{
	// 先获取其他业务流程的实例，没有就去获取login业务的实例
	CDBServConn* pDBConn = get_db_server_conn_in_range(g_db_server_login_count, g_db_server_count);
	if (!pDBConn) {
		pDBConn = get_db_server_conn_in_range(0, g_db_server_login_count);
	}

	return pDBConn;
}


CDBServConn::CDBServConn()
{
	m_bOpen = false;
}

CDBServConn::~CDBServConn()
{

}

void CDBServConn::Connect(const char* server_ip, uint16_t server_port, uint32_t serv_idx)
{
	log("Connecting to DB Storage Server %s:%d ", server_ip, server_port);

	m_serv_idx = serv_idx;
	m_handle = netlib_connect(server_ip, server_port, imconn_callback, (void*)&g_db_server_conn_map);

	if (m_handle != NETLIB_INVALID_HANDLE) {
		g_db_server_conn_map.insert(make_pair(m_handle, this));
	}
}

void CDBServConn::Close()
{
	// reset server information for the next connect
	serv_reset<CDBServConn>(g_db_server_list, g_db_server_count, m_serv_idx);

	if (m_handle != NETLIB_INVALID_HANDLE) {
		netlib_close(m_handle);
		g_db_server_conn_map.erase(m_handle);
	}

	ReleaseRef();
}

void CDBServConn::OnConfirm()
{
	log("connect to db server success");
	m_bOpen = true;
	g_db_server_list[m_serv_idx].reconnect_cnt = MIN_RECONNECT_CNT / 2;
}

void CDBServConn::OnClose()
{
	log("onclose from db server handle=%d", m_handle);
	Close();
}

void CDBServConn::OnTimer(uint64_t curr_tick)
{
	if (curr_tick > m_last_send_tick + SERVER_HEARTBEAT_INTERVAL) {
        IM::Other::IMHeartBeat msg;
        CImPdu pdu;
        pdu.SetPBMsg(&msg);
        pdu.SetServiceId(SID_OTHER);
        pdu.SetCommandId(CID_OTHER_HEARTBEAT);
		SendPdu(&pdu);
	}

	if (curr_tick > m_last_recv_tick + SERVER_TIMEOUT) {
		log("conn to db server timeout");
		Close();
	}
}

void CDBServConn::HandlePdu(CImPdu* pPdu)
{
	switch (pPdu->GetCommandId()) {
        case CID_OTHER_HEARTBEAT:
            break;
        case CID_OTHER_VALIDATE_RSP:
            _HandleValidateResponse(pPdu );
            break;
        case CID_LOGIN_RES_DEVICETOKEN:
            _HandleSetDeviceTokenResponse(pPdu);
            break;
        case CID_LOGIN_RES_PUSH_SHIELD:
            _HandlePushShieldResponse(pPdu);
            break;
        case CID_LOGIN_RES_QUERY_PUSH_SHIELD:
            _HandleQueryPushShieldResponse(pPdu);
            break;
        case CID_MSG_UNREAD_CNT_RESPONSE:
            _HandleUnreadMsgCountResponse( pPdu );
            break;
        case CID_MSG_LIST_RESPONSE:
            _HandleGetMsgListResponse(pPdu);
            break;
        case CID_MSG_GET_BY_MSG_ID_RES:
            _HandleGetMsgByIdResponse(pPdu);
            break;
        case CID_MSG_DATA:
            _HandleMsgData(pPdu);
            break;
        case CID_MSG_GET_LATEST_MSG_ID_RSP:
            _HandleGetLatestMsgIDRsp(pPdu);
            break;
        case CID_BUDDY_LIST_RECENT_CONTACT_SESSION_RESPONSE:
            _HandleRecentSessionResponse(pPdu);
            break;
        case CID_BUDDY_LIST_ALL_USER_RESPONSE:
            _HandleAllUserResponse(pPdu);
            break;
        case CID_BUDDY_LIST_SEARCH_USER_RESPONSE:
			_HandleSearchUserResponse(pPdu);
			break;
        case CID_BUDDY_LIST_DEL_FRIEND_RESPONSE:
            _HandleDelFriendResponse(pPdu);
            break;
        case CID_BUDDY_LIST_FOLLOW_USER_RESPONSE:
            _HandleFollowUserResponse(pPdu);
            break;
        case CID_BUDDY_LIST_DEL_FOLLOW_USER_RESPONSE:
            _HandleDelFollowUserResponse(pPdu);
            break;
        case CID_BLOG_SEND_ACK:
            _HandleMsgBlogAck(pPdu);
            break;
        case CID_BLOG_GET_LIST_RESPONSE:
        	_HandleGetBlogListResponse(pPdu);
            break;
        case CID_BLOG_ADD_COMMENT_RESPONSE:
            _HandleAddBlogCommentResponse(pPdu);
            break;
        case CID_BLOG_GET_COMMENT_RESPONSE:
            _HandleGetBlogCommentResponse(pPdu);
            break;
        case CID_BUDDY_LIST_USER_INFO_RESPONSE:
            _HandleUsersInfoResponse(pPdu );
            break;
        case CID_BUDDY_LIST_REMOVE_SESSION_RES:
            _HandleRemoveSessionResponse(pPdu );
            break;
        case CID_BUDDY_LIST_CHANGE_AVATAR_RESPONSE:
            _HandleChangeAvatarResponse(pPdu);
            break;
        case CID_BUDDY_LIST_CHANGE_SIGN_INFO_RESPONSE:
            _HandleChangeSignInfoResponse(pPdu);
            break;
        case CID_BUDDY_LIST_DEPARTMENT_RESPONSE:
            _HandleDepartmentResponse(pPdu);
            break;
        case CID_BUDDY_LIST_UPDATE_USER_INFO_RESPONSE:
        	_HandleUpdateUserInfoResponse(pPdu);
        	break;
        case CID_OTHER_GET_DEVICE_TOKEN_RSP:
            _HandleGetDeviceTokenResponse(pPdu);
            break;
        case CID_OTHER_GET_SHIELD_RSP:
            s_group_chat->HandleGroupGetShieldByGroupResponse(pPdu);
            break;
        case CID_OTHER_STOP_RECV_PACKET:
            _HandleStopReceivePacket(pPdu);
            break;
        //group
        case CID_GROUP_NORMAL_LIST_RESPONSE:
            s_group_chat->HandleGroupNormalResponse( pPdu );
            break;
        case CID_GROUP_INFO_RESPONSE:
            s_group_chat->HandleGroupInfoResponse(pPdu);
            break;
        case CID_GROUP_CREATE_RESPONSE:
            s_group_chat->HandleGroupCreateResponse(pPdu);
            break;
        case CID_GROUP_CHANGE_MEMBER_RESPONSE:
            s_group_chat->HandleGroupChangeMemberResponse(pPdu);
            break;
        case CID_GROUP_SHIELD_GROUP_RESPONSE:
            s_group_chat->HandleGroupShieldGroupResponse(pPdu);
            break;
        
        //cita add: 推荐群from db_server
        case CID_GROUP_RECOMMAND_LIST_INFO_RESPONSE:
            s_group_chat->HandleGetRecommandGroudListResponse(pPdu);
            break;

        
        case CID_FILE_HAS_OFFLINE_RES:
            s_file_handler->HandleFileHasOfflineRes(pPdu);
            break;
        
        case CID_SYS_MSG_SEND_DATA_RESPONSE:
        	_HandleSendSysMsgResponse(pPdu);
            break;
        case CID_SYS_MSG_GET_UNREAD_CNT_RESPONSE:
        	_HandleGetUnreadSysMsgCntResponse(pPdu);
        	break;
        case CID_SYS_MSG_GET_DATA_RESPONSE:
            _HandleGetSysMsgDataResponse(pPdu);
            break;
        case CID_SYS_GET_STUDY_TIME_RESPONSE:
        	_HandleGetStudyTimeResponse(pPdu);
        	break;
        //cita add:
        case CID_BUDDY_LIST_GET_ALL_ONLINE_USER_INFO_RESPONSE:
            _HandleGetAllOnlineUserResponse(pPdu);
            break;
        default:
            log("db server, wrong cmd id=%d ", pPdu->GetCommandId());
	}
}

void CDBServConn::_HandleValidateResponse(CImPdu* pPdu)
{
    IM::Server::IMValidateRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    string login_name = msg.user_name();
    uint32_t result = msg.result_code();
    string result_string = msg.result_string();
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    log("HandleValidateResp, user_name=%s, result=%d", login_name.c_str(), result);
    
    CImUser* pImUser = CImUserManager::GetInstance()->GetImUserById(login_name);
    CMsgConn* pMsgConn = NULL;
    if (!pImUser) {
        log("ImUser for user_name=%s not exist", login_name.c_str());
        return;
    } else {
        pMsgConn = pImUser->GetUnValidateMsgConn(attach_data.GetHandle());
        if (!pMsgConn || pMsgConn->IsOpen()) {
            log("no such conn is validated, user_name=%s", login_name.c_str());
            return;
        }
    }
    
    if (result != 0) {
        result = IM::BaseDefine::REFUSE_REASON_DB_VALIDATE_FAILED;
    }
    
    if (result == 0)
    {
        IM::BaseDefine::UserInfo user_info = msg.user_info();
        string user_id = user_info.user_id();
        CImUser* pUser = CImUserManager::GetInstance()->GetImUserById(user_id);
        /*if (pUser) //cita:不需要做无效连接处理
        {
            pUser->AddUnValidateMsgConn(pMsgConn);
            pImUser->DelUnValidateMsgConn(pMsgConn);
            if (pImUser->IsMsgConnEmpty())
            {
                CImUserManager::GetInstance()->RemoveImUserById(user_id);
                delete pImUser;
            }
        }
        else
        {
            pUser = pImUser;
        }
        */
       if(!pUser)
         pUser = pImUser;

        pUser->SetUserId(user_id);
        pUser->SetNickName(user_info.user_nick_name());
        pUser->SetValidated();
        CImUserManager::GetInstance()->AddImUserById(user_id, pUser);
        
        //pUser->KickOutSameClientType(pMsgConn->GetClientType(), IM::BaseDefine::KICK_REASON_DUPLICATE_USER, pMsgConn);//wystan modify for kickout same user 200628
        
        CRouteServConn* pRouteConn = get_route_serv_conn();
        if (pRouteConn) {
            IM::Server::IMServerKickUser msg2;
            msg2.set_user_id(user_id);
            msg2.set_client_type((::IM::BaseDefine::ClientType)pMsgConn->GetClientType());
            msg2.set_reason(1);
            CImPdu pdu;
            pdu.SetPBMsg(&msg2);
            pdu.SetServiceId(SID_OTHER);
            pdu.SetCommandId(CID_OTHER_SERVER_KICK_USER);
            pRouteConn->SendPdu(&pdu);
        }
        
        log("user_name: %s, uid: %s, Handle:%u .", login_name.c_str(), user_id.c_str(), pMsgConn->GetHandle());
        pMsgConn->SetUserId(user_id);
        pMsgConn->SetOpen();
        pMsgConn->SendUserStatusUpdate(IM::BaseDefine::USER_STATUS_ONLINE);
        pUser->ValidateMsgConn(pMsgConn->GetHandle(), pMsgConn);

        IM::Login::IMLoginRes msg3;
        msg3.set_server_time(time(NULL));
        msg3.set_result_code(IM::BaseDefine::REFUSE_REASON_NONE);
        msg3.set_result_string(result_string);
        msg3.set_online_status((IM::BaseDefine::UserStatType)pMsgConn->GetOnlineStatus());
        IM::BaseDefine::UserInfo* user_info_tmp = msg3.mutable_user_info();
        *user_info_tmp = user_info;
        user_info_tmp->set_user_id(login_name);
        IM::BaseDefine::GroupInfo* group_info_tmp = msg3.mutable_public_group_info();
        *group_info_tmp = msg.public_group_info();

        CImPdu pdu2;
        pdu2.SetPBMsg(&msg3);
        pdu2.SetServiceId(SID_LOGIN);
        pdu2.SetCommandId(CID_LOGIN_RES_USERLOGIN);
        pdu2.SetSeqNum(pPdu->GetSeqNum());
        pMsgConn->SendPdu(&pdu2);
		pUser->KickOutSameClientType(pMsgConn->GetClientType(), IM::BaseDefine::KICK_REASON_DUPLICATE_USER, pMsgConn);//wystan modify for kickout same user 200628
    }
    else
    {
        IM::Login::IMLoginRes msg4;
        msg4.set_server_time(time(NULL));
        msg4.set_result_code((IM::BaseDefine::ResultType)result);
        msg4.set_result_string(result_string);
        CImPdu pdu3;
        pdu3.SetPBMsg(&msg4);
        pdu3.SetServiceId(SID_LOGIN);
        pdu3.SetCommandId(CID_LOGIN_RES_USERLOGIN);
        pdu3.SetSeqNum(pPdu->GetSeqNum());
        pMsgConn->SendPdu(&pdu3);
        pMsgConn->Close();
    }
}

void CDBServConn::_HandleRecentSessionResponse(CImPdu *pPdu)
{
    IM::Buddy::IMRecentContactSessionRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    string user_id = msg.user_id();
    uint32_t session_cnt = msg.contact_session_list_size();
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();
    
    log("HandleRecentSessionResponse, userId=%s, session_cnt=%u", user_id.c_str(), session_cnt);
    
    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
    
    if (pMsgConn && pMsgConn->IsOpen())
    {
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pMsgConn->SendPdu(pPdu);
    }
}

void CDBServConn::_HandleAllUserResponse(CImPdu *pPdu)
{
    IM::Buddy::IMAllUserRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    string user_id = msg.user_id();
    uint32_t latest_update_time = msg.latest_update_time();
    uint32_t user_cnt = msg.user_list_size();
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();

    log("HandleAllUserResponse, userId=%s, latest_update_time=%u, user_cnt=%u", user_id.c_str(), latest_update_time, user_cnt);

    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);

    if (pMsgConn && pMsgConn->IsOpen())
    {
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pMsgConn->SendPdu(pPdu);
    }
}

void CDBServConn::_HandleSearchUserResponse(CImPdu *pPdu)
{
    IM::Buddy::IMSearchUserRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    string user_id = msg.user_id();
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();

    //log("HandleAllUserResponse, userId=%s, latest_update_time=%u", user_id.c_str(), latest_update_time);

    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
    log("search result list cout = %d",msg.search_user_list_size());
    if (pMsgConn && pMsgConn->IsOpen())
    {
        msg.clear_attach_data();
        pMsgConn->SendPdu(pPdu);
    }
}

void CDBServConn::_HandleMsgBlogAck(CImPdu *pPdu)
{
    IM::Blog::IMBlogSendAck blog;
    CHECK_PB_PARSE_MSG(blog.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    string user_id = blog.user_id();
    CDbAttachData attach_data((uchar_t*)blog.attach_data().c_str(), blog.attach_data().length());
    uint32_t handle = attach_data.GetHandle();

    log("_HandleMsgBlogAck, userId=%s", user_id.c_str());

    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);

    if (pMsgConn && pMsgConn->IsOpen())
    {
    	log("send Msg Blog Ack, userId=%s", user_id.c_str());
    	blog.clear_attach_data();
        pPdu->SetPBMsg(&blog);
        pMsgConn->SendPdu(pPdu);
    }
}

void CDBServConn::_HandleGetUnreadSysMsgCntResponse(CImPdu *pPdu)
{
	IM::System::IMSysMsgUnreadCntRsp msg;
	CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

	string user_id = msg.user_id();
	CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
	uint32_t handle = attach_data.GetHandle();

	CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);

	if (pMsgConn && pMsgConn->IsOpen())
	{
		msg.clear_attach_data();
		pPdu->SetPBMsg(&msg);
		pMsgConn->SendPdu(pPdu);
	}
}

void CDBServConn::_HandleGetSysMsgDataResponse(CImPdu *pPdu)
{
	IM::System::IMGetSysMsgDataRsp msg;
	CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

	string user_id = msg.user_id();
	CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
	uint32_t handle = attach_data.GetHandle();

	CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);

	if (pMsgConn && pMsgConn->IsOpen())
	{
		msg.clear_attach_data();
		pPdu->SetPBMsg(&msg);
		pMsgConn->SendPdu(pPdu);
	}
}

void CDBServConn::_HandleSendSysMsgResponse(CImPdu *pPdu)
{
    IM::System::IMSendSysMsgRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    IM::System::IMSysMsgData sysMsg = msg.sys_msg();
    string user_id = msg.user_id();
    string to_id = sysMsg.to_id();
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();

    //log("HandleAllUserResponse, userId=%s, latest_update_time=%u, user_cnt=%u", user_id.c_str(), latest_update_time, user_cnt);

    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);

    if (pMsgConn && pMsgConn->IsOpen())
    {
    	//回给请求发起client
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pMsgConn->SendPdu(pPdu);
    }

    IM::System::IMSysMsgData msg2;
    msg2.set_from_id(user_id);
    msg2.set_to_id(to_id);
    msg2.set_type(sysMsg.type());
    msg2.set_attach_data(sysMsg.attach_data());
	CImPdu pdu2;
	pdu2.SetPBMsg(&msg2);
	pdu2.SetServiceId(IM::BaseDefine::SID_SYS_MSG);
	pdu2.SetCommandId(IM::BaseDefine::CID_SYS_MSG_DATA);

    //发到router
    CRouteServConn* pRouteConn = get_route_serv_conn();
    if (pRouteConn) {
        pRouteConn->SendPdu(&pdu2);
    }

    CImUser* pFriendImUser = CImUserManager::GetInstance()->GetImUserById(to_id);

    if (pFriendImUser) {
    	pFriendImUser->BroadcastClientMsgData(&pdu2, 0, NULL, user_id);
    }
}

void CDBServConn::_HandleDelFriendResponse(CImPdu *pPdu)
{
    IM::Buddy::IMDelFriendRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    string user_id = msg.user_id();
    string friend_id = msg.friend_id();
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();

    //log("HandleAllUserResponse, userId=%s, latest_update_time=%u, user_cnt=%u", user_id.c_str(), latest_update_time, user_cnt);

    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);

    if (pMsgConn && pMsgConn->IsOpen())
    {
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pMsgConn->SendPdu(pPdu);
    }

    //删除好友，不用通知被删除用户，所以不用发到router

}

void CDBServConn::_HandleFollowUserResponse(CImPdu *pPdu)
{
    IM::Buddy::IMFollowUserRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    string user_id = msg.user_id();
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();

    //log("HandleAllUserResponse, userId=%s, latest_update_time=%u, user_cnt=%u", user_id.c_str(), latest_update_time, user_cnt);

    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);

    if (pMsgConn && pMsgConn->IsOpen())
    {
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pMsgConn->SendPdu(pPdu);
    }
}

void CDBServConn::_HandleDelFollowUserResponse(CImPdu *pPdu)
{
    IM::Buddy::IMDelFollowUserRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    string user_id = msg.user_id();
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();

    //log("HandleAllUserResponse, userId=%s, latest_update_time=%u", user_id.c_str(), latest_update_time);

    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);

    if (pMsgConn && pMsgConn->IsOpen())
    {
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pMsgConn->SendPdu(pPdu);
    }
}

void CDBServConn::_HandleGetBlogListResponse(CImPdu *pPdu)
{
    IM::Blog::IMBlogGetListRsp blog;
    CHECK_PB_PARSE_MSG(blog.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    string user_id = blog.user_id();
    //uint32_t session_type = msg.session_type();
    //string session_id = msg.session_id();
    //uint32_t msg_cnt = msg.msg_list_size();
    //uint32_t msg_id_begin = msg.msg_id_begin();
    CDbAttachData attach_data((uchar_t*)blog.attach_data().c_str(), blog.attach_data().length());
    uint32_t handle = attach_data.GetHandle();

    //log("HandleGetMsgListResponse, userId=%s, session_type=%u, opposite_user_id=%s, msg_id_begin=%u, cnt=%u.", user_id.c_str(), session_type, session_id.c_str(), msg_id_begin, msg_cnt);

    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
    if (pMsgConn && pMsgConn->IsOpen()) {
        blog.clear_attach_data();
        pPdu->SetPBMsg(&blog);
        pMsgConn->SendPdu(pPdu);
    }
}

void CDBServConn::_HandleAddBlogCommentResponse(CImPdu *pPdu)
{
    IM::Blog::IMBlogAddCommentRsp blog;
    CHECK_PB_PARSE_MSG(blog.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    string user_id = blog.user_id();
    CDbAttachData attach_data((uchar_t*)blog.attach_data().c_str(), blog.attach_data().length());
    uint32_t handle = attach_data.GetHandle();

    log("HandleAddBlogCommentResponse, userId=%s", user_id.c_str());

    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
    if (pMsgConn && pMsgConn->IsOpen()) {
    	blog.clear_attach_data();
        pPdu->SetPBMsg(&blog);
        pMsgConn->SendPdu(pPdu);
    }
}

void CDBServConn::_HandleGetBlogCommentResponse(CImPdu *pPdu)
{
    IM::Blog::IMBlogGetCommentRsp blog;
    CHECK_PB_PARSE_MSG(blog.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    string user_id = blog.user_id();
    CDbAttachData attach_data((uchar_t*)blog.attach_data().c_str(), blog.attach_data().length());
    uint32_t handle = attach_data.GetHandle();

    log("HandleGetBlogCommentResponse, userId=%s", user_id.c_str());

    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
    if (pMsgConn && pMsgConn->IsOpen()) {
    	blog.clear_attach_data();
        pPdu->SetPBMsg(&blog);
        pMsgConn->SendPdu(pPdu);
    }
}

void CDBServConn::_HandleGetStudyTimeResponse(CImPdu *pPdu)
{
	IM::System::IMSysGetStudyTimeRsp msg;
	CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

	string user_id = msg.user_id();
	CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
	uint32_t handle = attach_data.GetHandle();

	log("_HandleGetStudyTimeResponse, userId=%s", user_id.c_str());

	CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
	if (pMsgConn && pMsgConn->IsOpen()) {
		msg.clear_attach_data();
		pPdu->SetPBMsg(&msg);
		pMsgConn->SendPdu(pPdu);
	}

}

void CDBServConn::_HandleGetMsgListResponse(CImPdu *pPdu)
{
    IM::Message::IMGetMsgListRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    string user_id = msg.user_id();
    uint32_t session_type = msg.session_type();
    string session_id = msg.session_id();
    uint32_t msg_cnt = msg.msg_list_size();
    uint32_t msg_id_begin = msg.msg_id_begin();
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();
    
    log("HandleGetMsgListResponse, userId=%s, session_type=%u, opposite_user_id=%s, msg_id_begin=%u, cnt=%u.", user_id.c_str(), session_type, session_id.c_str(), msg_id_begin, msg_cnt);
    
    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
    if (pMsgConn && pMsgConn->IsOpen()) {
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pMsgConn->SendPdu(pPdu);
    }
}

void CDBServConn::_HandleGetMsgByIdResponse(CImPdu *pPdu)
{
    IM::Message::IMGetMsgByIdRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    
    string user_id = msg.user_id();
    uint32_t session_type = msg.session_type();
    string session_id = msg.session_id();
    uint32_t msg_cnt = msg.msg_list_size();
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();
    
    log("HandleGetMsgByIdResponse, userId=%s, session_type=%u, opposite_user_id=%s, cnt=%u.", user_id.c_str(), session_type, session_id.c_str(), msg_cnt);
    
    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
    if (pMsgConn && pMsgConn->IsOpen()) {
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pMsgConn->SendPdu(pPdu);
    }
}

//这个应是db,收到msg_server的msg_data请求后，再回给msg_server的
void CDBServConn::_HandleMsgData(CImPdu *pPdu)
{
	
    IM::Message::IMMsgData msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    if (CHECK_MSG_TYPE_GROUP(msg.msg_type())) {
        s_group_chat->HandleGroupMessage(pPdu);
        return;
    }
    
    string from_user_id = msg.from_user_id();
    string to_user_id = msg.to_session_id();
    uint32_t msg_id = msg.msg_id();
    uint8_t msg_type = msg.msg_type();
    if (msg_id == 0 && msg_type != ::IM::BaseDefine::MSG_TYPE_ERROR_NOT_FRIEND) {
        log("HandleMsgData, write db failed, %s->%s.", from_user_id.c_str(), to_user_id.c_str());
        return;
    }
    
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();
    
    log("HandleMsgData, from_user_id=%s, to_user_id=%s, msg_id=%u.", from_user_id.c_str(), to_user_id.c_str(), msg_id);
    
    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(from_user_id, attach_data.GetHandle());
    if (pMsgConn)
    {
        IM::Message::IMMsgDataAck msg2;
        msg2.set_user_id(from_user_id);
        msg2.set_msg_id(msg_id);
        msg2.set_session_id(to_user_id);
        if(msg_type == ::IM::BaseDefine::MSG_TYPE_ERROR_NOT_FRIEND){
        	msg2.set_session_type(::IM::BaseDefine::SESSION_TYPE_ERROR_NOT_FRIEND);
        }else{
        	msg2.set_session_type(::IM::BaseDefine::SESSION_TYPE_SINGLE);
        }

        CImPdu pdu;
        pdu.SetPBMsg(&msg2);
        pdu.SetServiceId(SID_MSG);
        pdu.SetCommandId(CID_MSG_DATA_ACK);
        pdu.SetSeqNum(pPdu->GetSeqNum());
        pMsgConn->SendPdu(&pdu);

        if(msg_type == ::IM::BaseDefine::MSG_TYPE_ERROR_NOT_FRIEND){
        	return;
        }
    }//是不是client收到ack，才显示消息发送成功?
    
    //上面只是给发送方一个ack，表示发送到了db，但还需发送到接送方 neil
    CRouteServConn* pRouteConn = get_route_serv_conn();
    if (pRouteConn) {
        pRouteConn->SendPdu(pPdu);
    }
    msg.clear_attach_data();
    pPdu->SetPBMsg(&msg);
    CImUser* pFromImUser = CImUserManager::GetInstance()->GetImUserById(from_user_id);
    CImUser* pToImUser = CImUserManager::GetInstance()->GetImUserById(to_user_id);
    pPdu->SetSeqNum(0);
    //应该是发送给同一账号登录的其它的client端 neil
    if (pFromImUser) {
        pFromImUser->BroadcastClientMsgData(pPdu, msg_id, pMsgConn, from_user_id);
    }

    if (pToImUser) {
        pToImUser->BroadcastClientMsgData(pPdu, msg_id, NULL, from_user_id);
    }
	else{ //wystan modify for invaild send 200612
	    CRouteServConn* pRouteConn = get_route_serv_conn();
	    if (pRouteConn) {
	        pRouteConn->SendPdu(pPdu);
	    }
	}
    
    IM::Server::IMGetDeviceTokenReq msg3;
    msg3.add_user_id(to_user_id);
    msg3.set_attach_data(pPdu->GetBodyData(), pPdu->GetBodyLength());
    CImPdu pdu2;
    pdu2.SetPBMsg(&msg3);
    pdu2.SetServiceId(SID_OTHER);
    pdu2.SetCommandId(CID_OTHER_GET_DEVICE_TOKEN_REQ);
    SendPdu(&pdu2); //应该是发到与其相连的db server
}

void CDBServConn::_HandleGetLatestMsgIDRsp(CImPdu *pPdu)
{
    IM::Message::IMGetLatestMsgIdRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    string user_id = msg.user_id();
    string session_id = msg.session_id();
    uint32_t session_type = msg.session_type();
    uint32_t latest_msg_id = msg.latest_msg_id();
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();
    
    log("HandleUnreadMsgCntResp, userId=%s, session_id=%s, session_type=%u, latest_msg_id=%u.", user_id.c_str(), session_id.c_str(), session_type, latest_msg_id);
    
    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
    if (pMsgConn && pMsgConn->IsOpen()) {
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pMsgConn->SendPdu(pPdu);
    }
}

void CDBServConn::_HandleUnreadMsgCountResponse(CImPdu* pPdu)
{
    IM::Message::IMUnreadMsgCntRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

	string user_id = msg.user_id();
    uint32_t total_cnt = msg.total_cnt();
	uint32_t user_unread_cnt = msg.unreadinfo_list_size();
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
	uint32_t handle = attach_data.GetHandle();
	
	log("HandleUnreadMsgCntResp, userId=%s, total_cnt=%u, user_unread_cnt=%u.", user_id.c_str(),total_cnt, user_unread_cnt);

    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);

	if (pMsgConn && pMsgConn->IsOpen()) {
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pMsgConn->SendPdu(pPdu);
	}
}

void CDBServConn::_HandleUsersInfoResponse(CImPdu* pPdu)
{
    IM::Buddy::IMUsersInfoRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    string user_id = msg.user_id();
    uint32_t user_cnt = msg.user_info_list_size();
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
	uint32_t handle = attach_data.GetHandle();
    
    log("HandleUsersInfoResp, user_id=%s, user_cnt=%u.", user_id.c_str(), user_cnt);
    
    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
    if (pMsgConn && pMsgConn->IsOpen()) {
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pMsgConn->SendPdu(pPdu);
    }
}

void CDBServConn::_HandleStopReceivePacket(CImPdu* pPdu)
{
	log("HandleStopReceivePacket, from %s:%d.",g_db_server_list[m_serv_idx].server_ip.c_str(), g_db_server_list[m_serv_idx].server_port);

	m_bOpen = false;
}

void CDBServConn::_HandleRemoveSessionResponse(CImPdu* pPdu)
{
    IM::Buddy::IMRemoveSessionRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

	string user_id = msg.user_id();
	uint32_t result = msg.result_code();
	uint32_t session_type = msg.session_type();
	string session_id = msg.session_id();
	log("HandleRemoveSessionResp, req_id=%s, result=%u, session_id=%s, type=%u.",user_id.c_str(), result, session_id.c_str(), session_type);

    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();
    CMsgConn* pConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
	if (pConn && pConn->IsOpen()) {
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pConn->SendPdu(pPdu);
	}
}

void CDBServConn::_HandleChangeAvatarResponse(CImPdu* pPdu)
{
    IM::Buddy::IMChangeAvatarRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    string user_id = msg.user_id();
    uint32_t result = msg.result_code();
    
	log("HandleChangeAvatarResp, user_id=%s, result=%u.", user_id.c_str(), result);
    
    CImUser* pUser = CImUserManager::GetInstance()->GetImUserById(user_id);
    if (NULL != pUser) {
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pUser->BroadcastPdu(pPdu);
    }
}

void CDBServConn::_HandleDepartmentResponse(CImPdu *pPdu)
{
    IM::Buddy::IMDepartmentRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    
    string user_id = msg.user_id();
    uint32_t latest_update_time = msg.latest_update_time();
    uint32_t dept_cnt = msg.dept_list_size();
    log("HandleDepartmentResponse, user_id=%s, latest_update_time=%u, dept_cnt=%u.", user_id.c_str(), latest_update_time, dept_cnt);
    
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();
    CMsgConn* pConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
    if (pConn && pConn->IsOpen()) {
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pConn->SendPdu(pPdu);
    }
}

void CDBServConn::_HandleUpdateUserInfoResponse(CImPdu *pPdu)
{
	IM::Buddy::IMDepartmentRsp msg;
	CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

	string user_id = msg.user_id();
	log("user_id=%s", user_id.c_str());

	CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
	uint32_t handle = attach_data.GetHandle();
	CMsgConn* pConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
	if (pConn && pConn->IsOpen()) {
		msg.clear_attach_data();
		pPdu->SetPBMsg(&msg);
		pConn->SendPdu(pPdu);
	}
}

void CDBServConn::_HandleSetDeviceTokenResponse(CImPdu *pPdu)
{
    IM::Login::IMDeviceTokenRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    string user_id = msg.user_id();
    log("HandleSetDeviceTokenResponse, user_id = %s.", user_id.c_str());
}

void CDBServConn::_HandleGetDeviceTokenResponse(CImPdu *pPdu)
{
    IM::Server::IMGetDeviceTokenRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    
    IM::Message::IMMsgData msg2;
    CHECK_PB_PARSE_MSG(msg2.ParseFromArray(msg.attach_data().c_str(), msg.attach_data().length()));
    string msg_data = msg2.msg_data();
    uint32_t msg_type = msg2.msg_type();
    string from_id = msg2.from_user_id();
    string to_id = msg2.to_session_id();
    if (msg_type == IM::BaseDefine::MSG_TYPE_SINGLE_TEXT || msg_type == IM::BaseDefine::MSG_TYPE_GROUP_TEXT)
    {
        //msg_data =
        char* msg_out = NULL;
        uint32_t msg_out_len = 0;
        if (pAes->Decrypt(msg_data.c_str(), msg_data.length(), &msg_out, msg_out_len) == 0)
        {
            msg_data = string(msg_out, msg_out_len);
        }
        else
        {
            log("HandleGetDeviceTokenResponse, decrypt msg failed, from_id: %s, to_id: %s, msg_type: %u.", from_id.c_str(), to_id.c_str(), msg_type);
            return;
        }
        if(msg_out)
            pAes->Free(msg_out);
    }
    build_ios_push_flash(msg_data, msg2.msg_type(), from_id);
    //{
    //    "msg_type": 1,
    //    "from_id": "1345232",
    //    "group_type": "12353",
    //}
    jsonxx::Object json_obj;
    json_obj << "msg_type" << (uint32_t)msg2.msg_type();
    json_obj << "from_id" << from_id;
    if (CHECK_MSG_TYPE_GROUP(msg2.msg_type())) {
        json_obj << "group_id" << to_id;
    }
    
    uint32_t user_token_cnt = msg.user_token_info_size();
    log("HandleGetDeviceTokenResponse, user_token_cnt = %u.", user_token_cnt);
    
    IM::Server::IMPushToUserReq msg3;
    for (uint32_t i = 0; i < user_token_cnt; i++)
    {
        IM::BaseDefine::UserTokenInfo user_token = msg.user_token_info(i);
        string user_id = user_token.user_id();
        string device_token = user_token.token();
        uint32_t push_cnt = user_token.push_count();
        uint32_t client_type = user_token.user_type();
        //自己发得消息不给自己发推送
        if (from_id == user_id) {
            continue;
        }
        
        log("HandleGetDeviceTokenResponse, user_id = %s, device_token = %s, push_cnt = %u, client_type = %u.",user_id.c_str(), device_token.c_str(), push_cnt, client_type);
        
        CImUser* pUser = CImUserManager::GetInstance()->GetImUserById(user_id);
        if (pUser)
        {
            msg3.set_flash(msg_data);
            msg3.set_data(json_obj.json());
            IM::BaseDefine::UserTokenInfo* user_token_tmp = msg3.add_user_token_list();
            user_token_tmp->set_user_id(user_id);
            user_token_tmp->set_user_type((IM::BaseDefine::ClientType)client_type);
            user_token_tmp->set_token(device_token);
            user_token_tmp->set_push_count(push_cnt);
            //pc client登录，则为勿打扰式推送
            if (pUser->GetPCLoginStatus() == IM_PC_LOGIN_STATUS_ON)
            {
                user_token_tmp->set_push_type(IM_PUSH_TYPE_SILENT);
                log("HandleGetDeviceTokenResponse, user id: %s, push type: silent.", user_id.c_str());
            }
            else
            {
                user_token_tmp->set_push_type(IM_PUSH_TYPE_NORMAL);
                log("HandleGetDeviceTokenResponse, user id: %s, push type: normal.", user_id.c_str());
            }
        }
        else
        {
            IM::Server::IMPushToUserReq msg4;
            msg4.set_flash(msg_data);
            msg4.set_data(json_obj.json());
            IM::BaseDefine::UserTokenInfo* user_token_tmp = msg4.add_user_token_list();
            user_token_tmp->set_user_id(user_id);
            user_token_tmp->set_user_type((IM::BaseDefine::ClientType)client_type);
            user_token_tmp->set_token(device_token);
            user_token_tmp->set_push_count(push_cnt);
            user_token_tmp->set_push_type(IM_PUSH_TYPE_NORMAL);
            CImPdu pdu;
            pdu.SetPBMsg(&msg4);
            pdu.SetServiceId(SID_OTHER);
            pdu.SetCommandId(CID_OTHER_PUSH_TO_USER_REQ);
            
            CPduAttachData attach_data(ATTACH_TYPE_PDU_FOR_PUSH, 0, pdu.GetBodyLength(), pdu.GetBodyData());
            IM::Buddy::IMUsersStatReq msg5;
            msg5.set_user_id("");
            msg5.add_user_id_list(user_id);
            msg5.set_attach_data(attach_data.GetBuffer(), attach_data.GetLength());
            CImPdu pdu2;
            pdu2.SetPBMsg(&msg5);
            pdu2.SetServiceId(SID_BUDDY_LIST);
            pdu2.SetCommandId(CID_BUDDY_LIST_USERS_STATUS_REQUEST);
            CRouteServConn* route_conn = get_route_serv_conn();
            if (route_conn)
            {
                route_conn->SendPdu(&pdu2);
            }
        }
    }
    
    if (msg3.user_token_list_size() > 0)
    {
        CImPdu pdu3;
        pdu3.SetPBMsg(&msg3);
        pdu3.SetServiceId(SID_OTHER);
        pdu3.SetCommandId(CID_OTHER_PUSH_TO_USER_REQ);
        
        CPushServConn* PushConn = get_push_serv_conn();
        if (PushConn) {
            PushConn->SendPdu(&pdu3);
        }
    }
	
}

void CDBServConn::_HandleChangeSignInfoResponse(CImPdu* pPdu) {
        IM::Buddy::IMChangeSignInfoRsp msg;
        CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    
        string user_id = msg.user_id();
        uint32_t result = msg.result_code();
    
        log("HandleChangeSignInfoResp: user_id=%s, result=%u.", user_id.c_str(), result);
    
        CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
        uint32_t handle = attach_data.GetHandle();
    
        CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
    
        if (pMsgConn && pMsgConn->IsOpen()) {
                msg.clear_attach_data();
                pPdu->SetPBMsg(&msg);
                pMsgConn->SendPdu(pPdu);
        }else {
                   log("HandleChangeSignInfoResp: can't found msg_conn by user_id = %s, handle = %u", user_id.c_str(), handle);

        }
    
        if (!result) {
                CRouteServConn* route_conn = get_route_serv_conn();
                if (route_conn) {
                        IM::Buddy::IMSignInfoChangedNotify notify_msg;
                        notify_msg.set_changed_user_id(user_id);//cita remodify
                        notify_msg.set_sign_info(msg.sign_info());
            
                        CImPdu notify_pdu;
                        notify_pdu.SetPBMsg(&notify_msg);
                        notify_pdu.SetServiceId(SID_BUDDY_LIST);
                        notify_pdu.SetCommandId(CID_BUDDY_LIST_SIGN_INFO_CHANGED_NOTIFY);
            
                        route_conn->SendPdu(&notify_pdu);
                }else {
                            log("HandleChangeSignInfoResp: can't found route_conn");
                    
                }
           }
    }


void CDBServConn::_HandlePushShieldResponse(CImPdu* pPdu) {
    IM::Login::IMPushShieldRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    
    string user_id = msg.user_id();
    uint32_t result = msg.result_code();
    
    log("_HandlePushShieldResponse: user_id=%s, result=%u.", user_id.c_str(), result);
    
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();
    
    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
    
    if (pMsgConn && pMsgConn->IsOpen()) {
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pMsgConn->SendPdu(pPdu);
    } else {
        log("_HandlePushShieldResponse: can't found msg_conn by user_id = %s, handle = %u", user_id.c_str(), handle);
    }
}

void CDBServConn::_HandleQueryPushShieldResponse(CImPdu* pPdu) {
    IM::Login::IMQueryPushShieldRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    
    string user_id = msg.user_id();
    uint32_t result = msg.result_code();
    // uint32_t shield_status = msg.shield_status();
    
    log("_HandleQueryPushShieldResponse: user_id=%s, result=%u.", user_id.c_str(), result);
    
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();
    
    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
    
    if (pMsgConn && pMsgConn->IsOpen()) {
        msg.clear_attach_data();
        pPdu->SetPBMsg(&msg);
        pMsgConn->SendPdu(pPdu);
    } else {
        log("_HandleQueryPushShieldResponse: can't found msg_conn by user_id = %s, handle = %u", user_id.c_str(), handle);
    }
}

void CDBServConn::_HandleGetAllOnlineUserResponse(CImPdu *pPdu)
{
    IM::Buddy::IMGetALLOnlineUserRsp msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    string user_id = msg.user_id();
    CDbAttachData attach_data((uchar_t*)msg.attach_data().c_str(), msg.attach_data().length());
    uint32_t handle = attach_data.GetHandle();

    CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(user_id, handle);
    log("online list cout = %d",msg.user_list_size());
    if (pMsgConn && pMsgConn->IsOpen())
    {
        msg.clear_attach_data();
        pMsgConn->SendPdu(pPdu);
    }
}

