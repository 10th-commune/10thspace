package com.tenth.space.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;

import com.tenth.space.imservice.service.IMService;
import com.tenth.space.utils.CrashHandler;
import com.tenth.space.utils.ImageLoaderUtil;
import com.tenth.space.utils.Logger;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.sharesdk.framework.ShareSDK;

public class IMApplication extends MultiDexApplication {

    public static IMApplication app;
    public   static String RootDirectory;//文件私有目录
    private Logger logger = Logger.getLogger(IMApplication.class);
    //private OSSClient ossClient;
    private ExecutorService fixedThreadPool;
    private int appCount = 0;

    public static Context mContext;
    /**
     * @param args
     */
    public static void main(String[] args) {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        app = this;
        //ShareSDK初始化
        ShareSDK.initSDK(this);
        //初始化ImageLoader框架
        ImageLoaderUtil.initImageLoaderConfig(getApplicationContext());
        //开启服务
        startIMService();

        RootDirectory = getDir("pictures", Context.MODE_PRIVATE).toString();
        File file = new File(RootDirectory);
        if (!file.exists()){
            file.mkdirs();
        }

//应用程序异常闪退，捕获异常
        //if (!Config.debug) {
            CrashHandler crashHandler = CrashHandler.getInstance();
            crashHandler.init(getApplicationContext(), this);
        //}

/*
注册目的：判断引用程序是否为前台可见进程
 */
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                appCount++;
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                appCount--;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }
    public int getAppCount() {
        return appCount;
    }

    public void setAppCount(int appCount) {
        this.appCount = appCount;
    }
    private void startIMService() {
        Intent intent = new Intent();
        intent.setClass(this, IMService.class);
        startService(intent);
    }
    public static boolean gifRunning = true;//gif是否运行
    //创建线程池

    public  ExecutorService getThreadPool(){
        if (fixedThreadPool==null){
            fixedThreadPool = Executors.newFixedThreadPool(3);
        }

       // ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(3, 5, 1, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(3));
        return fixedThreadPool;
    }

    /*public OSSClient GetGlobleOSSClent(){
        if (ossClient==null){
            ossClient = new OSSClient(this, Config.endpoint, STSGetter.instance(), Config.getAliClientConf());
        }
        return ossClient;
    }*/
//url特殊装换，将普通的url转换为可以直接被ImagLader识别的url
    public String UrlFormat(String nomalUrl){
        return  nomalUrl;
        //以下是将Aliyun的图片文件夹设为私有，必须转变URL
//        Log.i("GTAG","xxurl="+nomalUrl);
//        String endurl="";
//        String url="";
//        if (nomalUrl!=null&&nomalUrl.contains("oss-cn-shenzhen.aliyuncs.com/")){
//            endurl=nomalUrl.split("oss-cn-shenzhen.aliyuncs.com/")[1];
//        }else {
//            return nomalUrl;
//        }
//        try {
//            url = GetGlobleOSSClent().presignConstrainedObjectURL(Config.bucketName, endurl, 30 * 60);
//            Log.i("GTAG","xx特殊url="+url);
//            return url;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return url;
//        }
   }


    /*public String PrivateUrlFormat(String nomalUrl){
        //以下是将Aliyun的图片文件夹设为私有，必须转变URL
        //Log.i("GTAG","xxurl="+nomalUrl);
        String endurl="";
        String url="";
        if (nomalUrl!=null&&nomalUrl.contains("oss-cn-shenzhen.aliyuncs.com/")){
            endurl=nomalUrl.split("oss-cn-shenzhen.aliyuncs.com/")[1];
        }else {
            return nomalUrl;
        }
        try {
            url = GetGlobleOSSClent().presignConstrainedObjectURL(Config.privateBucketName, endurl, 30 * 60);
            //Log.i("GTAG","xx特殊url="+url);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            return url;
        }
    }*/
}
