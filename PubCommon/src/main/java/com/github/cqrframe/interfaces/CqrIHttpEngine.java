package com.github.cqrframe.interfaces;

import android.app.Application;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设计思想是使用接口对各模块解耦规范化，不强依赖某些明确的三方库，使得三方库可自由搭配组装。
 * <p>
 * 集成第三方HTTP框架（如：Kalle、okhttp-OkGo、Fast-Android-Networking、android-async-http、Volley），
 * <p>
 * https://github.com/jeasonlzy/okhttp-OkGo
 * https://github.com/yanzhenjie/Kalle
 * https://github.com/amitshekhariitbhu/Fast-Android-Networking
 * https://github.com/yanzhenjie/NoHttp
 * https://github.com/loopj/android-async-http
 * https://github.com/litesuits/android-lite-http
 * https://android.googlesource.com/platform/frameworks/volley
 * HttpRequest：https://github.com/Konloch/HTTPRequest
 * <p>
 * Created by liyujiang on 2016/12/31 15:37
 */
public interface CqrIHttpEngine {

    @MainThread
    void init(Application application);

    @MainThread
    UUID get(String urlOrPath, @Nullable HttpCallback callback);

    @MainThread
    UUID get(String urlOrPath, @Nullable HttpParams params, @Nullable HttpCallback callback);

    @MainThread
    UUID get(String urlOrPath, @Nullable HttpHeader header, @Nullable HttpParams params, @Nullable HttpCallback callback);

    @MainThread
    UUID post(String urlOrPath, @Nullable HttpParams params, HttpCallback callback);

    @MainThread
    UUID post(String urlOrPath, @Nullable HttpHeader header, @Nullable HttpParams params, HttpCallback callback);

    @MainThread
    UUID upload(String urlOrPath, List<File> files, HttpCallback callback);

    @MainThread
    UUID upload(String urlOrPath, HttpParams params, List<File> files, HttpCallback callback);

    @MainThread
    UUID upload(String urlOrPath, @Nullable HttpHeader header, @Nullable HttpParams params, List<File> files, HttpCallback callback);

    @MainThread
    UUID download(String url, final DownloadCallback callback);

    @MainThread
    void cancel(UUID uuid);

    /**
     * HTTP请求回调
     */
    abstract class HttpCallback<Result> {

        public void onStart() {
        }

        public abstract void onSuccess(Map<String, List<String>> headers, Result result);

        public abstract void onError(int code, Throwable throwable);

    }

    /**
     * 文件上传回调
     */
    abstract class UploadCallback extends HttpCallback<String> {

        public abstract void onProgress(long progress, long total);

    }

    /**
     * 下载回调
     */
    abstract class DownloadCallback extends HttpCallback<byte[]> {

        public abstract void onProgress(long progress, long total);

    }

    /**
     * HTTP请求参数
     */
    class HttpParams {
        private ConcurrentHashMap<String, String> map;

        public HttpParams() {
            map = new ConcurrentHashMap<>();
        }

        public void put(String key, String value) {
            if (value == null) {
                value = "";
            }
            if (!TextUtils.isEmpty(key)) {
                map.put(key, value);
            }
        }

        public void put(String key, int value) {
            put(key, String.valueOf(value));
        }

        public void put(String key, float value) {
            put(key, String.valueOf(value));
        }

        public void put(String key, double value) {
            put(key, String.valueOf(value));
        }

        public void put(String key, boolean value) {
            put(key, String.valueOf(value));
        }

        public void putAll(Map<String, String> params) {
            if (params != null && params.size() > 0) {
                map.putAll(params);
            }
        }

        public Map<String, String> getAll() {
            return map;
        }

        public void clearAll() {
            map.clear();
        }

        @NonNull
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (ConcurrentHashMap.Entry<String, String> entry : map.entrySet()) {
                if (result.length() > 0) {
                    result.append("&");
                }
                result.append(entry.getKey());
                result.append("=");
                result.append(entry.getValue());
            }
            return result.toString();
        }

    }

    /**
     * HTTP请求头
     */
    class HttpHeader extends HttpParams {
    }

}
