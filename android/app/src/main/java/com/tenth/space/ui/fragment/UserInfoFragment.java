package com.tenth.space.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.tenth.space.BitherjSettings;
import com.tenth.space.DB.DBInterface;
import com.tenth.space.DB.entity.GroupEntity;
import com.tenth.space.DB.entity.UserEntity;
import com.tenth.space.R;
import com.tenth.space.app.IMApplication;
import com.tenth.space.config.IntentConstant;
import com.tenth.space.imservice.event.GroupEvent;
import com.tenth.space.imservice.event.PriorityEvent;
import com.tenth.space.imservice.event.UserInfoEvent;
import com.tenth.space.imservice.manager.IMBuddyManager;
import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.imservice.manager.IMSocketManager;
import com.tenth.space.imservice.service.IMService;
import com.tenth.space.imservice.support.IMServiceConnector;
import com.tenth.space.protobuf.IMBaseDefine;
import com.tenth.space.protobuf.IMBuddy;
import com.tenth.space.protobuf.helper.ProtoBuf2JavaBean;
import com.tenth.space.ui.activity.AddActivity;
import com.tenth.space.ui.activity.DetailPortraitActivity;
import com.tenth.space.ui.activity.MainActivity;
import com.tenth.space.ui.activity.Share2DCode;
import com.tenth.space.utils.CommonUtil;
import com.tenth.space.utils.FileUtil;
import com.tenth.space.utils.IMUIHelper;
import com.tenth.space.utils.ImageLoaderUtil;
import com.tenth.space.utils.ToastUtils;
import com.tenth.space.utils.Utils;

import java.io.File;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import fastdfs.FdfsUtil;
import fastdfs.fastdfs.UploadRetCallback;

import static android.app.Activity.RESULT_OK;
import static android.media.MediaRecorder.VideoSource.CAMERA;
import static com.tenth.space.imservice.event.UserInfoEvent.Event.USER_INFO_UPDATE;

/**
 * 1.18 ??????currentUser??????
 */
public class UserInfoFragment extends MainFragment {

    private static final int FROM_CAMERA = 1;
    private static final int FROM_ALBUM = 2;
    private View curView = null;
    private IMService imService;
    private UserEntity currentUser;
    private String currentUserId;
    private EditText nickName;
    private EditText signature;


    private GroupEntity groupEntity;
    private boolean isGroup;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.i("detail#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                logger.e("detail#imService is null");
                return;
            }
            Intent intent = getActivity().getIntent();
            isGroup = intent.getBooleanExtra("isGroup", false);
            currentUserId = intent.getStringExtra(IntentConstant.KEY_PEERID);

