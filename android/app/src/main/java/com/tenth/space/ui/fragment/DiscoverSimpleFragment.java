package com.tenth.space.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.tenth.space.DB.DBInterface;
import com.tenth.space.DB.entity.DepartmentEntity;
import com.tenth.space.DB.entity.GroupEntity;
import com.tenth.space.DB.entity.GroupRequesterEntity;
import com.tenth.space.DB.entity.RequesterEntity;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.R;
import com.tenth.space.config.DBConstant;
import com.tenth.space.config.HandlerConstant;
import com.tenth.space.imservice.entity.RecentInfo;
import com.tenth.space.imservice.event.GroupEvent;
import com.tenth.space.imservice.event.PriorityEvent;
import com.tenth.space.imservice.event.SearchFriendListEvent;
import com.tenth.space.imservice.event.UserInfoEvent;
import com.tenth.space.imservice.manager.FriendManager;
import com.tenth.space.imservice.manager.IMContactManager;
import com.tenth.space.imservice.manager.IMGroupManager;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.imservice.manager.IMSocketManager;
import com.tenth.space.imservice.service.IMService;
import com.tenth.space.imservice.support.IMServiceConnector;
import com.tenth.space.protobuf.IMBaseDefine;
import com.tenth.space.protobuf.IMBuddy;
import com.tenth.space.protobuf.IMSystem;
import com.tenth.space.protobuf.helper.ProtoBuf2JavaBean;
import com.tenth.space.ui.activity.FriendsActivity;
import com.tenth.space.ui.activity.GroupActivity;
import com.tenth.space.ui.activity.MainActivity;
import com.tenth.space.ui.activity.NewFriendsActivity;
import com.tenth.space.ui.adapter.ChatAdapter;
import com.tenth.space.ui.adapter.ContactAdapter;
import com.tenth.space.ui.adapter.DeptAdapter;
import com.tenth.space.ui.adapter.DiscoverSimpleAdapter;
import com.tenth.space.ui.adapter.NewFriendAdapter;
import com.tenth.space.ui.adapter.NewGroupAdapter;
import com.tenth.space.ui.adapter.SearchAdapter;
import com.tenth.space.ui.widget.SortSideBar;
import com.tenth.space.utils.pinyin.PinYin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;




public class DiscoverSimpleFragment extends MainFragment implements SortSideBar.OnTouchingLetterChangedListener {
    private View curView = null;
    private static Handler uiHandler = null;
    private static boolean bFirstCreate = true;
    private static int nMaxUsersShow = 7;

    private ListView userListView;
    private ListView groupListView;

    private DiscoverSimpleAdapter usersAdapter;
    private DiscoverSimpleAdapter groupsAdapter;

    private IMService imService;
    private IMContactManager contactMgr;
    private int curTabIndex = 0;


    @SuppressLint("ValidFragment")
    public DiscoverSimpleFragment(int unreadBuddyAddCnt) {

    }

    public DiscoverSimpleFragment() {

    }

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
            initAdapter();
            renderEntityList();
            EventBus.getDefault().registerSticky(DiscoverSimpleFragment.this);

