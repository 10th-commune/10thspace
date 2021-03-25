package com.tenth.space.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tenth.space.R;
import com.tenth.space.config.IntentConstant;
import com.tenth.space.imservice.event.LoginEvent;
import com.tenth.space.imservice.manager.IMLoginManager;

import de.greenrobot.event.EventBus;

public class DialogActivity extends Activity implements View.OnClickListener {
private ProgressBar progress;
private TextView tv_content;
private Button bt_close;
private Button bt_ensure;
    private boolean click;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setFinishOnTouchOutside(false);//点击外部不消失
        setContentView(R.layout.activity_dialog);
        EventBus.getDefault().register(this);
        initView();
        click=getIntent().getBooleanExtra("click",false);
        if (click){
            bt_ensure.performClick();//点击事件
        }

    }

    private void initView() {
        progress = (ProgressBar)findViewById(R.id.progress);
        tv_content = (TextView)findViewById(R.id.tv_content);
        bt_close = (Button)findViewById(R.id.bt_close);
        bt_close.setOnClickListener(this);
        bt_ensure = (Button)findViewById(R.id.bt_ensure);
        bt_ensure.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_close:
                IMLoginManager.instance().setKickout(false);
                IMLoginManager.instance().logOut();
                finish();

                break;
            case R.id.bt_ensure:
                tv_content.setText("重连中，请稍后……");
                progress.setVisibility(View.VISIBLE);
                IMLoginManager.instance().relogin();
                break;
        }
    }
    public void onEventMainThread(LoginEvent event){
        switch (event){
            case LOGIN_INNER_FAILED:
           //case TIME_OUT:
                tv_content.setText("网络超时，请稍后再试！");
                progress.setVisibility(View.GONE);
                //登录失败
                break;

            case LOCAL_LOGIN_MSG_SERVICE:
            case LOGIN_OK:
                progress.setVisibility(View.GONE);
                finish();
                //登录成功后
                break;

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);//true对任何Activity都适用
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
