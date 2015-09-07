package com.qianmi.hack.network;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.HurlStack;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class JwtAuthStack extends HurlStack {
    private AtomicBoolean isAuth = new AtomicBoolean(false);
    //默认Token的名称
    private String tokenTypeName = "JWT";
    //如果登录后，需要将Token值加入到Header中
    private String token;

    public JwtAuthStack() {
    }

    public JwtAuthStack(String tokenTypeName) {
        this.tokenTypeName = tokenTypeName;
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {
        if (token != null) {
            additionalHeaders.put("Authorization", String.format("%s %s", "JWT", token));
        }
        return super.performRequest(request, additionalHeaders);
    }

    public void setAuth(String token) {
        isAuth.set(true);
        this.token = token;
    }


}