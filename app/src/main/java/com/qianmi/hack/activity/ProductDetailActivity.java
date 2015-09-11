package com.qianmi.hack.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qianmi.hack.BaseActivity;
import com.qianmi.hack.R;
import com.qianmi.hack.bean.Product;
import com.qianmi.hack.network.GsonRequest;

import java.util.Map;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by wv on 2015/8/21.
 */
public class ProductDetailActivity extends BaseActivity {
    private ImageView detailImage;
    private TextView productName;
    private TextView supPrice;
    private TextView wzPrice;
    private TextView xbdyPrice;
    private LineChartView chart;
    private String productId;
    private Product product;

    @Override
    public void onBeginRequest() {

    }

    @Override
    public void onNetworkFailed() {

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        this.setNeedBackGesture(true);
        detailImage = (ImageView) findViewById(R.id.product_detail_img);
        productName = (TextView) findViewById(R.id.product_name);
        supPrice = (TextView) findViewById(R.id.sup_price);
        wzPrice = (TextView) findViewById(R.id.wz_price);
        xbdyPrice = (TextView) findViewById(R.id.xbdy_price);
        chart = (LineChartView) findViewById(R.id.chart);
        product = (Product) getIntent().getSerializableExtra("product");
        productId = product.sku_id;
        productName.setText(product.product_name);
        supPrice.setText("￥"+String.valueOf(product.sale_price));
        Glide.with(this)
                .load(product.pic_url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.order_detail_proof_preload)
                .into(detailImage);
        initChart(chart);

    }

    private void initChart(LineChartView chart) {
        chart.setInteractive(false);
        chart.setZoomType(ZoomType.HORIZONTAL);
    }

    private void requestData(String productId){
        GsonRequest.Builder<Map> builder = new GsonRequest.Builder<>();
    }
}
