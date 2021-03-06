package com.tenth.space.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.interfaces.OnLoadMoreListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.github.jdsjlzx.util.RecyclerViewStateUtils;
import com.github.jdsjlzx.view.LoadingFooter;
import com.tenth.space.DB.DBInterface;
import com.tenth.space.DB.entity.BlogEntity;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.R;
import com.tenth.space.app.IMApplication;
import com.tenth.space.imservice.entity.BlogMessage;
import com.tenth.space.imservice.event.BlogInfoEvent;
import com.tenth.space.imservice.event.CountEvent;
import com.tenth.space.imservice.manager.IMBlogManager;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.imservice.manager.IMSocketManager;
import com.tenth.space.imservice.service.IMService;
import com.tenth.space.imservice.support.IMServiceConnector;
import com.tenth.space.moments.CommentActivity;
import com.tenth.space.moments.MomentsAdapter;
import com.tenth.space.moments.MomentsItemDecoration;
import com.tenth.space.protobuf.IMBaseDefine;
import com.tenth.space.utils.LogUtils;
import com.tenth.space.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.sharesdk.onekeyshare.OnekeyShare;
import de.greenrobot.event.EventBus;

public class InternalFragment extends Fragment {

    @BindView(R.id.lrv_internal)
    LRecyclerView mLrvInternal;
    @BindView(R.id.ll_progress_bar)
    LinearLayout ll_progress_bar;
    @BindView(R.id.tv_netok)
    TextView tv_netok;
    private View curView = null;
    IMService imService;
    private MomentsAdapter mMomentsAdapter;
   // private UserEntity mLoginInfo;
    private TextView mUser_name_head;
    private ImageView mUser_bgpic_head;
    private ImageView mUser_icon_head;
    public List<BlogEntity> globalList=new ArrayList<>();
    public int pager=0;
    private List<UserEntity> DBlists;//?????????????????????????????????????????????
//    Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case 0:
//                    if (mLoginInfo != null) {
//                        mUser_name_head.setText(mLoginInfo.getMainName());
//                    }
//                    break;
//                case 1://???????????????
//                   //  DBlists = (List<UserEntity>) msg.obj;
//                  //  Log.i("GTAG","Dblist.size="+DBlists.size());
//                  //  IMBlogManager.instance().reqBlogList(IMBaseDefine.BlogType.BLOG_TYPE_RCOMMEND,pager);
//                    break;
//
//                default:
//                    break;
//            }
//        }
//    };

//    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
//        @Override
//        public void onIMServiceConnected() {
//            LogUtils.d("InternalFragment----->onIMServiceConnected?????????????????????EventBus??????????????????");
//            imService = imServiceConnector.getIMService();//??????????????????????????????
//            /** ??????????????????????????????????????????EventBus??????????????????IMBlogManager?????????????????????????????????????????? */
//           // EventBus.getDefault().postSticky(new BlogInfoEvent(BlogInfoEvent.Event.GET_BLOG_OK));
//            //??????head???user??????
//            mLoginInfo = imService.getLoginManager().getLoginInfo();
//            Message message = new Message();
//            message.what = 0;
//            mHandler.handleMessage(message);
//
//        }
//
//        @Override
//        public void onServiceDisconnected() {
//            if (EventBus.getDefault().isRegistered(InternalFragment.this)) {
//                EventBus.getDefault().unregister(InternalFragment.this);
//            }
//        }
//    };
    private int mTag;
    private List<BlogEntity> mBlogList = null;
    private boolean IsShowFlow;

    public static InternalFragment newInstance(int arg) {
        InternalFragment fragment = new InternalFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("TAG", arg);
        fragment.setArguments(bundle);
        return fragment;
    }
    public InternalFragment(){
        //??????
        EventBus.getDefault().register(this);//???????????????EventBus???????????????
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (null != curView) {
            return curView;
        }
        mTag = getArguments().getInt("TAG", -1);
        curView = inflater.inflate(R.layout.tt_fragment_internal, null);
        ButterKnife.bind(this, curView);
        ll_progress_bar.setVisibility(View.VISIBLE);
        initData();
        //??????IMService.class
       // imServiceConnector.connect(getActivity());
        //??????

//???????????????
        boolean isConnect = IMSocketManager.instance().isSocketConnect();//?????????????????????????????????
        if (isConnect){
            getData(pager);
        }else {
            ll_progress_bar.setVisibility(View.GONE);
            tv_netok.setText("??????????????????????????????!");
            tv_netok.setVisibility(View.VISIBLE);
        }

        return curView;
    }

