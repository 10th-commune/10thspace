/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tenth.space.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.tenth.space.R;
import com.tenth.space.ui.activity.DialogTopOptActivity;


public class HomeFragment extends MainFragment  {
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }
    private View view;
    private ImageView optBtn = null;

    @Override
    protected void initHandler() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EventBus.getDefault().registerSticky(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        WebView webView = (WebView) view.findViewById(R.id.home_webview);
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.loadUrl("http://120.79.236.6/");
        webView.reload();
        optBtn = (ImageView) view.findViewById(R.id.opt_btn);
        optBtn.setVisibility(View.GONE);
        optBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logger.i("wystan addBtn clicked");
                Dialog dialog = null;
                dialog = new DialogTopOptActivity(getActivity());
                if (dialog != null) {
                    Window dialogWindow = dialog.getWindow();
                    WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                    dialogWindow.setGravity(Gravity.RIGHT | Gravity.TOP);
                    lp.x = 10; // 新位置X坐标
                    lp.y = 130; // 新位置Y坐标
                    lp.width = 600;
                    // lp.height = 620;
                    dialogWindow.setAttributes(lp);
                    dialog.show();
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }*/
    }
}
