package com.github.h.f.installer;

import android.app.Fragment;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.licenses.SILOpenFontLicense11;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import com.github.h.f.installer.util.NavUtil;
import com.github.h.f.installer.util.ThemeUtil;

public class AboutActivity extends XposedBaseActivity {

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
            ab.setTitle(com.github.h.f.installer.R.string.nav_item_about);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        setFloating(toolbar, com.github.h.f.installer.R.string.nav_item_about);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(com.github.h.f.installer.R.id.container, new AboutFragment()).commit();
        }
    }

    public static class AboutFragment extends Fragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(com.github.h.f.installer.R.layout.tab_about, container, false);

            View developersView = v.findViewById(com.github.h.f.installer.R.id.developersView);
            View licensesView = v.findViewById(com.github.h.f.installer.R.id.licensesView);
            View translatorsView = v.findViewById(com.github.h.f.installer.R.id.translatorsView);
            View sourceCodeView = v.findViewById(com.github.h.f.installer.R.id.sourceCodeView);

            String packageName = getActivity().getPackageName();
            String translator = getResources().getString(com.github.h.f.installer.R.string.translator);

            try {
                String version = getActivity().getPackageManager().getPackageInfo(packageName, 0).versionName;
                ((TextView) v.findViewById(com.github.h.f.installer.R.id.app_version)).setText(version);
            } catch (NameNotFoundException ignored) {
            }

            licensesView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createLicenseDialog();
                }
            });

            developersView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                            .title(com.github.h.f.installer.R.string.about_developers_label)
                            .content(com.github.h.f.installer.R.string.about_developers)
                            .positiveText(android.R.string.ok)
                            .show();

                    ((TextView) dialog.findViewById(com.github.h.f.installer.R.id.md_content)).setMovementMethod(LinkMovementMethod.getInstance());
                }
            });

            sourceCodeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavUtil.startURL(getActivity(), getString(com.github.h.f.installer.R.string.about_source));
                }
            });

            if (translator.isEmpty()) {
                translatorsView.setVisibility(View.GONE);
            }

            return v;
        }

        private void createLicenseDialog() {
            Notices notices = new Notices();
            notices.addNotice(new Notice("material-dialogs", "https://github.com/afollestad/material-dialogs", "Copyright (c) 2014-2016 Aidan Michael Follestad", new MITLicense()));
            notices.addNotice(new Notice("StickyListHeaders", "https://github.com/emilsjolander/StickyListHeaders", "Emil Sjölander", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("PreferenceFragment-Compat", "https://github.com/Machinarius/PreferenceFragment-Compat", "machinarius", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("libsuperuser", "https://github.com/Chainfire/libsuperuser", "Copyright (C) 2012-2015 Jorrit \"Chainfire\" Jongma", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("picasso", "https://github.com/square/picasso", "Copyright 2013 Square, Inc.", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("materialdesignicons", "http://materialdesignicons.com", "Copyright (c) 2014, Austin Andrews", new SILOpenFontLicense11()));
            notices.addNotice(new Notice("commons-compress", "https://github.com/apache/commons-compress", "Copyright (c) 2020 The Apache Software Foundation.", new ApacheSoftwareLicense20()));

            new LicensesDialog.Builder(getActivity())
                    .setNotices(notices)
                    .setIncludeOwnLicense(true)
                    .build()
                    .show();
        }
    }
}
