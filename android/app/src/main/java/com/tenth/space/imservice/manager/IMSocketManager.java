package com.tenth.space.imservice.manager;

import android.text.TextUtils;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessageLite;
import com.loopj.android.http.AsyncHttpClient;
import com.tenth.space.DB.sp.SystemConfigSp;
import com.tenth.space.config.SysConstant;
import com.tenth.space.imservice.callback.ListenerQueue;
import com.tenth.space.imservice.callback.Packetlistener;
import com.tenth.space.imservice.event.SocketEvent;
import com.tenth.space.imservice.network.MsgServerHandler;
import com.tenth.space.imservice.network.SocketThread;
import com.tenth.space.protobuf.IMBaseDefine;
import com.tenth.space.protobuf.base.DataBuffer;
import com.tenth.space.protobuf.base.DefaultHeader;
import com.tenth.space.utils.LogUtils;
import com.tenth.space.utils.Logger;
import com.tenth.space.utils.NetworkUtil;
import com.tenth.space.utils.ToastUtils;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * @author : yingmu on 14-12-30.
 * @email : yingmu@mogujie.com.
 *
 * 业务层面:
 * 长连接建立成功之后，就要发送登陆信息，否则15s之内就会断开
 * 所以connMsg 与 login是强耦合的关系
 */
public class IMSocketManager extends IMManager {

    private Logger logger = Logger.getLogger(IMSocketManager.class);
    private static IMSocketManager inst = new IMSocketManager();

    public static IMSocketManager instance() {
        return inst;
    }

    public IMSocketManager() {
        logger.i("login#creating IMSocketManager");
    }

    private ListenerQueue listenerQueue = ListenerQueue.instance();

    // 请求消息服务器地址
    private AsyncHttpClient client = new AsyncHttpClient();

    /**
     * 底层socket
     */
    private SocketThread msgServerThread;

    /**
     * 快速重新连接的时候需要
     */
    private MsgServerAddrsEntity currentMsgAddress = null;

    /**
     * 自身状态
     */
    private SocketEvent socketStatus = SocketEvent.NONE;

    /**
     * 获取Msg地址，等待链接
     */
    @Override
    public void doOnStart() {
        socketStatus = SocketEvent.NONE;
    }


    //todo check
    @Override
    public void reset() {
        disconnectMsgServer();
        socketStatus = SocketEvent.NONE;
        currentMsgAddress = null;
    }

    /**
     * 实现自身的事件驱动
     *
     * @param event
     */
    public void triggerEvent(SocketEvent event) {
        setSocketStatus(event);
        EventBus.getDefault().postSticky(event);
    }

    /**
     * -------------------------------功能方法--------------------------------------
     */

    public void sendRequest(GeneratedMessageLite requset, int sid, int cid) {
        sendRequest(requset, sid, cid, null);
    }


