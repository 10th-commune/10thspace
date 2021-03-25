/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：GroupMessageModel.h
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#ifndef GROUP_MESSAGE_MODEL_H_
#define GROUP_MESSAGE_MODEL_H_

#include <list>
#include <string>

#include "util.h"
#include "ImPduBase.h"
#include "AudioModel.h"
#include "GroupModel.h"
#include "IM.BaseDefine.pb.h"

using namespace std;


class CGroupMessageModel {
public:
	virtual ~CGroupMessageModel();
	static CGroupMessageModel* getInstance();
    
    bool sendMessage(string nFromId, string nGroupId, IM::BaseDefine::MsgType nMsgType, uint32_t nCreateTime, uint32_t nMsgId, const string& strMsgContent,const string & strSigData);
    bool sendAudioMessage(string nFromId, string nGroupId, IM::BaseDefine::MsgType nMsgType, uint32_t nCreateTime, uint32_t nMsgId,const char* pMsgContent, uint32_t nMsgLen);
    void getMessage(string nUserId, string nGroupId, uint32_t nMsgId, uint32_t nMsgCnt,
                    list<IM::BaseDefine::MsgInfo>& lsMsg);
    bool clearMessageCount(string nUserId, string nGroupId);
    uint32_t getMsgId(string nGroupId);
    void getUnreadMsgCount(string nUserId, uint32_t &nTotalCnt, list<IM::BaseDefine::UnreadInfo>& lsUnreadCount);
    void getLastMsg(string nGroupId, uint32_t& nMsgId, string& strMsgData, IM::BaseDefine::MsgType & nMsgType, string& nFromId);
    void getUnReadCntAll(string nUserId, uint32_t &nTotalCnt);
    void getMsgByMsgId(string nUserId, string nGroupId, const list<uint32_t>& lsMsgId, list<IM::BaseDefine::MsgInfo>& lsMsg);
    bool resetMsgId(string nGroupId);
private:
    CGroupMessageModel();
    bool incMessageCount(string nUserId, string nGroupId);

private:
	static CGroupMessageModel*	m_pInstance;
};



#endif /* MESSAGE_MODEL_H_ */
