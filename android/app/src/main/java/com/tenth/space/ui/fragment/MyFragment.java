package com.tenth.space.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tenth.space.BitherjSettings;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.R;
import com.tenth.space.config.IntentConstant;
import com.tenth.space.imservice.event.UserInfoEvent;
import com.tenth.space.imservice.manager.FriendManager;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.imservice.service.IMService;
import com.tenth.space.imservice.support.IMServiceConnector;
import com.tenth.space.protobuf.IMSystem;
import com.tenth.space.ui.activity.MainActivity;
import com.tenth.space.ui.activity.SettingActivity;
import com.tenth.space.ui.activity.SysMsgActivity;
import com.tenth.space.ui.activity.UserInfoActivity;
import com.tenth.space.ui.widget.IMBaseImageView;
import com.tenth.space.utils.FileUtil;
import com.tenth.space.utils.QRCodeUtils;
import com.tenth.space.utils.ToastUtils;
import com.tenth.space.utils.Utils;

import java.io.File;

import de.greenrobot.event.EventBus;

import static android.app.Activity.RESULT_OK;

public class MyFragment extends MainFragment {
    private View curView = null;
    private View contentView;
    private View exitView;
    private View clearView;
    private View saveIdCardView;
    private View settingView;
    private TextView nickNameView;
    private TextView userSignatureView;
    private TextView sysMsgCnt;
    private int cnt;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            if (curView == null) {
                return;
            }
            IMService imService = imServiceConnector.getIMService();
            if (imService == null) {
                return;
            }
            if (!imService.getContactManager().isUserDataReady()) {//wystan diasble 210115
                logger.i("detail#contact data are not ready");
            } else {
                init(imService);

            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        imServiceConnector.connect(getActivity());
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_my, topContentView);
        initRes();
        return curView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        cnt = ((MainActivity) getActivity()).getUnreadSysMsgCnt();
        if(!hidden && cnt>0){
            sysMsgCnt.setVisibility(View.VISIBLE);
            sysMsgCnt.setText(cnt+"");
        }
    }

    /**
     * @Description 初始化资源
     */
    private void initRes() {
        super.init(curView);
        sysMsgCnt = (TextView) curView.findViewById(R.id.sys_message);
        contentView = curView.findViewById(R.id.content);
        exitView = curView.findViewById(R.id.exitPage);
        clearView = curView.findViewById(R.id.clearPage);
        saveIdCardView = curView.findViewById(R.id.saveIdCardPage);
        settingView = curView.findViewById(R.id.settingPage);

        curView.findViewById(R.id.sys_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),SysMsgActivity.class);
                intent.putExtra("cnt",cnt);
                startActivity(intent);
                sysMsgCnt.setVisibility(View.GONE);
                MainActivity activity = (MainActivity) getActivity();
                activity.setUnreadSysMsgCnt(0);
                activity.setUnreadSysMsg(0);
                FriendManager.instance().AddReadDataAck(IMSystem.SysMsgType.SYS_MSG_SYSTEM);
            }
        });

        clearView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog));
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialog_view = inflater.inflate(R.layout.tt_custom_dialog, null);
                final EditText editText = (EditText) dialog_view.findViewById(R.id.dialog_edit_content);
                editText.setVisibility(View.GONE);
                TextView textText = (TextView) dialog_view.findViewById(R.id.dialog_title);
                textText.setText(R.string.clear_cache_tip);
                builder.setView(dialog_view);

                builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ImageLoader.getInstance().clearMemoryCache();
                        ImageLoader.getInstance().clearDiskCache();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                FileUtil.deleteHistoryFiles(new File(Environment.getExternalStorageDirectory().toString()
                                        + File.separator + "SPACE_BIT-IM" + File.separator), System.currentTimeMillis());
                                Toast toast = Toast.makeText(getActivity(), R.string.thumb_remove_finish, Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        }, 500);

                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

        saveIdCardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if (checkPermission()) {
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        File filep = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/");
                        if (!filep.exists()) {
                            filep.mkdirs();
                        }

                        final String filePath = Environment.getExternalStorageDirectory() + "/DCIM/Camera/Space3IdCard_" + System.currentTimeMillis() + ".jpg";

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String qrContent = IMLoginManager.instance().getIdCardEntity().toJsonString();
                                boolean success = QRCodeUtils.createQRImage(qrContent, 800, 800, null, filePath);
                                if (success) {
                                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera"));
                                    intent.setData(uri);
                                    getActivity().sendBroadcast(intent);//这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了！，记得要传你更新的file哦
                                    logger.i("wystan saveIdCardView :%s %s", filePath, qrContent);
                                    ToastUtils.show("账号二维码已保存到手机");
                                }
                            }
                        }).start();
                    }
                }
            }
        });

        exitView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog));
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialog_view = inflater.inflate(R.layout.tt_custom_dialog, null);
                final EditText editText = (EditText) dialog_view.findViewById(R.id.dialog_edit_content);
                editText.setVisibility(View.GONE);
                TextView textText = (TextView) dialog_view.findViewById(R.id.dialog_title);
                textText.setText(R.string.exit_teamtalk_tip);
                builder.setView(dialog_view);
                builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IMLoginManager.instance().setKickout(false);
                        IMLoginManager.instance().logOut();
                        getActivity().finish();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();

            }
        });

        settingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到配置页面
                startActivity(new Intent(MyFragment.this.getActivity(), SettingActivity.class));
            }
        });

        hideContent();

        // 设置顶部标题栏
        setTopTitle(getActivity().getString(R.string.page_me));
        // 设置页面其它控件

    }



    private void hideContent() {
        if (contentView != null) {
            contentView.setVisibility(View.GONE);
        }
    }

    private void showContent() {
        if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 应该放在这里嘛??
        imServiceConnector.disconnect(getActivity());
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initHandler() {
    }

    public void onEventMainThread(UserInfoEvent.Event event) {
        switch (event) {
            case USER_INFO_OK:
                init(imServiceConnector.getIMService());
                break;
            case USER_INFO_UPDATE:
                //清除本地缓存和内存缓存?x-oss-process=image/resize,h_100
//                String photo="";
//                if (!Utils.isStringEmpty(loginContact.getAvatar())){
//                    photo=loginContact.getAvatar();
//                }else {
//                    photo= Config.endpoint +Config.avatarPicsPath +IMLoginManager.instance().getLoginId()+Utils.PNG;
//                }
//               // MemoryCacheUtils.removeFromCache(photo+"?x-oss-process=image/resize,h_100", (MemoryCache) ImageLoaderUtil.instance().getDiskCache());
//               // MemoryCacheUtils.removeFromCache(photo+"?x-oss-process=image/resize,h_100", ImageLoaderUtil.instance().getMemoryCache());
//               Utils.clearDiskAndMemoryCache(photo+"?x-oss-process=image/resize,h_100",true,true);
                logger.i("wystan fragment#onEventMainThread, USER_INFO_UPDATE");
                loginContact = IMLoginManager.instance().getLoginInfo();
                init(imServiceConnector.getIMService());
                break;

        }
    }

    public UserEntity loginContact;

    private void init(IMService imService) {
        showContent();
        hideProgressBar();

        if (imService == null) {
            return;
        }

        loginContact = imService.getLoginManager().getLoginInfo();
        if (loginContact == null) {
            return;
        }
        nickNameView = (TextView) curView.findViewById(R.id.nickName);
        userSignatureView = (TextView) curView.findViewById(R.id.user_signature);
        IMBaseImageView portraitImageView = (IMBaseImageView) curView.findViewById(R.id.user_portrait);
       // final ImageView portraitImageView = (ImageView) curView.findViewById(R.id.user_portrait);

        nickNameView.setText(loginContact.getMainName());
        userSignatureView.setText(loginContact.getSignature());

        //头像设置

        //portraitImageView.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        //portraitImageView.setCorner(15);
        portraitImageView.setImageResource(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setImageUrl(loginContact.getAvatar());
        //暂时不考虑做无网络状态下的
        String photo = "";
        if (!Utils.isStringEmpty(loginContact.getAvatar())) {
            photo = loginContact.getAvatar();
        } else {
            // neil
            //photo = Config.endpoint + "/" + Config.avatarPicsPath + IMLoginManager.instance().getLoginId() + Utils.PNG;
        }
        //ImageLoaderUtil.getAvatarOptions(ImageLoaderUtil.CIRCLE_CORNER,0);
        /*  wystan disable avatar load from aliyun 210119
        ImageLoaderUtil.instance().displayImage(IMApplication.app.UrlFormat(photo), portraitImageView, ImageLoaderUtil.getAvatarOptions(20, 0), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
                //加载失败后展示
                portraitImageView.setImageResource(R.drawable.tt_default_user_portrait_corner);
            }
        });

         */
        RelativeLayout userContainer = (RelativeLayout) curView.findViewById(R.id.user_container);
        userContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                intent.putExtra(IntentConstant.KEY_PEERID, loginContact.getPub_key());
                startActivityForResult(intent, 0);
                //IMUIHelper.openUserProfileActivity(getActivity(), loginContact.getPeerId());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            //改变用户名
            String mainName = data.getStringExtra("backName");
            int gener = data.getIntExtra("gender",0);
            String signature = data.getStringExtra("signature");
            if (nickNameView != null) {
                nickNameView.setText(mainName + "");
                loginContact.setMainName(mainName);
                loginContact.setGender(gener);
                loginContact.setSignature(signature);
                imServiceConnector.getIMService().getLoginManager().setLoginInfo(loginContact);
            }
        }
    }

    private void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        } else {
            logger.e("fragment#deleteFilesByDirectory, failed");
        }
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

    private boolean checkPermission(){
        int permission = ActivityCompat.checkSelfPermission(getActivity(),
                BitherjSettings.STORAGE_PERMISSIONS[BitherjSettings.REQUEST_EXTERNAL_STORAGE_WRITE]);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), BitherjSettings.STORAGE_PERMISSIONS, BitherjSettings.PERMISSION_REQUEST_CODE_WRITE);// requestCode Application specific request code to match with a result
            return  false;
        }

        return true;
    }
}
