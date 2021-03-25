package com.tenth.space.ui.widget.message;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tenth.space.R;

public class SystemMsgRenderView extends LinearLayout {
    private TextView system_tip_content;

    public SystemMsgRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static SystemMsgRenderView inflater(Context context, ViewGroup viewGroup){
        SystemMsgRenderView systemMsgRenderView = (SystemMsgRenderView) LayoutInflater.from(context).inflate(R.layout.tt_system_text_message_item, viewGroup, false);
        return systemMsgRenderView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        system_tip_content = (TextView) findViewById(R.id.system_tip_content);
    }

    /**与数据绑定*/
    public void setContent(CharSequence conten){
        system_tip_content.setText("系统提示:" + conten);
    }

}