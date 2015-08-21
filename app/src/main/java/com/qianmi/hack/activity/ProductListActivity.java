/**
 * 
 * @package com.john.test
 * @type MainActivity
 * @date 2013-11-20 下午1:26:56
 * @author JohnWatson
 * @version 1.0
 */
package com.qianmi.hack.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.qianmi.hack.BaseActivity;
import com.qianmi.hack.PcApplication;
import com.qianmi.hack.R;
import com.qianmi.hack.bean.AppInfo;
import com.qianmi.hack.bean.ProductListResult;
import com.qianmi.hack.bean.Token;
import com.qianmi.hack.network.GsonRequest;
import com.qianmi.hack.utils.L;
import com.qianmi.hack.widget.CustomListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 如果有任何改进意见，请发送到本人邮箱，小弟感激涕零！
 * @date 2013-11-20 下午1:26:56
 * @change JohnWatson
 * @mail xxzhaofeng5412@gmail.com
 * @version 1.0
 */
public class ProductListActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "MainActivity";

	private static final int LOAD_DATA_FINISH = 10;
	private static final int REFRESH_DATA_FINISH = 11;

	private List<AppInfo> mList = new ArrayList<AppInfo>();
	private CustomListAdapter mAdapter;
	private CustomListView mListView;
	private int mCount = 10;

	private Button mCanPullRefBtn, mCanLoadMoreBtn, mCanAutoLoadMoreBtn, mIsMoveToFirstItemBtn;
	
	@SuppressWarnings("unchecked")
	private Handler mHandler = new Handler(){
		
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_DATA_FINISH:
				if(mAdapter!=null){
					mAdapter.mList = (ArrayList<AppInfo>)msg.obj;
					mAdapter.notifyDataSetChanged();
				}
				mListView.onRefreshComplete();	//下拉刷新完成
				break;
			case LOAD_DATA_FINISH:
				if(mAdapter!=null){
					mAdapter.mList.addAll((ArrayList<AppInfo>)msg.obj);
					mAdapter.notifyDataSetChanged();
				}
				mListView.onLoadMoreComplete();	//加载更多完成
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onBeginRequest() {

	}

	@Override
	public void onNetworkFailed() {

	}

	@Override
	public boolean needInitRequestQueue() {
		return true;
	}

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);
		setContentView(R.layout.activity_productlist);

		buildAppData();
		initView();
	}
	
	private void initView(){
		mAdapter = new CustomListAdapter(this, mList);
		mListView = (CustomListView) findViewById(R.id.mListView);
		mListView.setAdapter(mAdapter);

		mListView.setOnRefreshListener(new CustomListView.OnRefreshListener() {

			@Override
			public void onRefresh() {
				// TODO 下拉刷新
				Log.e(TAG, "onRefresh");
				loadData(0);
			}
		});

		mListView.setOnLoadListener(new CustomListView.OnLoadMoreListener() {

			@Override
			public void onLoadMore() {
				// TODO 加载更多
				Log.e(TAG, "onLoad");
				loadData(1);
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 此处传回来的position和mAdapter.getItemId()获取的一致;
				Log.e(TAG, "click position:" + position);
//				Log.e(TAG, "__ mAdapter.getItemId() = "+mAdapter.getItemId(position));
			}
		});
		
		mCanPullRefBtn = (Button)findViewById(R.id.canPullRefBtn);
		mCanLoadMoreBtn = (Button)findViewById(R.id.canLoadMoreFlagBtn);
		mCanAutoLoadMoreBtn = (Button)findViewById(R.id.autoLoadMoreFlagBtn);
		mIsMoveToFirstItemBtn = (Button)findViewById(R.id.isMoveToFirstItemBtn);
		
		mCanPullRefBtn.setOnClickListener(this);
		mCanLoadMoreBtn.setOnClickListener(this);
		mCanAutoLoadMoreBtn.setOnClickListener(this);
		mIsMoveToFirstItemBtn.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View pV) {
		switch (pV.getId()) {
		case R.id.canPullRefBtn:
			mListView.setCanRefresh(!mListView.isCanRefresh());
			if(mCanPullRefBtn.getText().toString().
					equals("关闭下拉刷新")){
				mCanPullRefBtn.setText("启用下拉刷新");
			}else{
				mCanPullRefBtn.setText("关闭下拉刷新");
			}
			break;
		case R.id.canLoadMoreFlagBtn:
			mListView.setCanLoadMore(!mListView.isCanLoadMore());
			if(mCanLoadMoreBtn.getText().toString().
					equals("关闭加载更多")){
				mCanLoadMoreBtn.setText("启用加载更多");
			}else{
				mCanLoadMoreBtn.setText("关闭加载更多");
			}
			break;
		case R.id.autoLoadMoreFlagBtn:
			mListView.setAutoLoadMore(!mListView.isAutoLoadMore());
			if(mCanAutoLoadMoreBtn.getText().toString().
					equals("关闭自动加载更多")){
				mCanAutoLoadMoreBtn.setText("启用自动加载更多");
			}else{
				mCanAutoLoadMoreBtn.setText("关闭自动加载更多");
			}
			break;
		case R.id.isMoveToFirstItemBtn:
			mListView.setMoveToFirstItemAfterRefresh(
						!mListView.isMoveToFirstItemAfterRefresh());
			if(mIsMoveToFirstItemBtn.getText().toString().
					equals("关闭移动到第一条Item")){
				mIsMoveToFirstItemBtn.setText("启用移动到第一条Item");
			}else{
				mIsMoveToFirstItemBtn.setText("关闭移动到第一条Item");
			}
			break;
		}
	}
	
	public void loadData(final int type){
		new Thread(){
			@Override
			public void run() {
				List<AppInfo> _List = null;
				switch (type) {
				case 0:
					mCount = 10;
					
					_List = new ArrayList<AppInfo>();
					for(int i = 1; i <= mCount; i++){
						AppInfo ai = new AppInfo();

						ai.setAppIcon(BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_launcher));
						ai.setAppName("应用Demo_" + i);
						ai.setAppVer("版本: " + (i % 10 + 1) + "." + (i % 8 + 2) + "."
							+ (i % 6 + 3));
						ai.setAppSize("大小: " + i * 10 + "MB");

						_List.add(ai);
					}
					break;

				case 1:					
					_List = new ArrayList<AppInfo>();
					int _Index = mCount + 10;
					
					for(int i = mCount+1; i <= _Index; i++){
						AppInfo ai = new AppInfo();

						ai.setAppIcon(BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_launcher));
						ai.setAppName("应用Demo_" + i);
						ai.setAppVer("版本: " + (i % 10 + 1) + "." + (i % 8 + 2) + "."
							+ (i % 6 + 3));
						ai.setAppSize("大小: " + i * 10 + "MB");

						_List.add(ai);
					}
					mCount = _Index;
					break;
				}
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if(type==0){	//下拉刷新
//					Collections.reverse(mList);	//逆序
					Message _Msg = mHandler.obtainMessage(REFRESH_DATA_FINISH, _List);
					mHandler.sendMessage(_Msg);
				}else if(type==1){
					Message _Msg = mHandler.obtainMessage(LOAD_DATA_FINISH, _List);
					mHandler.sendMessage(_Msg);
				}
			}
		}.start();
	}
	
	private void buildAppData() {

		for (int i = 1; i <= 10; i++) {
			AppInfo ai = new AppInfo();

			ai.setAppIcon(BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_launcher));
			ai.setAppName("应用Demo_" + i);
			ai.setAppVer("版本: " + (i % 10 + 1) + "." + (i % 8 + 2) + "."
					+ (i % 6 + 3));
			ai.setAppSize("大小: " + i * 10 + "MB");

			mList.add(ai);
		}
	}
	
	private class CustomListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		public List<AppInfo> mList;

		public CustomListAdapter(Context pContext, List<AppInfo> pList) {
			mInflater = LayoutInflater.from(pContext);
			if(pList != null){
				mList = pList;
			}else{
				mList = new ArrayList<AppInfo>();
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
			System.out.println("getItemId = "+position);
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

			AppInfo ai = mList.get(position);
			holder.mImage.setImageBitmap(ai.getAppIcon());
			holder.mName.setText(ai.getAppName());
			holder.mVer.setText(ai.getAppVer());
			holder.mSize.setText(ai.getAppSize());

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
