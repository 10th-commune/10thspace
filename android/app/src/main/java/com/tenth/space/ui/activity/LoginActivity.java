package com.tenth.space.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tenth.space.DB.entity.IdCardEntity;
import com.tenth.space.DB.sp.LoginSp;
import com.tenth.space.DB.sp.SystemConfigSp;
import com.tenth.space.R;
import com.tenth.space.config.IntentConstant;
import com.tenth.space.config.UrlConstant;
import com.tenth.space.imservice.event.LoginEvent;
import com.tenth.space.imservice.event.SocketEvent;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.imservice.manager.IMSocketManager;
import com.tenth.space.imservice.service.IMService;
import com.tenth.space.imservice.support.IMServiceConnector;
import com.tenth.space.protobuf.IMBaseDefine;
import com.tenth.space.protobuf.IMSystem;
import com.tenth.space.ui.base.TTBaseActivity;
import com.tenth.space.utils.IMUIHelper;
import com.tenth.space.utils.LogUtils;
import com.tenth.space.utils.Logger;
import com.tenth.space.utils.PrivateKeyUtil;
import com.tenth.space.utils.Utils;
import com.tenth.space.utils.crypto.ECKey;
import com.tenth.space.utils.crypto.SecureCharSequence;
import com.tenth.space.utils.xrandom.XRandom;

import de.greenrobot.event.EventBus;


/**
 * @YM 1. 链接成功之后，直接判断是否loginSp是否可以直接登陆
 * true: 1.可以登陆，从DB中获取历史的状态
 * 2.建立长连接，请求最新的数据状态 【网络断开没有这个状态】
 * 3.完成
 * <p/>
 * false:1. 不能直接登陆，跳转到登陆页面
 * 2. 请求消息服务器地址，链接，验证，触发loginSuccess
 * 3. 保存登陆状态
 */
public class LoginActivity extends TTBaseActivity implements View.OnClickListener {

    private Logger logger = Logger.getLogger(LoginActivity.class);
    private Handler uiHandler = new Handler();
    private EditText mNameView;
    private EditText mPasswordView;
    private View loginPage;
    private View splashPage;
    private View mLoginStatusView;
    private TextView mSwitchLoginServer;
    private InputMethodManager intputManager;
    private TextView mRegister;


