package com.tenth.space.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tenth.space.DB.DBInterface;
import com.tenth.space.DB.entity.GroupRequesterEntity;
import com.tenth.space.R;
import com.tenth.space.app.IMApplication;
import com.tenth.space.imservice.manager.IMBuddyManager;
import com.tenth.space.imservice.manager.IMGroupManager;
import com.tenth.space.protobuf.IMSystem;
import com.tenth.space.ui.activity.NewFriendsActivity;
import com.tenth.space.utils.ImageLoaderUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.tenth.space.R.id.add_msg;
import static com.tenth.space.R.id.agree;
import static com.tenth.space.R.id.nick_name;
import static com.tenth.space.R.id.state;

/**
 * Created by Administrator on 2016/11/21.
 */

public class NewGroupAdapter extends BaseAdapter {
    private List<GroupRequesterEntity>friendsList;
    private Context context;
    public NewGroupAdapter(List<GroupRequesterEntity>friendsList, Context context){
        this.friendsList = friendsList;
        this.context = context;
    }
    @Override
    public int getCount() {
        return friendsList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder=null;
        if(view==null){
            viewHolder=new ViewHolder();
            view= LayoutInflater.from(context).inflate(R.layout.add_friend_item_contact,null);
            viewHolder.photo = (ImageView) view.findViewById(R.id.phto);
            viewHolder.nick_name = (TextView) view.findViewById(nick_name);
            viewHolder.add_msg = (TextView) view.findViewById(add_msg);
            viewHolder.agree = (TextView) view.findViewById(agree);
            viewHolder.state = (TextView) view.findViewById(state);

            view.setTag(viewHolder);
        }else {
           viewHolder = (ViewHolder) view.getTag();
        }
        final GroupRequesterEntity requesterEntity = friendsList.get(position);

        ImageLoaderUtil.instance().displayImage(IMApplication.app.UrlFormat(requesterEntity.getAvatar_url()),viewHolder.photo,ImageLoaderUtil.getAvatarOptions(0,0));
        viewHolder.nick_name.setText(requesterEntity.getNick_name());
        viewHolder.add_msg.setText(requesterEntity.getAddition_msg());
        final ViewHolder finalViewHolder = viewHolder;
        if(requesterEntity.getAgree_states()==2){
            //状态2当前用户已同意
            finalViewHolder.agree.setVisibility(View.GONE);
            finalViewHolder.state.setVisibility(View.VISIBLE);
            finalViewHolder.state.setText("已添加");
        }else if(requesterEntity.getAgree_states()==3) {
            //状态3对方已同意
            finalViewHolder.agree.setVisibility(View.GONE);
            finalViewHolder.state.setVisibility(View.VISIBLE);
            finalViewHolder.state.setText("已同意");
        }else if(requesterEntity.getAgree_states()==4){
            finalViewHolder.agree.setVisibility(View.GONE);
            finalViewHolder.state.setVisibility(View.VISIBLE);
            finalViewHolder.state.setText("已请求");
        }else {
            finalViewHolder.agree.setVisibility(View.VISIBLE);
            finalViewHolder.state.setVisibility(View.GONE);
        }
        viewHolder.agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"已发送同意消息",Toast.LENGTH_SHORT).show();
                String msg="欢迎加入";
                IMBuddyManager.instance().agreeFriend(requesterEntity.getGroup_id(),requesterEntity.getFromUserId(), IMSystem.SysMsgOper.ADD_GROUP_AGREE,msg);
//                UserEntity userEntity =new UserEntity();
                requesterEntity.setAgree_states(2);
                DBInterface.instance().setRequestAgreeState(requesterEntity);
                finalViewHolder.agree.setVisibility(View.GONE);
                finalViewHolder.state.setVisibility(View.VISIBLE);
                finalViewHolder.state.setText("已同意");
                Set<String> checkListSet= new HashSet<String>();
                checkListSet.add(requesterEntity.getFromUserId());
                IMGroupManager.instance().reqAddGroupMember(requesterEntity.getGroup_id(), checkListSet);
                //同意添加好友，同步数据库
//                IMContactManager.instance().reqGetAllUsers(0);
//                userEntity.setId((long) requesterEntity.getFromUserId());
//                userEntity.setMainName(requesterEntity.getNick_name());
//                userEntity.setAvatar(requesterEntity.getAvatar_url());
//                userEntity.setRelation(IMBaseDefine.UserRelationType.RELATION_FRIEND.name());
                ((NewFriendsActivity)context).setUserInfo(requesterEntity);
            }
        });
        return view;
    }
    class ViewHolder{
        ImageView photo;
        TextView nick_name;
        TextView add_msg;
        TextView agree;
        TextView disagree;
        TextView state;
    }
}
