package com.github.cqrframe.toolkit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.annotation.WorkerThread;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.pm.PackageInfoCompat;

import com.github.cqrframe.logger.CqrLog;

import java.io.File;
import java.util.List;

/**
 * 安卓程序包工具类
 * <p>
 * Created by liyujiang on 2014-4-18
 */
public class CqrPackageUtils {

    protected CqrPackageUtils() {
        throw new UnsupportedOperationException("You can't instantiate me");
    }

    /**
     * 是否允许通知栏展示通知
     */
    public static boolean isNotificationAllowed(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /**
     * 跳转到应用权限设置页面
     */
    public static void startPermissionSettings(Context context) {
        CqrPermission.Setting.start(context, true);
    }

    /**
     * 启动当前应用设置页面
     */
    public static void startAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getApplicationContext().getPackageName()));
        context.startActivity(intent);
    }

    /**
     * 启动当前应用设置页面
     *
     * @see CqrActivityResult#startAppDetailsSettings(FragmentActivity, CqrActivityResult.OnActivityResultListener)
     */
    public static void startAppSettings(Activity activity, int requestCode) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + activity.getApplicationContext().getPackageName()));
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 跳转至“安装未知应用”权限界面，引导用户开启权限
     *
     * @see CqrActivityResult#startAppUnknownSources(FragmentActivity, CqrActivityResult.OnActivityResultListener)
     */
    @TargetApi(Build.VERSION_CODES.O)
    public static void startAppUnknownSources(Activity activity, int requestCode) {
        Uri selfPackageUri = Uri.parse("package:" + activity.getApplicationContext().getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, selfPackageUri);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 描述: 打开App
     *
     * @param packageName 包名
     */
    public static void startApp(Context context, String packageName) {
        if (CqrStringUtils.isEmpty(packageName)) {
            return;
        }
        context.startActivity(context.getPackageManager().getLaunchIntentForPackage(packageName));
    }

    /**
     * 重启本应用
     *
     * @param context the context
     */
    public static void restartApp(Context context) {
        //必须调用getApplicationContext()才能获得正确的包名
        String packageName = context.getApplicationContext().getPackageName();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 判断传入Intent的Uri是否有效
     */
    public static boolean isValidIntentUri(Context context, Uri uri) {
        try {
            PackageManager packageManager = context.getPackageManager();
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
            return activities.size() > 0;
        } catch (Throwable ignore) {
            return false;
        }
    }

    /**
     * 是否安装了指定包名的App
     *
     * @param packageName App包名
     * @return
     */
    public static boolean isInstalled(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        List<PackageInfo> pkgList = manager.getInstalledPackages(0);
        for (int i = 0; i < pkgList.size(); i++) {
            PackageInfo info = pkgList.get(i);
            if (info.packageName.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }

    /**
     * 判断包名所对应的应用是否安装在外置存储上
     */
    public static boolean isInstalledOnExternalStorage(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            if (pm != null) {
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                if (info == null) {
                    // 未安装
                    return false;
                }
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                if ((appInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException ignore) {
        }
        return false;
    }

    /**
     * 描述：打开并安装文件.
     *
     * @param file apk文件路径
     */
    public static void installApk(Activity activity, File file, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean hasInstallPermission = activity.getPackageManager().canRequestPackageInstalls();
            if (hasInstallPermission) {
                installApkCompatAndroid7Plus(activity, file);
            } else {
                //跳转至“安装未知应用”权限界面，引导用户开启权限
                startAppUnknownSources(activity, requestCode);
            }
        } else {
            installApkCompatAndroid7Plus(activity, file);
        }
    }

    private static void installApkCompatAndroid7Plus(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        CqrCompatUtils.setDataAndType(context, intent, "application/vnd.android.package-archive", file);
        context.startActivity(intent);
    }

    /**
     * 描述：卸载程序.
     *
     * @param packageName 包名
     */
    public static void uninstallApk(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        context.startActivity(intent);
    }

    /**
     * 关闭其他应用程序。需要KILL_BACKGROUND_PROCESSES权限
     *
     * @param context     the context
     * @param packageName the package name
     */
    @RequiresPermission(Manifest.permission.KILL_BACKGROUND_PROCESSES)
    public static void killApp(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return;
        }
        am.killBackgroundProcesses(packageName);
    }

    /**
     * whether packageName is system application
     */
    public static boolean isSystemApp(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null || packageName == null || packageName.length() == 0) {
            return false;
        }
        try {
            ApplicationInfo app = packageManager.getApplicationInfo(packageName, 0);
            return (app != null && (app.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
        } catch (Exception ignore) {
        }
        return false;
    }

    /**
     * need <uses-permission android:name ="android.permission.GET_TASKS"/>
     * <p>
     * 判断是否前台运行
     * <p>
     * 之前，使用该接口需要 android.permission.GET_TASKS
     * 即使是自己开发的普通应用，只要声明该权限，即可以使用getRunningTasks接口。
     * 但从L开始，这种方式已经废弃。
     * 应用要使用该接口必须声明权限android.permission.REAL_GET_TASKS
     * 而这个权限是不对三方应用开放的。（在Manifest里申请了也没有作用）
     * 系统应用（有系统签名）可以调用该权限。
     */
    public static boolean isRunningForeground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return false;
        }
        List<ActivityManager.RunningTaskInfo> taskList = am.getRunningTasks(1);
        if (taskList != null && !taskList.isEmpty()) {
            ComponentName componentName = taskList.get(0).topActivity;
            //noinspection RedundantIfStatement
            if (componentName != null && componentName.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 当你在主进程中判断时，获得进程信息始终是IMPORTANCE_FOREGROUND，
     * 就是说始终标识你的进程在前台运行，不管你按了HOME键还是返回键。
     * 解决方法：将判断过程放到异步中，不要放到主进程中。
     * 参见：http://blog.csdn.net/bdmh/article/details/40425213
     */
    @WorkerThread
    public static boolean isRunningBackground(Context context, String packageName) {
        boolean result = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return false;
        }
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.startsWith(packageName)) {
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    result = true;
                }
                break;
            }
        }
        return result;
    }

    /**
     * 用来判断服务是否运行.
     *
     * @param className 判断的服务名字 "com.xxx.xx..XXXService"
     * @return true 在运行 false 不在运行
     */
    public static boolean isServiceRunning(Context context, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return false;
        }
        List<ActivityManager.RunningServiceInfo> servicesList = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo si : servicesList) {
            if (className.equals(si.service.getClassName())) {
                isRunning = true;
            }
        }
        return isRunning;
    }

    /**
     * 停止服务.
     *
     * @param className the class name
     * @return true, if successful
     */
    public static boolean stopRunningService(Context context, String className) {
        Intent intent = null;
        boolean ret = false;
        try {
            intent = new Intent(context, Class.forName(className));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (intent != null) {
            ret = context.stopService(intent);
        }
        return ret;
    }

    /**
     * 判断是否为主进程
     */
    public static boolean isMainProcess(Context context) {
        Context applicationContext = context.getApplicationContext();
        ActivityManager am = (ActivityManager) applicationContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return false;
        }
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                String processName = proInfo.processName;
                if (processName != null) {
                    CqrLog.debug("processName=" + processName);
                    return applicationContext.getPackageName().equals(processName);
                }
            }
        }
        return false;
    }

    public static String getPackageName(Context context, String apkPath) {
        if (CqrFileUtils.exist(apkPath)) {
            PackageManager pm = context.getPackageManager();
            if (pm != null) {
                PackageInfo pi = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
                if (pi != null) {
                    return pi.packageName;
                }
            }
        }
        return "";
    }

    public static String getPackageName(Context context) {
        return context.getApplicationContext().getPackageName();
    }

    /**
     * 获取PackageInfo
     *
     * @return PackageInfo
     */
    public static PackageInfo getPackageInfo(Context context) {
        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException ignore) {
        }
        return packageInfo;
    }

    /**
     * 获取版本名称
     * String
     *
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        PackageInfo packageInfo = getPackageInfo(context);
        if (packageInfo == null) {
            return "0.0";
        }
        return packageInfo.versionName;
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static long getVersionCode(Context context) {
        PackageInfo packageInfo = getPackageInfo(context);
        if (packageInfo == null) {
            return 0;
        }
        return PackageInfoCompat.getLongVersionCode(packageInfo);
    }

    @DrawableRes
    public static int getAppIconRes(Context context) {
        ApplicationInfo appInfo = context.getApplicationContext().getApplicationInfo();
        if (appInfo == null) {
            return 0;
        }
        return appInfo.icon;
    }

    /**
     * G获取应用图标
     */
    public static Drawable getAppIcon(Context context) {
        ApplicationInfo appInfo = context.getApplicationContext().getApplicationInfo();
        if (appInfo == null) {
            return null;
        }
        return context.getPackageManager().getApplicationIcon(appInfo);
    }

    /**
     * 获取application节点的meta-data
     */
    public static String getAppMeta(Context context, String key) {
        String pkgName = context.getApplicationContext().getPackageName();
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
            String val = ai.metaData.getString(key);
            if (val == null) {
                val = "";
            }
            return val;
        } catch (Exception ignore) {
            return "";
        }
    }

    /**
     * 获取应用的签名(为安全起见，应该在C/C++层实现)
     */
    public static String getAppSign(Context context) {
        String pkgName = context.getApplicationContext().getPackageName();
        try {
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo info = context.getPackageManager().getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
            Signature[] signatures = info.signatures;
            StringBuilder builder = new StringBuilder();
            for (Signature signature : signatures) {
                builder.append(signature.toChars());
            }
            return builder.toString();
        } catch (PackageManager.NameNotFoundException ignore) {
            return "";
        }
    }

    /**
     * Gets app info.
     */
    public static ApplicationInfo getAppInfo(Context context, String apkPath) {
        apkPath = Uri.parse(apkPath).toString();
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        } catch (RuntimeException ignore) {
            // should be something wrong with parse
        }
        if (pkgInfo == null) {
            return null;
        }
        // Workaround for http://code.google.com/p/android/issues/detail?id=9151
        ApplicationInfo appInfo = pkgInfo.applicationInfo;
        appInfo.sourceDir = apkPath;
        appInfo.publicSourceDir = apkPath;
        return appInfo;
    }

    /**
     * 比较版本号的大小,前者大则返回一个正数,后者大返回一个负数,相等则返回0   支持4.1.2,4.1.23.4.1.rc111这种形式
     */
    public static int compareVersion(@NonNull String version1, @NonNull String version2) {
        try {
            String[] versionArray1 = version1.split("\\.");//注意此处为正则匹配，不能用"."；
            String[] versionArray2 = version2.split("\\.");
            int idx = 0;
            int minLength = Math.min(versionArray1.length, versionArray2.length);//取最小长度值
            int diff = 0;
            while (idx < minLength
                    && (diff = versionArray1[idx].length() - versionArray2[idx].length()) == 0//先比较长度
                    && (diff = versionArray1[idx].compareTo(versionArray2[idx])) == 0) {//再比较字符
                ++idx;
            }
            //如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；
            diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;
            return diff;
        } catch (Throwable ignore) {
            return 0;
        }
    }

}
