package com.example.briangriffey.notebook;

import com.briangriffey.notebook.PageTurnPageTransformer;
import com.briangriffey.notebook.PageTurnPagerAdapter;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;

public class MainActivity extends FragmentActivity {
	// When requested, this adapter returns a DemoObjectFragment,
	// representing an object in the collection.
	PageTurnPagerAdapter mPagerAdapter;
	ViewPager mViewPager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// ViewPager and its adapters use support library
		// fragments, so use getSupportFragmentManager.
		mPagerAdapter = new PageTurnPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setPageTransformer(true, new PageTurnPageTransformer());
		mViewPager.setAdapter(mPagerAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
