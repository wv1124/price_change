package com.qianmi.hack.test;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.network.GsonRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by caozupeng on 15/9/2.
 */
public class GsonRequestTest extends InstrumentationTestCase {
    private static String TAG = "UnitTest";
    private RequestQueue mVolleyQueue;
    private String mToken;

    /**
     * setUp两件事
     * 1、初始化Volley请求队列
     * 2、获取JWT的Token
     * 由于服务端接口都是基于JWT保护的，所以在进行业务接口测试之前
     * 需要首先获得JWT的Token，这个步骤就放在了Setup方法中
     */
    @Override
    protected void setUp() {
        try {
            super.setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mVolleyQueue = Volley.newRequestQueue(getInstrumentation().getContext());
        try {
            login();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void login() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        GsonRequest.Builder<Map> builder = new GsonRequest.Builder<>();
        Map<String, String> loginInfo = new HashMap<>();
        loginInfo.put("username", "caozupeng");
        loginInfo.put("password", "caozupeng");
        GsonRequest loginRequest = builder.registerRetClass(Map.class)
                .setUrl("http://frey.sj001.com/api-token-auth/")
                .setRequest(loginInfo)
                .registerResListener(new Response.Listener<Map<String, String>>() {
                    @Override
                    public void onResponse(Map<String, String> response) {
                        Log.d(TAG, "response = " + response);
                        GsonRequestTest.this.mToken = response.get("token");
                        countDownLatch.countDown();
                    }
                }).registerErrorListener(new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, String.valueOf(error));
                        countDownLatch.countDown();
                    }
                }).create();
        mVolleyQueue.add(loginRequest);
        countDownLatch.await();

    }


    public void test() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        GsonRequest.Builder<Map> builder = new GsonRequest.Builder<>();
        GsonRequest request = builder.registerRetClass(Map.class)
                .setUrl("http://frey.sj001.com/batchs/")
                .method(Request.Method.GET)
                .setToken(this.mToken)
                .registerResListener(new Response.Listener<Map<String, String>>() {
                    @Override
                    public void onResponse(Map<String, String> response) {
                        Log.d(TAG, String.valueOf(response));
                        countDownLatch.countDown();
                    }
                }).registerErrorListener(new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, String.valueOf(error));
                        countDownLatch.countDown();
                    }
                }).create();
        mVolleyQueue.add(request);
        countDownLatch.await();

    }

    public void testMappingUpdate() throws  Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final Map<String, String> request = new HashMap<String, String>();
        request.put("token", "cao");
        request.put("installation", "xxx-xxxx-xxxx");
        GsonRequest.Builder<Map> builder = new GsonRequest.Builder<>();
        GsonRequest infoRequest = builder
                .registerRetClass(Map.class)
                .setUrl("http://frey.sj001.com/tokens/")
                .setRequest(request)
                .setToken(this.mToken)
                .registerResListener(new Response.Listener<Map>() {
                    @Override
                    public void onResponse(Map response) {
                        Log.d(TAG, String.valueOf(request));
                        countDownLatch.countDown();
                    }
                })

                .create();
        mVolleyQueue.add(infoRequest);
        countDownLatch.await();
    }
}
