package com.tenth.space.imservice.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.tenth.space.DB.DBInterface;
import com.tenth.space.DB.entity.IdCardEntity;
import com.tenth.space.DB.entity.MessageEntity;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.DB.sp.ConfigurationSp;
import com.tenth.space.DB.sp.LoginSp;
import com.tenth.space.config.SysConstant;
import com.tenth.space.imservice.event.LoginEvent;
import com.tenth.space.imservice.event.PriorityEvent;
import com.tenth.space.imservice.manager.IMBlogManager;
import com.tenth.space.imservice.manager.IMContactManager;
import com.tenth.space.imservice.manager.IMGroupManager;
import com.tenth.space.imservice.manager.IMHeartBeatManager;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.imservice.manager.IMMessageManager;
import com.tenth.space.imservice.manager.IMNotificationManager;
import com.tenth.space.imservice.manager.IMReconnectManager;
import com.tenth.space.imservice.manager.IMSessionManager;
import com.tenth.space.imservice.manager.IMSocketManager;
import com.tenth.space.imservice.manager.IMUnreadMsgManager;
import com.tenth.space.utils.ImageLoaderUtil;
import com.tenth.space.utils.LogUtils;
import com.tenth.space.utils.Logger;
import com.tenth.space.utils.PrivateKeyUtil;
import com.tenth.space.utils.Utils;

import org.apache.http.util.TextUtils;

import de.greenrobot.event.EventBus;

/**
 * IMService 负责所有IMManager的初始化与reset
 * 并且Manager的状态的改变 也会影响到IMService的操作
 * 备注: 有些服务应该在LOGIN_OK 之后进行
 * todo IMManager reflect or just like  ctx.getSystemService()
 */
public class IMService extends Service {
    private Logger logger = Logger.getLogger(IMService.class);

    private IMServiceBinder binder = new IMServiceBinder();

    public class IMServiceBinder extends Binder {
        public IMService getService() {
            return IMService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        logger.i("IMService onBind");
        return binder;
    }

    //所有的管理类
    private IMSocketManager socketMgr = IMSocketManager.instance();
    private IMLoginManager loginMgr = IMLoginManager.instance();
    private IMContactManager contactMgr = IMContactManager.instance();
    private IMGroupManager groupMgr = IMGroupManager.instance();
    private IMMessageManager messageMgr = IMMessageManager.instance();
    private IMSessionManager sessionMgr = IMSessionManager.instance();
    private IMReconnectManager reconnectMgr = IMReconnectManager.instance();
    private IMUnreadMsgManager unReadMsgMgr = IMUnreadMsgManager.instance();
    private IMNotificationManager notificationMgr = IMNotificationManager.instance();
    private IMHeartBeatManager heartBeatManager = IMHeartBeatManager.instance();
    private IMBlogManager blogManager = IMBlogManager.instance();
    private ConfigurationSp configSp;
    private LoginSp loginSp = LoginSp.instance();
    private DBInterface dbInterface = DBInterface.instance();

    @Override
    public void onCreate() {
        logger.i("IMService onCreate");
        super.onCreate();
        EventBus.getDefault().register(this, SysConstant.SERVICE_EVENTBUS_PRIORITY);

        //modified by neil
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            // make the service foreground, so stop "360 yi jian qingli"(a clean
            // tool) to stop our app
            // todo eric study wechat's mechanism, use a better solution
            startForeground((int) System.currentTimeMillis(), new Notification());
    }

    @Override
    public void onDestroy() {
        logger.i("IMService onDestroy");
        // todo 在onCreate中使用startForeground
        // 在这个地方是否执行 stopForeground
        EventBus.getDefault().unregister(this);
        handleLoginout();
        // DB的资源的释放
        dbInterface.close();

        IMNotificationManager.instance().cancelAllNotifications();
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String CHANNEL_ID = "com.tenth.space";
        String CHANNEL_NAME = "10thspace";
        NotificationChannel notificationChannel = null;

        notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);

        //Intent intent = new Intent(this, MainActivity.class);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID).
                setContentTitle("This is content title").
                setContentText("This is content text").
                setWhen(System.currentTimeMillis()).build();
                //setSmallIcon(R.mipmap.ic_launcher).
                //setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher)).
                //setContentIntent(pendingIntent).build();
        startForeground(1, notification);
    }

