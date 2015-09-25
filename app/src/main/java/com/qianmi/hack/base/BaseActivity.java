package com.qianmi.hack.base;

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
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.R;
import com.qianmi.hack.listener.BackGestureListener;
import com.qianmi.hack.utils.L;
import com.qianmi.hack.view.LoginActivity;

import java.lang.reflect.Method;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    private CommonReceiver mCommonReceiver = null;
    protected AlertDialog mExitDialog;

    private boolean mNetworkOK = false;

    private boolean mShowBackIcon = true;
    private boolean mShowDefaultMenu = true;

    private ProgressDialog mLoadingDialog;
    private Snackbar mSnackbar;

    /** 手势监听 */
    GestureDetector mGestureDetector;
    /** 是否需要监听手势关闭功能 */
    private boolean mNeedBackGesture = false;

    @Nullable
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Nullable
    @Bind(R.id.toolbar_title)
    TextView mToolbarTitle;

    private void initGestureDetector() {
        if (mGestureDetector == null) {
            mGestureDetector = new GestureDetector(getApplicationContext(),
                    new BackGestureListener(this));
        }
    }

    /*
        * 设置是否进行手势监听
    */
    public void setNeedBackGesture(boolean mNeedBackGesture){
        this.mNeedBackGesture = mNeedBackGesture;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        if(mNeedBackGesture){
            return mGestureDetector.onTouchEvent(ev) || super.dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
    /**
     * callback which need to do for request http when enter current view
     */
    public abstract void onBeginRequest();

    /**
     * callback which need to do when network failed or cut, for example: unable button for further action
     */
    public abstract void onNetworkFailed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //TODO
        super.onCreate(savedInstanceState);
        initGestureDetector();


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

    public void handleError(VolleyError error) {
        if (error instanceof AuthFailureError) {
            L.d("authfailure");
            Toast.makeText(this, this.getString(R.string.session_expire), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
        } else {
            this.dismissLoadingDialog();
            L.e(error.getMessage());
            this.showSnackMsg(R.string.login_err_poor_network);
        }
    }

    public Response.ErrorListener createErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleError(error);
            }
        };
    }

}