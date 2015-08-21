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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.R;
import com.qianmi.hack.bean.PriceChange;
import com.qianmi.hack.bean.PriceChangeListResult;
import com.qianmi.hack.network.GsonRequest;
import com.qianmi.hack.utils.L;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wv on 2015/8/20.
 */
public class ChangePricePage extends Fragment implements View.OnClickListener, AbsListView.OnScrollListener {

    private static final String TAG = "MainActivity";

    private static final int LOAD_DATA_FINISH = 10;

    private List<PriceChange> mList = new ArrayList<PriceChange>();
    private CustomListAdapter mAdapter;
    private ListView mListView;
    private int curPage = 1;
    private boolean hasNext = true;
    private GsonRequest mRequest;

    private int visibleLastIndex = 0;   //最后的可视项索引
    private int visibleItemCount;       // 当前窗口可见项总数
    private LinearLayout loading;

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_DATA_FINISH:
                    mAdapter.mList = mList;
                    mAdapter.notifyDataSetChanged();
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
        mListView = (ListView) view.findViewById(R.id.mListView);
        loading = (LinearLayout) view.findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        initView();
        requestDate(curPage);
        return view;
    }

    private void initView() {
        mAdapter = new CustomListAdapter(ChangePricePage.this.getActivity(), mList);
        mAdapter.setOnCheckedChangeListener(priceLister);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);     //添加滑动监听
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

    private void requestDate(int curentPage) {
        mRequest = new GsonRequest(Request.Method.GET,
                PcApplication.SERVER_URL + "/changenotifys/?page=" + curentPage, null, PriceChangeListResult.class,
                new Response.Listener<PriceChangeListResult>() {
                    @Override
                    public void onResponse(PriceChangeListResult resp) {
                        L.d("buildAppData return ");
                        ((TabHostActivity) ChangePricePage.this.getActivity()).dismissLoadingDialog();
                        if (resp != null) {
                            L.d(resp.toString());
                            //mList.clear();
                            mList.addAll(resp.results);
                            if (resp.next == null || resp.next.length() == 0 || resp.next == "null") {
                                hasNext = false;
                            } else {
                                hasNext = true;
                            }
                            Message _Msg = mHandler.obtainMessage(LOAD_DATA_FINISH, mList);
                            mHandler.sendMessage(_Msg);
                            if (loading != null) {
                                loading.setVisibility(View.GONE);
                            }

                        } else {
                            L.e("requestDate return error");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ((TabHostActivity) ChangePricePage.this.getActivity()).handleError(error, mRequest);
            }
        });
        L.d("**************load date :curentPage=" + curentPage);
        ((TabHostActivity) ChangePricePage.this.getActivity()).startRequest(mRequest);
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

    private CompoundButton.OnCheckedChangeListener priceLister = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked && buttonView != null) {
                Object changeId = (Object) buttonView.getTag(R.id.sync);
                L.d("change id :" + changeId);
                if (changeId != null && changeId instanceof Integer) {
                    changePrice((Integer) changeId);
                    CheckBox sync = ((CheckBox) buttonView);
                    if (sync != null) {
                        sync.setText("已同步");
                        sync.setEnabled(false);
                        sync.setTag(R.id.priceIcon, true);
                    }
                }
            }
        }
    };

    private void changePrice(int id) {
        GsonRequest request = new GsonRequest(Request.Method.GET,
                PcApplication.SERVER_URL + "changenotifys/" + id, null, PriceChangeListResult.class,
                new Response.Listener<PriceChangeListResult>() {
                    @Override
                    public void onResponse(PriceChangeListResult resp) {
                        L.d("change price return ");
                        ((TabHostActivity) ChangePricePage.this.getActivity()).dismissLoadingDialog();
                        if (resp != null) {
                            Toast.makeText(PcApplication.getInstance(), "同步成功!", Toast.LENGTH_LONG);
                        } else {
                            L.e("changePrice return error");
                            Toast.makeText(PcApplication.getInstance(), "修改价格失败!", Toast.LENGTH_LONG);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ((TabHostActivity) ChangePricePage.this.getActivity()).handleError(error, null);
            }
        });
        L.d("changePrice start");
        ((TabHostActivity) ChangePricePage.this.getActivity()).startRequest(request);
    }

    private class CustomListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        public List<PriceChange> mList;
        CompoundButton.OnCheckedChangeListener mListener;

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

        public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
            mListener = listener;
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
            holder.oldPrice.setText("原价: ￥" + String.valueOf(ai.old_price));
            holder.newPrice.setText("新价: ￥" + String.valueOf(ai.new_price));
            holder.draftPrice.setText("拟设价: ￥" + String.valueOf(ai.draft_price));
            if (ai.is_sync) {
                holder.sync.setText(R.string.sync_alredy);
                holder.sync.setChecked(true);
                holder.sync.setEnabled(false);

            } else {
                holder.sync.setText(R.string.sync);
                holder.sync.setChecked(false);
                holder.sync.setEnabled(true);
            }
            holder.sync.setTag(R.id.sync, ai.id);
            holder.sync.setOnCheckedChangeListener(mListener);
            Object b = holder.sync.getTag(R.id.priceIcon);
            if (b != null && b instanceof Boolean) {
                Boolean isSync = (Boolean) b;
                if (isSync) {
                    holder.sync.setChecked(true);
                    holder.sync.setEnabled(false);
                }
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

