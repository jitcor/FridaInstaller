package com.github.h.f.installer;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.github.h.f.installer.installation.StatusInstallerFragment;
import com.github.h.f.installer.util.FridaUtil;
import com.github.h.f.installer.util.HexDump;
import com.github.h.f.installer.util.Loader;
import com.github.h.f.installer.util.ModuleUtil;
import com.github.h.f.installer.util.ModuleUtil.InstalledModule;
import com.github.h.f.installer.util.ModuleUtil.ModuleListener;
import com.github.h.f.installer.util.RepoLoader;
import com.github.h.f.installer.util.ThemeUtil;

import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.IOException;

public class WelcomeActivity extends XposedBaseActivity implements NavigationView.OnNavigationItemSelectedListener,
        ModuleListener, Loader.Listener<RepoLoader> {

    private static final String SELECTED_ITEM_ID = "SELECTED_ITEM_ID";
    private final Handler mDrawerHandler = new Handler();
    private RepoLoader mRepoLoader;
    private DrawerLayout mDrawerLayout;
    private int mPrevSelectedId;
    private NavigationView mNavigationView;
    private int mSelectedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setTheme(this);
        setContentView(com.github.h.f.installer.R.layout.activity_welcome);

        mDrawerLayout = (DrawerLayout) findViewById(com.github.h.f.installer.R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(com.github.h.f.installer.R.id.toolbar);
        setSupportActionBar(toolbar);

        mNavigationView = (NavigationView) findViewById(com.github.h.f.installer.R.id.navigation_view);
        assert mNavigationView != null;
        mNavigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, toolbar, com.github.h.f.installer.R.string.navigation_drawer_open,
                com.github.h.f.installer.R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                super.onDrawerSlide(drawerView, 0); // this disables the arrow @ completed state
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0); // this disables the animation
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSelectedId = mNavigationView.getMenu().getItem(prefs.getInt("default_view", 0)).getItemId();
        mSelectedId = savedInstanceState == null ? mSelectedId : savedInstanceState.getInt(SELECTED_ITEM_ID);
        mPrevSelectedId = mSelectedId;
        mNavigationView.getMenu().findItem(mSelectedId).setChecked(true);

        if (savedInstanceState == null) {
            mDrawerHandler.removeCallbacksAndMessages(null);
            mDrawerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigate(mSelectedId);
                }
            }, 250);

            boolean openDrawer = prefs.getBoolean("open_drawer", false);

            if (openDrawer)
                mDrawerLayout.openDrawer(GravityCompat.START);
            else
                mDrawerLayout.closeDrawers();
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int value = extras.getInt("fragment", prefs.getInt("default_view", 0));
            switchFragment(value);
        }

        mRepoLoader = RepoLoader.getInstance();
        ModuleUtil.getInstance().addListener(this);
        mRepoLoader.addListener(this);

        notifyDataSetChanged();
    }

    public void switchFragment(int itemId) {
        mSelectedId = mNavigationView.getMenu().getItem(itemId).getItemId();
        mNavigationView.getMenu().findItem(mSelectedId).setChecked(true);
        mDrawerHandler.removeCallbacksAndMessages(null);
        mDrawerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigate(mSelectedId);
            }
        }, 250);
        mDrawerLayout.closeDrawers();
    }

    private void navigate(final int itemId) {
        final View elevation = findViewById(com.github.h.f.installer.R.id.elevation);
        Fragment navFragment = null;
        switch (itemId) {
            case com.github.h.f.installer.R.id.nav_item_framework:
                mPrevSelectedId = itemId;
                setTitle(com.github.h.f.installer.R.string.app_name);
                navFragment = new StatusInstallerFragment();
                break;
            case com.github.h.f.installer.R.id.nav_item_modules:
                mPrevSelectedId = itemId;
                setTitle(com.github.h.f.installer.R.string.nav_item_modules);
                navFragment = new ModulesFragment();
                break;
            case com.github.h.f.installer.R.id.nav_item_downloads:
                mPrevSelectedId = itemId;
                setTitle(com.github.h.f.installer.R.string.nav_item_download);
                navFragment = new DownloadFragment();
                break;
            case com.github.h.f.installer.R.id.nav_item_logs:
                mPrevSelectedId = itemId;
                setTitle(com.github.h.f.installer.R.string.nav_item_logs);
                navFragment = new LogsFragment();
                break;
            case com.github.h.f.installer.R.id.nav_item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                mNavigationView.getMenu().findItem(mPrevSelectedId).setChecked(true);
                return;
            case com.github.h.f.installer.R.id.nav_item_support:
                startActivity(new Intent(this, SupportActivity.class));
                mNavigationView.getMenu().findItem(mPrevSelectedId).setChecked(true);
                return;
            case com.github.h.f.installer.R.id.nav_item_about:
                startActivity(new Intent(this, AboutActivity.class));
                mNavigationView.getMenu().findItem(mPrevSelectedId).setChecked(true);
                return;
        }

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(4));

        if (navFragment != null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setCustomAnimations(com.github.h.f.installer.R.animator.fade_in, com.github.h.f.installer.R.animator.fade_out);
            try {
                transaction.replace(com.github.h.f.installer.R.id.content_frame, navFragment).commit();

                if (elevation != null) {
                    Animation a = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            elevation.setLayoutParams(params);
                        }
                    };
                    a.setDuration(150);
                    elevation.startAnimation(a);
                }
            } catch (IllegalStateException ignored) {
            }
        }
    }

    public int dp(float value) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;

        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        HuDebug.invertCode(false,()->{
            System.out.println("outout:"+FridaApp.checkFridaActive());
            new Thread(()->{
                try {
                    PackageInfo packageInfo = getPackageManager().getPackageInfo("bin.mt.plus", 0);
                    ApplicationInfo applicationInfo=packageInfo.applicationInfo;
                    String apkPath=applicationInfo.publicSourceDir;
                    System.out.println("apkPath:"+apkPath);
                    File apkFile=new File(apkPath);
                    byte[] data=new byte[100];
                    IOUtils.read(apkFile,data);
                    System.out.println("Hex:\n"+ HexDump.dumpHexString(data));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }).start();
        });
        HuDebug.invertCode(true,()->{
            System.out.println("outout:"+FridaApp.checkFridaActive());
            new Thread(()->{
                try {
                    PackageInfo packageInfo = getPackageManager().getPackageInfo("bin.mt.plus", 0);
                    ApplicationInfo applicationInfo=packageInfo.applicationInfo;
//                    String apkPath="/data/local/tmp/frida_scripts/app.list";
//                    String apkPath=applicationInfo.publicSourceDir;
//                    String apkPath="/data/data/com.mhook.alipay/test.bin";
                    String apkPath="/system/etc/minitool.prop";
//                    String apkPath="/data/data/"+BuildConfig.APPLICATION_ID+"/files/fs/app.list";
                    System.out.println("apkPath:"+apkPath);
                    File apkFile=new File(apkPath);
                    byte[] data=new byte[100];
                    IOUtils.read(apkFile,data);
                    System.out.println("Hex:\n"+ HexDump.dumpHexString(data));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }).start();
        });
        HuDebug.invertCode(false,()->{
            Log.d(FridaApp.TAG,"[installJavascript]"+FridaUtil.installJavascript("showToast","Java.perform(function () {\n" +
                    "\n" +
                    "    function tryCode(enable, func) {\n" +
                    "        if (!enable) {\n" +
                    "            return\n" +
                    "        }\n" +
                    "        try {\n" +
                    "            func()\n" +
                    "        } catch (e) {\n" +
                    "            Java.use(\"android.util.Log\").e(\"FridaLog\", e)\n" +
                    "        }\n" +
                    "\n" +
                    "    }\n" +
                    "    tryCode(true,function (){\n" +
                    "        const String = Java.use('java.lang.String')\n" +
                    "        // @ts-ignore\n" +
                    "        Java.use(\"com.github.humenger.frida.installer.AboutActivity\").onCreate.implementation = function (bundle) {\n" +
                    "            this.onCreate(bundle)\n" +
                    "            Java.use(\"android.app.AlertDialog$Builder\").$new(this)\n" +
                    "                .setTitle(String.$new(\"Hook ti111ps\"))\n" +
                    "                .setMessage(String.$new(\"Hook 1111\"))\n" +
                    "                .setPositiveButton(String.$new(\"OK\"), null)\n" +
                    "                .show();\n" +
                    "\n" +
                    "        }\n" +
                    "    })\n" +
                    "    Java.use(\"android.util.Log\").e(\"FridaLog\", \"Hook Success\")\n" +
                    "});",new String[]{BuildConfig.APPLICATION_ID}));
        });
        HuDebug.invertCode(false,()->{
            Log.d(FridaApp.TAG,"[installJavascript]"+FridaUtil.installJavascript("showToast","Java.perform(function () {\n" +
                    "\n" +
                    "    function tryCode(enable, func) {\n" +
                    "        if (!enable) {\n" +
                    "            return\n" +
                    "        }\n" +
                    "        try {\n" +
                    "            func()\n" +
                    "        } catch (e) {\n" +
                    "            Java.use(\"android.util.Log\").e(\"FridaLog\", e)\n" +
                    "        }\n" +
                    "\n" +
                    "    }\n" +
                    "    tryCode(true,function (){\n" +
                    "        const String = Java.use('java.lang.String')\n" +
                    "        // @ts-ignore\n" +
                    "        Java.use(\"com.ihbing.fy.SettingsActivity\").onCreate.implementation = function (bundle) {\n" +
                    "            this.onCreate(bundle)\n" +
                    "            Java.use(\"android.app.AlertDialog$Builder\").$new(this)\n" +
                    "                .setTitle(String.$new(\"Hook ti111ps\"))\n" +
                    "                .setMessage(String.$new(\"Hook 1111\"))\n" +
                    "                .setPositiveButton(String.$new(\"OK\"), null)\n" +
                    "                .show();\n" +
                    "\n" +
                    "        }\n" +
                    "    })\n" +
                    "    Java.use(\"android.util.Log\").e(\"FridaLog\", \"Hook Success\")\n" +
                    "});",new String[]{"com.ihbing.fy"}));
        });
        HuDebug.invertCode(true,()->{
            Log.d(FridaApp.TAG,"[installJavascript]"+FridaUtil.installJavascript("active",
                    "Java.perform(function () {\n" +
                            "    Java.use('com.github.h.f.installer.FridaApp').checkFridaActive.implementation = function () {\n" +
                            "        return true;\n" +
                            "    }\n" +
                            "});"
                    ,new String[]{BuildConfig.APPLICATION_ID}));
        });
        super.onStart();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        mSelectedId = menuItem.getItemId();
        mDrawerHandler.removeCallbacksAndMessages(null);
        mDrawerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigate(mSelectedId);
            }
        }, 250);
        mDrawerLayout.closeDrawers();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM_ID, mSelectedId);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void notifyDataSetChanged() {
        View parentLayout = findViewById(com.github.h.f.installer.R.id.content_frame);
        String frameworkUpdateVersion = mRepoLoader.getFrameworkUpdateVersion();
        boolean moduleUpdateAvailable = mRepoLoader.hasModuleUpdates();

        Fragment currentFragment = getFragmentManager().findFragmentById(com.github.h.f.installer.R.id.content_frame);
        if (currentFragment instanceof DownloadDetailsFragment) {
            if (frameworkUpdateVersion != null) {
                Snackbar.make(parentLayout, com.github.h.f.installer.R.string.welcome_framework_update_available + " " + String.valueOf(frameworkUpdateVersion), Snackbar.LENGTH_LONG).show();
            }
        }

        boolean snackBar = FridaApp.getPreferences().getBoolean("snack_bar", true);

        if (moduleUpdateAvailable && snackBar) {
            Snackbar.make(parentLayout, com.github.h.f.installer.R.string.modules_updates_available, Snackbar.LENGTH_LONG).setAction(getString(com.github.h.f.installer.R.string.view), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switchFragment(2);
                }
            }).show();
        }
    }

    @Override
    public void onInstalledModulesReloaded(ModuleUtil moduleUtil) {
        notifyDataSetChanged();
    }

    @Override
    public void onSingleInstalledModuleReloaded(ModuleUtil moduleUtil, String packageName, InstalledModule module) {
        notifyDataSetChanged();
    }

    @Override
    public void onReloadDone(RepoLoader loader) {
        notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ModuleUtil.getInstance().removeListener(this);
        mRepoLoader.removeListener(this);
    }
}
