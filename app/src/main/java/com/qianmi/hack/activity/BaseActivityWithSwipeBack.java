package com.qianmi.hack.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.qianmi.hack.LoginActivity;
import com.qianmi.hack.R;
import com.qianmi.hack.utils.L;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * Created by caozupeng on 15/9/21.
 */
public class BaseActivityWithSwipeBack extends SwipeBackActivity {

    protected AlertDialog mExitDialog;

    private ProgressDialog mLoadingDialog;

    private Snackbar mSnackbar;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //TODO
        super.onCreate(savedInstanceState);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

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
}
