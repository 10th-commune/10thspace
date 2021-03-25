package com.tenth.space.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.dialog.NumberProgressBar;
import com.azhon.appupdate.listener.OnButtonClickListener;
import com.azhon.appupdate.listener.OnDownloadListenerAdapter;
import com.azhon.appupdate.manager.DownloadManager;
import com.tenth.space.BitherjSettings;
import com.tenth.space.DB.entity.IdCardEntity;
import com.tenth.space.DB.sp.LoginSp;
import com.tenth.space.DB.sp.SystemConfigSp;
import com.tenth.space.R;
import com.tenth.space.config.SysConstant;
import com.tenth.space.config.UrlConstant;
import com.tenth.space.imservice.event.LoginEvent;
import com.tenth.space.imservice.event.SocketEvent;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.imservice.service.IMService;
import com.tenth.space.imservice.support.IMServiceConnector;
import com.tenth.space.ui.base.TTBaseActivity;
import com.tenth.space.utils.FileUtil;
import com.tenth.space.utils.IMUIHelper;
import com.tenth.space.utils.LogUtils;
import com.tenth.space.utils.Logger;
import com.tenth.space.utils.ToastUtils;
import com.tenth.space.utils.runnable.GetNewIdCardRunnable;
import com.tenth.space.utils.runnable.HandlerMessage;
import com.tenth.space.utils.upgrade.UpgradeManager;

import java.io.File;

import de.greenrobot.event.EventBus;

public class EntryActivity extends TTBaseActivity implements UpgradeManager.UpgradeListener {
    private Logger logger = Logger.getLogger(EntryActivity.class);
    private Handler uiHandler = new Handler();

    private IdCardEntity idCardEntity;

    private View createPage;
    private View startingPage;
    private View loginingPage;
    private View updatingPage;
    private NumberProgressBar updatingProgressBar;
    private DownloadManager manager;
    private EditText etReferralCode;
    private UpgradeManager mUpgradeManager;

