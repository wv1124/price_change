package com.qianmi.hack.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.R;
import com.qianmi.hack.app.MyVolley;
import com.qianmi.hack.bean.TradeDetail;
import com.qianmi.hack.network.GsonRequest;

/**
 * Created by caozupeng on 15/9/23.
 * Display the trade detail include address reciver_name
 * orders is also need
 */
public class TradeDetailActivity extends BaseActivityWithSwipeBack {
    private static String TAG = "TradeDetailActivity";

    TextView orderDeliverStatus;
    TextView orderNo;
    TextView orderDate;
    TextView totalFee;
    TextView discountFee;
    TextView postFee;
    TextView totalTradeFee;
    TextView orderPayStatus;
    TextView reciverName;
    TextView reciverMobile;
    TextView reciverAddress;
    String tradeId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        orderDeliverStatus = (TextView) findViewById(R.id.order_deliver_status);
        orderNo = (TextView) findViewById(R.id.order_no);
        orderDate = (TextView) findViewById(R.id.order_date);
        totalFee = (TextView) findViewById(R.id.total_fee);
        discountFee = (TextView) findViewById(R.id.discount_fee);
        postFee = (TextView) findViewById(R.id.post_fee);
        totalTradeFee = (TextView) findViewById(R.id.total_trade_fee);
        orderPayStatus = (TextView) findViewById(R.id.order_pay_status);
        reciverName = (TextView) findViewById(R.id.reciver_name);
        reciverMobile = (TextView) findViewById(R.id.reciver_mobile);
        reciverAddress = (TextView) findViewById(R.id.reciver_address);
        tradeId = (String) getIntent().getSerializableExtra("tradeId");
        Log.v(TAG, String.format("get tradeId is %s", tradeId));
        requestData(tradeId);
    }

    private Response.Listener createSuccessListener() {
        return new Response.Listener<TradeDetail>() {
            @Override
            public void onResponse(TradeDetail response) {
                (TradeDetailActivity.this).dismissLoadingDialog();
                Log.d(TAG, "trade detail return");
                orderDeliverStatus.setText(response.deliver_status_display);
                orderNo.setText(response.tid);
                orderDate.setText(response.created);
                totalFee.setText("￥"+String.valueOf(response.total_fee));
                discountFee.setText("￥"+String.valueOf(response.discount_fee));
                postFee.setText("￥"+String.valueOf(response.post_fee));
                totalTradeFee.setText("￥"+String.valueOf(response.total_trade_fee));
                orderPayStatus.setText(response.pay_status_display);
                reciverName.setText(response.reciver_name);
                reciverMobile.setText(response.reciver_mobile);
                reciverAddress.setText(response.getFullAddress());
            }
        };
    }

    private void requestData(String tradeId) {
        GsonRequest.Builder<TradeDetail> builder = new GsonRequest.Builder<>();
        GsonRequest request = builder.retClazz(TradeDetail.class)
                .method(Request.Method.GET)
                .setUrl(String.format("%s/trades/%s", PcApplication.SERVER_URL, tradeId))
                .registerResListener(createSuccessListener())
                .registerErrorListener(this.createErrorListener())
                .create();
        MyVolley.getRequestQueue().add(request);
    }

}
