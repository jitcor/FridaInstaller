package com.github.h.f.installer;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.h.f.installer.util.NavUtil;
import com.github.h.f.installer.util.ThemeUtil;

public class SupportActivity extends XposedBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setTheme(this);
        setContentView(com.github.h.f.installer.R.layout.activity_container);

        Toolbar toolbar = (Toolbar) findViewById(com.github.h.f.installer.R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(com.github.h.f.installer.R.string.nav_item_support);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        setFloating(toolbar, 0);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(com.github.h.f.installer.R.id.container, new SupportFragment()).commit();
        }
    }

    public static class SupportFragment extends Fragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(com.github.h.f.installer.R.layout.tab_support, container, false);

            View installerSupportView = v.findViewById(com.github.h.f.installer.R.id.installerSupportView);
            View faqView = v.findViewById(com.github.h.f.installer.R.id.faqView);
            View donateView = v.findViewById(com.github.h.f.installer.R.id.donateView);
            TextView txtModuleSupport = (TextView) v.findViewById(com.github.h.f.installer.R.id.tab_support_module_description);

            txtModuleSupport.setText(getString(com.github.h.f.installer.R.string.support_modules_description,
                    getString(com.github.h.f.installer.R.string.module_support)));

            setupView(installerSupportView, com.github.h.f.installer.R.string.about_support);
            setupView(faqView, com.github.h.f.installer.R.string.support_faq_url);
            setupView(donateView, com.github.h.f.installer.R.string.support_donate_url);

            return v;
        }

        public void setupView(View v, final int url) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavUtil.startURL(getActivity(), getString(url));
                }
            });
        }
    }
}
