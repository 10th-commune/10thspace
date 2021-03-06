
package com.tenth.space.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.tenth.space.DB.entity.GroupEntity;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.R;
import com.tenth.space.config.DBConstant;
import com.tenth.space.imservice.entity.RecentInfo;
import com.tenth.space.imservice.event.GroupEvent;
import com.tenth.space.imservice.event.LoginEvent;
import com.tenth.space.imservice.event.PriorityEvent;
import com.tenth.space.imservice.event.ReconnectEvent;
import com.tenth.space.imservice.event.SessionEvent;
import com.tenth.space.imservice.event.SocketEvent;
import com.tenth.space.imservice.event.UnreadEvent;
import com.tenth.space.imservice.event.UserInfoEvent;
import com.tenth.space.imservice.manager.IMContactManager;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.imservice.manager.IMReconnectManager;
import com.tenth.space.imservice.manager.IMUnreadMsgManager;
import com.tenth.space.imservice.service.IMService;
import com.tenth.space.imservice.support.IMServiceConnector;
import com.tenth.space.protobuf.IMGroup;
import com.tenth.space.ui.activity.MainActivity;
import com.tenth.space.ui.adapter.ChatAdapter;
import com.tenth.space.utils.IMUIHelper;
import com.tenth.space.utils.NetworkUtil;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * @author Nana
 * @Description ???????????????Fragment???
 * @date 2014-7-24
 */
