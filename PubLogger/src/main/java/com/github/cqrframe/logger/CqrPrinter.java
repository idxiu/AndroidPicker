package com.github.cqrframe.logger;

/**
 * 突破LogCat字数限制
 * <p>
 * Created by wanglei on 2016/11/29.
 */
class CqrPrinter {
    private static final int MAX_LENGTH_OF_SINGLE_MESSAGE = 2048;//3500

    static void println(int logLevel, String tag, String msg) {
        if (msg.length() <= MAX_LENGTH_OF_SINGLE_MESSAGE) {
            printChunk(logLevel, tag, msg);
            return;
        }
        int msgLength = msg.length();
        int start = 0;
        int end = start + MAX_LENGTH_OF_SINGLE_MESSAGE;
        while (start < msgLength) {
            printChunk(logLevel, tag, msg.substring(start, end));
            start = end;
            end = Math.min(start + MAX_LENGTH_OF_SINGLE_MESSAGE, msgLength);
        }
    }

    private static void printChunk(int logLevel, String tag, String msg) {
        // 由于不少手机默认禁用了verbose、debug等级别的日志，
        // 为了所有机型能看到日志，使用WARN级别及ERROR级别
        if (logLevel == CqrLog.ERROR) {
            android.util.Log.println(android.util.Log.ERROR, tag, msg);
        } else {
            android.util.Log.println(android.util.Log.WARN, tag, msg);
        }
    }

}
