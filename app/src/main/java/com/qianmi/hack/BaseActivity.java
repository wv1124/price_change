package com.qianmi.hack;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.qianmi.hack.utils.L;

import org.json.JSONObject;

import java.lang.reflect.Method;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Chen Haitao on 2015/7/6.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private CommonReceiver mCommonReceiver = null;
    protected AlertDialog mExitDialog;

    private RequestQueue mVolleyQueue;
    public Request<JSONObject> mRequest;

    private boolean mNetworkOK = false;

    private boolean mShowBackIcon = true;
    private boolean mShowDefaultMenu = true;

    private ProgressDialog mLoadingDialog;
    private Snackbar mSnackbar;

    @Nullable
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Nullable
    @Bind(R.id.toolbar_title)
    TextView mToolbarTitle;

    /**
     * callback which need to do for request http when enter current view
     */
    public abstract void onBeginRequest();

    /**
     * callback which need to do when network failed or cut, for example: unable button for further action
     */
    public abstract void onNetworkFailed();

    /**
     * return true if want to use volley in this activity
     *
     * @return
     */
    public abstract boolean needInitRequestQueue();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (needInitRequestQueue()) {
            mVolleyQueue = Volley.newRequestQueue(this);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //TODO
        super.onCreate(savedInstanceState);


    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        setupToolbar();
        String title = getTitle().toString();
        if (mToolbarTitle != null) {
            mToolbarTitle.setText(title);
        }
        setTitle("");
    }

    protected void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (isShowBackIcon()) {
                toolbar.setNavigationIcon(R.drawable.toolbar_back);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

            }
            ActionBar ab = getActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregister();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyResources();
        ButterKnife.unbind(this);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        setOverflowIconVisible(featureId, menu);
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isShowDefaultMenu()) {
            getMenuInflater().inflate(R.menu.menu_toolbar_common, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        L.d("---> onOptionsItemSelected");

        if (isShowDefaultMenu()) {
            int id = item.getItemId();


            switch (id) {
                case R.id.menu_exit:
                    L.d("menu : reprint");
                    exitApplication(this);
                    break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void destroyResources() {

        if (mVolleyQueue != null) {
            mVolleyQueue.cancelAll(this);
        }

        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }

    }

    private void setOverflowIconVisible(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    L.d("OverflowIconVisible", e.getMessage());
                }
            }
        }
    }

    private class CommonReceiver extends BroadcastReceiver {
        boolean success = false;

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = null;
            if (intent != null) {
                action = intent.getAction();
            }

            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (context != null) {
                    ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                    if (mNetworkInfo != null) {
                        success = true;
                    } else {
                        success = false;
                        showSnackMsg(getString(R.string.netConnectedError));
                    }
                }
                mNetworkOK = success;
                if (success) {
                    onBeginRequest();
                } else {
                    onNetworkFailed();
                }

            }
        }
    }

    private void register() {
        mCommonReceiver = new CommonReceiver();
        // Registry network monitoring
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        // filter.addAction(Constant.ACTION_FINISHI_ACTION);
        registerReceiver(mCommonReceiver, filter);
    }

    private void unregister() {
        unregisterReceiver(mCommonReceiver);
    }


    /**
     * exit all application
     * all activities will finished itself one by one
     *
     * @param context
     */
    public void exitApplication(Context context) {
        mExitDialog = new AlertDialog.Builder(context).create();
        mExitDialog.show();
        mExitDialog.getWindow().setContentView(R.layout.dialog_exit_alert);
        mExitDialog.getWindow().findViewById(R.id.tv_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExitDialog.dismiss();
                mExitDialog = null;
                PcApplication application = (PcApplication) getApplication();
                application.exit();
            }
        });
        mExitDialog.getWindow().findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExitDialog.dismiss();
            }
        });
    }

    /* volley request */
    private void executeRequest(Request<?> request) {
        if (mNetworkOK) {
            if (request != null) {
                showLoadingDialog();
                mVolleyQueue.add(request);
            } else {
                L.e("request is null");
                showSnackMsg(getString(R.string.requestError));
            }
        } else {
            // network is not ok
            request.deliverError(new VolleyError(getString(R.string.netConnectedError)));
            showSnackMsg(getString(R.string.netConnectedError));
        }
    }

    /**
     * add a request to volley queue by a named tag
     *
     * @param req
     * @param tag
     * @param <T>
     */
    public <T> void startRequest(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? L.getTag() : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        executeRequest(req);
    }

    /**
     * add a request to volley queue by default tag("epos")
     *
     * @param req
     * @param <T>
     */
    public <T> void startRequest(Request<T> req) {
//        // set the default tag if tag is empty
//        req.setTag(L.getTag());
        L.i("###################### start a request : " + this.getClass().getSimpleName());
        L.i("###################### url : " + req.getUrl());
        L.i("###################### original url : " + req.getOriginUrl());
        executeRequest(req);
    }

    /**
     * cancel pending request by tag
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mVolleyQueue != null) {
            mVolleyQueue.cancelAll(tag);
        }
    }

    /* overview dialog */

    /**
     * show loading dialog, after sent request and loading finished, sub-activity should call dismissLoadingDialog
     */
    public void showLoadingDialog(String loadingStr) {
        //if (isShowLoadingDialog()) {
        if (null != mLoadingDialog) {
            mLoadingDialog.dismiss();
        }
        mLoadingDialog = new ProgressDialog(this);
        mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoadingDialog.setMessage(getString(R.string.dialog_common_loading));
        mLoadingDialog.setIndeterminate(true);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.show();
        //}
    }

    /**
     * show a normal loading dialog, everywhere on sub class can call this
     */
    public void showLoadingDialog() {
        showLoadingDialog(getString(R.string.dialog_common_loading));
    }

    /**
     * dismiss loading dialog
     */
    public void dismissLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }

    /* utils */

    /**
     * show a SnackBar message(instead of old Toast)
     *
     * @param view docking view
     * @param msg
     */
    public void showSnackMsg(View view, String msg) {
        if (mSnackbar != null) {
            mSnackbar.dismiss();
        }
        if (view != null) {
            mSnackbar = Snackbar.make(view, msg, Snackbar.LENGTH_LONG);
            mSnackbar.show();
        } else {
            mSnackbar = Snackbar.make(getWindow().getDecorView(), msg, Snackbar.LENGTH_LONG);
            mSnackbar.show();
        }
    }


    /**
     * show a Snackbar message
     *
     * @param msg
     */
    public void showSnackMsg(String msg) {
        showSnackMsg(toolbar, msg);
    }

    /**
     * show a Snackbar message by a string id
     *
     * @param resId
     */
    public void showSnackMsg(int resId) {
        showSnackMsg(toolbar, getResources().getString(resId));
    }

    /**
     * call the to close opening software keyboard
     * after click a button, some condition would hide the software keyboard to keep better UE
     */
    public void hideSoftKeyboard() {
        View focusingView = this.getCurrentFocus();
        if (focusingView != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(focusingView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * hide the software keyboard if it is opened,
     * show the software keyboard if it is closed.
     */
    public void toggleSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                    InputMethodManager.HIDE_NOT_ALWAYS);

        }
    }

    public void warningAnimation(View view) {
        if (view == null)
            return;
        view.setScaleX(0.2f);
        view.setScaleY(0.2f);
        view.animate().scaleX(1.0f)
                .setDuration(1300).setStartDelay(300).setInterpolator(new BounceInterpolator()).start();
        view.animate().scaleY(1.0f)
                .setDuration(1300).setStartDelay(300).setInterpolator(new BounceInterpolator()).start();
    }

    public boolean isShowBackIcon() {
        return mShowBackIcon;
    }

    public void setShowBackIcon(boolean show) {
        this.mShowBackIcon = show;
    }

    public boolean isShowDefaultMenu() {
        return mShowDefaultMenu;
    }

    public void setShowDefaultMenu(boolean show) {
        this.mShowDefaultMenu = show;
    }


}