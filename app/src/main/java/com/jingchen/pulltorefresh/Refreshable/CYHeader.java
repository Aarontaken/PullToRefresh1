package com.jingchen.pulltorefresh.Refreshable;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jingchen.pulltorefresh.R;

/**
 * Created by Aaron Wang on 2016/11/21.
 */
public class CYHeader extends AbsHeaderView {

    private TextView textView;
    private ViewGroup refreshContainer;

    public CYHeader(Context context) {
        super(context);
        initHeader();
    }

    protected void initHeader() {
        refreshContainer = (ViewGroup) header.findViewById(R.id.refresh_container);
        textView = (TextView) header.findViewById(R.id.text);
    }

    @Override
    public void onPullToRefresh(float moveDeltaY, float refreshDist) {
        textView.setText("下拉刷新");
        refreshContainer.setVisibility(View.GONE);
    }

    @Override
    public void onReleaseToRefresh() {
        textView.setText("松开刷新数据");
        refreshContainer.setVisibility(View.GONE);
    }

    @Override
    public void onRefreshing() {
        textView.setText("正在刷新");
        refreshContainer.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
    }

    @Override
    public void onRefreshSuccess() {
        refreshContainer.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        textView.setText("刷新成功");
    }

    @Override
    public void onRefreshFail() {
        refreshContainer.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        textView.setText("刷新失败");
    }

    @Override
    int getHeaderLayout() {
        return R.layout.cy_header;
    }
}
