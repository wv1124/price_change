package com.qianmi.hack.bean;

import android.content.Intent;

/**
 * Created by wv on 2015/8/21.
 */
public class PriceChange {
    public String batch;
    public String supplier;
    public Double old_price;
    public Double new_price;
    public String gonghuo_product;
    public String gonghuo_product_name;
    public Double gonghuo_price;
    public Double draft_price;
    public Intent type;
    public boolean is_sync;
}
