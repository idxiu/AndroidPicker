package cn.qqtheme.androidpicker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.github.cqrframe.logger.CqrLog;
import com.github.cqrframe.toolkit.CqrPackageUtils;

import java.util.List;
import java.util.Stack;

/**
 * Author:李玉江[QQ:1032694760]
 * DateTime:2016/7/20 20:28
 * Builder:Android Studio
 */
public class MyPickerApp extends Application implements Application.ActivityLifecycleCallbacks {
    private static MyPickerApp instance;
    private static Stack<Activity> activityStack = new Stack<>();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CqrLog.LOG_TAG = "liyujiang";
        if (BuildConfig.DEBUG) {
            CqrLog.LOG_ENABLE = true;
        } else {
            CqrLog.LOG_ENABLE = false;
            Log.d(CqrLog.LOG_TAG, "LogCat is disabled");
        }
        if (!CqrPackageUtils.isMainProcess(this)) {
            Log.d(CqrLog.LOG_TAG, "当前非主进程，不要初始化: pid=" + android.os.Process.myPid());
            return;
        }
        registerActivityLifecycleCallbacks(this);
    }

    public static synchronized MyPickerApp getInstance() {
        return instance;
    }

    public static synchronized Context getAppContext() {
        return instance.getApplicationContext();
    }

    @Nullable
    public static Activity currentActivity() {
        if (activityStack.isEmpty()) {
            return null;
        }
        return activityStack.lastElement();
    }

    public static void finishAllActivity() {
        while (!activityStack.isEmpty()) {
            Activity activity = activityStack.pop();
            if (activity != null) {
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
        }
    }

    private static boolean clearAllActivityTask() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        ActivityManager am = (ActivityManager) instance.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return false;
        }
        try {
            List<ActivityManager.AppTask> appTasks = am.getAppTasks();
            for (ActivityManager.AppTask appTask : appTasks) {
                appTask.finishAndRemoveTask();
            }
            return true;
        } catch (SecurityException ignore) {
        }
        return false;
    }

    /**
     * 应用程序退出
     *
     * @param forceKill 是否强制杀掉进程
     */
    public static void exitApp(boolean forceKill) {
        try {
            finishAllActivity();
            clearAllActivityTask();
            if (!forceKill) {
                return;
            }
            //退出JVM(Java虚拟机),释放所占内存资源,0表示正常退出(非0的都为异常退出)
            System.exit(0);
            //从操作系统中结束掉当前程序的进程
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
        } catch (Throwable ignore) {
            System.exit(-1);
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        activityStack.addElement(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        activityStack.removeElement(activity);
    }

}
