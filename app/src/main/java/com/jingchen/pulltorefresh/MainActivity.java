package com.jingchen.pulltorefresh;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jingchen.pulltorefresh.Refreshable.CYHeader;
import com.jingchen.pulltorefresh.Refreshable.DefaultFooter;
import com.jingchen.pulltorefresh.Refreshable.DotHeader;
import com.jingchen.pulltorefresh.Refreshable.OnRefreshListener;
import com.jingchen.pulltorefresh.Refreshable.PullToRefreshLayout;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements OnRefreshListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, PullToRefreshLayout.OnRefreshViewComplete {

	private PullToRefreshLayout pullToRefreshLayout;
	private ListView listView;
	private MyAdapter adapter;
	private List<String> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
	}

	private void init() {
		pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_view);
		pullToRefreshLayout.addHeader(new CYHeader(this));
		pullToRefreshLayout.addFooter(new DefaultFooter(this));
		pullToRefreshLayout.setOnRefreshListener(this);
		pullToRefreshLayout.setOnRefreshViewCompleteListener(this);// 花式刷新头
		listView = (ListView) pullToRefreshLayout.getContentView();
		adapter = new MyAdapter(this, getData());
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		listView.addHeaderView(getSimpleHeaderView());
	}

	private List<String> getData() {
		list = new ArrayList();
		for (int i=0;i<40;i++) {
			list.add(i+"块钱");
		}
		return list;
	}

	@Override
	public void onRefresh() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pullToRefreshLayout.refreshFinish(PullToRefreshLayout.REFRESH_SUCCEED);
						list.add(0,"新添数据");
						adapter.notifyDataSetChanged();
					}
				});
			}
		},3000);
	}

	public View getSimpleHeaderView() {
		TextView textView = new TextView(this);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		textView.setLayoutParams(params);
		textView.setPadding(0,20,0,20);
		textView.setText("i am header");
		textView.setGravity(Gravity.CENTER);
		return textView;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Toast.makeText(this, "onClick:" + position, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Toast.makeText(this, "longClick:" + position, Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public void onRefreshViewComplete() {
		if (pullToRefreshLayout.getAbsHeaderView() instanceof DotHeader) {
			pullToRefreshLayout.changeHeader(new CYHeader(MainActivity.this));
		}else {
			pullToRefreshLayout.changeHeader(new DotHeader(MainActivity.this));
		}
	}
}
