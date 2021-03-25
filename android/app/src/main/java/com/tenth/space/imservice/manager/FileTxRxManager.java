package com.tenth.space.imservice.manager;

import com.tenth.space.imservice.entity.FileMessage;
import com.tenth.space.imservice.event.MessageEvent;
import com.tenth.space.utils.Logger;

import de.greenrobot.event.EventBus;
import fastdfs.FdfsUtil;

/**
 * Created by wing on 2016/7/11.
 */
public class FileTxRxManager extends IMManager {

    private Logger logger = Logger.getLogger(FileTxRxManager.class);

    private boolean isInited = false;
    private FdfsUtil fdfsUtil;

    public FileTxRxManager() {
    }

    @Override
    public void doOnStart() {
    }

    @Override
    public void reset() {
        if(isInited)
            fdfsUtil.closeClient();

        isInited = false;
    }

    // 单例
    private static FileTxRxManager inst = new FileTxRxManager();

    public static FileTxRxManager instance() {
        return inst;
    }

    private void Init(){
        if(!isInited){
            fdfsUtil = new FdfsUtil();
            try {
                fdfsUtil.initStorageClient();
                isInited = true;
            }
            catch (Exception e){

            }
        }
    }

    public void sendFile(final FileMessage fileMessage)  {
        if(!isInited)
            Init();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String []ret = fdfsUtil.upload(fileMessage.getLocal_fileName(), fileMessage.getLocal_filePath());

                    if(null!=ret) {
                        logger.i("wystan sendFile:%d %s %s", ret.length, ret[0], ret[1]);
                        fileMessage.setRemote_filePath(ret[0] + "/" + ret[1]);
                        fileMessage.sign();
                        EventBus.getDefault().postSticky(new MessageEvent(
                                MessageEvent.Event.FILE_UPLOAD_SUCCESS
                                ,fileMessage));
                    }
                    else{
                        EventBus.getDefault().postSticky(new MessageEvent(
                                MessageEvent.Event.FILE_UPLOAD_FAILD
                                ,fileMessage));
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void downloadFile(final FileMessage fileMessage)  {
        if(!isInited)
            Init();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.i("wystan downloadFile start:%s %s",fileMessage.getLocal_fileName(), fileMessage.getRemote_filePath());
                    boolean ret = fdfsUtil.download(fileMessage.getLocal_fileName(), fileMessage.getRemote_filePath());

                    if(ret) {
                        logger.i("wystan downloadFile success");
                        EventBus.getDefault().postSticky(new MessageEvent(
                                MessageEvent.Event.FILE_DOWNLOAD_SUCCESS
                                ,fileMessage));
                    }
                    else{
                        logger.i("wystan downloadFile faild");
                        EventBus.getDefault().postSticky(new MessageEvent(
                                MessageEvent.Event.FILE_DOWNLOAD_FAILD
                                ,fileMessage));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}