package com.github.cqrframe.model;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 数据模型基类。
 * <p>
 * 用可以是数据表实体或者网络数据实体，数据表实体不能混淆，防止未注解时不能通过ORM方式正常操作表
 * <p>
 * Created by liyujiang on 2018/9/17 0:41
 */
public abstract class CqrBaseModel implements Serializable {
    private static final long serialVersionUID = -6111323241670458039L;

    /**
     * 反射出所有字段值，组装成JSON字符串，该方法主要用于打印日志以便调试
     */
    @NonNull
    @Override
    public String toString() {
        Class<?> clazz = getClass();
        List<Field> list = new ArrayList<>();//得到自身的所有字段
        Collections.addAll(list, clazz.getDeclaredFields());
        while (clazz != null && clazz != Object.class) {
            clazz = clazz.getSuperclass();//得到继承自父类的字段
            if (clazz != null) {
                Collections.addAll(list, clazz.getDeclaredFields());
            }
        }
        Field[] arr = new Field[list.size()];
        Field[] fields = list.toArray(arr);
        StringBuilder sb = new StringBuilder();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equalsIgnoreCase("serialVersionUID") ||
                    fieldName.equalsIgnoreCase("shadow$_monitor_") ||
                    fieldName.equalsIgnoreCase("shadow$_klass_")) {
                continue;//忽略指定字段
            }
            sb.append("\"");
            sb.append(fieldName);
            sb.append("\"");
            try {
                field.setAccessible(true);
                Object obj = field.get(this);
                if (obj instanceof String) {
                    sb.append(":\"");
                    sb.append(obj);
                    sb.append("\",");
                } else {
                    sb.append(":");
                    sb.append(obj);
                    sb.append(",");
                }
            } catch (IllegalAccessException e) {
                sb.append(":\"");
                sb.append(e.toString());
                sb.append("\",");
            }
        }
        int idx = sb.lastIndexOf(",");
        if (idx != -1) {
            sb.deleteCharAt(idx);
        }
        return "{" + sb.toString() + "}";
    }

}
