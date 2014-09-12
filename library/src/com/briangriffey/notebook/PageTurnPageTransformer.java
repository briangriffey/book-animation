package com.briangriffey.notebook;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * http://developer.android.com/training/animation/screen-slide.html
 *
 */
public class PageTurnPageTransformer implements ViewPager.PageTransformer {

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@Override
    public void transformPage(View page, float position) {
        float percentage = 1 - Math.abs(position);
        page.setCameraDistance(12000);
        setVisibility(page, position);
        setTranslation(page);
        setSize(page, position, percentage);
        setRotation(page, position, percentage);
    }
 
    private void setVisibility(View page, float position) {
        if (position < 0.5 && position > -0.5) {
            page.setVisibility(View.VISIBLE);
        } else {
            page.setVisibility(View.INVISIBLE);
        }
    }
 
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setTranslation(View page) {
        ViewPager viewPager = (ViewPager) page.getParent();
        int scroll = viewPager.getScrollX() - page.getLeft();
        page.setTranslationX(scroll);
    }
 
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setSize(View page, float position, float percentage) {
        page.setScaleX((position != 0 && position != 1) ? percentage : 1);
        page.setScaleY((position != 0 && position != 1) ? percentage : 1);
    }
 
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setRotation(View page, float position, float percentage) {
        if (position > 0) {
            page.setRotationY(-180 * (percentage + 1));
        } else {
            page.setRotationY(180 * (percentage + 1));
        }
    }
}
