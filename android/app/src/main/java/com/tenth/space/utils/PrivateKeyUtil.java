/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tenth.space.utils;

import com.tenth.space.utils.Logger;
import org.spongycastle.crypto.params.KeyParameter;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tenth.space.BitherjSettings;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.utils.crypto.DumpedPrivateKey;
import com.tenth.space.utils.crypto.ECKey;
import com.tenth.space.utils.crypto.EncryptedPrivateKey;
import com.tenth.space.utils.crypto.KeyCrypter;
import com.tenth.space.utils.crypto.KeyCrypterException;
import com.tenth.space.utils.crypto.KeyCrypterScrypt;
import com.tenth.space.utils.crypto.SecureCharSequence;
import com.tenth.space.utils.crypto.bip38.Bip38;
import com.tenth.space.utils.crypto.mnemonic.MnemonicCode;
import com.tenth.space.utils.crypto.mnemonic.MnemonicWordList;
import com.tenth.space.exception.AddressFormatException;
import com.tenth.space.utils.qrcode.QRCodeUtil;
import com.tenth.space.utils.qrcode.SaltForQRCode;

public class PrivateKeyUtil {
    private static final Logger log = Logger.getLogger(PrivateKeyUtil.class);


    public static String BACKUP_KEY_SPLIT_MUTILKEY_STRING = "\n";

    static String cloneContent;

    private static KeyParameter derivedKey = null;//wystan add for store aes key 201234

    public static String getEncryptedString(ECKey ecKey) {
        String salt = "1";
        if (ecKey.getKeyCrypter() instanceof KeyCrypterScrypt) {
            KeyCrypterScrypt scrypt = (KeyCrypterScrypt) ecKey.getKeyCrypter();
            salt = Utils.bytesToHexString(scrypt.getSalt());
        }
        EncryptedPrivateKey key = ecKey.getEncryptedPrivateKey();
        return Utils.bytesToHexString(key.getEncryptedBytes()) + QRCodeUtil.QR_CODE_SPLIT + Utils  //QR_CODE_SPLIT
                .bytesToHexString(key.getInitialisationVector()) + QRCodeUtil.QR_CODE_SPLIT + salt;
    }

