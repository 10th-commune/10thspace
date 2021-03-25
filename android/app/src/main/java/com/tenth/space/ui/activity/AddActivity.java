package com.tenth.space.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.tenth.space.R;
import com.tenth.space.imservice.manager.FriendManager;
import com.tenth.space.protobuf.IMSystem;

import java.util.Timer;
import java.util.TimerTask;

public class AddActivity extends Activity implements View.OnClickListener{

    private InputMethodManager imm;
    private EditText inputText;
    private TextView add_friend;
    private String friendId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        friendId = getIntent().getStringExtra("friendId");
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputText=(EditText)findViewById(R.id.editText);
        add_friend=(TextView)findViewById(R.id.go_add);
        add_friend.setOnClickListener(this);
        findViewById(R.id.go_back).setOnClickListener(this);
        findViewById(R.id.clear_text).setOnClickListener(this);
        ShowSoft();
    }
    private void ShowSoft(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask()
                       {
                           public void run()
                           {
                               imm.showSoftInput(inputText, 0);
                           }
                       },
                90);
    }

    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.go_add:
               int status=0;
               if(getIntent().getBooleanExtra("isGroup",false)){
                   status = 0x11;
               }
               FriendManager.instance().reqAddFriend(inputText.getText()+"",friendId, status==0x11? IMSystem.SysMsgOper.ADD_GROUP_REQUEST:IMSystem.SysMsgOper.ADD_FRIEND_REQUEST);
//               Toast.makeText(AddActivity.this,"已发送好友请求，等待对方确认",Toast.LENGTH_LONG).show();
               finish();
               break;
           case R.id.go_back:
               finish();
               break;
           case R.id.clear_text:
               inputText.setText("");
               break;
       }
    }
}
