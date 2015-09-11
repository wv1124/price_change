package com.qianmi.hack.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by caozupeng on 15/9/11.
 */
public class ProductDetail implements Serializable {
    public String item_id;
    public String sku_id;
    public String product_name;
    public double sale_price;
    public double cost_price;
    public String wuzhen_product_name;
    public String wuzhen_price;
    public String xbdy_product_name;
    public String xbdyzhen_price;
    public List<List> recently_changes;
}
