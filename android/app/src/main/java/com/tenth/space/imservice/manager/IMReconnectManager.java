package com.tenth.space.imservice.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import com.tenth.space.imservice.event.LoginEvent;
import com.tenth.space.imservice.event.ReconnectEvent;
import com.tenth.space.imservice.event.SocketEvent;
import com.tenth.space.utils.Logger;
import com.tenth.space.utils.NetworkUtil;

import de.greenrobot.event.EventBus;
/**
 * @modify yingmu
 * @Date 2014-12-28
 *
 * [修改重点]:
 *   重试的触发点是：1.网络链接的异常 2.请求消息服务器 3链接消息服务器
 */
public class IMReconnectManager extends IMManager {
    private Logger logger = Logger.getLogger(IMReconnectManager.class);

	private static IMReconnectManager inst = new IMReconnectManager();
    private PowerManager.WakeLock wakeLock;

    public static IMReconnectManager instance() {
       return inst;
    }

    /**重连所处的状态*/
    private volatile ReconnectEvent status = ReconnectEvent.NONE;

    private final int INIT_RECONNECT_INTERVAL_SECONDS = 3;
    private int reconnectInterval = INIT_RECONNECT_INTERVAL_SECONDS;
    private final int MAX_RECONNECT_INTERVAL_SECONDS = 60;

    private final int HANDLER_CHECK_NETWORK = 1;


    private volatile boolean isAlarmTrigger = false;

    /**
     * imService 服务建立的时候
     * 初始化所有的manager 调用onStartIMManager会调用下面的方法
     * eventBus 注册可以放在这里
     */
    @Override
    public void doOnStart() {
    }

    public void onNormalLoginOk(){
        onLocalLoginOk();
        status = ReconnectEvent.SUCCESS;
    }

    public void onLocalLoginOk(){
       // Log.i("GTAG","reconnect#LoginEvent onLocalLoginOk");

       if(!EventBus.getDefault().isRegistered(inst)){
           EventBus.getDefault().register(inst);
       }

       IntentFilter intentFilter = new IntentFilter();
       intentFilter.addAction(ACTION_RECONNECT);
       intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
       // Log.i("GTAG","reconnect#register actions");
       ctx.registerReceiver(imReceiver, intentFilter);
    }

    /** 网络链接成功*/
    public void onLocalNetOk(){
      //  Log.i("GTAG","reconnect#onLoginSuccess 网络链接上来,无需其他操作");
      status = ReconnectEvent.SUCCESS;
    }


    @Override
    public void reset() {
       // Log.i("GTAG","reconnect#reset begin");
        try {
            EventBus.getDefault().unregister(inst);
            ctx.unregisterReceiver(imReceiver);
            status = ReconnectEvent.NONE;
            isAlarmTrigger = false;
          //  Log.i("GTAG","reconnect#reset stop");
        }catch (Exception e){
           // Log.i("GTAG","reconnect#reset error:%s"+e.getCause());
        }finally {
            releaseWakeLock();
        }
    }

    /**
     * 网络socket状态监听
     * 并未登陆，请求链接消息服务器失败
     * @param socketEvent
     */
    public int time=2;
    public  long nextTime=2000L;
    public void onEventMainThread(SocketEvent socketEvent){
        Log.i("GTAG","reconnect#SocketEvent event:%s-->"+socketEvent.name());
        //MSG_SERVER_DISCONNECTED 没有必要重连吧。。
        switch (socketEvent){
            case MSG_SERVER_DISCONNECTED://心跳包链接超时
            case REQ_MSG_SERVER_ADDRS_FAILED://连接的时候，连接过程中连接超时了
            case CONNECT_MSG_SERVER_FAILED://自动重连失败，发送的
                Log.i("GTAG","心跳包连接超时");
                //此处做一个定时计数器
                Log.i("GTAG","time="+time+"nextTime="+nextTime);
                if (nextTime>128000){//2的九次方，时间差不多5分钟链接8次128000
                    EventBus.getDefault().post(LoginEvent.TIME_OUT);//发送弹出网络连接不可用，弹出dialog对话框
                    handler.removeMessages(HeatLive);
                    nextTime=2000L;
                    time=2;
                }else {
                    handler.sendEmptyMessageDelayed(HeatLive, nextTime);
                    nextTime= (long) (Math.pow(2,time)*1000L);
                    time+=1;
                }

                break;
            case CONNECT_MSG_SERVER_SUCCESS:
                //自动连接成功
                handler.removeMessages(HeatLive);
                nextTime=2000L;
                time=2;
                break;
        }
    }

