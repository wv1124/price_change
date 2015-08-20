package com.qianmi.hack;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import com.qianmi.hack.activity.TabHostActivity;
import com.support.android.designlibdemo.MainActivity;
import com.qianmi.hack.utils.Constant;
import com.qianmi.hack.utils.L;
import com.qianmi.hack.utils.SPUtils;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Chen Haitao on 2015/7/7.
 */
public class LoginActivity extends BaseActivity {

    private static final String REQ_TAG = "login";
    private Context mContext;

    @Bind(R.id.et_login_username)
    EditText mInputUsername;

    @Bind(R.id.et_login_pwd)
    EditText mInputPwd;

    @Bind(R.id.btn_login)
    Button mLoginBtn;

    @Bind(R.id.til_username_wrapper)
    TextInputLayout mWrapperUsername;

    @Bind(R.id.til_pwd_wrapper)
    TextInputLayout mWrapperPwd;

    @OnClick(R.id.btn_login)
    public void tryLogin(View view) {
        startLoginRequestData();
    }


    @Override
    public void onBeginRequest() {

    }

    @Override
    public void onNetworkFailed() {

    }

    @Override
    public boolean needInitRequestQueue() {
        return false;
    }

    @Override
    public boolean needPrintActionOnMenu() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mWrapperUsername.setErrorEnabled(true);
        mWrapperPwd.setErrorEnabled(true);

        // set username and password input text watcher
        setInputZone();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setInputZone() {
        mInputUsername.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                mWrapperUsername.setError("");
                mWrapperPwd.setError("");
                //if (mInputUsername.getText().toString().length() == 0) {
                mInputPwd.setText("");
                //}
            }
        });

        mInputPwd.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                if (mInputPwd.getText().toString().length() > 0) {
                    mWrapperUsername.setError("");
                    mWrapperPwd.setError("");
                }

            }
        });

        String storedNickName = (String) SPUtils.get(this, Constant.NICK_NAME, "");
        if (storedNickName != null && !storedNickName.equals("") && storedNickName.length() > 5) {
            mInputUsername.setText(storedNickName);
            mInputUsername.setSelection(mInputUsername.getText().toString().length());
        }
    }

    /**
     * start login request
     */
    private void startLoginRequestData() {
        hideSoftKeyboard();
        String nickName = mInputUsername.getText().toString().trim();
        String passwd = mInputPwd.getText().toString();
        if (!nickName.matches("[\u4e00-\u9fa5\\w-]+") || nickName.length() < 5 || nickName.length() > 25) {
            mWrapperUsername.setError(getString(R.string.login_username_format_error));
            shakeErrorInputUsername();
            return;
        }

        if (!passwd.matches("\\w+")) {
            mWrapperPwd.setError(getString(R.string.login_pwd_format_error));
            shakeErrorInputPwd();
            return;
        }

        try {
            loginRequest(nickName, passwd);
        } catch (Exception e) {
            L.e("loginRequest exception error : \n" + e.toString());
            showSnackMsg(R.string.login_request_error);
        }
    }


    private void loginRequest(String username, String password) {
        L.d("login");
        //NetworkRequest.getInstance().loginRequest(username, password);
        Intent intent = new Intent(this, TabHostActivity.class);
        this.startActivity(intent);
    }

    private void loginReturnHandler(String loginReturnData) {

    }


    private void shakeErrorInputPwd() {
        mInputPwd.startAnimation(AnimationUtils.loadAnimation(this, R.anim.login_error_shake));
    }

    private void shakeErrorInputUsername() {
        mInputUsername.startAnimation(AnimationUtils.loadAnimation(this, R.anim.login_error_shake));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
        }
        return true;
    }

}
