/*
 * MsgConn.cpp
 *
 *  Created on: 2013-7-5
 *      Author: ziteng@mogujie.com
 */

#include "MsgConn.h"
#include "DBServConn.h"
#include "LoginServConn.h"
#include "RouteServConn.h"
#include "FileHandler.h"
#include "GroupChat.h"
#include "ImUser.h"
#include "AttachData.h"
#include "IM.Buddy.pb.h"
#include "IM.Message.pb.h"
#include "IM.Login.pb.h"
#include "IM.Other.pb.h"
#include "IM.Group.pb.h"
#include "IM.Server.pb.h"
#include "IM.SwitchService.pb.h"
#include "IM.Blog.pb.h"
#include "public_define.h"
#include "ImPduBase.h"
#include "IM.BaseDefine.pb.h"
#include "IM.Friendship.pb.h"
#include "base_config.h"
#include "Base64.h"

using namespace IM::BaseDefine;

#define TIMEOUT_WATI_LOGIN_RESPONSE		15000	// 15 seconds
#define TIMEOUT_WAITING_MSG_DATA_ACK	15000	// 15 seconds
#define LOG_MSG_STAT_INTERVAL			300000	// log message miss status in every 5 minutes;
#define MAX_MSG_CNT_PER_SECOND			20	    // user can not send more than 20 msg in one second
static ConnMap_t g_msg_conn_map;
static UserMap_t g_msg_conn_user_map;

static uint64_t	g_last_stat_tick;	        // 上次显示丢包率信息的时间
static uint32_t g_up_msg_total_cnt = 0;		// 上行消息包总数
static uint32_t g_up_msg_miss_cnt = 0;		// 上行消息包丢数
static uint32_t g_down_msg_total_cnt = 0;	// 下行消息包总数
static uint32_t g_down_msg_miss_cnt = 0;	// 下行消息丢包数

static bool g_log_msg_toggle = true;	    // 是否把收到的MsgData写入Log的开关，通过kill -SIGUSR2 pid 打开/关闭

static CFileHandler* s_file_handler = NULL;
static CGroupChat* s_group_chat = NULL;

void msg_conn_timer_callback(void* callback_data, uint8_t msg, uint32_t handle, void* pParam)
{
	ConnMap_t::iterator it_old;
	CMsgConn* pConn = NULL;
	uint64_t cur_time = get_tick_count();

	for (ConnMap_t::iterator it = g_msg_conn_map.begin(); it != g_msg_conn_map.end(); ) {
		it_old = it;
		it++;
		pConn = (CMsgConn*)it_old->second;
		pConn->OnTimer(cur_time);
	}

	if (cur_time > g_last_stat_tick + LOG_MSG_STAT_INTERVAL) {
		g_last_stat_tick = cur_time;
		log("up_msg_cnt=%u, up_msg_miss_cnt=%u, down_msg_cnt=%u, down_msg_miss_cnt=%u ",
			g_up_msg_total_cnt, g_up_msg_miss_cnt, g_down_msg_total_cnt, g_down_msg_miss_cnt);
	}
}

static void signal_handler_usr1(int sig_no)
{
	if (sig_no == SIGUSR1) {
		log("receive SIGUSR1 ");
		g_up_msg_total_cnt = 0;
		g_up_msg_miss_cnt = 0;
		g_down_msg_total_cnt = 0;
		g_down_msg_miss_cnt = 0;
	}
}

static void signal_handler_usr2(int sig_no)
{
	if (sig_no == SIGUSR2) {
		log("receive SIGUSR2 ");
		g_log_msg_toggle = !g_log_msg_toggle;
	}
}

static void signal_handler_hup(int sig_no)
{
	if (sig_no == SIGHUP) {
		log("receive SIGHUP exit... ");
		exit(0);
	}
}

void init_msg_conn()
{
	g_last_stat_tick = get_tick_count();
	signal(SIGUSR1, signal_handler_usr1);
	signal(SIGUSR2, signal_handler_usr2);
	netlib_register_timer(msg_conn_timer_callback, NULL, 1000);
	s_file_handler = CFileHandler::getInstance();//cita delete
	s_group_chat = CGroupChat::GetInstance();
}

////////////////////////////
CMsgConn::CMsgConn()
{
    m_user_id = "";//cita delete
    m_bOpen = false;
    m_bKickOff = false;
    m_last_seq_no = 0;
    m_msg_cnt_per_sec = 0;
    m_send_msg_list.clear();
    m_online_status = IM::BaseDefine::USER_STATUS_OFFLINE;
    m_start_time = 0;
}

CMsgConn::~CMsgConn()
{

}

void CMsgConn::SendUserStatusUpdate(uint32_t user_status)
{
    if (!m_bOpen) {
		return;
	}
    
    CImUser* pImUser = CImUserManager::GetInstance()->GetImUserById(GetUserId());
    if (!pImUser) {
        return;
    }

    // 只有上下线通知才通知LoginServer
    if (user_status == ::IM::BaseDefine::USER_STATUS_ONLINE) {
       
        IM::Server::IMUserCntUpdate msg;
        msg.set_user_action(USER_CNT_INC);
        msg.set_user_id(pImUser->GetUserId());

        CImPdu pdu;
        pdu.SetPBMsg(&msg);
        pdu.SetServiceId(SID_OTHER);
        pdu.SetCommandId(CID_OTHER_USER_CNT_UPDATE);
        send_to_all_login_server(&pdu);
        
        IM::Server::IMUserStatusUpdate msg2;
        msg2.set_user_status(::IM::BaseDefine::USER_STATUS_ONLINE);
        msg2.set_user_id(pImUser->GetUserId());
        msg2.set_client_type((::IM::BaseDefine::ClientType)m_client_type);
        CImPdu pdu2;
        pdu2.SetPBMsg(&msg2);
        pdu2.SetServiceId(SID_OTHER);
        pdu2.SetCommandId(CID_OTHER_USER_STATUS_UPDATE);
        
        send_to_all_route_server(&pdu2);

    } else if (user_status == ::IM::BaseDefine::USER_STATUS_OFFLINE) {
        
        IM::Server::IMUserCntUpdate msg;
        msg.set_user_action(USER_CNT_DEC);
        msg.set_user_id(pImUser->GetUserId());
       
        CImPdu pdu;
        pdu.SetPBMsg(&msg);
        pdu.SetServiceId(SID_OTHER);
        pdu.SetCommandId(CID_OTHER_USER_CNT_UPDATE);
        send_to_all_login_server(&pdu);
        
        IM::Server::IMUserStatusUpdate msg2;
        msg2.set_user_status(::IM::BaseDefine::USER_STATUS_OFFLINE);
        msg2.set_user_id(pImUser->GetUserId());
        msg2.set_client_type((::IM::BaseDefine::ClientType)m_client_type);
        CImPdu pdu2;
        pdu2.SetPBMsg(&msg2);
        pdu2.SetServiceId(SID_OTHER);
        pdu2.SetCommandId(CID_OTHER_USER_STATUS_UPDATE);
        send_to_all_route_server(&pdu2);

        //删除aliyun上的抓拍图片
       // CAliyunOss* pAliyunOss = CAliyunOss::getInstance();
		//if(!pAliyunOss->delete_object("im/live/"+int2string(pImUser->GetUserId())+".png", INTERNAL)){
			//log("delete aliyun user pic failed");
		//}

		uint32_t nNow = (uint32_t)time(NULL);
		if(m_start_time != 0 && nNow > m_start_time+600 ){
			CDBServConn* pDBConn = get_db_serv_conn();
			if (pDBConn) {
				CImPdu pdu2;
				IM::Server::IMSaveStudyTimeReq msg2;
				CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
				msg2.set_user_id(GetUserId());
				msg2.set_start(m_start_time);
				msg2.set_duration(nNow-m_start_time);
				msg2.set_attach_data(attach.GetBuffer(), attach.GetLength());
				pdu2.SetPBMsg(&msg2);
				pdu2.SetServiceId(SID_SERVER);
				pdu2.SetCommandId(CID_SERVER_SAVE_TIME_CMD);
				pDBConn->SendPdu(&pdu2);
			}
			m_start_time = 0;
		}
    }
}

