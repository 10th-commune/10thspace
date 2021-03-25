package com.tenth.space.imservice.manager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.google.protobuf.ByteString;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.imservice.event.PriorityEvent;
import com.tenth.space.imservice.event.SearchFriendListEvent;
import com.tenth.space.protobuf.IMBaseDefine;
import com.tenth.space.protobuf.IMBuddy;
import com.tenth.space.protobuf.IMSystem;

import java.io.UnsupportedEncodingException;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.tenth.space.imservice.event.PriorityEvent.Event.MSG_DEL_FRIEND_RSP;
import static com.tenth.space.imservice.event.PriorityEvent.Event.MSG_UPDATE_USERINFO_SUCEED;

/**
 * Created by wing on 2016/7/11.
 */
public class FriendManager extends IMManager {
    public FriendManager() {
    }

    @Override
    public void doOnStart() {
    }

    @Override
    public void reset() {
        //userDataReady = false;
        //userMap.clear();
    }

    // 单例
    private static FriendManager inst = new FriendManager();

    public static FriendManager instance() {
        return inst;
    }
    public void onReqFriendsList(IMBuddy.IMSearchUserRsp rsp) {
        List<IMBaseDefine.UserInfo> searchUserList = rsp.getSearchUserListList();
        SearchFriendListEvent event = new SearchFriendListEvent();
        event.setEvent(SearchFriendListEvent.Event.SEARCH);
        event.setSearchUserList(searchUserList);
        EventBus.getDefault().postSticky(event);
//        for (IMBaseDefine.UserInfo userInfo:searchUserListList){
//            String userRealName = userInfo.getUserRealName();
//            String userNickName = userInfo.getUserNickName();
//            int userId = userInfo.getUserId();
//        }
    }
    public void onOperateFriendRsp(Object object){
        PriorityEvent priorityEvent=new PriorityEvent();
 //       if(object instanceof IMBuddy.IMAddFriendData){
//            priorityEvent.event=MSG_SYSTEM;
//        }else if(object instanceof IMBuddy.IMAddFriendRsp){
//            priorityEvent.event=MSG_ADD_FRIEND_RSP;
//        }else if(object instanceof IMBuddy.IMAgreeAddFriendRsp){
//            priorityEvent.event=MSG_ADD_AGREE_FRIEND_RSP;
//        }else
 //           if(object instanceof IMBuddy.IMDelFriendRsp){
  //          priorityEvent.event=MSG_DEL_FRIEND_RSP;
 //       }
//            else if(object instanceof IMBuddy.IMAddFriendUnreadCntRsp){
//            priorityEvent.event=MSG_UNREAD_CNT_ADD_RSP;
//        }else if(object instanceof IMBuddy.IMGetAddFriendDataRsp){
//            priorityEvent.event=MSG_UNREAD_DATA_ADD_RSP;
     //   }else
           if(object instanceof IMBuddy.IMUpdateUsersInfoRsp){
            priorityEvent.event= MSG_UPDATE_USERINFO_SUCEED;
        }
//        else {
//            LogUtils.d(object.toString());
//        }
        priorityEvent.object=object;
        EventBus.getDefault().postSticky(priorityEvent);
    }
    public void readAddFriendDate(String Id){
//        IMBuddy.IMAddFriendReadDataAck msg = IMBuddy.IMAddFriendReadDataAck.newBuilder().setUserId(Id).build();
//        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
//        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_ADD_FRIEND_READ_DATA_ACK_VALUE;
//        IMSocketManager.instance().sendRequest(msg, sid, cid);
    }
    public void searchUser(String buddyname) {
        String userId = IMLoginManager.instance().getPub_key();
        IMBuddy.IMSearchUserReq msg = IMBuddy.IMSearchUserReq.newBuilder()
                .setUserId(userId)
                .setSearchUserName(buddyname)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_SEARCH_USER_REQUEST_VALUE;
        IMSocketManager.instance().sendRequest(msg, sid, cid);
    }
    public void reqAddFriend(String addtionMsg,String friendId,IMSystem.SysMsgOper type) {
        String userId = IMLoginManager.instance().getPub_key();
        ByteString sendContent = null;
        try {
            String content = addtionMsg;
            sendContent = ByteString.copyFrom(content.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        IMSystem.IMSysMsgData imSysMsgData = IMSystem.IMSysMsgData.newBuilder()
                .setFromId(userId)
                .setToId(friendId)
                .setType(type)
                .setAttachData(sendContent)
                .build();
        IMSystem.IMSendSysMsgReq imSendSysMsgReq = IMSystem.IMSendSysMsgReq.newBuilder()
                .setUserId(userId)
                .setSysMsg(imSysMsgData)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_SYS_MSG_VALUE;
        int cid = IMBaseDefine.SysMsgCmdID.CID_SYS_MSG_SEND_DATA_REQUEST_VALUE;
        IMSocketManager.instance().sendRequest(imSendSysMsgReq, sid, cid);
    }
    public void deleteFriend(Context context, final UserEntity userEntity){
        new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_LIGHT)
                .setTitle("确定删除好友？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //删除好友请求
                        IMBuddy.IMDelFriendReq msg = IMBuddy.IMDelFriendReq.newBuilder()
                                .setFriendId(userEntity.getPeerId())
                                .setUserId("0")
                                .build();
                        int sid= IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
                        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_DEL_FRIEND_REQUEST_VALUE;
                        IMSocketManager.instance().sendRequest(msg,sid,cid);
                    }
                })
                .setNegativeButton("取消",null)
                .create()
                .show();
    }
    public void deleteFriend(final Context context, final String peerId){
        new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_LIGHT)
                .setTitle("确定删除好友？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //删除好友请求
                        IMBuddy.IMDelFriendReq msg = IMBuddy.IMDelFriendReq.newBuilder()
                                .setFriendId(peerId)
                                .setUserId("0")
                                .build();
                        int sid= IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
                        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_DEL_FRIEND_REQUEST_VALUE;
                        IMSocketManager.instance().sendRequest(msg,sid,cid);
                    }
                })
                .setNegativeButton("取消",null)
                .create()
                .show();
    }
    public void AddReadDataAck(IMSystem.SysMsgType type){
        IMSystem.IMSysMsgReadDataAck msg = IMSystem.IMSysMsgReadDataAck.newBuilder()
                .setType(type)
                .setUserId(IMLoginManager.instance().getPub_key())
                .build();
        int sid= IMBaseDefine.ServiceID.SID_SYS_MSG_VALUE;
        int cid = IMBaseDefine.SysMsgCmdID.CID_SYS_MSG_READ_ACK_VALUE;
        IMSocketManager.instance().sendRequest(msg,sid,cid);
    }

    public void reqRecommendUsers() {
        String userId = IMLoginManager.instance().getPub_key();
        IMBuddy.IMGetALLOnlineUserReq msg = IMBuddy.IMGetALLOnlineUserReq.newBuilder()
                .setUserId(userId)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_GET_ALL_ONLINE_USER_INFO_REQUEST_VALUE;
        IMSocketManager.instance().sendRequest(msg, sid, cid);
    }

    public void onReqRecommendUsers(IMBuddy.IMGetALLOnlineUserRsp rsp) {
        List<IMBaseDefine.UserInfo> recommendUserList = rsp.getUserListList();
        SearchFriendListEvent event = new SearchFriendListEvent();
        event.setEvent(SearchFriendListEvent.Event.RECOMMEND);
        event.setSearchUserList(recommendUserList);
        EventBus.getDefault().postSticky(event);
    }
}