package com.tenth.space.imservice.entity;

import com.tenth.space.DB.entity.IdCardEntity;
import com.tenth.space.DB.entity.MessageEntity;
import com.tenth.space.DB.entity.PeerEntity;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.config.DBConstant;
import com.tenth.space.config.MessageConstant;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.imservice.support.SequenceNumberMaker;
import com.tenth.space.utils.Base58;
import com.tenth.space.utils.PrivateKeyUtil;
import com.tenth.space.utils.UnsafeByteArrayOutputStream;
import com.tenth.space.utils.Utils;
import com.tenth.space.utils.crypto.ECKey;
import com.tenth.space.utils.crypto.SecureCharSequence;
import com.tenth.tools.EncryptTools;

import org.spongycastle.crypto.params.KeyParameter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import fastdfs.FdfsUtil;

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 */


public class FileMessage extends MessageEntity implements Serializable {

     private String local_fileName;
     private String local_filePath;
     private String remote_filePath;
     private String file_type;
     private int file_status;

     public FileMessage(){
         msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
     }

     private FileMessage(MessageEntity entity){
         /**父类的id*/
         id =  entity.getId();
         msgId  = entity.getMsgId();
         fromId = entity.getFromId();
         toId   = entity.getToId();
         sessionKey = entity.getSessionKey();
         content=entity.getContent();
         msgType=entity.getMsgType();
         displayType=entity.getDisplayType();
         status = entity.getStatus();
         created = entity.getCreated();
         updated = entity.getUpdated();

         signHash = entity.getSignHash();
         pubKey = entity.getPubKey();
     }

     public static FileMessage parseFromNet(MessageEntity entity){
         FileMessage textMessage = new FileMessage(entity);
         textMessage.setStatus(MessageConstant.MSG_SUCCESS);
         textMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
         textMessage.setLocal_fileName("");
         textMessage.setRemote_filePath("");

         String content = entity.getContent();
         int nPos1 = content.indexOf(FdfsUtil.FDFS_PROTOL);
         int nPos2 = content.indexOf("|");

         if(-1!=nPos1 && -1!=nPos2)
         {
             textMessage.setLocal_fileName(content.substring(nPos1+6,nPos2));
             textMessage.setRemote_filePath(content.substring(nPos2+1));
         }

         return textMessage;
     }

    public static FileMessage parseFromDB(MessageEntity entity){
        if(entity.getDisplayType()!=DBConstant.SHOW_ORIGIN_TEXT_TYPE){
            throw new RuntimeException("#TextMessage# parseFromDB,not SHOW_ORIGIN_TEXT_TYPE");
        }
        FileMessage textMessage = new FileMessage(entity);
        return textMessage;
    }

    public static FileMessage buildForSend(String filePath, UserEntity fromUser, PeerEntity peerEntity){
        int nPos = filePath.lastIndexOf("/");
        if(-1 == nPos) {
            return null;
        }

        FileMessage fileMessage = new FileMessage();
        fileMessage.setLocal_fileName(filePath.substring(nPos + 1));
        fileMessage.setLocal_filePath(filePath);
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        fileMessage.setFromId(fromUser.getPub_key());
        fileMessage.setToId(peerEntity.getPub_key());
        fileMessage.setUpdated(nowTime);
        fileMessage.setCreated(nowTime);
        fileMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
        fileMessage.setGIfEmo(true);
        int peerType = peerEntity.getType();
        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_TEXT
                : DBConstant.MSG_TYPE_SINGLE_TEXT;
        fileMessage.setMsgType(msgType);
        fileMessage.setStatus(MessageConstant.MSG_SENDING);

        fileMessage.buildSessionKey(true);

        fileMessage.sign();

        return fileMessage;
    }



    public String getLocal_fileName() {
        return local_fileName;
    }

    public void setLocal_fileName(String local_fileName) {
        this.local_fileName = local_fileName;
    }

    public String getLocal_filePath() {
        return local_filePath;
    }

    public void setLocal_filePath(String local_filePath) {
        this.local_filePath = local_filePath;
    }

    public String getRemote_filePath() {
        return remote_filePath;
    }

    public void setRemote_filePath(String remote_filePath) {
        this.remote_filePath = remote_filePath;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public String getFile_type() {
        return file_type;
    }

    /**
     * Not-null value.
     * DB的时候需要
     */
    @Override
    public String getContent() {
        content = FdfsUtil.FDFS_PROTOL + local_fileName + "|" + remote_filePath;//
        return content;
    }

    @Override
    public byte[] getSendContent() {
       try {

           /** 加密*/
           // String sendContent =new String(com.mogujie.tt.Security.getInstance().EncryptMsg(content));

           // 去掉加密，现在加密有问题
           //String sendContent= AESUtils.encrypt(content, Config.getKA());
           return getContent().getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
           e.printStackTrace();
       }
       return null;
    }

    public byte[] messageSerializeToStream(){
        int createTimeSize = 4;
        String md5Msg = EncryptTools.instance().toMD5(getContent());
        byte [] md5MsgByte = Utils.hexStringToByteArray(md5Msg);


        ByteArrayOutputStream stream = new UnsafeByteArrayOutputStream(createTimeSize + md5MsgByte.length);
        try {
            Utils.uint32ToByteStreamLE((long)getCreated(),stream);
            stream.write(md5MsgByte);
        }
        catch (IOException e){
            // Cannot happen, we are serializing to a memory stream.
        }

        return stream.toByteArray();
    }

    public void sign(){
        IdCardEntity idCardEntity = IMLoginManager.instance().getIdCardEntity();
        if(null == idCardEntity)//fromUser.getKeyParameter())
            return;

        CharSequence password = new SecureCharSequence("654321");

        if(null == PrivateKeyUtil.getDerivedKey()){
            PrivateKeyUtil.setDerivedKey(new KeyParameter(Utils.hexStringToByteArray(idCardEntity.getKeyParameter())));
        }

        ECKey key = PrivateKeyUtil.getECKeyFromSingleString(PrivateKeyUtil.getFullencryptPrivateKey(idCardEntity.getEncryptPrivakey()),password);
        if (key != null) {
            try {
                setPubKey(Base58.decode(idCardEntity.getPubKey()));
            }
            catch (Exception e){
                //logger.e("wystan onRecvMessage# get error pubKey");
            }
           // setPubKey(Utils.hexStringToByteArray(idCardEntity.getPubKey()));
            KeyParameter assKey = key.getKeyCrypter().deriveKey(password);
            setSignHash(key.sign(messageSerializeToStream(), assKey).encodeToDER());
        }
    }

    public boolean verify(){
        if(signHash == null || pubKey == null)
            return false;

        return ECKey.verify(messageSerializeToStream(),signHash, pubKey);
    }

    public byte[] getSignHash(){
        return signHash;
    }

    public byte[] getGetPubKey(){
        return pubKey;
    }
}
