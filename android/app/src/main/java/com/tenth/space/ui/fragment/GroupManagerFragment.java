package com.tenth.space.ui.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.tenth.space.DB.DBInterface;
import com.tenth.space.DB.entity.GroupEntity;
import com.tenth.space.DB.entity.PeerEntity;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.DB.sp.ConfigurationSp;
import com.tenth.space.R;
import com.tenth.space.config.DBConstant;
import com.tenth.space.config.IntentConstant;
import com.tenth.space.imservice.entity.RecentInfo;
import com.tenth.space.imservice.event.GroupEvent;
import com.tenth.space.imservice.event.UserInfoEvent;
import com.tenth.space.imservice.manager.IMGroupManager;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.imservice.manager.IMSessionManager;
import com.tenth.space.imservice.service.IMService;
import com.tenth.space.imservice.support.IMServiceConnector;
import com.tenth.space.protobuf.IMBaseDefine;
import com.tenth.space.ui.activity.MessageActivity;
import com.tenth.space.ui.adapter.GroupManagerAdapter;
import com.tenth.space.ui.base.TTBaseFragment;
import com.tenth.space.ui.helper.CheckboxConfigHelper;
import com.tenth.space.utils.ToastUtils;
import com.tenth.space.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.greenrobot.event.EventBus;

import static com.tenth.space.R.id.leave;


/**
 * @YM 个人与群组的聊天详情都会来到这个页面
 * single: 这有sessionId的头像，以及加号"+" ， 创建群成功之后，跳到聊天的页面
 * group:  群成员，加减号 ， 修改成功之后，跳到群管理页面
 * 临时群任何人都可以加人，但是只有群主可以踢人”这个逻辑修改下，正式群暂时只给createId开放
 */
public class GroupManagerFragment extends TTBaseFragment implements View.OnClickListener {
    private View curView = null;
    /**
     * adapter配置
     */
    private GridView gridView;
    private GroupManagerAdapter adapter;


    /**
     * 详情的配置  勿扰以及指定聊天
     */
    CheckboxConfigHelper checkBoxConfiger = new CheckboxConfigHelper();
    CheckBox noDisturbCheckbox;
    CheckBox topSessionCheckBox;

