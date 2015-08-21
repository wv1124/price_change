package com.qianmi.hack.network;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.qianmi.hack.PcApplication;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Gson请求类，传入请求对象和返回类名，返回解析好的对象，封装JWT安全验证，数据签名
 *
 * @param <T>
 */
public class GsonRequest<T> extends Request<T> {

    private static final String TAG = "GsonRequest";

    private final Listener<T> mListener;

    private Gson mGson;

    private Class<T> mClass;
    private Object mRequest;
    private String mBody;

    public GsonRequest(int method, String url, Object request, Class<T> clazz, Listener<T> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mGson = new Gson();
        mClass = clazz;
        mListener = listener;
        mRequest = request;
        if (null != request) {
            mBody = mGson.toJson(mRequest);
        }
    }

    public GsonRequest(String url, Object request, Class<T> clazz, Listener<T> listener, ErrorListener errorListener) {
        this(Method.POST, url, request, clazz, listener, errorListener);
        Log.d(TAG, "-----url is:" + url);
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (null == mRequest) {
            return null;
        }
        Log.d(TAG, "-----request json: " + mBody);
        return mBody.getBytes();
    }

    public int statusCode = 0;

    @Override
    public String getBodyContentType() {
        return "application/json; charset=UTF-8";
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            //String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            statusCode = response.statusCode;
            String jsonString = new String(response.data, "UTF-8");
            return Response.success(mGson.fromJson(jsonString, mClass), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
        Log.i(TAG, "-----response:" + new Gson().toJson(response));
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put("Content-Type", "application/json; charset=UTF-8");
        extraHeaders.put("Accept", "application/json");

        //Data Sign
        /*
        if (!TextUtils.isEmpty(mBody)) {
            extraHeaders.put("Sign", MD5Util.stringToMD5(PcApplication.SIGN_SECRET + mBody));
        }
        */

        //JWT token
        if (!TextUtils.isEmpty(PcApplication.TOKEN)) {
            extraHeaders.put("Authorization", "JWT " + PcApplication.TOKEN);
        }
        Log.d(TAG, "-----extra headers: " + extraHeaders.toString());

        return extraHeaders;
    }
}
