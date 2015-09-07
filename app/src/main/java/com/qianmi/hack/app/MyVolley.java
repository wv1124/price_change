package com.qianmi.hack.app;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.qianmi.hack.network.JwtAuthStack;

/**
 * 使用单例模式初始化Volley
 * Created by caozupeng on 15/9/7.
 */
public class MyVolley {
    private static RequestQueue requestQueue;
    private static JwtAuthStack jwtAuthStack;

    private MyVolley() {
        // no instances
    }

    public static void init(Context context) {
        jwtAuthStack = new JwtAuthStack();
        requestQueue = Volley.newRequestQueue(context, jwtAuthStack);
    }

    public static RequestQueue getRequestQueue() {
        if (requestQueue != null) {
            return requestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    public static JwtAuthStack getJwtAuthStack() {
        if (jwtAuthStack !=null) {
            return jwtAuthStack;
        }
        else {
            throw new IllegalStateException("HttpStack not initialized");
        }
    }
}
