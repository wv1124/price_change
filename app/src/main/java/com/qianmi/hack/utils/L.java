package com.qianmi.hack.utils;

import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log相关
 * Created by Chen Haitao on 2015/7/6.
 */
public class L {

    private L() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static boolean isDebug = true;
    private static final String TAG = "epos";

    /**
     * android logback logger
     */
    public static Logger log = LoggerFactory.getLogger(L.class);

    public static String getTag() {
        return TAG;
    }

    public static void i(String msg) {
        if (isDebug) {
            Log.i(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (isDebug) {
            Log.e(TAG, msg);
            log.error(msg);
        }
    }

    public static void v(String msg) {
        if (isDebug) {
            Log.v(TAG, msg);
            log.info(msg);

        }
    }

    public static void i(String tag, String msg) {
        if (isDebug) {
            Log.i(tag, msg);
            log.info(tag + "===" + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(tag, msg);
            log.debug(tag + "===" + msg);

        }
    }


    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, msg);
            log.error(tag + "===" + msg);
        }
    }

    public static void v(String tag, String msg) {
        if (isDebug) {
            Log.v(tag, msg);
            log.info(tag + "===" + msg);
        }
    }
}
