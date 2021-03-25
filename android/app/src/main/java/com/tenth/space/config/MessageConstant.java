package com.tenth.space.config;

/**
 * @author : yingmu on 15-1-11.
 * @email : yingmu@mogujie.com.
 */
public interface MessageConstant {

    /**基础消息状态，表示网络层收发成功*/
    public final int   MSG_SENDING = 1;
    public final int    MSG_FAILURE = 2;
    public final int    MSG_SUCCESS = 3;

    /**图片消息状态，表示下载到本地、上传到服务器的状态*/
    public final int  IMAGE_UNLOAD=1;
    public final int  IMAGE_LOADING=2;
    public final int IMAGE_LOADED_SUCCESS =3;
    public final int IMAGE_LOADED_FAILURE =4;

    /**文件消息状态，表示下载到本地、上传到服务器的状态*/
    public final int  FILE_UNUPLOAD=1;
    public final int  FILE_UPLOADING=2;
    public final int  FILE_UPLOADED_SUCCESS =3;
    public final int  FILE_UPLOADED_FAILURE =4;
    public final int  FILE_UNLOAD=5;
    public final int  FILE_LOADING=6;
    public final int  FILE_LOADED_SUCCESS =7;
    public final int  FILE_LOADED_FAILURE =8;

    /**语音状态，未读与已读*/
    public final int   AUDIO_UNREAD =1;
    public final int   AUDIO_READED = 2;

    /**图片消息的前后常量*/
    public  final String IMAGE_MSG_START = "&$#@~^@[{:";
    public  final String IMAGE_MSG_END = ":}]&$~@#@";

}