    public static ECKey getECKeyFromSingleString(String str, CharSequence password) {
        try {

            DecryptedECKey decryptedECKey = decryptionECKey(str, password, false);

            if (decryptedECKey != null && decryptedECKey.ecKey != null) {
                return decryptedECKey.ecKey;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static DecryptedECKey decryptionECKey(String str, CharSequence password, boolean needPrivteKeyText) throws Exception {
        String[] strs = QRCodeUtil.splitOfPasswordSeed(str);
        if (strs.length != 3) {
            log.e("decryption: PrivateKeyFromString format error");
            return null;
        }
        byte[] temp = Utils.hexStringToByteArray(strs[2]);
        if (temp.length != KeyCrypterScrypt.SALT_LENGTH + 1 && temp.length != KeyCrypterScrypt.SALT_LENGTH) {
            //log.e("decryption:  salt lenth is {} not {}", temp.length, KeyCrypterScrypt.SALT_LENGTH + 1);
            return null;
        }
        SaltForQRCode saltForQRCode = new SaltForQRCode(temp);
        byte[] salt = saltForQRCode.getSalt();
        boolean isCompressed = true;//saltForQRCode.isCompressed();//wystan modify for compressed flag 210205
        boolean isFromXRandom = saltForQRCode.isFromXRandom();//true;//

        KeyCrypterScrypt crypter = new KeyCrypterScrypt(salt);
        EncryptedPrivateKey epk = new EncryptedPrivateKey(Utils.hexStringToByteArray
                (strs[1]), Utils.hexStringToByteArray(strs[0]));


        byte[] decrypted = crypter.decrypt(epk, crypter.deriveKey(password));


        ECKey ecKey = null;
        SecureCharSequence privateKeyText = null;
        if (needPrivteKeyText) {
            DumpedPrivateKey dumpedPrivateKey = new DumpedPrivateKey(decrypted, isCompressed);
            privateKeyText = dumpedPrivateKey.toSecureCharSequence();
            dumpedPrivateKey.clearPrivateKey();
        } else {
            BigInteger bigInteger = new BigInteger(1, decrypted);
            byte[] pub = ECKey.publicKeyFromPrivate(bigInteger, isCompressed);

            ecKey = new ECKey(epk, pub, crypter);
            ecKey.setFromXRandom(isFromXRandom);

        }
        Utils.wipeBytes(decrypted);
        return new DecryptedECKey(ecKey, privateKeyText);
    }

    public static SecureCharSequence getDecryptPrivateKeyString(String str, CharSequence password) {
        try {
            DecryptedECKey decryptedECKey = decryptionECKey(str, password, true);
            if (decryptedECKey != null && decryptedECKey.privateKeyText != null) {
                return decryptedECKey.privateKeyText;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String changePassword(String str, CharSequence oldpassword, CharSequence newPassword) {
        String[] strs = QRCodeUtil.splitOfPasswordSeed(str);
        if (strs.length != 3) {
            log.e("change Password: PrivateKeyFromString format error");
            return null;
        }

        byte[] temp = Utils.hexStringToByteArray(strs[2]);
        if (temp.length != KeyCrypterScrypt.SALT_LENGTH + 1 && temp.length != KeyCrypterScrypt.SALT_LENGTH) {
       //     log.e("decryption:  salt lenth is {} not {}", temp.length, KeyCrypterScrypt.SALT_LENGTH + 1);
            return null;
        }
        byte[] salt = new byte[KeyCrypterScrypt.SALT_LENGTH];
        if (temp.length == KeyCrypterScrypt.SALT_LENGTH) {
            salt = temp;
        } else {
            System.arraycopy(temp, 1, salt, 0, salt.length);
        }
        KeyCrypterScrypt crypter = new KeyCrypterScrypt(salt);
        EncryptedPrivateKey epk = new EncryptedPrivateKey(Utils.hexStringToByteArray
                (strs[1]), Utils.hexStringToByteArray(strs[0]));

        byte[] decrypted = crypter.decrypt(epk, crypter.deriveKey(oldpassword));
        EncryptedPrivateKey encryptedPrivateKey = crypter.encrypt(decrypted, crypter.deriveKey(newPassword));
        byte[] newDecrypted = crypter.decrypt(encryptedPrivateKey, crypter.deriveKey(newPassword));
        if (!Arrays.equals(decrypted, newDecrypted)) {
            throw new KeyCrypterException("change Password, cannot be successfully decrypted after encryption so aborting wallet encryption.");
        }
        Utils.wipeBytes(decrypted);
        Utils.wipeBytes(newDecrypted);
        return Utils.bytesToHexString(encryptedPrivateKey.getEncryptedBytes())
                + QRCodeUtil.QR_CODE_SPLIT + Utils.bytesToHexString(encryptedPrivateKey.getInitialisationVector())
                + QRCodeUtil.QR_CODE_SPLIT + strs[2];

    }



    public static String getCloneContent() {
        return cloneContent;
    }

    /**
     * will release key
     *
     * @param key
     * @param password
     * @return
     */
    public static ECKey encrypt(ECKey key, CharSequence password) {
        KeyCrypter scrypt = new KeyCrypterScrypt();
        KeyParameter derivedKey = getDerivedKey()==null ? scrypt.deriveKey(password) : getDerivedKey();
        ECKey encryptedKey = key.encrypt(scrypt, derivedKey);
        setDerivedKey(derivedKey);//wystan add for store aes key 201234
        // Check that the encrypted key can be successfully decrypted.
        // This is done as it is a critical failure if the private key cannot be decrypted successfully
        // (all bitcoin controlled by that private key is lost forever).
        // For a correctly constructed keyCrypter the encryption should always be reversible so it is just being as cautious as possible.
        if (!ECKey.encryptionIsReversible(key, encryptedKey, scrypt, derivedKey)) {
            // Abort encryption
            throw new KeyCrypterException("The key " + key.toString() + " cannot be successfully decrypted after encryption so aborting wallet encryption.");
        }
        key.clearPrivateKey();
        return encryptedKey;
    }

    private static class DecryptedECKey {
        public DecryptedECKey(ECKey ecKey, SecureCharSequence privateKeyText) {
            this.ecKey = ecKey;
            this.privateKeyText = privateKeyText;
        }

        public ECKey ecKey;
        public SecureCharSequence privateKeyText;

    }

    public static boolean verifyMessage(String address, String messageText, String signatureText) {
        // Strip CRLF from signature text
        try {
            signatureText = signatureText.replaceAll("\n", "").replaceAll("\r", "");

            ECKey key = ECKey.signedMessageToKey(messageText, signatureText);
            String signAddress = key.toAddress();
            return Utils.compareString(address, signAddress);
        } catch (SignatureException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static String formatEncryptPrivateKeyForDb(String encryptPrivateKey) {
        if (Utils.isEmpty(encryptPrivateKey)) {
            return encryptPrivateKey;
        }
        String[] strs = QRCodeUtil.splitOfPasswordSeed(encryptPrivateKey);
        byte[] temp = Utils.hexStringToByteArray(strs[2]);
        byte[] salt = new byte[KeyCrypterScrypt.SALT_LENGTH];
        if (temp.length == KeyCrypterScrypt.SALT_LENGTH + 1) {
            System.arraycopy(temp, 1, salt, 0, salt.length);
        } else {
            salt = temp;
        }
        strs[2] = Utils.bytesToHexString(salt);
        return Utils.joinString(strs, QRCodeUtil.QR_CODE_SPLIT);

    }

    public static String getFullencryptPrivateKey(String encryptPrivKey) {
        log.i("wystan getFullencryptPrivateKey start");
        String[] strings = QRCodeUtil.splitString(encryptPrivKey);
        byte[] salt = Utils.hexStringToByteArray(strings[2]);
        if (salt.length == KeyCrypterScrypt.SALT_LENGTH) {
            SaltForQRCode saltForQRCode = new SaltForQRCode(salt, false, false);
            strings[2] = Utils.bytesToHexString(saltForQRCode.getQrCodeSalt());
        }
        log.i("wystan getFullencryptPrivateKey end");
        return Utils.joinString(strings, QRCodeUtil.QR_CODE_SPLIT);
    }

    public static String getFullencryptHDMKeyChain(boolean isFromXRandom, String encryptPrivKey) {
        String[] strings = QRCodeUtil.splitString(encryptPrivKey);
        byte[] salt = Utils.hexStringToByteArray(strings[2]);
        if (salt.length == KeyCrypterScrypt.SALT_LENGTH) {
            SaltForQRCode saltForQRCode = new SaltForQRCode(salt, true, isFromXRandom);
            strings[2] = Utils.bytesToHexString(saltForQRCode.getQrCodeSalt()).toUpperCase();
        }
        return Utils.joinString(strings, QRCodeUtil.QR_CODE_SPLIT);
    }

    public static KeyParameter getDerivedKey() {
        return derivedKey;
    }

    public static void setDerivedKey(KeyParameter derivedKey) {
        PrivateKeyUtil.derivedKey = derivedKey;
    }
}
