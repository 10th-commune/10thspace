package fastdfs;

import android.os.Environment;
import android.text.TextUtils;

import com.tenth.space.ui.helper.AudioPlayerHandler;
import com.tenth.space.utils.FileUtil;
import com.tenth.space.utils.Utils;

import fastdfs.common.NameValuePair;
import fastdfs.fastdfs.ClientGlobal;
import fastdfs.fastdfs.StorageClient;
import fastdfs.fastdfs.StorageServer;
import fastdfs.fastdfs.TrackerClient;
import fastdfs.fastdfs.TrackerServer;
import fastdfs.fastdfs.UploadRetCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;

/**
 * @author chengdu
 * @date 2019/7/13.
 */
public class FdfsUtil {

    public static final String FDFS_PROTOL = "fdfs://";
    private static final Logger LOGGER = LoggerFactory.getLogger(FdfsUtil.class);

    private static final String DEF_LOCAL_PATH = Environment.getExternalStorageDirectory() + "/DCIM/Fdfs";
    private static final String CONF_NAME = "F:\\Programs\\fastdfd_client_java_git_210119\\fastdfs-client-java\\fdfs_client.conf";//"fdfstest.conf";

    private StorageClient storageClient;

    private TrackerServer trackerServer;

    private String local_path; //download file path

    private String local_file_path;//upload file path

    private UploadRetCallback callback;

    public FdfsUtil(){
        local_path = DEF_LOCAL_PATH;
    }

    public FdfsUtil(String filePath, UploadRetCallback callback){
        local_file_path = filePath;
        this.callback = callback;
        local_path = DEF_LOCAL_PATH;
    }


    public FdfsUtil(String local_path){
        if(!Utils.isEmpty(local_path))
            this.local_path = local_path;// + "/fdfs/";
    }

    public void initStorageClient() throws Exception {
        ClientGlobal.init(CONF_NAME);
        LOGGER.info("network_timeout=" + ClientGlobal.g_network_timeout + "ms");
        LOGGER.info("charset=" + ClientGlobal.g_charset);
        TrackerClient tracker = new TrackerClient();
        trackerServer = tracker.getTrackerServer();
        StorageServer storageServer = null;
        storageClient = new StorageClient(trackerServer, storageServer);
    }

