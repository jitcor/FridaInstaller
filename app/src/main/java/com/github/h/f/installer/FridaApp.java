package com.github.h.f.installer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Keep;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import com.github.h.f.installer.system.FileUtils;
import com.github.h.f.installer.util.AssetUtil;
import com.github.h.f.installer.util.DownloadsUtil;
import com.github.h.f.installer.util.FridaUtil;
import com.github.h.f.installer.util.InstallZipUtil;
import com.github.h.f.installer.util.InstallZipUtil.FridaProp;
import com.github.h.f.installer.util.NotificationUtil;
import com.github.h.f.installer.util.RepoLoader;

public class FridaApp extends Application implements ActivityLifecycleCallbacks {
    public static final String TAG = "FridaInstaller";

    @SuppressLint("SdCardPath")
    private static final String BASE_DIR_LEGACY = "/data/data/"+BuildConfig.APPLICATION_ID+"/";

    public static final String BASE_DIR = Build.VERSION.SDK_INT >= 24
            ? "/data/user_de/0/"+BuildConfig.APPLICATION_ID+"/" : BASE_DIR_LEGACY;

    public static final String ENABLED_MODULES_LIST_FILE = FridaApp.BASE_DIR + "conf/enabled_modules.list";

    private static final String[] Frida_PROP_FILES = new String[]{
//            "/su/frida/frida.prop", // official systemless
//            "/system/frida.prop",    // classical
            FridaApp.BASE_DIR + "conf/frida.prop",
    };
    public static final String FRIDA_INJECT_BIN=BASE_DIR+"/frida_inject";
    public static final String BASE_TMP_DIR="/data/local/tmp/finstaller/";
    public static final String SCRIPTS_DIR ="/data/data/"+BuildConfig.APPLICATION_ID+"/files/fs/";
    public static final String SCRIPTS_TMP_DIR =BASE_TMP_DIR+"/fs/";

    public static int WRITE_EXTERNAL_PERMISSION = 69;
    private static FridaApp mInstance = null;
    private static Thread mUiThread;
    private static Handler mMainHandler;
    private boolean mIsUiLoaded = false;
    private SharedPreferences mPref;
    private FridaProp mFridaProp;
    public static FridaApp getInstance() {
        return mInstance;
    }
    public static void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != mUiThread) {
            mMainHandler.post(action);
        } else {
            action.run();
        }
    }

    public static void postOnUiThread(Runnable action) {
        mMainHandler.post(action);
    }

    // This method is hooked by XposedBridge to return the current version
    @Keep
    public static boolean checkFridaActive() {
        return false;
    }

    public static int getInstalledFridaVersion() {
        FridaProp prop = getFridaProp();
        return prop != null ? prop.getVersionInt() : -1;
    }

    public static FridaProp getFridaProp() {
        synchronized (mInstance) {
            return mInstance.mFridaProp;
        }
    }
    ///data/user_de/0/com.github.humenger.frida.installer//frida_inject  -n com.github.humenger.frida.installer -s /data/user/0/com.github.humenger.frida.installer/cache/active.js &

    public static SharedPreferences getPreferences() {
        return mInstance.mPref;
    }

    public static void installApk(Context context, DownloadsUtil.DownloadInfo info) {
        Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID+".fileprovider", new File(info.localFilename));
            installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(new File(info.localFilename));
        }
        installIntent.setDataAndType(uri, DownloadsUtil.MIME_TYPE_APK);
        installIntent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.getApplicationInfo().packageName);
        context.startActivity(installIntent);
    }

    public static String getDownloadPath() {
        return getPreferences().getString("download_location", Environment.getExternalStorageDirectory() + "/FridaInstaller");
    }

    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mUiThread = Thread.currentThread();
        mMainHandler = new Handler();

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        reloadFridaProp();
        createDirectories();
        NotificationUtil.init();
        AssetUtil.removeBusybox();
        AssetUtil.extractFridaProp();
        FridaUtil.initJavaScriptPath();

        registerActivityLifecycleCallbacks(this);
    }

    private void createDirectories() {
        FileUtils.setPermissions(BASE_DIR, 00711, -1, -1);
        mkdirAndChmod("conf", 00771);
        mkdirAndChmod("log", 00777);

        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method deleteDir = FileUtils.class.getDeclaredMethod("deleteContentsAndDir", File.class);
                deleteDir.invoke(null, new File(BASE_DIR_LEGACY, "bin"));
                deleteDir.invoke(null, new File(BASE_DIR_LEGACY, "conf"));
                deleteDir.invoke(null, new File(BASE_DIR_LEGACY, "log"));
            } catch (Exception e) {
                Log.w(FridaApp.TAG, "Failed to delete obsolete directories", e);
            }
        }
    }

    private void mkdirAndChmod(String dir, int permissions) {
        dir = BASE_DIR + dir;
        new File(dir).mkdir();
        FileUtils.setPermissions(dir, permissions, -1, -1);
    }

    public void reloadFridaProp() {
        Log.i(FridaApp.TAG,"reloadFridaProp");
        FridaProp prop = null;

        for (String path : Frida_PROP_FILES) {
            File file = new File(path);
            if (file.canRead()) {
                FileInputStream is = null;
                try {
                    is = new FileInputStream(file);
                    prop = InstallZipUtil.parseFridaProp(is);
                    Log.d(FridaApp.TAG,"reloadFridaProp success");
                    break;
                } catch (IOException e) {
                    Log.e(FridaApp.TAG, "Could not read " + file.getPath(), e);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        }
        if(prop==null){
            Log.e(FridaApp.TAG, "Could not read prop file");
        }
        synchronized (this) {
            mFridaProp = prop;
        }
    }

    // TODO find a better way to trigger actions only when any UI is shown for the first time
    @Override
    public synchronized void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (mIsUiLoaded)
            return;

        RepoLoader.getInstance().triggerFirstLoadIfNecessary();
        mIsUiLoaded = true;
    }

    @Override
    public synchronized void onActivityResumed(Activity activity) {
    }

    @Override
    public synchronized void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