            if (TextUtils.isEmpty(currentUserId)) {
                logger.e("detail#intent params error!! %s" ,currentUserId);
                return;
            }
            if (isGroup) {

                groupEntity = imService.getGroupManager().findGroup(currentUserId);
                if(groupEntity==null){
                    imService.getGroupManager().reqGroupDetailInfo(currentUserId,true);
                }else {
                    setGroupData();
                }
                initRes();
                //curView.findViewById(R.id.ll_gender).setVisibility(View.GONE);
                curView.findViewById(R.id.ll_sinal).setVisibility(View.INVISIBLE);
                //curView.findViewById(R.id.ll_referral).setVisibility(View.INVISIBLE);
                curView.findViewById(R.id.ll_share).setVisibility(View.INVISIBLE);
                TextView name = (TextView) curView.findViewById(R.id.name);
                //TextView num = (TextView) curView.findViewById(R.id.fans);
                //num.setText("?????????");
                name.setText("?????????");


                initDetailProfile();

            } else {
                currentUser = imService.getContactManager().findContact(currentUserId);
                logger.e("detail#intent params error!!");
                if (currentUser == null) {
                    //return;
                    UserEntity tmp = IMLoginManager.instance().getLoginInfo();
                    if(tmp.getPeerId().equals(currentUserId))
                        currentUser = IMLoginManager.instance().getLoginInfo();
                }
                if (currentUser != null) {
                    relation = currentUser.getRelation();
                    initBaseProfile();
                    initDetailProfile();
                }
                ArrayList<String> userIds = new ArrayList<>(1);
                //just single type
                userIds.add(currentUserId);
                imService.getContactManager().reqGetDetaillUsers(userIds);
            }
        }

        @Override
        public void onServiceDisconnected() {
        }
    };

    private void setGroupData() {
        portraitImageView = (ImageView) curView.findViewById(R.id.user_portrait);
        setTextViewContent(R.id.nickName, groupEntity.getMainName());
        //setTextViewContent(R.id.fans_cnt, groupEntity.getUserCnt() + "");
//        setTextViewContent(R.id.userName, currentUser.getRealName());
        //????????????
        ImageLoaderUtil.instance().displayImage(IMApplication.app.UrlFormat(groupEntity.getAvatar()), portraitImageView, ImageLoaderUtil.getAvatarOptions(20, 0));
        portraitImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUserId.equals(imService.getLoginManager().getPub_key())) {
                    //??????????????????
                    new MyPopupWindows(getActivity(), curView, groupEntity.getAvatar());
                } else {
                    //?????????????????????
                    if (!Utils.isStringEmpty(groupEntity.getAvatar())) {
                        //????????????
                        Intent intent = new Intent(getActivity(), DetailPortraitActivity.class);
                        intent.putExtra(IntentConstant.KEY_AVATAR_URL, groupEntity.getAvatar());
                        intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, true);
                        startActivity(intent);
                    } else {
                        //??????
                        ToastUtils.show("???????????????????????????");
                    }
                }
            }
        });
    }

    private Uri mUri;
    private String mTakePhotoPath;
    private ImageView portraitImageView;
    //private RadioButton gender1;
    //private RadioButton gender;
    //private EditText phone;
    private IMBaseDefine.UserInfo userInfo;
    //private TextView referralcode;
    private TextView userid;
    Intent intent;
    private TextView delete;
    private String relation;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imServiceConnector.disconnect(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        imServiceConnector.connect(getActivity());
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_user_detail, topContentView);
        super.init(curView);
        showProgressBar();
