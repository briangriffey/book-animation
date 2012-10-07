package com.briangriffey.notebook;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class PageTurnLayout extends FrameLayout {
	
	private Point mLastTouchPoint;
	private Rect mTopViewRect;
	private Rect mBottomViewRect;
	
	private Paint mPaint;
	
	private Handler mHandler = new Handler();
	
	public PageTurnLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);
		mPaint = new Paint();
		mBottomViewRect = new Rect();
		mTopViewRect = new Rect();
		
	}

	public PageTurnLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
		mPaint = new Paint();
		
		mBottomViewRect = new Rect();
		mTopViewRect = new Rect();
	}

	public PageTurnLayout(Context context) {
		super(context);
		setWillNotDraw(false);
		mPaint = new Paint();
		
		mBottomViewRect = new Rect();
		mTopViewRect = new Rect();
		
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if(event.getAction() == MotionEvent.ACTION_DOWN) {
			mLastTouchPoint = new Point((int)event.getX(), (int)event.getY());
			invalidate();
		} else if(event.getAction() == MotionEvent.ACTION_MOVE){
			mLastTouchPoint = new Point((int)event.getX(), (int)event.getY());
			invalidate();
		} else if(event.getAction() == MotionEvent.ACTION_UP) {
			int halfWidth = getMeasuredWidth() /2;
			if(mLastTouchPoint.x > halfWidth) {
				final Runnable animationRunnable = new Runnable() {
					public void run() {
						mLastTouchPoint.x += 20;
						invalidate();
						
						if(mLastTouchPoint.x < getMeasuredWidth())
							mHandler.post(this);
					}
				};
				
				mHandler.post(animationRunnable);
			} else {
				
				final Runnable animationRunnable = new Runnable() {
					public void run() {
						mLastTouchPoint.x -= 20;
						invalidate();
						
						if(mLastTouchPoint.x > -(getMeasuredWidth()/2))
							mHandler.post(this);
					}
				};
				
				mHandler.post(animationRunnable);
				
			}
		}
		
		return true;
	}
	
	@Override
	public void draw(Canvas canvas) {
		if(mLastTouchPoint != null && getChildCount() > 1){
			int height = getMeasuredHeight();
			int width = getMeasuredWidth();
			
			int halfWidth = (int)(width * .5);
			
			int distanceToEnd = width - mLastTouchPoint.x;
			int backOfPageWidth = Math.min(halfWidth, distanceToEnd/2);
			int shadowLength = Math.max(5, backOfPageWidth /20);
			
			Rect backOfPageRect = new Rect(mLastTouchPoint.x, 0, mLastTouchPoint.x + backOfPageWidth, height);
			Rect shadowRect = new Rect(mLastTouchPoint.x - shadowLength, 0, mLastTouchPoint.x, height);
			Rect backShadowRect = new Rect(backOfPageRect.right, 0, backOfPageRect.right + (backOfPageWidth/2), height);
			
			mTopViewRect.set(0,0, mLastTouchPoint.x, getMeasuredHeight());
			mBottomViewRect.set(backOfPageRect.right, 0, width, height);
			
			canvas.save();
			
			canvas.clipRect(mTopViewRect);
			getChildAt(0).draw(canvas);
			canvas.restore();
			
			canvas.save();
			canvas.clipRect(shadowRect);
			mPaint.setShader(new LinearGradient(shadowRect.left, shadowRect.top, shadowRect.right, shadowRect.top, 0x00000000, 0x44000000, Shader.TileMode.REPEAT));
			canvas.drawPaint(mPaint);
			canvas.restore();
			
			mPaint.setShader(null);
			
			canvas.save();
			canvas.clipRect(backOfPageRect);
			mPaint.setShadowLayer(0, 0, 0, 0x00000000);
			mPaint.setShader(new LinearGradient(backOfPageRect.left, backOfPageRect.top, backOfPageRect.right, backOfPageRect.top, 
					new int[]{0xFFEEEEEE, 0xFFDDDDDD, 0xFFEEEEEE, 0xFFD6D6D6},
					new float[]{.35f, .73f, 9f, 1.0f}
					, Shader.TileMode.REPEAT));
			canvas.drawPaint(mPaint);
			canvas.restore();
			
			
			canvas.save();
			canvas.clipRect(mBottomViewRect);
			getChildAt(1).draw(canvas);
			canvas.restore();
			
			canvas.save();
			canvas.clipRect(backShadowRect);
			mPaint.setShader(new LinearGradient(backShadowRect.left, backShadowRect.top, backShadowRect.right, backShadowRect.top, 0x44000000, 0x00000000, Shader.TileMode.REPEAT));
			canvas.drawPaint(mPaint);
//			canvas.drawColor(0xFF000000);
			canvas.restore();
		} 
	}

}
