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
import com.tenth.space.DB.entity.SysPushMsgEntity;
import com.tenth.space.R;
import com.tenth.space.app.IMApplication;
import com.tenth.space.imservice.manager.IMBuddyManager;
import com.tenth.space.imservice.manager.IMContactManager;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.protobuf.IMSystem;
import com.tenth.space.utils.ImageLoaderUtil;

import java.util.List;

import static com.tenth.space.R.id.add_msg;
import static com.tenth.space.R.id.agree;
import static com.tenth.space.R.id.nick_name;
import static com.tenth.space.R.id.state;
import static com.tenth.space.protobuf.IMSystem.SysMsgOper.ADD_FRIEND_AGREE;
import static com.tenth.space.protobuf.IMSystem.SysMsgOper.ADD_GROUP_AGREE;

/**
 * Created by Administrator on 2016/11/21.
 */

public class SysPushMsgAdapter extends BaseAdapter {
    private List<SysPushMsgEntity> friendsList;
    private Context context;

    public SysPushMsgAdapter(List<SysPushMsgEntity> friendsList, Context context) {
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
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.add_friend_item_contact, null);
            viewHolder.photo = (ImageView) view.findViewById(R.id.phto);
            viewHolder.nick_name = (TextView) view.findViewById(nick_name);
            viewHolder.add_msg = (TextView) view.findViewById(add_msg);
            viewHolder.agree = (TextView) view.findViewById(agree);
            viewHolder.state = (TextView) view.findViewById(state);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        final SysPushMsgEntity sysPushMsgEntity = friendsList.get(position);

        ImageLoaderUtil.instance().displayImage(IMApplication.app.UrlFormat(sysPushMsgEntity.getAvatar_url()), viewHolder.photo, ImageLoaderUtil.getAvatarOptions(0, 0));
        viewHolder.nick_name.setText(sysPushMsgEntity.getNick_name());
        viewHolder.add_msg.setText(sysPushMsgEntity.getAddition_msg());
        final ViewHolder finalViewHolder = viewHolder;
        if (sysPushMsgEntity.getAgree_states() == 1) {
            //??????2?????????????????????
            finalViewHolder.agree.setVisibility(View.VISIBLE);
            finalViewHolder.state.setVisibility(View.GONE);
            finalViewHolder.agree.setText("??????");
        } else if (sysPushMsgEntity.getAgree_states() == 2) {
            //??????2?????????????????????
            finalViewHolder.agree.setVisibility(View.GONE);
            finalViewHolder.state.setVisibility(View.VISIBLE);
            finalViewHolder.state.setText("?????????");
        } else if (sysPushMsgEntity.getAgree_states() == 3) {
            //??????3???????????????
            finalViewHolder.agree.setVisibility(View.GONE);
            finalViewHolder.state.setVisibility(View.VISIBLE);
            finalViewHolder.state.setText("?????????");
        } else if (sysPushMsgEntity.getAgree_states() == 4) {
            finalViewHolder.agree.setVisibility(View.GONE);
            finalViewHolder.state.setVisibility(View.VISIBLE);
            finalViewHolder.state.setText("?????????");
        } else {
            finalViewHolder.agree.setVisibility(View.VISIBLE);
            finalViewHolder.state.setVisibility(View.GONE);
        }
        viewHolder.agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "??????????????????", Toast.LENGTH_SHORT).show();
                String msg = "??????";
                IMSystem.SysMsgOper type = null;
                switch (sysPushMsgEntity.getMsg_type()) {
                    case 1:
                        type = ADD_FRIEND_AGREE;
                        break;
                    case 2:
                        type = ADD_GROUP_AGREE;
                        break;
                    default:
                        break;
                }
                IMBuddyManager.instance().agreeFriend(IMLoginManager.instance().getPub_key(), sysPushMsgEntity.getFromUserId(), type, msg);
                sysPushMsgEntity.setAgree_states(2);
                DBInterface.instance().setRequestAgreeState(sysPushMsgEntity);
                finalViewHolder.agree.setVisibility(View.GONE);
                finalViewHolder.state.setVisibility(View.VISIBLE);
                finalViewHolder.state.setText("?????????");
                //????????????????????????????????????
                IMContactManager.instance().reqGetAllUsers(0);
//                userEntity.setId((long) sysPushMsgEntity.getFromUserId());
//                userEntity.setMainName(sysPushMsgEntity.getNick_name());
//                userEntity.setAvatar(sysPushMsgEntity.getAvatar_url());
//                userEntity.setRelation(IMBaseDefine.UserRelationType.RELATION_FRIEND.name());
//                ((NewFriendsActivity)context).setUserInfo(sysPushMsgEntity);
            }
        });
        return view;
    }

    class ViewHolder {
        ImageView photo;
        TextView nick_name;
        TextView add_msg;
        TextView agree;
        TextView disagree;
        TextView state;
    }
}
