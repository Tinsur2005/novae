package cn.tinsur.mall.util;

import java.util.Map;

public class LoginContext {
    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    public static void setLoginInfo(Map<String, Object> loginInfo) {
        //THREAD_LOCAL.set(Thread.currentThread(), loginInfo);
        THREAD_LOCAL.set(loginInfo);
    }

    public static Map<String, Object> getLoginInfo() {
        return THREAD_LOCAL.get();
    }

    public static void removeLoginInfo() {
        THREAD_LOCAL.remove();
    }
}