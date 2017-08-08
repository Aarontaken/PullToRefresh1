package com.jingchen.pulltorefresh.Refreshable;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jingchen.pulltorefresh.R;

import java.lang.ref.WeakReference;

/**
 * Created by Aaron Wang on 2016/11/26.
 */
public class DotHeader extends AbsHeaderView {

    private LinearLayout dots;
    private TextView textView;

    private ObjectAnimator[] objectAnimators;

    private float[] UP;
    private float[] DOWN;
    private int DOT_NUM = 0;
    private static final float UP_DISTANCE = -30f;
    private static final int Duration = 150;
    private static volatile boolean flag = true;

    private int curFir = 0;
    private int curSec = 1;

    private MyHandler handler;

    private static class MyHandler extends Handler{
        private WeakReference<DotHeader> dotHeaderWeakReference;

        public MyHandler(DotHeader dotHeader) {
            dotHeaderWeakReference = new WeakReference<>(dotHeader);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0 && flag) {
                DotHeader dotHeader = dotHeaderWeakReference.get();
                dotHeader.move2Dots(msg.arg1,msg.arg2);
                Message message = dotHeader.createNextMsg();
                sendMessageDelayed(message, Duration);
                Log.e("dots", "moving");
            }
        }
    }

    private Message createNextMsg() {
        Message message = Message.obtain();
        message.what = 0;
        curFir = message.arg1 = curSec;
        curSec = message.arg2 = (curSec + 1) % DOT_NUM;
        return message;
    }


    public DotHeader(Context context) {
        super(context);
        initHeader();
    }

    private void initHeader() {
        dots = (LinearLayout) header.findViewById(R.id.dots);
        DOT_NUM = dots.getChildCount();

        textView = (TextView) header.findViewById(R.id.text);

        handler = new MyHandler(this);
        initAnim();
        moveDotUp(0);
    }

    private void initAnim() {
        float translationY = dots.getChildAt(0).getTranslationY();
        UP = new float[]{translationY, translationY + UP_DISTANCE};
        DOWN = new float[]{translationY + UP_DISTANCE, translationY};

        objectAnimators = new ObjectAnimator[DOT_NUM];
        for(int i=0;i<DOT_NUM;i++) {
            objectAnimators[i] = ObjectAnimator.ofFloat(dots.getChildAt(i), "translationY", translationY);
            objectAnimators[i].setInterpolator(new AccelerateInterpolator());
        }
    }

    private void move2Dots(int fir, int sec) {
        moveDotDown(fir);
        moveDotUp(sec);
    }

    private void moveDotUp(int pos) {
        ObjectAnimator objectAnimator = objectAnimators[pos];
        objectAnimator.setFloatValues(UP);
        objectAnimator.setDuration(Duration).start();
    }

    private void moveDotDown(int pos) {
        ObjectAnimator objectAnimator = objectAnimators[pos];
        objectAnimator.setFloatValues(DOWN);
        objectAnimator.setDuration(Duration).start();
    }

    @Override
    int getHeaderLayout() {
        return R.layout.dot_header;
    }

    @Override
    public void onPullToRefresh(float moveDeltaY, float refreshDist) {
        textView.setText("下拉刷新");
        dots.setVisibility(View.GONE);
    }

    @Override
    public void onReleaseToRefresh() {
        textView.setText("松开刷新");
        dots.setVisibility(View.GONE);
    }

    @Override
    public void onRefreshing() {
        textView.setVisibility(View.GONE);
        dots.setVisibility(View.VISIBLE);
        startDotsAnim();
    }

    private void startDotsAnim() {
        flag = true;
        Message message = Message.obtain();
        message.what = 0;
        message.arg1 = curFir;
        message.arg2 = curSec;
        handler.sendMessage(message);
    }

    @Override
    public void onRefreshSuccess() {
        dots.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        textView.setText("刷新成功");
        stopAnim();
    }

    @Override
    public void onRefreshFail() {
        dots.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        textView.setText("刷新失败");
        stopAnim();
    }

    private void stopAnim() {
        flag = false;
        handler.removeCallbacksAndMessages(null);
    }

}
