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

    //返回结果的监听器
    private Listener mListener;
    //用于解析Json，可以设置为静态变量，Thread Safe
    private static Gson Gson = new Gson();
    //返回结果的类型
    private Class<T> mRetClazz;
    //请求参数
    private String mBody;
    //是否要做md5防篡改校验
    private boolean isSign = false;
    //默认的HTTP请求字符集
    private String charset = "UTF-8";

    public static class Builder<T> {
        private Response.Listener responseListener;
        private Response.ErrorListener errorListener;
        private Class<T> retClazz;
        private String requestBody;
        private boolean isSign = false;
        private String charset = "UTF-8";
        private String url;
        private int method = Request.Method.POST;


        public Builder() {
        }

        public Builder registerResListener(Response.Listener listener) {
            this.responseListener = listener;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder method(int method) {
            this.method = method;
            return this;
        }

        public Builder registerErrorListener(Response.ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public Builder registerRetClass(Class<T> clazz) {
            this.retClazz = clazz;
            return this;
        }

        public Builder setRequest(Object request) {
            if (null != request) {
                this.requestBody = Gson.toJson(request);
            }
            return this;
        }

        public Builder needSignature(boolean isSign) {
            this.isSign = isSign;
            return this;
        }

        public GsonRequest<T> create() {
            GsonRequest<T> request = new GsonRequest<T>(method, url, errorListener);
            request.charset = charset;
            request.isSign = isSign;
            request.mRetClazz = retClazz;
            request.mListener = responseListener;
            request.mBody = requestBody;
            return request;

        }

    }


    public GsonRequest(int method, String url, ErrorListener errorListener) {
        super(method, url, errorListener);
    }

    public GsonRequest(int method, String url, Object request, Class<T> clazz, Listener<T> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mRetClazz = clazz;
        mListener = listener;
        if (null != request) {
            mBody = Gson.toJson(request);
        }
    }

    public GsonRequest(String url, Object request, Class<T> clazz, Listener<T> listener, ErrorListener errorListener) {
        this(Method.POST, url, request, clazz, listener, errorListener);
        Log.d(TAG, "-----url is:" + url);
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (null == mBody) {
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
            return Response.success(Gson.fromJson(jsonString, mRetClazz), HttpHeaderParser.parseCacheHeaders(response));
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
