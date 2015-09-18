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
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.R;
import com.qianmi.hack.app.MyVolley;
import com.qianmi.hack.bean.Order;
import com.qianmi.hack.bean.Trade;
import com.qianmi.hack.bean.TradeListResult;
import com.qianmi.hack.network.GsonRequest;
import com.qianmi.hack.utils.L;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wv on 2015/8/20.
 */
public class TradesPage extends Fragment implements View.OnClickListener, AbsListView.OnScrollListener {

    private static final String TAG = "TradePage";

    private List<Trade> mList = new ArrayList<Trade>();
    private TradeListAdapter mAdapter;
    private ExpandableListView mListView;
    private int curPage = 1;
    private boolean hasNext = true;
    private LinearLayout loading;
    private int visibleLastIndex = 0;   //最后的可视项索引
    private int visibleItemCount;       // 当前窗口可见项总数

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trade_list_view, null);
        mListView = (ExpandableListView) view.findViewById(R.id.mListView);
        loading = (LinearLayout) view.findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        initView();
        requestDate(curPage);
        return view;
    }

    private void initView() {
        mAdapter = new TradeListAdapter(TradesPage.this.getActivity(), mList);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);     //添加滑动监听
        mAdapter.notifyDataSetChanged();
       /* mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view.findViewById(R.id.updown) != null) {
                    ImageView updown = (ImageView) view.findViewById(R.id.updown);
                    if (updown != null) {
                        if ((boolean) view.getTag(R.id.updown)) {
                            updown.setImageResource(R.drawable.up);     // up
                            view.setTag(R.id.updown, true);
                        } else {
                            updown.setImageResource(R.drawable.down);     // down
                            view.setTag(R.id.updown, false);
                        }
                    }
                }
            }
        });*/
    }

    @Override
    public void onClick(View pV) {
        switch (pV.getId()) {

        }
    }

    public void loadData(final int type) {
        switch (type) {
            case 0:     // TODO 下拉刷新
                //requestDate(++curPage);
                break;

            case 1:         // TODO 加载更多
                if (hasNext) {
                    requestDate(++curPage);
                } else {
                    ((TabHostActivity) TradesPage.this.getActivity()).showSnackMsg("No more data!");
                }
                break;
        }

    }

    private Response.Listener createSuccessListener() {
        return new Response.Listener<TradeListResult>() {
            @Override
            public void onResponse(TradeListResult resp) {
                L.d("buildAppData return ");
                ((TabHostActivity) TradesPage.this.getActivity()).dismissLoadingDialog();
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
                    L.e("lonin return error");
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
        int itemsLastIndex = mAdapter.getGroupCount() - 1;    //数据集最后一项的索引
        int lastIndex = itemsLastIndex;             //加上底部的loadMoreView项
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            L.d("*************** onScrollStateChanged itemsLastIndex=" + itemsLastIndex + ", lastIndex=" + lastIndex);
            if (visibleLastIndex == lastIndex) {
                //如果是自动加载,可以在这里放置异步加载数据的代码
                ++curPage;
                if (hasNext) {
                    Log.i("LOADMORE", "loading... page: " + hasNext);
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

    private class TradeListAdapter extends BaseExpandableListAdapter {

        private LayoutInflater mInflater;
        public List<Trade> mList;

        public TradeListAdapter(Context pContext, List<Trade> pList) {
            mInflater = LayoutInflater.from(pContext);
            if (pList != null) {
                mList = pList;
            } else {
                mList = new ArrayList<Trade>();
            }
        }

        @Override
        public int getGroupCount() {
            if (mList == null) {
                return 0;
            } else {
                return mList.size();
            }
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            Trade order = mList.get(groupPosition);
            if (order != null && order.orders != null) {
                return order.orders.size();
            }
            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            if (mList != null) {
                return mList.get(groupPosition);
            } else {
                return null;
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            if (mList != null) {
                Trade order = mList.get(groupPosition);
                if (order != null && order.orders != null) {
                    return order.orders.get(childPosition);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.fragment_trade_list_item, null);

                holder = new ViewHolder();
                holder.img = (ImageView) convertView.findViewById(R.id.img);
                holder.updown = (ImageView) convertView.findViewById(R.id.updown);
                holder.tid = (TextView) convertView
                        .findViewById(R.id.tid);
                holder.payment = (TextView) convertView
                        .findViewById(R.id.payment);
                holder.totalFee = (TextView) convertView.findViewById(R.id.totalFee);
                holder.payStatus = (TextView) convertView
                        .findViewById(R.id.payStatus);
                holder.deliverStatus = (TextView) convertView
                        .findViewById(R.id.deliverStatus);
                holder.comletetStatus = (TextView) convertView
                        .findViewById(R.id.comletetStatus);
                holder.status = (TextView) convertView.findViewById(R.id.status);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Trade ai = mList.get(groupPosition);
            if (ai.complete_status == 0) { //进行中
                holder.status.setText("进行中");
            } else if (ai.complete_status == 1) { //已完成
                holder.status.setText("已完成");
            } else if (ai.complete_status == 2) { //作废
                holder.status.setText("作废");
            }
            holder.tid.setText("订单号:" + ai.tid);
            holder.payment.setText("应付金额: ￥" +
                    String.valueOf(ai.payment));
            holder.totalFee.setText("订单总额: ￥" +
                    String.valueOf(ai.total_fee));
            holder.payStatus.setText(ai.pay_status_display);
            holder.deliverStatus.setText(ai.deliver_status_display);
            holder.comletetStatus.setText(ai.complete_status_display);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            Trade ai = mList.get(groupPosition);
            if (ai == null || ai.orders == null) {
                return null;
            }
            ChildViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.fragment_trade_list_item_child, null);

                holder = new ChildViewHolder();
                holder.img = (ImageView) convertView.findViewById(R.id.img);
                holder.name = (TextView) convertView
                        .findViewById(R.id.name);
                holder.price = (TextView) convertView
                        .findViewById(R.id.price);
                holder.num = (TextView) convertView
                        .findViewById(R.id.num);
                convertView.setTag(holder);
            } else {
                holder = (ChildViewHolder) convertView.getTag();
            }

            Order order = ai.orders.get(childPosition);
            if (order != null) {
                //holder.img.setImageResource(ai.img);
                holder.name.setText(order.title);
                holder.price.setText("￥" + String.valueOf(order.unit_cost));
                holder.num.setText("X" + String.valueOf(order.num));
            }

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    private static class ChildViewHolder {
        private ImageView img;
        private TextView name;
        private TextView price;
        private TextView num;
    }

    private static class ViewHolder {
        private ImageView img;
        private ImageView updown;
        private TextView tid;
        private TextView payment;
        private TextView totalFee;
        private TextView payStatus;
        private TextView deliverStatus;
        private TextView comletetStatus;
        private TextView status;
    }
}
