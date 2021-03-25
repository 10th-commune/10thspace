package com.tenth.space.imservice.service;

import android.app.IntentService;
import android.content.Intent;

import android.text.TextUtils;

import com.tenth.space.DB.entity.MessageEntity;
import com.tenth.space.config.DBConstant;
import com.tenth.space.config.SysConstant;
import com.tenth.space.imservice.entity.AudioMessage;
import com.tenth.space.imservice.entity.ImageMessage;
import com.tenth.space.imservice.event.MessageEvent;
import com.tenth.space.utils.FileUtil;
import com.tenth.space.utils.Logger;

import java.io.File;

import de.greenrobot.event.EventBus;
import fastdfs.FdfsUtil;

public class FastdfsUploadService extends IntentService {//wystan add for upload small files(audio, image) 210129

    private static Logger logger = Logger.getLogger(FastdfsUploadService.class);

    public FastdfsUploadService(){
        super("FastdfsUploadService");
    }

    public FastdfsUploadService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        MessageEntity messageEntity = (MessageEntity)intent.getSerializableExtra(SysConstant.DFS_UPLOAD_FILE_INTENT_PARAMS);

        String filePath = "";
        if(DBConstant.SHOW_IMAGE_TYPE == messageEntity.getDisplayType())
            filePath = ((ImageMessage)messageEntity).getPath();
        else if(DBConstant.SHOW_AUDIO_TYPE == messageEntity.getDisplayType())
            filePath = ((AudioMessage)messageEntity).getAudioPath();

        String result = null;

        File file= new File(filePath);
        if(file.exists()){
            result = upLoad2Fastdfs(FileUtil.getFileNameFromFullPath(filePath), filePath);
        }

        if (TextUtils.isEmpty(result)) {
            logger.i("upload image faild,cause by result is empty/null");
            EventBus.getDefault().postSticky(new MessageEvent(DBConstant.SHOW_IMAGE_TYPE == messageEntity.getDisplayType() ?
                    MessageEvent.Event.IMAGE_UPLOAD_FAILD : MessageEvent.Event.AUDIO_UPLOAD_FAILD
                    ,messageEntity));
        } else {
            logger.i("upload image succcess,imageUrl is %s %s %s",result, messageEntity.getFromId(), messageEntity.getId());
            String fileUrl = result;
            if(DBConstant.SHOW_IMAGE_TYPE == messageEntity.getDisplayType())
               ((ImageMessage)messageEntity).setUrl(fileUrl);
            else if(DBConstant.SHOW_AUDIO_TYPE == messageEntity.getDisplayType())
                ((AudioMessage)messageEntity).setUrl(fileUrl);

            EventBus.getDefault().postSticky(new MessageEvent(
                    DBConstant.SHOW_IMAGE_TYPE == messageEntity.getDisplayType() ?
                            MessageEvent.Event.IMAGE_UPLOAD_SUCCESS : MessageEvent.Event.AUDIO_UPLOAD_SUCCESS
                    ,messageEntity));
        }
    }

    private String upLoad2Fastdfs(final String fileName,final String filePath)  {
       String result = null;

        String[] rets = new FdfsUtil().uploadSingle(fileName, filePath);
        if(null!=rets && rets.length == 2 && null!=rets[0] && null!=rets[1])
            result = FdfsUtil.FDFS_PROTOL + fileName + "|" + rets[0] + "/" + rets[1];

        logger.i("wystan upLoad2Fastdfs:%s",result);
        return result;
    }
}
