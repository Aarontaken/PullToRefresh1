package com.jingchen.pulltorefresh.Refreshable;

/**
 * Created by Aaron Wang on 2016/12/2.
 */
public interface OnFooterStateChangeListener {
    void pullToLoadMore();
    void onLoading();
    void onLoadFail();
    void onLoadNone();// 没数据了
}
