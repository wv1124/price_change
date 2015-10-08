package com.qianmi.hack.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.avos.avoscloud.PushService;
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.R;
import com.qianmi.hack.base.BaseActivity;
import com.qianmi.hack.common.MyVolley;
import com.qianmi.hack.network.GsonRequest;
import com.qianmi.hack.utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Map;


/**
 * Created by wv on 2015/8/20.
 */
public class TabHostActivity extends BaseActivity {

    private static final String TAG = "TabHostActivity";

    //定义FragmentTabHost对象
    private FragmentTabHost mTabHost;

    //定义一个布局
    private LayoutInflater layoutInflater;

    //定义数组来存放Fragment界面
    private Class fragmentArray[] = {BatchPage.class, ProductPage.class, TradesPage.class, PrefsFragement.class};

    //定义数组来存放按钮图片
    private int mImageViewArray[] = {R.drawable.tab_home_btn, R.drawable.tab_message_btn, R.drawable.tab_selfinfo_btn,
            R.drawable.tab_more_btn};

    //Tab选项卡的文字
    private String mTextviewArray[] = {"调价", "商品", "订单", "设置"};

    private TextView title;

    //安装文件
    private File installAPK = null;

    ProgressDialog progressDialog;

    @Override
    public void onBeginRequest() {

    }

    @Override
    public void onNetworkFailed() {

    }

    public boolean needInitRequestQueue() {
        return true;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tab_layout);
        PushService.setDefaultPushCallback(this, this.getClass());
        PushService.subscribe(this, "private", this.getClass());
        initView();
    }

    public void setTitle(String str) {
        if (title != null) {
            title.setText(str);
        }
    }

    /**
     * 初始化组件
     */
    private void initView() {
        title = (TextView) this.findViewById(R.id.title);

        //实例化布局对象
        layoutInflater = LayoutInflater.from(this);

        //实例化TabHost对象，得到TabHost
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        //得到fragment的个数
        int count = fragmentArray.length;

        for (int i = 0; i < count; i++) {
            //为每一个Tab按钮设置图标、文字和内容
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
            //将Tab按钮添加进Tab选项卡中
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
            //设置Tab按钮的背景
            mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.selector_tab_background);
        }

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                setTitle(tabId);
            }
        });
        checkVersion();
    }

    /**
     * 给Tab按钮设置图标和文字
     */
    private View getTabItemView(int index) {
        View view = layoutInflater.inflate(R.layout.tab_item_view, null);

        ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
        imageView.setImageResource(mImageViewArray[index]);

        TextView textView = (TextView) view.findViewById(R.id.textview);
        textView.setText(mTextviewArray[index]);

        return view;
    }

    private void checkVersion() {
        GsonRequest.Builder<Map> builder = new GsonRequest.Builder<>();
        GsonRequest versionRequest = builder
                .retClazz(Map.class)
                .setUrl(PcApplication.SERVER_URL + "static/android/version.json")
                .method(Request.Method.GET)
                .registerResListener(new Response.Listener<Map>() {
                    @Override
                    public void onResponse(Map response) {
                        int newVersion = Double.valueOf(String.valueOf(response.get("version"))).intValue();
                        int currentVersionCode = getVersionCode(getApplication().getApplicationContext());
                        if (newVersion > currentVersionCode) {
//                            Toast.makeText(PcApplication.getInstance(), "有新版本!", Toast.LENGTH_LONG).show();
                            showUpdateDialog(PcApplication.SERVER_URL + "static/android/app-release.apk", "new version");
                        }

                    }
                })
                .create();
        MyVolley.getRequestQueue().add(versionRequest);
    }

    public static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }


    private void showUpdateDialog(final String downloadUrl, final String message) {
        AlertDialog.Builder updateAlertDialog = new AlertDialog.Builder(this);
//        updateAlertDialog.setCancelable(false);
        updateAlertDialog.setTitle("版本更新");
        updateAlertDialog.setMessage("有新版本，是否下载？");
        updateAlertDialog.setNegativeButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        mLoginBtn.setClickable(false);
                        progressDialog = new ProgressDialog(TabHostActivity.this);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.setTitle("正在下载");
                        progressDialog.setMessage("请稍候...");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                        new DownloadFileAsync().execute(downloadUrl);
                    }
                }).setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        //if (!isFinishing())
        updateAlertDialog.show();
    }


    class DownloadFileAsync extends AsyncTask<String, Integer, File> {
        private float fileSize = 0;

        @Override
        protected File doInBackground(String... params) {
            installAPK = FileUtil.createFile("freyr-new.apk");

            try {

                URL url = new URL(URLDecoder.decode(params[0], "UTF-8"));
                URLConnection conn = url.openConnection();
                conn.connect();
                InputStream in = conn.getInputStream();
                fileSize = conn.getContentLength();
                if (fileSize > 0 && null != in) {
                    int length = 0;
                    int readLength = 0;
                    byte[] data = new byte[1024];
                    if (null != installAPK && installAPK.exists()) {
                        FileOutputStream fos = new FileOutputStream(installAPK);
                        while ((length = in.read(data)) != -1) {
                            readLength += length;
                            publishProgress((int) ((readLength / (float) fileSize) * 100));
                            fos.write(data, 0, length);
                        }
                        fos.flush();
                        fos.close();
                        in.close();
                    }
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "doInBackground failure!!!", e);
                publishProgress(-1);
            } catch (IOException e) {
                publishProgress(-1);
                Log.e(TAG, "doInBackground failure!!!", e);
            }
            return installAPK;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (-1 == values[0]) {
                resetBtn();
            } else if (100 <= values[0]) {
                if (null != installAPK && installAPK.exists() && installAPK.length() > 0) {
                    // unInstall(getActivity().getPackageName());
                    progressDialog.dismiss();
                    install();
                    resetBtn();
                }
            } else {
                progressDialog.setProgress(values[0]);
            }
        }


        private void resetBtn() {
//            mLoginBtn.setClickable(true);
        }
    }

    private void install() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(installAPK), "application/vnd.android.package-archive");
        startActivity(intent);
    }
}
