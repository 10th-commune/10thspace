/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：MessageModel.h
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#ifndef SYSTEM_MSG_H_
#define SYSTEM_MSG_H_

#include "ImPduBase.h"
#include "IM.System.pb.h"

namespace DB_PROXY {

	void sendSysMsg(CImPdu* pPdu, uint32_t conn_uuid);
	void getUnreadSysMsgCnt(CImPdu* pPdu, uint32_t conn_uuid);
	void getUnreadSysMsgData(CImPdu* pPdu, uint32_t conn_uuid);
	void clearSysMsgCounter(CImPdu* pPdu, uint32_t conn_uuid);

	void addFriend(IM::System::IMSendSysMsgReq &msg, IM::System::IMSendSysMsgRsp &msgResp);
	void agreeAddFriend(IM::System::IMSendSysMsgReq &msg, IM::System::IMSendSysMsgRsp &msgResp);
	void addGroup(IM::System::IMSendSysMsgReq &msg, IM::System::IMSendSysMsgRsp &msgResp);
	void agreeAddGroup(IM::System::IMSendSysMsgReq &msg, IM::System::IMSendSysMsgRsp &msgResp);

	void getStudyTime(CImPdu* pPdu, uint32_t conn_uuid);
};




#endif