    /**
     * todo check exception
     */
    public void sendRequest(GeneratedMessageLite requset, int sid, int cid, Packetlistener packetlistener) {
        int seqNo = 0;
        try {
            //组装包头 header
            if(cid == 769){
                LogUtils.e("de");
            }
            com.tenth.space.protobuf.base.Header header = new DefaultHeader(sid, cid);
            int bodySize = requset.getSerializedSize();
            header.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + bodySize);
            seqNo = header.getSeqnum();
            listenerQueue.push(seqNo, packetlistener);
            /**
             * wsq注：
             *
             * 注这里的请求，会放到socketThread中执行，而socketThread是在login成功后建立起来的
             */
        if (msgServerThread!=null){
            boolean sendRes = msgServerThread.sendRequest(requset, header);
        }

        } catch (Exception e) {
            if (packetlistener != null) {
                packetlistener.onFaild();
            }
            listenerQueue.pop(seqNo);
            logger.e("#sendRequest#channel is close!");
            //logger.e(e);
            e.printStackTrace();
        }
    }

    public void packetDispatch(ChannelBuffer channelBuffer) {
        DataBuffer buffer = new DataBuffer(channelBuffer);
        com.tenth.space.protobuf.base.Header header = new com.tenth.space.protobuf.base.Header();
        header.decode(buffer);
        /**buffer 的指针位于body的地方*/
        int commandId = header.getCommandId();
        int serviceId = header.getServiceId();
        int seqNo = header.getSeqnum();
        logger.i("dispatch packet, serviceId:%d, commandId:%d", serviceId, commandId);
        LogUtils.d("dispatch packet, serviceId:" + serviceId + ", commandId:" + commandId);
        CodedInputStream codedInputStream = CodedInputStream.newInstance(new ChannelBufferInputStream(buffer.getOrignalBuffer()));

        Packetlistener listener = listenerQueue.pop(seqNo);
        if (listener != null) {
            listener.onSuccess(codedInputStream);
            return;
        }

        // todo eric make it a table
        // 抽象 父类执行
        switch (serviceId) {
            case IMBaseDefine.ServiceID.SID_LOGIN_VALUE:
                IMPacketDispatcher.loginPacketDispatcher(commandId, codedInputStream);
                break;
            case IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE:
                IMPacketDispatcher.buddyPacketDispatcher(commandId, codedInputStream);
                break;
            case IMBaseDefine.ServiceID.SID_MSG_VALUE:
                IMPacketDispatcher.msgPacketDispatcher(commandId, codedInputStream);
                break;
            case IMBaseDefine.ServiceID.SID_GROUP_VALUE:
                IMPacketDispatcher.groupPacketDispatcher(commandId, codedInputStream);
                break;
//            case IMBaseDefine.ServiceID.SID_SYSTEM_VALUE:
//               // IMPacketDispatcher.onLineorDownLineDispatcher(commandId, codedInputStream);
//                break;
            case IMBaseDefine.ServiceID.SID_SYS_MSG_VALUE:
                try {
                    IMPacketDispatcher.systemPacketDispatcher(commandId, codedInputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case IMBaseDefine.ServiceID.SID_BLOG_VALUE:
                IMPacketDispatcher.onBlogDispatcher(commandId, codedInputStream);
                break;
            default:
                logger.e("packet#unhandled serviceId:%d, commandId:%d", serviceId,
                        commandId);
                break;
        }
    }


    /**
     * 新版本流程如下
     * 1.客户端通过域名获得login_server的地址
     * 2.客户端通过login_server获得msg_serv的地址
     * 3.客户端带着用户名密码对msg_serv进行登录
     * 4.msg_serv转给db_proxy进行认证（do not care on client）
     * 5.将认证结果返回给客户端
     */
    public void reqMsgServerAddrs(String server) {
        LogUtils.d("reqMsgServerAddrs");
        if(! NetworkUtil.isNetWorkAvalible(ctx)){
            triggerEvent(SocketEvent.REQ_MSG_SERVER_ADDRS_FAILED);
        }else {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(server);
                MsgServerAddrsEntity msgServer = onRepLoginServerAddrs(jsonObject);
                this.currentMsgAddress = msgServer;
                //connectMsgServer(msgServer);
                triggerEvent(SocketEvent.REQ_MSG_SERVER_ADDRS_SUCCESS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

//        logger.i("socket#reqMsgServerAddrs.");
//        client.setUserAgent("Android-TT");
//        client.get(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER), new BaseJsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int i, Header[] headers, String s, Object o) {
//                logger.i("socket#req msgAddress onSuccess, response:%s", s);
//                MsgServerAddrsEntity msgServer = (MsgServerAddrsEntity) o;
//                if (msgServer == null) {
//                    triggerEvent(SocketEvent.REQ_MSG_SERVER_ADDRS_FAILED);
//                    return;
//                }
//                connectMsgServer(msgServer);
//                triggerEvent(SocketEvent.REQ_MSG_SERVER_ADDRS_SUCCESS);
//            }
//
//            @Override
//            public void onFailure(int i, Header[] headers, Throwable throwable, String responseString, Object o) {
//                logger.i("socket#req msgAddress Failure, errorResponse:%s", responseString);
//                triggerEvent(SocketEvent.REQ_MSG_SERVER_ADDRS_FAILED);
//            }
//
//            @Override
//            protected Object parseResponse(String s, boolean b) throws Throwable {
//                /*子类需要提供实现，将请求结果解析成需要的类型 异常怎么处理*/
//                JSONObject jsonObject = new JSONObject(s);
//                MsgServerAddrsEntity msgServerAddrsEntity = onRepLoginServerAddrs(jsonObject);
//                return msgServerAddrsEntity;
//            }
//        });
    }

    /**
     * 与登陆login是强耦合的关系
     *
     * wsq注：此处是登陆成功后，建立起一个服务，服务中起了一个长连接线程
     */
    private void connectMsgServer(MsgServerAddrsEntity currentMsgAddress) {
        triggerEvent(SocketEvent.CONNECTING_MSG_SERVER);
        if(null == currentMsgAddress){
            ToastUtils.show("服务器连接失败");
            return;
        }

        this.currentMsgAddress = currentMsgAddress;

        String priorIP = currentMsgAddress.priorIP;
        int port = currentMsgAddress.port;
        logger.i("login#connectMsgServer -> (%s:%d)", priorIP, port);

        //check again,may be unimportance
        if (msgServerThread != null) {
            msgServerThread.close();
            msgServerThread = null;
        }

        msgServerThread = new SocketThread(priorIP, port, new MsgServerHandler());
        msgServerThread.start();
    }
    public void reconnectMsg() {
        synchronized (IMSocketManager.class) {
            if (currentMsgAddress != null) {
                connectMsgServer(currentMsgAddress);
            } else {
                disconnectMsgServer();
                IMLoginManager.instance().relogin();
            }
        }
    }

    /**
     * 断开与msg的链接
     */
    public void disconnectMsgServer() {
        listenerQueue.onDestory();
        logger.i("login#disconnectMsgServer");
        if (msgServerThread != null) {
            msgServerThread.close();
            msgServerThread = null;
            logger.i("login#do real disconnectMsgServer ok");
        }
    }

    /**
     * 判断链接是否处于断开状态
     */
    public boolean isSocketConnect() {
        if (msgServerThread == null || msgServerThread.isClose()) {
            return false;
        }
        return true;
    }

    public void onMsgServerConnected() {
        logger.i("login#onMsgServerConnected");
        listenerQueue.onStart();
        triggerEvent(SocketEvent.CONNECT_MSG_SERVER_SUCCESS);
        IMLoginManager.instance().reqLoginMsgServer();
    }

    /**
     * 1. kickout 被踢出会触发这个状态   -- 不需要重连
     * 2. 心跳包没有收到 会触发这个状态   -- 链接断开，重连
     * 3. 链接主动断开                 -- 重连
     * 之前的长连接状态 connected
     */
    // 先断开链接
    // only 2 threads(ui thread, network thread) would request sending  packet
    // let the ui thread to close the connection
    // so if the ui thread has a sending task, no synchronization issue
    public void onMsgServerDisconn() {
        logger.w("login#onMsgServerDisconn");
        disconnectMsgServer();
        triggerEvent(SocketEvent.MSG_SERVER_DISCONNECTED);
    }

    /**
     * 之前没有连接成功
     */
    public void onConnectMsgServerFail() {
        triggerEvent(SocketEvent.CONNECT_MSG_SERVER_FAILED);
    }


    /**----------------------------请求Msg server地址--实体信息--------------------------------------*/
    /**
     * 请求返回的数据
     */
    private class MsgServerAddrsEntity {
        int code;
        String msg;
        String priorIP;
        String backupIP;
        int port;

        @Override
        public String toString() {
            return "LoginServerAddrsEntity{" +
                    "code=" + code +
                    ", msg='" + msg + '\'' +
                    ", priorIP='" + priorIP + '\'' +
                    ", backupIP='" + backupIP + '\'' +
                    ", port=" + port +
                    '}';
        }
    }

    private MsgServerAddrsEntity onRepLoginServerAddrs(JSONObject json)
            throws JSONException {

        logger.i("login#onRepLoginServerAddrs");

        if (json == null) {
            logger.e("login#json is null");
            return null;
        }

        logger.i("login#onRepLoginServerAddrs json:%s", json);

        int code = json.getInt("code");
        if (code != 0) {
            logger.e("login#code is not right:%d, json:%s", code, json);
            return null;
        }

        String priorIP = json.getString("priorIP");
        String backupIP = json.getString("backupIP");
        int port = json.getInt("port");

        if (json.has("msfsPrior")) {
            String msfsPrior = json.getString("msfsPrior");
            String msfsBackup = json.getString("msfsBackup");
            if (!TextUtils.isEmpty(msfsPrior)) {
                SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER, msfsPrior);
            } else {
                SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER, msfsBackup);
            }
        }

        if (json.has("discovery")) {
            String discoveryUrl = json.getString("discovery");
            if (!TextUtils.isEmpty(discoveryUrl)) {
                SystemConfigSp.instance().init(ctx.getApplicationContext());
                SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.DISCOVERYURI, discoveryUrl);
            }
        }

        if (json.has("fastDfs")) {
            String dfsUrl = json.getString("fastDfs");
            if (!TextUtils.isEmpty(dfsUrl)) {
                SystemConfigSp.instance().init(ctx.getApplicationContext());
                SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.FASTDFSSERVER, dfsUrl);
            }
        }

        if (json.has("apkDownloadUrl")) {
            String upgradeUrl = json.getString("apkDownloadUrl");
            if (!TextUtils.isEmpty(upgradeUrl)) {
                SystemConfigSp.instance().init(ctx.getApplicationContext());
                SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.UPGRADESERVER, upgradeUrl);
            }
        }

        MsgServerAddrsEntity addrsEntity = new MsgServerAddrsEntity();
        addrsEntity.priorIP = priorIP;
        addrsEntity.backupIP = backupIP;
        addrsEntity.port = port;
        logger.i("login#got loginserverAddrsEntity:%s", addrsEntity);
        return addrsEntity;
    }

    /**
     * ------------get/set----------------------------
     */
    public SocketEvent getSocketStatus() {
        return socketStatus;
    }

    public void setSocketStatus(SocketEvent socketStatus) {
        this.socketStatus = socketStatus;
    }
}
