package com.jingchen.pulltorefresh.Refreshable;

import android.content.Context;
import android.widget.TextView;

import com.jingchen.pulltorefresh.R;

/**
 * Created by Aaron Wang on 2016/12/2.
 */
public class DefaultFooter extends AbsFooterView {
    private TextView textView;

    public DefaultFooter(Context context) {
        super(context);
        init();
    }

    private void init() {
        textView = (TextView) footer.findViewById(R.id.text);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.default_footer;
    }

    @Override
    public void pullToLoadMore() {
        textView.setText("上拉加载更多");
    }

    @Override
    public void onLoading() {
        textView.setText("正在加载...");
    }

    @Override
    public void onLoadFail() {
        textView.setText("加载失败,点击重试");
    }

    @Override
    public void onLoadNone() {
        textView.setText("到底了哦~");
    }
}
