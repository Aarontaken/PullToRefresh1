package com.jingchen.pulltorefresh.Refreshable;

import android.content.Context;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import com.jingchen.pulltorefresh.R;

/**
 * Created by chunyu on 2016/11/21.
 */
public class QQHeader extends AbsHeaderView {
    // 下拉箭头的转180°动画
    private RotateAnimation rotateAnimation;
    // 均匀旋转动画
    private RotateAnimation refreshingAnimation;
    // 下拉的箭头
    private View pullView;
    // 正在刷新的图标
    private View refreshingView;
    // 刷新结果图标
    private View stateImageView;
    // 刷新结果：成功或失败
    private TextView stateTextView;

    public QQHeader(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        pullView = header.findViewById(R.id.pull_icon);
        stateTextView = (TextView) header.findViewById(R.id.state_tv);
        refreshingView = header.findViewById(R.id.refreshing_icon);
        stateImageView = header.findViewById(R.id.state_iv);

        rotateAnimation = (RotateAnimation) AnimationUtils.loadAnimation(context, R.anim.reverse_anim);
        refreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(context, R.anim.rotating);
        // 添加匀速转动动画
        LinearInterpolator lir = new LinearInterpolator();
        rotateAnimation.setInterpolator(lir);
        refreshingAnimation.setInterpolator(lir);
    }

    @Override
    public void onPullToRefresh(float moveDeltaY, float refreshDist) {
        // 下拉刷新
        stateImageView.setVisibility(View.GONE);
        stateTextView.setText(R.string.pull_to_refresh);
        pullView.clearAnimation();
        pullView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onReleaseToRefresh() {
        // 释放刷新
        stateTextView.setText(R.string.release_to_refresh);
        pullView.startAnimation(rotateAnimation);
    }

    @Override
    public void onRefreshing() {
        // 正在刷新
        pullView.clearAnimation();
        refreshingView.setVisibility(View.VISIBLE);
        pullView.setVisibility(View.INVISIBLE);
        refreshingView.startAnimation(refreshingAnimation);
        stateTextView.setText(R.string.refreshing);
    }

    @Override
    public void onRefreshSuccess() {
        // 刷新成功
        onRefreshFinished();
        stateImageView.setVisibility(View.VISIBLE);
        stateTextView.setText(R.string.refresh_succeed);
        stateImageView.setBackgroundResource(R.drawable.refresh_succeed);
    }

    @Override
    public void onRefreshFail() {
        // 刷新失败
        onRefreshFinished();
        stateImageView.setVisibility(View.VISIBLE);
        stateTextView.setText(R.string.refresh_fail);
        stateImageView.setBackgroundResource(R.drawable.refresh_failed);
    }

    @Override
    int getHeaderLayout() {
        return R.layout.refresh_head;
    }

    public void onRefreshFinished() {
        refreshingView.clearAnimation();
        refreshingView.setVisibility(View.GONE);
    }
}
