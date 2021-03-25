package com.tenth.space.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.tenth.space.DB.DBInterface;
import com.tenth.space.DB.entity.GroupRequesterEntity;
import com.tenth.space.DB.entity.RequesterEntity;
import com.tenth.space.R;
import com.tenth.space.imservice.event.PriorityEvent;
import com.tenth.space.imservice.manager.IMContactManager;
import com.tenth.space.imservice.manager.IMGroupManager;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.protobuf.IMSystem;
import com.tenth.space.ui.activity.FriendsActivity;
import com.tenth.space.ui.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class ContactFragment extends Fragment implements ViewPager.OnPageChangeListener, View.OnClickListener {
    private View mView = null;
    private ViewPager mPager;
    private RadioGroup mGroup;
    private RadioButton rbContacts;
    private RadioButton rbAttention;
    private ArrayList<Fragment> fragmentList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != mView) {
            ((ViewGroup) mView.getParent()).removeView(mView);
            return mView;
        }
        mView = inflater.inflate(R.layout.tt_fragment_contact, null);
        initView();
        initViewPager();
        EventBus.getDefault().register(this);


        Log.i("", "注册时间：" + System.currentTimeMillis());
        return mView;
    }

    private void initView() {
        mPager = (ViewPager) mView.findViewById(R.id.viewPager);
        mGroup = (RadioGroup) mView.findViewById(R.id.radiogroup);
        rbAttention = (RadioButton) mView.findViewById(R.id.rb_attention);
        rbContacts = (RadioButton) mView.findViewById(R.id.rb_contacts);
        rbAttention.setVisibility(View.GONE);
        rbContacts.setVisibility(View.GONE);
        mView.findViewById(R.id.add_friends).setOnClickListener(this);
        //RadioGroup选中状态改变监听
        mGroup.setOnCheckedChangeListener(new myCheckChangeListener());
    }

    private void initViewPager() {
        fragmentList = new ArrayList<>();
        ContactsFragment contactsFragment = new ContactsFragment();
        fragmentList.add(contactsFragment);
        //AttentionFragment attentionFragment = new AttentionFragment(); // wystan disable attention 210225
        //fragmentList.add(attentionFragment);
        //ViewPager设置适配器
        mPager.setAdapter(new myFragmentPagerAdapter(getChildFragmentManager(), fragmentList));
        //ViewPager显示第一个Fragment
        mPager.setCurrentItem(0);
        //ViewPager页面切换监听
        mPager.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
//                mGroup.check(R.id.rb_contacts);
                rbContacts.setChecked(true);
                break;
            case 1:
                rbAttention.setChecked(true);
//                mGroup.check(R.id.rb_attention);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * RadioButton切换Fragment
     */
    private class myCheckChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.rb_attention:
                    //ViewPager显示第一个Fragment且关闭页面切换动画效果
                    mPager.setCurrentItem(1);
                    break;
                case R.id.rb_contacts:
                    mPager.setCurrentItem(0);
                    break;
            }
        }
    }

    public class myFragmentPagerAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> list;

        public myFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_friends:
                Intent intent = new Intent(getActivity(), FriendsActivity.class);
                startActivity(intent);
                break;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(PriorityEvent event) {
        switch (event.event) {
            case MSG_SYSTEM_DATA:
                IMSystem.IMSysMsgData sysMsg = null;
                IMSystem.IMSendSysMsgRsp imSendSysMsgRsp = null;
                if (event.object instanceof IMSystem.IMSendSysMsgRsp) {
                    imSendSysMsgRsp = (IMSystem.IMSendSysMsgRsp) event.object;
                    sysMsg = imSendSysMsgRsp.getSysMsg();
                } else if (event.object instanceof IMSystem.IMSysMsgData) {
                    sysMsg = (IMSystem.IMSysMsgData) event.object;
                }
                IMSystem.SysMsgOper type = sysMsg.getType();
                String fromId = sysMsg.getFromId();
                String loginId = IMLoginManager.instance().getPub_key();
                switch (type) {
                    case ADD_FRIEND_REQUEST:
                    case ADD_GROUP_REQUEST:
                        if (fromId.equals(loginId)) {
                            //我请求别人的请求结果
//                            if(imSendSysMsgRsp.getResultCode()==0)
//                                Toast.makeText(getActivity(),"好友请求发送成功，等待对方确认",Toast.LENGTH_LONG).show();
                        } else {
                            //别人请求加我
                            int i = ((MainActivity) getActivity()).getLocalUreadCnt(2) + 1;
                            ((MainActivity) getActivity()).setNewContact(i);
                        }
                        break;
                    case ADD_FRIEND_AGREE:
                        int agreeStates = 0;
                        if (fromId.equals(loginId)) {
                            //自己同意添加别人好友
                            if (imSendSysMsgRsp.getResultCode() == 0) {
                                Toast.makeText(getActivity(), "同意消息发送成功", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            //对方同意添加好友
                            agreeStates = 3;
                            int cnt = ((MainActivity) getActivity()).getLocalUreadCnt(2) + 1;
                            ((MainActivity) getActivity()).setNewContact(cnt);
                            try {
                                String date = sysMsg.getAttachData().toStringUtf8();
                                final JSONObject object = new JSONObject(date);
                                //保存到本地数据库

//                                if ("".equals(group_id)) {
                                RequesterEntity requesterEntity = new RequesterEntity();
                                requesterEntity.setFromUserId(fromId);
                                requesterEntity.setAddition_msg(object.optString("addition_msg"));
                                requesterEntity.setAvatar_url(object.optString("avatar_url"));
                                requesterEntity.setNick_name(object.optString("nick_name"));
                                requesterEntity.setIsRead(true);
                                requesterEntity.setAgree_states(agreeStates);
                                DBInterface.instance().batchInsertOrUpdateRquest(requesterEntity);
                                IMContactManager.instance().reqGetAllUsers(0);
//                                } else {
//                                    GroupRequesterEntity requesterEntity = new GroupRequesterEntity();
//                                    requesterEntity.setFromUserId(fromId);
//                                    requesterEntity.setAddition_msg(object.optString("addition_msg"));
//                                    requesterEntity.setAvatar_url(object.optString("avatar_url"));
//                                    requesterEntity.setNick_name(object.optString("nick_name"));
//                                    requesterEntity.setIsRead(true);
//                                    requesterEntity.setAgree_states(agreeStates);
//                                    DBInterface.instance().batchInsertOrUpdateRquest(requesterEntity);
//                                }

                                //更新userEnitity数据库
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case ADD_GROUP_AGREE:
                        if (!fromId.equals(loginId)) {
                            int cnt = ((MainActivity) getActivity()).getLocalUreadCnt(2) + 1;
                            ((MainActivity) getActivity()).setNewContact(cnt);
                            String msg = sysMsg.getAttachData().toStringUtf8();
                            try {
                                JSONObject groupAgreeMsg = new JSONObject(msg);
                                String group_id = groupAgreeMsg.optString("group_id");
                                GroupRequesterEntity requesterEntity = new GroupRequesterEntity();
                                requesterEntity.setFromUserId(fromId);
                                requesterEntity.setGroup_id(group_id);
                                requesterEntity.setAddition_msg(groupAgreeMsg.optString("addition_msg"));
                                requesterEntity.setAvatar_url(groupAgreeMsg.optString("avatar_url"));
                                requesterEntity.setNick_name(groupAgreeMsg.optString("nick_name"));
                                requesterEntity.setIsRead(true);
                                requesterEntity.setAgree_states(3);
                                DBInterface.instance().batchInsertOrUpdateRquest(requesterEntity);
                                IMGroupManager.instance().reqGroupDetailInfo(group_id);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
        }
    }
}