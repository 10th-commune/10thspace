package com.tenth.space.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.google.common.base.Charsets;
import com.google.common.primitives.UnsignedLongs;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.tenth.space.BitherjSettings;
import com.tenth.space.R;
import com.tenth.space.app.IMApplication;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * 工具类
 * Created by linzh01 on 2016/5/12.
 */
public class Utils {

    public static String PNG = ".png";
    private static PopupWindow popupWindow;

    private static final MessageDigest digest;
    public static final String BitcoinNewAddressPrefix = "bc1";
    public static final String BitcoinRegTestNewAddressPrefix = "bcrt1";

    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Can't happen.
        }
    }
    /**
     * 格式化小数
     */
    public static String formatDecimal(float f) {
        DecimalFormat df = new DecimalFormat("0.00");
        String format = df.format(f);
        return format;
    }

    /**
     * 图片按照格式保存方法
     *
     * @param original_bitmap
     * @param path
     */
    public static void saveBitmapByFormat(Bitmap original_bitmap, String path, Bitmap.CompressFormat format) {
        File file = new File(path);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (original_bitmap.compress(format, 100, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //p
    public static boolean isCameraCanUse() {
        boolean canUse = true;
        Camera mCamera = null;
        try {
            // TODO camera驱动挂掉,处理??
            mCamera = Camera.open();
        } catch (Exception e) {
            canUse = false;
        }
        if (canUse) {
            mCamera.release();
            mCamera = null;
        }

        return canUse;
    }

    /**
     * 转换uri为path
     */
    public static String getPathByUri(Activity activity, Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor actualimagecursor = activity.managedQuery(uri, proj, null, null, null);
        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();
        String path = actualimagecursor.getString(actual_image_column_index);
        return path;
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public static void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        activity.getWindow().setAttributes(lp);
    }

    /*
    获取手机唯一识别码IMEI号
     */
    public static String getPhoneIMEI(Context context) {
        TelephonyManager mTm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        String imei = mTm.getDeviceId();
        return imei;
    }

    /**
     * 验证手机格式
     */

    public static boolean isMobileNO(String mobiles) {
        /*
        移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
        联通：130、131、132、152、155、156、185、186
        电信：177,133、153、180、189、（1349卫通）
        总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
        */
        String telRegex = "[1][3578]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobiles))
            return false;
        else
            return mobiles.matches(telRegex);
    }

    /**
     * 获取屏幕宽高
     *
     * @return
     */
    public static int[] getDisplayInfo() {
        WindowManager systemService = (WindowManager) IMApplication.app.getSystemService(Context.WINDOW_SERVICE);
        int width = systemService.getDefaultDisplay().getWidth();
        int height = systemService.getDefaultDisplay().getHeight();
        return new int[]{width, height};

    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     *
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                //result = android.util.Base64.encodeToString(bitmapBytes, android.util.Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 此方法用于判断String是否为null，或者长度为0，或者为字符串"null"(忽略大小写)
     *
     * @param str
     *
     * @return
     */
    public static boolean isStringEmpty(String str) {
        if (str == null || str.length() == 0 || str.equalsIgnoreCase("null"))
            return true;
        else
            return false;
    }

    private static long getFileSizes(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        }
        return size;
    }

    public static double getFileOrFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSizes(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FormetFileSize(blockSize, SIZETYPE_B);
    }

    private static String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 转换文件大小,指定转换的类型
     *
     * @param fileS
     * @param sizeType
     * @return
     */
    final static int SIZETYPE_B = 1;
    final static int SIZETYPE_KB = 2;
    final static int SIZETYPE_MB = 3;
    final static int SIZETYPE_GB = 4;

    private static double FormetFileSize(long fileS, int sizeType) {
        DecimalFormat df = new DecimalFormat("#.00");
        double fileSizeLong = 0;
        switch (sizeType) {
            case SIZETYPE_B:

                fileSizeLong = Double.valueOf(df.format((double) fileS));
                break;
            case SIZETYPE_KB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1024));
                break;
            case SIZETYPE_MB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1048576));
                break;
            case SIZETYPE_GB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1073741824));
                break;
            default:
                break;
        }
        return fileSizeLong;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static byte[] BitmapToStream(Bitmap bitmap) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();//初始化一个流对象
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);//把bitmap100%高质量压缩 到 output对象里
        // bitmap.recycle();//自由选择是否进行回收
        byte[] result = output.toByteArray();//转换成功了
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getPhotoPathFromContentUri(Context context, Uri uri) {
        String photoPath = "";
        if (context == null || uri == null) {
            return photoPath;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if (isExternalStorageDocument(uri)) {
                String[] split = docId.split(":");
                if (split.length >= 2) {
                    String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        photoPath = Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
            } else if (isDownloadsDocument(uri)) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                photoPath = getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                String[] split = docId.split(":");
                if (split.length >= 2) {
                    String type = split[0];
                    Uri contentUris = null;
                    if ("image".equals(type)) {
                        contentUris = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUris = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUris = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    String selection = MediaStore.Images.Media._ID + "=?";
                    String[] selectionArgs = new String[]{split[1]};
                    photoPath = getDataColumn(context, contentUris, selection, selectionArgs);
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            photoPath = uri.getPath();
        } else {
            photoPath = getDataColumn(context, uri, null, null);
        }

        return photoPath;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }
        return null;
    }

    //获取当前是哪一年
    public static String getCurrentYear() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        return String.valueOf(year);
    }

    //获取当前是哪一年
    public static String getCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        String currentmonth = "";
        if (month < 10) {
            currentmonth = "0" + month;
        } else if (month >= 10) {
            currentmonth = String.valueOf(month);
        }
        return currentmonth;
    }

    //Json字符串转换JsonArray
    public static JSONArray string2JsonArray(String blogImages) {
        if (!TextUtils.isEmpty(blogImages)) {
            try {
                JSONArray jsonArray = new JSONArray(blogImages);
                return jsonArray;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    //Json字符串转换JsonObject
    public static JSONObject string2JsonObject(String blogImages) {
        if (!TextUtils.isEmpty(blogImages)) {
            try {
                JSONObject jsonObject = new JSONObject(blogImages);
                return jsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public static String getPhoneModel() {
        return Build.MODEL;
    }
    //上传用户默认头像

    public  static  byte[] input2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }
//    public   static void upLoadToAliYUN(Bitmap btm,Context context) {
//        //上传到aliyun服务器
//        //OSSClient ossClient = new OSSClient(context, Config.endpoint, STSGetter.instance(), Config.getAliClientConf());
//        OSSClient ossClient = IMApplication.app.GetGlobleOSSClent();
//        final String imageName = IMLoginManager.instance().getLoginId() + Utils.PNG;
//        //构建上传请求
//        ByteArrayOutputStream output = new ByteArrayOutputStream();//初始化一个流对象
//        btm.compress(Bitmap.CompressFormat.JPEG, 50, output);//把bitmap100%高质量压缩 到 output对象里
//        byte[] result = output.toByteArray();//转换成功了
//        try {
//            output.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        PutObjectResult recode = new AliyunUpload(ossClient, Config.privateBucketName, Config.livePicsPath + imageName, null, null, null).uploadBytes(result);
//    }
    //清理指定本地缓存和内存缓存

    public  static void clearDiskAndMemoryCache(String pictrueUrl,boolean IsDisk ,boolean IsMemory){
        if (IsDisk){
            DiskCacheUtils.removeFromCache(pictrueUrl, ImageLoaderUtil.instance().getDiskCache());
        }
        if (IsMemory){
            MemoryCacheUtils.removeFromCache(pictrueUrl, ImageLoaderUtil.instance().getMemoryCache());
        }


    }

    public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }
    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }
    //弹出提交举报的popuwindow
    public static void showPopupWindow(final Activity context,final long toID ,final int reportCode) {
        /*
        reportCode:10表示举报个人，表示举报群
         */
        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.popuwindow_layout, null);
        Button btn_report=(Button)view.findViewById(R.id.btn_report);
        Button btn_cancel=(Button)view.findViewById(R.id.btn_cancel);
        btn_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击举报按钮,弹出dialog
              popupWindow.dismiss();
                final EditText editText=new EditText(context);
                new AlertDialog.Builder(context).setTitle("请输入举报内容")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(editText).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!Utils.isStringEmpty(editText.getText().toString())){//举报一个人是10，举报群是11
                            // neil
                            String params="arg="+"&from_id="+IMLoginManager.instance().getLoginId()+"&to_id="+toID+"&complain_text="+editText.getText().toString()+"&type="+reportCode;
                            OkHttpUtils.sendReport(params);
                        }else {
                            ToastUtils.show("举报信息不能为空！");
                        }
                        //请求网络，将用户意见提交到服务器 "userid" + "=" + user + "&" +"code" + "=" + codeid + "&" + "Photo" + "=";

                        /*
                        1. arg放登录后的token.
                        2. from_id, to_id, complain_text, type为post的其他参数。
                        3. 第十空间的 type 为 2.
                         */

                    }
                }).setNegativeButton("取消", null).show();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.AnimBottom);
        // 显示(靠底部)
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
    }
    /**
     *判断当前应用程序处于前台还是后台
     */
