package com.qianmi.hack.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.qianmi.hack.BaseActivity;
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.R;
import com.qianmi.hack.app.MyVolley;
import com.qianmi.hack.bean.PriceChange;
import com.qianmi.hack.bean.PriceChangeListResult;
import com.qianmi.hack.network.GsonRequest;
import com.qianmi.hack.utils.L;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wv on 2015/8/20.
 */
public class ChangePricePage extends BaseActivity implements View.OnClickListener, AbsListView.OnScrollListener {

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
    private String batchId;

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
    public void onBeginRequest() {

    }

    @Override
    public void onNetworkFailed() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_list);
        mListView = (ListView) findViewById(R.id.mListView);
        loading = (LinearLayout) findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        this.batchId = (String) getIntent().getSerializableExtra("batch");
        initView();
        requestDate(curPage, batchId);
    }

    private void initView() {
        mAdapter = new CustomListAdapter(this, mList);
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

    private void requestDate(int curentPage, String batchId) {
        mRequest = new GsonRequest(Request.Method.GET,
                PcApplication.SERVER_URL + "/batchs/" + batchId + "/modifications/?page=" + curentPage, null, PriceChangeListResult.class,
                new Response.Listener<PriceChangeListResult>() {
                    @Override
                    public void onResponse(PriceChangeListResult resp) {
                        L.d("buildAppData return ");
                        ChangePricePage.this.dismissLoadingDialog();
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
                ChangePricePage.this.handleError(error);
            }
        });
        L.d("**************load date :curentPage=" + curentPage);
        MyVolley.getRequestQueue().add(mRequest);
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
                    requestDate(curPage, batchId);
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
//                        sync.setTag(R.id.priceIcon, true);
                    }
                }
            }
        }
    };

    class Ret {
        public String ret;
        public int id;
    }

    private void changePrice(int id) {
        Ret ret = new Ret();
        ret.id = id;
        GsonRequest request = new GsonRequest(Request.Method.PUT,
                PcApplication.SERVER_URL + "changenotifys/" + id + "/", ret, Ret.class,
                new Response.Listener<Ret>() {
                    @Override
                    public void onResponse(Ret resp) {
                        L.d("change price return ");
                        ChangePricePage.this.dismissLoadingDialog();
                        if (resp != null && resp.ret.equalsIgnoreCase("0")) {
                            Toast.makeText(PcApplication.getInstance(), "同步成功!", Toast.LENGTH_LONG).show();
                        } else {
                            L.e("changePrice return error");
                            Toast.makeText(PcApplication.getInstance(), "修改价格失败!", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ChangePricePage.this.handleError(error);
            }
        });
        L.d("changePrice start");
        MyVolley.getRequestQueue().add(request);
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

        private int getResourceByType(int type) {
            /**
             CHANGE_TYPE = (
             (1, u'上架'),
             (2, u'降价'),
             (3, u'下架'),
             (4, u'涨价'),
             (5, u'新增商品'),
             (6, u'维持')
             )
             */
            int resource = 0;
            switch (type) {
                case 1:
                    resource = R.drawable.ic_product_up_24dp;
                    break;
                case 2:
                    resource = R.drawable.ic_price_reduce_24dp;
                    break;
                case 3:
                    resource = R.drawable.ic_product_down_24dp;
                    break;
                case 4:
                    resource = R.drawable.ic_priceup_24dp;
                    break;
                case 5:
                    resource = R.drawable.ic_new_product_24dp;
                    break;
                case 6:
                    resource = R.drawable.ic_price_maintain_24dp;
                    break;
            }
            return resource;
        }

        private String getOldDescByType(int type, String oldPrice, String newPrice, String source) {
            String desc = "";
            switch (type) {
                case 1:
                    desc = String.format("%s商品上架，新价格为￥%s", source, newPrice);
                    break;
                case 2:
                    desc = String.format("%s从￥%s降至￥%s", source, oldPrice, newPrice);
                    break;
                case 3:
                    desc = String.format("%s商品下架", source);
                    break;
                case 4:
                    desc = String.format("%s从￥%s涨至￥%s", source, oldPrice, newPrice);
                    break;
                case 5:
                    desc = String.format("%s新增商品", source);
                    break;
                case 6:
                    //不应该出现
                    break;
            }
            return desc;
        }

        private String getNewDescByType(int draft_type, String draft_supplier, String draft_price) {
            String desc = "";
            switch (draft_type) {
                case 1:
                    desc = String.format("拟采用%s货源,新价格￥%s", draft_supplier, draft_price);
                    break;
                case 2:
                    desc = String.format("拟采用%s货源,降至￥%s", draft_supplier, draft_price);
                    break;
                case 3:
                    desc = "拟将商品下架";
                    break;
                case 4:
                    desc = String.format("拟采用%s货源,涨至￥%s", draft_supplier, draft_price);
                    break;
                case 5:
                    desc = "新增商品";
                    break;
                case 6:
                    desc = String.format("拟采用%s货源,维持￥%s", draft_supplier, draft_price);
                    break;
            }
            return desc;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (getCount() == 0) {
                return null;
            }
//			System.out.println("position = "+position);
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_price_list_item, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            PriceChange ai = mList.get(position);
            holder.draftAction.setImageResource(getResourceByType(ai.draft_opt_type));
            holder.priceAction.setImageResource(getResourceByType(ai.type));
            holder.name.setText(ai.gonghuo_product_name);
            holder.oldPrice.setText(getOldDescByType(ai.type,
                    String.valueOf(ai.old_price),
                    String.valueOf(ai.new_price),
                    ai.supplier));
//            holder.newPrice.setText("新价格: ￥" + String.valueOf(ai.new_price));
            holder.draftPrice.setText(getNewDescByType(ai.draft_opt_type,
                    ai.draft_price_source,
                    String.valueOf(ai.draft_price)));
            holder.sync.setTag(R.id.sync, ai.id);

//            Object b = holder.sync.getTag(R.id.priceIcon);
//            if (b != null && b instanceof Boolean) {
//                Boolean isSync = (Boolean) b;
//                if (isSync) {
//                    holder.sync.setChecked(true);
//                    holder.sync.setEnabled(false);
//                }
//            }

            if (ai.is_sync) {
                holder.sync.setText(R.string.sync_alredy);
                holder.sync.setChecked(true);
                holder.sync.setEnabled(false);

            } else {
                holder.sync.setText(R.string.sync);
                holder.sync.setChecked(false);
                holder.sync.setEnabled(true);
                holder.sync.setOnCheckedChangeListener(mListener);
            }

            return convertView;
        }
    }

    private static class ViewHolder {
        private ImageView priceAction;
        private TextView name;
        private TextView oldPrice;
        private TextView draftPrice;
        private ImageView draftAction;
        private CheckBox sync;

        public ViewHolder(View convertView) {
            priceAction = (ImageView) convertView
                    .findViewById(R.id.price_action);
            name = (TextView) convertView
                    .findViewById(R.id.name);
            oldPrice = (TextView) convertView
                    .findViewById(R.id.old_price);
            draftPrice = (TextView) convertView
                    .findViewById(R.id.draft_price);
            sync = (CheckBox) convertView
                    .findViewById(R.id.sync);
            draftAction = (ImageView) convertView.findViewById(R.id.draft_action);

        }
    }
}