void CMsgConn::Close(bool kick_user)
{
	if(m_handle == NETLIB_INVALID_HANDLE)//wystan add for close 200624
		return;
    log("Close client, handle=%d, user_id=%s, kick_user=%d ", m_handle, GetUserId().c_str(),kick_user);
    if (m_handle != NETLIB_INVALID_HANDLE) {
        netlib_close(m_handle);
        g_msg_conn_map.erase(m_handle);
		
    }

    CImUser *pImUser = CImUserManager::GetInstance()->GetImUserById(GetUserId());
    if (pImUser) {
        pImUser->DelUnValidateMsgConn(this);
        if (pImUser->IsMsgConnEmpty()) {
            CImUserManager::GetInstance()->RemoveImUser(pImUser);
        }

        if(!kick_user)	//wystan add 200608
            SendUserStatusUpdate(::IM::BaseDefine::USER_STATUS_OFFLINE);//// wystan disable it 200609 for offline status broadcast
    }
	
    pImUser = CImUserManager::GetInstance()->GetImUserById(GetUserId());
    if (pImUser) {
        pImUser->DelMsgConn(GetHandle());
        pImUser->DelUnValidateMsgConn(this);
        if (pImUser->IsMsgConnEmpty()) {
            CImUserManager::GetInstance()->RemoveImUser(pImUser);
        }
		
		if(!kick_user)	//wystan add 200608
        	SendUserStatusUpdate(::IM::BaseDefine::USER_STATUS_OFFLINE);//// wystan disable it 200609 for offline status broadcast
    }
	m_handle = NETLIB_INVALID_HANDLE;//wystan add for kickout client 200608
    ReleaseRef();
}



void CMsgConn::OnConnect(net_handle_t handle)
{
	m_handle = handle;
	m_login_time = get_tick_count();
	g_msg_conn_map.insert(make_pair(handle, this));//保存当前msg_server所有的客户端socket连接

	netlib_option(handle, NETLIB_OPT_SET_CALLBACK, (void*)imconn_callback);
	netlib_option(handle, NETLIB_OPT_SET_CALLBACK_DATA, (void*)&g_msg_conn_map);
	netlib_option(handle, NETLIB_OPT_GET_REMOTE_IP, (void*)&m_peer_ip);//获取客户端ip地址
	netlib_option(handle, NETLIB_OPT_GET_REMOTE_PORT, (void*)&m_peer_port);//获取客户端端口
}

/* 
*   onClonse() 表示客户端主动断开的回调
*/
void CMsgConn::OnClose()
{
    log("Warning: peer closed. ");
	Close();
}

void CMsgConn::OnWriteCompelete()
{
	if(m_bKickOff){
		log(" will colse, handle=%d, user_id=%s, kick_user=%d ", m_handle, GetUserId().c_str(),m_bKickOff);
		m_bKickOff = false;
		Close(true);
	}
}

void CMsgConn::OnTimer(uint64_t curr_tick)
{
	m_msg_cnt_per_sec = 0;

    if (CHECK_CLIENT_TYPE_MOBILE(GetClientType()))
    {
        if (curr_tick > m_last_recv_tick + MOBILE_CLIENT_TIMEOUT) {
            log("mobile client timeout1, handle=%d, uid=%s ", m_handle, GetUserId().c_str());
            Close();
            return;
        }
    }
    else
    {
        if (curr_tick > m_last_recv_tick + CLIENT_TIMEOUT) {
            log("client timeout2, handle=%d, uid=%s ", m_handle, GetUserId().c_str());
            Close();
            return;
        }
    }
    

	if (!IsOpen()) {
		if (curr_tick > m_login_time + TIMEOUT_WATI_LOGIN_RESPONSE) {
			log("login timeout3, handle=%d, uid=%s ", m_handle, GetUserId().c_str());
			Close();
			return;
		}
	}

	list<msg_ack_t>::iterator it_old;
	for (list<msg_ack_t>::iterator it = m_send_msg_list.begin(); it != m_send_msg_list.end(); ) {
		msg_ack_t msg = *it;
		it_old = it;
		it++;
		if (curr_tick >= msg.timestamp + TIMEOUT_WAITING_MSG_DATA_ACK) {
			log("!!!a msg missed, msg_id=%u, %s->%s ", msg.msg_id, msg.from_id.c_str(), GetUserId().c_str());
			g_down_msg_miss_cnt++;
			m_send_msg_list.erase(it_old);
		} else {
			break;
		}
	}
}