    private IMService imService;
    private boolean autoLogin = true;
    private boolean loginSuccess = false;
    private boolean updateCancel = false;
    private String referralCode = "";

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
                        LogUtils.e("EntryActivity------启动服务失败");
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
                        autoLogin = false;
                        break;
                    }

                 //   if (autoLogin == false) {
               //        break;
                //    }

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUpgradeManager = new UpgradeManager(EntryActivity.this,EntryActivity.this);

        initView();

        SystemConfigSp.instance().init(getApplicationContext());
        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, UrlConstant.ACCESS_MSG_ADDRESS);
        }
        imServiceConnector.connect(EntryActivity.this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(EntryActivity.this);
        EventBus.getDefault().unregister(this);
        startingPage = null;
        loginingPage = null;
        createPage = null;
    }

    public void initView(){
        setContentView(R.layout.activity_entry);
        startingPage = findViewById(R.id.starting_page);
        loginingPage = findViewById(R.id.logining_page);
        createPage = findViewById(R.id.createPage);
        updatingPage = findViewById(R.id.updating_page);
        updatingProgressBar = findViewById(R.id.number_progress_bar);
        etReferralCode = findViewById(R.id.etReferralCode);
        showStartingPage();
        findViewById(R.id.createnew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewId();
            }
        });
        findViewById(R.id.importnew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                importIdFromPhoto();
            }
        });
        findViewById(R.id.importRefferalCode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewIdWithRid();
                //importIdFromPhoto();
            }
        });
    }

    public void createNewId(){
        logger.i("wystan start createNewId");
        showLoginingPage();

        GetNewIdCardRunnable createIdRunable = new GetNewIdCardRunnable(completeCreateHandler);//new Thread(rechargeRun).start();
        Thread thread = new Thread(createIdRunable);
        thread.start();
    }

    public void createNewIdWithRid(){
        Intent intent = new Intent(EntryActivity.this, ScanActivity.class);//TransferAmountActivity  //ScanActivity
        //activity.startActivity(intent);
        startActivityForResult(intent, BitherjSettings.INTENT_REF.SCAN_REQUEST_CODE);
    }

    public void importIdFromPhoto(){
        /*
        Intent intent = new Intent(EntryActivity.this, PickPhotoActivity.class);
        intent.putExtra(IntentConstant.KEY_SESSION_KEY, "currentSessionKey");
        startActivityForResult(intent, SysConstant.ALBUM_BACK_DATA);

        EntryActivity.this.overridePendingTransition(R.anim.tt_album_enter, R.anim.tt_stay);

         */

        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
       // intent.addCategory(Intent.CATEGORY_OPENABLE);
        //intent.setType("image/*");

        if(checkPermission()){
            logger.i("wystan importIdFromPhoto");
            realImport();
        }
    }

    public void realImport(){
        logger.i("wystan realImport");
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
        startActivityForResult(intent, SysConstant.ALBUM_BACK_DATA);
    }



    public void login(){
        logger.i("wystan start login");
        showLoginingPage();
        //referralCode = etReferralCode.getText().toString();
        imService.getLoginManager().setIdCardEntity(idCardEntity);
        imService.getLoginManager().login(idCardEntity.getAddress(), idCardEntity.getPubKey(), referralCode);//idCardEntity.getAddress()
    }

    private void handleGotLoginIdentity(final LoginSp.SpLoginIdentity loginIdentity) {
        logger.i("login#handleGotLoginIdentity");
        showLoginingPage();
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                logger.i("login#start auto login");
                if (imService == null || imService.getLoginManager() == null) {
                    Toast.makeText(EntryActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                    showCreatePage();
                }
                imService.getLoginManager().login(loginIdentity);
            }
        }, 500);
    }

    private void handleNoLoginIdentity() {
        logger.i("login#handleNoLoginIdentity");
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showCreatePage();
            }
        }, 1000);
    }

    private void showStartingPage() {
        startingPage.setVisibility(View.VISIBLE);
        loginingPage.setVisibility(View.GONE);
        createPage.setVisibility(View.GONE);
        updatingPage.setVisibility(View.GONE);

        if(!updateCancel)
            ;//upgrade();
    }

    private void showCreatePage() {
        if(!autoLogin) {
            startingPage.setVisibility(View.GONE);
            loginingPage.setVisibility(View.GONE);
            updatingPage.setVisibility(View.GONE);
            createPage.setVisibility(View.VISIBLE);
        }
    }

    private void showLoginingPage() {
        startingPage.setVisibility(View.GONE);
        updatingPage.setVisibility(View.GONE);
        loginingPage.setVisibility(View.VISIBLE);
        createPage.setVisibility(View.GONE);
    }

    private void showUpdatingPage() {
        startingPage.setVisibility(View.GONE);
        loginingPage.setVisibility(View.GONE);
        createPage.setVisibility(View.GONE);
        updatingPage.setVisibility(View.VISIBLE);
    }



    public void onEventMainThread(LoginEvent event) {
        switch (event) {
            case LOCAL_LOGIN_SUCCESS:
            case LOGIN_OK:
                onLoginSuccess();
                break;
            case LOGIN_AUTH_FAILED:
            case LOGIN_INNER_FAILED:
                // if (!loginSuccess)
                //  onLoginSuccess();//
                  onLoginFailure(event);//wystan modify for login no server 201027
                break;
            case LOGIN_OUT:
                handleOnLogout();
                break;
        }
    }


    public void onEventMainThread(SocketEvent event) {
        switch (event) {
            case CONNECT_MSG_SERVER_FAILED:
            case REQ_MSG_SERVER_ADDRS_FAILED:
                //  if (!loginSuccess)
                     onSocketFailure(event);
                break;
            case REQ_MSG_SERVER_ADDRS_SUCCESS:
                String upgradeUrl = SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.UPGRADESERVER);
                if (!TextUtils.isEmpty(upgradeUrl)) {
                    upgrade(upgradeUrl);
                }
                break;
            default:
                break;
        }
    }

    private void onLoginSuccess() {
        logger.i("wystan onLoginSuccess");

        loginSuccess = true;

        Intent intent = new Intent(EntryActivity.this, MainActivity.class);
        startActivity(intent);
        EntryActivity.this.finish();
    }

    private void onLoginFailure(LoginEvent event) {
        //showCreatePage();
        Intent intent = new Intent(this, DialogActivity.class);
        startActivity(intent);
        String errorTip = getString(IMUIHelper.getLoginErrorTip(event));
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
    }


    private void handleOnLogout() {
        logger.i("EntryActivity#login#handleOnLogout");
        finish();
        logger.i("EntryActivity#login#kill self, and start login activity");
        // jumpToLoginPage(); //wystan modify for just exit app 210224
    }

    private void onSocketFailure(SocketEvent event) {
        showCreatePage();
        String errorTip = getString(IMUIHelper.getSocketErrorTip(event));
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
    }

    private Handler completeCreateHandler = new Handler() {
        public void handleMessage(Message msg) {
            //Intent intent = getIntent();
            switch (msg.what) {
                case HandlerMessage.MSG_CREATEID_SUCCESS:
                    //setResult(RESULT_OK, intent);
                    idCardEntity = (IdCardEntity)msg.obj;
                    if (imService != null) {
                        login();
                    }
                    else{
                        showCreatePage();
                    }
                    ToastUtils.show(BitherjSettings.accountCrateOK);
                    break;
                case HandlerMessage.MSG_IMPORTID_SUCCESS:
                    idCardEntity = (IdCardEntity)msg.obj;
                    if (imService != null) {
                        login();
                    }
                    else{
                        showCreatePage();
                    }
                    ToastUtils.show(BitherjSettings.accountImportOk);
                    break;
                case HandlerMessage.MSG_CREATEID_FAILURE:
                    showCreatePage();
                    ToastUtils.show(BitherjSettings.accountCrateFail);
                    break;
                case HandlerMessage.MSG_IMPORTID_FAILURE:
                    showCreatePage();
                    ToastUtils.show(BitherjSettings.accountCreateFailWithRid);
                    break;
                case HandlerMessage.MSG_REFERRAL_DECODE_SUCCESS:
                    showLoginingPage();
                    referralCode = (String)msg.obj;
                    logger.i("wystan HandlerMessage.MSG_REFERRAL_DECODE_SUCCESS %s", referralCode);
                    break;
                case HandlerMessage.MSG_REFERRAL_DECODE_FAILURE:
                    showCreatePage();
                    ToastUtils.show(BitherjSettings.accountCreateFailWithRid);
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode,final Intent data) {
        if(requestCode == SysConstant.ALBUM_BACK_DATA && resultCode == RESULT_OK) {
            logger.i("wystan EntryActivity onActivityResult:%s", data.getData());

            Uri uri = data.getData();
            if (uri != null) {
                File picFile = FileUtil.convertUriToFile(EntryActivity.this, uri);
                if (picFile != null && picFile.exists()) {
                    GetNewIdCardRunnable createIdRunable = new GetNewIdCardRunnable(picFile, completeCreateHandler);//new Thread(rechargeRun).start();
                    Thread thread = new Thread(createIdRunable);
                    thread.start();
                }
                else{
                    ToastUtils.show(BitherjSettings.accountImportFail);
                }
            }
            else{
                ToastUtils.show(BitherjSettings.accountImportFail);
            }

        }
        else if (requestCode == BitherjSettings.INTENT_REF.SCAN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            final String qrResult = data.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
            logger.i("wystan EntryActivity onActivityResult:%s", qrResult);
            if (null != qrResult) {
                if(qrResult.contains("http://")&&qrResult.contains("rid=")) {//create with referral_code
                    referralCode = qrResult.substring(qrResult.lastIndexOf("rid=") + 4);
                    createNewId();
                }
                else{
                    ToastUtils.show(BitherjSettings.accountCreateFailWithRid);
                }
            }
            else{

                Uri uri = data.getData();
                if (uri != null) {
                    File picFile = FileUtil.convertUriToFile(EntryActivity.this, uri);
                    if (picFile != null && picFile.exists()) {
                        GetNewIdCardRunnable createIdRunable = new GetNewIdCardRunnable(picFile, completeCreateHandler);//new Thread(rechargeRun).start();
                        Thread thread = new Thread(createIdRunable);
                        thread.start();
                    }
                    else{
                        ToastUtils.show(BitherjSettings.accountImportFail);
                    }
                }
                else{
                    ToastUtils.show(BitherjSettings.accountImportFail);
                }

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean checkPermission(){
        int permission = ActivityCompat.checkSelfPermission(this,
                BitherjSettings.STORAGE_PERMISSIONS[BitherjSettings.REQUEST_EXTERNAL_STORAGE_READ]);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, BitherjSettings.STORAGE_PERMISSIONS, BitherjSettings.PERMISSION_REQUEST_CODE_READ);// requestCode Application specific request code to match with a result
            return  false;
        }

        return true;
    }


    public void upgrade(String strUpgradeUrl){
        mUpgradeManager.start(strUpgradeUrl);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == BitherjSettings.PERMISSION_REQUEST_CODE_READ) {
            for (int i = 0; i < permissions.length; i++) {
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    realImport();
                    break;
                } else {
                    Toast.makeText(this, "PERMISSION" + permissions[i] + "DENIED!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    public void doUpgrade() {
        updatingProgressBar.setProgress(0);
        showUpdatingPage();
    }

    @Override
    public void cancelUpgrade() {
        updateCancel = true;
        imService.getLoginManager().doLoginMsgServer();
        //showStartingPage();
        //imServiceConnector.connect(EntryActivity.this);
    }

    @Override
    public void downloading(int max, int progress){
        int curr = (int) (progress / (double) max * 100.0);
        updatingProgressBar.setMax(100);
        updatingProgressBar.setProgress(curr);
    }

    @Override
    public void  error(Exception e){
        Toast.makeText(this, R.string.download_err, Toast.LENGTH_SHORT).show();
    }
}