    /**
     *
     * @param event
     */
    public void onEventMainThread(LoginEvent event){
        //Log.i("GTAG","reconnect#LoginEvent event: %s"+event.name());
        switch (event){
            case LOGIN_INNER_FAILED:
                //登录失败
                    tryReconnect();
                Log.i("GTAG","login_inner_failed");
                break;

            case LOCAL_LOGIN_MSG_SERVICE:
                //登录成功后
                resetReconnectTime();
                releaseWakeLock();
                break;
        }
    }

    private final int HeatLive=2;
    /**EventBus 没有找到延迟发送的*/
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case HANDLER_CHECK_NETWORK:{
                      if(!NetworkUtil.isNetWorkAvalible(ctx)){
                       //   Log.i("GTAG","reconnect#handleMessage#网络依旧不可用");
                          releaseWakeLock();
                          EventBus.getDefault().post(ReconnectEvent.DISABLE);
                          //网络重连之后任然是断开的，不可用
                          Log.i("GTAG","网络任然不可用");
                         EventBus.getDefault().post(LoginEvent.TIME_OUT);//发送弹出网络连接不可用，弹出dialog对话框
                      }
                }break;
                case HeatLive:
                    tryReconnect();
                    break;
            }
        }
    };

    private boolean isReconnecting(){
        SocketEvent socketEvent =   IMSocketManager.instance().getSocketStatus();
        LoginEvent loginEvent = IMLoginManager.instance().getLoginStatus();

        if(socketEvent.equals(SocketEvent.CONNECTING_MSG_SERVER)
                || socketEvent.equals(SocketEvent.REQING_MSG_SERVER_ADDRS)
                || loginEvent.equals(LoginEvent.LOGINING)){
            return true;
        }
        return false;
    }

    /**
     * 可能是网络环境切换
     * 可能是数据包异常
     * 启动快速重连机制
     */
    private  void tryReconnect(){
        /**检测网络状态*/
        ConnectivityManager nw = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = nw.getActiveNetworkInfo();
        if(netinfo == null){
            // 延迟检测
           // Log.i("GTAG","reconnect#netinfo 为空延迟检测");
            status = ReconnectEvent.DISABLE;
            handler.sendEmptyMessageDelayed(HANDLER_CHECK_NETWORK, 2000);
            return ;
        }

        synchronized(IMReconnectManager.this) {
            // 网络状态可用 当前状态处于重连的过程中 可能会死循环嘛
            if (netinfo.isAvailable()) {
                /**网络显示可用*/
                /**判断是否有必要重练*/
                if (status == ReconnectEvent.NONE
                        || !IMLoginManager.instance().isEverLogined()
                        || IMLoginManager.instance().isKickout()
                        || IMSocketManager.instance().isSocketConnect()
                        ) {
                   // Log.i("GTAG","reconnect#无需启动重连程序");
                    return;
                }
                if (isReconnecting()) {
                    /**升级时间，部署下一次的重练*/
                    Log.i("GTAG","reconnect#正在重连中..");
                    incrementReconnectInterval();
                    scheduleReconnect(reconnectInterval);
                   // Log.i("GTAG","reconnect#tryReconnect#下次重练时间间隔:%d"+ reconnectInterval);
                    return;
                }

                /**确保之前的链接已经关闭*/
                handleReconnectServer();
            } else {
                //通知上层UI修改
               // Log.i("GTAG","reconnect#网络不可用!! 通知上层");
                status = ReconnectEvent.DISABLE;
                EventBus.getDefault().postSticky(ReconnectEvent.DISABLE);
            }
        }
    }

    /**
     * 部署下次请求的时间
     * 为什么用这种方式，而不是handler?
     * @param seconds
     */
	private void scheduleReconnect(int seconds) {
       // Log.i("GTAG","reconnect#scheduleReconnect after %d seconds"+seconds);
		Intent intent = new Intent(ACTION_RECONNECT);
		PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, intent,PendingIntent.FLAG_CANCEL_CURRENT);
		if (pi == null) {
           //Log.i("GTAG","reconnect#pi is null");
			return;
		}
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + seconds
				* 1000, pi);//发送广播
	}

    /**
     * 在RECONNECT 的时候，时间加成处理
     */
    private void incrementReconnectInterval() {
		if (reconnectInterval >= MAX_RECONNECT_INTERVAL_SECONDS) {
			reconnectInterval = MAX_RECONNECT_INTERVAL_SECONDS;
		} else {
			reconnectInterval = reconnectInterval * 2;
		}
	}

    /**
     * 重新设定重连时间
     */
    private void resetReconnectTime() {
      //  Log.i("GTAG","reconnect#resetReconnectTime");
        reconnectInterval = INIT_RECONNECT_INTERVAL_SECONDS;
    }




    /**--------------------boradcast-广播相关-----------------------------*/
    public static final String  ACTION_RECONNECT = "com.mogujie.tt.imlib.action.reconnect";
    private BroadcastReceiver imReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
              //  Log.i("GTAG","reconnect#im#receive action:%s"+action);
                onAction(action, intent);
        }
    };

    /**
     * 【重要】这个地方作为重连判断的唯一标准
     *  飞行模式: 触发
     *  切换网络状态： 触发
     *  没有网络 ： 触发
     * */
    public void onAction(String action, Intent intent) {
       // Log.i("GTAG","reconnect#onAction action:%s"+action);
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
           // Log.i("GTAG","reconnect#onAction#网络状态发生变化!!");
            tryReconnect();
        } else if (action.equals(ACTION_RECONNECT)) {
            isAlarmTrigger = true;
            tryReconnect();
        }
    }

    /**
     * 设定的调度开始执行
     * todo 其他reconnect需要获取锁嘛
     */
    private void handleReconnectServer() {
       // Log.i("GTAG","reconnect#handleReconnectServer#定时任务触发");
        acquireWakeLock();
        IMSocketManager.instance().disconnectMsgServer();
        if (isAlarmTrigger) {
            isAlarmTrigger = false;
            logger.d("reconnect#定时器触发重连。。。");
            if(reconnectInterval > 24){
                // 重新请求msg地址
                IMLoginManager.instance().relogin();
            }else{
                IMSocketManager.instance().reconnectMsg();
            }
        } else {
            Log.i("GTAG","reconnect#正常重连，非定时器");
            IMSocketManager.instance().reconnectMsg();
        }
    }

    /**
     * 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
     * 由于重连流程中充满了异步操作,按照函数执行流判断加锁代码侵入性较大，而且还需要traceId跟踪（因为可能会并发）
     * 所以这个锁定义为时间锁，15s的重连容忍时间
     */
    private void acquireWakeLock() {
        try {
            if (null == wakeLock) {
                PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "teamtalk_reconnecting_wakelock");
               // Log.i("GTAG","acquireWakeLock#call acquireWakeLock");
                wakeLock.acquire(15000);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 释放设备电源锁
    private void releaseWakeLock() {
        try {
            if (null != wakeLock && wakeLock.isHeld()) {
              //  Log.i("GTAG","releaseWakeLock##call releaseWakeLock");
                wakeLock.release();
                wakeLock = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}