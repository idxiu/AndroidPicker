package com.github.cqrframe.toolkit;

import android.support.annotation.Nullable;

import com.github.cqrframe.logger.CqrLog;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * 反射工具类，参阅 https://github.com/scwang90/AndFrameWorks 的AfReflecter类
 * <p>
 * 对非SDK接口的限制，参阅 https://developer.android.google.cn/about/versions/pie/restrictions-non-sdk-interfaces
 * 限制名单参见 https://android.googlesource.com/platform/prebuilts/runtime/+/master/appcompat
 * 通过 Class.getDeclaredField() 或 Class.getField() 反射，引发 NoSuchFieldException
 * 通过 Class.getDeclaredMethod() 或 Class.getMethod() 反射，引发 NoSuchMethodException
 * 通过 Class.getDeclaredFields() 或 Class.getFields() 反射，结果中未出现非 SDK 成员
 * 通过 Class.getDeclaredMethods() 或 Class.getMethods() 反射，结果中未出现非 SDK 成员
 * <p>
 * Created by liyujiang on 2014-4-18.
 */
@SuppressWarnings("WeakerAccess")
public class CqrReflectUtils {

    protected CqrReflectUtils() {
        throw new UnsupportedOperationException("You can't instantiate me");
    }

    /**
     * 实例化
     *
     * @param clazz          需要实例化的Class
     * @param parameterTypes 参数的字节码类型
     * @param args           参数列表
     * @return T
     */
    @Nullable
    public static <T> T newInstance(Class<T> clazz, Class<?>[] parameterTypes, Object... args) {
        T instance = null;
        try {
            if (parameterTypes == null) {
                parameterTypes = new Class<?>[]{};
            }
            if (args == null) {
                args = new Object[]{};
            }
            Constructor<T> constructor = clazz.getConstructor(parameterTypes);
            try {
                instance = constructor.newInstance(args);//有构造函数
            } catch (Throwable e) {
                CqrLog.error(e);
            }
        } catch (NoSuchMethodException e) {
            CqrLog.debug(e.toString());
            try {
                instance = clazz.newInstance();
            } catch (Throwable e2) {
                CqrLog.error(e2);
            }
        }
        return instance;
    }

    /**
     * 为 className 创建实例
     *
     * @param className 类路径
     * @param <T>       模板参数
     * @return 新的实例 失败会 null （很少会失败）
     */
    @Nullable
    public static <T> T newInstance(String className) {
        try {
            Class<?> aClass = Class.forName(className);
            //noinspection unchecked
            return newInstance((Class<T>) aClass);
        } catch (ClassNotFoundException e) {
            CqrLog.error(e);
            return null;
        }
    }

    /**
     * 为 type 创建实例
     *
     * @param type 类型
     * @param <T>  模板参数
     * @return 新的实例 失败会 null （很少会失败）
     */
    @Nullable
    public static <T> T newInstance(Class<T> type) {
        try {
            return type.newInstance();
        } catch (Throwable e) {
            CqrLog.debug(e.toString());
        }
        return null;
    }

