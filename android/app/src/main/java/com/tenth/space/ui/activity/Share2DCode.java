package com.tenth.space.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tenth.space.R;
import com.tenth.space.config.UrlConstant;
import com.tenth.space.utils.QRCodeUtils;
import com.tenth.space.utils.ToastUtils;
import com.tenth.space.utils.Utils;


import java.io.File;
import butterknife.ButterKnife;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;

/**
 * Created by wsq on 2016/9/6.
 */
public class Share2DCode extends Activity implements View.OnClickListener {
    private static final int SCANNIN_GREQUEST_CODE = 1;
    TextView mTitle;
    ImageView mCreateQrIv;
    Button bt_left_back;
    TextView mTvResult;
    ImageView mIvResult;
    ImageView mIvLeft;
    Button mBtnSave;
    Button mBtnShare;

    private String mContent;
    private Bitmap mDest;
    private Bitmap mBitmap;
    private String referralCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_share_code);
        ButterKnife.bind(this);

        referralCode = getIntent().getStringExtra("referralcode");
        mContent = UrlConstant.APP_SHARE_2DCODE+referralCode;
        initView();
        createQr(mContent);
    }

    private void initView() {
        mBtnSave=(Button) findViewById(R.id.btn_save);
        mBtnShare=(Button) findViewById(R.id.btn_share);
        bt_left_back=(Button) findViewById(R.id.bt_left_back);
        mCreateQrIv=(ImageView) findViewById(R.id.create_qr_iv);
        mBtnSave.setOnClickListener(this);
        mBtnShare.setOnClickListener(this);
        bt_left_back.setOnClickListener(this);
    }

    private void showShare() {
        //?????????shareSDK
        ShareSDK.initSDK(this);

        OnekeyShare oks = new OnekeyShare();
        //??????sso??????
        oks.disableSSOWhenAuthorize();

        // title???????????????????????????????????????????????????????????????QQ???????????????
        oks.setTitle("????????????");
//            oks.setTitle(mPruductShareBean.goodName);


        // titleUrl???????????????????????????QQ???QQ???????????????
        oks.setTitleUrl(UrlConstant.APP_SHARE_2DCODE + referralCode);

        // text???????????????????????????????????????????????????
//            oks.setText(mPruductShareBean.shareText);

        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
                if ("Wechat".equals(platform.getName())) {
//                        paramsToShare.setTitle(mPruductShareBean.goodName);
                }
                if ("WechatMoments".equals(platform.getName())) {
//                        paramsToShare.setTitle(mPruductShareBean.goodName);
                }
                if ("WechatFavorite".equals(platform.getName())) {
//                        paramsToShare.setTitle(mPruductShareBean.goodName);
                }
                if ("SinaWeibo".equals(platform.getName())) {
                    paramsToShare.setText(UrlConstant.APP_SHARE_2DCODE + referralCode);
                }

            }
        });

        // url???????????????????????????????????????????????????
        oks.setUrl(UrlConstant.APP_SHARE_2DCODE + referralCode);

        // ????????????GUI
        oks.show(this);
    }

    private void saveZxing() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String parPath = Environment.getExternalStorageDirectory() + "/DCIM/2DCODE";

            File file = new File(parPath);
            if (!file.exists()) {
                file.mkdirs();
            }

            String path = parPath + "shareMMJ.jpg";
            if (mBitmap != null) {
                Utils.saveBitmapByFormat(mBitmap, path, Bitmap.CompressFormat.JPEG);
            }
            ToastUtils.show("????????????????????????");

            //???????????????????????????????????????
            updateMedia(file);

            //??????????????????

            //????????????
            updateUI(path);
        }
    }

    private void updateMedia(File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        Uri photoUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                file);

       // Uri uri = Uri.fromFile(file);
        intent.setData(photoUri);
        this.sendBroadcast(intent);//??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????file???
    }


    private void updateUI(String path) {
        MediaScannerConnection.scanFile(this,
                new String[]{
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/" + new File(path).getParentFile().getAbsolutePath()
                }, null, null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scan_qr_btn:
                break;

            case R.id.btn_save:
                saveZxing();
                break;

            case R.id.btn_share:
                showShare();
                break;
            case R.id.bt_left_back:
                finish();
                break;

            default:
                break;
        }
    }

//    private void scanQr() {
//        Intent intent = new Intent(this, MipcaActivityCapture.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    //????????????????????????
                    mTvResult.setText(bundle.getString("result"));
                    //??????
                    mIvResult.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));
                }
                break;
        }
    }

    private void createQr(final String referralCode) {
//        final String filePath = getFileRoot(Share2DCode.this) + File.separator + "qr_" + System.currentTimeMillis() + ".jpg";
        final String filePath = getFileRoot(Share2DCode.this) + File.separator + "qr_" + ".jpg";

        //???????????????????????????????????????????????????????????????????????????????????????????????????
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = QRCodeUtils.createQRImage(referralCode, 800, 800, null, filePath);
                if (success) {
                    mBitmap = BitmapFactory.decodeFile(filePath);

                    runOnUiThread(new Runnable() {//???????????????UI??????
                        @Override
                        public void run() {
                            mCreateQrIv.setImageBitmap(mBitmap);
                        }
                    });
                }

//                //??????????????????logo
//                if (success) {
//                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
//                    Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.icon);
//                    mDest = QRCodeUtils.addLogo(bitmap, logo);
//
//                    //??????
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mCreateQrIv.setImageBitmap(mDest);
//                        }
//                    });
//                }

            }
        }).start();
    }

    private String getFileRoot(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File external = context.getExternalFilesDir(null);
            if (external != null) {
                return external.getAbsolutePath();
            }
        }

        return context.getFilesDir().getAbsolutePath();
    }
}
