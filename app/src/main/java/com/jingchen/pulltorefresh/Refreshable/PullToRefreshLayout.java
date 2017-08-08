package com.jingchen.pulltorefresh.Refreshable;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 整个下拉刷新就这一个布局，用来管理两个子控件，其中一个是下拉头，另一个是包含内容的contentView（可以是AbsListView的任何子类）
 *
 */
public class PullToRefreshLayout extends RelativeLayout implements OnTouchListener {
	public static final String TAG = "PullToRefreshLayout";
	// 下拉刷新
	public static final int PULL_TO_REFRESH = 0;
	// 释放刷新
	public static final int RELEASE_TO_REFRESH = 1;
	// 正在刷新
	public static final int REFRESHING = 2;
	// 刷新完毕
	public static final int DONE = 3;
	// 当前状态
	private int state = DONE;
	// 刷新回调接口
	private OnRefreshListener mListener;
	// 刷新成功
	public static final int REFRESH_SUCCEED = 0;
	// 刷新失败
	public static final int REFRESH_FAIL = 1;
	// 具体下拉头
	private View headView;
	// 下拉头封装类
	private AbsHeaderView absHeaderView;
	// 具体加载更多footer
	private View footView;
	// 加载更多view封装类
	private AbsFooterView absFooterView;
	// 内容
	private View contentView;
	// 按下Y坐标，上一个事件点Y坐标
	private float downY, lastY;
	// 下拉的距离
	public float moveDeltaY = 0;
	// 释放刷新的距离
	private float refreshDist = 200;
	private Timer timer;
	private MyTimerTask mTask;
	// 回滚速度
	public float MOVE_SPEED = 8;
	// 第一次执行布局
	private boolean isLayout = false;
	// 是否可以下拉
	private boolean canPull = true;
	// 在刷新过程中滑动操作
	private boolean isTouchInRefreshing = false;
	// 手指滑动距离与下拉头的滑动距离比
	private float radio = 2;
	// 刷新完成后停留时间
	private long mPauseTime = 200L;
	// 是否可以下拉刷新
	private boolean mRefreshEnable = true;
	/**
	 * 执行自动回滚的handler
	 */
	Handler updateHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// 回弹速度随下拉距离moveDeltaY增大而增大
			MOVE_SPEED = (float) (8 + 5 * Math.tan(Math.PI / 2 / getMeasuredHeight() * moveDeltaY));
			if (state == REFRESHING && moveDeltaY <= refreshDist && !isTouchInRefreshing) {
				// 正在刷新，且没有往上推的话则悬停，显示"正在刷新..."
				moveDeltaY = refreshDist;
				mTask.cancel();
			}
			if (canPull)
				moveDeltaY -= MOVE_SPEED;
			if (moveDeltaY <= 0) {
				// 已完成回弹
				moveDeltaY = 0;
				if(littleUseListener != null) littleUseListener.onRefreshViewComplete();// 这个几乎没用处, 动态切换刷新头可能会用(谁会那么干! —我)
				// 隐藏下拉头时有可能还在刷新，只有当前状态不是正在刷新时才改变状态
				if (state != REFRESHING) {
					changeState(DONE);
				}
				mTask.cancel();
			}
			// 刷新布局,会自动调用onLayout
			requestLayout();
		}

	};

	private boolean canLoadMore = true;
	private static final String PULL_TO_LOAD_MORE = "PULL_TO_LOAD_MORE";
	private static final String LOADING_MORE = "LOADING_MORE";
	private static final String LOAD_FAILED = "LOAD_FAILED";
	private static final String LOAD_NONE = "LOAD_NONE";
	private static String foot_state = PULL_TO_LOAD_MORE;

	private int footHeight;

	private Handler footerHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {

		}
	};

	private OnRefreshViewComplete littleUseListener;

	public interface OnRefreshViewComplete{
		void onRefreshViewComplete();
	}

	public void setOnRefreshViewCompleteListener(OnRefreshViewComplete littleUseListener) {
		this.littleUseListener = littleUseListener;
	}

	public void setOnRefreshListener(OnRefreshListener listener)
	{
		mListener = listener;
	}

	public PullToRefreshLayout(Context context) {
		super(context);
		init();
	}

	public PullToRefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		timer = new Timer();
		mTask = new MyTimerTask(updateHandler);
	}

	private void hideHead() {
		if (mTask != null) {
			mTask.cancel();
			mTask = null;
		}
		mTask = new MyTimerTask(updateHandler);
		timer.schedule(mTask, 0, 5);
	}

	/**
	 * 完成刷新操作，显示刷新结果
	 */
	public void refreshFinish(int refreshResult) {
		switch (refreshResult) {
			case REFRESH_SUCCEED:
				// 刷新成功
				absHeaderView.onRefreshSuccess();
				break;
			case REFRESH_FAIL:
				// 刷新失败
				absHeaderView.onRefreshFail();
				break;
			default:
				break;
		}
		// 刷新结果停留mPause秒
		new Handler() {
			@Override
			public void handleMessage(Message msg) {
				state = PULL_TO_REFRESH;
				hideHead();
			}
		}.sendEmptyMessageDelayed(0, mPauseTime);
	}

	private void changeState(int to) {
		state = to;
		switch (state) {
			case PULL_TO_REFRESH:
				// 下拉刷新
				absHeaderView.onPullToRefresh(moveDeltaY, refreshDist);
				break;
			case RELEASE_TO_REFRESH:
				// 释放刷新
				absHeaderView.onReleaseToRefresh();
				break;
			case REFRESHING:
				// 正在刷新
				absHeaderView.onRefreshing();
				break;
			default:
				break;
		}
	}

	/*
     * （非 Javadoc）由父控件决定是否分发事件，防止事件冲突
     *
     * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent)
     */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				downY = ev.getY();
				lastY = downY;
				if (mTask != null) {
					mTask.cancel();
				}
				/*
				 * 触碰的地方位于下拉头布局，由于我们没有对下拉头做事件响应，这时候它会给咱返回一个false导致接下来的事件不再分发进来。
				 * 所以我们不能交给父类分发，直接返回true
				 */
				if (ev.getY() < moveDeltaY)
					return true;
				break;
			case MotionEvent.ACTION_MOVE:
				// canPull这个值在底下onTouch中会根据ListView是否滑到顶部来改变，意思是是否可下拉
				if (canPull) {
					// 对实际滑动距离做缩小，造成用力拉的感觉
					moveDeltaY = moveDeltaY + (ev.getY() - lastY) / radio;

					if (moveDeltaY < 0)
						moveDeltaY = 0;

					// 正在刷新的时候触摸移动
					isTouchInRefreshing = state == REFRESHING;

					lastY = ev.getY();

					// 根据下拉距离改变比例
					radio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight() * moveDeltaY));

					moveDeltaY = mRefreshEnable ? moveDeltaY : 0;
					requestLayout();

					// headerView状态变化
					if (moveDeltaY <= refreshDist && state == DONE) {
						changeState(PULL_TO_REFRESH);
					}
					if (moveDeltaY <= refreshDist && state == RELEASE_TO_REFRESH) {
						// 如果下拉距离没达到刷新的距离且当前状态是释放刷新，改变状态为下拉刷新
						changeState(PULL_TO_REFRESH);
					}
					if (moveDeltaY >= refreshDist && state == PULL_TO_REFRESH) {
						changeState(RELEASE_TO_REFRESH);
					}

					if (moveDeltaY > 8) {
						// 防止下拉过程中误触发长按事件和点击事件
						clearContentViewEvents();
					}

					// 正在下拉，不让子控件捕获事件
					if (moveDeltaY > 0) {
						return true;
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				if (moveDeltaY > refreshDist)
					// 正在刷新时往下拉释放后下拉头不隐藏
					isTouchInRefreshing = false;
				if (state == RELEASE_TO_REFRESH) {
					// 刷新操作
					setRefresh();
				}
				hideHead();
			default:
				break;
		}
		// 事件分发交给父类
		return super.dispatchTouchEvent(ev);
	}

	/*
     * （非 Javadoc）绘制阴影效果，颜色值可以修改
     *
     * @see android.view.ViewGroup#dispatchDraw(android.graphics.Canvas)
     */