/*    public static boolean isApplicationBroughtToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }*/
    public static boolean getApplicationValue(IMApplication myApplication) {
        return myApplication.getAppCount() > 0;
    }
    /**
     * 打开日志文件并写入日志
     *
     * @return
     * **/

    private static Boolean MYLOG_SWITCH=true; // 日志文件总开关
    private static Boolean MYLOG_WRITE_TO_FILE=true;// 日志写入文件开关
    private static char MYLOG_TYPE='v';// 输入日志类型，w代表只输出告警信息等，v代表输出所有信息
    private static String MYLOG_PATH_SDCARD_DIR="/sdcard/";// 日志文件在sdcard中的路径
    private static int SDCARD_LOG_FILE_SAVE_DAYS = 0;// sd卡中日志文件的最多保存天数
    private static String MYLOGFILEName = "Log.txt";// 本类输出的日志文件名称
    private static SimpleDateFormat myLogSdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");// 日志的输出格式
    private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");// 日志文件格式
    public static void writeLogtoFile(String mylogtype,String text) {// 新建或打开日志文件
        Date nowtime = new Date();
        String needWriteFiel = logfile.format(nowtime);
        String needWriteMessage = myLogSdf.format(nowtime) + "    GTAG" + mylogtype
                + "   info--> "  +"    " + text;
        File file = new File(MYLOG_PATH_SDCARD_DIR, needWriteFiel
                + MYLOGFILEName);
        try {
            FileWriter filerWriter = new FileWriter(file, true);//后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(needWriteMessage);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    //------------------------------------------wystan add for bitcoin key about 201229------------------------------------------//

    public static byte[] doubleDigest(byte[] input) {
        return doubleDigest(input, 0, input.length);
    }

    public static byte[] doubleDigest(byte[] input, int offset, int length) {
        synchronized (digest) {
            digest.reset();
            digest.update(input, offset, length);
            byte[] first = digest.digest();
            return digest.digest(first);
        }
    }

    final protected static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars).toUpperCase(Locale.US);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0;
             i < len;
             i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s
                    .charAt(i + 1), 16));
        }
        return data;
    }

    public static volatile Date mockTime;

    public static long currentTimeMillis() {
        if (mockTime != null) {
            return mockTime.getTime();
        } else {
            return System.currentTimeMillis();
        }
    }

    public static long currentTimeSeconds() {
        return currentTimeMillis() / 1000;
    }

    public static byte[] sha256hash160(byte[] input) {
        try {
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(input);
            RIPEMD160Digest digest = new RIPEMD160Digest();
            digest.update(sha256, 0, sha256.length);
            byte[] out = new byte[20];
            digest.doFinal(out, 0);
            return out;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
        if (b == null) {
            return null;
        }
        byte[] bytes = new byte[numBytes];
        byte[] biBytes = b.toByteArray();
        int start = (biBytes.length == numBytes + 1) ? 1 : 0;
        int length = Math.min(biBytes.length, numBytes);
        System.arraycopy(biBytes, start, bytes, numBytes - length, length);
        return bytes;
    }

    public static final String BITCOIN_SIGNED_MESSAGE_HEADER = "Bitcoin Signed Message:\n";
    public static final byte[] BITCOIN_SIGNED_MESSAGE_HEADER_BYTES =
            BITCOIN_SIGNED_MESSAGE_HEADER.getBytes(Charsets.UTF_8);

    public static byte[] formatMessageForSigning(String message) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(BITCOIN_SIGNED_MESSAGE_HEADER_BYTES.length);
            bos.write(BITCOIN_SIGNED_MESSAGE_HEADER_BYTES);
            byte[] messageBytes = message.getBytes(Charsets.UTF_8);
            VarInt size = new VarInt(messageBytes.length);
            bos.write(size.encode());
            bos.write(messageBytes);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    public static boolean isLessThanUnsigned(long n1, long n2) {
        return UnsignedLongs.compare(n1, n2) < 0;
    }

    public static long readUint32(byte[] bytes, int offset) {
        return ((bytes[offset++] & 0xFFL)) |
                ((bytes[offset++] & 0xFFL) << 8) |
                ((bytes[offset++] & 0xFFL) << 16) |
                ((bytes[offset] & 0xFFL) << 24);
    }

    public static void uint32ToByteArrayBE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & (val >> 24));
        out[offset + 1] = (byte) (0xFF & (val >> 16));
        out[offset + 2] = (byte) (0xFF & (val >> 8));
        out[offset + 3] = (byte) (0xFF & (val));
    }

    public static void uint32ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & (val));
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
    }

    public static void uint64ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & (val));
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
        out[offset + 4] = (byte) (0xFF & (val >> 32));
        out[offset + 5] = (byte) (0xFF & (val >> 40));
        out[offset + 6] = (byte) (0xFF & (val >> 48));
        out[offset + 7] = (byte) (0xFF & (val >> 56));
    }

    public static void uint32ToByteStreamLE(long val, OutputStream stream) throws IOException {
        stream.write((int) (0xFF & (val)));
        stream.write((int) (0xFF & (val >> 8)));
        stream.write((int) (0xFF & (val >> 16)));
        stream.write((int) (0xFF & (val >> 24)));
    }

    public static void int64ToByteStreamLE(long val, OutputStream stream) throws IOException {
        stream.write((int) (0xFF & (val)));
        stream.write((int) (0xFF & (val >> 8)));
        stream.write((int) (0xFF & (val >> 16)));
        stream.write((int) (0xFF & (val >> 24)));
        stream.write((int) (0xFF & (val >> 32)));
        stream.write((int) (0xFF & (val >> 40)));
        stream.write((int) (0xFF & (val >> 48)));
        stream.write((int) (0xFF & (val >> 56)));
    }

    public static void uint64ToByteStreamLE(BigInteger val, OutputStream stream) throws
            IOException {
        byte[] bytes = val.toByteArray();
        if (bytes.length > 8) {
            throw new RuntimeException("Input too large to encode into a uint64");
        }
        bytes = reverseBytes(bytes);
        stream.write(bytes);
        if (bytes.length < 8) {
            for (int i = 0;
                 i < 8 - bytes.length;
                 i++)
                stream.write(0);
        }
    }


    public static long readUint32BE(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFFL) << 24) |
                ((bytes[offset + 1] & 0xFFL) << 16) |
                ((bytes[offset + 2] & 0xFFL) << 8) |
                ((bytes[offset + 3] & 0xFFL));
    }

    public static String toAddress(byte[] pubKeyHash) {
        checkArgument(pubKeyHash.length == 20, "Addresses are 160-bit hashes, " +
                "so you must provide 20 bytes");

        int version = BitherjSettings.addressHeader;
        checkArgument(version < 256 && version >= 0);

        byte[] addressBytes = new byte[1 + pubKeyHash.length + 4];
        addressBytes[0] = (byte) version;
        System.arraycopy(pubKeyHash, 0, addressBytes, 1, pubKeyHash.length);
        byte[] check = Utils.doubleDigest(addressBytes, 0, pubKeyHash.length + 1);
        System.arraycopy(check, 0, addressBytes, pubKeyHash.length + 1, 4);
        return Base58.encode(addressBytes);
    }

    public static char[] charsFromBytes(byte[] bytes) {
        char[] chars = new char[bytes.length];
        for (int i = 0;
             i < bytes.length;
             i++) {
            chars[i] = (char) bytes[i];
        }
        return chars;
    }

    public static String toSegwitAddress(byte[] pubKeyHash) {
        assert (pubKeyHash.length == 20);

        int version = BitherjSettings.p2shHeader;;
        assert (version < 256 && version >= 0);

        byte[] scriptSig = new byte[pubKeyHash.length + 2];
        scriptSig[0] = 0x00;
        scriptSig[1] = (byte) pubKeyHash.length;
        System.arraycopy(pubKeyHash, 0, scriptSig, 2, pubKeyHash.length);
        byte[] addressBytes = Utils.sha256hash160(scriptSig);

        byte[] b = new byte[1 + addressBytes.length + 4];
        b[0] = (byte) version;
        System.arraycopy(addressBytes, 0, b, 1, addressBytes.length);
        byte[] check = doubleDigest(b, 0, addressBytes.length + 1);
        System.arraycopy(check, 0, b, addressBytes.length + 1, 4);
        return Base58.encode(b);

    }

    public static BigInteger decodeMPI(byte[] mpi, boolean hasLength) {
        byte[] buf;
        if (hasLength) {
            int length = (int) readUint32BE(mpi, 0);
            buf = new byte[length];
            System.arraycopy(mpi, 4, buf, 0, length);
        } else {
            buf = mpi;
        }
        if (buf.length == 0) {
            return BigInteger.ZERO;
        }
        boolean isNegative = (buf[0] & 0x80) == 0x80;
        if (isNegative) {
            buf[0] &= 0x7f;
        }
        BigInteger result = new BigInteger(buf);
        return isNegative ? result.negate() : result;
    }


    public static BigInteger decodeCompactBits(long compact) {
        int size = ((int) (compact >> 24)) & 0xFF;
        byte[] bytes = new byte[4 + size];
        bytes[3] = (byte) size;
        if (size >= 1) {
            bytes[4] = (byte) ((compact >> 16) & 0xFF);
        }
        if (size >= 2) {
            bytes[5] = (byte) ((compact >> 8) & 0xFF);
        }
        if (size >= 3) {
            bytes[6] = (byte) ((compact) & 0xFF);
        }
        return decodeMPI(bytes, true);
    }

    public static byte[] reverseBytes(byte[] bytes) {
        // We could use the XOR trick here but it's easier to understand if we don't. If we find
        // this is really a
        // performance issue the matter can be revisited.
        byte[] buf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            buf[i] = bytes[bytes.length - 1 - i];
        return buf;
    }

    public static void wipeBytes(byte[] bytes) {
        if (bytes == null) {
            return;
        }
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 0;
        }
        SecureRandom r = new SecureRandom();
        r.nextBytes(bytes);
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 0;
        }
    }

    public static boolean isEmpty(String str) {
        return str == null || str.equals("");
    }

    public static String joinString(String[] strs, String spiltStr) {
        String result = "";
        for (int i = 0; i < strs.length; i++) {
            String str = strs[i];
            if (!Utils.isEmpty(str)) {
                if (i < strs.length - 1) {
                    result = result + str + spiltStr;
                } else {
                    result = result + str;
                }
            }
        }
        return result;
    }

    public static boolean compareString(String str, String other) {
        if (str == null) {
            return other == null;
        } else {
            return other != null && str.equals(other);
        }

    }

    public static String stringToMD5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("md5算法未匹配");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }

    public static byte[] copyOf(byte[] in, int length) {
        byte[] out = new byte[length];
        System.arraycopy(in, 0, out, 0, Math.min(length, in.length));
        return out;
    }
    
    //------------------------------------------wystan add for bitcoin key about 201229------------------------------------------//

}
