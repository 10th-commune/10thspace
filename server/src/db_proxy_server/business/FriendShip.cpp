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
#include "FriendShip.h"
#include "../DBPool.h"
#include "EncDec.h"
#include "../ProxyConn.h"
#include "IM.Friendship.pb.h"
#include "IM.Server.pb.h"
#include "UserModel.h"
#include <list>

int FriendShipStrategy::addFriendWithNickName(string myid,string friendid,const std::string &nickname,IM::BaseDefine::UserInfo &info)
{
    int bRet = 3;

    CDBManager *pDBManager = CDBManager::getInstance();
 	CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
    if (pDBConn) 
    {
        string strUSerid = (myid);
        string strFriendid = (friendid);
        string strNickName = nickname;
        string strSql = "select * from IMUser where (id='"+ strFriendid + "' or name='"+strNickName+"')";

        CResultSet *pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
        log("sql %s",strSql.c_str());
        if(pResultSet)
        {
            string friend_id;
            
                /* code */
            while(pResultSet->Next())
            {
                friend_id = pResultSet->GetString("id");
                info.set_user_id(pResultSet->GetString("id"));
                info.set_user_gender(pResultSet->GetInt("sex"));
                info.set_user_nick_name(pResultSet->GetString("nick"));
                info.set_user_domain(pResultSet->GetString("domain"));
                info.set_user_real_name(pResultSet->GetString("name"));
                info.set_user_tel(pResultSet->GetString("phone"));
                info.set_email(pResultSet->GetString("email"));
                info.set_avatar_url(pResultSet->GetString("avatar"));
		        info.set_sign_info(pResultSet->GetString("sign_info"));
                info.set_department_id(pResultSet->GetInt("departId"));
                info.set_status(pResultSet->GetInt("status"));
                break;
            }
            delete pResultSet;
            pResultSet = nullptr;

            if(friend_id != "")
            {
                string strFriendID = (friend_id);
                strSql = "select count(*) from IMFriendship where (myid='"+strUSerid+"' and friendid='"+strFriendID+"') or (myid='"+strFriendID+"' and friendid='"+strUSerid+"')";
                        log("sql %s",strSql.c_str());

                pResultSet = pDBConn->ExecuteQuery(strSql.c_str());//crash !
                if(pResultSet)
                {
                    bool set = false;
                    int count = 0;
                    while(pResultSet->Next())
                    {
                        count = pResultSet->GetInt("count(*)");
                        set = true;
                        break;
                    }
                    delete pResultSet;
                    pResultSet = nullptr;  

                    if(count == 0)
                    {
                        uint32_t cur_time = time(NULL);
                        strSql = "insert into IMFriendship (myid,friendid,status,createtime,updatetime) values ('"+strUSerid+"','"+strFriendID+"',1,"+std::to_string(cur_time)+","+std::to_string(cur_time)+")";
                                log("sql %s",strSql.c_str());
                        bool ret = pDBConn->ExecuteUpdate(strSql.c_str());
                        if(ret)
                            bRet = 0;
                    }
                    else if(count > 0)
                    {
                        strSql = "update IMFriendship set status=1 where (myid='"+strUSerid+"' and friendid='"+strFriendID+"') or (myid='"+strFriendID+"' and friendid='"+strUSerid+"')";
                        bool ret = pDBConn->ExecuteUpdate(strSql.c_str());
                        log("ret = %d sql %s",ret,strSql.c_str());

                        bRet = 0;
                    }
                
                }
                
            }
            
        }
        pDBManager->RelDBConn(pDBConn);
    }
    return bRet;
}

bool FriendShipStrategy::getUserInfos(const std::vector<string> & ids,std::vector<IM::BaseDefine::UserInfo> &infos)
{
    bool bRet = false;
    CDBManager *pDBManager = CDBManager::getInstance();

 	CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
    if (pDBConn) 
	{
        string strSQL = "select * from IMUser id in (";
        int n = ids.size();
        for(int i=0;i<ids.size();i++)
        {
            string strMyId = (ids[i]);
            strSQL += strMyId;
            if(i != n-1)
                strSQL += ",";
        }
        strSQL += ")";
        log("get user info log = %s",strSQL.c_str());
        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSQL.c_str());
        if(pResultSet)
        {
            while(pResultSet->Next())
            {
                
            }
            delete pResultSet;
        }
        pDBManager->RelDBConn(pDBConn);
    }

    return bRet;
}


