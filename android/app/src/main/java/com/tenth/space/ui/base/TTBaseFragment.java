package com.tenth.space.ui.base;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tenth.tools.ScreenTools;
import com.tenth.space.R;
import com.tenth.space.ui.activity.AddBlogActivity;
import com.tenth.space.utils.Logger;
import com.tenth.space.ui.widget.SearchEditText;
import com.tenth.space.ui.widget.TopTabButton;

public abstract class TTBaseFragment extends Fragment {
    protected ImageView topLeftBtn;
    protected ImageView topRightImg;
    protected TextView topTitleTxt;
    protected TextView topLetTitleTxt;
    protected TextView topRightTitleTxt;

    protected ViewGroup topBar;
    protected TopTabButton topContactTitle;
    protected SearchEditText topSearchEdt;
    protected ViewGroup topContentView;
    protected RelativeLayout topLeftContainerLayout;
//    protected FrameLayout searchFrameLayout;
    protected FrameLayout topContactFrame;

    protected float x1, y1, x2, y2 = 0;
    protected static Logger logger = Logger.getLogger(TTBaseFragment.class);
    private Button add;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        topContentView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.tt_fragment_base, null);

        topBar = (ViewGroup) topContentView.findViewById(R.id.topbar);
        topTitleTxt = (TextView) topContentView.findViewById(R.id.base_fragment_title);
        topLetTitleTxt = (TextView) topContentView.findViewById(R.id.left_txt);
        topRightTitleTxt = (TextView) topContentView.findViewById(R.id.right_txt);
        topLeftBtn = (ImageView) topContentView.findViewById(R.id.left_btn);
        topRightImg = (ImageView) topContentView.findViewById(R.id.right_img);
        topContactTitle = (TopTabButton) topContentView.findViewById(R.id.contact_tile);
        topSearchEdt = (SearchEditText) topContentView.findViewById(R.id.chat_title_search);
        topLeftContainerLayout = (RelativeLayout) topContentView.findViewById(R.id.top_left_container);
//        searchFrameLayout = (FrameLayout) topContentView.findViewById(R.id.searchbar);
        topContactFrame = (FrameLayout) topContentView.findViewById(R.id.contactTopBar);
        add = (Button) topContentView.findViewById(R.id.add_friends);

        topTitleTxt.setVisibility(View.GONE);
        topRightImg.setVisibility(View.GONE);
        topLeftBtn.setVisibility(View.GONE);
        topLetTitleTxt.setVisibility(View.GONE);
        topRightTitleTxt.setVisibility(View.GONE);
        topContactTitle.setVisibility(View.GONE);
        topSearchEdt.setVisibility(View.GONE);
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg,
                             Bundle bundle) {
        if (null != topContentView) {
            ((ViewGroup) topContentView.getParent()).removeView(topContentView);
            return topContentView;
        }
        return topContentView;
    }

    protected void setTopTitleBold(String title) {
        if (title == null) {
            return;
        }
        if (title.length() > 28) {
            title = title.substring(0, 27) + "...";
        }
        // 设置字体为加粗
        TextPaint paint = topTitleTxt.getPaint();
        paint.setFakeBoldText(true);

        topTitleTxt.setText(title);
        topTitleTxt.setVisibility(View.VISIBLE);

    }

    protected void setTopTitle(String title) {
        if (title == null) {
            return;
        }
        if (title.length() > 28) {
            title = title.substring(0, 27) + "...";
        }
        topTitleTxt.setText(title);
        topTitleTxt.setVisibility(View.VISIBLE);
    }

    protected void hideTopTitle() {
        topTitleTxt.setVisibility(View.GONE);
    }

    protected void showContactTopBar() {
//        topContactFrame.setVisibility(View.VISIBLE);
        //topContactTitle.setVisibility(View.VISIBLE);
    }

    protected void setTopLeftButton(int resID) {
        if (resID <= 0) {
            return;
        }

        topLeftBtn.setImageResource(resID);
        topLeftBtn.setVisibility(View.VISIBLE);
    }

    protected void setTopLeftButtonPadding(int l, int t, int r, int b) {
        topLeftBtn.setPadding(l, t, r, b);
    }

    protected void hideTopLeftButton() {
        topLeftBtn.setVisibility(View.GONE);
    }

    protected void setTopLeftText(String text) {
        /*
        不显示“返回”
         */
        if (null == text) {
            return;
        }
        //topLetTitleTxt.setText(text);
        if (text.trim().equals("返回")){
            topLetTitleTxt.setText("    ");
        }else {
            topLetTitleTxt.setText(text);
        }

        topLetTitleTxt.setVisibility(View.VISIBLE);
    }

    protected void setTopRightText(String text) {
        if (null == text) {
            return;
        }
        topRightTitleTxt.setText(text);
        topRightTitleTxt.setTextColor(Color.WHITE);
        topRightTitleTxt.setVisibility(View.VISIBLE);
    }

    protected void setTopRightButton(int resID) {
        if (resID <= 0) {
            return;
        }

        topRightImg.setImageResource(resID);
        topRightImg.setVisibility(View.VISIBLE);
    }

    protected void hideTopRightButton() {
        topRightImg.setVisibility(View.GONE);
    }

    protected void setTopBar(int resID) {
        if (resID <= 0) {
            return;
        }
        topBar.setBackgroundResource(resID);
    }

    protected void hideTopBar() {
        topBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) topContactFrame.getLayoutParams();
        linearParams.height = ScreenTools.instance(getActivity()).dip2px(45);
        topContactFrame.setLayoutParams(linearParams);
        topContactFrame.setPadding(0, ScreenTools.instance(getActivity()).dip2px(10), 0, 0);
    }

    protected void showTopSearchBar() {
        topSearchEdt.setVisibility(View.VISIBLE);
    }

    protected void hideTopSearchBar() {
        topSearchEdt.setVisibility(View.GONE);
    }

    protected void showSearchFrameLayout() {
//        searchFrameLayout.setVisibility(View.VISIBLE);
        /**还是以前的页面，没有看psd是否改过*/
//        searchFrameLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                showSearchView();
//            }
//        });
        //下面的历史代码
        //tryHandleSearchAction(action);
    }

    protected abstract void initHandler();

    @Override
    public void onActivityCreated(Bundle bundle) {
        logger.i("Fragment onActivityCreate:" + getClass().getName());
        super.onActivityCreated(bundle);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    protected void initRigthButton() {
        setTopRightButton(R.mipmap.camera_capture);
        topRightImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showSearchView();
            }
        });
    }

    public void showSearchView() {
        startActivity(new Intent(getActivity(), AddBlogActivity.class));
    }

    public Button getAddFriendsButtonObject(){
        return add;
    }

    protected void onSearchDataReady() {
        initRigthButton();
    }
}
