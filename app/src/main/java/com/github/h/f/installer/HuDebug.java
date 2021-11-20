package com.github.h.f.installer;

public class HuDebug {
    public static final String TAG = "HuDebug";
    public interface Func{
        void run();
    }
    public static void invertCode(boolean enable ,Func func){
        if (!enable||func==null)return;
            func.run();
    }
    public static void replaceCode(boolean enable ,Func target,Func original){
        if(original==null)return;
        if (!enable) {
            original.run();
        }else if (target!=null){
            target.run();
        }

    }

}
