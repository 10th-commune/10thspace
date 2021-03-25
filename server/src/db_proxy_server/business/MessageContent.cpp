/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：MessageContent.cpp
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#include "../ProxyConn.h"
#include "../CachePool.h"
#include "../DBPool.h"
#include "MessageContent.h"
#include "MessageModel.h"
#include "GroupMessageModel.h"
#include "Common.h"
#include "GroupModel.h"
#include "ImPduBase.h"
#include "IM.Message.pb.h"
#include "SessionModel.h"
#include "RelationModel.h"
#include "Base64.h"

namespace DB_PROXY {

    void getMessage(CImPdu* pPdu, uint32_t conn_uuid)
    {
        IM::Message::IMGetMsgListReq msg;
  if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
        {
            string nUserId = msg.user_id();
            string nPeerId = msg.session_id();
            uint32_t nMsgId = msg.msg_id_begin();
            uint32_t nMsgCnt = msg.msg_cnt();
            IM::BaseDefine::SessionType nSessionType = msg.session_type();
            if(IM::BaseDefine::SessionType_IsValid(nSessionType))
            {
                CImPdu* pPduResp = new CImPdu;
                IM::Message::IMGetMsgListRsp msgResp;

                list<IM::BaseDefine::MsgInfo> lsMsg;

                if(nSessionType == IM::BaseDefine::SESSION_TYPE_SINGLE)//获取个人消息
                {
                    CMessageModel::getInstance()->getMessage(nUserId, nPeerId, nMsgId, nMsgCnt, lsMsg);
                }
                else if(nSessionType == IM::BaseDefine::SESSION_TYPE_GROUP)//获取群消息
                {
                    if(CGroupModel::getInstance()->isInGroup(nUserId, nPeerId))
                    {
                        CGroupMessageModel::getInstance()->getMessage(nUserId, nPeerId, nMsgId, nMsgCnt, lsMsg);
                    }
                }
                else if(nSessionType == IM::BaseDefine::SESSION_TYPE_SINGLE)//获取个人消息
                {
                    //CMessageModel::getInstance()->getMessage(nUserId, nPeerId, nMsgId, nMsgCnt, lsMsg);
                }

                msgResp.set_user_id(nUserId);
                msgResp.set_session_id(nPeerId);
                msgResp.set_msg_id_begin(nMsgId);
                msgResp.set_session_type(nSessionType);
                for(auto it=lsMsg.begin(); it!=lsMsg.end();++it)
                {
                    IM::BaseDefine::MsgInfo* pMsg = msgResp.add_msg_list();
        //            *pMsg = *it;
                    pMsg->set_msg_id(it->msg_id());
                    pMsg->set_from_session_id(it->from_session_id());
                    pMsg->set_create_time(it->create_time());
                    pMsg->set_msg_type(it->msg_type());
                    pMsg->set_msg_data(it->msg_data());
//                    log("userId=%u, peerId=%u, msgId=%u", nUserId, nPeerId, it->msg_id());
                }

                log("userId=%s, peerId=%s, msgId=%u, msgCnt=%u, count=%u", nUserId.c_str(), nPeerId.c_str(), nMsgId, nMsgCnt, msgResp.msg_list_size());
                msgResp.set_attach_data(msg.attach_data());
                pPduResp->SetPBMsg(&msgResp);
                pPduResp->SetSeqNum(pPdu->GetSeqNum());
                pPduResp->SetServiceId(IM::BaseDefine::SID_MSG);
                pPduResp->SetCommandId(IM::BaseDefine::CID_MSG_LIST_RESPONSE);
                CProxyConn::AddResponsePdu(conn_uuid, pPduResp);
            }
            else
            {
                log("invalid sessionType. userId=%s, peerId=%s, msgId=%u, msgCnt=%u, sessionType=%u",
                    nUserId.c_str(), nPeerId.c_str(), nMsgId, nMsgCnt, nSessionType);
            }
        }
        else
        {
            log("parse pb failed");
        }
    }

