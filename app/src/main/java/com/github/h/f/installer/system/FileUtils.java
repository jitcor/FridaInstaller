package com.github.h.f.installer.system;

import java.io.File;

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