    private void getData(final int pager) {
        switch (mTag){
            case 0://????????????
                //???????????????
                IMApplication.app.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        //???????????????
                        //DBlists = DBInterface.instance().loadAllUsers();
                        DBlists = DBInterface.instance().LoadUserFromTypeNOT(IMBaseDefine.UserRelationType.RELATION_RECOMMEND);
                        IMBlogManager.instance().reqBlogList(IMBaseDefine.BlogType.BLOG_TYPE_RCOMMEND,pager);
                        if (DBlists==null){
                            return;
                        }

                    }});

                break;
            case 1://????????????
                IMBlogManager.instance().reqBlogList(IMBaseDefine.BlogType.BLOG_TYPE_FRIEND,pager);
                break;
            case 2://????????????
                IMBlogManager.instance().reqBlogList(IMBaseDefine.BlogType.BLOG_TYPE_FOLLOWUSER,pager);
                break;
        }
    }

    private void initData() {
        final LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mLrvInternal.setLayoutManager(manager);
        //??????????????????
        int topSpace = Utils.dip2px(getActivity(), 5);
        int bottomSpace = Utils.dip2px(getActivity(), 5);
        int leftSpace = Utils.dip2px(getActivity(), 0);
        int rightSpace = Utils.dip2px(getActivity(), 0);
        MomentsItemDecoration decoration = new MomentsItemDecoration(topSpace, bottomSpace, leftSpace, rightSpace);
        decoration.setTag(mTag);
        mLrvInternal.addItemDecoration(decoration);
        //????????????????????????
         IsShowFlow = false;
        if (mTag==0||mTag==2){
            IsShowFlow=true;
        }
        mMomentsAdapter = new MomentsAdapter(getContext(),IsShowFlow);
        LRecyclerViewAdapter lRecyclerViewAdapter = new LRecyclerViewAdapter(mMomentsAdapter);
        mLrvInternal.setAdapter(lRecyclerViewAdapter);

        //?????????????????????????????????
      //  View inflate = View.inflate(getActivity(), R.layout.item_blog_head, null);
      //  initInflate(inflate);
        mLrvInternal.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {//????????????
                if (IMSocketManager.instance().isSocketConnect()){
                    RecyclerViewStateUtils.setFooterViewState(mLrvInternal, LoadingFooter.State.Normal);
                    //globalList.clear();
                    pager=0;
                    getData(pager);
                }else {
                    mLrvInternal.refreshComplete();
                }

            }
        });
        lRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getActivity(), CommentActivity.class);
                intent.putExtra("BlogEntity", globalList.get(position));
                intent.putExtra("position", position);
                intent.putExtra("IsShowFlow", IsShowFlow&&globalList.get(position).isShowButton);
                getActivity().startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });

        mLrvInternal.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (IMSocketManager.instance().isSocketConnect()){
                    //????????????
                    pager++;
                    getData(pager);
                    int position = manager.findFirstVisibleItemPosition();
                    if (position > 1) {
                        RecyclerViewStateUtils.setFooterViewState(mLrvInternal, LoadingFooter.State.TheEnd);
                    }

                }else {
                    mLrvInternal.refreshComplete();
                }

            }
        });
    }
    //----------------------------event ????????????----------------------------
    public void onEventMainThread(BlogInfoEvent event) {
        int tags = event.position;
        if (ll_progress_bar!=null){
            ll_progress_bar.setVisibility(View.GONE);
            tv_netok.setVisibility(View.GONE);
        }
        switch (event.getEvent()) {
            case GET_BLOG_OK://???????????????????????????????????????????????????//??????????????????
                switch (mTag) {
                    case 0://??????
                        if (tags==-1){
                            mBlogList = IMBlogManager.instance().getRecommendBlogList();
                            //????????????????????????????????????????????????????????????????????????????????????
                            setShowORFalse(mBlogList,DBlists);
                            if (mLrvInternal!=null){
                                mLrvInternal.refreshComplete();
                            }
                            if(pager==0){
                                globalList.clear();
                            }
                            if (mMomentsAdapter!=null){
                                if (mBlogList.size()>0) {
                                    globalList.addAll(mBlogList);
                                    mMomentsAdapter.setData(globalList);
                                }else {
                                    mMomentsAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                        break;

                    case 1://??????
                        if (tags==-2) {
                            mBlogList = IMBlogManager.instance().getFridendBlogList();
                            if (mLrvInternal!=null){
                                mLrvInternal.refreshComplete();
                            }
                            if(pager==0){
                                globalList.clear();
                            }
                            if (mMomentsAdapter!=null){
                                if (mBlogList.size()>0) {
                                    globalList.addAll(mBlogList);
                                    mMomentsAdapter.setData(globalList);
                                }else {
                                    mMomentsAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                        break;

                    case 2://??????
                        if (tags==-3){
                            mBlogList = IMBlogManager.instance().getFollowBlogList();
                            for (BlogEntity blogEntity:mBlogList){
                                //??????????????????????????????
                                blogEntity.isFollow=1;
                            }
                            if (mLrvInternal!=null){
                                mLrvInternal.refreshComplete();
                            }
                            if(pager==0){
                                globalList.clear();
                            }
                            if (mMomentsAdapter!=null){
                                if (mBlogList.size()>0) {
                                    globalList.addAll(mBlogList);
                                    mMomentsAdapter.setData(globalList);
                                }else {
                                    mMomentsAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                        break;
                }
                break;

            case ADD_BLOG_UPDATE_OK://???????????????????????????
                if (mTag == 1) {//???????????????????????????????????????????????????
                    BlogMessage blogMessage = event.getBlogMessage();
                    mMomentsAdapter.getData().add(0, blogMessage);//????????????
                    mMomentsAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    private void setShowORFalse(List<BlogEntity> mBlogList, List<UserEntity> dBlists) {
        if (mBlogList!=null&&dBlists!=null){
            /*
            SparseBooleanArray sparseArray = new SparseBooleanArray();
            sparseArray.put(IMLoginManager.instance().getLoginId(),true);//???????????????
            for (int j=0;j<dBlists.size();j++){
                sparseArray.put(dBlists.get(j).getPeerId(),true);
            }
            for (int i=0;i<mBlogList.size();i++){
                Long writerID = mBlogList.get(i).getWriterUserId();
                long id=writerID;
                boolean isExist = sparseArray.get((int) id);
                if (isExist){
                    mBlogList.get(i).isShowButton=false;
                }
            }

             */
        }

    }


    private void checkAndDelete(List<BlogEntity> mBlogList, List<UserEntity> DBlists) {
        //??????????????????????????????id?????????
        if (mBlogList!=null&&mBlogList.size()>0&&DBlists!=null&&DBlists.size()>0){
            //??????
            /*
            Iterator<BlogEntity> mBlogListIterator = mBlogList.iterator();
           while (mBlogListIterator.hasNext()){
               Long friendId = mBlogListIterator.next().getWriterUserId();
               for (UserEntity userEntity:DBlists){
                   if (userEntity.getPeerId()==friendId||friendId== IMLoginManager.instance().getLoginId()){
                     // mBlogListIterator.remove();
                     mBlogListIterator.next().isShowButton=true;
                       break;
                   }
               }
           }

             */
        }
    }
//
//    private void initInflate(View inflate) {
//        mUser_name_head = (TextView) inflate.findViewById(R.id.user_name_head);
//        mUser_bgpic_head = (ImageView) inflate.findViewById(R.id.user_bgpic_head);
//        mUser_icon_head = (ImageView) inflate.findViewById(R.id.user_icon_head);
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
       // imServiceConnector.disconnect(getActivity());
    }

}
