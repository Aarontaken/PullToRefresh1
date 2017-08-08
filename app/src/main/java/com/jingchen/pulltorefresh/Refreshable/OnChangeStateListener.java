package com.jingchen.pulltorefresh.Refreshable;

/**
 * Created by Aaron Wang on 2016/11/21.
 */
public interface OnChangeStateListener {
    /**
     * @param moveDeltaY 下拉距离
     * @param refreshDist 下拉刷新和松开刷新的边界距离
     */
    void onPullToRefresh(float moveDeltaY, float refreshDist);
    void onReleaseToRefresh();
    void onRefreshing();
}