    /**
     * 需要的状态参数
     */
    private IMService imService;
    private String curSessionKey;
    private PeerEntity peerEntity;
    private TextView tv_report;
    private TextView leave_group;
    private LinearLayout progress;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(getActivity());
        EventBus.getDefault().register(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_group_manage, topContentView);
        progress = (LinearLayout) curView.findViewById(R.id.ll_progress_bar);
        noDisturbCheckbox = (CheckBox) curView.findViewById(R.id.NotificationNoDisturbCheckbox);
        topSessionCheckBox = (CheckBox) curView.findViewById(R.id.NotificationTopMessageCheckbox);
        tv_report = (TextView) curView.findViewById(R.id.tv_report);
        leave_group = (TextView) curView.findViewById(leave);
        tv_report.setOnClickListener(this);
        leave_group.setOnClickListener(this);
        initRes();
        return curView;
    }

    private void initRes() {
        // 设置标题栏
        setTopLeftButton(R.drawable.tt_top_back);
        topLetTitleTxt.setTextColor(getResources().getColor(R.color.default_bk));
        setTopLeftText(getActivity().getString(R.string.top_left_back));
        topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        imServiceConnector.disconnect(getActivity());
    }

    @Override
    protected void initHandler() {
    }


    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            logger.i("groupmgr#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                Toast.makeText(GroupManagerFragment.this.getActivity(),
                        getResources().getString(R.string.im_service_disconnected), Toast.LENGTH_SHORT).show();
                return;
            }
            checkBoxConfiger.init(imService.getConfigSp());
            initView();
            initAdapter();
        }
    };


    private void initView() {

        if (null == imService || null == curView) {
            logger.e("groupmgr#init failed,cause by imService or curView is null");
            return;
        }

        curSessionKey = getActivity().getIntent().getStringExtra(IntentConstant.KEY_SESSION_KEY);
        if (TextUtils.isEmpty(curSessionKey)) {
            logger.e("groupmgr#getSessionInfoFromIntent failed");
            return;
        }
        peerEntity = imService.getSessionManager().findPeerEntity(curSessionKey);
        if (peerEntity == null) {
            logger.e("groupmgr#findPeerEntity failed,sessionKey:%s", curSessionKey);
            return;
        }
        switch (peerEntity.getType()) {
            case DBConstant.SESSION_TYPE_GROUP: {
                GroupEntity groupEntity = (GroupEntity) peerEntity;

                setTopTitle(peerEntity.getMainName() + "(" + groupEntity.getUserCnt()+ ")");
                // 群组名称的展示
                TextView groupNameView = (TextView) curView.findViewById(R.id.group_manager_title);
                groupNameView.setText(groupEntity.getMainName());
                leave_group.setVisibility(View.VISIBLE);
            }
            break;

            case DBConstant.SESSION_TYPE_SINGLE: {
                setTopTitle(getString(R.string.chat_detail));
                // 个人不显示群聊名称
                View groupNameContainerView = curView.findViewById(R.id.group_manager_name);
                groupNameContainerView.setVisibility(View.GONE);
            }
            break;
        }
        // 初始化配置checkBox
        initCheckbox();
    }

    private void initAdapter() {
        logger.i("groupmgr#initAdapter");

        gridView = (GridView) curView.findViewById(R.id.group_manager_grid);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));// 去掉点击时的黄色背影
        gridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

        adapter = new GroupManagerAdapter(getActivity(), imService, peerEntity);
        gridView.setAdapter(adapter);
    }

    /**
     * 事件驱动通知
     */
    public void onEventMainThread(GroupEvent event) {
        switch (event.getEvent()) {
            case CHANGE_GROUP_MEMBER_FAIL:
                Toast.makeText(getActivity(), getString(R.string.change_temp_group_failed), Toast.LENGTH_SHORT).show();
                progress.setVisibility(View.GONE);

                break;
            case CHANGE_GROUP_MEMBER_TIMEOUT: {
                Toast.makeText(getActivity(), getString(R.string.change_temp_group_timeout), Toast.LENGTH_SHORT).show();
                progress.setVisibility(View.GONE);

                return;
            }
            case CHANGE_GROUP_MEMBER_SUCCESS: {
                onMemberChangeSuccess(event);
            }
            break;
            case QUIT_GROUP_SUCESS:
                ToastUtils.show("已退出群");
                progress.setVisibility(View.GONE);
                GroupEntity groupEntity = event.getGroupEntity();
                int groupType = groupEntity.getGroupType();
                if(groupType==1){
                    IMGroupManager.instance().removeGroupMap(groupEntity.getPeerId());
                    DBInterface.instance().deleteGroup(groupEntity);
                    EventBus.getDefault().postSticky(new GroupEvent(GroupEvent.Event.GROUP_INFO_UPDATED));
                }
                String peerId = groupEntity.getPeerId();
                RecentInfo targetRecentInfo=null;
                List<RecentInfo> recentListInfo = IMSessionManager.instance().getRecentListInfo();
                for(RecentInfo recentInfo:recentListInfo){
                    if(recentInfo.getPeerId().equals(peerId)){
                        targetRecentInfo=recentInfo;
                        IMSessionManager.instance().reqRemoveSession(targetRecentInfo);
                    }
                }
                MessageActivity.instance.finish();
                getActivity().finish();
                break;
            case GROUP_INFO_UPDATED:
                logger.i("wystan groupmgr#GROUP_INFO_UPDATED");
                peerEntity = imService.getSessionManager().findPeerEntity(curSessionKey);
                adapter.updateGroupData(peerEntity);
                break;

            default:
                break;
        }

    }

    public void onEventMainThread(UserInfoEvent.Event event){
        switch(event){
            case USER_INFO_UPDATE:
                    logger.i("wystan groupmgr#USER_INFO_UPDATE:%d",imService.getContactManager().queryAllUser().size());
                   // adapter.updateGroupData();

                break;

            default:
                break;
        }
    }

    private void onMemberChangeSuccess(GroupEvent event) {
        String groupId = event.getGroupEntity().getPeerId();
        if (!groupId.equals(peerEntity.getPeerId()) ) {
            return;
        }
        List<String> changeList = event.getChangeList();
        if (changeList == null || changeList.size() <= 0) {
            return;
        }



        setTopTitle(peerEntity.getMainName() + "(" + event.getGroupEntity().getUserCnt() + ")");
        int changeType = event.getChangeType();

        switch (changeType) {
            case DBConstant.GROUP_MODIFY_TYPE_ADD:

                ArrayList<UserEntity> newList = new ArrayList<>();
                for (String userId : changeList) {
                    UserEntity userEntity = imService.getContactManager().findContact(userId);
                    if (userEntity != null) {
                        newList.add(userEntity);
                    }
                }
                adapter.add(newList);


                break;
            case DBConstant.GROUP_MODIFY_TYPE_DEL:
                for (String userId : changeList) {
                    adapter.removeById(userId);
                }
                break;
        }
    }

    private void initCheckbox() {
        checkBoxConfiger.initCheckBox(noDisturbCheckbox, curSessionKey, ConfigurationSp.CfgDimension.NOTIFICATION);
        checkBoxConfiger.initTopCheckBox(topSessionCheckBox, curSessionKey);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_report:
                //Utils.showPopupWindow(getActivity(), peerEntity.getPeerId(), 11);
                break;
            case R.id.leave:
                progress.setVisibility(View.VISIBLE);
                Set<String> removeMemberlist = new HashSet<>(1);
                removeMemberlist.add(IMLoginManager.instance().getLoginId());
                IMGroupManager.instance().reqChangeGroupMember(peerEntity.getPeerId(), IMBaseDefine.GroupModifyType.GROUP_MODIFY_TYPE_DEL, removeMemberlist, 34);
                break;
        }
    }
}