bool FriendShipStrategy::addFriend(string myid,string friendid,uint32_t status)
{
    bool bRet = false;
	CDBManager *pDBManager = CDBManager::getInstance();

 	CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
    if (pDBConn) 
	{
		string strMyId = (myid);
		string strFriendId = (friendid);
        string strStatus = std::to_string(status);
        //string strSql = "INSERT INTO IMFriendship ("+strMyId+","+strFriendId+")"+" select "+strMyId+","+"friendId "+"from dual where not exists (select 1 from IMFriendship where myid="+strMyId+" AND friendid="+strFriendId+")";
        string strSql = "insert into IMFriendship (myid,friendid,status) values ("+strMyId+","+strFriendId+","+strStatus+")";
        log("add friend sql = %s",strSql.c_str());

        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
        if(pResultSet)
        {
            bRet = true;
            delete pResultSet;
        }
		
		pDBManager->RelDBConn(pDBConn);
	}
    log("add friend result = %d",bRet);
    return bRet;
}

bool FriendShipStrategy::getFriendList(string myid,std::vector<string> & friendList,int status)
{
    bool bRet = false;
    CDBManager *pDBManager = CDBManager::getInstance();
 	CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
    if (pDBConn) 
	{
		string strMyId = (myid);

        string strSql = "select myid,friendid,status from IMFriendship where status=0 and (myid="+strMyId+" or friendid="+strMyId+")";
        log("get friend list sql = %s",strSql.c_str());
        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
        string friend_id = myid;//
        if(pResultSet)
        {
            while(pResultSet->Next())
            {
                if(pResultSet->GetInt("status") == status)
                {
                    if(pResultSet->GetString("friendid") != myid)
                        friendList.push_back(pResultSet->GetString("friendid"));
                    if(pResultSet->GetString("myid") != myid)
                        friendList.push_back(pResultSet->GetString("myid"));
                }
            }

            bRet = true;
            delete pResultSet;
        }
		
		pDBManager->RelDBConn(pDBConn);
	}
    log("get friend list result = %d",bRet);

    return bRet;
}

bool FriendShipStrategy::updateFriendRelation(string myid, string friendid, uint32_t update_flag)
{
    bool bRet = false;
    do
    {
        CDBManager *pDBManager = CDBManager::getInstance();
 	    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
        if (!pDBConn) 
            break;
        std::string strUser_id = (myid);
        std::string strFriend_id = (friendid);
        std::string strStatus = "2";
        if(update_flag == 1)
        {
            strStatus = "0";// agree add friend
        } 
        else if(update_flag == 2) 
            strStatus = "2";//refuse add friend
        else if(update_flag == 3)
            strStatus = "3";//delete friend
        
        uint32_t cur_time = time(NULL);
        std::string str_express = "(myid="+strUser_id+" and friendid="+strFriend_id+")" + " or " +"(friendid="+strUser_id+" and myid="+strFriend_id+")";
        std::string strSql = "update IMFriendship set status="+strStatus+", updatetime="+std::to_string(cur_time)+" where "+str_express;
        log("SQL = %s",strSql.c_str());
        bool result = pDBConn->ExecuteUpdate(strSql.c_str());
        if(result)
            bRet = true;
        pDBManager->RelDBConn(pDBConn);
        /* code */
    } while (0);
    return bRet;
}

bool FriendShipStrategy::getUnAgreeFriendList(string user_id,std::vector<string> &friends)
{
    bool bRet = false;
    do
    {
        CDBManager *pDBManager = CDBManager::getInstance();
 	    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
        if (!pDBConn) 
            break;
        std::string strUser_id = (user_id);
        std::string strSql = "select myid from IMFriendship where (friendid="+strUser_id+" and status=1)";
        CResultSet *pSet = pDBConn->ExecuteQuery(strSql.c_str());
        if(pSet)
        {
            while (pSet->Next())
            {
                /* code */
                friends.push_back(pSet->GetString("myid"));
            }
            delete pSet;
            bRet = true;
        }
        pDBManager->RelDBConn(pDBConn);
    }while(0);
    return bRet;
}

