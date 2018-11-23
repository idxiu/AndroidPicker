package com.github.cqrframe.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.github.cqrframe.logger.CqrLog;
import com.github.cqrframe.toolkit.CqrJsonUtils;

/**
 * JavaBean基类，为了可以通过反射序列化，故不允许混淆其子类
 * <p>
 * Created by liyujiang on 2014-04-23 16:14
 */
@SuppressWarnings("WeakerAccess")
public abstract class CqrJavaBean extends CqrBaseModel {

    protected String obtainFile() {
        return "java_bean";
    }

    protected String obtainSuffix() {
        return "";
    }

    protected String obtainKey() {
        String suffix = obtainSuffix();
        String key = getClass().getSimpleName().toLowerCase();
        if (TextUtils.isEmpty(suffix)) {
            return key;
        }
        return suffix + "_" + key;
    }

    public void writeToPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(obtainFile(), Context.MODE_PRIVATE);
        String json = CqrJsonUtils.toJsonNoThrow(this);
        preferences.edit().putString(obtainKey(), json).apply();
    }

    @Nullable
    public <T> T readFromPreferences(Context context) {
        Class<?> aClass = getClass();
        SharedPreferences preferences = context.getSharedPreferences(obtainFile(), Context.MODE_PRIVATE);
        String key = obtainKey();
        String json = preferences.getString(key, "");
        CqrLog.debug(aClass.getSimpleName() + "-----" + key + "-----" + json);
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        //noinspection unchecked
        return (T) CqrJsonUtils.fromJsonNoThrow(json, aClass);
    }

    public void removeFromPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(obtainFile(), Context.MODE_PRIVATE);
        preferences.edit().remove(obtainKey()).apply();
    }

    public String toJSONString() {
        return CqrJsonUtils.toJsonNoThrow(this);
    }

    @NonNull
    @Override
    public String toString() {
        return toJSONString();
    }

}