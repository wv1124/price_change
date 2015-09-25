package com.qianmi.hack.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.R;
import com.qianmi.hack.common.MyVolley;
import com.qianmi.hack.base.BaseActivity;
import com.qianmi.hack.bean.Order;
import com.qianmi.hack.bean.Trade;
import com.qianmi.hack.bean.TradeListResult;
import com.qianmi.hack.network.GsonRequest;
import com.qianmi.hack.utils.L;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by wv on 2015/8/20.
 */
public class TradesPage extends Fragment implements OnRefreshListener, View.OnClickListener, AbsListView.OnScrollListener {

    private static final String TAG = "TradePage";

    private List<Trade> mList = new ArrayList<>();
    private CustomListAdaper mAdapter;
    private ListView mListView;
    private int currentPage = 1;
    private boolean hasNext = true;
    private LinearLayout loading;
    private int visibleLastIndex = 0;   //最后的可视项索引
    private int visibleItemCount;       // 当前窗口可见项总数
    //下拉刷新组件
    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trade_list_view, null);
        mListView = (ListView) view.findViewById(R.id.mListView);
        loading = (LinearLayout) view.findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                // Mark All Children as pullable
                .allChildrenArePullable()
                        // Set a OnRefreshListener
                .listener(this)
                        // Finally commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);
        initView();
        requestDate(currentPage);
        return view;
    }

    @Override
    public void onRefreshStarted(View view) {
        Log.d(TAG, "上一页");
        loading.setVisibility(View.VISIBLE);
        mList.clear();
        currentPage = 1;
        requestDate(currentPage);
        mPullToRefreshLayout.setRefreshComplete();
    }

    private void initView() {
        mAdapter = new CustomListAdaper(getActivity(), mList);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);     //添加滑动监听
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.e(TAG, "click position:" + position);
                Trade trade = (Trade) mAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), TradeDetailActivity.class);
                intent.putExtra("tradeId", trade.tid);
                startActivity(intent);
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View pV) {
        switch (pV.getId()) {

        }
    }

    public void loadData(final int type) {
        switch (type) {
            case 0:     // TODO 下拉刷新
                //requestDate(++currentPage);
                break;

            case 1:         // TODO 加载更多
                if (hasNext) {
                    requestDate(++currentPage);
                } else {
                    ((BaseActivity) getActivity()).showSnackMsg("No more data!");
                }
                break;
        }

    }

    private Response.Listener createSuccessListener() {
        return new Response.Listener<TradeListResult>() {
            @Override
            public void onResponse(TradeListResult resp) {
                L.d("buildAppData return ");
                ((BaseActivity) getActivity()).dismissLoadingDialog();
                if (resp != null) {
                    L.d(resp.toString());
                    //mList.clear();
                    mList.addAll(resp.results);
                    if (resp.next == null || resp.next.length() == 0 || resp.next == "null") {
                        hasNext = false;
                    } else {
                        hasNext = true;
                    }

                    mAdapter.mList = mList;
                    mAdapter.notifyDataSetChanged();
                    if (loading != null) {
                        loading.setVisibility(View.GONE);
                    }

                } else {
                    L.e("fetch return error");
                }
            }
        };
    }

    private void requestDate(int currentPage) {
        GsonRequest.Builder<TradeListResult> builder = new GsonRequest.Builder<>();
        GsonRequest request = builder.retClazz(TradeListResult.class)
                .method(Request.Method.GET)
                .setUrl(PcApplication.SERVER_URL + "/trades/?page=" + currentPage)
                .registerResListener(createSuccessListener())
                .create();
        L.d("**************load date :currentPage=" + currentPage);
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
                    Log.i("LOADMORE", "loading... page: " + hasNext);
                    loading.setVisibility(View.VISIBLE);
                    requestDate(currentPage);
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.visibleItemCount = visibleItemCount;
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }


    private class CustomListAdaper extends BaseAdapter {
        private LayoutInflater mInflater;
        public List<Trade> mList;

        public CustomListAdaper(Context pContext, List<Trade> pList) {
            mInflater = LayoutInflater.from(pContext);
            if (pList != null) {
                mList = pList;
            } else {
                mList = new ArrayList<Trade>();
            }
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            if (getCount() == 0) {
                return null;
            }
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.fragment_trade_list_item, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Trade trade = mList.get(position);
            holder.totalFee.setText("￥" + String.valueOf(trade.total_fee));
            holder.orderCompleteStatus.setText(trade.complete_status_display);
            holder.orderDeliverStatus.setText(trade.deliver_status_display);
            if (trade.orders != null) {
                List<Order> orders = trade.orders;
                holder.orderCount.setText("有" + orders.size() + "个商品");
                holder.itemImg1.setVisibility(View.VISIBLE);
                holder.itemImg2.setVisibility(View.GONE);
                holder.itemImg3.setVisibility(View.GONE);
                Glide.with(convertView.getContext())
                        .load(orders.get(0).pic_path)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(holder.itemImg1);
                holder.itemImg1.setVisibility(View.VISIBLE);
                if (orders.size() >= 2) {
                    holder.itemImg2.setVisibility(View.VISIBLE);
                    Glide.with(convertView.getContext())
                            .load(orders.get(1).pic_path)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(holder.itemImg2);
                }
                if (orders.size() == 3) {
                    holder.itemImg3.setVisibility(View.VISIBLE);
                    Glide.with(convertView.getContext())
                            .load(orders.get(2).pic_path)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(holder.itemImg3);
                }

            } else {
                holder.orderCount.setText("没有商品");
            }
            holder.orderDate.setText(trade.created);

            return convertView;
        }
    }


    private static class ViewHolder {
        private TextView orderDate;
        private TextView orderDeliverStatus;
        private TextView orderCompleteStatus;
        private ImageView itemImg1;
        private ImageView itemImg2;
        private ImageView itemImg3;
        private TextView orderCount;
        private TextView totalFee;

        public ViewHolder(View convertView) {
            itemImg1 = (ImageView) convertView.findViewById(R.id.item_img1);
            itemImg2 = (ImageView) convertView.findViewById(R.id.item_img2);
            itemImg3 = (ImageView) convertView.findViewById(R.id.item_img3);
            orderDate = (TextView) convertView.findViewById(R.id.order_date);
            orderDeliverStatus = (TextView) convertView.findViewById(R.id.order_deliver_status);
            orderCompleteStatus = (TextView) convertView.findViewById(R.id.order_complete_status);
            orderCount = (TextView) convertView.findViewById(R.id.order_count);
            totalFee = (TextView) convertView.findViewById(R.id.total_fee);
        }
    }
}
