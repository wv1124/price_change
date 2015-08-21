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
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.R;
import com.qianmi.hack.bean.Order;
import com.qianmi.hack.bean.PriceChange;
import com.qianmi.hack.bean.Trade;
import com.qianmi.hack.bean.TradeListResult;
import com.qianmi.hack.network.GsonRequest;
import com.qianmi.hack.utils.L;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wv on 2015/8/20.
 */
public class TradesPage extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final int LOAD_DATA_FINISH = 10;
    private static final int REFRESH_DATA_FINISH = 11;
    private static final int LOADING_DATA = 12;

    private List<Trade> mList = new ArrayList<Trade>();
    private TradeListAdapter mAdapter;
    private ExpandableListView mListView;
    private int curPage = 1;
    private boolean hasNext = true;

    private Button mCanPullRefBtn, mCanLoadMoreBtn, mCanAutoLoadMoreBtn, mIsMoveToFirstItemBtn;

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_DATA_FINISH:
                    if (mAdapter != null) {
                        mAdapter.mList = (ArrayList<Trade>) msg.obj;
                        mAdapter.notifyDataSetChanged();
                    }
//                    mListView.onRefreshComplete();    //下拉刷新完成
                    break;
                case LOAD_DATA_FINISH:
                    if (mAdapter != null) {
                        mAdapter.mList.addAll((ArrayList<Trade>) msg.obj);
                        mAdapter.notifyDataSetChanged();
                    }
                    //mListView.onLoadMoreComplete();    //加载更多完成
                    break;
                case LOADING_DATA:
                    break;
                default:
                    break;
            }
        }

        ;
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_tradelist, null);

        mListView = (ExpandableListView) view.findViewById(R.id.mListView);
        mCanPullRefBtn = (Button) view.findViewById(R.id.canPullRefBtn);
        mCanLoadMoreBtn = (Button) view.findViewById(R.id.canLoadMoreFlagBtn);
        mCanAutoLoadMoreBtn = (Button) view.findViewById(R.id.autoLoadMoreFlagBtn);
        mIsMoveToFirstItemBtn = (Button) view.findViewById(R.id.isMoveToFirstItemBtn);
        mCanPullRefBtn.setOnClickListener(this);
        mCanLoadMoreBtn.setOnClickListener(this);
        mCanAutoLoadMoreBtn.setOnClickListener(this);
        mIsMoveToFirstItemBtn.setOnClickListener(this);
        initView();
        requestDate(curPage);
        return view;
    }

    private void initView() {
        mAdapter = new TradeListAdapter(TradesPage.this.getActivity(), mList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        });
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

    private void requestDate(int curentPage) {
        GsonRequest mRequest = new GsonRequest(Request.Method.GET,
                PcApplication.SERVER_URL + "/trades/?page=" + curentPage, null, TradeListResult.class,
                new Response.Listener<TradeListResult>() {
                    @Override
                    public void onResponse(TradeListResult resp) {
                        L.d("buildAppData return ");
                        ((TabHostActivity) TradesPage.this.getActivity()).dismissLoadingDialog();
                        if (resp != null) {
                            L.d(resp.toString());
                            mList.clear();
                            mList.addAll(resp.results);
                            if (resp.next == null || resp.next.length() == 0 || resp.next == "null") {
                                hasNext = false;
                            } else {
                                hasNext = true;
                            }
                            //mCount = resp.count;
                            //if (type == 0) {    //下拉刷新
                            //					Collections.reverse(mList);	//逆序
                            //Message _Msg = mHandler.obtainMessage(REFRESH_DATA_FINISH, mList);
                            //mHandler.sendMessage(_Msg);
                            //} else if (type == 1) {
                            Message _Msg = mHandler.obtainMessage(LOAD_DATA_FINISH, mList);
                            mHandler.sendMessage(_Msg);
                            //}

                        } else {
                            L.e("lonin return error");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ((TabHostActivity) TradesPage.this.getActivity()).dismissLoadingDialog();
                Log.e("TAG", error.getMessage(), error);
                ((TabHostActivity) TradesPage.this.getActivity()).showSnackMsg(R.string.login_err_poor_network);
            }
        });
        L.d("**************load date :curentPage=" + curentPage);
        ((TabHostActivity) TradesPage.this.getActivity()).startRequest(mRequest);
    }

    private void syncPrice(PriceChange change) {
        GsonRequest request = new GsonRequest(Request.Method.GET,
                PcApplication.SERVER_URL + "/trades/", null, TradeListResult.class,
                new Response.Listener<TradeListResult>() {
                    @Override
                    public void onResponse(TradeListResult resp) {
                        L.d("buildAppData return ");
                        if (resp != null) {
                            ((TabHostActivity) TradesPage.this.getActivity()).dismissLoadingDialog();
                        } else {
                            L.e("lonin return error");
                            ((TabHostActivity) TradesPage.this.getActivity()).showSnackMsg(R.string.sync_success);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ((TabHostActivity) TradesPage.this.getActivity()).dismissLoadingDialog();
                Log.e("TAG", error.getMessage(), error);
                ((TabHostActivity) TradesPage.this.getActivity()).showSnackMsg(R.string.login_err_poor_network);
            }
        });
        ((TabHostActivity) TradesPage.this.getActivity()).startRequest(request);

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
                convertView = mInflater.inflate(R.layout.trade_listview, null);

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
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Trade ai = mList.get(groupPosition);
            if (ai.complete_status == 0) { //进行中
                holder.img.setBackgroundResource(R.drawable.shopping_going);
            } else if (ai.complete_status == 1) { //已完成
                holder.img.setBackgroundResource(R.drawable.shopping_finish);
            } else if (ai.complete_status == 2) { //作废
                holder.img.setBackgroundResource(R.drawable.shopping_cancel);
            }
            holder.tid.setText("订单号:" + ai.tid);
            holder.payment.setText("应付金额: ￥"  +
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
                convertView = mInflater.inflate(R.layout.trade_listview_child, null);

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
    }
}
