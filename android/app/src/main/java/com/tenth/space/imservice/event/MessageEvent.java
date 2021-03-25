package com.tenth.space.imservice.event;

import com.tenth.space.DB.entity.MessageEntity;

import java.util.ArrayList;

/**
 * @author : yingmu on 14-12-30.
 * @email : yingmu@mogujie.com.
 *
 */
public class MessageEvent {

    private ArrayList<MessageEntity> msgList;
    private Event event;

    public MessageEvent(){
    }

    public MessageEvent(Event event){
        //默认值 初始化使用
        this.event = event;
    }

    public MessageEvent(Event event,MessageEntity entity){
        //默认值 初始化使用
        this.event = event;
        msgList = new ArrayList<>(1);
        msgList.add(entity);
    }

    public enum Event{
        NONE,
        ISNOT_FRIEND,
        HISTORY_MSG_OBTAIN,

        SENDING_MESSAGE,

        ACK_SEND_MESSAGE_OK,
        ACK_SEND_MESSAGE_TIME_OUT,
        ACK_SEND_MESSAGE_FAILURE,

        //---------- small files(image,audio)--------//
        HANDLER_IMAGE_UPLOAD_FAILD,
        IMAGE_UPLOAD_FAILD,
        HANDLER_IMAGE_UPLOAD_SUCCESS,
        IMAGE_UPLOAD_SUCCESS,

        HANDLER_AUDIO_UPLOAD_FAILD,
        AUDIO_UPLOAD_FAILD,
        HANDLER_AUDIO_UPLOAD_SUCCESS,
        AUDIO_UPLOAD_SUCCESS,
        //---------- small files(image,audio)--------//


        //---------- large files(filesend)--------//
        HANDLER_FILE_UPLOAD_FAILD,
        FILE_UPLOAD_FAILD,
        HANDLER_FILE_UPLOAD_SUCCESS,
        FILE_UPLOAD_SUCCESS,

        HANDLER_FILE_DOWNLOAD_FAILD,
        FILE_DOWNLOAD_FAILD,
        HANDLER_FILE_DOWNLOAD_SUCCESS,
        FILE_DOWNLOAD_SUCCESS,

        //---------- large files(filesend)--------//

     }

    public MessageEntity getMessageEntity() {
        if(msgList == null || msgList.size() <=0){
            return null;
        }
        return msgList.get(0);
    }

    public void setMessageEntity(MessageEntity messageEntity) {
        if(msgList == null){
            msgList = new ArrayList<>();
        }
        msgList.clear();
        msgList.add(messageEntity);
    }

    public ArrayList<MessageEntity> getMsgList() {
        return msgList;
    }

    public void setMsgList(ArrayList<MessageEntity> msgList) {
        this.msgList = msgList;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
