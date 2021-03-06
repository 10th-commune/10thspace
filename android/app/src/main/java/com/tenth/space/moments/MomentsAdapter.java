package com.tenth.space.moments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tenth.space.DB.entity.BlogEntity;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.R;
import com.tenth.space.config.IntentConstant;
import com.tenth.space.imservice.entity.BlogMessage;
import com.tenth.space.imservice.event.BlogInfoEvent;
import com.tenth.space.imservice.manager.IMBuddyManager;
import com.tenth.space.imservice.manager.IMContactManager;
import com.tenth.space.ui.activity.DetailPortraitActivity;
import com.tenth.space.utils.IMUIHelper;
import com.tenth.space.utils.ImageLoaderUtil;
import com.tenth.space.utils.LogUtils;
import com.tenth.space.utils.ToastUtils;
import com.tenth.space.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.sharesdk.onekeyshare.OnekeyShare;
import de.greenrobot.event.EventBus;

/**
 * Created by wsq on 2016/11/2.
 */

public class MomentsAdapter extends RecyclerView.Adapter {
    private final boolean IsShowFlow;
    Context context;
    private List<BlogEntity> mData = new ArrayList<>();
    private PopupWindow popupWindow;

    public void setData(List<BlogEntity> data) {
        mData = data;
        this.notifyDataSetChanged();
    }

    public Context getContext() {
        return context;
    }

    public List<BlogEntity> getData() {
        return mData;
    }

