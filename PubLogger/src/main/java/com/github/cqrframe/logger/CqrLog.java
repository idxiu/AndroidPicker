package com.github.cqrframe.logger;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 调试日志打印，基于 https://github.com/fodroid/XDroid-Base 的log模块修改
 * <p>
 * Created by wanglei on 2016/11/29.
 */
@SuppressWarnings("WeakerAccess")
public class CqrLog {
    public static final int METHOD_COUNT = 2; // show method count in trace
    public static boolean LOG_ENABLE = true;
    public static String LOG_TAG = CqrLog.class.getSimpleName();
    protected static final int DEBUG = 3;
    protected static final int WARN = 5;
    protected static final int ERROR = 6;

    private CqrLog() {
    }

    public static void json(String json) {
        if (LOG_ENABLE) {
            String msg = CqrFormatter.formatJson(json);
            msg = wrapLog(msg);
            String formatJson = CqrFormatter.formatBorder(new String[]{msg});
            CqrPrinter.println(DEBUG, LOG_TAG, formatJson);
        }
    }

    public static void xml(String xml) {
        if (LOG_ENABLE) {
            String msg = CqrFormatter.formatXml(xml);
            msg = wrapLog(msg);
            String formatXml = CqrFormatter.formatBorder(new String[]{msg});
            CqrPrinter.println(DEBUG, LOG_TAG, formatXml);
        }
    }

    public static void error(Throwable throwable) {
        if (LOG_ENABLE) {
            String msg = CqrFormatter.formatString(throwable);
            msg = wrapLog(msg);
            String formatError = CqrFormatter.formatBorder(new String[]{msg});
            CqrPrinter.println(ERROR, LOG_TAG, formatError);
        }
    }

    public static void warn(Object msg, Object... args) {
        msg(WARN, LOG_TAG, msg, args);
    }

    public static void debug(Object msg, Object... args) {
        msg(DEBUG, LOG_TAG, msg, args);
    }

    private static void msg(int logLevel, String tag, Object msg, Object... args) {
        if (LOG_ENABLE) {
            String formatMsg;
            String msgStr = wrapLog(msg);
            if (args == null || args.length == 0) {
                formatMsg = CqrFormatter.formatBorder(new String[]{msgStr});
            } else {
                formatMsg = CqrFormatter.formatBorder(new String[]{CqrFormatter.formatArgs(msgStr, args)});
            }
            CqrPrinter.println(logLevel, TextUtils.isEmpty(tag) ? LOG_TAG : tag, formatMsg);
        }
    }

    private static String wrapLog(Object msg) {
        return CqrFormatter.formatString(msg) + getTraceElement();
    }

    /**
     * 可显示调用方法所在的文件行号，在AndroidStudio的logcat处可点击定位。
     * 此方法参考：https://github.com/orhanobut/logger
     */
    private static String getTraceElement() {
        try {
            int methodCount = METHOD_COUNT + 1;
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            int stackOffset = _getStackOffset(trace);
            //corresponding method count with the current stack may exceeds the stack trace. Trims the count
            if (methodCount + stackOffset > trace.length) {
                methodCount = trace.length - stackOffset - 1;
            }
            ArrayList<StackTraceElement> showTraces = new ArrayList<>();
            for (int i = methodCount; i > 1; i--) {
                int stackIndex = i + stackOffset;
                if (stackIndex >= trace.length) {
                    continue;
                }
                showTraces.add(trace[stackIndex]);
            }
            Collections.reverse(showTraces);
            StringBuilder builder = new StringBuilder();
            String level = "    ";
            for (StackTraceElement showTrace : showTraces) {
                builder.append("\n")
                        .append(level)
                        .append(_getSimpleClassName(showTrace.getClassName()))
                        .append(".")
                        .append(showTrace.getMethodName())
                        .append(" ");
                String fileName = showTrace.getFileName();
                if (!TextUtils.isEmpty(fileName)) {
                    builder.append("(")
                            .append(fileName)
                            .append(":")
                            .append(showTrace.getLineNumber())
                            .append(")");
                }
                level += "    ";
            }
            return builder.toString();
        } catch (Exception ignore) {
            return "";
        }
    }

    /**
     * Determines the starting index of the stack trace, after method calls made by this class.
     *
     * @param trace the stack trace
     * @return the stack offset
     */
    private static int _getStackOffset(StackTraceElement[] trace) {
        final int MIN_STACK_OFFSET = 3;// starts at this class after two native calls
        for (int i = MIN_STACK_OFFSET; i < trace.length; i++) {
            StackTraceElement e = trace[i];
            String name = e.getClassName();
            if (!name.equals(CqrLog.class.getName())) {
                return --i;
            }
        }
        return -1;
    }

    private static String _getSimpleClassName(String name) {
        int lastIndex = name.lastIndexOf(".");
        return name.substring(lastIndex + 1);
    }

}
