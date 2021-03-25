package com.tenth.space.imservice.entity;

import android.text.TextUtils;

import com.tenth.space.DB.entity.PeerEntity;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.config.DBConstant;
import com.tenth.space.DB.entity.MessageEntity;
import com.tenth.space.config.MessageConstant;
import com.tenth.space.utils.CommonUtil;
import com.tenth.space.utils.FileUtil;
import com.tenth.space.imservice.support.SequenceNumberMaker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 *
 */
public class AudioMessage extends MessageEntity implements Serializable{

    private String audioPath = "";
    private int audiolength =0 ;
    private int readStatus = MessageConstant.AUDIO_UNREAD;
    private String url = "";

    public AudioMessage(){
        msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
    }

    private AudioMessage(MessageEntity entity){
        // 父类主键
        id =  entity.getId();
        msgId  = entity.getMsgId();
        fromId = entity.getFromId();
        toId   = entity.getToId();
        content=entity.getContent();
        msgType=entity.getMsgType();
        sessionKey = entity.getSessionKey();
        displayType=entity.getDisplayType();
        status = entity.getStatus();
        created = entity.getCreated();
        updated = entity.getUpdated();
    }


    public static AudioMessage parseFromDB(MessageEntity entity)  {
        if(entity.getDisplayType() != DBConstant.SHOW_AUDIO_TYPE){
           throw new RuntimeException("#AudioMessage# parseFromDB,not SHOW_AUDIO_TYPE");
        }
        AudioMessage audioMessage = new AudioMessage(entity);
        // 注意坑 啊
        String originContent = entity.getContent();

        JSONObject extraContent = null;
        try {
            extraContent = new JSONObject(originContent);
            audioMessage.setAudioPath(extraContent.getString("audioPath"));
            audioMessage.setAudiolength(extraContent.getInt("audiolength"));
            audioMessage.setReadStatus(extraContent.getInt("readStatus"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return audioMessage;
    }

    public static AudioMessage buildForSend(float audioLen,String audioSavePath,UserEntity fromUser,PeerEntity peerEntity){
        int tLen = (int) (audioLen + 0.5);
        tLen = tLen < 1 ? 1 : tLen;
        if (tLen < audioLen) {
            ++tLen;
        }

        int nowTime = (int) (System.currentTimeMillis() / 1000);
        AudioMessage audioMessage = new AudioMessage();
        audioMessage.setFromId(fromUser.getPub_key());
        audioMessage.setToId(peerEntity.getPub_key());
        audioMessage.setCreated(nowTime);
        audioMessage.setUpdated(nowTime);
        int peerType = peerEntity.getType();
        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_AUDIO :
                DBConstant.MSG_TYPE_SINGLE_AUDIO;
        audioMessage.setMsgType(msgType);

        audioMessage.setAudioPath(audioSavePath);
        audioMessage.setAudiolength(tLen);
        audioMessage.setReadStatus(MessageConstant.AUDIO_READED);
        audioMessage.setDisplayType(DBConstant.SHOW_AUDIO_TYPE);
        audioMessage.setStatus(MessageConstant.MSG_SENDING);
        audioMessage.buildSessionKey(true);
        return audioMessage;
    }


    /**
     * Not-null value.
     * DB 存数解析的时候需要
     */
    @Override
    public String getContent() {
        JSONObject extraContent = new JSONObject();
        try {
            extraContent.put("audioPath",audioPath);
            extraContent.put("audiolength",audiolength);
            extraContent.put("readStatus",readStatus);
            String audioContent = extraContent.toString();
            return audioContent;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public byte[] getSendContent() {
        /*
        byte[] result = new byte[4];
        result = CommonUtil.intToBytes(audiolength);
        if (TextUtils.isEmpty(audioPath)) {
            return result;
        }

        byte[] bytes = FileUtil.getFileContent(audioPath);
        if (bytes == null) {
            return bytes;
        }
        int contentLength = bytes.length;
        byte[] byteAduioContent = new byte[4 + contentLength];
        System.arraycopy(result, 0, byteAduioContent, 0, 4);
        System.arraycopy(bytes, 0, byteAduioContent, 4, contentLength);
        return byteAduioContent;
         */

        JSONObject extraContent = new JSONObject();
        try {
            extraContent.put("audiolength",audiolength);
            extraContent.put("url",url);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String sendContent = extraContent.toString();

        //String  encrySendContent =new String(com.mogujie.tt.Security.getInstance().EncryptMsg(sendContent));
        String  encrySendContent= sendContent;//AESUtils.encrypt(sendContent, Config.getKA()); //wystan disable encrypt 210121

        try {
            return encrySendContent.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    /***-------------------------------set/get----------------------------------*/
    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public int getAudiolength() {
        return audiolength;
    }

    public void setAudiolength(int audiolength) {
        this.audiolength = audiolength;
    }

    public int getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
