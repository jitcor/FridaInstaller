package com.github.humenger.frida.installer.system;

import android.os.Build;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    public static final String TAG = "FileUtils";
    public static int setPermissions(String path, int mode, int uid, int gid) {
       return android.os.FileUtils.setPermissions(path,mode,uid,gid);
    }

    public static boolean copyFile(File inFile, File targetFile) {
        return android.os.FileUtils.copyFile(inFile,targetFile);
//        throw new RuntimeException("not implement FileUtils.copyFile");
    }
}
