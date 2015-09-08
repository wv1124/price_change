package com.qianmi.hack.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.avos.avoscloud.PushService;
import com.qianmi.hack.BaseActivity;
import com.qianmi.hack.R;


/**
 * Created by wv on 2015/8/20.
 */
public class TabHostActivity extends BaseActivity {

    //定义FragmentTabHost对象
    private FragmentTabHost mTabHost;

    //定义一个布局
    private LayoutInflater layoutInflater;

    //定义数组来存放Fragment界面
    private Class fragmentArray[] = {ProductPage.class, BatchPage.class, TradesPage.class, PrefsFragement.class};

    //定义数组来存放按钮图片
    private int mImageViewArray[] = {R.drawable.tab_home_btn, R.drawable.tab_message_btn, R.drawable.tab_selfinfo_btn,
            R.drawable.tab_more_btn};

    //Tab选项卡的文字
    private String mTextviewArray[] = {"商品", "调价", "订单", "设置"};

    private TextView title;

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
}
