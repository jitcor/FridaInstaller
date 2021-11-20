package com.github.h.f.installer.util;

import android.text.TextUtils;

import com.github.h.f.installer.FridaApp;

import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class FridaUtil {
    public static final String TAG = "FridaUtil";
    public static final String APP_LIST_PATH = FridaApp.SCRIPTS_DIR + "/app.list";

    public static boolean checkFridaActive() {

        return true;
    }

    //https://github.com/frida/frida/releases/download/{version}/frida-inject-{version}-android-{arch}.xz
    public static void downloadFridaServer() {

    }

    public static void initJavaScriptPath() {
        File path = new File(FridaApp.SCRIPTS_DIR);
        File appListPath = new File(APP_LIST_PATH);
        if (!path.exists()) {
            if (!path.mkdirs()) {
                return;
            }
        }
//        FileUtils.setPermissions(path.getAbsolutePath(),00644,-1,-1);
        path.setReadable(true, false);
        if (!appListPath.exists()) {
            try {
                if (appListPath.createNewFile()) {
//                   FileUtils.setPermissions(appListPath.getAbsolutePath(),00644,-1,-1);
                    appListPath.setReadable(true, false);
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public static boolean installJavascript(String name, String code, List<String> processes) {
        String suffix = "" ;
        File jsPath = new File(FridaApp.SCRIPTS_DIR, String.format("%s%s.js", name, suffix));
        File jsConfigPath = new File(FridaApp.SCRIPTS_DIR, String.format("%s%s.config", name, suffix));
        try {
            String processesText = toString(processes);
            if (TextUtils.isEmpty(processesText)) {
                return false;
            }
            if (TextUtils.isEmpty(code)) {
                return false;
            }
            if (!jsPath.exists()) {
                if (!jsPath.createNewFile()) {
                    return false;
                }
//                FileUtils.setPermissions(jsPath.getAbsolutePath(),00644,-1,-1);
                jsPath.setReadable(true, false);
            }
            if (!jsConfigPath.exists()) {
                if (!jsConfigPath.createNewFile()) {
                    return false;
                }
//                FileUtils.setPermissions(jsPath.getAbsolutePath(),00644,-1,-1);
                jsConfigPath.setReadable(true, false);
            }
             String config=buildConfig(processes);
            IOUtils.copy(new ByteArrayInputStream(processesText.getBytes()), new FileOutputStream(APP_LIST_PATH,true));
            IOUtils.copy(new ByteArrayInputStream(code.getBytes()), new FileOutputStream(jsPath));
            IOUtils.copy(new ByteArrayInputStream(config.getBytes()), new FileOutputStream(jsConfigPath));
            delAppListRepeat();
            delDirWithRoot(FridaApp.SCRIPTS_TMP_DIR);
            copyDirWithRoot(FridaApp.SCRIPTS_DIR,FridaApp.BASE_TMP_DIR);
            chmodRoot(FridaApp.BASE_TMP_DIR,true,777);
            return true;
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return false;
    }

    private static void delAppListRepeat() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(APP_LIST_PATH)));

        String str = null;
        StringBuilder builder=new StringBuilder();
        while((str = bufferedReader.readLine()) != null)
        {
            if(builder.indexOf(str)==-1){
                builder.append(str);
                builder.append("\n");
            }
        }
        bufferedReader.close();
        IOUtils.copy(new ByteArrayInputStream(builder.toString().getBytes()), new FileOutputStream(APP_LIST_PATH));
    }

    public static void copyDirWithRoot(String source,String target){
        File sourceFile=new File(source);
        File targetFile=new File(target);
        if(!sourceFile.exists())return;
        if(!targetFile.exists()){
            Shell.SU.run("mkdir -p "+targetFile.getAbsolutePath());
        }
        Shell.SU.run("cp -fr "+sourceFile.getAbsolutePath()+" "+targetFile.getAbsolutePath());
    }
    public static void chmodRoot(String path,boolean R,int mode){
        Shell.SU.run("chmod "+(R?"-R":"")+" "+mode+" "+path);
    }
    public static void copyDirWithRootAll(String source,String target){
        File sourceFile=new File(source);
        File targetFile=new File(target);
        File prentFile=targetFile.getParentFile();
        if(!sourceFile.exists())return;
        if(!prentFile.exists()){
            Shell.SU.run("mkdir -r "+prentFile.getAbsolutePath());
        }
        Shell.SU.run("cp -frp "+sourceFile.getAbsolutePath()+" "+targetFile.getAbsolutePath());
    }
    public static void delDirWithRoot(String path){
        Shell.SU.run("rm -rf "+path);
    }

    private static String buildConfig(List<String> processes) {
        String prefix="{\n" +
                "  \"filter\": {\n" +
                "    \"executables\": [";
        String suffix="],\n" +
                "    \"bundles\": [],\n" +
                "    \"objc_classes\": []\n" +
                "  }\n" +
                "}\n";
        StringBuilder content=new StringBuilder();
        for (String process:processes){
            content.append("\"");
            content.append(process);
            content.append("\",");
        }
        content.deleteCharAt(content.length()-1);
        return prefix+content.toString()+suffix;
    }

    public static boolean installJavascript(String name, String code, String[] processes) {
        return installJavascript(name, code, Arrays.asList(processes));
    }

    public static boolean installJavascript(String name, String code, String process) {
        return installJavascript(name, code, new String[]{process});
    }

    private static String toString(List<String> processes) {
        if (processes == null || processes.size() <= 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String process : processes) {
            builder.append(process);
            builder.append("\n");
        }
        return builder.toString();
    }

}





























