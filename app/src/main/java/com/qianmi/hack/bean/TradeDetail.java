package com.qianmi.hack.bean;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by caozupeng on 15/9/23.
 */
public class TradeDetail implements Serializable {
    public String tid;
    public double payment;
    public double pay_status;
    public String pay_status_display;
    public double deliver_status;
    public String deliver_status_display;
    public double complete_status;
    public String complete_status_display;
    public double total_fee;
    public String applier_nick;
    public String reciver_name;
    public String add_user_name;
    public String sale_user_name;
    public String created;
    public String end_time;
    public String applier_memo;
    public double post_fee;
    public double discount_fee;
    public String reciver_state;
    public String reciver_city;
    public String reciver_district;
    public String reciver_address;
    public String reciver_mobile;
    public String pay_type_name;
    public double total_trade_fee;
    public int invoice_flag;
    public String invoice_flag_display;

    public ArrayList<Order> orders;

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(reciver_state)) {
            sb.append(reciver_state);
        }
        if (!TextUtils.isEmpty(reciver_city)) {
            sb.append(reciver_city);
        }
        if (!TextUtils.isEmpty(reciver_district)) {
            sb.append(reciver_district);
        }
        if (!TextUtils.isEmpty(reciver_address)) {
            sb.append(reciver_address);
        }
        return sb.toString();
    }
}