//        initRes();
        EventBus.getDefault().register(this);
        return curView;
    }

    @Override
    public void onResume() {
        if (null != intent) {
            String fromPage = intent.getStringExtra(IntentConstant.USER_DETAIL_PARAM);
            setTopLeftText(fromPage);
        }
        super.onResume();
    }

    /**
     * @Description ???????????????
     */
    private void initRes() {
        TextView operate = (TextView) curView.findViewById(R.id.tv_logo);
        final String loginId = IMLoginManager.instance().getPub_key();

        // ???????????????
        setTopTitle(getActivity().getString(R.string.page_user_detail));
        setTopLeftButton(R.drawable.tt_top_back);
        topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                getActivity().finish();
            }
        });
        topLetTitleTxt.setTextColor(getResources().getColor(R.color.default_bk));
        setTopLeftText(getResources().getString(R.string.top_left_back));
        signature = (EditText) curView.findViewById(R.id.et_signature);
        //gender = (RadioButton) curView.findViewById(R.id.rb_man);
        //gender1 = (RadioButton) curView.findViewById(R.id.rb_woman);
        nickName = (EditText) curView.findViewById(R.id.nickName);
        //phone = (EditText) curView.findViewById(R.id.et_phone);
        //referralcode = (TextView) curView.findViewById(R.id.tv_referralcode);
        userid = (TextView) curView.findViewById(R.id.user_id);
        delete = (TextView) curView.findViewById(R.id.delete);
        curView.findViewById(R.id.ll_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Share2DCode.class);
                intent.putExtra("referralcode", loginId);
                startActivity(intent);
            }
        });
        if (!currentUserId.equals(loginId)) {
            //curView.findViewById(R.id.ll_referral).setVisibility(View.GONE);
            curView.findViewById(R.id.ll_share).setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
            if (isGroup) {
                if (groupEntity == null) {
                    operate.setText("???????????????");
                    delete.setVisibility(View.GONE);
                } else {
                    operate.setText("?????????");
                    delete.setText("?????????");
                    delete.setVisibility(View.VISIBLE);
                }
            } else {
                if (IMBaseDefine.UserRelationType.RELATION_FRIEND.toString().equals(relation)) {
                    operate.setText("?????????");
                    delete.setText("????????????");
                } else if (IMBaseDefine.UserRelationType.RELATION_FOLLOW.toString().equals(relation)) {
                    operate.setText("????????????");
                    delete.setText("????????????");
                } else {
                    operate.setText("?????????");//operate.setText("????????????");
                    delete.setText("????????????");
                }
            }


            //gender.setEnabled(false);
            //gender1.setEnabled(false);
            nickName.setEnabled(false);
            signature.setEnabled(false);
            //phone.setEnabled(false);

        } else {
            delete.setVisibility(View.GONE);
            //gender.setEnabled(true);
            //gender1.setEnabled(true);
            nickName.setEnabled(true);
            signature.setEnabled(true);
            //phone.setEnabled(true);
            operate.setText("??????");
        }
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IMBaseDefine.UserRelationType.RELATION_FRIEND.toString().equals(relation)) {
                   // FriendManager.instance().deleteFriend(getActivity(), currentUserId);
                    getActivity().finish();
                } else if (IMBaseDefine.UserRelationType.RELATION_FOLLOW.toString().equals(currentUserId)) {
                   // IMBuddyManager.instance().reqDelFollowUser((long) currentUserId, 0);
                    getActivity().finish();
                } else {//????????????
                  //  IMBuddyManager.instance().reqFollowUser((long) currentUserId, 0);
                    getActivity().finish();
                }
            }
        });
        operate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentUserId.equals(loginId)) {
                    if (true || IMBaseDefine.UserRelationType.RELATION_FRIEND.toString().equals(relation)) {//wystan modify to disable friendship 210107
                        String sessionKey = currentUser.getSessionKey();
                        imService.getContactManager().putContactByChat(currentUser);
                        IMUIHelper.openChatActivity(getActivity(), sessionKey);
                    } else if (isGroup) {
                        Intent intent = new Intent(getActivity(), AddActivity.class);
                        intent.putExtra("friendId", groupEntity.getPeerId());
                        intent.putExtra("isGroup", true);
                        startActivity(intent);
                    } else {
                        Intent intent1 = new Intent(getActivity(), AddActivity.class);
                        intent1.putExtra("friendId", currentUserId);
                        startActivity(intent1);
                    }

                } else {
                    userInfo = IMBaseDefine.UserInfo.newBuilder()
                            //.setUserId(IMLoginManager.instance().getLoginId())  //wystan disable for user_id
                            .setSignInfo(signature.getText().toString())
                            //.setUserGender(gender.isChecked() ? 1 : 2)
                            .setUserNickName(nickName.getText().toString())
                            .setUserTel("")
                            // neil
                            .setAvatarUrl(!Utils.isStringEmpty(currentUser.getAvatar()) ? currentUser.getAvatar() : IMLoginManager.instance().getLoginId() + Utils.PNG)
                            .setDepartmentId(0)
                            .setUserRealName("")
                            .setUserDomain("")
                            .setStatus(0)
                            .setEmail("")
                            .build();

                    IMBuddy.IMUpdateUsersInfoReq msg = IMBuddy.IMUpdateUsersInfoReq.newBuilder()
                            .setUserInfo(userInfo)
                            .setUserId(IMLoginManager.instance().getLoginId())
                            .build();
                    int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
                    int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_UPDATE_USER_INFO_REQUEST_VALUE;
                    IMSocketManager.instance().sendRequest(msg, sid, cid);
                }
                //getActivity().finish(); //wystan modify for get real update info 210223
            }
        });
    }

    @Override
    protected void initHandler() {
    }

    public void onEventMainThread(GroupEvent event) {
        switch (event.getEvent()) {
            case STRENGE_GROUP:
                groupEntity = event.getGroupEntity();
                setGroupData();
                break;
        }
    }

    public void onEventMainThread(PriorityEvent event) {
        switch (event.event) {
            case MSG_UPDATE_USERINFO_SUCEED:
                //??????????????????

                UserEntity entity = ProtoBuf2JavaBean.getUserEntity(userInfo);
                imService.getContactManager().putContact(entity);
                DBInterface.instance().insertOrUpdateUser(entity);
                IMBuddy.IMUpdateUsersInfoRsp obj = (IMBuddy.IMUpdateUsersInfoRsp) event.object;
                int resultCode = obj.getResultCode();
                if (resultCode == 0)
                    ToastUtils.show("????????????????????????");
                //??????actitity????????????????????????
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("backName", entity.getMainName());
                intent.putExtra("gender", entity.getGender());
                intent.putExtra("signature", entity.getSignature());
                logger.i("wystan MSG_UPDATE_USERINFO_SUCEED backName:%s gender:%d signature:%s", entity.getMainName(), entity.getGender(),entity.getSignature());
                getActivity().setResult(RESULT_OK, intent);
                getActivity().finish();
                break;
            case STRANGER_INFO_UPDATE:
                currentUser= (UserEntity) event.object;
                    initBaseProfile();
                    initDetailProfile();
                break;

        }
    }


    private void initBaseProfile() {
        initRes();
        portraitImageView = (ImageView) curView.findViewById(R.id.user_portrait);
        setTextViewContent(R.id.nickName, currentUser.getMainName());
        //setTextViewContent(R.id.fans_cnt, currentUser.getFansCnt() + "");
        //referralcode.setText(currentUser.getRealName());
        /*if (currentUser.getGender() == 2) {
            gender1.setChecked(true);
        } else {
            gender.setChecked(true);
        }*/
        userid.setText(currentUser.getId());
        signature.setText(currentUser.getSignature());
//        setTextViewContent(R.id.userName, currentUser.getRealName());
        //????????????
        ImageLoaderUtil.instance().displayImage(IMApplication.app.UrlFormat(currentUser.getAvatar()), portraitImageView, ImageLoaderUtil.getAvatarOptions(20, 0));
        portraitImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUserId.equals(imService.getLoginManager().getPub_key())) {
                    //??????????????????
                    new MyPopupWindows(getActivity(), curView, currentUser.getAvatar());
                } else {
                    //?????????????????????
                    if (!Utils.isStringEmpty(currentUser.getAvatar())) {
                        //????????????
                        Intent intent = new Intent(getActivity(), DetailPortraitActivity.class);
                        intent.putExtra(IntentConstant.KEY_AVATAR_URL, currentUser.getAvatar());
                        intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, true);
                        startActivity(intent);
                    } else {
                        //??????
                        ToastUtils.show("???????????????????????????");
                    }
                }
            }
        });

        // ??????????????????
