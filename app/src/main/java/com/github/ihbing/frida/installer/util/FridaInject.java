package com.github.ihbing.frida.installer.util;

import android.text.TextUtils;

import com.github.ihbing.frida.installer.FridaApp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FridaInject {
    public static final String TAG = "FridaInject";
    private ConcurrentHashMap<String,List<String>> injectPIDMap=new ConcurrentHashMap<>();
    //cmd::/data/user_de/0/com.github.ihbing.frida.installer/frida_inject  -n com.github.ihbing.frida.installer -s /data/user/0/com.github.ihbing.frida.installer/cache/active.js
    public boolean inject(final String processName, String jsPath, boolean reboot){
        if(!new File(FridaApp.FRIDA_INJECT_BIN).exists())return false;
        RootUtil rootUtil=new RootUtil();
        rootUtil.startShell();
        rootUtil.execute(String.format("%s -n %s -s %s", FridaApp.FRIDA_INJECT_BIN, processName, jsPath), null);
        rootUtil.execute("echo $!", new RootUtil.LineCallback() {
            @Override
            public void onLine(String line) {
                synchronized (injectPIDMap){
                    if(!TextUtils.isEmpty(line.trim())){
                        if(injectPIDMap.containsKey(processName)){

                        }
                    }
                }
            }

            @Override
            public void onErrorLine(String line) {

            }
        });
        return false;
    }
    public boolean uninject(){

        return false;
    }
}