    void sendMessage(CImPdu* pPdu, uint32_t conn_uuid)
    {
        IM::Message::IMMsgData msg;
        if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
        {
            string nFromId = msg.from_user_id();
            string nToId = msg.to_session_id();
            uint32_t nCreateTime = msg.create_time();
            IM::BaseDefine::MsgType nMsgType = msg.msg_type();
            uint32_t nMsgLen = msg.msg_data().length();
            uint32_t nNow = (uint32_t)time(NULL);
            if (IM::BaseDefine::MsgType_IsValid(nMsgType))
            {
                if(nMsgLen != 0)
                {
                    CImPdu* pPduResp = new CImPdu;
                    uint32_t nMsgId = INVALID_VALUE;
                    string nSessionId;
                    string nPeerSessionId;
                    CMessageModel* pMsgModel = CMessageModel::getInstance();
                    CGroupMessageModel* pGroupMsgModel = CGroupMessageModel::getInstance();
                    if(nMsgType == IM::BaseDefine::MSG_TYPE_GROUP_TEXT) {
                        CGroupModel* pGroupModel = CGroupModel::getInstance();
                        if (pGroupModel->isValidateGroupId(nToId) && pGroupModel->isInGroup(nFromId, nToId))
                        {
                            nSessionId = CSessionModel::getInstance()->getSessionId(nFromId, nToId, IM::BaseDefine::SESSION_TYPE_GROUP, false);
                            if ("" == nSessionId) {
                                nSessionId = CSessionModel::getInstance()->addSession(nFromId, nToId, IM::BaseDefine::SESSION_TYPE_GROUP);
                            }
                            if(nSessionId != "")
                            {
                                nMsgId = pGroupMsgModel->getMsgId(nToId);
                                if (nMsgId != INVALID_VALUE) {
                                    pGroupMsgModel->sendMessage(nFromId, nToId, nMsgType, nCreateTime, nMsgId, (string&)msg.msg_data(),(string &)msg.msg_sig());
                                    CSessionModel::getInstance()->updateSession(nSessionId, nNow);
                                }
                            }
                        }
                        else
                        {
                            log("invalid groupId. fromId=%s, groupId=%s", nFromId.c_str(), nToId.c_str());
                            delete pPduResp;
                            return;
                        }
                    } else if (nMsgType == IM::BaseDefine::MSG_TYPE_GROUP_AUDIO) {
                        CGroupModel* pGroupModel = CGroupModel::getInstance();
                        if (pGroupModel->isValidateGroupId(nToId)&& pGroupModel->isInGroup(nFromId, nToId))
                        {
                            nSessionId = CSessionModel::getInstance()->getSessionId(nFromId, nToId, IM::BaseDefine::SESSION_TYPE_GROUP, false);
                            if ("" == nSessionId) {
                                nSessionId = CSessionModel::getInstance()->addSession(nFromId, nToId, IM::BaseDefine::SESSION_TYPE_GROUP);
                            }
                            if(nSessionId != "")
                            {
                                nMsgId = pGroupMsgModel->getMsgId(nToId);
                                if(nMsgId != INVALID_VALUE)
                                {
                                    pGroupMsgModel->sendAudioMessage(nFromId, nToId, nMsgType, nCreateTime, nMsgId, msg.msg_data().c_str(), nMsgLen);
                                    CSessionModel::getInstance()->updateSession(nSessionId, nNow);
                                }
                            }
                        }
                        else
                        {
                            log("invalid groupId. fromId=%s, groupId=%s", nFromId.c_str(), nToId.c_str());
                            delete pPduResp;
                            return;
                        }
                    } else if(nMsgType== IM::BaseDefine::MSG_TYPE_SINGLE_TEXT) {                                                
                        if (nFromId != nToId) {
                            CRelationModel::getInstance()->addRelation(nFromId,nToId,RELATION_ACTION_ADD_FRIEND);//cita add:添加聊天关系
                            nSessionId = CSessionModel::getInstance()->getSessionId(nFromId, nToId, IM::BaseDefine::SESSION_TYPE_SINGLE, false);
                            if ("" == nSessionId) {
                                nSessionId = CSessionModel::getInstance()->addSession(nFromId, nToId, IM::BaseDefine::SESSION_TYPE_SINGLE);
                            }
                            nPeerSessionId = CSessionModel::getInstance()->getSessionId(nToId, nFromId, IM::BaseDefine::SESSION_TYPE_SINGLE, false);
                            if("" ==  nPeerSessionId)
                            {
                            	//这里好像是写错了吧，不然说不过去啊? neil
                                //nSessionId = CSessionModel::getInstance()->addSession(nToId, nFromId, IM::BaseDefine::SESSION_TYPE_SINGLE);
                            	nPeerSessionId = CSessionModel::getInstance()->addSession(nToId, nFromId, IM::BaseDefine::SESSION_TYPE_SINGLE);
                            }
                            uint32_t nRelateId = CRelationModel::getInstance()->getRelationId(nFromId, nToId, true);
                            if(nSessionId != "" && nRelateId != INVALID_VALUE)//cita 临时聊天无需关系
                            {
                                nMsgId = pMsgModel->getMsgId(nRelateId);
                                if(nMsgId != INVALID_VALUE)
                                {
                                    pMsgModel->sendMessage(nRelateId, nFromId, nToId, nMsgType, nCreateTime, nMsgId, (string&)msg.msg_data(),(string &)msg.msg_sig());
                                    CSessionModel::getInstance()->updateSession(nSessionId, nNow);
                                    CSessionModel::getInstance()->updateSession(nPeerSessionId, nNow);
                                }
                                else
                                {
                                    log("msgId is invalid. fromId=%s, toId=%s, nRelateId=%u, nSessionId=%s, nMsgType=%u", nFromId.c_str(), nToId.c_str(), nRelateId, nSessionId.c_str(), nMsgType);
                                }
                            }
                            else{
                            	msg.set_msg_type(IM::BaseDefine::MSG_TYPE_ERROR_NOT_FRIEND);
                                log("sessionId or relateId is invalid. fromId=%s, toId=%s, nRelateId=%u, nSessionId=%s, nMsgType=%u", nFromId.c_str(), nToId.c_str(), nRelateId, nSessionId.c_str(), nMsgType);
                            }
                        }
                        else
                        {
                            log("send msg to self. fromId=%s, toId=%s, msgType=%u", nFromId.c_str(), nToId.c_str(), nMsgType);
                        }
                        
                    } else if(nMsgType == IM::BaseDefine::MSG_TYPE_SINGLE_AUDIO) {
                        
                        if(nFromId != nToId)
                        {
                            nSessionId = CSessionModel::getInstance()->getSessionId(nFromId, nToId, IM::BaseDefine::SESSION_TYPE_SINGLE, false);
                            if ("" == nSessionId) {
                                nSessionId = CSessionModel::getInstance()->addSession(nFromId, nToId, IM::BaseDefine::SESSION_TYPE_SINGLE);
                            }
                            nPeerSessionId = CSessionModel::getInstance()->getSessionId(nToId, nFromId, IM::BaseDefine::SESSION_TYPE_SINGLE, false);
                            if("" ==  nPeerSessionId)
                            {
                            	nPeerSessionId = CSessionModel::getInstance()->addSession(nToId, nFromId, IM::BaseDefine::SESSION_TYPE_SINGLE);
                            }
                            uint32_t nRelateId = CRelationModel::getInstance()->getRelationId(nFromId, nToId, true);
                            if(nSessionId != "" && nRelateId != INVALID_VALUE)
                            {
                                nMsgId = pMsgModel->getMsgId(nRelateId);
                                if(nMsgId != INVALID_VALUE) {
                                    pMsgModel->sendAudioMessage(nRelateId, nFromId, nToId, nMsgType, nCreateTime, nMsgId, msg.msg_data().c_str(), nMsgLen);
                                    CSessionModel::getInstance()->updateSession(nSessionId, nNow);
                                    CSessionModel::getInstance()->updateSession(nPeerSessionId, nNow);
                                }
                                else {
                                    log("msgId is invalid. fromId=%s, toId=%s, nRelateId=%u, nSessionId=%s, nMsgType=%u", nFromId.c_str(), nToId.c_str(), nRelateId, nSessionId.c_str(), nMsgType);
                                }
                            }
                            else {
                            	msg.set_msg_type(IM::BaseDefine::MSG_TYPE_ERROR_NOT_FRIEND);
                                log("sessionId or relateId is invalid. fromId=%s, toId=%s, nRelateId=%u, nSessionId=%s, nMsgType=%u", nFromId.c_str(), nToId.c_str(), nRelateId, nSessionId.c_str(), nMsgType);
                            }
                        }
                        else
                        {
                            log("send msg to self. fromId=%s, toId=%s, msgType=%u", nFromId.c_str(), nToId.c_str(), nMsgType);
                        }
                    }

                    log("fromId=%s, toId=%s, type=%u, msgId=%u, sessionId=%s", nFromId.c_str(), nToId.c_str(), nMsgType, nMsgId, nSessionId.c_str());

                    msg.set_msg_id(nMsgId);
                    pPduResp->SetPBMsg(&msg);
                    pPduResp->SetSeqNum(pPdu->GetSeqNum());
                    pPduResp->SetServiceId(IM::BaseDefine::SID_MSG);
                    pPduResp->SetCommandId(IM::BaseDefine::CID_MSG_DATA);
                    CProxyConn::AddResponsePdu(conn_uuid, pPduResp);
                }
                else
                {
                    log("msgLen error. fromId=%s, toId=%s, msgType=%u", nFromId.c_str(), nToId.c_str(), nMsgType);
                }
            }
            else
            {
                log("invalid msgType.fromId=%s, toId=%s, msgType=%u", nFromId.c_str(), nToId.c_str(), nMsgType);
            }
        }
        else
        {
            log("parse pb failed");
        }
    }

