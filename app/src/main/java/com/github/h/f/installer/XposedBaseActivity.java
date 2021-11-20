package com.github.h.f.installer;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.github.h.f.installer.util.ThemeUtil;

public abstract class XposedBaseActivity extends AppCompatActivity {
    public int mTheme = -1;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        ThemeUtil.setTheme(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ThemeUtil.reloadTheme(this);
    }

    @SuppressWarnings("deprecation")
    public void setFloating(android.support.v7.widget.Toolbar toolbar, @StringRes int details) {
        boolean isTablet = getResources().getBoolean(com.github.h.f.installer.R.bool.isTablet);
        if (isTablet) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.height = getResources().getDimensionPixelSize(com.github.h.f.installer.R.dimen.floating_height);
            params.width = getResources().getDimensionPixelSize(com.github.h.f.installer.R.dimen.floating_width);
            params.alpha = 1.0f;
            params.dimAmount = 0.6f;
            params.flags |= 2;
            getWindow().setAttributes(params);

            if (details != 0) {
                toolbar.setTitle(details);
            }
            toolbar.setNavigationIcon(com.github.h.f.installer.R.drawable.ic_close);
            setFinishOnTouchOutside(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(com.github.h.f.installer.R.color.colorPrimaryDark));
        }
    }
}