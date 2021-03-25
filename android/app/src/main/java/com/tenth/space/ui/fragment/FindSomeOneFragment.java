package com.tenth.space.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tenth.space.BitherjSettings;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.R;
//import com.tenth.space.aliyun.Config;
import com.tenth.space.config.IntentConstant;
import com.tenth.space.imservice.event.UserInfoEvent;
import com.tenth.space.imservice.manager.FriendManager;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.imservice.service.IMService;
import com.tenth.space.imservice.support.IMServiceConnector;
import com.tenth.space.protobuf.IMSystem;
import com.tenth.space.ui.activity.FriendsActivity;
import com.tenth.space.ui.activity.MainActivity;
import com.tenth.space.ui.activity.SettingActivity;
import com.tenth.space.ui.activity.SysMsgActivity;
import com.tenth.space.ui.activity.UserInfoActivity;
import com.tenth.space.ui.widget.IMBaseImageView;
import com.tenth.space.utils.FileUtil;
import com.tenth.space.utils.QRCodeUtils;
import com.tenth.space.utils.ToastUtils;
import com.tenth.space.utils.Utils;

import java.io.File;

import de.greenrobot.event.EventBus;

import static android.app.Activity.RESULT_OK;


public class FindSomeOneFragment extends MainFragment {
    private View curView = null;
    private View contentView;
    private View settingView;
    private Button serachBt;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            if (curView == null) {
                return;
            }
            IMService imService = imServiceConnector.getIMService();
            if (imService == null) {
                return;
            }
            if (!imService.getContactManager().isUserDataReady()) {//wystan diasble 210115
                logger.i("detail#contact data are not ready");
            } else {
                init(imService);

            }
        }
    };
    private TextView nickNameView;
    private TextView userNameView;
    //private View ranking_list;
    //private View step;
    private TextView sysMsgCnt;
    private int cnt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        imServiceConnector.connect(getActivity());
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_findsomeone, topContentView);
        initRes();
        return curView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        cnt = ((MainActivity) getActivity()).getUnreadSysMsgCnt();
        if(!hidden && cnt>0){
            sysMsgCnt.setVisibility(View.VISIBLE);
            sysMsgCnt.setText(cnt+"");
        }
    }

    /**
     * @Description 初始化资源
     */
    private void initRes() {
        super.init(curView);
        sysMsgCnt = (TextView) curView.findViewById(R.id.sys_message);
        contentView = curView.findViewById(R.id.content);

        settingView = curView.findViewById(R.id.settingPage);
        //ranking_list = curView.findViewById(R.id.ranking_list);
        //step = curView.findViewById(R.id.step);
        serachBt = curView.findViewById(R.id.bt_search);

        curView.findViewById(R.id.sys_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        settingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        serachBt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendsActivity.class);
                startActivity(intent);
            }
        });

        hideContent();
        hideTopBar();
    }

    private void hideContent() {
        if (contentView != null) {
            contentView.setVisibility(View.GONE);
        }
    }

    private void showContent() {
        if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 应该放在这里嘛??
        imServiceConnector.disconnect(getActivity());
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initHandler() {
    }

    public void onEventMainThread(UserInfoEvent.Event event) {
        switch (event) {

        }
    }

    public UserEntity loginContact;

    private void init(IMService imService) {
        showContent();
        hideProgressBar();

        if (imService == null) {
            return;
        }

        loginContact = imService.getLoginManager().getLoginInfo();
        if (loginContact == null) {
            return;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {

        }
    }

    private boolean checkPermission(){
        int permission = ActivityCompat.checkSelfPermission(getActivity(),
                BitherjSettings.STORAGE_PERMISSIONS[BitherjSettings.REQUEST_EXTERNAL_STORAGE_WRITE]);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), BitherjSettings.STORAGE_PERMISSIONS, BitherjSettings.PERMISSION_REQUEST_CODE_WRITE);// requestCode Application specific request code to match with a result
            return  false;
        }

        return true;
    }
}