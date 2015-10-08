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
import java.util.Stack;

/**
 * Created by wv on 2015/8/19.
 */
public class PcApplication extends Application {

    public static final String SERVER_URL = "http://frey.sj001.com/";
    public static String TOKEN = "";
    public static String INSTALLATION_ID = "";
    public static final String SIGN_SECRET = "secret";
    private Stack<WeakReference<Activity>> activityStacks = new Stack<WeakReference<Activity>>();
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
            activityStacks.add(new WeakReference<>(activity));
        }
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity currentActivity() {
        WeakReference<Activity> activityRef = activityStacks.lastElement();
        return activityRef.get();
    }


    /**
     * 移除当前Activity（堆栈中最后一个压入的）
     */
    public void removeActivity() {
        WeakReference<Activity> activityRef = activityStacks.lastElement();
        removeActivity(activityRef);
    }


    /**
     * 移除指定的Activity
     */
    public void removeActivity(WeakReference<Activity> activityRef) {
        if (activityRef != null) {
            activityStacks.remove(activityRef);
        }
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    public void finishActivity() {
        WeakReference<Activity> activityRef = activityStacks.lastElement();
        finishActivity(activityRef);
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(WeakReference<Activity> activityRef) {
        if (activityRef != null) {
            activityStacks.remove(activityRef);
            Activity activity = activityRef.get();
            activity.finish();
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        for (WeakReference<Activity> ref : activityStacks) {
            Class<?> refClazz = ref.get().getClass();
            if (refClazz.equals(cls)) {
                finishActivity(ref);
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStacks.size(); i < size; i++) {
            if (activityStacks.get(i) != null) {
                finishActivity(activityStacks.get(i));
            }
        }
        activityStacks.clear();
    }

    public void printActivityStackInfo() {
        L.d(" -- bottom --");
        for (WeakReference activity : activityStacks) {
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
        for (WeakReference activity : activityStacks) {
            if (activity != null && activity.get() != null) {
                //L.d("Exit" + ((Activity)activity.get()).getClass().getSimpleName() + " --");
                ((Activity) activity.get()).finish();
            }
        }

        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
