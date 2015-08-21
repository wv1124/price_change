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
}