bool FriendShipStrategy::searchFriend(const std::string & user_nick,const std::string & user_name,std::vector<IM::BaseDefine::UserInfo> &infos)
{
    bool bRet = false;
    do
    {
        /* code */
        CDBManager *pDBManager = CDBManager::getInstance();
 	    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
        if (!pDBConn) 
            break;
        std::string sql = "select * from IMUser where (nick='"+user_nick+"' or name='"+user_name+"')";
        log("sql = %s",sql.c_str());
        CResultSet *pResultSet = pDBConn->ExecuteQuery(sql.c_str());
        if(pResultSet)
        {
            while(pResultSet->Next())
            {
                IM::BaseDefine::UserInfo cUser;
                cUser.set_user_id(pResultSet->GetString("id"));
                cUser.set_user_gender(pResultSet->GetInt("sex"));
                cUser.set_user_nick_name(pResultSet->GetString("nick"));
                cUser.set_user_domain(pResultSet->GetString("domain"));
                cUser.set_user_real_name(pResultSet->GetString("name"));
                cUser.set_user_tel(pResultSet->GetString("phone"));
                cUser.set_email(pResultSet->GetString("email"));
                cUser.set_avatar_url(pResultSet->GetString("avatar"));
		        cUser.set_sign_info(pResultSet->GetString("sign_info"));
                cUser.set_department_id(pResultSet->GetInt("departId"));
                cUser.set_status(pResultSet->GetInt("status"));
                infos.push_back(cUser);
                log("Result set user id = %s",cUser.user_id().c_str());
            }

            delete pResultSet;
        }
        else
        {
            log("no result!");
        }
        
        pDBManager->RelDBConn(pDBConn);

    } while (0);
    log("search list size = %d",infos.size());
    return bRet;
}



