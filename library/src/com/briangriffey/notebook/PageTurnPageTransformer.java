package com.briangriffey.notebook;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

/**
 * http://developer.android.com/training/animation/screen-slide.html
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class PageTurnPageTransformer implements ViewPager.PageTransformer {
	private static final float MIN_SCALE = 0.75f;

	@Override
	public void transformPage(View view, float position) {

		int pageWidth = view.getWidth();
		final float normalizedposition = Math.abs(Math.abs(position) - 1) * 100;

		if (position < -1) { // [-Infinity,-1)
			// This page is way off-screen to the left.
			view.setAlpha(0);

		} else if (position <= 0) { // [-1,0]
			// Use the default slide transition when moving to the left page
			// view.setAlpha(1);
			// view.setTranslationX(0);
			// view.setScaleX(1);
			// view.setScaleY(1);

			Log.d("PageTurn", " Transforming page " + position + " " + normalizedposition);
			float percentage = 1 - Math.abs(position);
			view.setCameraDistance(12000);
			setVisibility(view, position);
			setTranslation(view);
			setSize(view, position, percentage);
			setRotation(view, position, percentage);

		} else if (position <= 1) { // (0,1]

			// Fade the page out.
			view.setAlpha(1 - position);

			// Counteract the default slide transition
			view.setTranslationX(pageWidth * -position);

//			// Scale the page down (between MIN_SCALE and 1)
//			float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
//			view.setScaleX(scaleFactor);
//			view.setScaleY(scaleFactor);

		} else { // (1,+Infinity]
			// This page is way off-screen to the right.
			view.setAlpha(0);
		}

	}

	private void setVisibility(View page, float position) {
		if (position < 0.5 && position > -0.5) {
			page.setVisibility(View.VISIBLE);
		} else {
			page.setVisibility(View.INVISIBLE);
		}
	}

	private void setTranslation(View page) {
		ViewPager viewPager = (ViewPager) page.getParent();
		int scroll = viewPager.getScrollX() - page.getLeft();
		page.setTranslationX(scroll);
	}

	private void setSize(View page, float position, float percentage) {
		page.setScaleX((position != 0 && position != 1) ? percentage : 1);
		page.setScaleY((position != 0 && position != 1) ? percentage : 1);
	}

	private void setRotation(View page, float position, float percentage) {
		int pageWidth = page.getWidth();

		if (position > 0) {
			page.setPivotX(pageWidth - 100);
			page.setRotationY(-180 * (percentage + 1));
		} else {
			page.setPivotX(100);

			page.setRotationY(180 * (percentage + 1));
		}
	}

}