    /**
     * 收到消息需要上层的activity判断 {MessageActicity onEvent(PriorityEvent event)}，这个地方是特殊分支
     */
    public void onEvent(PriorityEvent event) {
        switch (event.event) {
            case MSG_RECEIVED_MESSAGE: {
                MessageEntity entity = (MessageEntity) event.object;
                /**非当前的会话*/
                logger.i("messageactivity#not this session msg -> id:%s", entity.getFromId());
                messageMgr.ackReceiveMsg(entity);
                unReadMsgMgr.add(entity);
            }
            break;
        }
    }

    // EventBus 事件驱动
    public void onEvent(LoginEvent event) {
        switch (event) {
            case LOGIN_OK:
                onNormalLoginOk();
                break;
            case LOCAL_LOGIN_SUCCESS:
                onLocalLoginOk();
                break;
            case LOCAL_LOGIN_MSG_SERVICE:
                onLocalNetOk();
                break;
            case LOGIN_OUT:
                handleLoginout();
                break;
        }
    }

    // 负责初始化 每个manager
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.i("IMService onStartCommand");
        //应用开启初始化 下面这几个怎么释放 todo
        Context ctx = getApplicationContext();
        loginSp.init(ctx);
        // 放在这里还有些问题 todo
        socketMgr.onStartIMManager(ctx);
        loginMgr.onStartIMManager(ctx);
        contactMgr.onStartIMManager(ctx);
        messageMgr.onStartIMManager(ctx);
        groupMgr.onStartIMManager(ctx);
        sessionMgr.onStartIMManager(ctx);
        unReadMsgMgr.onStartIMManager(ctx);
        notificationMgr.onStartIMManager(ctx);
        reconnectMgr.onStartIMManager(ctx);
        heartBeatManager.onStartIMManager(ctx);
        blogManager.onStartIMManager(ctx);

