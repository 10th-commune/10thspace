package com.tenth.space.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.tenth.space.DB.entity.GroupEntity;
import com.tenth.space.R;
import com.tenth.space.imservice.event.GroupEvent;
import com.tenth.space.imservice.manager.IMContactManager;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.imservice.service.IMService;
import com.tenth.space.imservice.support.IMServiceConnector;
import com.tenth.space.ui.adapter.ContactAdapter;
import com.tenth.space.utils.IMUIHelper;

import java.util.List;

import de.greenrobot.event.EventBus;

public class GroupActivity extends Activity {
    public IMContactManager contactMgr;
    private IMService imService;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                logger.e("ContactFragment#onIMServiceConnected# imservice is null!!");
                return;
            }
            contactMgr = imService.getContactManager();
            // 初始化视图
            if (imService.getGroupManager().isGroupReady()) {
                setData();
                renderGroupList();
            }
        }

        @Override
        public void onServiceDisconnected() {
        }
    };

    private ListView groupList;
    private ContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        imServiceConnector.connect(this);
        EventBus.getDefault().register(this);
    }

    private void setData() {
        groupList = (ListView) findViewById(R.id.group_list);
        contactAdapter = new ContactAdapter(this, imService);
        groupList.setAdapter(contactAdapter);
        groupList.setOnItemClickListener(contactAdapter);
        groupList.setOnItemLongClickListener(contactAdapter);
        findViewById(R.id.go_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMUIHelper.openGroupMemberSelectActivity(GroupActivity.this,"1_"+ IMLoginManager.instance().getLoginId(), 0);//wystan modify to only create tmp group
            }
        });
        findViewById(R.id.go_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void onEventMainThread(GroupEvent event) {
        switch (event.getEvent()) {
            case GROUP_INFO_UPDATED:
            case GROUP_INFO_OK:
            case CREATE_GROUP_OK:
                renderGroupList();
                searchDataReady();
                break;
        }
    }

    public void searchDataReady() {
        if (imService.getContactManager().isUserDataReady() &&
                imService.getGroupManager().isGroupReady()) {
//            showSearchFrameLayout();
        }
    }

    private void renderGroupList() {
        List<GroupEntity> originList = imService.getGroupManager().getNormalGroupSortedList();
        contactAdapter.putGroupList(originList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        imServiceConnector.disconnect(this);
    }
}