    void getMessageById(CImPdu* pPdu, uint32_t conn_uuid)
    {
        IM::Message::IMGetMsgByIdReq msg;
        if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
        {
            string nUserId = msg.user_id();
            IM::BaseDefine::SessionType nType = msg.session_type();
            string nPeerId = msg.session_id();
            list<uint32_t> lsMsgId;
            uint32_t nCnt = msg.msg_id_list_size();
            for(uint32_t i=0; i<nCnt; ++i)
            {
                lsMsgId.push_back(msg.msg_id_list(i));
            }
            if (IM::BaseDefine::SessionType_IsValid(nType))
            {
                CImPdu* pPduResp = new CImPdu;
                IM::Message::IMGetMsgByIdRsp msgResp;

                list<IM::BaseDefine::MsgInfo> lsMsg;
                if(IM::BaseDefine::SESSION_TYPE_SINGLE == nType)
                {
                    CMessageModel::getInstance()->getMsgByMsgId(nUserId, nPeerId, lsMsgId, lsMsg);
                }
                else if(IM::BaseDefine::SESSION_TYPE_GROUP)
                {
                    CGroupMessageModel::getInstance()->getMsgByMsgId(nUserId, nPeerId, lsMsgId, lsMsg);
                }
                msgResp.set_user_id(nUserId);
                msgResp.set_session_id(nPeerId);
                msgResp.set_session_type(nType);
                for(auto it=lsMsg.begin(); it!=lsMsg.end(); ++it)
                {
                    IM::BaseDefine::MsgInfo* pMsg = msgResp.add_msg_list();
                    pMsg->set_msg_id(it->msg_id());
                    pMsg->set_from_session_id(it->from_session_id());
                    pMsg->set_create_time(it->create_time());
                    pMsg->set_msg_type(it->msg_type());
                    pMsg->set_msg_data(it->msg_data());
                }
                log("userId=%s, peerId=%s, sessionType=%u, reqMsgCnt=%u, resMsgCnt=%u", nUserId.c_str(), nPeerId.c_str(), nType, msg.msg_id_list_size(), msgResp.msg_list_size());
                msgResp.set_attach_data(msg.attach_data());
                pPduResp->SetPBMsg(&msgResp);
                pPduResp->SetSeqNum(pPdu->GetSeqNum());
                pPduResp->SetServiceId(IM::BaseDefine::SID_MSG);
                pPduResp->SetCommandId(IM::BaseDefine::CID_MSG_GET_BY_MSG_ID_RES);
                CProxyConn::AddResponsePdu(conn_uuid, pPduResp);
            }
            else
            {
                log("invalid sessionType. fromId=%s, toId=%s, sessionType=%u, msgCnt=%u", nUserId.c_str(), nPeerId.c_str(), nType, nCnt);
            }
        }
        else
        {
            log("parse pb failed");
        }
    }

