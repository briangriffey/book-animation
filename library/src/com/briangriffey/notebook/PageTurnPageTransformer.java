package com.briangriffey.notebook;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class PageTurnPageTransformer implements ViewPager.PageTransformer {

	private Point mLastTouchPoint;
	private Rect mTopViewRect;
	private Rect mBottomViewRect;

	private Paint mPaint;
	private int mCurrentPage;

	private int mPageTouchSlop;
	private boolean mIsTurning;

	private PageTurnDirection mDirection;
	private float mFirstX;

	private Handler mHandler = new Handler();

	public PageTurnPageTransformer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();

	}

	public PageTurnPageTransformer(Context context) {
		super(context);
		init();

	}

	private void init() {
		setWillNotDraw(false);
		mPaint = new Paint();

		mBottomViewRect = new Rect();
		mTopViewRect = new Rect();

		mPageTouchSlop = (int) getResources().getDimension(R.dimen.touch_start_padding);
	}

	protected boolean isTouchAPageTurnStart(MotionEvent ev) {
		if (ev.getAction() != MotionEvent.ACTION_DOWN)
			return false;

		return isTouchNearEdge(ev);

	}

	protected boolean isTouchNearEdge(MotionEvent ev) {
		if (Math.abs(ev.getX() - getMeasuredWidth()) < mPageTouchSlop)
			return true;
		else if (ev.getX() < mPageTouchSlop)
			return true;

		return false;
	}

	protected PageTurnDirection getPageTurnDirection(MotionEvent ev) {
		if (mFirstX - ev.getX() == 0.0f)
			return null;

		PageTurnDirection direction = mFirstX - ev.getX() > 0 ? PageTurnDirection.LEFT : PageTurnDirection.RIGHT;
		return direction;
	}

	protected boolean shouldTurn() {
		if (mDirection == null)
			return false;

		if (mDirection == PageTurnDirection.LEFT && mCurrentPage == getChildCount() - 1)
			return false;
		else if (mDirection == PageTurnDirection.RIGHT && mCurrentPage == 0)
			return false;

		return true;
	}

	public boolean onInterceptTouchEventDeprecated(MotionEvent ev) {
		return true;
	}

	@Override
	protected void onPageScrolled(int arg0, float arg1, int arg2) {
//		Log.d("PageTurn", "onPageScrolled:  " + arg0 + " : " + arg1 + " : " + arg2);
		super.onPageScrolled(arg0, arg1, arg2);
	}

	public boolean onTouchEventDeprecated(MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN && !mIsTurning) {

			mIsTurning = isTouchAPageTurnStart(event);

			if (!mIsTurning) {
				return false;
			} else {

				invalidate();
				mLastTouchPoint = new Point((int) event.getX(), (int) event.getY());
				mFirstX = event.getX();
				return true;
			}

		} else if (event.getAction() == MotionEvent.ACTION_MOVE && mIsTurning) {
			if (mDirection == null) {
				// get the page turn direction
				mDirection = getPageTurnDirection(event);

				// if we shouldn't turn then abort everything and reset it
				if (!shouldTurn()) {
					mDirection = null;
					mIsTurning = false;
					return false;
				}
			}

			mLastTouchPoint = new Point((int) event.getX(), (int) event.getY());
			invalidate();

		} else if (event.getAction() == MotionEvent.ACTION_UP && mIsTurning) {

			int halfWidth = getMeasuredWidth() / 2;

			if (mLastTouchPoint.x > halfWidth) {
				final Runnable animationRunnable = new Runnable() {
					public void run() {
						mLastTouchPoint.x += 20;
						invalidate();

						if (mLastTouchPoint.x < getMeasuredWidth())
							mHandler.post(this);
						else {
							mIsTurning = false;

							if (mDirection == PageTurnDirection.RIGHT)
								mCurrentPage--;
							mDirection = null;
						}
					}
				};

				mHandler.post(animationRunnable);
			} else {

				final Runnable animationRunnable = new Runnable() {
					public void run() {
						mLastTouchPoint.x -= 20;
						invalidate();

						if (mLastTouchPoint.x > -(getMeasuredWidth() / 2)) {
							mHandler.post(this);
						} else {
							mIsTurning = false;

							if (mDirection == PageTurnDirection.LEFT)
								mCurrentPage++;
							mDirection = null;
						}
					}
				};

				mHandler.post(animationRunnable);

			}
		}

		return true;
	}

	@Override
	public void transformPage(View page, float position){

		if (mLastTouchPoint != null && mIsTurning && mDirection != null) {
			View topView;
			View bottomView;

			if (mDirection == PageTurnDirection.LEFT) {
				topView = getChildAt(mCurrentPage);
				bottomView = getChildAt(mCurrentPage + 1);
			} else {
				topView = getChildAt(mCurrentPage - 1);
				bottomView = getChildAt(mCurrentPage);
			}

			int height = getMeasuredHeight();
			int width = getMeasuredWidth();

			int halfWidth = (int) (width * .5);

			int distanceToEnd = width - mLastTouchPoint.x;
			int backOfPageWidth = Math.min(halfWidth, distanceToEnd / 2);
			int shadowLength = Math.max(5, backOfPageWidth / 20);

			// The rect that represents the backofthepage
			Rect backOfPageRect = new Rect(mLastTouchPoint.x, 0, mLastTouchPoint.x + backOfPageWidth, height);
			// The along the crease of the turning page
			Rect shadowRect = new Rect(mLastTouchPoint.x - shadowLength, 0, mLastTouchPoint.x, height);
			// The shadow cast onto the next page by teh turning page
			Rect backShadowRect = new Rect(backOfPageRect.right, 0, backOfPageRect.right + (backOfPageWidth / 2), height);

			// set the top view to be the current page to the crease of the
			// turning page
			mTopViewRect.set(0, 0, mLastTouchPoint.x, getMeasuredHeight());
			mBottomViewRect.set(backOfPageRect.right, 0, width, height);

			canvas.save();
			// clip and draw the top page to your touch
			canvas.clipRect(mTopViewRect);
			topView.draw(canvas);
			canvas.restore();

			// clip and draw the first shadow
			canvas.save();
			canvas.clipRect(shadowRect);
			mPaint.setShader(new LinearGradient(shadowRect.left, shadowRect.top, shadowRect.right, shadowRect.top, 0x00000000, 0x44000000,
					Shader.TileMode.REPEAT));
			canvas.drawPaint(mPaint);
			canvas.restore();

			mPaint.setShader(null);

			// clip and draw the gradient that makes the page look bent
			canvas.save();
			canvas.clipRect(backOfPageRect);
			mPaint.setShadowLayer(0, 0, 0, 0x00000000);
			mPaint.setShader(new LinearGradient(backOfPageRect.left, backOfPageRect.top, backOfPageRect.right, backOfPageRect.top, new int[]{0xFFEEEEEE,
					0xFFDDDDDD, 0xFFEEEEEE, 0xFFD6D6D6}, new float[]{.35f, .73f, 9f, 1.0f}, Shader.TileMode.REPEAT));
			canvas.drawPaint(mPaint);
			canvas.restore();

			// draw the second page in the remaining space
			canvas.save();
			canvas.clipRect(mBottomViewRect);
			bottomView.draw(canvas);
			canvas.restore();

			// now draw a shadow
			if (backShadowRect.left > 0) {
				canvas.save();
				canvas.clipRect(backShadowRect);
				mPaint.setShader(new LinearGradient(backShadowRect.left, backShadowRect.top, backShadowRect.right, backShadowRect.top, 0x44000000, 0x00000000,
						Shader.TileMode.REPEAT));
				canvas.drawPaint(mPaint);
				// canvas.drawColor(0xFF000000);
				canvas.restore();
			}
		} else {
			getChildAt(mCurrentPage).draw(canvas);
		}
	}

}
