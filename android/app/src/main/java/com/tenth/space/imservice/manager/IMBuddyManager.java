package com.tenth.space.imservice.manager;

import android.os.Handler;
import android.os.Looper;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.tenth.space.DB.DBInterface;
import com.tenth.space.imservice.callback.Packetlistener;
import com.tenth.space.imservice.event.BlogInfoEvent;
import com.tenth.space.imservice.event.UserInfoEvent;
import com.tenth.space.protobuf.IMBaseDefine;
import com.tenth.space.protobuf.IMBuddy;
import com.tenth.space.protobuf.IMSystem;
import com.tenth.space.utils.LogUtils;
import com.tenth.space.utils.ToastUtils;

import java.io.IOException;

import de.greenrobot.event.EventBus;

import static com.tenth.space.imservice.event.UserInfoEvent.Event.USER_INFO_CHANGED_NOTIFY;
import static com.tenth.space.imservice.event.UserInfoEvent.Event.USER_INFO_CHANGE_AVATAR;

/**
 * Created by wsq on 2016/11/11.
 */

public class IMBuddyManager extends IMManager {

    IMSocketManager imSocketManager = IMSocketManager.instance();
    private static IMBuddyManager inst = new IMBuddyManager();
    private DBInterface dbInterface = DBInterface.instance();
    private int position;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public static IMBuddyManager instance() {
        return inst;
    }

    //发送关注
    public void reqFollowUser(String friendId, int position) {
        this.position = position;
        String userId = IMLoginManager.instance().getPub_key();

        IMBuddy.IMFollowUserReq followUserReq
                = IMBuddy.IMFollowUserReq
                .newBuilder()
                .setUserId(userId)
                .setFriendId(friendId)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_FOLLOW_USER_REQUEST_VALUE;
        imSocketManager.sendRequest(followUserReq, sid, cid);
        LogUtils.d("IMBuddyManager-----reqFollowUser:发送关注请求(loginId:" + userId + "--writerId:" + friendId + ")");
    }
    public void agreeFriend(String userId, String toId, IMSystem.SysMsgOper type, String Msg) {
        ByteString addtionMsg= ByteString.copyFromUtf8(Msg);
        IMSystem.IMSysMsgData imSysMsgData = IMSystem.IMSysMsgData.newBuilder()
                .setFromId(userId)
                .setToId(toId)
                .setType(type)
                .build();
        IMSystem.IMSendSysMsgReq imSendSysMsgReq = IMSystem.IMSendSysMsgReq.newBuilder()
                .setSysMsg(imSysMsgData)
                .setUserId(userId)
                .setAttachData(addtionMsg)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_SYS_MSG_VALUE;
        int cid = IMBaseDefine.SysMsgCmdID.CID_SYS_MSG_SEND_DATA_REQUEST_VALUE;
        IMSocketManager.instance().sendRequest(imSendSysMsgReq, sid, cid);
    }
    public void onReqFollowUser(IMBuddy.IMFollowUserRsp rsp) {
        String rspUTF8 = rsp.getAttachData().toStringUtf8();
        if (rsp.getResultCode() == 0) {
            LogUtils.d("关注成功");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.show("关注成功");
                }
            });
            IMContactManager.instance().reqGetAllUsers(0);
            EventBus.getDefault().postSticky(new BlogInfoEvent(BlogInfoEvent.Event.FOLLOW_SUCCESS, position));
        } else {
            ToastUtils.show("关注失败");
            LogUtils.d("关注失败");
        }
    }

    public void reqDelFollowUser(String friendId, int position) {
        this.position = position;
        String userId = IMLoginManager.instance().getPub_key();

        IMBuddy.IMFollowUserReq followUserReq
                = IMBuddy.IMFollowUserReq
                .newBuilder()
                .setUserId(userId)
                .setFriendId(friendId)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_DEL_FOLLOW_USER_REQUEST_VALUE;
        imSocketManager.sendRequest(followUserReq, sid, cid);
    }

    public void onReqDelFollowUser(IMBuddy.IMDelFollowUserRsp rsp) {
        if (rsp.getResultCode() == 0) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.show("取消关注成功");

                }
            });
            IMContactManager.instance().reqGetAllUsers(0);//每次更新好友列表只能添加，或者更新，不能够删除好友，此处要删除已关注的好友
            EventBus.getDefault().postSticky(new BlogInfoEvent(BlogInfoEvent.Event.DEL_FOLLOW_SUCCESS, position));
        } else {
            ToastUtils.show("取消关注失败");
        }
    }

    public void reqChangeAvatar(ByteString urlBytes) {
        String userId = IMLoginManager.instance().getPub_key();
        IMBuddy.IMChangeAvatarReq changeAvatarReq
                = IMBuddy.IMChangeAvatarReq
                .newBuilder()
                .setUserId(userId)
                .setAvatarUrlBytes(urlBytes)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_CHANGE_AVATAR_REQUEST_VALUE;
        imSocketManager.sendRequest(changeAvatarReq, sid, cid, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMBuddy.IMChangeAvatarRsp changeAvatarRsp = IMBuddy.IMChangeAvatarRsp.parseFrom((CodedInputStream) response);
                    int resultCode = changeAvatarRsp.getResultCode();
                    if (resultCode == 0) {
                        EventBus.getDefault().postSticky(new UserInfoEvent(USER_INFO_CHANGE_AVATAR));
                        LogUtils.d("修改头像成功的回调:resultCode:" + resultCode);
                        ToastUtils.show("设置头像成功");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFaild() {
                LogUtils.d("wystan 修改头像失败" );
            }

            @Override
            public void onTimeout() {
                LogUtils.d("wystan 修改头像超时" );
            }
        });
        LogUtils.d("IMBuddyManager-----reqChangeAvatar:改变头像请求发送");
    }

    public void onChangeAvatarNotify(IMBuddy.IMAvatarChangedNotify rsp) {
        int changedUserId = rsp.getChangedUserId();
        String avatarUrl = rsp.getAvatarUrl();

        LogUtils.d("IMBuddyManager-----onChangeAvatarNotify:有人好友的头像修改了");
        EventBus.getDefault().postSticky(new UserInfoEvent(USER_INFO_CHANGED_NOTIFY, changedUserId, avatarUrl));
    }

    @Override
    public void doOnStart() {
    }

    @Override
    public void reset() {
    }
}