namespace DB_PROXY {

void doAddFriend(CImPdu* pPdu, uint32_t conn_uuid)
{
    log("do Add Friend ship");
	CImPdu* pPduResp = new CImPdu;
    
    IM::Friend::IMFriendShipAddReq msg;
    IM::Friend::IMFriendShipAddRsp msgResp;
    if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
    {
        string myid = msg.myid();
		string friend_id = msg.friend_id();
        std::string nick_name = msg.friend_nickname();
        FriendShipStrategy strategy;
        IM::BaseDefine::UserInfo friendInfo;
        int ret = strategy.addFriendWithNickName(myid,friend_id,nick_name,friendInfo);
        msgResp.set_user_id(myid);
        msgResp.set_result_code(ret);
        if(ret == 0)
        {
            IM::BaseDefine::UserInfo *pUser = msgResp.mutable_friend_info();
            pUser->set_user_id(friendInfo.user_id());
            pUser->set_user_gender(friendInfo.user_gender());
            pUser->set_user_nick_name(friendInfo.user_nick_name());
            pUser->set_avatar_url(friendInfo.avatar_url());
            pUser->set_sign_info(friendInfo.sign_info());
            pUser->set_department_id(friendInfo.department_id());
            pUser->set_email(friendInfo.email());
            pUser->set_user_real_name(friendInfo.user_real_name());
            pUser->set_user_tel(friendInfo.user_tel());
            pUser->set_user_domain(friendInfo.user_domain());
            pUser->set_status(friendInfo.status());
            msgResp.set_result_string("Add success!");


            IM::Friend::IMFriendShipAddNotify notify;
            notify.set_from_user_id(myid);
            notify.set_to_user_id(friendInfo.user_id());
            notify.set_notify_value(1);
            std::list<IM::BaseDefine::UserInfo> lsUser;
            std::list<string> idList;
            idList.push_back(myid);
            CUserModel::getInstance()->getUsers(idList, lsUser);
            if(idList.size() > 0)
            {
                list<IM::BaseDefine::UserInfo>::iterator it=lsUser.begin();
                IM::BaseDefine::UserInfo* pUser = notify.mutable_friend_info();
                pUser->set_user_id(it->user_id());
                pUser->set_user_gender(it->user_gender());
                pUser->set_user_nick_name(it->user_nick_name());
                pUser->set_avatar_url(it->avatar_url());

                pUser->set_sign_info(it->sign_info());
                pUser->set_department_id(it->department_id());
                pUser->set_email(it->email());
                pUser->set_user_real_name(it->user_real_name());
                pUser->set_user_tel(it->user_tel());
                pUser->set_user_domain(it->user_domain());
                pUser->set_status(it->status());

                notify.set_attach_msg(msg.attach_msg());
                CImPdu * nPdu = new CImPdu();
                nPdu->SetPBMsg(&notify);
                nPdu->SetServiceId(IM::BaseDefine::SID_FRIENDSHIP);
                nPdu->SetCommandId(IM::BaseDefine::CID_FRIEND_SHIP_ADD_NOTIFY);
                CProxyConn::AddResponsePdu(conn_uuid,nPdu);
            }

        }
        msgResp.set_attach_data(msg.attach_data());
        if(ret == 0)
            log("do Add Friend ship ad success! friend ");

    }
    else
    {
        msgResp.set_result_code(IM::BaseDefine::REFUSE_REASON_MSG_SERVER_FULL);
        msgResp.set_result_string("服务端内部错误");
        log("do Add Friend ship ad failed!");

    }
    
    pPduResp->SetPBMsg(&msgResp);
    pPduResp->SetSeqNum(pPdu->GetSeqNum());
    pPduResp->SetServiceId(IM::BaseDefine::SID_FRIENDSHIP);
    pPduResp->SetCommandId(IM::BaseDefine::CID_FRIEND_SHIP_ADD_RES);
    CProxyConn::AddResponsePdu(conn_uuid, pPduResp);
}

void doGetFriendList(CImPdu* pPdu, uint32_t conn_uuid)
{
    IM::Friend::IMFriendShipListReq msg;
    IM::Friend::IMFriendShipListRsp resMsg;
    if(msg.ParseFromArray(pPdu->GetBodyData(),pPdu->GetBodyLength()))
    {
        string user_id = msg.myid();
        resMsg.set_user_id(user_id);
        FriendShipStrategy strategy;
        std::vector<string> friend_list;
        strategy.getFriendList(user_id,friend_list);
        std::list<string> idList;
        for(int i=0;i<friend_list.size();i++)
            idList.push_back(friend_list[i]);

        std::list<IM::BaseDefine::UserInfo> lsUser;
        CUserModel::getInstance()->getUsers(idList, lsUser);
        for(list<IM::BaseDefine::UserInfo>::iterator it=lsUser.begin();
                it!=lsUser.end(); ++it)
            {
                IM::BaseDefine::UserInfo* pUser = resMsg.add_user_list();
                pUser->set_user_id(it->user_id());
                pUser->set_user_gender(it->user_gender());
                pUser->set_user_nick_name(it->user_nick_name());
                pUser->set_avatar_url(it->avatar_url());

                pUser->set_sign_info(it->sign_info());
                pUser->set_department_id(it->department_id());
                pUser->set_email(it->email());
                pUser->set_user_real_name(it->user_real_name());
                pUser->set_user_tel(it->user_tel());
                pUser->set_user_domain(it->user_domain());
                pUser->set_status(it->status());
            }
            
            resMsg.set_attach_data(msg.attach_data());
    }
    CImPdu *pdu = new CImPdu();
    pdu->SetPBMsg(&resMsg);
    pdu->SetSeqNum(pPdu->GetSeqNum());
    pdu->SetServiceId(IM::BaseDefine::SID_FRIENDSHIP);
    pdu->SetCommandId(IM::BaseDefine::CID_FRIEND_SHIP_LIST_RES);
    CProxyConn::AddResponsePdu(conn_uuid, pdu);

}

void doUpdateFriendRelation(CImPdu *pPdu, uint32_t conn_uuid) //agree friend request
{
    do
    {
        IM::Friend::IMFriendShipUpdateRelationReq msg;
        IM::Friend::IMFriendShipUpdateRelationRes msgRes;
        if(!msg.ParseFromArray(pPdu->GetBodyData(),pPdu->GetBodyLength()))
            break;

        string user_id = msg.user_id();
        string friend_id = msg.friend_id();
        uint32_t opration = msg.update_opration();
        FriendShipStrategy strategy;
        bool bsuccess = strategy.updateFriendRelation(user_id,friend_id,opration);
        log("Update relation ret = %d",bsuccess);
        msgRes.set_user_id(user_id);
        msgRes.set_friend_id(friend_id);
        msgRes.set_update_status(!bsuccess);
        msgRes.set_attach_data(msg.attach_data());
        CImPdu *pduRes = new CImPdu();
        pduRes->SetPBMsg(&msgRes);
        pduRes->SetSeqNum(pPdu->GetSeqNum());
        pduRes->SetServiceId(IM::BaseDefine::SID_FRIENDSHIP);
        pduRes->SetCommandId(IM::BaseDefine::FriendShipCmdID::CID_FRIEND_SHIP_UPDATE_RELATION_RES);
        CProxyConn::AddResponsePdu(conn_uuid,pduRes);

        IM::Friend::IMFriendShipAddNotify notify;
        notify.set_from_user_id(user_id);
        notify.set_to_user_id(friend_id);
        notify.set_notify_value(opration);

        notify.set_attach_data(msg.attach_data());
        CImPdu * nPdu = new CImPdu();
        nPdu->SetPBMsg(&notify);
        nPdu->SetServiceId(IM::BaseDefine::SID_FRIENDSHIP);
        nPdu->SetCommandId(IM::BaseDefine::CID_FRIEND_SHIP_ADD_NOTIFY);
        CProxyConn::AddResponsePdu(conn_uuid,nPdu);

    }while(0);
}

void doGetUnGreedFriendList(CImPdu* pPdu, uint32_t conn_uuid)
{
    log("doGetUnGreedFriendList start");

    do{
        IM::Friend::IMFriendGetUnAgreeListReq msg;
        IM::Friend::IMFriendGetUnAgreeListRsp resMsg;
        if(!msg.ParseFromArray(pPdu->GetBodyData(),pPdu->GetBodyLength()))
            break;

        string user_id = msg.user_id();
        resMsg.set_user_id(user_id);

        std::vector<string> friends;
        FriendShipStrategy strategy;
        if(strategy.getUnAgreeFriendList(user_id,friends))
        {
            std::list<string> idList;
            for(int i=0;i<friends.size();i++)
                idList.push_back(friends[i]);

            std::list<IM::BaseDefine::UserInfo> lsUser;
            CUserModel::getInstance()->getUsers(idList, lsUser);
            for(list<IM::BaseDefine::UserInfo>::iterator it=lsUser.begin();
                    it!=lsUser.end(); ++it)
                {
                    IM::BaseDefine::UserInfo* pUser = resMsg.add_user_list();
                    pUser->set_user_id(it->user_id());
                    pUser->set_user_gender(it->user_gender());
                    pUser->set_user_nick_name(it->user_nick_name());
                    pUser->set_avatar_url(it->avatar_url());

                    pUser->set_sign_info(it->sign_info());
                    pUser->set_department_id(it->department_id());
                    pUser->set_email(it->email());
                    pUser->set_user_real_name(it->user_real_name());
                    pUser->set_user_tel(it->user_tel());
                    pUser->set_user_domain(it->user_domain());
                    pUser->set_status(it->status());
                }
                
        }
        resMsg.set_attach_data(msg.attach_data());

        CImPdu *pdu = new CImPdu();
        pdu->SetPBMsg(&resMsg);
        pdu->SetSeqNum(pPdu->GetSeqNum());
        pdu->SetServiceId(IM::BaseDefine::SID_FRIENDSHIP);
        pdu->SetCommandId(IM::BaseDefine::CID_FRIEND_SHIP_GET_UNAGREE_FRIEND_LIST_RES);
        CProxyConn::AddResponsePdu(conn_uuid,pdu);

    }while(0);

    log("doGetUnGreedFriendList end");

}
void doSearchFriend(CImPdu *pPdu, uint32_t conn_uuid)//search  friend request
{
    log("start search!");
    do
    {
        /* code */
        IM::Friend::IMFriendShipSearchReq msg;
        if(!msg.ParseFromArray(pPdu->GetBodyData(),pPdu->GetBodyLength()))
            break;

        string user_id = msg.user_id();
        std::string user_nick = msg.user_nick();
        std::string user_name = msg.user_name();
        FriendShipStrategy strategy;
        std::vector<IM::BaseDefine::UserInfo> vInfos;
        strategy.searchFriend(user_nick,user_name,vInfos);

        IM::Friend::IMFriendShipSearchRes res;
        res.set_user_id(user_id);
        for(int i=0;i < vInfos.size();i++)
        {
            IM::BaseDefine::UserInfo* pUser = res.add_user_list();
            pUser->set_user_id(vInfos[i].user_id());
            pUser->set_user_gender(vInfos[i].user_gender());
            pUser->set_user_nick_name(vInfos[i].user_nick_name());
            pUser->set_avatar_url(vInfos[i].avatar_url());

            pUser->set_sign_info(vInfos[i].sign_info());
            pUser->set_department_id(vInfos[i].department_id());
            pUser->set_email(vInfos[i].email());
            pUser->set_user_real_name(vInfos[i].user_real_name());
            pUser->set_user_tel(vInfos[i].user_tel());
            pUser->set_user_domain(vInfos[i].user_domain());
            pUser->set_status(vInfos[i].status());
        }
        res.set_attach_data(msg.attach_data());
        CImPdu *pdu = new CImPdu();
        pdu->SetPBMsg(&res);
        pdu->SetSeqNum(pPdu->GetSeqNum());
        pdu->SetServiceId(IM::BaseDefine::SID_FRIENDSHIP);
        pdu->SetCommandId(IM::BaseDefine::FriendShipCmdID::CID_FRIEND_SHIP_SEARCH_FRIEND_RES);
        CProxyConn::AddResponsePdu(conn_uuid,pdu);

    } while (0);
    log("end search!");

}

};