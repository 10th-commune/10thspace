package com.tenth.space.utils.upgrade;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.listener.OnDownloadListenerAdapter;
import com.azhon.appupdate.manager.DownloadManager;
import com.tenth.space.R;
import com.tenth.space.config.UrlConstant;
import com.tenth.space.imservice.event.LoginEvent;
import com.tenth.space.imservice.event.SocketEvent;
import com.tenth.space.imservice.manager.IMSocketManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class UpgradeManager {

    Activity mContext;
    UpgradeListener mListener;
    private DownloadManager manager;

    private OkHttpClient client;

    public UpgradeManager(Activity context,UpgradeListener listener){
        mContext = context;
        mListener = listener;
    }

    public void start(String strUpgradeUrl){
        reqUpgrade(strUpgradeUrl);
    }

    private void reqUpgrade(String strUpgradeUrl){

        if (client==null){
            client = new OkHttpClient();
        }

        final Request request = new Request.Builder()
                .url(strUpgradeUrl)//
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mListener.cancelUpgrade();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    Message messages = handler.obtainMessage();
                    messages.obj = response.body().string();
                    messages.what = 0;
                    handler.sendMessage(messages);
                }
                else{
                    mListener.cancelUpgrade();
                }
            }
        });
    }

    private void parseUpgradeConfig(String strConfigData){
        if(strConfigData.contains("curVer")&&strConfigData.contains("downloadUrl")) {
            int nPos1 = strConfigData.indexOf("{");
            int nPos2 = strConfigData.indexOf("}");
            strConfigData = strConfigData.substring(nPos1,nPos2+1);
            strConfigData = strConfigData.replace("\r\n","");

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(strConfigData);
                if(jsonObject.getInt("curVer") > 2) {
                    String strUrl = jsonObject.getString("downloadUrl");
                    showUpgradDialog(strUrl);
                    return;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mListener.cancelUpgrade();
    }

    private void showUpgradDialog(String downloadUrl){
        String url = downloadUrl;//"http://192.168.88.235:8080/update/app-release.apk";//"http://192.168.88.235:80/app-release.apk";

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Light_Dialog)).setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialog_view = inflater.inflate(R.layout.tt_custom_dialog, null);
        final EditText editText = (EditText) dialog_view.findViewById(R.id.dialog_edit_content);
        editText.setVisibility(View.GONE);
        TextView textText = (TextView) dialog_view.findViewById(R.id.dialog_title);
        textText.setText(R.string.dialog_msg);
        builder.setView(dialog_view);

        builder.setPositiveButton(mContext.getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UpdateConfiguration configuration = new UpdateConfiguration()
                        .setEnableLog(true)
                        .setJumpInstallPage(true)
                        .setDialogButtonTextColor(Color.WHITE)
                        .setShowNotification(true)
                        .setUsePlatform(true)
                        .setForcedUpgrade(false)
                        .setOnDownloadListener(updatelistenerAdapter);

                manager = DownloadManager.getInstance(mContext);
                manager.setApkName("wystan_test.apk")
                        .setApkUrl(url)
                        .setSmallIcon(R.mipmap.zan)
                        .setConfiguration(configuration)
                        .download();

                mListener.doUpgrade();
            }
        });

        builder.setNegativeButton(mContext.getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.cancelUpgrade();
            }
        });

        builder.show();
    }

    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    String return_server = (String)msg.obj;
                    parseUpgradeConfig(return_server);
                    break;
            }
        }
    };

    private OnDownloadListenerAdapter updatelistenerAdapter = new OnDownloadListenerAdapter() {
        @Override
        public void downloading(int max, int progress) {
            mListener.downloading(max, progress);
        }
        @Override
        public void done(File apk) {

        }

        @Override
        public void cancel(){
            mListener.cancelUpgrade();
        }

        @Override
        public void error(Exception e){
            mListener.error(e);
        }
    };


    public interface UpgradeListener{
        void doUpgrade();
        void cancelUpgrade();
        void error(Exception e);
        void downloading(int max, int progress);
    }
}