            if(bFirstCreate) {
                bFirstCreate = false;
                getRecommendUserRequest();
             //   getRecommendGroupRequest();
            }
        }

        @Override
        public void onServiceDisconnected() {
            if (EventBus.getDefault().isRegistered(DiscoverSimpleFragment.this)) {
                EventBus.getDefault().unregister(DiscoverSimpleFragment.this);
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(getActivity());
        initHandler();
    }

    public void onDestroy() {
        super.onDestroy();
        bFirstCreate = true;
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        imServiceConnector.disconnect(getActivity());
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void initHandler() {
        uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HandlerConstant.HANDLER_CHANGE_CONTACT_TAB:
                        if (null != msg.obj) {
                            curTabIndex = (Integer) msg.obj;
                            if (0 == curTabIndex) {
                                userListView.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.fragment_discoversimple, topContentView);
        initRes();
        initData();
        return curView;
    }

    private void initData() {

    }

    /**
     * @Description 初始化界面资源
     */
    private void initRes() {
        // 设置顶部标题栏
        setTopTitle(getActivity().getString(R.string.main_innernet));
        showContactTopBar();

        //hideTopBar();

        super.init(curView);

        userListView = (ListView)curView.findViewById(R.id.friends_list);
        groupListView = (ListView)curView.findViewById(R.id.group_list);

        userListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
        groupListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
       // showProgressBar();
        hideProgressBar();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 201) {

        }
    }

    private void initAdapter() {
        usersAdapter = new DiscoverSimpleAdapter(getActivity(), imService);
        groupsAdapter  = new DiscoverSimpleAdapter(getActivity(), imService);

        userListView.setAdapter(usersAdapter);
        groupListView.setAdapter(groupsAdapter);

        userListView.setOnItemClickListener(usersAdapter);
        groupListView.setOnItemClickListener(groupsAdapter);

    }


    /**
     * 刷新单个entity
     * 很消耗性能
     */
    private void renderEntityList() {
        hideProgressBar();
        logger.i("wystan renderEntityList");
/*
        if (contactMgr.isUserDataReady()) {
            renderUserList();
        }
 */
        if (imService.getGroupManager().isGroupReady()) {
            ;//renderGroupList();//wystan disable for get newest from server 210304
        }
    }


    private void renderUserList(List<UserEntity> contactList) {
        logger.i("wystan renderUserList");
       // List<UserEntity> contactList = contactMgr.getContactSortedList();
        // wystan disable for not friendship 210118

        for (Iterator<UserEntity> userEntitys = contactList.iterator(); userEntitys.hasNext(); ) {
            UserEntity userEntity = userEntitys.next();
            //logger.i("wystan renderUserList :%s %s",IMLoginManager.instance().getLoginId(), userEntity.getPeerId());
            if (IMLoginManager.instance().getLoginId().equals(userEntity.getPeerId())){//!userEntity.getRelation().equals(IMBaseDefine.UserRelationType.RELATION_FRIEND.name())) {
                userEntitys.remove();
            }
        }

        // 没有任何的联系人数据
        if (contactList.size() <= 0) {
            return;
        }
        usersAdapter.putUserList(contactList);
        resetUserListViewHeight();
    }

    private void renderGroupList() {

        List<GroupEntity> originList = imService.getGroupManager().getGroupList(DBConstant.GROUP_TYPE_ACTIVE);
        logger.i("group#onGroupReady :%d", originList.size());
        if (originList.size() <= 0) {
            return;
        }
        groupsAdapter.putGroupList(originList);
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        int position = -1;
        if (0 == curTabIndex) {
//            position = usersAdapter.getPositionForSection(s.charAt(0));
        } else {

        }
        if (position != -1) {
            //getCurListView().setSelection(position);
        }
    }

    public static Handler getHandler() {
        return uiHandler;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    public void onEventMainThread(PriorityEvent event) {
        switch (event.event) {

        }
    }

    /*
    public void onEventMainThread(UserInfoEvent.Event event) {
        switch (event) {
            case USER_INFO_UPDATE:
            case USER_INFO_OK:
                renderUserList();
                searchDataReady();
                resetUserListViewHeight();
                break;
        }
    }
     */

    public void onEventMainThread(SearchFriendListEvent event) {
        switch (event.getEvent()) {
            case RECOMMEND:
                List<UserEntity> contactList = new ArrayList<>();
                for (IMBaseDefine.UserInfo userInfo : event.getSearchUserList()) {
                    UserEntity userEntity = ProtoBuf2JavaBean.getUserEntity(userInfo);
                    if (!IMLoginManager.instance().getLoginId().equals(userEntity.getPeerId())){//wystan add for filter self 210126
                        contactList.add(userEntity);
                    }
                }
                renderUserList(contactList);
            default:
                break;
        }

    }

    public void onEventMainThread(GroupEvent event) {
        switch (event.getEvent()) {
            case GROUP_INFO_UPDATED:
                //renderGroupList();

                break;
            default:
                break;
        }
    }

    public void searchDataReady() {
        if (imService.getContactManager().isUserDataReady() &&
                imService.getGroupManager().isGroupReady()) {
            showSearchFrameLayout();
        }
    }

    public void getRecommendUserRequest(){
        FriendManager.instance().reqRecommendUsers();
    }

    public void getRecommendGroupRequest(){
        logger.i("contact#getRecommendGroupRequest");
        IMGroupManager.instance().reqRecommendGroup();
    }

    private void resetUserListViewHeight(){

        if(usersAdapter == null || usersAdapter.getCount() == 0){
            return;
        }
        View itemView = usersAdapter.getView(0, null, null);
        itemView.measure(0,0);
        int itemHeight = itemView.getMeasuredHeight();
        int itemCount = usersAdapter.getCount();
        LinearLayout.LayoutParams layoutParams = null;
        if(itemCount <= nMaxUsersShow) {
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT ,ViewGroup.LayoutParams.WRAP_CONTENT);
        }else{
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT ,itemHeight*nMaxUsersShow);
        }
        userListView.setLayoutParams(layoutParams);
    }

}