void CMsgConn::HandlePdu(CImPdu* pPdu)
{
	// request authorization check
	if (pPdu->GetCommandId() != CID_LOGIN_REQ_USERLOGIN && !IsOpen() && IsKickOff()) {
        log("HandlePdu, wrong msg. ");
        throw CPduException(pPdu->GetServiceId(), pPdu->GetCommandId(), ERROR_CODE_WRONG_SERVICE_ID, "HandlePdu error, user not login. ");
		return;
    }
	switch (pPdu->GetCommandId()) {
        case CID_OTHER_HEARTBEAT:
            _HandleHeartBeat(pPdu);
            break;
        case CID_LOGIN_REQ_USERLOGIN:
            _HandleLoginRequest(pPdu );
            break;
        case CID_LOGIN_REQ_LOGINOUT:
            _HandleLoginOutRequest(pPdu);
            break;
        case CID_LOGIN_REQ_DEVICETOKEN:
            _HandleClientDeviceToken(pPdu);
            break;
        case CID_LOGIN_REQ_KICKPCCLIENT:
            _HandleKickPCClient(pPdu);
            break;
        case CID_LOGIN_REQ_PUSH_SHIELD:
            _HandlePushShieldRequest(pPdu);
            break;
        case CID_LOGIN_REQ_QUERY_PUSH_SHIELD:
            _HandleQueryPushShieldRequest(pPdu);
            break;
        case CID_MSG_DATA:
            _HandleClientMsgData(pPdu);
            break;
        case CID_MSG_DATA_ACK:
            _HandleClientMsgDataAck(pPdu);
            break;
        case CID_BLOG_SEND:
            _HandleClientBlog(pPdu);
            break;
        case CID_BLOG_GET_LIST_REQUEST:
            _HandleClientGetBlogListRequest(pPdu);
            break;
        case CID_BLOG_ADD_COMMENT_REQUEST:
            _HandleClientAddBlogCommentRequest(pPdu);
            break;
        case CID_BLOG_GET_COMMENT_REQUEST:
            _HandleClientGetBlogCommentRequest(pPdu);
            break;
        case CID_MSG_TIME_REQUEST:
            _HandleClientTimeRequest(pPdu);
            break;
        case CID_MSG_LIST_REQUEST:
            _HandleClientGetMsgListRequest(pPdu);
            break;
        case CID_MSG_GET_BY_MSG_ID_REQ:
            _HandleClientGetMsgByMsgIdRequest(pPdu);
            break;
        case CID_MSG_UNREAD_CNT_REQUEST:
            _HandleClientUnreadMsgCntRequest(pPdu );
            break;
        case CID_MSG_READ_ACK:
            _HandleClientMsgReadAck(pPdu);
            break;
        case CID_MSG_GET_LATEST_MSG_ID_REQ:
            _HandleClientGetLatestMsgIDReq(pPdu);
            break;
        case CID_SWITCH_P2P_CMD:
            _HandleClientP2PCmdMsg(pPdu );
            break;
        case CID_BUDDY_LIST_RECENT_CONTACT_SESSION_REQUEST:
            _HandleClientRecentContactSessionRequest(pPdu);
            break;
        case CID_BUDDY_LIST_USER_INFO_REQUEST:
            _HandleClientUserInfoRequest( pPdu );
            break;
        case CID_BUDDY_LIST_REMOVE_SESSION_REQ:
            _HandleClientRemoveSessionRequest( pPdu );
            break;
        case CID_BUDDY_LIST_ALL_USER_REQUEST:
            _HandleClientAllUserRequest(pPdu );
            break;
        case CID_BUDDY_LIST_SEARCH_USER_REQUEST:
            _HandleClientSearchUserRequest(pPdu );
            break;
        case CID_BUDDY_LIST_DEL_FRIEND_REQUEST:
        	_HandleClientDelFriendRequest(pPdu);
        	break;
        case CID_BUDDY_LIST_FOLLOW_USER_REQUEST:
        	_HandleClientFollowUserRequest(pPdu );
        	break;
        case CID_BUDDY_LIST_DEL_FOLLOW_USER_REQUEST:
            _HandleClientDelFollowUserRequest(pPdu );
            break;
        case CID_BUDDY_LIST_CHANGE_AVATAR_REQUEST:
            _HandleChangeAvatarRequest(pPdu);
            break;
        case CID_BUDDY_LIST_CHANGE_SIGN_INFO_REQUEST:
            _HandleChangeSignInfoRequest(pPdu);
            break;
            
        case CID_BUDDY_LIST_USERS_STATUS_REQUEST:
            _HandleClientUsersStatusRequest(pPdu);
            break;
        case CID_BUDDY_LIST_DEPARTMENT_REQUEST:
            _HandleClientDepartmentRequest(pPdu);
            break;
        case CID_BUDDY_LIST_UPDATE_USER_INFO_REQUEST:
        	_HandleClientUpdateUserInfoRequest(pPdu);
        	break;
        case CID_BUDDY_LIST_RECOMMEND_LIST_REQUEST:
        	_HandleClientRecommendListRequest(pPdu);
        	break;

        // for group process
        case CID_GROUP_NORMAL_LIST_REQUEST:
            s_group_chat->HandleClientGroupNormalRequest(pPdu, this);
            break;
        case CID_GROUP_INFO_REQUEST:
            s_group_chat->HandleClientGroupInfoRequest(pPdu, this);
            break;
        case CID_GROUP_CREATE_REQUEST:
            s_group_chat->HandleClientGroupCreateRequest(pPdu, this);
            break;
        case CID_GROUP_CHANGE_MEMBER_REQUEST:
            s_group_chat->HandleClientGroupChangeMemberRequest(pPdu, this);
            break;
        case CID_GROUP_SHIELD_GROUP_REQUEST:
            s_group_chat->HandleClientGroupShieldGroupRequest(pPdu, this);
            break;
        
        //cita add:获取推荐群信息
        case CID_GROUP_RECOMMAND_LIST_INFO_REQUEST:
            s_group_chat->HandleGetRecommandGroudListRequest(pPdu,this);
            break;

        case CID_FILE_REQUEST:
            s_file_handler->HandleClientFileRequest(this, pPdu);
            break;
        case CID_FILE_HAS_OFFLINE_REQ:
            s_file_handler->HandleClientFileHasOfflineReq(this, pPdu);
            break;
        case CID_FILE_ADD_OFFLINE_REQ:
            s_file_handler->HandleClientFileAddOfflineReq(this, pPdu);
            break;
        case CID_FILE_DEL_OFFLINE_REQ:
            s_file_handler->HandleClientFileDelOfflineReq(this, pPdu);
            break;

        case CID_BUDDY_LIST_ALL_ONLINE_USER_CNT_REQUEST:
        	_HandleAllOnlineUserCntRequest(pPdu);
        	break;

        case CID_SYS_MSG_SEND_DATA_REQUEST:
        	_HandleClientSendSysMsgRequest(pPdu);
        	break;
        case CID_SYS_MSG_GET_UNREAD_CNT_REQUEST:
        	_HandleClientGetUnreadSysMsgCntRequest(pPdu);
        	break;
        case CID_SYS_MSG_GET_DATA_REQUEST:
        	_HandleClientGetSysMsgDataRequest(pPdu);
        	break;
        case CID_SYS_MSG_READ_ACK:
        	_HandleClientSysMsgReadDataAck(pPdu);
        	break;
        case CID_SYS_GET_STUDY_TIME_REQUEST:
			_HandleClientGetStudyTimeRequest(pPdu);
			break;
        case CID_FRIEND_SHIP_SEARCH_FRIEND_REQ://cita add:搜索好友
            _HandleClientSearchUserRequest(pPdu);
            break;
        //cita add:获取所有在线用户列表
        case CID_BUDDY_LIST_GET_ALL_ONLINE_USER_INFO_REQUEST:
            _HandleClientGetAllOnlineUserRequest(pPdu);
            break;
        default:
            log("wrong msg, cmd id=%d, user id=%s. ", pPdu->GetCommandId(), GetUserId().c_str());
            break;
	}
}

void CMsgConn::_HandleHeartBeat(CImPdu *pPdu)
{
    //响应
    SendPdu(pPdu);
}

