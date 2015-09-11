package com.qianmi.hack.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qianmi.hack.BaseActivity;
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.R;
import com.qianmi.hack.app.MyVolley;
import com.qianmi.hack.bean.Product;
import com.qianmi.hack.bean.ProductDetail;
import com.qianmi.hack.network.GsonRequest;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
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
//        this.setNeedBackGesture(true);
        detailImage = (ImageView) findViewById(R.id.product_detail_img);
        productName = (TextView) findViewById(R.id.product_name);
        supPrice = (TextView) findViewById(R.id.sup_price);
        wzPrice = (TextView) findViewById(R.id.wz_price);
        xbdyPrice = (TextView) findViewById(R.id.xbdy_price);
        chart = (LineChartView) findViewById(R.id.chart);
        product = (Product) getIntent().getSerializableExtra("product");
        productId = product.sku_id;
        productName.setText(product.product_name);
        supPrice.setText("￥" + String.valueOf(product.sale_price));
        Glide.with(this)
                .load(product.pic_url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.order_detail_proof_preload)
                .into(detailImage);
        initChart(chart);
        requestData(productId);

    }

    private void initChart(LineChartView chart) {
        chart.setInteractive(true);
        chart.setZoomType(ZoomType.HORIZONTAL);
        chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);

    }

    private Response.Listener createListener() {
        return new Response.Listener<ProductDetail>() {
            @Override
            public void onResponse(ProductDetail response) {
                if (response.wuzhen_product_name == null) {
                    wzPrice.setVisibility(View.GONE);
                } else {
                    wzPrice.setText("￥"+response.wuzhen_price);
                }
                if (response.xbdy_product_name == null) {
                    xbdyPrice.setVisibility(View.GONE);
                } else {
                    if(response.xbdyzhen_price != null) {
                        xbdyPrice.setText("￥" + response.xbdyzhen_price);
                    } else {
                        xbdyPrice.setText("");
                    }
                }
                List<PointValue> pointValues = new ArrayList<>();
                List<AxisValue> axisValues = new ArrayList<>();
                int i = 1;
                for (List pairs : response.recently_changes) {
                    int x = ((Double) pairs.get(0)).intValue();
                    int y = ((Double) pairs.get(1)).intValue();
                    pointValues.add(new PointValue(x, y));
                    axisValues.add(new AxisValue(x).setLabel(String.valueOf(i)));
                    i++;
                }
                //In most cased you can call data model methods in builder-pattern-like manner.
                Line line = new Line(pointValues).setColor(R.color.primary).setCubic(false);
                line.setStrokeWidth(1);
                List<Line> lines = new ArrayList<Line>();
                lines.add(line);

                LineChartData data = new LineChartData();
                //坐标轴
                Axis axisX = new Axis(); //X轴
                axisX.setHasTiltedLabels(true);
                axisX.setTextColor(R.color.primary);
                axisX.setName("采集批次");
                axisX.setMaxLabelChars(5);
                axisX.setValues(axisValues);
                data.setAxisXBottom(axisX);

                Axis axisY = new Axis();  //Y轴
                axisY.setMaxLabelChars(5); //默认是3，只能看最后三个数字
                axisY.setName("价格");
                axisY.setTextColor(R.color.primary);
                data.setAxisYLeft(axisY);

                data.setLines(lines);
                chart.setLineChartData(data);

            }
        };
    }

    private void requestData(String productId) {
        GsonRequest.Builder<ProductDetail> builder = new GsonRequest.Builder<>();
        GsonRequest request = builder
                .retClazz(ProductDetail.class)
                .method(Request.Method.GET)
                .setUrl(PcApplication.SERVER_URL + "supproducts/" + productId)
                .registerResListener(createListener())
                .create();
        MyVolley.getRequestQueue().add(request);
    }
}
