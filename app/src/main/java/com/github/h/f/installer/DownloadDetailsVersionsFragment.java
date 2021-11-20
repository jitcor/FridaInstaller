package com.github.h.f.installer;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import com.github.h.f.installer.repo.Module;
import com.github.h.f.installer.repo.ModuleVersion;
import com.github.h.f.installer.repo.ReleaseType;
import com.github.h.f.installer.repo.RepoParser;
import com.github.h.f.installer.util.DownloadsUtil;
import com.github.h.f.installer.util.HashUtil;
import com.github.h.f.installer.util.ModuleUtil.InstalledModule;
import com.github.h.f.installer.util.RepoLoader;
import com.github.h.f.installer.util.ThemeUtil;
import com.github.h.f.installer.util.chrome.LinkTransformationMethod;
import com.github.h.f.installer.widget.DownloadView;

public class DownloadDetailsVersionsFragment extends ListFragment {
    private static VersionsAdapter sAdapter;
    private DownloadDetailsActivity mActivity;
    private Module module;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (DownloadDetailsActivity) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        module = mActivity.getModule();
        if (module == null)
            return;

        if (module.versions.isEmpty()) {
            setEmptyText(getString(com.github.h.f.installer.R.string.download_no_versions));
            setListShown(true);
        } else {
            RepoLoader repoLoader = RepoLoader.getInstance();
            if (!repoLoader.isVersionShown(module.versions.get(0))) {
                TextView txtHeader = new TextView(getActivity());
                txtHeader.setText(com.github.h.f.installer.R.string.download_test_version_not_shown);
                txtHeader.setTextColor(getResources().getColor(com.github.h.f.installer.R.color.warning));
                txtHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActivity.gotoPage(DownloadDetailsActivity.DOWNLOAD_SETTINGS);
                    }
                });
                getListView().addHeaderView(txtHeader);
            }

            sAdapter = new VersionsAdapter(mActivity, mActivity.getInstalledModule());
            for (ModuleVersion version : module.versions) {
                if (repoLoader.isVersionShown(version))
                    sAdapter.add(version);
            }
            setListAdapter(sAdapter);
        }

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int sixDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, metrics);
        int eightDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, metrics);
        getListView().setDivider(null);
        getListView().setDividerHeight(sixDp);
        getListView().setPadding(eightDp, eightDp, eightDp, eightDp);
        getListView().setClipToPadding(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    static class ViewHolder {
        TextView txtStatus;
        TextView txtVersion;
        TextView txtRelType;
        TextView txtUploadDate;
        DownloadView downloadView;
        TextView txtChangesTitle;
        TextView txtChanges;
    }

    public static class DownloadModuleCallback implements DownloadsUtil.DownloadFinishedCallback {
        private final ModuleVersion moduleVersion;

        public DownloadModuleCallback(ModuleVersion moduleVersion) {
            this.moduleVersion = moduleVersion;
        }

        @Override
        public void onDownloadFinished(Context context,
                                       DownloadsUtil.DownloadInfo info) {
            File localFile = new File(info.localFilename);
            if (!localFile.isFile())
                return;

            if (moduleVersion.md5sum != null && !moduleVersion.md5sum.isEmpty()) {
                try {
                    String actualMd5Sum = HashUtil.md5(localFile);
                    if (!moduleVersion.md5sum.equals(actualMd5Sum)) {
                        Toast.makeText(context, context.getString(com.github.h.f.installer.R.string.download_md5sum_incorrect, actualMd5Sum, moduleVersion.md5sum), Toast.LENGTH_LONG).show();
                        DownloadsUtil.removeById(context, info.id);
                        return;
                    }
                } catch (Exception e) {
                    Toast.makeText(context, context.getString(com.github.h.f.installer.R.string.download_could_not_read_file, e.getMessage()), Toast.LENGTH_LONG).show();
                    DownloadsUtil.removeById(context, info.id);
                    return;
                }
            }

            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(info.localFilename, 0);

            if (packageInfo == null) {
                Toast.makeText(context, com.github.h.f.installer.R.string.download_no_valid_apk, Toast.LENGTH_LONG).show();
                DownloadsUtil.removeById(context, info.id);
                return;
            }

            if (!packageInfo.packageName
                    .equals(moduleVersion.module.packageName)) {
                Toast.makeText(context, context.getString(com.github.h.f.installer.R.string.download_incorrect_package_name, packageInfo.packageName, moduleVersion.module.packageName), Toast.LENGTH_LONG).show();
                DownloadsUtil.removeById(context, info.id);
                return;
            }

            FridaApp.installApk(context, info);
        }
    }

    private class VersionsAdapter extends ArrayAdapter<ModuleVersion> {
        private final DateFormat mDateFormatter = DateFormat
                .getDateInstance(DateFormat.SHORT);
        private final int mColorRelTypeStable;
        private final int mColorRelTypeOthers;
        private final int mColorInstalled;
        private final int mColorUpdateAvailable;
        private final String mTextInstalled;
        private final String mTextUpdateAvailable;
        private final int mInstalledVersionCode;

        public VersionsAdapter(Context context, InstalledModule installed) {
            super(context, com.github.h.f.installer.R.layout.list_item_version);
            mColorRelTypeStable = ThemeUtil.getThemeColor(context, android.R.attr.textColorTertiary);
            mColorRelTypeOthers = getResources().getColor(com.github.h.f.installer.R.color.warning);
            mColorInstalled = ThemeUtil.getThemeColor(context, com.github.h.f.installer.R.attr.download_status_installed);
            mColorUpdateAvailable = getResources().getColor(com.github.h.f.installer.R.color.download_status_update_available);
            mTextInstalled = getString(com.github.h.f.installer.R.string.download_section_installed) + ":";
            mTextUpdateAvailable = getString(com.github.h.f.installer.R.string.download_section_update_available) + ":";
            mInstalledVersionCode = (installed != null) ? installed.versionCode : -1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(com.github.h.f.installer.R.layout.list_item_version, null, true);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.txtStatus = (TextView) view.findViewById(com.github.h.f.installer.R.id.txtStatus);
                viewHolder.txtVersion = (TextView) view.findViewById(com.github.h.f.installer.R.id.txtVersion);
                viewHolder.txtRelType = (TextView) view.findViewById(com.github.h.f.installer.R.id.txtRelType);
                viewHolder.txtUploadDate = (TextView) view.findViewById(com.github.h.f.installer.R.id.txtUploadDate);
                viewHolder.downloadView = (DownloadView) view.findViewById(com.github.h.f.installer.R.id.downloadView);
                viewHolder.txtChangesTitle = (TextView) view.findViewById(com.github.h.f.installer.R.id.txtChangesTitle);
                viewHolder.txtChanges = (TextView) view.findViewById(com.github.h.f.installer.R.id.txtChanges);
                viewHolder.downloadView.fragment = DownloadDetailsVersionsFragment.this;
                view.setTag(viewHolder);
            }

            ViewHolder holder = (ViewHolder) view.getTag();
            ModuleVersion item = getItem(position);

            holder.txtVersion.setText(item.name);
            holder.txtRelType.setText(item.relType.getTitleId());
            holder.txtRelType.setTextColor(item.relType == ReleaseType.STABLE
                    ? mColorRelTypeStable : mColorRelTypeOthers);

            if (item.uploaded > 0) {
                holder.txtUploadDate.setText(
                        mDateFormatter.format(new Date(item.uploaded)));
                holder.txtUploadDate.setVisibility(View.VISIBLE);
            } else {
                holder.txtUploadDate.setVisibility(View.GONE);
            }

            if (item.code <= 0 || mInstalledVersionCode <= 0
                    || item.code < mInstalledVersionCode) {
                holder.txtStatus.setVisibility(View.GONE);
            } else if (item.code == mInstalledVersionCode) {
                holder.txtStatus.setText(mTextInstalled);
                holder.txtStatus.setTextColor(mColorInstalled);
                holder.txtStatus.setVisibility(View.VISIBLE);
            } else { // item.code > mInstalledVersionCode
                holder.txtStatus.setText(mTextUpdateAvailable);
                holder.txtStatus.setTextColor(mColorUpdateAvailable);
                holder.txtStatus.setVisibility(View.VISIBLE);
            }

            holder.downloadView.setUrl(item.downloadLink);
            holder.downloadView.setTitle(mActivity.getModule().name);
            holder.downloadView.setDownloadFinishedCallback(new DownloadModuleCallback(item));

            if (item.changelog != null && !item.changelog.isEmpty()) {
                holder.txtChangesTitle.setVisibility(View.VISIBLE);
                holder.txtChanges.setVisibility(View.VISIBLE);

                if (item.changelogIsHtml) {
                    holder.txtChanges.setText(RepoParser.parseSimpleHtml(getActivity(), item.changelog, holder.txtChanges));
                    holder.txtChanges.setTransformationMethod(new LinkTransformationMethod(getActivity()));
                    holder.txtChanges.setMovementMethod(LinkMovementMethod.getInstance());
                } else {
                    holder.txtChanges.setText(item.changelog);
                    holder.txtChanges.setMovementMethod(null);
                }

            } else {
                holder.txtChangesTitle.setVisibility(View.GONE);
                holder.txtChanges.setVisibility(View.GONE);
            }

            return view;
        }
    }
}
