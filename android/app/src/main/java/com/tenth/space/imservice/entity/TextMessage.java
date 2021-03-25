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
import com.tenth.space.utils.pinyin.Base64;
import com.tenth.tools.EncryptTools;

import org.spongycastle.crypto.params.KeyParameter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 */


public class TextMessage extends MessageEntity implements Serializable {

     public static String quote_sep = "|_quote_|";
     public TextMessage(){
         msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
     }

     private TextMessage(MessageEntity entity){
         /**父类的id*/
         id =  entity.getId();
         msgId  = entity.getMsgId();
         fromId = entity.getFromId();
         toId   = entity.getToId();
         sessionKey = entity.getSessionKey();
         content=entity.getContent();
         quoteContent=entity.getQuoteContent();
         msgType=entity.getMsgType();
         displayType=entity.getDisplayType();
         status = entity.getStatus();
         created = entity.getCreated();
         updated = entity.getUpdated();

         signHash = entity.getSignHash();
         pubKey = entity.getPubKey();
     }

     public static TextMessage parseFromNet(MessageEntity entity){
         TextMessage textMessage = new TextMessage(entity);
         textMessage.setStatus(MessageConstant.MSG_SUCCESS);
         textMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
         if(textMessage.getContent().contains(quote_sep)){
             textMessage.separaContentQuote();
         }
         return textMessage;
     }

    public static TextMessage parseFromDB(MessageEntity entity){
        if(entity.getDisplayType()!=DBConstant.SHOW_ORIGIN_TEXT_TYPE){
            throw new RuntimeException("#TextMessage# parseFromDB,not SHOW_ORIGIN_TEXT_TYPE");
        }
        TextMessage textMessage = new TextMessage(entity);
        return textMessage;
    }

    public static TextMessage buildForSend(String content,UserEntity fromUser,PeerEntity peerEntity){
        TextMessage textMessage = new TextMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        textMessage.setFromId(fromUser.getPub_key());
        textMessage.setToId(peerEntity.getPub_key());
        textMessage.setUpdated(nowTime);
        textMessage.setCreated(nowTime);
        textMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
        textMessage.setGIfEmo(true);
        int peerType = peerEntity.getType();
        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_TEXT
                : DBConstant.MSG_TYPE_SINGLE_TEXT;
        textMessage.setMsgType(msgType);
        textMessage.setStatus(MessageConstant.MSG_SENDING);
        // 内容的设定
        textMessage.setContent(content);
        textMessage.buildSessionKey(true);

        textMessage.sign();

        return textMessage;
    }

    public static TextMessage buildForSend(String content, String quoteContent,UserEntity fromUser,PeerEntity peerEntity){
        TextMessage textMessage = new TextMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        textMessage.setFromId(fromUser.getPub_key());
        textMessage.setToId(peerEntity.getPub_key());
        textMessage.setUpdated(nowTime);
        textMessage.setCreated(nowTime);
        textMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
        textMessage.setGIfEmo(true);
        int peerType = peerEntity.getType();
        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_TEXT
                : DBConstant.MSG_TYPE_SINGLE_TEXT;
        textMessage.setMsgType(msgType);
        textMessage.setStatus(MessageConstant.MSG_SENDING);
        // 内容的设定
        textMessage.setContent(content);
        textMessage.setQuoteContent(quoteContent);
        textMessage.buildSessionKey(true);

        textMessage.sign();

        return textMessage;
    }


    /**
     * Not-null value.
     * DB的时候需要
     */
    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getQuoteContent() {
        return quoteContent;
    }

    @Override
    public byte[] getSendContent() {
       try {

           /** 加密*/
           // String sendContent =new String(com.mogujie.tt.Security.getInstance().EncryptMsg(content));

           // 去掉加密，现在加密有问题
           //String sendContent= AESUtils.encrypt(content, Config.getKA());
           return Utils.isEmpty(quoteContent) ? content.getBytes("utf-8") : (content + quote_sep + quoteContent).getBytes("utf-8");//;
        } catch (UnsupportedEncodingException e) {
           e.printStackTrace();
       }
       return null;
    }

    public byte[] messageSerializeToStream(){
        int createTimeSize = 0;
        String md5Msg = EncryptTools.instance().toMD5(content + (Utils.isEmpty(quoteContent)?"":quoteContent));//getContent()
        byte [] md5MsgByte = Utils.doubleDigest(Utils.hexStringToByteArray(md5Msg));//Utils.hexStringToByteArray(md5Msg);

        ByteArrayOutputStream stream = new UnsafeByteArrayOutputStream(createTimeSize + md5MsgByte.length);
        try {
           // Utils.uint32ToByteStreamLE((long)getCreated(),stream);
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
            try {
                PrivateKeyUtil.setDerivedKey(new KeyParameter(Base58.decode(idCardEntity.getKeyParameter())));
            }
            catch (Exception e){
                //logger.e("wystan onRecvMessage# get error pubKey");
            }
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
        if(null == signHash)
            setSignHash("ak");
        return signHash;
    }

    public byte[] getGetPubKey(){
        return pubKey;
    }

    public void separaContentQuote(){
        if(content.contains(quote_sep)){
            int nindex = content.indexOf(quote_sep);
            quoteContent = content.substring(nindex + quote_sep.length());
            content = content.substring(0,nindex);
        }
    }
}
