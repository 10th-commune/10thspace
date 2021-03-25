package com.tenth.space.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.R;
import com.tenth.space.config.IntentConstant;
import com.tenth.space.imservice.event.SearchFriendListEvent;
import com.tenth.space.imservice.manager.FriendManager;
import com.tenth.space.imservice.manager.IMContactManager;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.imservice.service.IMService;
import com.tenth.space.imservice.support.IMServiceConnector;
import com.tenth.space.protobuf.IMBaseDefine;
import com.tenth.space.protobuf.helper.ProtoBuf2JavaBean;
import com.tenth.space.ui.adapter.SearchAdapter;
import com.tenth.space.ui.widget.DrawableCenterEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

public class FriendsActivity extends Activity implements DrawableCenterEditText.DrawableRightListener, AdapterView.OnItemClickListener {
    private IMService imService;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.i("message_activity#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            initData();
        }

        @Override
        public void onServiceDisconnected() {
        }
    };
    private DrawableCenterEditText inputText;
    private ListView result;
    private List<IMBaseDefine.UserInfo> searchUserList;
    List<UserEntity> userList;
    private InputMethodManager imm;

    private void initData() {
        imServiceConnector.connect(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);
        EventBus.getDefault().register(this);
        inputText = (DrawableCenterEditText) findViewById(R.id.search_input);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputText.setDrawableRightListener(this);
        findViewById(R.id.go_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ShowSoft();
        result = (ListView) findViewById(R.id.search_result);
        result.setOnItemClickListener(this);
        inputText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    // 先隐藏键盘
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(FriendsActivity.this.getCurrentFocus()
                                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    //进行搜索操作的方法，在该方法中可以加入mEditSearchUser的非空判断
                    searchUser(v);
                }
                return false;
            }
        });
    }

    private void ShowSoft() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
                           public void run() {
                               imm.showSoftInput(inputText, 0);
                           }

                       },
                90);
    }

    public void onEventMainThread(Object event) {
        SearchFriendListEvent event_ = null;
        if (event instanceof SearchFriendListEvent) {
            findViewById(R.id.search_friend).setVisibility(View.GONE);
            event_ = (SearchFriendListEvent) event;
            searchUserList = event_.getSearchUserList();
            if (searchUserList.size() == 0) {
                result.setVisibility(View.GONE);
                findViewById(R.id.no_one).setVisibility(View.VISIBLE);
            } else {
                userList = new ArrayList<>();
                SearchAdapter searchAdapter = new SearchAdapter(this, imService);
//                finish();
                result.setVisibility(View.VISIBLE);
                findViewById(R.id.no_one).setVisibility(View.GONE);
                for (IMBaseDefine.UserInfo userInfo : searchUserList) {
                    UserEntity userEntity = ProtoBuf2JavaBean.getUserEntity(userInfo);
                    if (!IMLoginManager.instance().getLoginId().equals(userEntity.getPeerId())){//wystan add for filter self 210126
                        int status = userEntity.getStatus();
                        userList.add(userEntity);
                    }
                }
                searchAdapter.putUserList(userList);
                result.setAdapter(searchAdapter);
//                String avatar = userEntity.getAvatar();
//                int peerId = userEntity.getPeerId();
//                Intent intent = new Intent(this, UserActivity.class);
//                intent.putExtra("avatar", avatar);
//                intent.putExtra("peerId", peerId);
//                intent.putExtra("main_name", userEntity.getMainName());
//                startActivity(intent);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDrawableRightClick(View view) {
        searchUser(view);
    }

    private void searchUser(View view) {
        String s = inputText.getText().toString();
        if (!"".equals(s)) {
            FriendManager.instance().searchUser(s);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            findViewById(R.id.search_friend).setVisibility(View.VISIBLE);
//            List<UserEntity> userEntities = IMContactManager.instance().queryAllUser();
//            for(UserEntity userEntity:userEntities){
//                if(s.equals(userEntity.getMainName())){
//            Intent intent = new Intent(this, UserActivity.class);
//                    intent.putExtra("seach_text",s);
//            startActivity(intent);
//            finish();
//                    return;
//                }
//            }
        } else {
            Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserEntity userEntity = userList.get(position);
        String peerId = userEntity.getPub_key();
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra(IntentConstant.KEY_PEERID, peerId);
        if(userEntity.getStatus() == 17){
           intent.putExtra("isGroup",true);
        }else {
            IMContactManager.instance().addFriend(userEntity);
        }
        startActivity(intent);
        finish();
    }
}