// process: send validate request to db server
void CMsgConn::_HandleLoginRequest(CImPdu* pPdu)
{
    //一个客户端已经登录过了，就不会再处理登录消息了
    if (m_user_id.length() != 0) {
        log("duplicate LoginRequest in the same conn ");
        return;
    }

    // check if all server connection are OK
    uint32_t result = 0;
    string result_string = "";
    CDBServConn* pDbConn = get_db_serv_conn_for_login();
    if (!pDbConn) {
        result = IM::BaseDefine::REFUSE_REASON_NO_DB_SERVER;
        result_string = "服务端异常-no db server";
	}
    else if (!is_login_server_available()) {
        result = IM::BaseDefine::REFUSE_REASON_NO_LOGIN_SERVER;
        result_string = "服务端异常-no login server";
	}
    else if (!is_route_server_available()) {
        result = IM::BaseDefine::REFUSE_REASON_NO_ROUTE_SERVER;
        result_string = "服务端异常-no route server";
    }
    if (result) {
        IM::Login::IMLoginRes msg;
        msg.set_server_time(time(NULL));
        msg.set_result_code((IM::BaseDefine::ResultType)result);
        msg.set_result_string(result_string);
        CImPdu pdu;
        pdu.SetPBMsg(&msg);
        pdu.SetServiceId(SID_LOGIN);
        pdu.SetCommandId(CID_LOGIN_RES_USERLOGIN);
        pdu.SetSeqNum(pPdu->GetSeqNum());
        SendPdu(&pdu);
        Close();
        return;
    }
    

    IM::Login::IMLoginReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    m_user_id = msg.user_id();
    uint32_t online_status = msg.online_status();
    if (online_status < IM::BaseDefine::USER_STATUS_ONLINE || online_status > IM::BaseDefine::USER_STATUS_LEAVE) {
        log("HandleLoginReq, online status wrong: %u ", online_status);
        online_status = IM::BaseDefine::USER_STATUS_ONLINE;
    }

    m_client_version = msg.client_version();
    /*client type主要指以下几种
    	CLIENT_TYPE_WINDOWS     = 0x01;
        CLIENT_TYPE_MAC         = 0x02;
        CLIENT_TYPE_IOS         = 0x11;
        CLIENT_TYPE_ANDROID     = 0x12;
    */
    m_client_type = msg.client_type();
    m_online_status = online_status;
    log("HandleLoginReq, user_id=%s, status=%u, client_type=%u, client=%s, ",m_user_id.c_str(), online_status, m_client_type, m_client_version.c_str());

    CImUser* pImUser = CImUserManager::GetInstance()->GetImUserById(GetUserId());
    if (!pImUser) {
        pImUser = new CImUser(GetUserId());
    	CImUserManager::GetInstance()->AddImUserById(GetUserId(), pImUser);
    }
    pImUser->AddUnValidateMsgConn(this);
  
    CDbAttachData attach_data(ATTACH_TYPE_HANDLE, m_handle, 0);
    // continue to validate if the user is OK
    
    IM::Server::IMValidateReq msg2;
    msg2.set_user_name(GetUserId());
    msg2.set_password(msg.referral_code());
    msg2.set_attach_data(attach_data.GetBuffer(), attach_data.GetLength());
    CImPdu pdu;
    pdu.SetPBMsg(&msg2);
    pdu.SetServiceId(SID_OTHER);
    pdu.SetCommandId(CID_OTHER_VALIDATE_REQ);
    pdu.SetSeqNum(pPdu->GetSeqNum());
    pDbConn->SendPdu(&pdu);
}

void CMsgConn::_HandleLoginOutRequest(CImPdu *pPdu)
{
    log("HandleLoginOutRequest, user_id=%s, client_type=%u. ", GetUserId().c_str(), GetClientType());
    CDBServConn* pDBConn = get_db_serv_conn();
	if (pDBConn) {
        IM::Login::IMDeviceTokenReq msg;
        msg.set_user_id(GetUserId());
        msg.set_device_token("");
        CImPdu pdu;
        pdu.SetPBMsg(&msg);
        pdu.SetServiceId(SID_LOGIN);
        pdu.SetCommandId(CID_LOGIN_REQ_DEVICETOKEN);
        pdu.SetSeqNum(pPdu->GetSeqNum());
		pDBConn->SendPdu(&pdu);
	}
    
    
    IM::Login::IMLogoutRsp msg2;
    msg2.set_result_code(0);
    CImPdu pdu2;
    pdu2.SetPBMsg(&msg2);
    pdu2.SetServiceId(SID_LOGIN);
    pdu2.SetCommandId(CID_LOGIN_RES_LOGINOUT);
    pdu2.SetSeqNum(pPdu->GetSeqNum());
    SendPdu(&pdu2);
    Close();//note:理论上，用户下线即可关闭socket连接，避免资源浪费
}

void CMsgConn::_HandleKickPCClient(CImPdu *pPdu)
{
    IM::Login::IMKickPCClientReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    string user_id = GetUserId();
    if (!CHECK_CLIENT_TYPE_MOBILE(GetClientType()))
    {
        log("HandleKickPCClient, user_id = %s, cmd must come from mobile client. ", user_id.c_str());
        return;
    }
    log("HandleKickPCClient, user_id = %s. ", user_id.c_str());
    
    CImUser* pImUser = CImUserManager::GetInstance()->GetImUserById(user_id);
    if (pImUser)
    {
        pImUser->KickOutSameClientType(CLIENT_TYPE_MAC, IM::BaseDefine::KICK_REASON_MOBILE_KICK,this);
    }
    
    CRouteServConn* pRouteConn = get_route_serv_conn();
    if (pRouteConn) {
        IM::Server::IMServerKickUser msg2;
        msg2.set_user_id(user_id);
        msg2.set_client_type(::IM::BaseDefine::CLIENT_TYPE_MAC);
        msg2.set_reason(IM::BaseDefine::KICK_REASON_MOBILE_KICK);
        CImPdu pdu;
        pdu.SetPBMsg(&msg2);
        pdu.SetServiceId(SID_OTHER);
        pdu.SetCommandId(CID_OTHER_SERVER_KICK_USER);
        pRouteConn->SendPdu(&pdu);
    }
    
    IM::Login::IMKickPCClientRsp msg2;
    msg2.set_user_id(user_id);
    msg2.set_result_code(0);
    CImPdu pdu;
    pdu.SetPBMsg(&msg2);
    pdu.SetServiceId(SID_LOGIN);
    pdu.SetCommandId(CID_LOGIN_RES_KICKPCCLIENT);
    pdu.SetSeqNum(pPdu->GetSeqNum());
    SendPdu(&pdu);
}

void CMsgConn::_HandleClientRecentContactSessionRequest(CImPdu *pPdu)
{
    CDBServConn* pConn = get_db_serv_conn_for_login();
    if (!pConn) {
        return;
    }
    
    IM::Buddy::IMRecentContactSessionReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    log("HandleClientRecentContactSessionRequest, user_id=%s, latest_update_time=%u. ", GetUserId().c_str(), msg.latest_update_time());

    msg.set_user_id(GetUserId());
    // 请求最近联系会话列表
    CDbAttachData attach_data(ATTACH_TYPE_HANDLE, m_handle, 0);
    msg.set_attach_data(attach_data.GetBuffer(), attach_data.GetLength());
    pPdu->SetPBMsg(&msg);
    pConn->SendPdu(pPdu);
}

