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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.R;
import com.qianmi.hack.bean.Product;
import com.qianmi.hack.bean.ProductListResult;
import com.qianmi.hack.network.GsonRequest;
import com.qianmi.hack.utils.L;
import com.qianmi.hack.widget.CustomListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wv on 2015/8/20.
 */
public class FragmentPage extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final int LOAD_DATA_FINISH = 10;
    private static final int REFRESH_DATA_FINISH = 11;

    private List<Product> mList = new ArrayList<Product>();
    private CustomListAdapter mAdapter;
    private CustomListView mListView;
    private int curPage = 1;
    private boolean hasNext = true;

    private Button mCanPullRefBtn, mCanLoadMoreBtn, mCanAutoLoadMoreBtn, mIsMoveToFirstItemBtn;

    @SuppressWarnings("unchecked")
    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_DATA_FINISH:
                    if (mAdapter != null) {
                        mAdapter.mList = (ArrayList<Product>) msg.obj;
                        mAdapter.notifyDataSetChanged();
                    }
                    mListView.onRefreshComplete();    //下拉刷新完成
                    break;
                case LOAD_DATA_FINISH:
                    if (mAdapter != null) {
                        mAdapter.mList.addAll((ArrayList<Product>) msg.obj);
                        mAdapter.notifyDataSetChanged();
                    }
                    mListView.onRefreshComplete();
                    //mListView.onLoadMoreComplete();    //加载更多完成
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
        View view = inflater.inflate(R.layout.activity_productlist, null);

        mListView = (CustomListView) view.findViewById(R.id.mListView);
        /*mCanPullRefBtn = (Button) view.findViewById(R.id.canPullRefBtn);
        mCanLoadMoreBtn = (Button) view.findViewById(R.id.canLoadMoreFlagBtn);
        mCanAutoLoadMoreBtn = (Button) view.findViewById(R.id.autoLoadMoreFlagBtn);
        mIsMoveToFirstItemBtn = (Button) view.findViewById(R.id.isMoveToFirstItemBtn);*/
        /*mCanPullRefBtn.setOnClickListener(this);
        mCanLoadMoreBtn.setOnClickListener(this);
        mCanAutoLoadMoreBtn.setOnClickListener(this);
        mIsMoveToFirstItemBtn.setOnClickListener(this);*/
        initView();
        requestDate(curPage);
        return view;
    }

    private void initView() {
        mAdapter = new CustomListAdapter(FragmentPage.this.getActivity(), mList);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mListView.setOnRefreshListener(new CustomListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO 下拉刷新
                Log.e(TAG, "-----------------------onRefresh");
                //loadData(0);
            }
        });

        mListView.setOnLoadListener(new CustomListView.OnLoadMoreListener() {

            @Override
            public void onLoadMore() {
                // TODO 加载更多
                Log.e(TAG, "-----------------------onLoad");
                //loadData(1);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // 此处传回来的position和mAdapter.getItemId()获取的一致;
                Log.e(TAG, "click position:" + position);
//				Log.e(TAG, "__ mAdapter.getItemId() = "+mAdapter.getItemId(position));
            }
        });
        mListView.setCanLoadMore(true);
        mListView.setMoveToFirstItemAfterRefresh(true);
        mListView.setAutoLoadMore(true);

    }

    @Override
    public void onClick(View pV) {
        switch (pV.getId()) {
            /*case R.id.canPullRefBtn:
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
                break;*/
        }
    }

    public void loadData(final int type) {
        new Thread() {
            @Override
            public void run() {
                List<Product> _List = null;
                switch (type) {
                    case 0:     // TODO 下拉刷新
                        //_List = new ArrayList<Product>();
                        //requestDate(++curPage);
                        break;

                    case 1:         // TODO 加载更多
                        //_List = new ArrayList<Product>();
                        if (hasNext) {
                            requestDate(++curPage);
                        } else {
                            ((TabHostActivity) FragmentPage.this.getActivity()).showSnackMsg("No more data!");
                            //Toast.makeText(FragmentPage.this.getActivity(), "No more data!", Toast.LENGTH_LONG).show();
                        }
                        break;
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void requestDate(int curentPage) {
        GsonRequest mRequest = new GsonRequest(Request.Method.GET,
                PcApplication.SERVER_URL + "/supproducts/?page=" + curentPage, null, ProductListResult.class,
                new Response.Listener<ProductListResult>() {
                    @Override
                    public void onResponse(ProductListResult resp) {
                        L.d("buildAppData return ");
                        ((TabHostActivity) FragmentPage.this.getActivity()).dismissLoadingDialog();
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
                ((TabHostActivity) FragmentPage.this.getActivity()).dismissLoadingDialog();
                Log.e("TAG", error.getMessage(), error);
                ((TabHostActivity) FragmentPage.this.getActivity()).showSnackMsg(FragmentPage.this.getActivity().getString(R.string.login_err));
            }
        });
        L.d("**************load date :curentPage=" + curentPage);
        ((TabHostActivity) FragmentPage.this.getActivity()).startRequest(mRequest);
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
//			System.out.println("position = "+position);
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.product_listview, null);

                holder = new ViewHolder();
                holder.mImage = (ImageView) convertView
                        .findViewById(R.id.ivIcon);
                holder.mName = (TextView) convertView
                        .findViewById(R.id.tvName);
                holder.mVer = (TextView) convertView.findViewById(R.id.tvVer);
                holder.mSize = (TextView) convertView
                        .findViewById(R.id.tvSize);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Product ai = mList.get(position);
            Glide.with(FragmentPage.this.getActivity())
                    .load(ai.pic_url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.order_detail_proof_preload)
                    .into(holder.mImage);
            //holder.mImage.setImageUrl(ai.getAppIcon());
            try {
                String name = new String(ai.product_name.getBytes(), "UTF-8");
                holder.mName.setText(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.mVer.setText(String.valueOf(ai.sale_price));
            holder.mSize.setText(String.valueOf(ai.cost_price));

            return convertView;
        }
    }

    private static class ViewHolder {
        private ImageView mImage;
        private TextView mName;
        private TextView mVer;
        private TextView mSize;
    }
}