    private IMService imService;
    private boolean autoLogin = true;
    private boolean loginSuccess = false;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            logger.i("login#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            try {
                do {
                    if (imService == null) {
                        //后台服务启动链接失败
                        LogUtils.e("LoginActivity------启动服务失败");
                        break;
                    }

                    IMLoginManager loginManager = imService.getLoginManager();
                    LoginSp loginSp = imService.getLoginSp();
                    if (loginManager == null || loginSp == null) {
                        // 无法获取登陆控制器
                        break;
                    }

                    LoginSp.SpLoginIdentity loginIdentity = loginSp.getLoginIdentity();
                    if (loginIdentity == null) {
                        // 之前没有保存任何登陆相关的，跳转到登陆页面
                        break;
                    }

                    mNameView.setText(loginIdentity.getLoginName());
                    if (TextUtils.isEmpty(loginIdentity.getPwd())) {
                        // 密码为空，可能是loginOut
                        break;
                    }
                    mPasswordView.setText(loginIdentity.getPwd());

                    if (autoLogin == false) {
                        break;
                    }

                    //自动登录
                    handleGotLoginIdentity(loginIdentity);

                    return;
                } while (false);

                //所有break都需要要重新登录，都会执行这个
                handleNoLoginIdentity();
            } catch (Exception e) {
                // 任何未知的异常，都需要重新登录
                logger.w("loadIdentity failed");
                handleNoLoginIdentity();
            }
        }
    };
    private TextView findPwd;

    /**
     * 跳转到登陆的页面
     */
    private void handleNoLoginIdentity() {
        logger.i("login#handleNoLoginIdentity");
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showLoginPage();
            }
        }, 1000);
    }

    /**
     * 自动登陆
     */
    private void handleGotLoginIdentity(final LoginSp.SpLoginIdentity loginIdentity) {
        logger.i("login#handleGotLoginIdentity");

        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                logger.i("login#start auto login");
                if (imService == null || imService.getLoginManager() == null) {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                    showLoginPage();
                }
                imService.getLoginManager().login(loginIdentity);
            }
        }, 500);
    }


    private void showLoginPage() {
        splashPage.setVisibility(View.GONE);
        loginPage.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {

            finish();
            return;
        }
        intputManager = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        logger.i("login#onCreate");
        SystemConfigSp.instance().init(getApplicationContext());
        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, UrlConstant.ACCESS_MSG_ADDRESS);
        }

        imServiceConnector.connect(LoginActivity.this);
        EventBus.getDefault().register(this);

        setContentView(R.layout.tt_activity_login);
        mSwitchLoginServer = (TextView) findViewById(R.id.sign_switch_login_server);
        mSwitchLoginServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, android.R.style.Theme_Holo_Light_Dialog));
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialog_view = inflater.inflate(R.layout.tt_custom_dialog, null);
                final EditText editText = (EditText) dialog_view.findViewById(R.id.dialog_edit_content);
                editText.setText(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER));
                TextView textText = (TextView) dialog_view.findViewById(R.id.dialog_title);
                textText.setText(R.string.switch_login_server_title);
                builder.setView(dialog_view);
                builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!TextUtils.isEmpty(editText.getText().toString().trim())) {
                            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, editText.getText().toString().trim());
                            dialog.dismiss();
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

        mRegister = (TextView) findViewById(R.id.register);
        findPwd = (TextView) findViewById(R.id.find_pwd);
        mRegister.setOnClickListener(this);
        findPwd.setOnClickListener(this);
        mNameView = (EditText) findViewById(R.id.name);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {

                if (id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mLoginStatusView = findViewById(R.id.login_status);
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intputManager.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
                attemptLogin();
            }
        });
        initAutoLogin();
    }

    private void initAutoLogin() {
        logger.i("login#initAutoLogin");

        splashPage = findViewById(R.id.splash_page);
        loginPage = findViewById(R.id.login_page);
        autoLogin = shouldAutoLogin();

        splashPage.setVisibility(autoLogin ? View.VISIBLE : View.GONE);
        loginPage.setVisibility(autoLogin ? View.GONE : View.VISIBLE);

        loginPage.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (mPasswordView != null) {
                    intputManager.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
                }

                if (mNameView != null) {
                    intputManager.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
                }

                return false;
            }
        });

        if (autoLogin) {
            Animation splashAnimation = AnimationUtils.loadAnimation(this, R.anim.login_splash);
            if (splashAnimation == null) {
                logger.e("login#loadAnimation login_splash failed");
                return;
            }

            splashPage.startAnimation(splashAnimation);
        }
    }

    // 主动退出的时候， 这个地方会有值,更具pwd来判断
    private boolean shouldAutoLogin() {
        Intent intent = getIntent();
        if (intent != null) {
            boolean notAutoLogin = intent.getBooleanExtra(IntentConstant.KEY_LOGIN_NOT_AUTO, false);
            logger.i("login#notAutoLogin:%s", notAutoLogin);
            if (notAutoLogin) {
                return false;
            }
        }
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(LoginActivity.this);
        EventBus.getDefault().unregister(this);
        splashPage = null;
        loginPage = null;
    }


    public void attemptLogin() {
        String loginName = mNameView.getText().toString();
        String mPassword = mPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(mPassword)) {
            Toast.makeText(this, getString(R.string.error_pwd_required), Toast.LENGTH_SHORT).show();
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(loginName)) {
            Toast.makeText(this, getString(R.string.error_name_required), Toast.LENGTH_SHORT).show();
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            if (imService != null) {
//				boolean userNameChanged = true;
//				boolean pwdChanged = true;

                //loginName = loginName.trim();
                //mPassword = mPassword.trim();

                XRandom xRandom = new XRandom(null);
                ECKey ecKey = ECKey.generateECKey(xRandom);
                ecKey = PrivateKeyUtil.encrypt(ecKey, new SecureCharSequence("654321"));

                //   loginInfo.setIdCard(ecKey.toAddress(), PrivateKeyUtil.getEncryptedString(ecKey), ecKey.getPubKey(), PrivateKeyUtil.getDerivedKey().getKey());

                IdCardEntity idCardEntity = new IdCardEntity(ecKey.toAddress(), PrivateKeyUtil.getEncryptedString(ecKey), Utils.bytesToHexString(ecKey.getPubKey()), Utils.bytesToHexString(PrivateKeyUtil.getDerivedKey().getKey()));
                imService.getLoginManager().setIdCardEntity(idCardEntity);

                loginName = ecKey.toAddress();
                mPassword = Utils.bytesToHexString(ecKey.getPubKey());

                imService.getLoginManager().login(loginName, mPassword);
            }
        }
    }

    private void showProgress(final boolean show) {
        if (show) {
            mLoginStatusView.setVisibility(View.VISIBLE);
        } else {
            mLoginStatusView.setVisibility(View.GONE);
        }
    }

    // 为什么会有两个这个
    // 可能是 兼容性的问题 导致两种方法onBackPressed
    @Override
    public void onBackPressed() {
        logger.i("login#onBackPressed");
        //imLoginMgr.cancel();
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//            LoginActivity.this.finish();
//            return true;
//        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * ----------------------------event 事件驱动----------------------------
     */
    public void onEventMainThread(LoginEvent event) {
        switch (event) {
            case LOCAL_LOGIN_SUCCESS:
            case LOGIN_OK:
                onLoginSuccess();
                break;
            case LOGIN_AUTH_FAILED:
            case LOGIN_INNER_FAILED:
                if (!loginSuccess)
                    onLoginFailure(event);
                break;
        }
    }


    public void onEventMainThread(SocketEvent event) {
        switch (event) {
            case CONNECT_MSG_SERVER_FAILED:
            case REQ_MSG_SERVER_ADDRS_FAILED:
                if (!loginSuccess)
                    onSocketFailure(event);
                break;
        }
    }

    private void onLoginSuccess() {
        logger.i("login#onLoginSuccess");

        //modified by neil
        //Utils.checkAndUpLoadPicture(this);//上传用户默认的一张小图

        //登录成功，请求离线添加好友请求
        getUnreadFriendRequstCnt();
        loginSuccess = true;
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        LoginActivity.this.finish();
    }


    private void getUnreadFriendRequstCnt() {
        IMSystem.IMSysMsgUnreadCntReq msg = IMSystem.IMSysMsgUnreadCntReq.newBuilder()
                .setUserId(IMLoginManager.instance().getPub_key())
                .build();
        int sid = IMBaseDefine.ServiceID.SID_SYS_MSG_VALUE;
        int cid = IMBaseDefine.SysMsgCmdID.CID_SYS_MSG_GET_UNREAD_CNT_REQUEST_VALUE;
        IMSocketManager.instance().sendRequest(msg,sid,cid);
    }

    private void onLoginFailure(LoginEvent event) {
        showLoginPage();
        String errorTip = getString(IMUIHelper.getLoginErrorTip(event));
        mLoginStatusView.setVisibility(View.GONE);
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
    }

    private void onSocketFailure(SocketEvent event) {
        logger.e("login#onLoginError -> errorCode:%s,", event.name());
        showLoginPage();
        String errorTip = getString(IMUIHelper.getSocketErrorTip(event));
        logger.i("login#errorTip:%s", errorTip);
        mLoginStatusView.setVisibility(View.GONE);
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity1.class);
        switch (v.getId()){
            case R.id.register:
                intent.putExtra("isFindPwd",false);
                break;
            case R.id.find_pwd:
                intent.putExtra("isFindPwd",true);
                break;
        }
        startActivity(intent);
    }
}