void CMsgConn::_HandleClientMsgData(CImPdu* pPdu)
{
    IM::Message::IMMsgData msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
	if (msg.msg_data().length() == 0) {
		log("discard an empty message, uid=%s ", GetUserId().c_str());
		return;
	}

	if (m_msg_cnt_per_sec >= MAX_MSG_CNT_PER_SECOND) {
		log("!!!too much msg cnt in one second, uid=%s ", GetUserId().c_str());
		return;
	}
    
    if (msg.from_user_id() == msg.to_session_id() && CHECK_MSG_TYPE_SINGLE(msg.msg_type()))
    {
        log("!!!from_user_id == to_user_id. ");
        return;
    }

	m_msg_cnt_per_sec++;

	string to_session_id = msg.to_session_id();
    uint8_t msg_id = msg.msg_id();
	uint8_t msg_type = msg.msg_type();
    string msg_data = msg.msg_data();
    string msg_sig = HexStr(msg.msg_sig());

	if (g_log_msg_toggle) {
		log("HandleClientMsgData, %s->%s, msg_type=%u, msg_id=%u. ", GetUserId().c_str(), to_session_id.c_str(), msg_type, msg_id);
	}
    log("%s->%s data:%s sig:%s",GetUserId().c_str(), to_session_id.c_str(),msg.msg_data().c_str(),msg_sig.c_str());
    //msg.set_msg_sig(msg_sig);

	//uint32_t cur_time = time(NULL);
    CDbAttachData attach_data(ATTACH_TYPE_HANDLE, m_handle, 0);
    msg.set_from_user_id(GetUserId());
    //msg.set_create_time(cur_time);//cita delete:用client的create time
    msg.set_attach_data(attach_data.GetBuffer(), attach_data.GetLength());
    pPdu->SetPBMsg(&msg);
	// send to DB storage server
	CDBServConn* pDbConn = get_db_serv_conn();
	if (pDbConn) {
		pDbConn->SendPdu(pPdu);
	}
}

