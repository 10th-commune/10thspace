
package com.tenth.space.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

import com.tenth.space.app.IMApplication;
import com.tenth.space.config.SysConstant;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class FileUtil {
    public static String SDCardRoot;
    public static File updateFile;

    static {
        // 获取SD卡路径
        SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }

    //uri转文件绝对路径
    public static String getFilePathFromContentUri(Uri uri, ContentResolver contentResolver) {
        String filePath="";
        //url分为2中：1、file开头标示文件夹选中的图片；2、content开头；
        if (uri!=null){
            if (uri.toString().contains("file://")){

                filePath=uri.toString().split("file://")[1];
                return filePath;
            }else {
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = contentResolver.query(uri, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filePath = cursor.getString(columnIndex);
                cursor.close();
                return filePath;
            }
        }else {
            return filePath;
        }
    }

    /**
     * 创建文件夹
     *
     * @throws IOException
     */
    public static File createFileInSDCard(String fileName, String dir) {
        File file = new File(SDCardRoot + dir + File.separator + fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateFile = file;
        return file;
    }

    /**
     * 创建目录
     *
     * @param dir
     *
     * @return
     */
    public static File creatSDDir(String dir) {
        File dirFile = new File(SDCardRoot + dir + File.separator);
        dirFile.mkdirs();
        return dirFile;
    }

    /**
     * 检测文件是否存在
     */
    public static boolean isFileExist(String fileName, String path) {
        File file = new File(SDCardRoot + path + File.separator + fileName);
        return file.exists();
    }

    public static boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * 通过流往文件里写东西
     */
    public static File writeToSDFromInput(String path, String fileName, InputStream input) {

        File file = null;
        OutputStream output = null;
        try {
            file = createFileInSDCard(fileName, path);
            output = new FileOutputStream(file, false);
            byte buffer[] = new byte[4 * 1024];
            int temp;
            while ((temp = input.read(buffer)) != -1) {
                output.write(buffer, 0, temp);
            }
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 把字符串写入文件
     */
    public static File writeToSDFromInput(String path, String fileName, String data) {

        File file = null;
        OutputStreamWriter outputWriter = null;
        OutputStream outputStream = null;
        try {
            creatSDDir(path);
            file = createFileInSDCard(fileName, path);
            outputStream = new FileOutputStream(file, false);
            outputWriter = new OutputStreamWriter(outputStream);
            outputWriter.write(data);
            outputWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static String getFromCipherConnection(String actionUrl, String content, String path) {

        try {
            File[] files = new File[1];
            files[0] = new File(path);
            // content = CipherUtil.getCipherString(content);
            String BOUNDARY = java.util.UUID.randomUUID().toString();
            String PREFIX = "--", LINEND = "\r\n";
            String MULTIPART_FROM_DATA = "multipart/form-data";
            String CHARSET = "UTF-8";
            URL uri = new URL(actionUrl);
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setReadTimeout(5 * 1000); // 缓存的最长时间
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST");
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);
            // 首先组拼文本类型的参数
            StringBuilder sb = new StringBuilder();
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINEND);
            sb.append("Content-Disposition: form-data; name=\"userName\"" + LINEND);// \"userName\"
            sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
            sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
            sb.append(LINEND);
            sb.append(content);
            sb.append(LINEND);

            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
            outStream.write(sb.toString().getBytes());
            // 发送文件数据
            if (files != null) {
                for (File file : files) {
                    StringBuilder sb1 = new StringBuilder();
                    sb1.append(PREFIX);
                    sb1.append(BOUNDARY);
                    sb1.append(LINEND);
                    sb1.append("Content-Disposition: form-data; name=\"" + file.getName()
                            + "\"; filename=\"" + file.getName() + "\"" + LINEND);
                    sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET
                            + LINEND);
                    sb1.append(LINEND);
                    outStream.write(sb1.toString().getBytes());
                    try {
                        InputStream is = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while ((len = is.read(buffer)) != -1) {

                            outStream.write(buffer, 0, len);
                        }
                        is.close();
                        outStream.write(LINEND.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 请求结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);
            outStream.flush();
            outStream.close();
            // 得到响应码
            int res = conn.getResponseCode();
            if (res == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        (InputStream) conn.getInputStream()));
                String line = null;
                StringBuilder result = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
                in.close();
                conn.disconnect();// 断开连接
                return "true";
            } else {
                return "";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    public static byte[] getFileContent(String fileName) {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(fileName);
            int length = fin.available();
            byte[] bytes = new byte[length];
            fin.read(bytes);
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param file
     *
     * @Description 删除文件或文件夹
     */
    public static void delete(File file) {
        if (!file.exists()) {
            return; // 不存在直接返回
        }

        if (file.isFile()) {
            file.delete(); // 若是文件则删除后返回
            return;
        }

        // 若是目录递归删除后,并最后删除目录后返回
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete(); // 如果是空目录，直接删除
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]); // 递归删除子文件或子文件夹
            }
            file.delete(); // 删除最后的空目录
        }
        return;
    }

    /**
     * @param dirFile  目录文件
     * @param timeLine 时间分隔线
     *
     * @Description 删除目录下过旧的文件
     */
    public static void deleteHistoryFiles(File dirFile, long timeLine) {
        // 不存在或是文件直接返回
        if (!dirFile.exists() || dirFile.isFile()) {
            return;
        }

        try {
            // 如果是目录则删除过旧的文件
            if (dirFile.isDirectory()) {
                File[] childFiles = dirFile.listFiles();
                if (childFiles == null || childFiles.length == 0) {
                    return; // 如果是空目录就直接返回
                }
                // 遍历文件，删除过旧的文件
                Long fileTime;
                for (int i = 0; i < childFiles.length; i++) {
                    fileTime = childFiles[i].lastModified();
                    if (fileTime < timeLine) {
                        delete(childFiles[i]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void saveBitmap(Bitmap bm,String picName) {
       // File f = new File(SDCardRoot, picName);
        File f = new File(IMApplication.RootDirectory, picName);
//        if (f.exists()) {
//            f.delete();
//        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static Bitmap getDiskBitmap(String pathString)
    {
        Bitmap bitmap = null;
        try
        {
            File file = new File(pathString);
            if(file.exists())
            {
                bitmap = BitmapFactory.decodeFile(pathString);
            }
        } catch (Exception e)
        {
            // TODO: handle exception
        }


        return bitmap;
    }
    public static File save2File(String savePath, String saveName,
                                 String crashReport) {
        try {
            File dir = new File(savePath);
            if (!dir.exists())
                dir.mkdir();
            File file = new File(dir, saveName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(crashReport.toString().getBytes());
            fos.close();
            return file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String saveAudioResourceToFile(byte[] content, String userId) {
        try {
            String audioSavePath = CommonUtil.getAudioSavePath(userId);
            File file = new File(audioSavePath);
            FileOutputStream fops = new FileOutputStream(file);
            fops.write(content);
            fops.flush();
            fops.close();
            return audioSavePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String saveGifResourceToFile(byte[] content) {
        try {
            String gifSavePath = CommonUtil.getSavePath(SysConstant.FILE_SAVE_TYPE_IMAGE);
            File file = new File(gifSavePath);
            FileOutputStream fops = new FileOutputStream(file);
            fops.write(content);
            fops.flush();
            fops.close();
            return gifSavePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @SuppressWarnings("unused")
    private static Bitmap getBitmap(InputStream fs) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 1;
        Bitmap imgBitmap = BitmapFactory.decodeStream(fs, null, opts);
        if (imgBitmap != null) {
            int width = imgBitmap.getWidth();
            int height = imgBitmap.getHeight();
            imgBitmap = Bitmap.createScaledBitmap(imgBitmap, width, height,
                    true);
        }
        return imgBitmap;
    }

    public static long getFileLen(File file) {
        long total = 0;
        try {
            InputStream is = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = is.read(bytes)) != -1) {
                total += len;
            }
            is.close();
        } catch (Exception e) {

        }
        return total;
    }

    public static boolean isSdCardAvailuable() {

        boolean bRet = false;
        do {
            if (!Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                break;
            }

            if (CommonUtil.getSDFreeSize() < 5) {
                break;
            }

            bRet = true;
        } while (false);

        return bRet;
    }

    public static byte[] InputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();


        return imgdata;
    }

    public static byte[] File2byte(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    public static String getFileNameFromFullPath(String filepath) {
        if ((filepath != null) && (filepath.length() > 0)) {
            int dot = filepath.lastIndexOf('/');
            if ((dot > -1) && (dot < (filepath.length() - 1))) {
                return filepath.substring(dot + 1);
            }
        }
        return filepath;
    }

    public static File convertUriToFile(Activity activity, Uri uri) {
        File file = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            @SuppressWarnings("deprecation")
            Cursor actualimagecursor = activity.managedQuery(uri, proj, null,
                    null, null);
            if (actualimagecursor != null) {
                int actual_image_column_index = actualimagecursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                actualimagecursor.moveToFirst();
                String img_path = actualimagecursor
                        .getString(actual_image_column_index);

                if (Utils.isEmpty(img_path)){
                    CursorLoader loader = new CursorLoader(activity, uri, proj, null,
                            null, null);
                    Cursor cursor = loader.loadInBackground();

                    if (cursor != null) {
                        cursor.moveToFirst();
                        img_path = cursor.getString(cursor.getColumnIndex(proj[0]));
                        cursor.close();
                    }

                    if (Utils.isEmpty(img_path)){
                        Cursor cursor2 = activity.getContentResolver().query(uri, proj,
                                null, null, null);
                        if (cursor2 != null) {
                            cursor2.moveToFirst();
                            img_path = cursor2.getString(cursor2.getColumnIndex(proj[0]));
                            cursor2.close();
                        }
                    }
                }

                if (!Utils.isEmpty(img_path)) {
                    file = new File(img_path);
                }


            } else {

                file = new File(new URI(uri.toString()));
                if (file.exists()) {
                    return file;
                }

            }
        } catch (Exception e) {
        }
        return file;

    }

    public static String convertUriToFilePath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
//                Log.i(TAG,"isDownloadsDocument***"+uri.toString());
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
//                Log.i(TAG,"isMediaDocument***"+uri.toString());
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
//            Log.i(TAG,"content***"+uri.toString());
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            Log.i(TAG,"file***"+uri.toString());
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}