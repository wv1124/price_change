package com.qianmi.hack.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.qianmi.hack.BaseActivity;
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.R;
import com.qianmi.hack.app.MyVolley;
import com.qianmi.hack.bean.Batch;
import com.qianmi.hack.bean.BatchListResult;
import com.qianmi.hack.network.GsonRequest;
import com.qianmi.hack.utils.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by caozupeng on 15/8/31.
 * 显示价格更新的批次信息
 */
public class BatchPage extends Fragment implements View.OnClickListener, AbsListView.OnScrollListener {
    //设置一个事件的ID
    private static final int LOAD_DATA_FINISH = 10;

    //给自己一个名字，日志中可以显示
    private static final String TAG = "BatchPageActivity";

    //驻留在内存中的Model对象列表，用于缓存
    private List<Batch> modelList = new ArrayList<>();
    //自定义的展现Adapter，因为Item是特别定制的
    private CustomListAdapter mAdapter;
    //list view的实例
    private SwipeMenuListView mListView;
    //当前page页数，默认从1开始
    private int currentPage = 1;
    //布局管理器
    private LayoutInflater mInflater;
    //加载的时候显示的图标，这里获取句柄用于控制图标的显示
    private LinearLayout loading;
    //标识是否还有下一页
    private boolean hasNext = true;

