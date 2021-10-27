package com.github.humenger.frida.installer.util.chrome;

import android.app.Activity;
import android.text.style.URLSpan;
import android.view.View;

import com.github.humenger.frida.installer.util.NavUtil;

/**
 * Created by Nikola D. on 12/23/2015.
 */
public class CustomTabsURLSpan extends URLSpan {

	private Activity activity;

	public CustomTabsURLSpan(Activity activity, String url) {
		super(url);
		this.activity = activity;
	}

	@Override
	public void onClick(View widget) {
		String url = getURL();
		NavUtil.startURL(activity, url);
	}
}