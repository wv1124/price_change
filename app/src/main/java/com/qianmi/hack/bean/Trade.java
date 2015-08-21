package com.qianmi.hack.bean;

import android.content.Intent;

import java.util.ArrayList;

/**
 * Created by wv on 2015/8/21.
 */
public class Trade {

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
    public String detail;
    public ArrayList<Order> orders;

}
