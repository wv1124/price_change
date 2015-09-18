package com.qianmi.hack.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.qianmi.hack.bean.Batch;
import com.qianmi.hack.bean.Product;
import com.qianmi.hack.bean.ProductListResult;
import com.qianmi.hack.network.GsonRequest;
import com.qianmi.hack.utils.L;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


/**
 * Created by wv on 2015/8/20.
 */
public class ProductPage extends Fragment implements OnRefreshListener, View.OnClickListener, AbsListView.OnScrollListener {

    private static final String TAG = "ProductPage";

    private int visibleLastIndex = 0;   //最后的可视项索引
    private int visibleItemCount;       // 当前窗口可见项总数

    private List<Product> mList = new ArrayList<Product>();
    private CustomListAdapter mAdapter;
    private ListView mListView;
    private int currentPage = 1;
    private boolean hasNext = true;
    private LayoutInflater mInflater;
    private LinearLayout loading;
    //下拉刷新组件
    private PullToRefreshLayout mPullToRefreshLayout;
    //搜索组件
    private EditText etSearch;
    private String mKeyword;
    private ImageView deleteText;
    private Button btnSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_product_list_view, null);
        loading = (LinearLayout) view.findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        mListView = (ListView) view.findViewById(R.id.mListView);
        initPullRefresh(view);
        initListView();
        initSearchInput(view);
        requestDate(currentPage, mKeyword);
        return view;
    }

    private void initSearchInput(View view) {
        deleteText = (ImageView) view.findViewById(R.id.ivDeleteText);
        deleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etSearch.setText("");
                btnSearch.post(new Runnable() {
                    @Override
                    public void run() {
                        btnSearch.performClick();
                    }
                });
            }
        });
        btnSearch = (Button) view.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPage = 1;
                mKeyword = etSearch.getText().toString();
                mList.clear();
                Log.d(TAG, "keyword = " + mKeyword);
                requestDate(currentPage, mKeyword);
            }
        });
        etSearch = (EditText) view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    deleteText.setVisibility(View.GONE);
                } else {
                    deleteText.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    private void initPullRefresh(View view) {
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                // Mark All Children as pullable
                .allChildrenArePullable()
                        // Set a OnRefreshListener
                .listener(this)
                        // Finally commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);
    }

    @Override
    public void onRefreshStarted(View view) {
        Log.d(TAG, "上一页");
        loading.setVisibility(View.VISIBLE);
        mList.clear();
        currentPage = 1;
        requestDate(currentPage, mKeyword);
        mPullToRefreshLayout.setRefreshComplete();
    }

    private void initListView() {
        mAdapter = new CustomListAdapter(ProductPage.this.getActivity(), mList);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);     //添加滑动监听
        mAdapter.notifyDataSetChanged();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.e(TAG, "click position:" + position);
                Product product = (Product) mAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
                intent.putExtra("product", product);
                startActivity(intent);
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
                ((BaseActivity) getActivity()).dismissLoadingDialog();
                if (resp != null) {
                    L.d(resp.toString());
                    mList.addAll(resp.results);
                    //判断是否还有下一页
                    hasNext = resp.next != null;
                    mAdapter.mList = mList;
                    mAdapter.notifyDataSetChanged();
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

    private void requestDate(int currentPage, String keyword) {
        GsonRequest.Builder<ProductListResult> builder = new GsonRequest.Builder<>();
        StringBuilder url = new StringBuilder();

        url.append(PcApplication.SERVER_URL).append("supproducts/?page=").append(currentPage);

        if (keyword != null && !"".equals(keyword)) {
            try {
                String query = URLEncoder.encode(keyword, "utf-8");
                url.append("&name=").append(query);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        GsonRequest request = request = builder.retClazz(ProductListResult.class)
                .setUrl(url.toString())
                .registerResListener(createSuccessListener())
                .registerErrorListener(((BaseActivity) getActivity()).createErrorListener())
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
                ++currentPage;
                if (hasNext) {
                    Log.i(TAG, "loading... page: " + hasNext);
                    loading.setVisibility(View.VISIBLE);
                    requestDate(currentPage, mKeyword);
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
                convertView = mInflater.inflate(R.layout.fragment_product_list_item, null);

                holder = new ViewHolder(convertView);
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

        public ViewHolder(View v) {
            mImage = (ImageView) v.findViewById(R.id.img);
            name = (TextView) v.findViewById(R.id.name);
            salePrice = (TextView) v.findViewById(R.id.sale_price);
            updateTime = (TextView) v.findViewById(R.id.update_time);
        }
    }

}
