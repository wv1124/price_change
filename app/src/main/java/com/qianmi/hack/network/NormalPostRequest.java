package com.qianmi.hack.network;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * POST请求
 */
public class NormalPostRequest extends Request<JSONObject> {
    private static String TAG = "NormalPostRequest";
    private Map<String, String> mMap;
    private Listener<JSONObject> mListener;

    public NormalPostRequest(String url, Listener<JSONObject> listener, ErrorListener errorListener,
                             Map<String, String> map) {
        super(Method.POST, url, errorListener);
        mListener = listener;
        mMap = map;
        Log.i(TAG, "url is:" + url);
      //  Log.i(TAG, map);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mMap;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            Log.i(TAG, "response:" + jsonString);
            return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException:" + e.getMessage());
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            Log.e(TAG, "JSONException:" + je.getMessage());
            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        mListener.onResponse(response);
    }

    @Override
    public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
        retryPolicy = new DefaultRetryPolicy(5000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        return super.setRetryPolicy(retryPolicy);
    }
}