    private int visibleLastIndex = 0;   //最后的可视项索引
    private int visibleItemCount;       // 当前窗口可见项总数

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_DATA_FINISH:
                    mAdapter.mList = modelList;
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }

        ;
    };

    /**
     * 用于转到dp到像素px
     * @param dp
     * @return
     */
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_batch_list_view, null);
        loading = (LinearLayout) view.findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        mListView = (SwipeMenuListView) view.findViewById(R.id.mListView);

        initListView();
        requestDate(currentPage);
        return view;
    }


    /**
     * 用于初始化该页面中最重要的组件ListView，
     * 设置其数据Adapter
     * 设置滚动监听
     * 设置每个Item左滑动的菜单
     * 设置点击Item的事件触发
     */
    private void initListView() {
        //创建Adapter
        mAdapter = new CustomListAdapter(getActivity(), modelList);
        //设置ListView的控制源为Adapter
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);     //添加滑动监听
        //预先加载一次，看看是否合适？
        mAdapter.notifyDataSetChanged();
        //设置点击ListItem后转到该批次下面所有的变更一览页面
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.e(TAG, "click position:" + position);
                Batch batch = (Batch) mAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), ChangePricePage.class);
                intent.putExtra("batch", batch);
                startActivity(intent);

            }
        });

        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getActivity());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle("全部同步");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem allSyncMenuItem = new SwipeMenuItem(
                        getActivity());
                // set item background
                allSyncMenuItem.setBackground(R.color.lightgray);
                // set item width
                allSyncMenuItem.setWidth(dp2px(90));
                // set a icon
                allSyncMenuItem.setIcon(R.drawable.ic_uncheck_48dp);
                // add to menu
                menu.addMenuItem(allSyncMenuItem);
            }
        };
        // set creator
        mListView.setMenuCreator(creator);

        // step 2. listener item click event
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                Batch item = modelList.get(position);
                Log.d(TAG, "batch id is " + item.id);
                ((BaseActivity)getActivity()).showLoadingDialog();
                batchUpdateChange(item.id, position);
                return true;
            }
        });
    }


    private void batchUpdateChange(int batchId, int position) {
        Map<String, Integer> batchInfo = new HashMap<>();
        batchInfo.put("id", batchId);
        GsonRequest.Builder<Map> builder = new GsonRequest.Builder<>();
        GsonRequest request = builder
                .retClazz(Map.class)
                .method(Request.Method.PUT)
                .setUrl(PcApplication.SERVER_URL + "batchs/" + batchId + "/")
                .setRequest(batchInfo)
                .registerResListener(new Response.Listener<Map<String, Double>>() {
                    @Override
                    public void onResponse(Map<String, Double> response) {
                        ((BaseActivity) getActivity()).dismissLoadingDialog();
                        int success = response.get("success_count").intValue();
                        int fail = response.get("fail_count").intValue();
                        int skip = response.get("skip_count").intValue();
                        Toast.makeText(PcApplication.getInstance(),
                                String.format("同步操作成功%s条,失败%s条,略过%s条", success, fail, skip),
                                Toast.LENGTH_LONG).show();
                        mAdapter.notifyDataSetChanged();

                    }
                })
                .create();
        MyVolley.getRequestQueue().add(request);
    }


    private Response.Listener createSuccessListener() {
        return new Response.Listener<BatchListResult>() {
            @Override
            public void onResponse(BatchListResult resp) {
                L.d("batch result return ");
                ((BaseActivity) getActivity()).dismissLoadingDialog();
                if (resp != null) {
                    L.d(resp.toString());
                    //mList.clear();
                    List<Batch> listResult = resp.results;
                    modelList.addAll(listResult);
                    hasNext = resp.next != null;
                    //将结果通知到监听器中，修改界面显示结果
                    Message _Msg = mHandler.obtainMessage(LOAD_DATA_FINISH, modelList);
                    mHandler.sendMessage(_Msg);
                    if (loading != null) {
                        loading.setVisibility(View.GONE);
                    }
                } else {
                    L.e("return error");
                }
            }
        };
    }

    /**
     * 用于发出Http请求
     *
     * @param currentPage
     */
    private void requestDate(int currentPage) {
        GsonRequest.Builder<BatchListResult> builder = new GsonRequest.Builder<>();
        GsonRequest request = builder.retClazz(BatchListResult.class)
                .setUrl(String.format("%s/batchs/?page=%d", PcApplication.SERVER_URL, currentPage))
                .method(Request.Method.GET)
                .registerResListener(createSuccessListener())
                .registerErrorListener(((BaseActivity) getActivity()).createErrorListener())
                .create();
        L.d("**************load date :currentPage=" + currentPage);
        MyVolley.getRequestQueue().add(request);
    }

    @Override
    public void onClick(View view) {

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
                    Log.i("LOAD MORE", "loading... page: " + hasNext);
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

    private class CustomListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        public List<Batch> mList;
        private int[] resouces = new int[]{R.drawable.ic_poll_black_48dp,
                R.drawable.ic_public_black_48dp, R.drawable.ic_whatshot_black_48dp};

        public CustomListAdapter(Context pContext, List<Batch> pList) {
            mInflater = LayoutInflater.from(pContext);
            if (pList != null) {
                mList = pList;
            } else {
                mList = new ArrayList<Batch>();
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
                convertView = mInflater.inflate(R.layout.fragment_batch_list_item, null);

                holder = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.batchimg);
                holder.batchTime = (TextView) convertView.findViewById(R.id.batch_update_time);
                holder.productDowns = (TextView) convertView.findViewById(R.id.product_downs);
                holder.productModifications = (TextView) convertView.findViewById(R.id.product_modifications);
                holder.productUps = (TextView) convertView.findViewById(R.id.product_ups);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Batch batch = mList.get(position);
            //holder.mImage.setImageUrl(batch.getAppIcon());
            holder.image.setImageResource(resouces[position % 3]);
            holder.batchTime.setText(batch.created);
            holder.productDowns.setText("下架:" + String.valueOf(batch.down_rows));
            holder.productUps.setText("上架:" + String.valueOf(batch.add_rows));
            holder.productModifications.setText(("变更:" + String.valueOf(batch.update_rows)));
            return convertView;
        }
    }

    private static class ViewHolder {
        private ImageView image;
        private TextView batchTime;
        private TextView productModifications;
        private TextView productUps;
        private TextView productDowns;
    }
}