    /**
     * 调用方法
     *
     * @param handler        方法所在的对象
     * @param methodName     方法名称
     * @param parameterTypes 参数的字节码类型
     * @param args           参数列表
     * @return object 调用方法返回的结果
     */
    public static Object invokeMethod(Object handler, String methodName, Class<?>[] parameterTypes, Object... args) {
        CqrLog.debug("invoke: " + handler + "." + methodName);
        if (handler == null || methodName == null) {
            return null;
        }
        //noinspection TryWithIdenticalCatches
        try {
            if (parameterTypes == null) {
                parameterTypes = new Class[0];
            }
            Method method = handler.getClass().getMethod(methodName, parameterTypes);
            return method.invoke(handler, args);
        } catch (NoSuchMethodException e) {
            CqrLog.error(e);
        } catch (IllegalAccessException e) {
            CqrLog.error(e);
        } catch (IllegalArgumentException e) {
            CqrLog.error(e);
        } catch (InvocationTargetException e) {
            CqrLog.error(e);
        }
        return null;
    }

    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = clazz.getMethod(methodName, parameterTypes);
            return method.invoke(clazz, args);
        } catch (Throwable e) {
            CqrLog.error(e);
        }
        return null;
    }

    public static Method getFieldGetMethod(Class<?> clazz, Field f) {
        String fn = f.getName();
        Method m = null;
        if (f.getType() == boolean.class) {
            m = getBooleanFieldGetMethod(clazz, fn);
        }
        if (m == null) {
            m = getFieldGetMethod(clazz, fn);
        }
        return m;
    }

    public static Method getFieldGetMethod(Class<?> clazz, String fieldName) {
        String mn = "get" + fieldName.substring(0, 1).toUpperCase()
                + fieldName.substring(1);
        try {
            return clazz.getDeclaredMethod(mn);
        } catch (NoSuchMethodException e) {
            CqrLog.debug(e.toString());
            ;
            return null;
        }
    }

    public static Method getBooleanFieldGetMethod(Class<?> clazz, String fieldName) {
        String mn = "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        if (startsWithIs(fieldName)) {
            mn = fieldName;
        }
        try {
            return clazz.getDeclaredMethod(mn);
        } catch (NoSuchMethodException e) {
            CqrLog.debug(e.toString());
            return null;
        }
    }

    public static Method getFieldSetMethod(Class<?> clazz, Field f) {
        String fn = f.getName();
        String mn = "set" + fn.substring(0, 1).toUpperCase() + fn.substring(1);
        try {
            return clazz.getDeclaredMethod(mn, f.getType());
        } catch (NoSuchMethodException e) {
            if (f.getType() == boolean.class) {
                return getBooleanFieldSetMethod(clazz, f);
            }
        }
        return null;
    }

    public static Method getBooleanFieldSetMethod(Class<?> clazz, Field f) {
        String fn = f.getName();
        String mn = "set" + fn.substring(0, 1).toUpperCase() + fn.substring(1);
        if (startsWithIs(f.getName())) {
            mn = "set" + fn.substring(2, 3).toUpperCase() + fn.substring(3);
        }
        try {
            return clazz.getDeclaredMethod(mn, f.getType());
        } catch (NoSuchMethodException e) {
            CqrLog.debug(e.toString());
            return null;
        }
    }

    private static boolean startsWithIs(String fieldName) {
        if (fieldName == null || fieldName.trim().length() == 0)
            return false;
        // is开头，并且is之后第一个字母是大写 比如 isAdmin
        return fieldName.startsWith("is") && !Character.isLowerCase(fieldName.charAt(2));
    }

    /**
     * 直接读取对象属性值,无视private/protected修饰符,不经过getter函数
     *
     * @param className the class name
     * @param fieldName the field name
     * @return field value by name
     */
    public static Object getFieldValueByName(String className, String fieldName) {
        try {
            Class clazz = Class.forName(className);
            return getFieldValueByClass(clazz, fieldName);
        } catch (ClassNotFoundException e) {
            CqrLog.debug(e.toString());
        }
        return null;
    }

    /**
     * 直接读取对象属性值,无视private/protected修饰符,不经过getter函数
     *
     * @param clazz     the clazz
     * @param fieldName the field name
     * @return field value by class
     */
    public static Object getFieldValueByClass(Class<?> clazz, String fieldName) {
        try {
            return getFieldValueByObject(clazz.newInstance(), fieldName);
        } catch (Exception e) {
            CqrLog.debug(e.toString());
        }
        return null;
    }

    /**
     * 直接读取对象属性值,无视private/protected修饰符,不经过getter函数
     */
    public static Object getFieldValueByObject(Object object, String fieldName) {
        Field field = getDeclaredField(object.getClass(), fieldName);
        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");
        }
        makeAccessible(field);
        Object result = null;
        try {
            result = field.get(object);
        } catch (IllegalAccessException e) {
            CqrLog.error(e);
        }
        return result;
    }

    /**
     * 直接设置对象属性值,无视private/protected修饰符,不经过setter函数.
     */
    public static void setFieldValueByObject(Object object, String fieldName, Object value) {
        Field field = getDeclaredField(object.getClass(), fieldName);
        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");
        }
        makeAccessible(field);
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            CqrLog.error(e);
        }
    }

    /**
     * 直接设置静态属性值,无视private/protected修饰符（不过final是无法设置的）,不经过setter函数.
     */
    public static void setStaticFieldValue(Class<?> clazz, String fieldName, Object value) {
        Field field = getDeclaredField(clazz, fieldName);
        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + clazz.getName() + "]");
        }
        makeAccessible(field);
        try {
            field.set(null, value);
        } catch (IllegalAccessException e) {
            CqrLog.error(e);
        }
    }

    /**
     * 循环向上转型获取类的所有字段（包括继承自父类的）
     */
    public static Field[] getDeclaredFields(Class clazz) {
        List<Field> list = Arrays.asList(clazz.getDeclaredFields());//得到自身的所有字段
        while (clazz != null && clazz != Object.class) {
            clazz = clazz.getSuperclass();//得到继承自父类的字段
            if (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    int modifier = field.getModifiers();
                    if (Modifier.isPublic(modifier) || Modifier.isProtected(modifier)) {
                        list.add(field);
                    }
                }
            }
        }
        Field[] a = new Field[list.size()];
        return list.toArray(a);
    }

    /**
     * 循环向上转型获取类的所有字段（包括继承自父类的）
     *
     * @param className the class name
     * @param fieldName the field name
     * @return the declared field
     */
    public static Field getDeclaredField(String className, String fieldName) {
        try {
            Class clazz = Class.forName(className);
            return getDeclaredField(clazz, fieldName);
        } catch (ClassNotFoundException e) {
            CqrLog.debug(e);
        }
        return null;
    }

    /**
     * 循环向上转型获取类的所有字段（包括继承自父类的）
     */
    public static Field getDeclaredField(Class clazz, String fieldName) {
        //优先getDeclaredField再getField
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                // Field不在当前类定义,继续向父类转型
                clazz = clazz.getSuperclass();
            }
        }
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getField(fieldName);
            } catch (NoSuchFieldException e) {
                // Field不在当前类定义,继续向父类转型
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 强制转换{@link Field}可访问.
     */
    public static void makeAccessible(Field field) {
        if (!Modifier.isPublic(field.getModifiers()) ||
                !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
            field.setAccessible(true);
        }
    }

    /**
     * 强制转换{@link Method}可访问.
     */
    public static void makeAccessible(Method method) {
        if (!Modifier.isPublic(method.getModifiers()) ||
                !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
            method.setAccessible(true);
        }
    }

}
