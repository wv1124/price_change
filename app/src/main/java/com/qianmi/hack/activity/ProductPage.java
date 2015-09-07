package com.qianmi.hack.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.qianmi.hack.bean.ProductListResult;
import com.qianmi.hack.network.GsonRequest;
import com.qianmi.hack.utils.L;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wv on 2015/8/20.
 */
public class ProductPage extends Fragment implements View.OnClickListener, AbsListView.OnScrollListener {

    private static final String TAG = "MainActivity";

    private static final int LOAD_DATA_FINISH = 10;

    private int visibleLastIndex = 0;   //最后的可视项索引
    private int visibleItemCount;       // 当前窗口可见项总数

    private List<Product> mList = new ArrayList<Product>();
    private CustomListAdapter mAdapter;
    private ListView mListView;
    private int curPage = 1;
    private boolean hasNext = true;
    private LayoutInflater mInflater;
    private LinearLayout loading;


    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_DATA_FINISH:
                    mAdapter.mList = mList;
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }

        ;
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        View view = inflater.inflate(R.layout.activity_productlist, null);
        loading = (LinearLayout) view.findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        mListView = (ListView) view.findViewById(R.id.mListView);

        initView();
        requestDate(curPage);
        return view;
    }

    private void initView() {
        mAdapter = new CustomListAdapter(ProductPage.this.getActivity(), mList);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);     //添加滑动监听
        mAdapter.notifyDataSetChanged();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.e(TAG, "click position:" + position);
            }
        });
    }

    @Override
    public void onClick(View pV) {
        switch (pV.getId()) {

        }
    }

    /**
     * 在界面中下拉到底部，实现加载更多
     *
     * @return
     */
    private Response.Listener<ProductListResult> createSuccessListener() {
        return new Response.Listener<ProductListResult>() {
            @Override
            public void onResponse(ProductListResult resp) {
                L.i(TAG, "product data return ");
                ((TabHostActivity) ProductPage.this.getActivity()).dismissLoadingDialog();
                if (resp != null) {
                    L.d(resp.toString());
                    mList.addAll(resp.results);
                    //判断是否还有下一页
                    hasNext = resp.next != null;
                    //将结果通知到监听器中，修改界面显示结果
                    Message _Msg = mHandler.obtainMessage(LOAD_DATA_FINISH, mList);
                    mHandler.sendMessage(_Msg);
                    //将load控件隐藏掉
                    if (loading != null) {
                        loading.setVisibility(View.GONE);
                    }
                } else {
                    L.e(TAG, "fetch product list return error");
                }
            }
        };
    }

    private void requestDate(int currentPage) {
        GsonRequest.Builder<ProductListResult> builder = new GsonRequest.Builder<>();
        GsonRequest request = builder.retClazz(ProductListResult.class)
                .setUrl(String.format("%s/supproducts/?page=%d", PcApplication.SERVER_URL, currentPage))
                .registerResListener(createSuccessListener())
                .registerErrorListener(((BaseActivity) this.getActivity()).createErrorListener())
                .method(Request.Method.GET)
                .create();
        L.d("**************load data :currentPage=" + currentPage);
        MyVolley.getRequestQueue().add(request);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int itemsLastIndex = mAdapter.getCount() - 1;    //数据集最后一项的索引
        int lastIndex = itemsLastIndex;             //加上底部的loadMoreView项
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            L.d("*************** onScrollStateChanged itemsLastIndex=" + itemsLastIndex + ", lastIndex=" + lastIndex);
            if (visibleLastIndex == lastIndex) {
                //如果是自动加载,可以在这里放置异步加载数据的代码
                ++curPage;
                if (hasNext) {
                    Log.i(TAG, "loading... page: " + hasNext);
                    loading.setVisibility(View.VISIBLE);
                    requestDate(curPage);
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //L.d("------------ onScrollStateChanged firstVisibleItem=" + firstVisibleItem
        //        + ", visibleItemCount=" + visibleItemCount + ", totalItemCount=" + totalItemCount);
        this.visibleItemCount = visibleItemCount;
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }

    private class CustomListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        public List<Product> mList;

        public CustomListAdapter(Context pContext, List<Product> pList) {
            mInflater = LayoutInflater.from(pContext);
            if (pList != null) {
                mList = pList;
            } else {
                mList = new ArrayList<Product>();
            }
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            System.out.println("getItemId = " + position);
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (getCount() == 0) {
                return null;
            }
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.product_listitem, null);

                holder = new ViewHolder();
                holder.mImage = (ImageView) convertView
                        .findViewById(R.id.img);
                holder.name = (TextView) convertView
                        .findViewById(R.id.name);
                holder.salePrice = (TextView) convertView.findViewById(R.id.sale_price);

                holder.updateTime = (TextView) convertView.findViewById(R.id.update_time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Product ai = mList.get(position);
            Glide.with(ProductPage.this.getActivity())
                    .load(ai.pic_url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.order_detail_proof_preload)
                    .into(holder.mImage);
            //holder.mImage.setImageUrl(ai.getAppIcon());
            holder.name.setText(ai.product_name);
            holder.salePrice.setText("￥" + String.valueOf(ai.sale_price));
            if (ai.updated != null && ai.updated.length() > "2015-08-18".length()) {
                String date = ai.updated.substring(0, "2015-08-18".length());
                holder.updateTime.setText(date);
            }
            return convertView;
        }
    }

    private static class ViewHolder {
        private ImageView mImage;
        private TextView name;
        private TextView salePrice;
        private TextView updateTime;
    }

}
