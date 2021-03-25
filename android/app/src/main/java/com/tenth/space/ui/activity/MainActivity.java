package com.tenth.space.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.tenth.space.R;
import com.tenth.space.app.IMApplication;
import com.tenth.space.config.IntentConstant;
import com.tenth.space.imservice.event.LoginEvent;
import com.tenth.space.imservice.event.PriorityEvent;
import com.tenth.space.imservice.event.UnreadEvent;
import com.tenth.space.imservice.manager.IMHeartBeatManager;
import com.tenth.space.imservice.manager.IMSocketManager;
import com.tenth.space.imservice.service.IMService;
import com.tenth.space.imservice.support.IMServiceConnector;
import com.tenth.space.protobuf.IMSystem;
import com.tenth.space.ui.fragment.ChatFragment;
import com.tenth.space.ui.fragment.ContactsFragment;
import com.tenth.space.ui.widget.NaviTabButton;
import com.tenth.space.utils.Logger;
import com.tenth.space.utils.Utils;

import java.util.List;

import de.greenrobot.event.EventBus;


public class MainActivity extends FragmentActivity {
    private Fragment[] mFragments;
    private NaviTabButton[] mTabButtons;
    private Logger logger = Logger.getLogger(MainActivity.class);
    private IMService imService;
    private int lastWitch = 0;
    public static MainActivity instance = null;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService();
        }

        @Override
        public void onServiceDisconnected() {
        }
    };
    private boolean netIsNotEnable;
    private int unreadSysMsgCnt;
    private int UnreadBuddy;
    private int UnreadGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger.i("MainActivity#savedInstanceState:%s", savedInstanceState);
        //todo eric when crash, this will be called, why?
        if (savedInstanceState != null) {
            logger.w("MainActivity#crashed and restarted, just exit");
            jumpToLoginPage();
            finish();
        }

        // 在这个地方加可能会有问题吧
        EventBus.getDefault().register(this);
        imServiceConnector.connect(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tt_activity_main);

        initTab();
        initFragment();
        setFragmentIndicator(0);
        instance = this;
    }

    @Override
    public void onBackPressed() {
        //don't let it exit
        //super.onBackPressed();

        //nonRoot	If false then this only works if the activity is the root of a task; if true it will work for any activity in a task.
        //document http://developer.android.com/reference/android/app/Activity.html

        //moveTaskToBack(true);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);

    }


    private void initFragment() {
        mFragments = new Fragment[5];
        mFragments[0] = getSupportFragmentManager().findFragmentById(R.id.fragment_home);
        mFragments[1] = getSupportFragmentManager().findFragmentById(R.id.fragment_chat);
        mFragments[2] = getSupportFragmentManager().findFragmentById(R.id.fragment_contact);
        mFragments[3] = getSupportFragmentManager().findFragmentById(R.id.fragment_discover);//fragment_blog //wystan modify 210227
        mFragments[4] = getSupportFragmentManager().findFragmentById(R.id.fragment_my);
    }

    private void initTab() {
        mTabButtons = new NaviTabButton[5];

        mTabButtons[0] = (NaviTabButton) findViewById(R.id.tabbutton_home);
        mTabButtons[1] = (NaviTabButton) findViewById(R.id.tabbutton_chat);
        mTabButtons[2] = (NaviTabButton) findViewById(R.id.tabbutton_contact);
        mTabButtons[3] = (NaviTabButton) findViewById(R.id.tabbutton_internal);
        mTabButtons[4] = (NaviTabButton) findViewById(R.id.tabbutton_my);

        mTabButtons[0].setTitle(getString(R.string.main_home));
        mTabButtons[0].setIndex(0);
        mTabButtons[0].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_first_sel));
        mTabButtons[0].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_first_nor));

        mTabButtons[1].setTitle(getString(R.string.main_chat));
        mTabButtons[1].setIndex(1);
        mTabButtons[1].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_chat_sel));
        mTabButtons[1].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_chat_nor));

        mTabButtons[2].setTitle(getString(R.string.main_contact));
        mTabButtons[2].setIndex(2);
        mTabButtons[2].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_contact_sel));
        mTabButtons[2].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_contact_nor));

        mTabButtons[3].setTitle(getString(R.string.main_innernet));
        mTabButtons[3].setIndex(3);
        mTabButtons[3].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_blog_select));
        mTabButtons[3].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_blog_nor));

        mTabButtons[4].setTitle(getString(R.string.main_me_tab));
        mTabButtons[4].setIndex(4);
        mTabButtons[4].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_me_sel));
        mTabButtons[4].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_me_nor));
    }

    public void setFragmentIndicator(int which) {
        //此处是开启与关闭定时器，开启关闭摄像头
//        HomeFragment currentFramgent = (HomeFragment)mFragments[0];
//        if (which==0){
//            if (lastWitch!=0){
//                currentFramgent.doOpenCamare();
//                //currentFramgent.customadapter.startTimer();
//            }
//        }else {
//            currentFramgent.doCloseCamare();
//           // currentFramgent.customadapter.stopTimer();
//        }
        getSupportFragmentManager().beginTransaction().hide(mFragments[0]).hide(mFragments[1]).hide(mFragments[2]).hide(mFragments[3]).hide(mFragments[4]).show(mFragments[which]).commit();
        mTabButtons[0].setSelectedButton(false);
        mTabButtons[1].setSelectedButton(false);
        mTabButtons[2].setSelectedButton(false);
        mTabButtons[3].setSelectedButton(false);
        mTabButtons[4].setSelectedButton(false);

        mTabButtons[which].setSelectedButton(true);
        lastWitch = which;
    }

    public void setUnreadMessageCnt(int unreadCnt) {
        mTabButtons[1].setUnreadNotify(unreadCnt);
    }

    public void setNewContact(int total) {
        mTabButtons[2].setUnreadNotify(total);
    }

    public int getLocalUreadCnt(int index) {
        return mTabButtons[index].getLocalUnreadCnt();
    }


    /**
     * 双击事件
     */
    public void chatDoubleListener() {
        setFragmentIndicator(1);
        ((ChatFragment) mFragments[1]).scrollToUnreadPosition();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleLocateDepratment(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //当页面再次可见的时候，判断网络是否已经连接ok，ok就不用处理，没有ok就再次重连
        if (netIsNotEnable) {
            Intent intent = new Intent(this, DialogActivity.class);
            intent.putExtra("click", true);
            startActivity(intent);
            netIsNotEnable = false;

        }
    }

    private void handleLocateDepratment(Intent intent) {
        int departmentIdToLocate = intent.getIntExtra(IntentConstant.KEY_LOCATE_DEPARTMENT, -1);
        if (departmentIdToLocate == -1) {
            return;
        }

        logger.i("department#got department to locate id:%d", departmentIdToLocate);
        setFragmentIndicator(2);
        ContactsFragment fragment = (ContactsFragment) mFragments[2];
        if (fragment == null) {
            logger.e("department#fragment is null");
            return;
        }
        fragment.locateDepartment(departmentIdToLocate);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        logger.i("mainactivity#onDestroy");
        EventBus.getDefault().unregister(this);
        //imService.getReconnectManager().reset();
        imServiceConnector.disconnect(this);
        //IMSocketManager.instance().onMsgServerDisconn();
       // IMHeartBeatManager.instance().onMsgServerDisconn();
        super.onDestroy();
    }


    public void onEventMainThread(UnreadEvent event) {
        switch (event.event) {
            case SESSION_READED_UNREAD_MSG:
            case UNREAD_MSG_LIST_OK:
            case UNREAD_MSG_RECEIVED:
                showUnreadMessageCount();
                break;
        }
    }

    private void showUnreadMessageCount() {
        //todo eric when to
        if (imService != null) {
            int unreadNum = imService.getUnReadMsgManager().getTotalUnreadCount();
            mTabButtons[1].setUnreadNotify(unreadNum);
        }

    }

    public void onEventMainThread(PriorityEvent event) {
        switch (event.event) {
            case MSG_UNREAD_CNT_ADD_RSP:
                //未读请求数
                int unreadCnt = 0;
                int unreadSysMsg=0;
                int unreadBuddy = 0;
                int ureadGroup = 0;
                IMSystem.IMSysMsgUnreadCntRsp cntObj = (IMSystem.IMSysMsgUnreadCntRsp) event.object;
                List<IMSystem.UnreadSysMsgCnt> unreadListList = cntObj.getUnreadListList();
                IMSystem.SysMsgType type;
                for (IMSystem.UnreadSysMsgCnt unreadSysMsgCnt : unreadListList) {
                    type = unreadSysMsgCnt.getType();
                    int count = unreadSysMsgCnt.getCount();
                    if (type == IMSystem.SysMsgType.SYS_MSG_SYSTEM) {
                        unreadSysMsg = count;
                    }
                    else if (type == IMSystem.SysMsgType.SYS_MSG_BUDDY ) {
                        unreadCnt += count;
                        unreadBuddy = count;

                    }
                    else if (type == IMSystem.SysMsgType.SYS_MSG_GROUP) {
                        unreadCnt += count;
                        ureadGroup=count;
                    }
                }
                if(unreadSysMsg>0){
                    setUnreadSysMsgCnt(unreadSysMsg);
                    setUnreadSysMsg(unreadSysMsg);
                }
                if (unreadCnt > 0) {
                    setNewContact(unreadCnt);
                    setUnreadBuddy(unreadBuddy);
                    setUreadGroup(ureadGroup);
                }
                break;
        }
    }
    public void setUnreadSysMsg(int unreadSysMsg){
        mTabButtons[4].setUnreadNotify(unreadSysMsg);
    }
    public void setUnreadBuddy(int unread){
        UnreadBuddy=unread;
    }
    public void setUreadGroup(int unread){
        UnreadGroup=unread;
    }
    public int getUnreadBuddy(){
        return  UnreadBuddy;
    }
    public int getUreadGroup(){
        return  UnreadGroup;
    }
    public void setUnreadSysMsgCnt(int cnt){
        unreadSysMsgCnt=cnt;
    }
  public int getUnreadSysMsgCnt(){
        return unreadSysMsgCnt;
    }

    public void onEventMainThread(LoginEvent event) {
        switch (event) {
            case LOGIN_OUT:
                handleOnLogout();
                break;
            case TIME_OUT:/*2016.12.31xubo*/
                //自动重连超过3次
                //判断是前台进程还是后台进程,只有在前台进程的时候才弹出dialog
                 boolean isBackground = Utils.getApplicationValue(IMApplication.app);
               // boolean isBackground = Utils.isApplicationBroughtToBackground(this);
                if (isBackground) {
                    //前台进程
                    Intent intent = new Intent(this, DialogActivity.class);
                    startActivity(intent);
                } else {
                    //当其可见的时候判断，网络状态是否正常，不正常就从新连接
                    netIsNotEnable = true;
                }
                break;
        }
    }

    private void handleOnLogout() {
        logger.i("mainactivity#login#handleOnLogout");
        finish();
        logger.i("mainactivity#login#kill self, and start login activity");
       // jumpToLoginPage(); //wystan modify for just exit app 210224
    }

    private void jumpToLoginPage() {
        Intent intent = new Intent(this, EntryActivity.class);
        intent.putExtra(IntentConstant.KEY_LOGIN_NOT_AUTO, true);
        startActivity(intent);
    }
}
