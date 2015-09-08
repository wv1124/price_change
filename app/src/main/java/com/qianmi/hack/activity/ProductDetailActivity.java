package com.qianmi.hack.activity;

import android.os.Bundle;

import com.qianmi.hack.BaseActivity;
import com.qianmi.hack.R;

/**
 * Created by wv on 2015/8/21.
 */
public class ProductDetailActivity extends BaseActivity {

    @Override
    public void onBeginRequest() {

    }

    @Override
    public void onNetworkFailed() {

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tab_layout);
    }
}
