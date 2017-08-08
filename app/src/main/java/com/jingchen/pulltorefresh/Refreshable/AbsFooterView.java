package com.jingchen.pulltorefresh.Refreshable;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Aaron Wang on 2016/12/2.
 * 这个类并不是个View,而是创建FooterView的抽象父类
 */
public abstract class AbsFooterView implements OnFooterStateChangeListener{
    protected View footer;

    protected AbsFooterView(Context context) {
        footer = LayoutInflater.from(context).inflate(getLayoutId(), null);
    }

    protected abstract @LayoutRes int getLayoutId();

    public View getFooter(){
        return footer;
    }
}
