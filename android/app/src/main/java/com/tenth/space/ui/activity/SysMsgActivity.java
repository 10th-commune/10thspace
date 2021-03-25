package com.tenth.space.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.tenth.space.DB.DBInterface;
import com.tenth.space.DB.entity.SysPushMsgEntity;
import com.tenth.space.R;
import com.tenth.space.imservice.event.PriorityEvent;
import com.tenth.space.imservice.manager.IMUnreadMsgManager;
import com.tenth.space.protobuf.IMSystem;
import com.tenth.space.ui.adapter.SysPushMsgAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.greenrobot.event.EventBus;



public class SysMsgActivity extends Activity {

    private List<SysPushMsgEntity> sysPushMsgEntitys;
    private SysPushMsgAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_msg);
        EventBus.getDefault().register(this);
        setData();
    }

    private void setData() {
        findViewById(R.id.go_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ListView msg_list = (ListView) findViewById(R.id.sys_msg_list);
        sysPushMsgEntitys = DBInterface.instance().loadAllSysNsg();
        adapter = new SysPushMsgAdapter(sysPushMsgEntitys,this);
        msg_list.setAdapter(adapter);
        Intent intent = getIntent();
        int cnt = intent.getIntExtra("cnt", 0);
        if (cnt > 0) {
            IMUnreadMsgManager.instance().getUnreadSysMsgRequest(cnt, IMSystem.SysMsgType.SYS_MSG_SYSTEM);
        }
    }

    public void onEventMainThread(PriorityEvent event) {
        switch (event.event) {
            case MSG_UNREAD_DATA_ADD_RSP:
                IMSystem.IMGetSysMsgDataRsp imGetSysMsgDataRsp = (IMSystem.IMGetSysMsgDataRsp) event.object;
                List<IMSystem.IMSysMsgData> msgListList = imGetSysMsgDataRsp.getMsgListList();
                JSONObject infoObj;
                SysPushMsgEntity sysPushMsgEntity=null;
                for (IMSystem.IMSysMsgData imSysMsgData : msgListList) {
                    IMSystem.SysMsgOper type = imSysMsgData.getType();
                    String info = imSysMsgData.getAttachData().toStringUtf8();
                    try {
                        infoObj = new JSONObject(info);
                        switch (type) {
                            case PUSH_SYSTEM_MSG:
                                break;
                            case PUSH_INVITE_ADD_FRIEND_MSG:
                                sysPushMsgEntity = new SysPushMsgEntity();
                                sysPushMsgEntity.setMsg_type(1);
                                sysPushMsgEntity.setFromUserId(infoObj.optString("user_id"));
                                sysPushMsgEntity.setAddition_msg(infoObj.optString("addition_msg"));
                                sysPushMsgEntity.setAvatar_url(infoObj.optString("avatar_url"));
                                sysPushMsgEntity.setNick_name(infoObj.optString("nick_name"));
                                sysPushMsgEntity.setCreated(System.currentTimeMillis());
                                sysPushMsgEntity.setIsRead(true);
                                sysPushMsgEntity.setAgree_states(1);
                                break;
                            case PUSH_INVITE_ADD_GROUP_MSG:
                                sysPushMsgEntity = new SysPushMsgEntity();
                                sysPushMsgEntity.setMsg_type(2);
                                sysPushMsgEntity.setFromUserId(infoObj.optString("group_id"));
                                sysPushMsgEntity.setAddition_msg(infoObj.optString("addition_msg"));
                                sysPushMsgEntity.setAvatar_url(infoObj.optString("avatar_url"));
                                sysPushMsgEntity.setNick_name(infoObj.optString("nick_name"));
                                sysPushMsgEntity.setCreated(System.currentTimeMillis());
                                sysPushMsgEntity.setIsRead(true);
                                sysPushMsgEntity.setAgree_states(1);
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    sysPushMsgEntitys.add(0, sysPushMsgEntity);
                    adapter.notifyDataSetChanged();
                    DBInterface.instance().batchInsertOrUpdateRquest(sysPushMsgEntity);
                }
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
