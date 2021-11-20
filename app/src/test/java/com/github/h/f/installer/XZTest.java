package com.github.h.f.installer;

import com.github.h.f.installer.util.XZipUtil;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class XZTest {
    public static final String TAG = "XZTest";
    @Test
    public void testXZ(){
        try {
            XZipUtil.deCompress(new File("D:\\Project\\Android\\4.1\\FridaInstaller\\app\\build\\outputs\\apk\\debug\\frida-inject-12.0.0-android-arm64.xz"),false);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
