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

package com.tenth.space;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import com.tenth.space.utils.Utils;

public class BitherjSettings {

    public static CharSequence[] ALL_SYSTEM_TIPS = {
            "",
            "小心钱财",
            "小心火烛",
            "真爱生命",
            "多运动"
    };

    public static String accountCrateOK = "账号创建成功";
    public static String accountCrateFail= "账号创建失败";
    public static String accountImportOk = "账号导入成功";
    public static String accountImportFail = "账号导入失败,无法识别的二维码";
    public static String accountCreateFailWithRid = "账号创建失败,无法识别的推荐码";

    public static String[] ALL_PERMISSIONS = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.RECORD_AUDIO",
            "android.permission.CAMERA"
    };

    public static String[] STORAGE_PERMISSIONS = {
            //"android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    public static String[] CAM_PERMISSION = {
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    public static String[] RECORD_AUDIO_PERMISSION = {
            "android.permission.RECORD_AUDIO"
    };

    public final static int REQUEST_EXTERNAL_STORAGE_READ = 0;
    public final static int PERMISSION_REQUEST_CODE_READ = 0;

    public final static int REQUEST_EXTERNAL_STORAGE_WRITE = 0;
    public final static int PERMISSION_REQUEST_CODE_WRITE = 0;

    public final static int REQUEST_RECORD_AUIIO = 2;
    public final static int REQUEST_CAMERA = 3;

    public static final boolean LOG_DEBUG = true;
    public static final boolean DEV_DEBUG = true;

    public static final int BITHER_DESKTOP_NETWORK_SOCKET = 8329;
    public static final int BITHER_ENTERPRISE_NETWORK_SOCKET = 8328;
    public static final int BITHER_DAEMON_NETWORK_SOCKET = 8327;

    public static final int PROTOCOL_VERSION = 70001;
    public static final int MIN_PROTO_VERSION = 70001;

    public static final int MAX_TX_SIZE = 100000;
    public static final int COMPRESS_OUT_NUM = 5;
    public static final int TX_PAGE_SIZE = 20;

    public static final int REQUEST_CODE_PERMISSION_CAMERA = 1010;

    public static final String DONATE_ADDRESS = "1BitherUnNvB2NsfxMnbS35kS3DTPr7PW5";

    /**
     * The alert signing key originally owned by Satoshi, and now passed on to Gavin along with a few others.
     */
    public static final byte[] SATOSHI_KEY = Hex.decode("04fc9702847840aaf195de8442ebecedf5b095cdbb9bc716bda9110971b28a49e0ead8564ff0db22209e0374782c093bb899692d524e9d6a6956e7c5ecbcd68284");


    /**
     * The string returned by getId() for the main, production network where people trade things.
     */
    public static final String ID_MAINNET = "org.bitcoin.production";


    public static final BigInteger proofOfWorkLimit = Utils.decodeCompactBits(0x207fffff);//wystan modify for regtest  0x1d00ffffL
    public static final int port = 18444;// 8333;//wystan modify for regtest port
    public static final long packetMagic = 0xfabfb5daL;//0xf9beb4d9L; wystan modify for regtest magic

    public static final int addressHeader = 0;
    public static final int btgAddressHeader = 38;
    public static final int btwAddressHeader = 73;
    public static final int btfAddressHeader = 36;
    public static final int btpAddressHeader = 56;

    public static final int p2shHeader = 5;
    public static final int btgP2shHeader = 23;
    public static final int btwP2shHeader = 31;
    public static final int btfP2shHeader = 40;
    public static final int btpP2shHeader = 58;

    public static final int dumpedPrivateKeyHeader = 128;
    public static final int TARGET_TIMESPAN = 14 * 24 * 60 * 60;  // 2 weeks per difficulty cycle, on average.
    public static final int TARGET_SPACING = 10 * 60;  // 10 minutes per block.
    public static final int INTERVAL = TARGET_TIMESPAN / TARGET_SPACING;

    public static final long TX_UNCONFIRMED = Long.MAX_VALUE;

    public static final int PROTOCOL_TIMEOUT = 30000;

    public static final int PUBLISH_TIMEOUT = 5 * 60 * 1000; //wystan  add for pushlishtimeout

    public static final boolean DISABLE_SYNC_BLOCK = true;

    public static final String id = ID_MAINNET;

    /**
     * The depth of blocks required for a coinbase transaction to be spendable.
     */
    public static final int spendableCoinbaseDepth = 100;
    public static final String[] dnsSeeds = new String[]{ //wystan mark for dns seeds
            "seed.bitcoin.sipa.be",        // Pieter Wuille
            "dnsseed.bluematt.me",         // Matt Corallo
            "seed.bitcoinstats.com",       // Chris Decker
            "bitseed.xf2.org",
            "seed.bitcoinstats.com",
            "seed.bitnodes.io"
    };

    public static final byte[] node_ip =  {(byte)122,(byte)76,(byte)54,(byte)17};//{(byte)192,(byte)168,(byte)88,(byte)234};//

    public static final long MAX_MONEY = 9233720368l * 100000000l;//  21000000l * 100000000l;  wystan modify 201021

    public static final byte[] GENESIS_BLOCK_HASH = Utils.reverseBytes(Hex.decode("6aaed28a90a11ee78a7b19ebb6bc5b2d885cdb46f744af4965abb394f86485d2")); // wystan modify  for regtest
    //Utils.reverseBytes(Hex.decode("0f9188f13cb7b2c71f2a335e3a4fc328bf5beb436012afca590b1a11466e2206"));
    //Utils.reverseBytes(Hex.decode("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f")); // wystan modify  for regtest
    public static final int BLOCK_DIFFICULTY_INTERVAL = 2016;
    public static final int BITCOIN_REFERENCE_BLOCK_HEIGHT = 250000;
    public static final int MaxPeerConnections = 2;//6; //wystan modify for max peers 200820
    public static final int MaxPeerBackgroundConnections = 2;

    public static enum AppMode {
        COLD, HOT
    }

    public static enum ApiConfig {
        BLOCKCHAIN_INFO(1), BITHER_NET(0);

        private int value;
        ApiConfig(int value){
            this.value = value;
        }

        public int value(){
            return value;
        }
    }

    public static final String PRIVATE_KEY_FILE_NAME = "%s/%s.key";
    public static final String WATCH_ONLY_FILE_NAME = "%s/%s.pub";

    public static final boolean ensureMinRequiredFee = true;

    public enum TransactionFeeMode {//wystan mark for fee 200919
        Normal(10000), High(20000), Higher(50000), TenX(100000), TwentyX(200000), Low(5000), Lower(1000);

        private int satoshi;

        TransactionFeeMode(int satoshi) {
            this.satoshi = satoshi;
        }

        public int getMinFeeSatoshi() {
            return satoshi;
        }
    }

    public enum MarketType {
        BITSTAMP, BITFINEX, COINBASE;
    }

    public static MarketType getMarketType(int value) {
        switch (value) {
            case 2:
                return MarketType.BITFINEX;
            case 3:
                return MarketType.COINBASE;
        }
        return MarketType.BITSTAMP;
    }

    public static int getMarketValue(MarketType marketType) {
        switch (marketType) {
            case BITFINEX:
                return 2;
            case COINBASE:
                return 3;
        }
        return 1;
    }

    public static boolean validAddressPrefixPubkey(int pubkey) {
        if(pubkey == addressHeader || pubkey == btgAddressHeader || pubkey == btwAddressHeader ||
                pubkey == btfAddressHeader || pubkey == btpAddressHeader) {
            return true;
        }
        return false;
    }

    public static  boolean validAddressPrefixScript(int script) {
        if(script == p2shHeader || script == btgP2shHeader || script == btwP2shHeader ||
                script == btfP2shHeader || script == btpP2shHeader) {
            return true;
        }

        return false;
    }

    public enum KlineTimeType {
        ONE_MINUTE(1), FIVE_MINUTES(5), ONE_HOUR(60), ONE_DAY(1440);
        private int mVal;

        private KlineTimeType(int val) {
            this.mVal = val;
        }

        public int getValue() {
            return this.mVal;
        }
    }

    public class INTENT_REF {
        public static final int SCAN_REQUEST_CODE = 536;
        public static final String SCAN_ADDRESS_POSITION_TAG = "scan_address_position";
        public static final int SEND_REQUEST_CODE = 437;
        public static final String QR_CODE_STRING = "qr_code_string";
        public static final String OLD_QR_CODE_STRING = "old_qr_code_string";
        public static final String QR_CODE_HAS_CHANGE_ADDRESS_STRING = "qr_code_has_change_address";
        public static final String TITLE_STRING = "title_string";
        public static final String QRCODE_TYPE = "qrcode_type";
    }

}
