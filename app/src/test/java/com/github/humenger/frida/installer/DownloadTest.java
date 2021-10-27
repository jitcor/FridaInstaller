package com.github.humenger.frida.installer;

import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTest {
    public static final String TAG = "DownloadTest";
    @Test
    public void test(){
        try {
            System.out.println("Url:"+getRedirectUrl("https://github.com/frida/frida/releases/download/12.0.0/frida-inject-12.0.0-android-arm64.xz"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取重定向地址
     */
    public static String getRedirectUrl(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(5000);
        return conn.getHeaderField("Location");
    }

}