    public MomentsAdapter(Context context,boolean IsShowFlow) {
        this.context = context;
        this.IsShowFlow=IsShowFlow;
        EventBus.getDefault().register(this);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = View.inflate(context, R.layout.item_moments_adapter, null);

        CardView cardView = new CardView(context);

//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
//        layoutParams.bottomMargin = Utils.dip2px(context, 10);
//        layoutParams.topMargin = Utils.dip2px(context, 10);
//        cardView.setLayoutParams(layoutParams);
//        cardView.setRadius(Utils.dip2px(context, 5));

        cardView.setCardElevation(1);
        cardView.addView(inflate);

        return new MyViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof MyViewHolder) {
            JSONArray jsonArray = null;
            String blogText = "";

            //??????????????????(BlogMessage/BlogEntity)???????????????
            if (mData.get(position) instanceof BlogMessage) {
                BlogMessage blogMessage = (BlogMessage) mData.get(position);

                blogText = blogMessage.getBlogText();
                jsonArray = new JSONArray(blogMessage.getUrlList());
            } else {
                blogText = mData.get(position).getBlogText();
                jsonArray = Utils.string2JsonArray(mData.get(position).getBlogImages());
            }
            //??????????????????
           // ((MyViewHolder) holder).mCommentCnt.setText(mData.get(position).getCommentCnt() + "");
           // ((MyViewHolder) holder).mZanCnt.setText(mData.get(position).getLikeCnt() + "");
            //????????????(???????????????url)?????????????????????????????????
           ImageLoaderUtil.instance().displayImage(mData.get(position).getAvatarUrl()+"?x-oss-process=image/resize,m_fill,h_100,w_100", ((MyViewHolder) holder).mIvHeadIcon, ImageLoaderUtil.getAvatarOptions(10, 0));
           // ImageLoaderUtil.instance().displayImage(IMApplication.app.UrlFormat(mData.get(position).getAvatarUrl()), ((MyViewHolder) holder).mIvHeadIcon, ImageLoaderUtil.getAvatarOptions(10, 0));
            //??????
            ((MyViewHolder) holder).mTvName.setText(mData.get(position).getNickName());
            //????????????
            String model = Utils.getPhoneModel();
            ((MyViewHolder) holder).mTvMyPhone.setText("??????" + model + "??????");
            //??????
            long created = mData.get(position).getCreated();
            Date date = new Date(created * 1000L);
            LogUtils.d("date:" + date.toString());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");//yyyy-MM-dd HH:mm:ss
            String format = simpleDateFormat.format(date);
            ((MyViewHolder) holder).mTvTime.setText(format);
            //??????
            ((MyViewHolder) holder).mTvContent.setText(blogText);

            //????????????GridView(?????? ??????/???????????? ???????????????)
           /// ((MyViewHolder) holder).mGvImages.setOnScrollListener(ImageLoaderUtil.getPauseOnScrollLoader());
            if (jsonArray != null) {
                if (jsonArray.length() > 0) {
                    ((MyViewHolder) holder).mGvImages.setVisibility(View.VISIBLE);
                } else {
                    ((MyViewHolder) holder).mGvImages.setVisibility(View.GONE);
                }
                NineGridLrvAdapter2 nineGridLrvAdapter2 = new NineGridLrvAdapter2(context, jsonArray);
                ((MyViewHolder) holder).mGvImages.setAdapter(nineGridLrvAdapter2);
                final JSONArray finalJsonArray = jsonArray;
                ((MyViewHolder) holder).mGvImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //??????????????????
                        Intent intent = new Intent(context, DetailPortraitActivity.class);
                        intent.putExtra(IntentConstant.KEY_AVATAR_URL, finalJsonArray.optString(position));
                        intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, true);
                        context.startActivity(intent);
                    }
                });

            }
            if(IsShowFlow&&mData.get(position).isShowButton){
                ((MyViewHolder) holder).mtvPulldown.setVisibility(View.VISIBLE);
                //??????????????????
                if (mData.get(position).isFollow == 1) {
                    ((MyViewHolder) holder).mtvPulldown.setSelected(true);
                    ((MyViewHolder) holder).mtvPulldown.setText("-??????");
                } else {
                    ((MyViewHolder) holder).mtvPulldown.setSelected(false);
                    ((MyViewHolder) holder).mtvPulldown.setText("+??????");
                }
            }else {
                ((MyViewHolder) holder).mtvPulldown.setVisibility(View.INVISIBLE);
            }

            final Long writerUserId = mData.get(position).getWriterUserId();
            ((MyViewHolder) holder).mtvPulldown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mData.get(position).isFollow == 0) {
                      // IMBuddyManager.instance().reqFollowUser(writerUserId, position);
                    } else {
                    //   IMBuddyManager.instance().reqDelFollowUser(writerUserId, position);
                    }
                }
            });

            //???????????????
            ((MyViewHolder) holder).mComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, CommentActivity.class);
                    intent.putExtra("BlogEntity", mData.get(position));
                    intent.putExtra("position", position);
                    intent.putExtra("IsShowFlow", IsShowFlow&&mData.get(position).isShowButton);
                    context.startActivity(intent);
                }
            });
            //??????
            ((MyViewHolder) holder).mTranspond.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  //  ToastUtils.show("??????");
                    showShare(mData.get(position));
                }
            });
            //??????
            ((MyViewHolder) holder).mZan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    ToastUtils.show("??????");
                    int cnt = mData.get(position).getLikeCnt() + 1;
                    mData.get(position).setLikeCnt(cnt);
                    MomentsAdapter.this.notifyItemChanged(position);
                }
            });

            ((MyViewHolder) holder).mIvHeadIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long writerUserId1 = mData.get(position).getWriterUserId();
                    UserEntity userEntity = IMContactManager.instance().findContact( "writerUserId1");
                    String relation= "";
                    if(userEntity !=null){
                        relation = userEntity.getRelation();
                    }
                    //IMUIHelper.openUserProfileActivity(context, (int) writerUserId1,relation);
                }
            });
            //??????
            ((MyViewHolder) holder).iv_report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //???????????????
                    long writerUserId1 = mData.get(position).getWriterUserId();
                    Utils.showPopupWindow((Activity) context,writerUserId1,10);
                }
            });

        }
    }

    private void showShare(BlogEntity blogEntity) {
        OnekeyShare oks = new OnekeyShare();
        //??????sso??????
        oks.disableSSOWhenAuthorize();
        String s1 = "http://mobile.10thcommune.com/blog/" + blogEntity.getBlogId()+"/tenth";
        // title???????????????????????????????????????????????????????????????QQ???QQ????????????
        oks.setTitle("????????????");
        // titleUrl?????????????????????????????????Linked-in,QQ???QQ????????????
        oks.setTitleUrl(s1);
        // text???????????????????????????????????????????????????
        oks.setText(blogEntity.getBlogText());
        //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        JSONArray jsonArray = Utils.string2JsonArray(blogEntity.getBlogImages());
        String s = null;
        try {
            s = jsonArray.get(0).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        oks.setImageUrl(s);
        // imagePath???????????????????????????Linked-In?????????????????????????????????
        //oks.setImagePath("/sdcard/test.jpg");//??????SDcard????????????????????????
        // url???????????????????????????????????????????????????

        oks.setUrl(s1);
        // comment???????????????????????????????????????????????????QQ????????????
        oks.setComment("????????????????????????");
        // site??????????????????????????????????????????QQ????????????
        oks.setSite("????????????");
        // siteUrl??????????????????????????????????????????QQ????????????

        oks.setSiteUrl(s1);

// ????????????GUI
        oks.show(context);
    }
    public void onEventMainThread(BlogInfoEvent event) {
        switch (event.getEvent()) {
            case FOLLOW_SUCCESS:
               // if (mData.size()>0){ }???????????????????????????????????????
                    mData.get(event.position).isFollow = 1;

                this.notifyItemChanged(event.position);
                LogUtils.d("?????????????????????UI");
                break;

            case DEL_FOLLOW_SUCCESS:
                    mData.get(event.position).isFollow = 0;
                //??????????????????????????????????????????
                this.notifyItemChanged(event.position);
                LogUtils.d("???????????????????????????UI");

                break;
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_head_icon)
        ImageView mIvHeadIcon;
        @BindView(R.id.tv_name)
        TextView mTvName;
        @BindView(R.id.tv_time)
        TextView mTvTime;
        @BindView(R.id.tv_my_phone)
        TextView mTvMyPhone;

        @BindView(R.id.tv_pulldown)
        TextView mtvPulldown;
        @BindView(R.id.tv_content)
        TextView mTvContent;
        @BindView(R.id.gv_images)
        MyGridView mGvImages;
        @BindView(R.id.transpond)
        LinearLayout mTranspond;
        @BindView(R.id.comment)
        LinearLayout mComment;
        @BindView(R.id.comment_cnt)
        TextView mCommentCnt;
        @BindView(R.id.zan)
        LinearLayout mZan;
        @BindView(R.id.zan_cnt)
        TextView mZanCnt;
        @BindView(R.id.iv_report)
        ImageView iv_report;

        MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
