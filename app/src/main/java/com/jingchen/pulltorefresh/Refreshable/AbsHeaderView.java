package com.jingchen.pulltorefresh.Refreshable;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Aaron Wang on 2016/11/21.
 * 这个类并不是个View,而是创建HeaderView的抽象父类
 */
public abstract class AbsHeaderView implements OnChangeStateListener,OnRefreshFinish{

    protected View header;

    public AbsHeaderView(Context context) {
        header = LayoutInflater.from(context).inflate(getHeaderLayout(),null);
    }

    public View getHeader(){
        return header;
    }

    abstract @LayoutRes int getHeaderLayout();
}
