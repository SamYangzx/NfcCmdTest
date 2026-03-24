package com.example.gg.nfcproject.util;

import android.os.Build;
import android.util.Log;


public class LogUtil {
    public static final boolean DBG = true;

    public static void v(String tag, String msg) {
        if (DBG) {
            Log.v(tag, buildLogMsg(msg));
        }
    }

    public static void d(String tag, String msg) {
        if (DBG) {
            Log.d(tag, buildLogMsg(msg));
        }
    }

    public static void i(String tag, String msg) {
        if (DBG) {
            Log.i(tag, buildLogMsg(msg));
        }
    }

    public static void w(String tag, String msg) {
        if (DBG) {
            Log.w(tag, buildLogMsg(msg));
        }
    }

    public static void e(String tag, String msg) {
        if (DBG) {
            Log.e(tag, buildLogMsg(msg));
        }
    }


    /**
     * 只额外要函数名+代码行数做快速定位
     * @return 带函数名，代码行数 log
     */
    private static String buildLogMsg(String msg) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[4 /*若封装了函数为4，直接写在LogUtil.d中为3 */];

        return String.format("[%s:%d]  %s",
                element.getMethodName(),
                element.getLineNumber(),
                msg
        );
    }
}