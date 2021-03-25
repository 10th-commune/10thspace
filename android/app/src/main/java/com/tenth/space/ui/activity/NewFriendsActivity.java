package com.tenth.space.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.tenth.space.DB.DBInterface;
import com.tenth.space.DB.entity.GroupRequesterEntity;
import com.tenth.space.DB.entity.RequesterEntity;
import com.tenth.space.R;
import com.tenth.space.imservice.event.PriorityEvent;
import com.tenth.space.imservice.manager.FriendManager;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.imservice.manager.IMSocketManager;
import com.tenth.space.protobuf.IMBaseDefine;
import com.tenth.space.protobuf.IMSystem;
import com.tenth.space.ui.adapter.NewFriendAdapter;
import com.tenth.space.ui.adapter.NewGroupAdapter;
import com.tenth.space.ui.widget.DrawableCenterEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.greenrobot.event.EventBus;

public class NewFriendsActivity extends Activity{

    private List<RequesterEntity> requesterEntities;
    private List<GroupRequesterEntity> groupRequesterEntities;
    private NewFriendAdapter adapter;
    private DrawableCenterEditText inputText;
    private boolean isGroup;
    private NewGroupAdapter groupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friends);
        Intent intent = getIntent();
        int unreadbuddycnt = intent.getIntExtra("unreadbuddycnt", 0);
        int unreadgroupcnt = intent.getIntExtra("unreadgroupcnt", 0);

        //已读添加好友消息
        FriendManager.instance().readAddFriendDate(IMLoginManager.instance().getLoginId());
        initView();
        getOutLineFriendRequest("buddy",unreadbuddycnt);
        getOutLineFriendRequest("group",unreadgroupcnt);
        EventBus.getDefault().register(this);
    }

    private void getOutLineFriendRequest(String type,int unreadcnt) {
        //获取离线好友请求
        if (unreadcnt > 0) {
            IMSystem.IMGetSysMsgDataReq msg = IMSystem.IMGetSysMsgDataReq.newBuilder()
                    .setMsgCnt(unreadcnt)
                    .setUserId(IMLoginManager.instance().getPub_key())
                    .setType(type.equals("group")?IMSystem.SysMsgType.SYS_MSG_GROUP:IMSystem.SysMsgType.SYS_MSG_BUDDY).build();
            int sid = IMBaseDefine.ServiceID.SID_SYS_MSG_VALUE;
            int cid = IMBaseDefine.SysMsgCmdID.CID_SYS_MSG_GET_DATA_REQUEST_VALUE;
            IMSocketManager.instance().sendRequest(msg,sid,cid);
        }
    }
    public void onEventMainThread(PriorityEvent event){
        switch (event.event){
            case MSG_UNREAD_DATA_ADD_RSP:
                IMSystem.IMGetSysMsgDataRsp imGetSysMsgDataRsp= (IMSystem.IMGetSysMsgDataRsp) event.object;
                List<IMSystem.IMSysMsgData> msgListList = imGetSysMsgDataRsp.getMsgListList();
                JSONObject infoObj;
                for(IMSystem.IMSysMsgData imSysMsgData : msgListList){
                    IMSystem.SysMsgOper type = imSysMsgData.getType();
                    String info = imSysMsgData.getAttachData().toStringUtf8();
                    try {
                        infoObj = new JSONObject(info);
                        String userId = imSysMsgData.getFromId();
                        //保存到本地数据库
                        if(type== IMSystem.SysMsgOper.ADD_FRIEND_AGREE || type== IMSystem.SysMsgOper.ADD_FRIEND_REQUEST){
                            RequesterEntity requesterEntity1= new RequesterEntity();
                            requesterEntity1.setFromUserId(userId);
                            requesterEntity1.setAddition_msg(infoObj.optString("addition_msg"));
                            requesterEntity1.setAvatar_url(infoObj.optString("avatar_url"));
                            requesterEntity1.setNick_name(infoObj.optString("nick_name"));
                            requesterEntity1.setCreated(System.currentTimeMillis());
                            requesterEntity1.setIsRead(true);
                            requesterEntity1.setAgree_states(1);
//                        if(requesterEntity1.getIsGroup()==isGroup){
                            requesterEntities.add(0,requesterEntity1);
                            adapter.notifyDataSetChanged();
//                        }
                            DBInterface.instance().batchInsertOrUpdateRquest(requesterEntity1);
                        }else if(type== IMSystem.SysMsgOper.ADD_GROUP_AGREE || type== IMSystem.SysMsgOper.ADD_GROUP_REQUEST){
                            GroupRequesterEntity requesterEntity1= new GroupRequesterEntity();
                            requesterEntity1.setFromUserId(userId);
                            requesterEntity1.setGroup_id(infoObj.optString("group_id"));
                            requesterEntity1.setAddition_msg(infoObj.optString("addition_msg"));
                            requesterEntity1.setAvatar_url(infoObj.optString("avatar_url"));
                            requesterEntity1.setNick_name(infoObj.optString("nick_name"));
                            requesterEntity1.setCreated(System.currentTimeMillis());
                            requesterEntity1.setIsRead(true);
                            requesterEntity1.setAgree_states(1);
//                        if(requesterEntity1.getIsGroup()==isGroup){
                            groupRequesterEntities.add(0,requesterEntity1);
                            groupAdapter.notifyDataSetChanged();
//                        }
                            DBInterface.instance().batchInsertOrUpdateRquest(requesterEntity1);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

//                    if(isGroup){
                        FriendManager.instance().AddReadDataAck(IMSystem.SysMsgType.SYS_MSG_GROUP);
//                    }else {
//                        //通知已读离线好友请求
//                        FriendManager.instance().AddReadDataAck(IMSystem.SysMsgType.SYS_MSG_BUDDY);
//                    }
                }
//                IMBuddy.IMGetAddFriendDataRsp imGetAddFriendDataRsp = (IMBuddy.IMGetAddFriendDataRsp) event.object;
//                List<IMBuddy.IMAddFriendData> dataListList = imGetAddFriendDataRsp.getDataListList();
//                JSONObject infoObj;
//                for(IMBuddy.IMAddFriendData data:dataListList){
//                    String info = data.getAddFriendData().toStringUtf8();
//                    try {
//                        infoObj= new JSONObject(info);
//                        int userId = data.getUserId();
//                        //保存到本地数据库
//                        RequesterEntity requesterEntity= new RequesterEntity();
//                        requesterEntity.setFromUserId(userId);
//                        requesterEntity.setAddition_msg(infoObj.optString("addition_msg"));
//                        requesterEntity.setAvatar_url(infoObj.optString("avatar_url"));
//                        requesterEntity.setNick_name(infoObj.optString("nick_name"));
//                        requesterEntity.setCreated(System.currentTimeMillis());
//                        requesterEntity.setIsRead(true);
//                        requesterEntity.setAgree_states(1);
//                        requesterEntities.add(0,requesterEntity);
//                        DBInterface.instance().batchInsertOrUpdateRquest(requesterEntity);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//                adapter.notifyDataSetChanged();
//                //通知已读离线好友请求
//                FriendManager.instance().AddFriendReadDataAck();
                break;

        }
    }
    private void initView() {
        findViewById(R.id.text_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewFriendsActivity.this,FriendsActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.go_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewFriendsActivity.this,FriendsActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.go_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ListView listView=(ListView)findViewById(R.id.friends_list);
        ListView groupList=(ListView)findViewById(R.id.group_list);
        requesterEntities = DBInterface.instance().setAllRequestIsRead();
        groupRequesterEntities = DBInterface.instance().setAllGroupRequestIsRead();
        inputText=(DrawableCenterEditText) findViewById(R.id.search_input);
        adapter=new NewFriendAdapter(requesterEntities,this);
        listView.setAdapter(adapter);
        groupAdapter=new NewGroupAdapter(groupRequesterEntities,this);
        groupList.setAdapter(groupAdapter);
        listView.setAdapter(adapter);
    }

    public void setUserInfo(RequesterEntity entity) {

        Intent intent = new Intent();
        intent.putExtra("entity",entity);
        setResult(200,intent);
        this.finish();
    }
    public void setUserInfo(GroupRequesterEntity entity) {
        Intent intent = new Intent();
        intent.putExtra("entity",entity);
        setResult(200,intent);
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