//	@Override
//	protected void dispatchDraw(Canvas canvas) {
//		super.dispatchDraw(canvas);
//		if (moveDeltaY == 0)
//			return;
//		RectF rectF = new RectF(0, 0, getMeasuredWidth(), moveDeltaY);
//		Paint paint = new Paint();
//		paint.setAntiAlias(true);
//		// 阴影的高度为26
//		LinearGradient linearGradient = new LinearGradient(0, moveDeltaY, 0, moveDeltaY - 26, 0x66000000, 0x00000000, Shader.TileMode.CLAMP);
//		paint.setShader(linearGradient);
//		paint.setStyle(Paint.Style.FILL);
//		// 在moveDeltaY处往上变淡
//		canvas.drawRect(rectF, paint);
//	}

	/**
	 * 通过反射修改字段去掉长按事件和点击事件
	 */
	private void clearContentViewEvents() {
		try {
			Field[] fields = AbsListView.class.getDeclaredFields();
			for (int i = 0; i < fields.length; i++)
				if (fields[i].getName().equals("mPendingCheckForLongPress")) {
					// mPendingCheckForLongPress是AbsListView中的字段，通过反射获取并从消息列表删除，去掉长按事件
					fields[i].setAccessible(true);
					contentView.getHandler().removeCallbacks((Runnable) fields[i].get(contentView));
				} else if (fields[i].getName().equals("mTouchMode")) {
					// TOUCH_MODE_REST = -1， 这个可以去除点击事件
					fields[i].setAccessible(true);
					fields[i].set(contentView, -1);
				}
			// 去掉焦点
			((AbsListView) contentView).getSelector().setState(new int[]
					{ 0 });
		} catch (Exception e) {
			Log.d(TAG, "error : " + e.toString());
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// 初始化AbsListView
		contentView =  getChildAt(0);
		contentView.setOnTouchListener(this);
	}

	// 第一次onLayout执行的标记
	private boolean flag = true;

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (canPull) {
			// 改变子控件的布局
			headView.layout(0, (int) moveDeltaY - headView.getMeasuredHeight(), headView.getMeasuredWidth(), (int) moveDeltaY);
			contentView.layout(0, (int) moveDeltaY, contentView.getMeasuredWidth(), (int) moveDeltaY + contentView.getMeasuredHeight());
		}
	}

	public void addHeader(AbsHeaderView absHeaderView) {
		if (this.absHeaderView != null) {
			// 不能重复添加刷新头 改变刷新头
			throw new RuntimeException("headerView has been added! you can use method changeHeader(AbsHeaderView absHeaderView) to replace the current header");
		}
		this.absHeaderView = absHeaderView;
		headView = this.absHeaderView.getHeader();
		addView(headView);
		headView.measure(0,0);
		refreshDist = headView.getMeasuredHeight();
	}

	public void changeHeader(AbsHeaderView absHeaderView) {
		if (this.absHeaderView == null) {
			addHeader(absHeaderView);
		}else {
			removeView(headView);
			this.absHeaderView = null;
			addHeader(absHeaderView);
		}
	}

	public void addFooter(AbsFooterView absFooterView) {
		if (!(contentView instanceof AbsListView)) {
			throw new RuntimeException("只有AbsListView才能添加footer");
		}
		this.absFooterView = absFooterView;
		footView = absFooterView.getFooter();
		footView.measure(0,0);
		footHeight = footView.getMeasuredHeight();
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(ABOVE, contentView.getId());
		addView(footView, layoutParams);
	}

	public View getContentView() {
		return contentView;
	}

	class MyTimerTask extends TimerTask {
		Handler handler;

		public MyTimerTask(Handler handler) {
			this.handler = handler;
		}

		@Override
		public void run()
		{
			handler.sendMessage(handler.obtainMessage());
		}

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		AbsListView alv;
		try {
			alv = (AbsListView) v;
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
			return false;
		}
		if (alv.getCount() == 0) {
			// 没有item的时候也可以下拉刷新
			canPull = true;
		} else {
			// 是否滑到AbsListView的顶部
			canPull = alv.getFirstVisiblePosition() == 0 && alv.getChildAt(0).getTop() >= 0;
		}
		return false;
	}

	public AbsHeaderView getAbsHeaderView() {
		return absHeaderView;
	}

	// 设置刷新完成后停留时间(显示刷新成功或失败)
	public void setmPauseTime(int pauseTime) {
		mPauseTime = pauseTime;
	}

	public void setRefreshEnable(boolean refreshEnable) {
		this.mRefreshEnable = refreshEnable;
	}

	// 手动调用刷新
	public void setRefresh() {
		if (mListener != null) {
			mListener.onRefresh();
			changeState(REFRESHING);
		}
	}
}