package com.qianmi.hack.test;

import android.test.InstrumentationTestCase;

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

    RequestQueue mVolleyQueue;
    @Override
    protected void setUp(){
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
                        System.out.println(response);
                        PcApplication.TOKEN = response.get("token");
                        countDownLatch.countDown();
                    }
                }).registerErrorListener(new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error);
                        countDownLatch.countDown();
                    }
                }).create();
        mVolleyQueue.add(loginRequest);
        countDownLatch.await();

    }


    public void test() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        GsonRequest.Builder<Map> builder = new GsonRequest.Builder<>();
        GsonRequest<Map> request = builder.setUrl("http://frey.sj001.com/batchs/")
//                .setRequest(new Object())
                .registerRetClass(Map.class)
                .method(Request.Method.GET)
                .registerResListener(new Response.Listener<Map>() {
                    @Override
                    public void onResponse(Map response) {
                        System.out.println(response);
                        countDownLatch.countDown();
                    }
                }).registerErrorListener(new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error);
                        countDownLatch.countDown();
                    }
                }).create();
        mVolleyQueue.add(request);
        countDownLatch.await();

    }
}
