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
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qianmi.hack.BaseActivity;
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.R;
import com.qianmi.hack.app.MyVolley;
import com.qianmi.hack.bean.Batch;
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

    private static final String TAG = "ChangePricePage";

    private List<PriceChange> mList = new ArrayList<PriceChange>();
    private CustomListAdapter mAdapter;
    private ListView mListView;
    private int curPage = 1;
    private boolean hasNext = true;

    private int visibleLastIndex = 0;   //最后的可视项索引
    private int visibleItemCount;       // 当前窗口可见项总数
    private LinearLayout loading;
    private String batchId;

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
        Batch batch = (Batch) getIntent().getSerializableExtra("batch");
        this.batchId = String.valueOf(batch.id);
        initView();
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(batch.created + "变更");
        setNeedBackGesture(true);
        requestData(curPage, batchId);
    }

    private void initView() {
        mAdapter = new CustomListAdapter(this, mList);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);     //添加滑动监听
    }

    @Override
    public void onClick(View pV) {
        switch (pV.getId()) {
        }
    }

    private Response.Listener createSuccessListener() {
        return new Response.Listener<PriceChangeListResult>() {
            @Override
            public void onResponse(PriceChangeListResult resp) {
                L.d("buildAppData return ");
                ChangePricePage.this.dismissLoadingDialog();
                if (resp != null) {
                    L.d(resp.toString());
                    mList.addAll(resp.results);
                    hasNext = resp.next != null;
                    mAdapter.mList = mList;
                    mAdapter.notifyDataSetChanged();
                    if (loading != null) {
                        loading.setVisibility(View.GONE);
                    }

                } else {
                    L.e("requestData return error");
                }
            }
        };
    }

    private void requestData(int currentPage, String batchId) {
        GsonRequest.Builder<PriceChangeListResult> builder = new GsonRequest.Builder<>();
        GsonRequest request = builder
                .retClazz(PriceChangeListResult.class)
                .method(Request.Method.GET)
                .setUrl(String.format("%s/batchs/%s/modifications/?page=%d", PcApplication.SERVER_URL, batchId, currentPage))
                .registerResListener(createSuccessListener())
                .registerErrorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ChangePricePage.this.handleError(error);
                    }
                })
                .create();
        L.d("**************load date :currentPage=" + currentPage);
        MyVolley.getRequestQueue().add(request);
    }

    /**
     * 正在滚动时回调，回调2-3次，手指没抛则回调2次。scrollState = 2的这次不回调
     * 回调顺序如下
     * 第1次：scrollState = SCROLL_STATE_TOUCH_SCROLL(1) 正在滚动
     * 第2次：scrollState = SCROLL_STATE_FLING(2) 手指做了抛的动作（手指离开屏幕前，用力滑了一下）
     * 第3次：scrollState = SCROLL_STATE_IDLE(0) 停止滚动
     *
     * @param view
     * @param scrollState
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int lastIndex = mAdapter.getCount() - 1;             //加上底部的loadMoreView项
        //在滑动停止下来的时候开始计算是否到了底部
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (visibleLastIndex == lastIndex) {
                //如果是自动加载,可以在这里放置异步加载数据的代码
                ++curPage;
                if (hasNext) {
                    Log.i(TAG, "loading... page: ");
                    loading.setVisibility(View.VISIBLE);
                    requestData(curPage, batchId);
                }
            }
        }
    }

    /**
     * 滚动时一直回调，直到停止滚动时才停止回调。单击时回调一次。
     *
     * @param view             ListView对象引用
     * @param firstVisibleItem 当前能看见的第一个列表项ID（从0开始
     * @param visibleItemCount 当前能看见的列表项个数（小半个也算）
     * @param totalItemCount   列表项总数
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.visibleItemCount = visibleItemCount;
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }

    class Ret {
        public String ret;
        public int id;
    }

    private void changePrice(int id, final int position) {
        Ret ret = new Ret();
        ret.id = id;
        GsonRequest.Builder<Ret> builder = new GsonRequest.Builder<>();
        GsonRequest request = builder
                .retClazz(Ret.class)
                .method(Request.Method.PUT)
                .setUrl(PcApplication.SERVER_URL + "changenotifys/" + id + "/")
                .setRequest(ret)
                .registerResListener(new Response.Listener<Ret>() {
                    @Override
                    public void onResponse(Ret resp) {
                        L.d("change price return ");
                        ChangePricePage.this.dismissLoadingDialog();
                        if (resp != null && resp.ret.equalsIgnoreCase("0")) {
                            PriceChange priceChange = mList.get(position);
                            priceChange.is_sync = true;
                            mAdapter.notifyDataSetChanged();
                            Toast.makeText(PcApplication.getInstance(), "同步操作成功!", Toast.LENGTH_LONG).show();
                        } else {
                            L.e("changePrice return error");
                            Toast.makeText(PcApplication.getInstance(), "修改价格失败!", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .registerErrorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ChangePricePage.this.handleError(error);
                    }
                })
                .create();

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
                    desc = String.format("%s上架新价格为￥%s", source, newPrice);
                    break;
                case 2:
                    desc = String.format("%s从￥%s降至￥%s", source, oldPrice, newPrice);
                    break;
                case 3:
                    desc = String.format("%s下架", source);
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
                    desc = String.format("拟采用%s货源新价格￥%s", draft_supplier, draft_price);
                    break;
                case 2:
                    desc = String.format("拟采用%s货源降至￥%s", draft_supplier, draft_price);
                    break;
                case 3:
                    desc = "拟将商品下架";
                    break;
                case 4:
                    desc = String.format("拟采用%s货源涨至￥%s", draft_supplier, draft_price);
                    break;
                case 5:
                    desc = "新增商品";
                    break;
                case 6:
                    desc = String.format("拟采用%s货源维持￥%s", draft_supplier, draft_price);
                    break;
            }
            return desc;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (getCount() == 0) {
                return null;
            }
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
            Glide.with(convertView.getContext())
                    .load(ai.pic_url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.order_detail_proof_preload)
                    .into(holder.productImg);
            holder.draftPrice.setText(getNewDescByType(ai.draft_opt_type,
                    ai.draft_price_source,
                    String.valueOf(ai.draft_price)));
            holder.isChecked.setTag(R.id.sync, ai.id);
            holder.pressArea.setTag(R.id.sync, ai.id);


            if (ai.is_sync) {
                holder.sync.setText(R.string.sync_alredy);
                holder.isChecked.setImageResource(R.drawable.ic_checked_48dp);

            } else {
                holder.sync.setText(R.string.sync);
                holder.isChecked.setImageResource(R.drawable.ic_uncheck_48dp);
                holder.pressArea.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //position被类似闭包包住了，状态被保留住了，所以必须用new client事件对象处理
                        PriceChange priceChange = mList.get(position);
                        Log.d(TAG, "checked is onclick, id=" + priceChange.id + " position=" + position);
                        if (!priceChange.is_sync) {
                            ChangePricePage.this.showLoadingDialog();
                            changePrice(priceChange.id, position);
                        }
                    }
                });
            }

            return convertView;
        }
    }

    private static class ViewHolder {
        private ImageView productImg;
        private ImageView priceAction;
        private TextView name;
        private TextView oldPrice;
        private TextView draftPrice;
        private ImageView draftAction;
        private TextView sync;
        private ImageView isChecked;
        private View pressArea;

        public ViewHolder(View convertView) {
            priceAction = (ImageView) convertView
                    .findViewById(R.id.price_action);
            name = (TextView) convertView
                    .findViewById(R.id.name);
            oldPrice = (TextView) convertView
                    .findViewById(R.id.old_price);
            draftPrice = (TextView) convertView
                    .findViewById(R.id.draft_price);
            sync = (TextView) convertView
                    .findViewById(R.id.sync);
            draftAction = (ImageView) convertView.findViewById(R.id.draft_action);
            productImg = (ImageView) convertView.findViewById(R.id.product_img);
            isChecked = (ImageView) convertView.findViewById(R.id.is_checked);
            pressArea = (View) convertView.findViewById(R.id.press_area);

        }
    }
}

