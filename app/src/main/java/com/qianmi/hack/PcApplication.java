package com.qianmi.hack;

import android.app.Activity;
import android.app.Application;

import com.qianmi.hack.network.RequestManager;
import com.qianmi.hack.utils.L;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

/**
 * Created by wv on 2015/8/19.
 */
public class PcApplication extends Application {

    public static final String SERVER_URL = "http://frey.sj001.com/";
    public static String TOKEN = "";
    public static final String SIGN_SECRET = "secret";

    private static PcApplication sInstance;

    public static PcApplication getInstance() {
        RequestManager.init(sInstance);
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void addActivity(Activity activity) {
        if (null != activity) {
            activityList.add(new WeakReference<>(activity));
        }
    }

    private LinkedList<WeakReference<Activity>> activityList = new LinkedList<WeakReference<Activity>>();

    public void printActivityStackInfo() {
        L.d(" -- bottom --");
        for (WeakReference activity : activityList) {
            if (activity != null) {
                Activity a = (Activity) activity.get();
                if (a != null) {
                    L.d(" -- " + a.getClass().getSimpleName() + " --");
                }
            }
        }
        L.d(" -- top --");
    }

    public void exit() {
        printActivityStackInfo();
        for (WeakReference activity : activityList) {
            if (activity != null && activity.get() != null) {
                //L.d("Exit" + ((Activity)activity.get()).getClass().getSimpleName() + " --");
                ((Activity) activity.get()).finish();
            }
        }

        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
