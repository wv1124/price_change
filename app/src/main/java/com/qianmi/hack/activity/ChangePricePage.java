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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.R;
import com.qianmi.hack.bean.PriceChange;
import com.qianmi.hack.bean.PriceChange;
import com.qianmi.hack.bean.PriceChangeListResult;
import com.qianmi.hack.bean.ProductListResult;
import com.qianmi.hack.network.GsonRequest;
import com.qianmi.hack.utils.L;
import com.qianmi.hack.widget.CustomListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wv on 2015/8/20.
 */
public class ChangePricePage extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final int LOAD_DATA_FINISH = 10;
    private static final int REFRESH_DATA_FINISH = 11;
    private static final int LOADING_DATA = 12;

    private List<PriceChange> mList = new ArrayList<PriceChange>();
    private CustomListAdapter mAdapter;
    private CustomListView mListView;
    private int curPage = 1;
    private boolean hasNext = true;

    private Button mCanPullRefBtn, mCanLoadMoreBtn, mCanAutoLoadMoreBtn, mIsMoveToFirstItemBtn;

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_DATA_FINISH:
                    if (mAdapter != null) {
                        mAdapter.mList = (ArrayList<PriceChange>) msg.obj;
                        mAdapter.notifyDataSetChanged();
                    }
                    mListView.onRefreshComplete();    //下拉刷新完成
                    break;
                case LOAD_DATA_FINISH:
                    if (mAdapter != null) {
                        mAdapter.mList.addAll((ArrayList<PriceChange>) msg.obj);
                        mAdapter.notifyDataSetChanged();
                    }
                    mListView.onRefreshComplete();
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
        View view = inflater.inflate(R.layout.activity_pricelist, null);

        mListView = (CustomListView) view.findViewById(R.id.mListView);
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
        mAdapter = new CustomListAdapter(ChangePricePage.this.getActivity(), mList);
        mListView.setAdapter(mAdapter);
        //mAdapter.notifyDataSetChanged();
        /*mListView.setOnRefreshListener(new CustomListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO 下拉刷新
                Log.e(TAG, "-----------------------onRefresh");
                //loadData(0);
            }
        });*/

        mListView.setOnLoadListener(new CustomListView.OnLoadMoreListener() {

            @Override
            public void onLoadMore() {
                // TODO 加载更多
                Log.e(TAG, "-----------------------onLoad");
                loadData(1);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.e(TAG, "click position:" + position);
            }
        });
        //mListView.setCanLoadMore(true);
        //mListView.setMoveToFirstItemAfterRefresh(true);
        mListView.setAutoLoadMore(true);
    }

    @Override
    public void onClick(View pV) {
        switch (pV.getId()) {
            case R.id.canPullRefBtn:
                mListView.setCanRefresh(!mListView.isCanRefresh());
                if (mCanPullRefBtn.getText().toString().
                        equals("关闭下拉刷新")) {
                    mCanPullRefBtn.setText("启用下拉刷新");
                } else {
                    mCanPullRefBtn.setText("关闭下拉刷新");
                }
                break;
            case R.id.canLoadMoreFlagBtn:
                mListView.setCanLoadMore(!mListView.isCanLoadMore());
                if (mCanLoadMoreBtn.getText().toString().
                        equals("关闭加载更多")) {
                    mCanLoadMoreBtn.setText("启用加载更多");
                } else {
                    mCanLoadMoreBtn.setText("关闭加载更多");
                }
                break;
            case R.id.autoLoadMoreFlagBtn:
                mListView.setAutoLoadMore(!mListView.isAutoLoadMore());
                if (mCanAutoLoadMoreBtn.getText().toString().
                        equals("关闭自动加载更多")) {
                    mCanAutoLoadMoreBtn.setText("启用自动加载更多");
                } else {
                    mCanAutoLoadMoreBtn.setText("关闭自动加载更多");
                }
                break;
            case R.id.isMoveToFirstItemBtn:
                mListView.setMoveToFirstItemAfterRefresh(
                        !mListView.isMoveToFirstItemAfterRefresh());
                if (mIsMoveToFirstItemBtn.getText().toString().
                        equals("关闭移动到第一条Item")) {
                    mIsMoveToFirstItemBtn.setText("启用移动到第一条Item");
                } else {
                    mIsMoveToFirstItemBtn.setText("关闭移动到第一条Item");
                }
                break;
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
                    ((TabHostActivity) ChangePricePage.this.getActivity()).showSnackMsg("No more data!");
                }
                break;
        }

    }

    private void requestDate(int curentPage) {
        GsonRequest mRequest = new GsonRequest(Request.Method.GET,
                PcApplication.SERVER_URL + "/changenotifys/?page=" + curentPage, null, PriceChangeListResult.class,
                new Response.Listener<PriceChangeListResult>() {
                    @Override
                    public void onResponse(PriceChangeListResult resp) {
                        L.d("buildAppData return ");
                        ((TabHostActivity) ChangePricePage.this.getActivity()).dismissLoadingDialog();
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
                ((TabHostActivity) ChangePricePage.this.getActivity()).dismissLoadingDialog();
                Log.e("TAG", error.getMessage(), error);
                ((TabHostActivity) ChangePricePage.this.getActivity()).showSnackMsg(R.string.login_err_poor_network);
            }
        });
        L.d("**************load date :curentPage=" + curentPage);
        ((TabHostActivity) ChangePricePage.this.getActivity()).startRequest(mRequest);
    }

    private class CustomListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        public List<PriceChange> mList;

        public CustomListAdapter(Context pContext, List<PriceChange> pList) {
            mInflater = LayoutInflater.from(pContext);
            if (pList != null) {
                mList = pList;
            } else {
                mList = new ArrayList<PriceChange>();
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
//			System.out.println("position = "+position);
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.pricechange_listview, null);

                holder = new ViewHolder();
                holder.priceIcon = (ImageView) convertView
                        .findViewById(R.id.priceIcon);
                holder.name = (TextView) convertView
                        .findViewById(R.id.name);
                holder.supplier = (TextView) convertView.findViewById(R.id.supplier);
                holder.oldPrice = (TextView) convertView
                        .findViewById(R.id.old_price);
                holder.newPrice = (TextView) convertView
                        .findViewById(R.id.new_price);
                holder.draftPrice = (TextView) convertView
                        .findViewById(R.id.draft_price);
                holder.sync = (CheckBox) convertView
                        .findViewById(R.id.sync);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            PriceChange ai = mList.get(position);
            /**
             * CHANGE_TYPE = (
             (1, u’上架’),
             (2, u’降价’),
             (3, u’下架’),
             (4, u’涨价’),
             (5, u’新增商品’),
             )
             */
            switch (ai.type) {
                case 1:
                    holder.priceIcon.setImageResource(R.drawable.upl);
                    break;
                case 2:
                    holder.priceIcon.setImageResource(R.drawable.down);
                    break;
                case 3:
                    holder.priceIcon.setImageResource(R.drawable.downl);
                    break;
                case 4:
                    holder.priceIcon.setImageResource(R.drawable.up);
                    break;
                case 5:
                    holder.priceIcon.setImageResource(R.drawable.add);
                    break;
            }

            holder.name.setText(ai.gonghuo_product_name);
            holder.supplier.setText(ai.supplier);
            holder.oldPrice.setText("原价:" + String.valueOf(ai.old_price));
            holder.newPrice.setText("新价:" + String.valueOf(ai.new_price));
            holder.draftPrice.setText("拟设价:" + String.valueOf(ai.draft_price));
            if (ai.is_sync) {
                holder.sync.setText(R.string.sync_alredy);
                holder.sync.setChecked(true);
                holder.sync.setEnabled(false);

            } else {
                holder.sync.setText(R.string.sync);
                holder.sync.setChecked(false);
                holder.sync.setEnabled(true);
            }

            return convertView;
        }
    }

    private static class ViewHolder {
        private ImageView priceIcon;
        private TextView name;
        private TextView supplier;
        private TextView oldPrice;
        private TextView newPrice;
        private TextView draftPrice;
        private CheckBox sync;
    }
}

