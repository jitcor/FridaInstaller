package com.github.ihbing.frida.installer.util;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Util {
    public static final String TAG = "Util";
    /**
     * 获取重定向地址
     */
    public static String getRedirectUrl(String url) {
        try{
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(5000);
            return conn.getHeaderField("Location");
        }catch (IOException e){
            return url;
        }

    }
    public static void makeSureFileExist(File file){
        if(file.exists())return;
        if (file.getParentFile().mkdirs()){
            try {
                file.createNewFile();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
