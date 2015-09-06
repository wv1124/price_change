package com.qianmi.hack;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.qianmi.hack.activity.TabHostActivity;
import com.qianmi.hack.bean.LoginRequest;
import com.qianmi.hack.bean.Token;
import com.qianmi.hack.network.GsonRequest;
import com.qianmi.hack.utils.Constant;
import com.qianmi.hack.utils.L;
import com.qianmi.hack.utils.SPUtils;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;

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
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mWrapperUsername.setErrorEnabled(true);
        mWrapperPwd.setErrorEnabled(true);

        String token = (String) SPUtils.get(this, Constant.TOKEN, "");
        String user = (String) SPUtils.get(this, Constant.USER_NAME, "");
        String paswd = (String) SPUtils.get(this, Constant.PASSOWD, "");
        if (user.length() > 0) {
            mInputUsername.setText(user);
            mInputPwd.setText(paswd);
        }
        L.d("get token:" + token);
        if (token != null && token.length() > 0) {
            //loginSuccess(token);
        }

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


    private void loginRequest(final String username, final String password) {
        L.d("login");
        //NetworkRequest.getInstance().loginRequest(username, password);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.username = username;
        loginRequest.password = password;
        GsonRequest.Builder<Token> builder = new GsonRequest.Builder<>();
        GsonRequest request = builder.retClazz(Token.class)
                .setUrl(PcApplication.SERVER_URL + "api-token-auth/")
                .setRequest(loginRequest)
                .registerResListener(new Response.Listener<Token>() {
                    @Override
                    public void onResponse(Token resp) {
                        LoginActivity.this.dismissLoadingDialog();
                        L.d("TAG", "token is " + resp.token);
                        if (resp != null) {
                            PcApplication.TOKEN = resp.token;
                            tokenRequest(username, PcApplication.TOKEN, PcApplication.INSTALLATION_ID);
                            loginSuccess(resp.token, username, password);
                        } else {
                            L.e("login return error");
                        }
                    }
                })
                .registerErrorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        LoginActivity.this.dismissLoadingDialog();
                        Log.e("TAG", error.getMessage(), error);
                        LoginActivity.this.showSnackMsg(LoginActivity.this.getString(R.string.login_err));
                    }
                })
                .create();
        startRequest(request);
    }

    private void loginSuccess(String loginReturnData, String username, String passwd) {
        SPUtils.put(this, Constant.USER_NAME, username);
        SPUtils.put(this, Constant.PASSOWD, passwd);
        SPUtils.put(this, Constant.TOKEN, loginReturnData);
        Intent intent = new Intent(this, TabHostActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
        this.finish();
    }


    private void shakeErrorInputPwd() {
        mInputPwd.startAnimation(AnimationUtils.loadAnimation(this, R.anim.login_error_shake));
    }

    private void shakeErrorInputUsername() {
        mInputUsername.startAnimation(AnimationUtils.loadAnimation(this, R.anim.login_error_shake));
    }

    public void tokenRequest(final String username, final String token, String installationId) {
        Map<String, String> request = new HashMap<String, String>();
        request.put("token", username);
        request.put("installation", installationId);
        GsonRequest.Builder<Map> builder = new GsonRequest.Builder<>();
        GsonRequest infoRequest = builder
                .retClazz(Map.class)
                .setUrl(PcApplication.SERVER_URL + "tokens/")
                .setRequest(request)
                .setToken(PcApplication.TOKEN)
                .registerErrorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(REQ_TAG, String.valueOf(error));
                    }
                })
                .create();
        startRequest(infoRequest);

    }
}
