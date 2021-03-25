package com.tenth.space.utils.runnable;

import android.graphics.Bitmap;
import android.os.Handler;

import com.tenth.space.DB.entity.IdCardEntity;
import com.tenth.space.utils.Base58;
import com.tenth.space.utils.ImageManageUtil;
import com.tenth.space.utils.PrivateKeyUtil;
import com.tenth.space.utils.Utils;
import com.tenth.space.utils.crypto.ECKey;
import com.tenth.space.utils.crypto.SecureCharSequence;
import com.tenth.space.utils.xrandom.XRandom;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.crypto.params.KeyParameter;

import java.io.File;

public class GetNewIdCardRunnable extends BaseRunnable {
    private int createType;//0:createnew   1:importfrom pic
    private File picFile;


    public GetNewIdCardRunnable(Handler handler)
    {
        createType = 0;
        setHandler(handler);
    }

    public GetNewIdCardRunnable(File picFile,Handler handler)
    {
        createType = 1;
        this.picFile = picFile;
        setHandler(handler);
    }

    public void run()
    {
        try {

            if(0 == createType) {

                XRandom xRandom = new XRandom(null);
                ECKey ecKey = ECKey.generateECKey(xRandom);
                ecKey = PrivateKeyUtil.encrypt(ecKey, new SecureCharSequence("654321"));

                if(null != ecKey){
                    IdCardEntity idCardEntity = new IdCardEntity(ecKey.toAddress(), PrivateKeyUtil.getEncryptedString(ecKey),Base58.encode(ecKey.getPubKey()), Base58.encode(PrivateKeyUtil.getDerivedKey().getKey()));

                    obtainMessage(HandlerMessage.MSG_CREATEID_SUCCESS, idCardEntity);
                }
                else{
                    obtainMessage(HandlerMessage.MSG_CREATEID_FAILURE);
                }
            }
            else if(1 == createType){

                if (picFile != null) {
                    Bitmap bmp = ImageManageUtil.getBitmapNearestSize(picFile,
                            ImageManageUtil.IMAGE_SIZE);
                    boolean bCreateWithRid = false;

                    if (bmp != null) {
                        String qrResult = ImageManageUtil.decodeQrCodeFromBitmap(bmp);
                        if (null != qrResult) {
                            if(qrResult.contains("http://")&&qrResult.contains("rid=")) {//create with referral_code
                                bCreateWithRid = true;
                                obtainMessage(HandlerMessage.MSG_REFERRAL_DECODE_SUCCESS, qrResult.substring(qrResult.lastIndexOf("rid=")+4));
                                XRandom xRandom = new XRandom(null);
                                ECKey ecKey = ECKey.generateECKey(xRandom);
                                ecKey = PrivateKeyUtil.encrypt(ecKey, new SecureCharSequence("654321"));

                                if(null != ecKey){
                                    IdCardEntity idCardEntity = new IdCardEntity(ecKey.toAddress(), PrivateKeyUtil.getEncryptedString(ecKey),Base58.encode(ecKey.getPubKey()), Base58.encode(PrivateKeyUtil.getDerivedKey().getKey()));

                                    obtainMessage(HandlerMessage.MSG_CREATEID_SUCCESS, idCardEntity);
                                }
                                else{
                                    obtainMessage(HandlerMessage.MSG_CREATEID_FAILURE);
                                }
                            }
                            else{//import from pic
                                JSONObject jsonObject = new JSONObject(qrResult);
                                try {

                                    CharSequence password = new SecureCharSequence("654321");

                                    if (null == PrivateKeyUtil.getDerivedKey()) {
                                        PrivateKeyUtil.setDerivedKey(new KeyParameter(Base58.decode(jsonObject.getString("keyParameter"))));
                                    }
                                    ECKey key = PrivateKeyUtil.getECKeyFromSingleString(PrivateKeyUtil.getFullencryptPrivateKey(jsonObject.getString("encryptPrivakey")), password);

                                    IdCardEntity idCardEntity = new IdCardEntity(key.toAddress(), jsonObject.getString("encryptPrivakey"), Base58.encode(key.getPubKey())
                                            , jsonObject.getString("keyParameter"), jsonObject.getInt("created"));

                                    obtainMessage(HandlerMessage.MSG_IMPORTID_SUCCESS, idCardEntity);
                                } catch (JSONException e) {
                                    obtainMessage(HandlerMessage.MSG_IMPORTID_FAILURE);
                                    e.printStackTrace();
                                }
                            }
                        }
                        else{
                            if(bCreateWithRid)
                                obtainMessage(HandlerMessage.MSG_REFERRAL_DECODE_FAILURE);
                            else
                                obtainMessage(HandlerMessage.MSG_IMPORTID_FAILURE);
                        }
                    }
                    else{
                        if(bCreateWithRid)
                            obtainMessage(HandlerMessage.MSG_REFERRAL_DECODE_FAILURE);
                        else
                            obtainMessage(HandlerMessage.MSG_IMPORTID_FAILURE);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
};