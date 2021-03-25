package com.tenth.space.DB.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * @author : yingmu on 15-1-6.
 * @email : yingmu@mogujie.com.
 *
 * todo need Encryption
 */
public class LoginSp {

    private final String fileName = "login.ini";
    private Context ctx;
    private final String KEY_LOGIN_NAME = "loginName";
    private final String KEY_PWD = "pwd";
    private final String KEY_LOGIN_ID = "loginId";


    SharedPreferences sharedPreferences;

    private static LoginSp loginSp = null;
    public static LoginSp instance(){
        if(loginSp ==null){
            synchronized (LoginSp.class){
                loginSp = new LoginSp();
            }
        }
        return loginSp;
    }
    private LoginSp(){
    }


    public void  init(Context ctx){
        this.ctx = ctx;
        sharedPreferences= ctx.getSharedPreferences
                (fileName,ctx.MODE_PRIVATE);
    }

    public  void setLoginInfo(String userName,String pwd,String loginId){
        // 横写
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LOGIN_NAME, userName);
        editor.putString(KEY_PWD, pwd);
        editor.putString(KEY_LOGIN_ID, loginId);
        //提交当前数据
        editor.commit();
    }

    public SpLoginIdentity getLoginIdentity(){
        String userName =  sharedPreferences.getString(KEY_LOGIN_NAME,null);
        String pwd = sharedPreferences.getString(KEY_PWD,null);
        String loginId = sharedPreferences.getString(KEY_LOGIN_ID, "");
        /**pwd不判空: loginOut的时候会将pwd清空*/
        if(TextUtils.isEmpty(pwd) ||TextUtils.isEmpty(userName) || TextUtils.isEmpty(loginId)){
            return null;
        }
        return new SpLoginIdentity(userName,pwd,loginId);
    }

    public class  SpLoginIdentity{
        private String loginName;
        private String pwd;
        private String loginId;

        public String getReturn_server() {
            return return_server;
        }

        public void setReturn_server(String return_server) {
            this.return_server = return_server;
        }

        private String loginUserName;
        private String return_server;

        public String getLoginUserName() {
            return loginUserName;
        }

        public void setLoginUserName(String loginUserName) {
            this.loginUserName = loginUserName;
        }

        public SpLoginIdentity(String mUserName, String mPwd, String mLoginId){
            loginName = mUserName;
            pwd = mPwd;
            loginId = mLoginId;

        }

        public String getLoginId() {
            return loginId;
        }

        public void setLoginId(String loginId) {
            this.loginId = loginId;
        }

        public String getLoginName() {
            return loginName;
        }

        public String getPwd() {
            return pwd;
        }
    }
}