void CMsgConn::_HandleClientBlog(CImPdu* pPdu)
{
    IM::Blog::IMBlogSend blog;
    CHECK_PB_PARSE_MSG(blog.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
	if (blog.blog_data().length() == 0) {
		log("discard an empty message, uid=%s ", GetUserId().c_str());
		return;
	}

	if (m_msg_cnt_per_sec >= MAX_MSG_CNT_PER_SECOND) {
		log("!!!too much msg cnt in one second, uid=%u ", GetUserId().c_str());
		return;
	}

	log("send blog %s", GetUserId().c_str());

	m_msg_cnt_per_sec++;

    CDbAttachData attach_data(ATTACH_TYPE_HANDLE, m_handle, 0);
    blog.set_user_id(GetUserId());
    blog.set_attach_data(attach_data.GetBuffer(), attach_data.GetLength());
    pPdu->SetPBMsg(&blog);
	// send to DB storage server
	CDBServConn* pDbConn = get_db_serv_conn();
	if (pDbConn) {
		//log("sid %u, cid %u ", pPdu->GetServiceId(), pPdu->GetCommandId());
		pDbConn->SendPdu(pPdu);
	}
}

void CMsgConn::_HandleClientMsgDataAck(CImPdu* pPdu)
{
    //客户端收到消息的确认
    IM::Message::IMMsgDataAck msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    IM::BaseDefine::SessionType session_type = msg.session_type();
    if (session_type == IM::BaseDefine::SESSION_TYPE_SINGLE)
    {
        uint32_t msg_id = msg.msg_id();
        string session_id = msg.session_id();
        DelFromSendList(msg_id, session_id);
    }
}

void CMsgConn::_HandleClientTimeRequest(CImPdu* pPdu)
{
    IM::Message::IMClientTimeRsp msg;
    msg.set_server_time((uint32_t)time(NULL));
    CImPdu pdu;
    pdu.SetPBMsg(&msg);
    pdu.SetServiceId(SID_MSG);
    pdu.SetCommandId(CID_MSG_TIME_RESPONSE);
    pdu.SetSeqNum(pPdu->GetSeqNum());
	SendPdu(&pdu);
}

void CMsgConn::_HandleClientGetMsgListRequest(CImPdu *pPdu)
{
    IM::Message::IMGetMsgListReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    string session_id = msg.session_id();
    uint32_t msg_id_begin = msg.msg_id_begin();
    uint32_t msg_cnt = msg.msg_cnt();
    uint32_t session_type = msg.session_type();
    log("HandleClientGetMsgListRequest, req_id=%s, session_type=%u, session_id=%s, msg_id_begin=%u, msg_cnt=%u. ",
        GetUserId().c_str(), session_type, session_id.c_str(), msg_id_begin, msg_cnt);
    CDBServConn* pDBConn = get_db_serv_conn_for_login();
    if (pDBConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pDBConn->SendPdu(pPdu);
    }
}

void CMsgConn::_HandleClientGetBlogListRequest(CImPdu *pPdu)
{
    IM::Blog::IMBlogGetListReq blog;
    CHECK_PB_PARSE_MSG(blog.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    log("get blog list");
    CDBServConn* pDBConn = get_db_serv_conn_for_login();
    if (pDBConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        blog.set_user_id(GetUserId());
        blog.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&blog);
        pDBConn->SendPdu(pPdu);
    }
}

void CMsgConn::_HandleClientAddBlogCommentRequest(CImPdu *pPdu)
{
    IM::Blog::IMBlogAddCommentReq blog;
    CHECK_PB_PARSE_MSG(blog.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    CDBServConn* pDBConn = get_db_serv_conn_for_login();
    if (pDBConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        blog.set_user_id(GetUserId());
        blog.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&blog);
        pDBConn->SendPdu(pPdu);
    }
}

void CMsgConn::_HandleClientGetBlogCommentRequest(CImPdu *pPdu)
{
    IM::Blog::IMBlogGetCommentReq blog;
    CHECK_PB_PARSE_MSG(blog.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    CDBServConn* pDBConn = get_db_serv_conn_for_login();
    if (pDBConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        blog.set_user_id(GetUserId());
        blog.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&blog);
        pDBConn->SendPdu(pPdu);
    }
}

void CMsgConn::_HandleClientGetStudyTimeRequest(CImPdu *pPdu)
{
	IM::System::IMSysGetStudyTimeReq msg;
	CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

	CDBServConn* pDBConn = get_db_serv_conn_for_login();
	if (pDBConn) {
		CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
		msg.set_user_id(GetUserId());
		msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
		pPdu->SetPBMsg(&msg);
		pDBConn->SendPdu(pPdu);
	}

}

void CMsgConn::_HandleClientGetMsgByMsgIdRequest(CImPdu *pPdu)
{
    IM::Message::IMGetMsgByIdReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    string session_id = msg.session_id();
    uint32_t session_type = msg.session_type();
    uint32_t msg_cnt = msg.msg_id_list_size();
    log("_HandleClientGetMsgByMsgIdRequest, req_id=%s, session_type=%u, session_id=%s, msg_cnt=%u.",
        GetUserId().c_str(), session_type, session_id.c_str(), msg_cnt);
    CDBServConn* pDBConn = get_db_serv_conn_for_login();
    if (pDBConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pDBConn->SendPdu(pPdu);
    }
}

void CMsgConn::_HandleClientUnreadMsgCntRequest(CImPdu* pPdu)
{
	log("HandleClientUnreadMsgCntReq, from_id=%s ", GetUserId().c_str());
    IM::Message::IMUnreadMsgCntReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    
	CDBServConn* pDBConn = get_db_serv_conn_for_login();
	if (pDBConn) {
		CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pDBConn->SendPdu(pPdu);
	}
}

void CMsgConn::_HandleClientMsgReadAck(CImPdu* pPdu)
{
    IM::Message::IMMsgDataReadAck msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    uint32_t session_type = msg.session_type();
    string session_id = msg.session_id();
    uint32_t msg_id = msg.msg_id();
    log("HandleClientMsgReadAck, user_id=%s, session_id=%s, msg_id=%u, session_type=%u. ", GetUserId().c_str(),session_id.c_str(), msg_id, session_type);
    
	CDBServConn* pDBConn = get_db_serv_conn();
	if (pDBConn) {
        msg.set_user_id(GetUserId());
        pPdu->SetPBMsg(&msg);
		pDBConn->SendPdu(pPdu);
	}
    IM::Message::IMMsgDataReadNotify msg2;
    msg2.set_user_id(GetUserId());
    msg2.set_session_id(session_id);
    msg2.set_msg_id(msg_id);
    msg2.set_session_type((IM::BaseDefine::SessionType)session_type);
    CImPdu pdu;
    pdu.SetPBMsg(&msg2);
    pdu.SetServiceId(SID_MSG);
    pdu.SetCommandId(CID_MSG_READ_NOTIFY);
    CImUser* pUser = CImUserManager::GetInstance()->GetImUserById(GetUserId());
    if (pUser)
    {
        pUser->BroadcastPdu(&pdu, this);
    }
	else{//wystan modify for invalid route send 200612
	    CRouteServConn* pRouteConn = get_route_serv_conn();
	    if (pRouteConn) {
	        pRouteConn->SendPdu(&pdu);
	    }
	}
	
    if (session_type == IM::BaseDefine::SESSION_TYPE_SINGLE)
    {
        DelFromSendList(msg_id, session_id);
    }
}

void CMsgConn::_HandleClientGetLatestMsgIDReq(CImPdu *pPdu)
{
    IM::Message::IMGetLatestMsgIdReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    uint32_t session_type = msg.session_type();
    string session_id = msg.session_id();
    log("HandleClientGetMsgListRequest, user_id=%s, session_id=%s, session_type=%u. ", GetUserId().c_str(),session_id.c_str(), session_type);
    
    CDBServConn* pDBConn = get_db_serv_conn();
    if (pDBConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pDBConn->SendPdu(pPdu);
    }
}


void CMsgConn::_HandleClientP2PCmdMsg(CImPdu* pPdu)
{
    IM::SwitchService::IMP2PCmdMsg msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
	string cmd_msg = msg.cmd_msg_data();
	string from_user_id = msg.from_user_id();
	string to_user_id = msg.to_user_id();

	log("HandleClientP2PCmdMsg, %s->%s, cmd_msg: %s ", from_user_id.c_str(), to_user_id.c_str(), cmd_msg.c_str());

    CImUser* pFromImUser = CImUserManager::GetInstance()->GetImUserById(GetUserId());
	CImUser* pToImUser = CImUserManager::GetInstance()->GetImUserById(to_user_id);
    
	if (pFromImUser) {
		pFromImUser->BroadcastPdu(pPdu, this);
	}
    
	if (pToImUser) {
		pToImUser->BroadcastPdu(pPdu, NULL);
	}
    else{//wystan modify for invalid route send 200612
		CRouteServConn* pRouteConn = get_route_serv_conn();
		if (pRouteConn) {
			pRouteConn->SendPdu(pPdu);
		}
   	}
}

void CMsgConn::_HandleClientUserInfoRequest(CImPdu* pPdu)
{
    IM::Buddy::IMUsersInfoReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    uint32_t user_cnt = msg.user_id_list_size();
	log("HandleClientUserInfoReq, req_id=%s, user_cnt=%u, Handle=%u .", GetUserId().c_str(), user_cnt,GetHandle());
	CDBServConn* pDBConn = get_db_serv_conn_for_login();
	if (pDBConn) {
		CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
		pDBConn->SendPdu(pPdu);
	}
}

void CMsgConn::_HandleClientRemoveSessionRequest(CImPdu* pPdu)
{
    IM::Buddy::IMRemoveSessionReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    uint32_t session_type = msg.session_type();
    string session_id = msg.session_id();
    log("HandleClientRemoveSessionReq, user_id=%s, session_id=%s, type=%u ", GetUserId().c_str(), session_id.c_str(), session_type);
    
    CDBServConn* pConn = get_db_serv_conn();
    if (pConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pConn->SendPdu(pPdu);
    }
    
    if (session_type == IM::BaseDefine::SESSION_TYPE_SINGLE)
    {
        IM::Buddy::IMRemoveSessionNotify msg2;
        msg2.set_user_id(GetUserId());
        msg2.set_session_id(session_id);
        msg2.set_session_type((IM::BaseDefine::SessionType)session_type);
        CImPdu pdu;
        pdu.SetPBMsg(&msg2);
        pdu.SetServiceId(SID_BUDDY_LIST);
        pdu.SetCommandId(CID_BUDDY_LIST_REMOVE_SESSION_NOTIFY);
        CImUser* pImUser = CImUserManager::GetInstance()->GetImUserById(GetUserId());
        if (pImUser) {
            pImUser->BroadcastPdu(&pdu, this);
        }
        CRouteServConn* pRouteConn = get_route_serv_conn();
        if (pRouteConn) {
            pRouteConn->SendPdu(&pdu);
        }
    }
}

void CMsgConn::_HandleClientAllUserRequest(CImPdu* pPdu)
{
    IM::Buddy::IMAllUserReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    uint32_t latest_update_time = msg.latest_update_time();
    log("HandleClientAllUserReq, user_id=%s, latest_update_time=%u. ", GetUserId().c_str(), latest_update_time);

    CDBServConn* pConn = get_db_serv_conn();
    if (pConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pConn->SendPdu(pPdu);
    }
}

void CMsgConn::_HandleClientSearchUserRequest(CImPdu* pPdu)
{
    IM::Buddy::IMSearchUserReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    string search_user_name = msg.search_user_name();
    log("user_id=%s search %s", GetUserId().c_str(), search_user_name.c_str());

    CDBServConn* pConn = get_db_serv_conn();
    if (pConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pConn->SendPdu(pPdu);
    }
    
    //cita add:获取所有在线用户信息
    //cita modify:此处无法获取用户详细信息，因此还要转发到db_proxy_server中获取
    /*
    list<CImUser *> allUsers;
    CImUserManager::GetInstance()->GetAllUser(allUsers);
    IM::Buddy::IMSearchUserRsp msgRes;
    for(auto iter = allUsers.begin();iter!=allUsers.end();++iter)
    {
        if((*iter)->GetLoginName() != m_user_id)
        {
            IM::BaseDefine::UserInfo * userInfo = msgRes.add_search_user_list();
            userInfo->set_user_id((*iter)->GetUserId());
            userInfo->set_user_addr((*iter)->GetUserAddress());
            userInfo->set_user_pubkey((*iter)->GetUserId());
            log("address = %s ,pubkey =%s",userInfo->user_addr().c_str(),userInfo->user_pubkey().c_str());
        }
    }
    CImPdu pdu;
    msgRes.set_user_id(m_user_id);
    pdu.SetPBMsg(&msgRes);
    pdu.SetServiceId(SID_BUDDY_LIST);
    pdu.SetCommandId(CID_BUDDY_LIST_SEARCH_USER_RESPONSE);
    pdu.SetSeqNum(pPdu->GetSeqNum());
    SendPdu(&pdu);
    */

}

void CMsgConn::_HandleClientSendSysMsgRequest(CImPdu* pPdu)
{
    IM::System::IMSendSysMsgReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    IM::System::IMSysMsgData sysMsg = msg.sys_msg();
    string to_id = sysMsg.to_id();
    log("user_id=%s send sys msg to %s", GetUserId().c_str(), to_id.c_str());

    //自己加自己为好友的限制
    if(sysMsg.type() == IM::System::ADD_FRIEND_REQUEST && to_id == GetUserId()){
    	CMsgConn* pMsgConn = CImUserManager::GetInstance()->GetMsgConnByHandle(GetUserId(), m_handle);

    	IM::System::IMSendSysMsgRsp msgResp;
    	msgResp.set_result_code(1);
		msgResp.set_user_id(GetUserId());
		IM::System::IMSysMsgData *pMsgData = msgResp.mutable_sys_msg();
		pMsgData->set_from_id(0);
		pMsgData->set_to_id(0);
		pMsgData->set_type(IM::System::ADD_FRIEND_REQUEST);

		msgResp.set_attach_data(msg.attach_data());
		CImPdu pdu;
		pdu.SetPBMsg(&msgResp);
		pdu.SetSeqNum(pPdu->GetSeqNum());
		pdu.SetServiceId(IM::BaseDefine::SID_SYS_MSG);
		pdu.SetCommandId(IM::BaseDefine::CID_SYS_MSG_SEND_DATA_RESPONSE);

		pMsgConn->SendPdu(&pdu);

    	return;
    }

    CDBServConn* pConn = get_db_serv_conn();
    if (pConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pConn->SendPdu(pPdu);
    }

}

void CMsgConn::_HandleClientSysMsgReadDataAck(CImPdu* pPdu)
{
    IM::System::IMSysMsgReadDataAck msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    log("user_id=%s", GetUserId().c_str());

    CDBServConn* pConn = get_db_serv_conn();
    if (pConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pConn->SendPdu(pPdu);
    }

}

void CMsgConn::_HandleClientGetSysMsgDataRequest(CImPdu* pPdu)
{
    IM::System::IMGetSysMsgDataReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    uint32_t cnt = msg.msg_cnt();
    log("user_id=%s cnt %u", GetUserId().c_str(), cnt);

    CDBServConn* pConn = get_db_serv_conn();
    if (pConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pConn->SendPdu(pPdu);
    }

}

void CMsgConn::_HandleClientGetUnreadSysMsgCntRequest(CImPdu* pPdu)
{
    IM::System::IMSysMsgUnreadCntReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    log("user_id=%s", GetUserId().c_str());

    CDBServConn* pConn = get_db_serv_conn();
    if (pConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pConn->SendPdu(pPdu);
    }

}

void CMsgConn::_HandleClientDelFriendRequest(CImPdu* pPdu)
{
    IM::Buddy::IMDelFriendReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    string friend_id = msg.friend_id();
    log("user_id=%s del friend %s", GetUserId().c_str(), friend_id.c_str());

    CDBServConn* pConn = get_db_serv_conn();
    if (pConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pConn->SendPdu(pPdu);
    }

}

void CMsgConn::_HandleClientFollowUserRequest(CImPdu* pPdu)
{
    IM::Buddy::IMFollowUserReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    string friend_id = msg.friend_id();
    log("user_id=%s follow user %s", GetUserId().c_str(), friend_id.c_str());

    CDBServConn* pConn = get_db_serv_conn();
    if (pConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pConn->SendPdu(pPdu);
    }
}

void CMsgConn::_HandleClientDelFollowUserRequest(CImPdu* pPdu)
{
    IM::Buddy::IMDelFollowUserReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    string friend_id = msg.friend_id();
    log("user_id=%s follow user %s", GetUserId().c_str(), friend_id.c_str());

    CDBServConn* pConn = get_db_serv_conn();
    if (pConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pConn->SendPdu(pPdu);
    }
}

void CMsgConn::_HandleChangeAvatarRequest(CImPdu* pPdu)
{
    IM::Buddy::IMChangeAvatarReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    log("HandleChangeAvatarRequest, user_id=%s ", GetUserId().c_str());
    CDBServConn* pDBConn = get_db_serv_conn();
    if (pDBConn) {
        msg.set_user_id(GetUserId());
        pPdu->SetPBMsg(&msg);
        pDBConn->SendPdu(pPdu);
    }
}

void CMsgConn::_HandleClientUsersStatusRequest(CImPdu* pPdu)
{
    IM::Buddy::IMUsersStatReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
	uint32_t user_count = msg.user_id_list_size();
	log("HandleClientUsersStatusReq, user_id=%s, query_count=%u.", GetUserId().c_str(), user_count);
    
    CRouteServConn* pRouteConn = get_route_serv_conn();
    if(pRouteConn)
    {
        msg.set_user_id(GetUserId());
        CPduAttachData attach(ATTACH_TYPE_HANDLE, m_handle,0, NULL);
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pRouteConn->SendPdu(pPdu);
    }
}

void CMsgConn::_HandleClientDepartmentRequest(CImPdu *pPdu)
{
    IM::Buddy::IMDepartmentReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    log("HandleClientDepartmentRequest, user_id=%s, latest_update_time=%u.", GetUserId().c_str(), msg.latest_update_time());
    CDBServConn* pDBConn = get_db_serv_conn();
    if (pDBConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        pPdu->SetPBMsg(&msg);
        pDBConn->SendPdu(pPdu);
    }
}

void CMsgConn::_HandleClientUpdateUserInfoRequest(CImPdu *pPdu)
{
	IM::Buddy::IMUpdateUsersInfoReq msg;
	CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
	log("user_id=%s", GetUserId().c_str());
	CDBServConn* pDBConn = get_db_serv_conn();
	if (pDBConn) {
		CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
		msg.set_user_id(GetUserId());
		msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
		pPdu->SetPBMsg(&msg);
		pDBConn->SendPdu(pPdu);
	}

}

void CMsgConn::_HandleClientRecommendListRequest(CImPdu *pPdu)
{
	IM::Buddy::IMRecommendListReq msg;
	IM::Buddy::IMRecommendListRsp msgResp;
	CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

	CImUserManager::GetInstance()->GetOnlineRecommendList(msg.page(), msg.page_size(), msgResp);

	log("get recommend list user_id=%s recommend %u", GetUserId().c_str(),
			msgResp.recommend_list_size());

	CImPdu pdu;
	pdu.SetPBMsg(&msgResp);
	pdu.SetServiceId(IM::BaseDefine::SID_BUDDY_LIST);
	pdu.SetCommandId(IM::BaseDefine::CID_BUDDY_LIST_RECOMMEND_LIST_RESPONSE);
	pdu.SetSeqNum(pPdu->GetSeqNum());
	SendPdu(&pdu);
}

void CMsgConn::_HandleClientDeviceToken(CImPdu *pPdu)
{
    if (!CHECK_CLIENT_TYPE_MOBILE(GetClientType()))
    {
        log("HandleClientDeviceToken, user_id=%s, not mobile client.", GetUserId().c_str());
        return;
    }
    IM::Login::IMDeviceTokenReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    string device_token = msg.device_token();
    log("HandleClientDeviceToken, user_id=%s, device_token=%s ", GetUserId().c_str(), device_token.c_str());
    
    IM::Login::IMDeviceTokenRsp msg2;
    msg.set_user_id(GetUserId());
    msg.set_client_type((::IM::BaseDefine::ClientType)GetClientType());
    CImPdu pdu;
    pdu.SetPBMsg(&msg2);
    pdu.SetServiceId(SID_LOGIN);
    pdu.SetCommandId(CID_LOGIN_RES_DEVICETOKEN);
    pdu.SetSeqNum(pPdu->GetSeqNum());
    SendPdu(&pdu);
    
    CDBServConn* pDBConn = get_db_serv_conn();
	if (pDBConn) {
        msg.set_user_id(GetUserId());
        pPdu->SetPBMsg(&msg);
		pDBConn->SendPdu(pPdu);
	}
}

void CMsgConn::AddToSendList(uint32_t msg_id, string from_id)
{
	log("AddToSendList, msg_id=%u, from_id=%s ", msg_id, from_id.c_str());
	msg_ack_t msg;
	msg.msg_id = msg_id;
	msg.from_id = from_id;
	msg.timestamp = get_tick_count();
	m_send_msg_list.push_back(msg);

	g_down_msg_total_cnt++;
}

void CMsgConn::DelFromSendList(uint32_t msg_id, string from_id)
{
	log("DelFromSendList, msg_id=%u, from_id=%s ", msg_id, from_id.c_str());
	for (list<msg_ack_t>::iterator it = m_send_msg_list.begin(); it != m_send_msg_list.end(); it++) {
		msg_ack_t msg = *it;
		if ( (msg.msg_id == msg_id) && (msg.from_id == from_id) ) {
			m_send_msg_list.erase(it);
			break;
		}
	}
}

uint32_t CMsgConn::GetClientTypeFlag()
{
    uint32_t client_type_flag = 0x00;
    if (CHECK_CLIENT_TYPE_PC(GetClientType()))
    {
        client_type_flag = CLIENT_TYPE_FLAG_PC;
    }
    else if (CHECK_CLIENT_TYPE_MOBILE(GetClientType()))
    {
        client_type_flag = CLIENT_TYPE_FLAG_MOBILE;
    }
    return client_type_flag;
}

void CMsgConn::_HandleChangeSignInfoRequest(CImPdu* pPdu)
{
	IM::Buddy::IMChangeSignInfoReq msg;
	CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
	log("HandleChangeSignInfoRequest, user_id=%s ", GetUserId().c_str());
	CDBServConn* pDBConn = get_db_serv_conn();
	if (pDBConn) {
			msg.set_user_id(GetUserId());
			CPduAttachData attach(ATTACH_TYPE_HANDLE, m_handle,0, NULL);
			msg.set_attach_data(attach.GetBuffer(), attach.GetLength());

			pPdu->SetPBMsg(&msg);
			pDBConn->SendPdu(pPdu);
		}
}

void CMsgConn::_HandlePushShieldRequest(CImPdu* pPdu)
{
    IM::Login::IMPushShieldReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    log("_HandlePushShieldRequest, user_id=%s, shield_status %d", GetUserId().c_str(), msg.shield_status());
    CDBServConn* pDBConn = get_db_serv_conn();
    if (pDBConn) {
        msg.set_user_id(GetUserId());
        CPduAttachData attach(ATTACH_TYPE_HANDLE, m_handle,0, NULL);
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        
        pPdu->SetPBMsg(&msg);
        pDBConn->SendPdu(pPdu);
    }
}

void CMsgConn::_HandleQueryPushShieldRequest(CImPdu* pPdu)
{
    IM::Login::IMQueryPushShieldReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
    log("HandleChangeSignInfoRequest, user_id=%s ", GetUserId().c_str());
    CDBServConn* pDBConn = get_db_serv_conn();
    if (pDBConn) {
        msg.set_user_id(GetUserId());
        CPduAttachData attach(ATTACH_TYPE_HANDLE, m_handle,0, NULL);
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        
        pPdu->SetPBMsg(&msg);
        pDBConn->SendPdu(pPdu);
    }
}

void CMsgConn::_HandleAllOnlineUserCntRequest(CImPdu* pPdu)
{
    IM::Buddy::IMALLOnlineUserCntReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));

    uint32_t nNow = (uint32_t)time(NULL);
   	if(msg.study_state() == IM::Buddy::start && m_start_time == 0){
   		m_start_time = nNow;
   		log("%s study start start=%u study_state=%u", GetUserId().c_str(), m_start_time, msg.study_state());

   	}else if(msg.study_state() == IM::Buddy::end && m_start_time != 0 && nNow > m_start_time+600 ){
   		CDBServConn* pDBConn = get_db_serv_conn();
   		if (pDBConn) {
   			CImPdu pdu2;
   			IM::Server::IMSaveStudyTimeReq msg2;
   			CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
   			msg2.set_user_id(GetUserId());
   			msg2.set_start(m_start_time);
   			msg2.set_duration(nNow-m_start_time);
   			msg2.set_attach_data(attach.GetBuffer(), attach.GetLength());
   			pdu2.SetPBMsg(&msg2);
   			pdu2.SetServiceId(SID_SERVER);
   			pdu2.SetCommandId(CID_SERVER_SAVE_TIME_CMD);
   			pDBConn->SendPdu(&pdu2);
   		}
   		m_start_time = 0;
    }

   	log("user_id=%s start=%u study_state=%u", GetUserId().c_str(), m_start_time, msg.study_state());

    CLoginServConn* pConn = get_login_serv_conn();
    if (pConn) {
        CDbAttachData attach(ATTACH_TYPE_HANDLE, m_handle, 0);
        msg.set_user_id(GetUserId());
        msg.set_attach_data(attach.GetBuffer(), attach.GetLength());
        CImPdu pdu;
        pdu.SetPBMsg(&msg);
        pdu.SetServiceId(SID_BUDDY_LIST);
        pdu.SetCommandId(CID_BUDDY_LIST_ALL_ONLINE_USER_CNT_REQUEST);
        pdu.SetSeqNum(pPdu->GetSeqNum());
        pConn->SendPdu(&pdu);
    }
}

