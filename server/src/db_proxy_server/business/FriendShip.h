/*================================================================
*     Copyright (c) 2020年 Aigan. All rights reserved.
*   
*   文件名称：FriendShip.h
*   创 建 者：lcf
*   邮    箱：754759490@qq.com
*   创建日期：2020年05月28日
*   描    述：Add friends 
*
#pragma once
================================================================*/
#ifndef __FRIENDSHIP_H__
#define __FRIENDSHIP_H__


#include <iostream>
#include <vector>
#include <string>

#include "IM.BaseDefine.pb.h"
#include "ImPduBase.h"

namespace DB_PROXY {

void doAddFriend(CImPdu* pPdu, uint32_t conn_uuid); // add friend request
void doGetFriendList(CImPdu* pPdu, uint32_t conn_uuid); //get friend list
void doGetUnGreedFriendList(CImPdu* pPdu, uint32_t conn_uuid);//get unagreed list
void doUpdateFriendRelation(CImPdu *pPdu, uint32_t conn_uuid); //agree friend request
void doSearchFriend(CImPdu *pPdu, uint32_t conn_uuid); //search  friend request

};

class FriendShipStrategy 
{
public:
    //通过好友ID添加
    bool addFriend(string myid,string friendid,uint32_t status);
    //通过好友昵称添加
    int  addFriendWithNickName(string myid,string friendid,const std::string &nickname,IM::BaseDefine::UserInfo &info);
    //获取好友列表
    bool getFriendList(string myid,std::vector<string> & friendList,int status = 0);
    //获取好友列表，暂时不用
    bool getUserInfos(const std::vector<string> & ids,std::vector<IM::BaseDefine::UserInfo> &infos);
    //获取未同意好友列表
    bool getUnAgreeFriendList(string user_id,std::vector<string> &friends);
    //更新好友状态
    bool updateFriendRelation(string myid, string friendid, uint32_t update_flag);
    //按名称或者昵称查找好友
    bool searchFriend(const std::string & user_nick,const std::string & user_name,std::vector<IM::BaseDefine::UserInfo> &infos);
    
};

#endif /*defined(__FRIENDSHIP_H__) */