//        Button chatBtn = (Button) curView.findViewById(R.id.chat_btn);
//        if (currentUserId == imService.getLoginManager().getLoginId()) {
//            chatBtn.setVisibility(View.GONE);
//        } else {
//            chatBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View arg0) {
//                    String sessionKey = currentUser.getSessionKey();
//                    IMUIHelper.openChatActivity(getActivity(), sessionKey);
//                    getActivity().finish();
//                }
//            });
//
//        }
    }

    private void openCamera() {
        String sdStatus = Environment.getExternalStorageState();
        // ??????sd???????????????
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            ToastUtils.show("?????????SD???????????????");
            return;
        } else {
            mTakePhotoPath = CommonUtil.getImageSavePath(String.valueOf(System
                    .currentTimeMillis())
                    + ".jpg");//Environment.getExternalStorageDirectory() + "/IM";
            /*
            File file1 = new File(path);
            if (!file1.exists()) {
                file1.mkdirs();
            }

            File file = new File(file1, "userAvatar.png");
           //mUri = Uri.fromFile(file);

             */

            mUri = FileProvider.getUriForFile(
                    getActivity(),
                    getActivity().getPackageName() + ".fileprovider",
                    new File(mTakePhotoPath));


            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);// ???????????????????????????????????????????????????
            startActivityForResult(intent, CAMERA);
        }
    }

    private void openAlbum() {
        // ???????????????????????????????????????
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //intent.setType("image/*");
        startActivityForResult(intent, FROM_ALBUM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA && resultCode == RESULT_OK) {//????????????
            String encodedPath = mTakePhotoPath;//mUri.getPath();
            ToastUtils.show("?????????...");
            showProgressBar();
            upload2Dfs(encodedPath);
        } else if (requestCode == FROM_ALBUM && null != data && resultCode == RESULT_OK) {//????????????
            Uri uri = data.getData();
            String filePath = FileUtil.getFilePathFromContentUri(uri, getActivity().getContentResolver());
            ToastUtils.show("?????????...");
            showProgressBar();
            upload2Dfs(filePath);
        }
    }


    private void upload2Dfs(final String path){
        new FdfsUtil(path, new  UploadRetCallback(){

            @Override
            public void onSuccess(String url) {
                logger.i("wystan upLoad2Fastdfs:%s",url);
                IMBuddyManager.instance().reqChangeAvatar(ByteString.copyFromUtf8(url));
                UserEntity tmp = IMLoginManager.instance().getLoginInfo();
                tmp.setAvatar(url);
                IMLoginManager.instance().setLoginInfo(tmp);
                hideProgressBar();
                //?????????????????????????????????
                Utils.clearDiskAndMemoryCache(IMApplication.app.UrlFormat(url), true, true);
                EventBus.getDefault().postSticky(USER_INFO_UPDATE);
                //?????????????????????
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageLoaderUtil.instance().displayImage("file:/" + IMApplication.app.UrlFormat(path), portraitImageView, ImageLoaderUtil.getAvatarOptions(10, 0));
                    }
                });
            }

            @Override
            public void onFailure() {
                hideProgressBar();
                ToastUtils.show("??????????????????");
            }
        }).asyncUpload();
    }

    private void initDetailProfile() {
        logger.i("detail#initDetailProfile");
        hideProgressBar();
//        setTextViewContent(R.id.telno, currentUser.getPhone());
//        setTextViewContent(R.id.email, currentUser.getEmail());
//        View phoneView = curView.findViewById(R.id.phoneArea);
//        View emailView = curView.findViewById(R.id.emailArea);
//        IMUIHelper.setViewTouchHightlighted(phoneView);
//        IMUIHelper.setViewTouchHightlighted(emailView);

//        emailView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (currentUserId == IMLoginManager.instance().getLoginId())
//                    return;
//                IMUIHelper.showCustomDialog(getActivity(), View.GONE, String.format(getString(R.string.confirm_send_email), currentUser.getEmail()), new IMUIHelper.dialogCallback() {
//                    @Override
//                    public void callback() {
//                        Intent data = new Intent(Intent.ACTION_SENDTO);
//                        data.setData(Uri.parse("mailto:" + currentUser.getEmail()));
//                        data.putExtra(Intent.EXTRA_SUBJECT, "");
//                        data.putExtra(Intent.EXTRA_TEXT, "");
//                        startActivity(data);
//                    }
//                });
//            }
//        });

//        phoneView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (currentUserId == IMLoginManager.instance().getLoginId())
//                    return;
//                IMUIHelper.showCustomDialog(getActivity(), View.GONE, String.format(getString(R.string.confirm_dial), currentUser.getPhone()), new IMUIHelper.dialogCallback() {
//                    @Override
//                    public void callback() {
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                IMUIHelper.callPhone(getActivity(), currentUser.getPhone());
//                            }
//                        }, 0);
//                    }
//                });
//            }
//        });
//???????????????????????????
        //  setSex(currentUser.getGender());
    }

    private void setTextViewContent(int id, String content) {
        TextView textView = (TextView) curView.findViewById(id);
        if (textView == null || content == null) {
            return;
        } else {
            textView.setText(content);
        }


    }

