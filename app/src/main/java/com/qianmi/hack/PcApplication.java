package com.qianmi.hack;

import android.app.Activity;
import android.app.Application;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.SaveCallback;
import com.qianmi.hack.common.MyVolley;
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
    public static String INSTALLATION_ID = "";
    public static final String SIGN_SECRET = "secret";

    private static PcApplication sInstance;

    public static PcApplication getInstance() {
        RequestManager.init(sInstance);
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化应用信息
        String appId = "4lrycm58b7jgw0rfoxa03rdr7589eojojweu2ub0lihhxtzq";
        String appKey = "cuyjnokfsi6vz4s87xu8u5b7p911fiur7tdm9a0p9d83w3i9";

        AVOSCloud.initialize(this, appId, appKey);
        AVInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
            public void done(AVException e) {
                if (e == null) {
                    // 保存成功
                    String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
                    INSTALLATION_ID = installationId;
                    // 关联  installationId 到用户表等操作……
                } else {
                    // 保存失败，输出错误信息
                }
            }
        });


        sInstance = this;
        MyVolley.init(this);
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