public class ChatFragment extends MainFragment
        implements
        OnItemSelectedListener,
        OnItemClickListener,
        OnItemLongClickListener,
        SwipeMenuCreator {

    private ChatAdapter contactAdapter;
    private SwipeMenuListView contactListView;
    private View curView = null;
    private View noNetworkView;
    private View noChatView;
    private ImageView notifyImage;
    private TextView displayView;
    private ProgressBar reconnectingProgressBar;
    private IMService imService;

    //??????????????????????????????fasle:??????????????????????????????. true:?????????????????????????????????
    private volatile boolean isManualMConnect = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {

        @Override
        public void onServiceDisconnected() {
            if (EventBus.getDefault().isRegistered(ChatFragment.this)) {
                EventBus.getDefault().unregister(ChatFragment.this);
            }
        }

        @Override
        public void onIMServiceConnected() {
            // TODO Auto-generated method stub
            logger.i("chatfragment#recent#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                // why ,some reason
                return;
            }
            // ?????????????????????????????????????????????????????????????????????
            onRecentContactDataReady();
            EventBus.getDefault().registerSticky(ChatFragment.this);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(getActivity());
        logger.i("chatfragment#onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle bundle) {
        logger.i("onCreateView");
        if (null != curView) {
            logger.i("curView is not null, remove it");
            ((ViewGroup) curView.getParent()).removeView(curView);
        }
        curView = inflater.inflate(R.layout.tt_fragment_chat, topContentView);
        // ???????????????????????????view
        noNetworkView = curView.findViewById(R.id.layout_no_network);
        noChatView = curView.findViewById(R.id.layout_no_chat);
        reconnectingProgressBar = (ProgressBar) curView.findViewById(R.id.progressbar_reconnect);
        displayView = (TextView) curView.findViewById(R.id.disconnect_text);
        notifyImage = (ImageView) curView.findViewById(R.id.imageWifi);

        super.init(curView);
        initTitleView();// ???????????????view
        initContactListView(); // ??????????????????????????????
        showProgressBar();// ??????????????????????????????????????????zz
        return curView;
    }

    /**
     * @Description ??????????????????
     */
    private void initTitleView() {
        // ????????????
        //setTopTitleBold(getActivity().getString(R.string.chat_title));
        setTopTitle(getActivity().getString(R.string.chat_title));
    }

    private void initContactListView() {
        contactListView = (SwipeMenuListView) curView.findViewById(R.id.ContactListView);
        contactListView.setOnItemClickListener(this);
        contactListView.setOnItemLongClickListener(this);  //wystan disable 210303
        //contactListView.setMenuCreator(this);
        contactAdapter = new ChatAdapter(getActivity());
        contactListView.setAdapter(contactAdapter);
//        setTopRightText("????????????");
//        topRightTitleTxt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                IMUIHelper.openGroupMemberSelectActivity(getActivity(),"1_"+IMLoginManager.instance().getLoginId(), 1);
//            }
//        });

        // this is critical, disable loading when finger sliding, otherwise
        // you'll find sliding is not very smooth
        contactListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(),
                true, true));
    }

    @Override
    public void onStart() {
        logger.i("chatfragment#onStart");
        super.onStart();
    }

    @Override
    public void onStop() {
        logger.i("chatfragment#onStop");
        super.onStop();
    }

    @Override
    public void onPause() {
        logger.i("chatfragment#onPause");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(ChatFragment.this)) {
            EventBus.getDefault().unregister(ChatFragment.this);
        }
        imServiceConnector.disconnect(getActivity());
        super.onDestroy();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // ??????????????????????????????
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        RecentInfo recentInfo = contactAdapter.getItem(position);
        if (recentInfo == null) {
            logger.e("recent#null recentInfo -> position:%d", position);
            return;
        }
        IMUIHelper.openChatActivity(getActivity(), recentInfo.getSessionKey());
    }

    public void onEventMainThread(SessionEvent sessionEvent) {
        logger.i("chatfragment#SessionEvent# -> %s", sessionEvent);
        switch (sessionEvent) {
            case RECENT_SESSION_LIST_UPDATE:
            case RECENT_SESSION_LIST_SUCCESS:
            case SET_SESSION_TOP:
                onRecentContactDataReady();
                break;
        }
    }
    public void onEventMainThread(GroupEvent event) {
        switch (event.getEvent()) {
            case GROUP_INFO_OK:
            case CHANGE_GROUP_MEMBER_SUCCESS:
                onRecentContactDataReady();
                searchDataReady();
                break;

            case GROUP_INFO_UPDATED:
                onRecentContactDataReady();
                searchDataReady();
                break;
            case SHIELD_GROUP_OK:
                // ???????????????????????????????????????session
                onShieldSuccess(event.getGroupEntity());
                break;
            case SHIELD_GROUP_FAIL:
            case SHIELD_GROUP_TIMEOUT:
                onShieldFail();
                break;
        }
    }

    public void onEventMainThread(UnreadEvent event) {
        switch (event.event) {
            case UNREAD_MSG_RECEIVED:
            case UNREAD_MSG_LIST_OK:
            case SESSION_READED_UNREAD_MSG:
                onRecentContactDataReady();
                break;
        }
    }

    public void onEventMainThread(UserInfoEvent.Event event) {
        switch (event) {
            case USER_INFO_UPDATE:
            case USER_INFO_OK:
                onRecentContactDataReady();
                searchDataReady();
                break;
        }
    }

    public void onEventMainThread(LoginEvent loginEvent) {
        logger.i("chatfragment#LoginEvent# -> %s", loginEvent);
        switch (loginEvent) {
            case LOCAL_LOGIN_SUCCESS:
            case LOGINING:
                logger.i("chatFragment#login#recv handleDoingLogin event");
                if (reconnectingProgressBar != null) {
                    reconnectingProgressBar.setVisibility(View.VISIBLE);
                }
                break;

            case LOCAL_LOGIN_MSG_SERVICE:
            case LOGIN_OK:
                isManualMConnect = false;
                logger.i("chatfragment#loginOk");
                noNetworkView.setVisibility(View.GONE);
                break;

            case LOGIN_AUTH_FAILED:
            case LOGIN_INNER_FAILED:
                onLoginFailure(loginEvent);
                break;

            case PC_OFFLINE:
            case KICK_PC_SUCCESS:
                onPCLoginStatusNotify(false);
                break;

            case KICK_PC_FAILED:
                Toast.makeText(getActivity(), getString(R.string.kick_pc_failed), Toast.LENGTH_SHORT).show();
                break;
            case PC_ONLINE:
                onPCLoginStatusNotify(true);
                break;

            default:
                reconnectingProgressBar.setVisibility(View.GONE);
                break;
        }
    }


    public void onEventMainThread(SocketEvent socketEvent) {
        switch (socketEvent) {
            case MSG_SERVER_DISCONNECTED:
                handleServerDisconnected();
                break;

            case CONNECT_MSG_SERVER_FAILED:
            case REQ_MSG_SERVER_ADDRS_FAILED:
                handleServerDisconnected();
                onSocketFailure(socketEvent);
                break;
        }
    }

    public void onEventMainThread(ReconnectEvent reconnectEvent) {
        switch (reconnectEvent) {
            case DISABLE: {
                handleServerDisconnected();//???????????????????????????????????????????????????
            }
            break;
        }
    }
    public void onEventMainThread(PriorityEvent priorityEvent) {
        switch (priorityEvent.event) {
            case CREATE_GROUP_OK:
                IMGroup.IMGroupCreateRsp imGroupCreateRsp = (IMGroup.IMGroupCreateRsp) priorityEvent.object;
            break;
        }
    }

    private void onLoginFailure(LoginEvent event) {
        if (!isManualMConnect) {
            return;
        }
        isManualMConnect = false;
        String errorTip = getString(IMUIHelper.getLoginErrorTip(event));
        logger.i("login#errorTip:%s", errorTip);
        reconnectingProgressBar.setVisibility(View.GONE);
        Toast.makeText(getActivity(), errorTip, Toast.LENGTH_SHORT).show();
    }

    private void onSocketFailure(SocketEvent event) {
        if (!isManualMConnect) {
            return;
        }
        isManualMConnect = false;
        String errorTip = getString(IMUIHelper.getSocketErrorTip(event));
        logger.i("login#errorTip:%s", errorTip);
        reconnectingProgressBar.setVisibility(View.GONE);
        Toast.makeText(getActivity(), errorTip, Toast.LENGTH_SHORT).show();
    }

    // ?????????????????? ????????????????????????
    private void onShieldSuccess(GroupEntity entity) {
        if (entity == null) {
            return;
        }
        // ????????????sessionId
        contactAdapter.updateRecentInfoByShield(entity);
        IMUnreadMsgManager unreadMsgManager = imService.getUnReadMsgManager();

        int totalUnreadMsgCnt = unreadMsgManager.getTotalUnreadCount();
        logger.i("unread#total cnt %d", totalUnreadMsgCnt);
        ((MainActivity) getActivity()).setUnreadMessageCnt(totalUnreadMsgCnt);
    }

    private void onShieldFail() {
        Toast.makeText(getActivity(), R.string.req_msg_failed, Toast.LENGTH_SHORT).show();
    }

    /**
     * ????????????OK
     * ??????????????? user?????????????????????
     */
    public void searchDataReady() {
        if (imService.getContactManager().isUserDataReady() &&
                imService.getGroupManager().isGroupReady()) {
            showSearchFrameLayout();
        }
    }

    /**
     * ?????????PC?????????????????????
     *
     * @param isOnline
     */
    public void onPCLoginStatusNotify(boolean isOnline) {
        logger.i("chatfragment#onPCLoginStatusNotify");
        if (isOnline) {
            reconnectingProgressBar.setVisibility(View.GONE);
            noNetworkView.setVisibility(View.VISIBLE);
            notifyImage.setImageResource(R.drawable.pc_notify);
            displayView.setText(R.string.pc_status_notify);
            /**??????????????????*/
            noNetworkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reconnectingProgressBar.setVisibility(View.VISIBLE);
                    imService.getLoginManager().reqKickPCClient();
                }
            });
        } else {
            noNetworkView.setVisibility(View.GONE);
        }
    }

    private void handleServerDisconnected() {
        logger.i("chatfragment#handleServerDisconnected");

        if (reconnectingProgressBar != null) {
            reconnectingProgressBar.setVisibility(View.GONE);
        }

        if (noNetworkView != null) {

            if (imService != null) {
                if (imService.getLoginManager().isKickout()) {
                    notifyImage.setImageResource(R.drawable.warning);
                    noNetworkView.setVisibility(View.VISIBLE);
                    displayView.setText(R.string.disconnect_kickout);
                }
//                else {
//                    displayView.setText(R.string.no_network);
//                }
            }
            /**?????????????????????????????????????????????*/
            noNetworkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logger.i("chatFragment#noNetworkView clicked");
                    IMReconnectManager manager = imService.getReconnectManager();
                    if (NetworkUtil.isNetWorkAvalible(getActivity())) {
                        isManualMConnect = true;
                        IMLoginManager.instance().relogin();
                    } else {
                        Toast.makeText(getActivity(), R.string.no_network_toast, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    reconnectingProgressBar.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    /**
     * ??????????????????????????????
     */
    private void onRecentContactDataReady() {
        boolean isUserData = imService.getContactManager().isUserDataReady();
        boolean isSessionData = imService.getSessionManager().isSessionListReady();
        boolean isGroupData = imService.getGroupManager().isGroupReady();

        if (!(isUserData && isSessionData && isGroupData)) {
            return;
        }
//        IMUnreadMsgManager unreadMsgManager = imService.getUnReadMsgManager();
//
//        int totalUnreadMsgCnt = unreadMsgManager.getTotalUnreadCount();
//        logger.i("unread#total cnt %d", totalUnreadMsgCnt);
//        ((MainActivity) getActivity()).setUnreadMessageCnt(totalUnreadMsgCnt);

        List<RecentInfo> recentSessionList = imService.getSessionManager().getRecentListInfo();

        setNoChatView(recentSessionList);
        contactAdapter.setData(recentSessionList);
        hideProgressBar();
        showSearchFrameLayout();
    }

    private void setNoChatView(List<RecentInfo> recentSessionList) {
        if (recentSessionList.size() == 0) {
            noChatView.setVisibility(View.VISIBLE);
        } else {
            noChatView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {

        RecentInfo recentInfo = contactAdapter.getItem(position);
        if (recentInfo == null) {
            logger.e("recent#onItemLongClick null recentInfo -> position:%d", position);
            return false;
        }
        if (recentInfo.getSessionType() == DBConstant.SESSION_TYPE_SINGLE) {
            handleContactItemLongClick(getActivity(), recentInfo);
        } else {
            handleGroupItemLongClick(getActivity(), recentInfo);
        }
        return true;
    }

    private void handleContactItemLongClick(final Context ctx, final RecentInfo recentInfo) {

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ctx, android.R.style.Theme_Holo_Light_Dialog));
        builder.setTitle(recentInfo.getName());
        final boolean isTop = imService.getConfigSp().isTopSession(recentInfo.getSessionKey());

        int topMessageRes = isTop ? R.string.cancel_top_message : R.string.top_message;
        String[] items = new String[]{ctx.getString(R.string.check_profile),
                ctx.getString(R.string.delete_session),
                ctx.getString(topMessageRes)};

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    /*
                    case 0:
                        UserEntity userEntity = IMContactManager.instance().findContact(recentInfo.getPeerId());
                        String relation= "";
                        if(userEntity !=null){
                            relation = userEntity.getRelation();
                        }
                        IMUIHelper.openUserProfileActivity(ctx, recentInfo.getPeerId(),relation);
                        break;

                     */
                    case 1:
                        imService.getSessionManager().reqRemoveSession(recentInfo);
                        break;
                    case 2: {
                        imService.getConfigSp().setSessionTop(recentInfo.getSessionKey(), !isTop);
                    }
                    break;
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    // ????????????????????????????????????
    private void handleGroupItemLongClick(final Context ctx, final RecentInfo recentInfo) {

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ctx, android.R.style.Theme_Holo_Light_Dialog));
        builder.setTitle(recentInfo.getName());

        final boolean isTop = imService.getConfigSp().isTopSession(recentInfo.getSessionKey());
        final boolean isForbidden = recentInfo.isForbidden();
        int topMessageRes = isTop ? R.string.cancel_top_message : R.string.top_message;
        int forbidMessageRes = isForbidden ? R.string.cancel_forbid_group_message : R.string.forbid_group_message;

        String[] items = new String[]{ctx.getString(R.string.delete_session), ctx.getString(topMessageRes), ctx.getString(forbidMessageRes)};

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        imService.getSessionManager().reqRemoveSession(recentInfo);
                        break;
                    case 1: {
                        imService.getConfigSp().setSessionTop(recentInfo.getSessionKey(), !isTop);
                    }
                    break;
                    case 2: {
                        // ???????????????????????????
                        int shieldType = isForbidden ? DBConstant.GROUP_STATUS_ONLINE : DBConstant.GROUP_STATUS_SHIELD;
                        imService.getGroupManager().reqShieldGroup(recentInfo.getPeerId(), shieldType);
                    }
                    break;
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    @Override
    protected void initHandler() {
        // TODO Auto-generated method stub
    }

    /**
     * ?????????????????????????????????????????????
     * ?????????????????????
     */
    public void scrollToUnreadPosition() {
        if (contactListView != null) {
            int currentPosition = contactListView.getFirstVisiblePosition();
            int needPosition = contactAdapter.getUnreadPositionOnView(currentPosition);
            // ?????????????????????!!
            //contactListView.smoothScrollToPosition(needPosition);
            contactListView.setSelection(needPosition);
        }
    }

    @Override
    public void create(SwipeMenu menu) {
        // create "open" item
        SwipeMenuItem openItem = new SwipeMenuItem(getActivity());
        // set item background
        openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
        // set item width
        openItem.setWidth(90);
        // set item title
        openItem.setTitle("??????");
        // set item title fontsize
        openItem.setTitleSize(18);
        // set item title font color
        openItem.setTitleColor(Color.WHITE);
        // add to menu
        menu.addMenuItem(openItem);

        // create "delete" item
        SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity());
        // set item background
        deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
        // set item width
        deleteItem.setWidth(90);
        // set a icon
        deleteItem.setIcon(R.drawable.bt_shanchu);
        // add to menu
        menu.addMenuItem(deleteItem);
    }
}
