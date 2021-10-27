package com.github.ihbing.frida.installer.installation;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.FileUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.github.ihbing.frida.installer.BuildConfig;
import com.github.ihbing.frida.installer.FridaApp;
import com.github.ihbing.frida.installer.util.AssetUtil;
import com.github.ihbing.frida.installer.util.FrameworkZips;
import com.github.ihbing.frida.installer.util.InstallZipUtil.ZipCheckResult;
import com.github.ihbing.frida.installer.util.RootUtil;
import com.github.ihbing.frida.installer.util.Util;
import com.github.ihbing.frida.installer.util.XZipUtil;

import org.apache.commons.compress.utils.FileNameUtils;

import static com.github.ihbing.frida.installer.util.InstallZipUtil.closeSilently;
import static com.github.ihbing.frida.installer.util.InstallZipUtil.triggerError;
import static com.github.ihbing.frida.installer.util.RootUtil.getShellPath;

public class FlashDirectly extends Flashable {
    private final boolean mSystemless;

    public FlashDirectly(File zipPath, FrameworkZips.Type type, String title, boolean systemless) {
        super(zipPath, type, title);
        mSystemless = systemless;
    }

    public void flash(Context context, final FlashCallback callback) {
        File targetFile = new File(FridaApp.FRIDA_INJECT_BIN);
        final RootUtil rootUtil = new RootUtil();
        if (!rootUtil.startShell(callback)) {
            return;
        }
        try {
            String outPath = XZipUtil.deCompress(mZipPath, true);
            File outFile = new File(outPath);
            Util.makeSureFileExist(targetFile);
            if (!FileUtils.copyFile(outFile, targetFile)) {
                triggerError(callback, FlashCallback.ERROR_COPY_FILE);
                return;
            }
            callback.onLine("Unzip success:" + targetFile.getAbsolutePath());
        } catch (IOException ioException) {
            ioException.printStackTrace();
            triggerError(callback, FlashCallback.ERROR_INVALID_ZIP);
            return;
        }
//        ZipCheckResult zipCheck = openAndCheckZip(callback);
//        if (zipCheck == null) {
//            return;
//        }
//        // Do additional checks.
//        ZipFile zip = zipCheck.getZip();
//        if (!zipCheck.isFlashableInApp()) {
//            triggerError(callback, FlashCallback.ERROR_NOT_FLASHABLE_IN_APP);
//            closeSilently(zip);
//            return;
//        }
//
//        // Extract update-binary.
//        ZipEntry entry = zip.getEntry("META-INF/com/google/android/update-binary");
//        File updateBinaryFile = new File(FridaApp.getInstance().getCacheDir(), "update-binary");
//        try {
//            AssetUtil.writeStreamToFile(zip.getInputStream(entry), updateBinaryFile, 0700);
//        } catch (IOException e) {
//            Log.e(FridaApp.TAG, "Could not extract update-binary", e);
//            triggerError(callback, FlashCallback.ERROR_INVALID_ZIP);
//            return;
//        } finally {
//            closeSilently(zip);
//        }

        // Execute the flash commands.


        callback.onStarted();

        rootUtil.execute("export NO_UIPRINT=1", callback);
        if (mSystemless) {
            rootUtil.execute("export SYSTEMLESS=1", callback);
        }

        int result = rootUtil.execute("chmod 755 " + targetFile.getAbsolutePath(), callback);
        if (result != FlashCallback.OK) {
            triggerError(callback, result);
            return;
        }
        AssetUtil.extractActiveJs();

        FridaApp.getInstance().reloadFridaProp();
        String cmd=FridaApp.FRIDA_INJECT_BIN+"  -n "+ BuildConfig.APPLICATION_ID+" -s " + AssetUtil.ACTIVE_JS_FILE+" &";
        Log.i(FridaApp.TAG,"cmd:"+cmd);
        rootUtil.execute(cmd, callback);
        SystemClock.sleep(1000);//Wait Frida inject active.js success
        callback.onDone();
    }

    public static final Parcelable.Creator<FlashDirectly> CREATOR
            = new Parcelable.Creator<FlashDirectly>() {
        @Override
        public FlashDirectly createFromParcel(Parcel in) {
            return new FlashDirectly(in);
        }

        @Override
        public FlashDirectly[] newArray(int size) {
            return new FlashDirectly[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(mSystemless ? 1 : 0);
    }

    protected FlashDirectly(Parcel in) {
        super(in);
        mSystemless = in.readInt() == 1;
    }
}
