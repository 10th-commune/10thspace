package com.tenth.space.utils;

import android.util.Log;

import com.tenth.space.app.IMApplication;
import com.tenth.space.config.UrlConstant;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by wsq on 2016/11/4.
 */

public class OkHttpUtils {

    public static void request(String jsonString) {
        final OkHttpClient okHttpClient = new OkHttpClient();
//        参数 post -》 arg = {json格式} phone, valid_code, passwd, username
        FormBody.Builder arg = new FormBody.Builder().add("arg", "{\"phone\":\"1\",\"passwd\":\"52c69e3a57331081823331c4e69d3f2e\"}\n");
        final Request request = new Request.Builder().url("http://www.d10gs.com:86/login").post(arg.build()).build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response execute = okHttpClient.newCall(request).execute();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
    public  static void sendReport(final String params){
        IMApplication.app.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try{
                    //String urlPath = new String("http://www.d10gs.com:86/login");
                    String urlPath =UrlConstant.REPORT;
                    //String urlPath = new String("http://localhost:8080/Test1/HelloWorld?name=丁丁".getBytes("UTF-8"));10thcommune.com/complain
                    //  String param="arg="+ URLEncoder.encode("{\"phone\":\"1\",\"passwd\":\"52c69e3a57331081823331c4e69d3f2e\"}\n","UTF-8");
                    String param=params;
                   //Log.i("GTAG","params="+params+"  url="+urlPath);
                    //建立连接
                    URL url=new URL(urlPath);
                    HttpURLConnection httpConn=(HttpURLConnection)url.openConnection();
                    //设置参数
                    httpConn.setDoOutput(true);   //需要输出
                    httpConn.setDoInput(true);   //需要输入
                    httpConn.setUseCaches(false);  //不允许缓存
                    httpConn.setRequestMethod("POST");   //设置POST方式连接
                    //设置请求属性
                    httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
                    httpConn.setRequestProperty("Charset", "UTF-8");
                    //连接,也可以不用明文connect，使用下面的httpConn.getOutputStream()会自动connect
                    httpConn.connect();
                    //建立输入流，向指向的URL传入参数
                    DataOutputStream dos=new DataOutputStream(httpConn.getOutputStream());
                    dos.writeBytes(param);
                    dos.flush();
                    dos.close();
                    //获得响应状态
                    int resultCode=httpConn.getResponseCode();
                    //Log.i("GTAG","resultCode="+resultCode);
                    if(HttpURLConnection.HTTP_OK==resultCode){
                        StringBuffer sb=new StringBuffer();
                        String readLine=new String();
                        BufferedReader responseReader=new BufferedReader(new InputStreamReader(httpConn.getInputStream(),"UTF-8"));
                        while((readLine=responseReader.readLine())!=null){
                            sb.append(readLine).append("\n");
                        }
                        JSONObject jsonObject=new JSONObject(sb.toString());
                        int error_code = jsonObject.optInt("error_code");
                       // Log.i("GTAG","error_code="+error_code);
                        if (error_code==0){
                            ToastUtils.show("举报成功");
                        }else {
                            ToastUtils.show("举报失败");
                        }
                       // Log.i("GTAG","content="+sb);
                        responseReader.close();
                    }
                }catch (Exception e){

                }
            }
        });
    }

}


