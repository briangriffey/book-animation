package com.briangriffey.notebook;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PageTurnFragment extends Fragment {
	public static final String ARG_OBJECT = "object";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// The last two arguments ensure LayoutParams are inflated
		// properly.
		View rootView = inflater.inflate(R.layout.fragment_page_detail, container, false);
		Bundle args = getArguments();
		((TextView) rootView.findViewById(R.id.text)).setText(Integer.toString(args.getInt(ARG_OBJECT)));
		return rootView;
	}
}
