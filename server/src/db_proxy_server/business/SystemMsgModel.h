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

#ifndef SYSTEM_MSG_MODEL_H_
#define SYSTEM_MSG_MODEL_H_

#include <list>
#include <string>

#include "util.h"
#include "ImPduBase.h"
#include "AudioModel.h"
#include "IM.BaseDefine.pb.h"
#include "IM.System.pb.h"
using namespace std;

class CSystemMsgModel {
public:
	virtual ~CSystemMsgModel();
	static CSystemMsgModel* getInstance();

    bool sendSystemMsg(string nFromId, string nToId, IM::System::SysMsgOper oper_type,
    		uint32_t nMsgId, string& strMsgContent, uint32_t nStatus);
    //void getSystemMsg(uint32_t nUserId, uint32_t nMsgId, uint32_t nMsgCnt,
    	//	list<IM::BaseDefine::MsgInfo>& lsMsg);
    bool getUnreadSysMsgCount(string nUserId, IM::System::IMSysMsgUnreadCntRsp &msgResp);
    //void getUnReadCntAll(uint32_t nUserId, uint32_t &nTotalCnt);
    void clearSysMsgCounter(string nUserId, IM::System::SysMsgType type);

    void getSysMsgData(string user_id, IM::System::SysMsgType type, uint32_t msg_cnt,
    		IM::System::IMGetSysMsgDataRsp& msgResp);

    void getStudyTime(list<string> lsIds, IM::System::StatisticsType stype,
    		string sparam, uint32_t page, uint32_t page_size,
			IM::System::IMSysGetStudyTimeRsp& resp);

private:
    CSystemMsgModel();
    void incMsgCount(string nToId, IM::System::SysMsgType type);
private:
	static CSystemMsgModel*	m_pInstance;
};



#endif