//    private void setSex(int sex) {
//        if (curView == null) {
//            return;
//        }
//
//        TextView sexTextView = (TextView) curView.findViewById(R.id.sex);
//        if (sexTextView == null) {
//            return;
//        }
//
//        int textColor = Color.rgb(255, 138, 168); //xiaoxian
//        String text = getString(R.string.sex_female_name);
//
//        if (sex == DBConstant.SEX_MAILE) {
//            textColor = Color.rgb(144, 203, 1);
//            text = getString(R.string.sex_male_name);
//        }
//
//        sexTextView.setVisibility(View.VISIBLE);
//        sexTextView.setText(text);
//        sexTextView.setTextColor(textColor);
//    }

    private boolean checkPermission(int permiss){
        int permission = ActivityCompat.checkSelfPermission(getActivity(),
                BitherjSettings.ALL_PERMISSIONS[permiss]);
        if(permission != PackageManager.PERMISSION_GRANTED){
            String pemiss_strings[] = new String[]{};
            if(permiss == BitherjSettings.REQUEST_EXTERNAL_STORAGE_READ || permiss == BitherjSettings.REQUEST_EXTERNAL_STORAGE_WRITE)
                pemiss_strings = BitherjSettings.STORAGE_PERMISSIONS;
            else if(permiss == BitherjSettings.REQUEST_RECORD_AUIIO)
                pemiss_strings = BitherjSettings.RECORD_AUDIO_PERMISSION;
            else if(permiss == BitherjSettings.REQUEST_CAMERA)
                pemiss_strings = BitherjSettings.CAM_PERMISSION;

            ActivityCompat.requestPermissions(getActivity(), pemiss_strings, permiss);// requestCode Application specific request code to match with a result
            return  false;
        }

        return true;
    }

    public class MyPopupWindows extends PopupWindow {

        public MyPopupWindows(Context mContext, View parent, final String avatar) {
            View view = View.inflate(mContext, R.layout.popup_avatar, null);
            setContentView(view);
            view.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_ins));
            LinearLayout ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
            ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in));
            View hideArea = (View) view.findViewById(R.id.v_hide);
            hideArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

            setFocusable(true);
            setOutsideTouchable(true);
            setBackgroundDrawable(new BitmapDrawable());

            showAtLocation(parent, Gravity.BOTTOM, 0, 0);
            update();

            Button bt1 = (Button) view.findViewById(R.id.btn_pre);
            final Button bt2 = (Button) view.findViewById(R.id.btn_camera);
            final Button bt3 = (Button) view.findViewById(R.id.btn_album);
            bt1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (!Utils.isStringEmpty(avatar)) {
                        //????????????
                        Intent intent = new Intent(getActivity(), DetailPortraitActivity.class);
                        intent.putExtra(IntentConstant.KEY_AVATAR_URL, avatar);
                        intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, true);
                        startActivity(intent);
                    } else {
                        //??????
                        ToastUtils.show("????????????????????????");
                    }
                    dismiss();
                }
            });
            bt2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(checkPermission(BitherjSettings.REQUEST_CAMERA)&&checkPermission(BitherjSettings.REQUEST_EXTERNAL_STORAGE_WRITE)){
                        openCamera();
                        dismiss();
                    }
                }
            });
            bt3.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(checkPermission(BitherjSettings.REQUEST_EXTERNAL_STORAGE_WRITE)) {
                        openAlbum();
                        dismiss();
                    }
                }
            });
        }
    }
}
