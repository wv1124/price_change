package com.qianmi.hack.network;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.bean.LoginRequest;
import com.qianmi.hack.bean.Token;
import com.qianmi.hack.utils.L;

import java.util.HashMap;
import java.util.Map;

public class NetworkRequest {

    private static NetworkRequest mServerAgency = null;

    public synchronized static NetworkRequest getInstance() {
        if (null == mServerAgency) {
            mServerAgency = new NetworkRequest();
        }

        return mServerAgency;
    }

    public void loginRequest(String username, String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.username = "caozupeng";
        loginRequest.password = "caozupeng";

        GsonRequest mRequest = new GsonRequest(
                PcApplication.SERVER_URL + "api-token-auth/", loginRequest, Token.class,
                new Response.Listener<Token>() {
                    @Override
                    public void onResponse(Token resp) {
                        L.d("TAG", "token is " + resp.token);
                        PcApplication.TOKEN = resp.token;

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        });
        ServerConnector.getInstance(PcApplication.getInstance()).add(mRequest);
    }


}