//cita add:客户端请求获取所有在线用户
void CMsgConn::_HandleClientGetAllOnlineUserRequest(CImPdu *pPdu)
{
    IM::Buddy::IMGetALLOnlineUserReq msg;
    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(),pPdu->GetBodyLength()));

    IM::Server::IMGetOnlineUserInfoReq msgReq; 
    msgReq.set_user_id(m_user_id);
    list<CImUser *> allUsers;
    CImUserManager::GetInstance()->GetAllUser(allUsers);
    for(auto iter = allUsers.begin();iter!=allUsers.end();++iter)
    {
        if((*iter)->GetUserId() != m_user_id)
        {
            msgReq.add_user_id_list((*iter)->GetUserId());
        }
    }

    CDBServConn *pConn = get_db_serv_conn();
    if(pConn)
    {
        CDbAttachData attach(ATTACH_TYPE_HANDLE,m_handle,0);
        msgReq.set_attach_data(attach.GetBuffer(),attach.GetLength());
        CImPdu pdu;
        pdu.SetPBMsg(&msgReq);
        pdu.SetSeqNum(pPdu->GetSeqNum());
		pdu.SetServiceId(IM::BaseDefine::SID_SERVER);
		pdu.SetCommandId(IM::BaseDefine::ServerCmdID::CID_ONLINEUSER_INFO);
        pConn->SendPdu(&pdu);
    }

}
