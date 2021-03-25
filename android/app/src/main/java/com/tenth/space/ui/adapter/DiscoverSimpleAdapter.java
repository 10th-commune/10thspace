package com.tenth.space.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.tenth.space.DB.entity.GroupEntity;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.R;
import com.tenth.space.config.DBConstant;
import com.tenth.space.config.SysConstant;
import com.tenth.space.imservice.service.IMService;
import com.tenth.space.ui.widget.IMBaseImageView;
import com.tenth.space.ui.widget.IMGroupAvatar;
import com.tenth.space.utils.IMUIHelper;
import com.tenth.space.utils.Logger;
import com.tenth.space.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class DiscoverSimpleAdapter extends BaseAdapter implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener{

    private Logger logger = Logger.getLogger(ContactAdapter.class);
    public List<GroupEntity> groupList = new ArrayList<>();
    public List<UserEntity> userList = new ArrayList<>();

    private Context ctx;
    private IMService imService;
    public DiscoverSimpleAdapter(Context context){
        this.ctx = context;
    }
    public DiscoverSimpleAdapter(Context context,IMService imService){
        this.ctx = context;
        this.imService = imService;
    }

    public void putUserList(List<UserEntity> pUserList){
        this.userList.clear();
        if(pUserList == null || pUserList.size() <=0){
            return;
        }
        this.userList = pUserList;
        notifyDataSetChanged();
    }

    public void putGroupList(List<GroupEntity> pGroupList){
        this.groupList.clear();
        this.groupList = pGroupList;
        notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object object =  getItem(position);
        if(object instanceof UserEntity){
            UserEntity userEntity = (UserEntity) object;
            IMUIHelper.openUserProfileActivity(ctx, userEntity.getPeerId(),userEntity.getRelation());
        }else if(object instanceof GroupEntity){
            GroupEntity groupEntity = (GroupEntity) object;

            String loginId = imService.getLoginManager().getLoginId();
            if(groupEntity.getGroupType() == DBConstant.GROUP_TYPE_ACTIVE){// && !groupEntity.getlistGroupMemberIds().contains(loginId) ) {
                Set<String> checkListSet = new HashSet<>();
                checkListSet.add(loginId);
                imService.getGroupManager().reqAddGroupMember(groupEntity.getPeerId(), checkListSet);
            }

            IMUIHelper.openChatActivity(ctx,groupEntity.getSessionKey());
        }else{
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Object object =  getItem(position);
        if(object instanceof UserEntity){
            UserEntity contact = (UserEntity) object;
            IMUIHelper.handleContactItemLongClick(contact, ctx);
        }else{
        }
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        int groupSize = groupList==null?0:groupList.size();
        if(groupSize > position){
            return ContactType.GROUP.ordinal();
        }
        return ContactType.USER.ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return ContactType.values().length;
    }

    @Override
    public int getCount() {
        int groupSize = groupList==null?0:groupList.size();
        int userSize = userList==null?0:userList.size();
        int sum = groupSize + userSize;
        return sum;
    }
    public List<UserEntity> getUserList(){
        return userList;
    }

    @Override
    public Object getItem(int position) {
        int typeIndex =  getItemViewType(position);
        ContactType renderType = ContactType.values()[typeIndex];
        switch (renderType){
            case USER:{
                int groupSize = groupList==null?0:groupList.size();
                int realIndex = position - groupSize;
                if(realIndex <0){
                    throw new IllegalArgumentException("ContactAdapter#getItem#user类型判断错误!");
                }
                return userList.get(realIndex);
            }
            case GROUP:{
                int groupSize = groupList==null?0:groupList.size();
                if(position > groupSize){
                    throw new IllegalArgumentException("ContactAdapter#getItem#group类型判断错误");
                }
                return groupList.get(position);
            }
            default:
                throw new IllegalArgumentException("ContactAdapter#getItem#不存在的类型" + renderType.name());
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int typeIndex =  getItemViewType(position);
        ContactType renderType = ContactType.values()[typeIndex];
        View view = null;
        switch (renderType){
            case USER:{
                view = renderUser(position,convertView,parent);
            }
            break;
            case GROUP:{
                view = renderGroup(position,convertView,parent);
            }
            break;
        }
        return view;
    }


    public View renderUser(int position, View view, ViewGroup parent){
        UserHolder userHolder = null;
        UserEntity  userEntity= (UserEntity)getItem(position);
        if(userEntity == null){
            logger.e("ContactAdapter#renderUser#userEntity is null!position:%d",position);
            // todo 这个会报错误的，怎么处理
            return null;
        }
        if (view == null) {
            userHolder = new UserHolder();
            view = LayoutInflater.from(ctx).inflate(R.layout.tt_item_contact, parent,false);
            userHolder.nameView = (TextView) view.findViewById(R.id.contact_item_title);
            userHolder.realNameView = (TextView) view.findViewById(R.id.contact_realname_title);
            userHolder.sectionView = (TextView) view.findViewById(R.id.contact_category_title);
            userHolder.avatar = (IMBaseImageView)view.findViewById(R.id.contact_portrait);
            userHolder.divider = view.findViewById(R.id.contact_divider);
            view.setTag(userHolder);
        } else {
            userHolder = (UserHolder) view.getTag();
        }

        /***reset-- 控件的默认值*/
        userHolder.nameView.setText(userEntity.getMainName());
        userHolder.avatar.setImageResource(R.drawable.tt_default_user_portrait_corner);
        userHolder.divider.setVisibility(View.VISIBLE);
        userHolder.sectionView.setVisibility(View.GONE);

        // 字母序第一个要展示
        // todo pinyin控件不能处理多音字的情况，或者UserEntity类型的统统用pinyin字段进行判断
        String sectionName = userEntity.getSectionName();
        // 正式群在用户列表的上方展示

        int groupSize = groupList == null?0:groupList.size();
        if (position == groupSize) {
           // userHolder.sectionView.setVisibility(View.VISIBLE);
           // userHolder.sectionView.setText(sectionName);

            //分栏已经显示，最上面的分割线不用显示
            userHolder.divider.setVisibility(View.GONE);
        }
         /*
        else{
            // 获取上一个实体的preSectionName,这个时候position > groupSize
            UserEntity preUser =  (UserEntity)getItem(position-1);
            String preSectionName = preUser.getSectionName();
            if(TextUtils.isEmpty(preSectionName) || !preSectionName.equals(sectionName)){
                userHolder.sectionView.setVisibility(View.VISIBLE);
                userHolder.sectionView.setText(sectionName);
                // 不显示分割线
                userHolder.divider.setVisibility(View.GONE);
            }else{
                userHolder.sectionView.setVisibility(View.GONE);
            }
        }

         */

        userHolder.sectionView.setVisibility(View.GONE);

        userHolder.avatar.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        userHolder.avatar.setCorner(0);
        logger.i("neilimg10");
        userHolder.avatar.setImageUrl(userEntity.getAvatar());

        userHolder.realNameView.setText(userEntity.getRealName());
        userHolder.realNameView.setVisibility(View.GONE);
        return view;
    }


    public View renderGroup(int position, View view, ViewGroup parent){
        GroupHolder groupHolder = null;
        GroupEntity groupEntity = (GroupEntity) getItem(position);
        if(groupEntity == null){
            logger.e("ContactAdapter#renderGroup#groupEntity is null!position:%d",position);
            return null;
        }
        if (view == null) {
            groupHolder = new GroupHolder();
            view = LayoutInflater.from(ctx).inflate(R.layout.tt_item_contact_group, parent,false);
            groupHolder.nameView = (TextView) view.findViewById(R.id.contact_item_title);
            groupHolder.sectionView = (TextView) view.findViewById(R.id.contact_category_title);
            groupHolder.avatar = (IMGroupAvatar)view.findViewById(R.id.contact_portrait);
            groupHolder.divider = view.findViewById(R.id.contact_divider);
            view.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) view.getTag();
        }

        groupHolder.nameView.setText(groupEntity.getMainName());
        groupHolder.sectionView.setVisibility(View.GONE);

        // 分割线的处理【位于控件的最上面】
        groupHolder.divider.setVisibility(View.VISIBLE);
//        if(position  == 0){
//            groupHolder.divider.setVisibility(View.GONE);
//        }

        groupHolder.avatar.setVisibility(View.VISIBLE);
        List<String> avatarUrlList = new ArrayList<>();
        Set<String> userIds = groupEntity.getlistGroupMemberIds();
        int i = 0;
        /*
        for(Integer buddyId:userIds){
            UserEntity entity = imService.getContactManager().findContact(buddyId);
            if (entity == null) {
                //logger.i("已经离职。userId:%d", buddyId);
                continue;
            }
            avatarUrlList.add(entity.getAvatar());
            if (i >= 3) {
                break;
            }
            i++;
        }

         */
        setGroupAvatar(groupHolder.avatar,avatarUrlList);
        return view;
    }


    /**
     * 与search 有公用的地方，可以抽取IMUIHelper
     * 设置群头像
     * @param avatar
     * @param avatarUrlList
     */
    private void setGroupAvatar(IMGroupAvatar avatar,List<String> avatarUrlList){
        try {
            avatar.setViewSize(ScreenUtil.instance(ctx).dip2px(38));
            avatar.setChildCorner(2);
            avatar.setAvatarUrlAppend(SysConstant.AVATAR_APPEND_32);
            avatar.setParentPadding(3);
            avatar.setAvatarUrls((ArrayList<String>) avatarUrlList);
        }catch (Exception e){
            logger.e(e.toString());
        }
    }


    // 将分割线放在上面，利于判断
    public static class UserHolder {
        View divider;
        TextView sectionView;
        TextView nameView;
        TextView realNameView;
        IMBaseImageView avatar;
    }

    public static class GroupHolder {
        View divider;
        TextView sectionView;
        TextView nameView;
        IMGroupAvatar avatar;
    }

    private enum ContactType{
        USER,
        GROUP
    }
}
