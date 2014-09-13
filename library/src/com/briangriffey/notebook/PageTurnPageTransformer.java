package com.briangriffey.notebook;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.ViewPager;
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
	private float mPageWidth;
	private PageTurnDirection mDirection;
	private float mFirstX;

	private Handler mHandler = new Handler();
	View mTopView;
	View mBottomView;

	public PageTurnPageTransformer() {
		super();
		init();
	}

	private void init() {
		mPaint = new Paint();

		mBottomViewRect = new Rect();
		mTopViewRect = new Rect();

		mPageTouchSlop = 10;// (int)
							// getResources().getDimension(R.dimen.touch_start_padding);
	}

	protected boolean isTouchAPageTurnStart(float touchXPosition) {
		return isTouchNearEdge(touchXPosition);
	}

	protected boolean isTouchNearEdge(float touchXPosition) {
		if (Math.abs(touchXPosition - mPageWidth) < mPageTouchSlop)
			return true;
		else if (touchXPosition < mPageTouchSlop)
			return true;

		return false;
	}

	protected PageTurnDirection getPageTurnDirection(float touchXPosition) {
		if (mFirstX - touchXPosition == 0.0f)
			return null;

		PageTurnDirection direction = mFirstX - touchXPosition > 0 ? PageTurnDirection.LEFT : PageTurnDirection.RIGHT;
		return direction;
	}

	protected boolean shouldTurn() {
		if (mDirection == null)
			return false;

		return true;
	}

	public boolean onInterceptTouchEventDeprecated(MotionEvent ev) {
		Log.d("PageTurn", "onInterceptTouchEventDeprecated");
		return true;
	}

	public boolean onTouchXEvent(float touchXPosition) {

		if (!mIsTurning) {

			mIsTurning = isTouchAPageTurnStart(touchXPosition);

			if (!mIsTurning) {
				return false;
			} else {

				mLastTouchPoint = new Point((int) touchXPosition, 0);
				mFirstX = touchXPosition;
				return true;
			}

		} else if (mIsTurning) {

		}

		if (mIsTurning) {

			if (mDirection == null) {
				// get the page turn direction
				mDirection = getPageTurnDirection(touchXPosition);

				// if we shouldn't turn then abort everything and reset it
				if (!shouldTurn()) {
					mDirection = null;
					mIsTurning = false;
					return false;
				}
			}
			mLastTouchPoint = new Point((int) touchXPosition, 0);

			final double halfWidth = 0.5;
			final double fullWidth = 1;

			if (mLastTouchPoint.x > halfWidth) {
				final Runnable animationRunnable = new Runnable() {
					public void run() {
						mLastTouchPoint.x += 20;

						if (mLastTouchPoint.x < fullWidth)
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

						if (mLastTouchPoint.x > -halfWidth) {
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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void transformPage(View page, float position) {
		if (page == null) {
			return;
		}
		mPageWidth = page.getWidth();

		if (position < -1) { // [-Infinity,-1)
			// This page is way off-screen to the left.
			page.setAlpha(0);
		} else if (position <= 0) { // [-1,0]
			onTouchXEvent(mPageWidth - Math.abs(mPageWidth * position));
			mTopView = page;
		} else if (position <= 1) { // (0,1]
			mBottomView = page;
		} else { // (1,+Infinity]
			// This page is way off-screen to the right.
			page.setAlpha(0);
		}
		if (mTopView == null || mBottomView == null || mLastTouchPoint == null) {
			Log.d("PageTurn", "Missing top or bottom view, not manimulating the drawing. " + mLastTouchPoint.toString() + " " + mFirstX + " " + position);
			return;
		}

		int height = page.getMeasuredHeight();
		int width = page.getMeasuredWidth();
		if (height == 0 || width == 0) {
			Log.d("PageTurn", "height or width of this view is 0, not manimulating the drawing. " + mLastTouchPoint.toString() + " " + mFirstX + " " + position);
			return;
		}

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
		mTopViewRect.set(0, 0, mLastTouchPoint.x, page.getMeasuredHeight());
		mBottomViewRect.set(backOfPageRect.right, 0, width, height);

		Canvas canvas;
		if (page.getDrawingCache() != null) {
			canvas = new Canvas(page.getDrawingCache());
		} else {
			Log.d("PageTurn", "This page had no canvas, using a small blank canvas. " + mLastTouchPoint.toString() + " " + mFirstX + " " + position);
			Bitmap b = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
//			Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			canvas = new Canvas(b);
		}
		page.setTranslationX(page.getWidth() * -position);

		canvas.save();
		// clip and draw the top page to your touch
		canvas.clipRect(mTopViewRect);
		mTopView.draw(canvas);
		canvas.restore();

		// clip and draw the first shadow
		canvas.save();
		canvas.clipRect(shadowRect);
		mPaint.setShader(new LinearGradient(shadowRect.left, shadowRect.top, shadowRect.right, shadowRect.top, 0x00000000, 0x44000000, Shader.TileMode.REPEAT));
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
		mBottomView.draw(canvas);
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
	}

}
