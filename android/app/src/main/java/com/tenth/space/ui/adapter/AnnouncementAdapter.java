package com.tenth.space.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.R;
import com.tenth.space.config.IntentConstant;
import com.tenth.space.imservice.manager.IMContactManager;
import com.tenth.space.ui.activity.UserInfoActivity;
import com.tenth.space.utils.ImageLoaderUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/29.
 */

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.MyViewHolder> {
    private ArrayList<Map.Entry<Integer, Integer>> studytimeListList = new ArrayList<>();
    private Context context;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//初始化Formatter的转换格式。

    public AnnouncementAdapter() {
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_announcement, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        /*
        Map.Entry<Integer, Integer> integerIntegerEntry = studytimeListList.get(position);
        Integer id = integerIntegerEntry.getKey();
        Integer duration = integerIntegerEntry.getValue();
        final UserEntity contact = IMContactManager.instance().findContact(id);
        holder.studyDuration.setText(duration / 3600 + "小时" + duration % 3600 / 60 + "分钟" + duration % 3600 % 60 + "秒");
        String mainName;
        if (null == contact) {
            mainName="手机用户";
        }else {
            mainName = contact.getMainName();
            ImageLoader.getInstance().displayImage(contact.getAvatar(), holder.photo, ImageLoaderUtil.getAvatarOptions(5, 0));
        }
            holder.name.setText(mainName);
            holder.someBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, UserInfoActivity.class);
                    intent.putExtra(IntentConstant.KEY_PEERID, contact.getPub_key());
                    intent.putExtra("relation",contact.getRelation());
                    context.startActivity(intent);
                }
            });

         */
    }

    @Override
    public int getItemCount() {
        return studytimeListList.size();
    }

    public AnnouncementAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<Map.Entry<Integer, Integer>> tList) {
        studytimeListList.clear();
        studytimeListList.addAll(tList);
        notifyDataSetChanged();
    }

    public void addData(List<Map.Entry<Integer, Integer>> List) {
        studytimeListList.addAll(List);
        notifyDataSetChanged();
    }

    public void clear() {
        studytimeListList.clear();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private View someBody;
        private TextView name;
        private TextView startTime;
        private TextView studyDuration;
        private ImageView photo;

        public MyViewHolder(View itemView) {
            super(itemView);
            photo = (ImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.name);
            startTime = (TextView) itemView.findViewById(R.id.start);
            studyDuration = (TextView) itemView.findViewById(R.id.duration);
            someBody = itemView.findViewById(R.id.someBody);
        }
    }
}