    public void closeClient() {
        LOGGER.info("close connection");
        if(storageClient != null){
            try {
               storageClient.close();
            }catch (Exception e){
                e.printStackTrace();
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    }

    public void writeByteToFile(byte[] fbyte, String fileName) throws IOException {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = new File(fileName);
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(fbyte);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                bos.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    public String[] upload(String fileName, String filePath) throws Exception{

        NameValuePair[] metaList = new NameValuePair[1];
        metaList[0] = new NameValuePair("fileName", fileName);
        File file = new File(filePath);//"F:\\Programs\\fastdfd_client_java_git_210119\\fastdfs-client-java\\bg1.png");
        InputStream inputStream = new FileInputStream(file);
        int length = inputStream.available();
        byte[] bytes = new byte[length];
        inputStream.read(bytes);
        String[] result = storageClient.upload_file(bytes, null, metaList);
        LOGGER.info("wystan result {}", Arrays.asList(result));

        return result;
      //  Assert.assertEquals(2, result.length);
    }

    public boolean download(String fileName, String filePath) throws Exception {
        int nPos = filePath.indexOf("/");
        if(-1 == nPos) {
            return false;
        }
        String[] uploadresult = {filePath.substring(0,nPos),filePath.substring(nPos+1)};// {"group1", "M00/00/00/wKhY0WAGm-KANcSwAR5Jqv4uEQA0447460"};
        byte[] result = storageClient.download_file(uploadresult[0], uploadresult[1]);
        File filep = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/");
        if (!filep.exists()) {
            filep.mkdirs();
        }

        String local_filename = Environment.getExternalStorageDirectory() + "/DCIM/Camera/" + fileName;
        writeByteToFile(result, local_filename);
        File file = new File(local_filename);
        return  file.isFile();
     //   Assert.assertTrue(file.isFile());
    }

    public void testUploadDownload() throws Exception {
        NameValuePair[] metaList = new NameValuePair[1];
        String local_filename = "build.PNG";
        metaList[0] = new NameValuePair("fileName", local_filename);
        File file = new File("F:\\Programs\\fastdfd_client_java_git_210119\\fastdfs-client-java\\精通比特币第二版.pdf");
        InputStream inputStream = new FileInputStream(file);
        int length = inputStream.available();
        byte[] bytes = new byte[length];
        inputStream.read(bytes);
        String[] result = storageClient.upload_file(bytes, null, metaList);
     //   Assert.assertTrue(storageClient.isConnected());
        // pool testOnborrow  isAvaliable
    //    Assert.assertTrue(storageClient.isAvaliable());
        LOGGER.info("result {}", Arrays.asList(result));
        byte[] resultbytes = storageClient.download_file(result[0], result[1]);
        writeByteToFile(resultbytes, local_filename);
        File downfile = new File(local_filename);
    //    Assert.assertTrue(downfile.isFile());
    }


    public String downloadSingle(String remoteUrl){

        try {
            if(remoteUrl.contains(FdfsUtil.FDFS_PROTOL)) {
                int nPos = remoteUrl.indexOf("|");
                if(-1!=nPos){
                    String fileName = remoteUrl.substring(6, nPos);

                    File filep = new File(local_path);
                    if (!filep.exists()) {
                        filep.mkdirs();
                    }

                    String local_filepath = local_path + "/" + fileName;

                    if(new File(local_filepath).isFile())
                        return local_filepath;

                    String fileRemotePath = remoteUrl.substring(nPos + 1);

                    int nPos1 = fileRemotePath.indexOf("/");
                    if (-1 != nPos1) {
                        initStorageClient();

                        String[] uploadresult = {fileRemotePath.substring(0, nPos1), fileRemotePath.substring(nPos1 + 1)};// {"group1", "M00/00/00/wKhY0WAGm-KANcSwAR5Jqv4uEQA0447460"};
                        byte[] result = storageClient.download_file(uploadresult[0], uploadresult[1]);
                        writeByteToFile(result, local_filepath);
                        File file = new File(local_filepath);
                        closeClient();

                        if(file.isFile())
                            return local_filepath;
                    }
                }
            }
        }
        catch (Exception e){
            closeClient();
        }

        return null;
    }


    public String[] uploadSingle(String fileName, String filePath){
        String[] result = {null};
        try{
            initStorageClient();
            NameValuePair[] metaList = new NameValuePair[1];
            metaList[0] = new NameValuePair("fileName", fileName);
            File file = new File(filePath);//"F:\\Programs\\fastdfd_client_java_git_210119\\fastdfs-client-java\\bg1.png");
            InputStream inputStream = new FileInputStream(file);
            int length = inputStream.available();
            byte[] bytes = new byte[length];
            inputStream.read(bytes);
            result = storageClient.upload_file(bytes, null, metaList);
            LOGGER.info("wystan result {}", Arrays.asList(result));
            closeClient();
        }
        catch (Exception e){
            closeClient();
        }

        return result;
    }


    public void asyncUpload(){
        Thread uploadThread = new Thread() {
            public void run() {
                try {
                    File file= new File(local_file_path);
                    if(file.exists()){
                        String rets[] = uploadSingle(FileUtil.getFileNameFromFullPath(local_file_path),local_file_path);
                        if(null!=callback) {
                            if (null != rets && rets.length == 2 && null!=rets[0] && null!=rets[1]) {
                                callback.onSuccess(FdfsUtil.FDFS_PROTOL + FileUtil.getFileNameFromFullPath(local_file_path) + "|" + rets[0] + "/" + rets[1]);
                            } else {
                                callback.onFailure();
                            }
                        }
                    }
                    else{
                        if(null!=callback) {
                            callback.onFailure();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if(null!=callback) {
                        callback.onFailure();
                    }
                }
            }
        };
        uploadThread.start();
    }
}