    void getLatestMsgId(CImPdu* pPdu, uint32_t conn_uuid)
    {
        IM::Message::IMGetLatestMsgIdReq msg;
        if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
        {
            string nUserId = msg.user_id();
            IM::BaseDefine::SessionType nType = msg.session_type();
            string nPeerId = msg.session_id();
            if (IM::BaseDefine::SessionType_IsValid(nType)) {
                CImPdu* pPduResp = new CImPdu;
                IM::Message::IMGetLatestMsgIdRsp msgResp;
                msgResp.set_user_id(nUserId);
                msgResp.set_session_type(nType);
                msgResp.set_session_id(nPeerId);
                uint32_t nMsgId = INVALID_VALUE;
                if(IM::BaseDefine::SESSION_TYPE_SINGLE == nType)
                {
                    string strMsg;
                    IM::BaseDefine::MsgType nMsgType;
                    CMessageModel::getInstance()->getLastMsg(nUserId, nPeerId, nMsgId, strMsg, nMsgType, 1);
                }
                else
                {
                    string strMsg;
                    IM::BaseDefine::MsgType nMsgType;
                    string nFromId;
                    CGroupMessageModel::getInstance()->getLastMsg(nPeerId, nMsgId, strMsg, nMsgType, nFromId);
                }
                msgResp.set_latest_msg_id(nMsgId);
                log("userId=%s, peerId=%s, sessionType=%u, msgId=%u", nUserId.c_str(), nPeerId.c_str(), nType,nMsgId);
                msgResp.set_attach_data(msg.attach_data());
                pPduResp->SetPBMsg(&msgResp);
                pPduResp->SetSeqNum(pPdu->GetSeqNum());
                pPduResp->SetServiceId(IM::BaseDefine::SID_MSG);
                pPduResp->SetCommandId(IM::BaseDefine::CID_MSG_GET_LATEST_MSG_ID_RSP);
                CProxyConn::AddResponsePdu(conn_uuid, pPduResp);

            }
            else
            {
                log("invalid sessionType. userId=%s, peerId=%s, sessionType=%u", nUserId.c_str(), nPeerId.c_str(), nType);
            }
        }
        else
        {
            log("parse pb failed");
        }
    }
};