        ImageLoaderUtil.initImageLoaderConfig(ctx);
        return START_STICKY;
    }


    /**
     * 用户输入登陆流程
     * userName/pwd -> reqMessage ->connect -> loginMessage ->loginSuccess
     */
    private void onNormalLoginOk() {
        LogUtils.d("IMSerVice-----onNormalLoginOk:执行开始");
        logger.i("imservice#onLogin Successful");
        //初始化其他manager todo 这个地方注意上下文的清除
        Context ctx = getApplicationContext();
        String loginId = loginMgr.getLoginId();
        configSp = ConfigurationSp.instance(ctx, loginId);
        dbInterface.initDbHelp(ctx, loginId);

        if(null!=loginMgr.getIdCardEntity())
            DBInterface.instance().insertOrUpdateIdCard(loginMgr.getIdCardEntity());
        else{
            IdCardEntity entity =  DBInterface.instance().getIdCardByAddress(loginMgr.getLoginUserName());
            loginMgr.setIdCardEntity(entity);
        }

        UserEntity logininfo = loginMgr.getLoginInfo();//wystan add for can not get all info from server 210204
        UserEntity local_UserInfo = DBInterface.instance().getByLoginId(loginId);
        if(null != local_UserInfo){
            if (TextUtils.isEmpty(logininfo.getAvatar()) && !TextUtils.isEmpty(local_UserInfo.getAvatar()))
                logininfo.setAvatar(local_UserInfo.getAvatar());
            if ((TextUtils.isEmpty(logininfo.getMainName()) || logininfo.getMainName().equals("xxx")) && !TextUtils.isEmpty(local_UserInfo.getMainName()))
                logininfo.setMainName(local_UserInfo.getMainName());
            if (TextUtils.isEmpty(logininfo.getSignature())  &&!TextUtils.isEmpty(local_UserInfo.getSignature()))
                logininfo.setSignature(local_UserInfo.getSignature());
            logininfo.setGender(local_UserInfo.getGender());
            loginMgr.setLoginInfo(logininfo);
        }


        contactMgr.onNormalLoginOk();
        sessionMgr.onNormalLoginOk();
        groupMgr.onNormalLoginOk();
        unReadMsgMgr.onNormalLoginOk();
        reconnectMgr.onNormalLoginOk();
        //依赖的状态比较特殊
        messageMgr.onLoginSuccess();
        notificationMgr.onLoginSuccess();
        heartBeatManager.onloginNetSuccess();
        // 这个时候loginManage中的localLogin 被置为true

       //blogManager.reqBlogList(IMBaseDefine.BlogType.BLOG_TYPE_RCOMMEND,1);
       //blogManager.reqBlogList(IMBaseDefine.BlogType.BLOG_TYPE_FRIEND,1);
     // blogManager.reqBlogList(IMBaseDefine.BlogType.BLOG_TYPE_FOLLOWUSER,1);

        LogUtils.d("IMSerVice-----onNormalLoginOk:执行完成");
    }


    /**
     * 自动登陆/离线登陆成功
     * autoLogin -> DB(loginInfo,loginId...) -> loginSucsess
     */
    private void onLocalLoginOk() {
        LogUtils.d("IMSerVice-----onLocalLoginOk:执行开始");
        Context ctx = getApplicationContext();
        String loginId = loginMgr.getLoginId();

        configSp = ConfigurationSp.instance(ctx, loginId);
        dbInterface.initDbHelp(ctx, loginId);

        contactMgr.onLocalLoginOk();
        groupMgr.onLocalLoginOk();
        sessionMgr.onLocalLoginOk();
        reconnectMgr.onLocalLoginOk();
        notificationMgr.onLoginSuccess();
        messageMgr.onLoginSuccess();

       // blogManager.reqBlogList();
       // blogManager.reqBlogList(IMBaseDefine.BlogType.BLOG_TYPE_RCOMMEND,1);
       // blogManager.reqBlogList(IMBaseDefine.BlogType.BLOG_TYPE_FRIEND,1);
       // blogManager.reqBlogList(IMBaseDefine.BlogType.BLOG_TYPE_FOLLOWUSER,1);
        LogUtils.d("IMSerVice-----onLocalLoginOk:执行完成");
    }

    /**
     * 1.从本机加载成功之后，请求MessageService建立链接成功(loginMessageSuccess)
     * 2. 重连成功之后
     */
    private void onLocalNetOk() {
        /**为了防止逗比直接把loginId与userName的对应直接改了,重刷一遍*/
        LogUtils.d("IMSerVice-----onLocalNetOk:执行开始");
        Context ctx = getApplicationContext();
        String loginId = loginMgr.getLoginId();
        configSp = ConfigurationSp.instance(ctx, loginId);
        dbInterface.initDbHelp(ctx, loginId);

        contactMgr.onLocalNetOk();
        groupMgr.onLocalNetOk();
        sessionMgr.onLocalNetOk();
        unReadMsgMgr.onLocalNetOk();
        reconnectMgr.onLocalNetOk();
        heartBeatManager.onloginNetSuccess();

        //blogManager.reqBlogList();
       // blogManager.reqBlogList(IMBaseDefine.BlogType.BLOG_TYPE_RCOMMEND,1);
       // blogManager.reqBlogList(IMBaseDefine.BlogType.BLOG_TYPE_FRIEND,1);
      //  blogManager.reqBlogList(IMBaseDefine.BlogType.BLOG_TYPE_FOLLOWUSER,1);
        LogUtils.d("IMSerVice-----onLocalNetOk:执行完成");
    }

    private void handleLoginout() {
        logger.i("imservice#handleLoginout");

        // login需要监听socket的变化,在这个地方不能释放，设计上的不合理?
        socketMgr.reset();
        loginMgr.reset();
        contactMgr.reset();
        messageMgr.reset();
        groupMgr.reset();
        sessionMgr.reset();
        unReadMsgMgr.reset();
        notificationMgr.reset();
        reconnectMgr.reset();
        heartBeatManager.reset();
        configSp = null;
        EventBus.getDefault().removeAllStickyEvents();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        logger.i("imservice#onTaskRemoved");
        // super.onTaskRemoved(rootIntent);
        this.stopSelf();
    }

    /**
     * -----------------get/set 的实体定义---------------------
     */
    public IMLoginManager getLoginManager() {
        return loginMgr;
    }

    public IMContactManager getContactManager() {
        return contactMgr;
    }

    public IMMessageManager getMessageManager() {
        return messageMgr;
    }


    public IMGroupManager getGroupManager() {
        return groupMgr;
    }

    public IMSessionManager getSessionManager() {
        return sessionMgr;
    }

    public IMReconnectManager getReconnectManager() {
        return reconnectMgr;
    }


    public IMUnreadMsgManager getUnReadMsgManager() {
        return unReadMsgMgr;
    }

    public IMNotificationManager getNotificationManager() {
        return notificationMgr;
    }

    public DBInterface getDbInterface() {
        return dbInterface;
    }

    public ConfigurationSp getConfigSp() {
        return configSp;
    }

    public LoginSp getLoginSp() {
        return loginSp;
    }

    public IMBlogManager getBlogManager() {
        return blogManager;
    }

}
