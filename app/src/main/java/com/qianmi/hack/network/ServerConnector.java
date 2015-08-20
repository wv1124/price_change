//
// Copyright (c) 2013 Samsung Electronics Co., Ltd. All Rights Reserved.
// 
// This software and its documentation are the confidential and proprietary information
// of Samsung Electronics Co., Ltd. 
// 
// No part of the software and documents may be copied, reproduced, transmitted, translated, or reduced
// to any electronic medium or machine-readable form without the prior written consent of Samsung Electronics.
//

package com.qianmi.hack.network;

import android.app.Application;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RequestQueue.RequestFilter;
import com.android.volley.toolbox.ImageLoader;

public class ServerConnector {

    private static final String TAG = ServerConnector.TAG;

    private final RequestQueue mRequestQueue;

    private ImageLoader mImageLoader;

    // for singleton
    private volatile static ServerConnector mInstance;

    public static ServerConnector getInstance(Application app) {
        if (mInstance == null) {
            synchronized (ServerConnector.class) {
                if (mInstance == null) {
                    mInstance = new ServerConnector(app);
                }
            }
        }
        return mInstance;
    }

    private ServerConnector(Context context) {

        // Note: this was using OKHttp stack before, but that caused some images to fail
        // Specifically some Facebook images, and any image from twitter that has a video
        // HurlStack hurlstack = new HurlStack(null, ServerConnectorUtil.getSSLSocketFactory());
        // mRequestQueue = Volley.newRequestQueue(context, hurlstack);
        //        mRequestQueue = Volley.newRequestQueue(context);
        //        VolleyLog.setTag(TAG);
        //        VolleyLog.DEBUG = false;

        mRequestQueue = RequestManager.getRequestQueue();
        //        mImageLoader = new ImageLoader(mRequestQueue, new LruImageCache(context));
        //        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
        //
        //            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);
        //
        //            public void putBitmap(String url, Bitmap bitmap) {
        //                mCache.put(url, bitmap);
        //            }
        //
        //            public Bitmap getBitmap(String url) {
        //                return mCache.get(url);
        //            }
        //        });

    }

    public Request add(Request request) {
        return mRequestQueue.add(request);
    }

    public void clearCache() {
        mRequestQueue.getCache().clear();
    }

    public void cancelAll() {
        mRequestQueue.cancelAll(new RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }

    public void cancelAll(RequestFilter filter) {
        mRequestQueue.cancelAll(filter);
    }

//    public ImageLoader getmImageLoader() {
//        return ImageCacheManager.getInstance().getImageLoader();
//        //        return mImageLoader;
//    }
}
