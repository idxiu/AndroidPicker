package com.github.cqrframe.toolkit;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.github.cqrframe.logger.CqrLog;

/**
 * 获取屏幕宽高等信息、全屏切换、保持屏幕常亮、截屏等
 * <p>
 * Created by liyujiang on 2015/11/26
 */
@SuppressWarnings("WeakerAccess")
public class CqrScreenUtils {
    private static DisplayMetrics dm = null;

    protected CqrScreenUtils() {
        throw new UnsupportedOperationException("You can't instantiate me");
    }

    public static DisplayMetrics displayMetrics(Context context) {
        if (null != dm) {
            return dm;
        }
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return dm;
        }
        wm.getDefaultDisplay().getMetrics(dm);
        CqrLog.debug("screen width=" + dm.widthPixels + "px, screen height=" + dm.heightPixels
                + "px, densityDpi=" + dm.densityDpi + ", density=" + dm.density);
        return dm;
    }

    public static int widthPixels(Context context) {
        return displayMetrics(context).widthPixels;
    }

    public static int heightPixels(Context context) {
        return displayMetrics(context).heightPixels;
    }

    public static float density(Context context) {
        return displayMetrics(context).density;
    }

    public static int densityDpi(Context context) {
        return displayMetrics(context).densityDpi;
    }

    public static boolean isFullScreen(Activity activity) {
        int flag = activity.getWindow().getAttributes().flags;
        //noinspection RedundantIfStatement
        if ((flag & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
            return true;
        } else {
            return false;
        }
    }

    public static void toggleFullScreen(Activity activity) {
        if (isFullScreen(activity)) {
            exitFullScreen(activity);
        } else {
            enterFullScreen(activity);
        }
    }

    public static void enterFullScreen(Activity activity) {
        int flagFullscreen = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        activity.getWindow().setFlags(flagFullscreen, flagFullscreen);
    }

    public static void exitFullScreen(Activity activity) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 保持屏幕常亮
     */
    public static void keepBright(Activity activity) {
        //需在setContentView前调用
        int keepScreenOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        activity.getWindow().setFlags(keepScreenOn, keepScreenOn);
    }

}
