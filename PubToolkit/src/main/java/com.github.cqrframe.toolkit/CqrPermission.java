package com.github.cqrframe.toolkit;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.PermissionChecker;

import com.github.cqrframe.logger.CqrLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API23+动态权限处理，原理就是用fragment发起请求,创建一个没有界面的fragment,可感知activity生命周期
 * 参阅以下开源项目：
 * https://github.com/majunm/PermmisonCompat
 * https://github.com/florent37/RuntimePermission
 * https://github.com/getActivity/XXPermissions
 * <p>
 * Created by liyujiang on 2018/9/11 10:07
 *
 * @see Group
 * @see Setting
 * @see Callback
 * @see SimpleCallback
 */
@SuppressWarnings({"WeakerAccess", "BooleanMethodIsAlwaysInverted"})
public class CqrPermission {
    private static final String TAG = "cqr_perm_fragment";
    private static final int REQUEST_CODE = 200;

    private CqrPermission() {
        throw new UnsupportedOperationException("You can't instantiate me");
    }

    public static void tryRequest(FragmentActivity activity, Callback callback) {
        tryRequest(activity, callback, findNeededPermissionsFromManifest(activity));
    }

    public static void tryRequest(FragmentActivity activity, Callback callback, String... permissions) {
        if (permissions == null || permissions.length == 0) {
            if (callback != null) {
                callback.accepted(new ArrayList<String>());
            }
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (callback != null) {
                callback.accepted(Arrays.asList(permissions));
            }
            return;
        }
        List<String> deniedPermissions = findDeniedPermissions(activity, permissions);
        if (deniedPermissions.size() == 0) {
            if (callback != null) {
                callback.accepted(Arrays.asList(permissions));
            }
        } else {
            requestPermissions(activity, deniedPermissions, callback);
        }
    }

    /**
     * Returns true if the Activity or Fragment has access to all given permissions.
     *
     * @param context     context
     * @param permissions permission list
     * @return returns true if the Activity or Fragment has access to all given permissions.
     */
    public static boolean hasSelfPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (!hasSelfPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }

    private static void requestPermissions(final FragmentActivity activity, List<String> deniedPermissions, Callback callback) {
        String[] a = new String[deniedPermissions.size()];
        final String[] deniedPerms = deniedPermissions.toArray(a);
        final FragmentManager fragmentManager = activity.getSupportFragmentManager();
        InternalFragment oldFragment = (InternalFragment) fragmentManager.findFragmentByTag(TAG);
        if (oldFragment != null) {
            oldFragment.setCallback(callback);
            oldFragment.requestPermissions(deniedPerms, REQUEST_CODE);
        } else {
            final InternalFragment newFragment = InternalFragment.newInstance(deniedPermissions);
            newFragment.setCallback(callback);
            fragmentManager.beginTransaction().add(newFragment, TAG).commitAllowingStateLoss();
            try {
                //必须在主线程执行
                fragmentManager.executePendingTransactions();
                newFragment.requestPermissions(deniedPerms, REQUEST_CODE);
            } catch (IllegalStateException e) {
                //避免华为BND-AL10等报IllegalStateException:
                //FragmentManager is already executing transactions
                CqrLog.debug(e);
            }
        }
    }

    private static List<String> findDeniedPermissions(Context context, String... permissions) {
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (!hasSelfPermission(context, permission)) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions;
    }

    /**
     * Determine context has access to the given permission.
     * <p>
     * This is a workaround for RuntimeException of Parcel#readException.
     * For more detail, check this issue https://github.com/hotchemi/PermissionsDispatcher/issues/107
     *
     * @param context    context
     * @param permission permission
     * @return returns true if context has access to the given permission, false otherwise.
     */
    private static boolean hasSelfPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && "Xiaomi".equalsIgnoreCase(Build.MANUFACTURER)) {
            return hasSelfPermissionForXiaomi(context, permission);
        }
        try {
            return PermissionChecker.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        } catch (RuntimeException ignore) {
            return false;
        }
    }

    private static boolean hasSelfPermissionForXiaomi(Context context, String permission) {
        String permissionToOp = AppOpsManagerCompat.permissionToOp(permission);
        if (permissionToOp == null) {
            // in case of normal permissions(e.g. INTERNET)
            return true;
        }
        int noteOp = AppOpsManagerCompat.noteOp(context, permissionToOp, Process.myUid(), context.getPackageName());
        return noteOp == AppOpsManagerCompat.MODE_ALLOWED && PermissionChecker.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @NonNull
    private static String[] findNeededPermissionsFromManifest(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException ignore) {
            /* */
        }
        List<String> needed = new ArrayList<>();
        if (info != null && info.requestedPermissions != null && info.requestedPermissionsFlags != null) {
            for (int i = 0; i < info.requestedPermissions.length; i++) {
                final int flags = info.requestedPermissionsFlags[i];
                String group = null;
                try {
                    group = pm.getPermissionInfo(info.requestedPermissions[i], 0).group;
                } catch (PackageManager.NameNotFoundException ignore) {
                    /* */
                }
                if (((flags & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) && group != null) {
                    needed.add(info.requestedPermissions[i]);
                }
            }
        }
        String[] a = new String[needed.size()];
        return needed.toArray(a);
    }

    /**
     * 敏感权限组
     */
    public static class Group {
        public static final String[] CALENDAR;
        public static final String[] CAMERA;
        public static final String[] CONTACTS;
        public static final String[] LOCATION;
        public static final String[] MICROPHONE;
        public static final String[] PHONE_STATE;
        public static final String[] PHONE_CALL;
        public static final String[] SENSORS;
        public static final String[] SMS;
        public static final String[] STORAGE;
        private static final Map<String, String> map = new HashMap<>();

        static {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                CALENDAR = new String[]{};
                CAMERA = new String[]{};
                CONTACTS = new String[]{};
                LOCATION = new String[]{};
                MICROPHONE = new String[]{};
                PHONE_STATE = new String[]{};
                PHONE_CALL = new String[]{};
                SENSORS = new String[]{};
                SMS = new String[]{};
                STORAGE = new String[]{};
            } else {
                CALENDAR = new String[]{Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR};
                CAMERA = new String[]{Manifest.permission.CAMERA};
                CONTACTS = new String[]{Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.GET_ACCOUNTS};
                LOCATION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION};
                MICROPHONE = new String[]{Manifest.permission.RECORD_AUDIO};
                PHONE_STATE = new String[]{Manifest.permission.READ_PHONE_STATE};
                PHONE_CALL = new String[]{Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.WRITE_CALL_LOG,
                        Manifest.permission.USE_SIP,
                        Manifest.permission.PROCESS_OUTGOING_CALLS};
                SENSORS = new String[]{Manifest.permission.BODY_SENSORS};
                SMS = new String[]{Manifest.permission.SEND_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_WAP_PUSH,
                        Manifest.permission.RECEIVE_MMS};
                STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }
            for (String p : CALENDAR) {
                map.put(p, "读写日历");
            }
            for (String p : CAMERA) {
                map.put(p, "使用相机");
            }
            for (String p : CONTACTS) {
                map.put(p, "获取联系人");
            }
            for (String p : LOCATION) {
                map.put(p, "获取地理位置");
            }
            for (String p : MICROPHONE) {
                map.put(p, "使用麦克风");
            }
            for (String p : PHONE_STATE) {
                map.put(p, "获取设备状态");
            }
            for (String p : PHONE_CALL) {
                map.put(p, "拨打电话");
            }
            for (String p : SENSORS) {
                map.put(p, "使用传感器");
            }
            for (String p : SMS) {
                map.put(p, "收发短信");
            }
            for (String p : STORAGE) {
                map.put(p, "存储空间");
            }
        }

        /**
         * 合并权限组
         */
        public static String[] concat(String[] first, String[] second) {
            String[] result = Arrays.copyOf(first, first.length + second.length);
            System.arraycopy(second, 0, result, first.length, second.length);
            return result;
        }

        /**
         * 根据权限获取权限组名称
         *
         * @see Manifest.permission
         */
        public static String getName(String permission) {
            return map.get(permission);
        }

    }

    /**
     * 权限设置页（支持大部分国产手机）
     */
    public static final class Setting {
        private static final String MARK = Build.MANUFACTURER.toLowerCase();

        /**
         * 跳转到应用权限设置页面。参阅 https://blog.csdn.net/zhuhai__yizhi/article/details/78737593?utm_source=blogxgwz1
         *
         * @param context 上下文对象
         * @param newTask 是否使用新的任务栈启动
         */
        public static void start(Context context, boolean newTask) {
            Intent intent = null;
            if (MARK.contains("huawei")) {
                intent = huawei(context);
            } else if (MARK.contains("xiaomi")) {
                intent = xiaomi(context);
            } else if (MARK.contains("oppo")) {
                intent = oppo(context);
            } else if (MARK.contains("vivo")) {
                intent = vivo(context);
            } else if (MARK.contains("meizu")) {
                intent = meizu(context);
            } else if (MARK.contains("sony")) {
                intent = sony(context);
            } else if (MARK.contains("lg")) {
                intent = lg(context);
            } else if (MARK.contains("letv")) {
                intent = letv(context);
            }

            if (intent == null || !hasIntent(context, intent)) {
                intent = google(context);
            }

            if (newTask) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            try {
                context.startActivity(intent);
            } catch (Exception ignored) {
                intent = google(context);
                context.startActivity(intent);
            }
        }

        private static Intent google(Context context) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
            return intent;
        }

        private static Intent huawei(Context context) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity"));
            if (hasIntent(context, intent)) return intent;
            intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity"));
            if (hasIntent(context, intent)) return intent;
            intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.notificationmanager.ui.NotificationManagmentActivity"));
            return intent;
        }

        private static Intent xiaomi(Context context) {
            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.putExtra("extra_pkgname", context.getPackageName());
            if (hasIntent(context, intent)) return intent;

            intent.setPackage("com.miui.securitycenter");
            if (hasIntent(context, intent)) return intent;

            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            if (hasIntent(context, intent)) return intent;

            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            return intent;
        }

        private static Intent oppo(Context context) {
            Intent intent = new Intent();
            intent.putExtra("packageName", context.getPackageName());
            intent.setClassName("com.color.safecenter", "com.color.safecenter.permission.floatwindow.FloatWindowListActivity");
            if (hasIntent(context, intent)) return intent;

            intent.setClassName("com.coloros.safecenter", "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity");
            if (hasIntent(context, intent)) return intent;

            intent.setClassName("com.oppo.safe", "com.oppo.safe.permission.PermissionAppListActivity");
            return intent;
        }

        private static Intent vivo(Context context) {
            Intent intent = new Intent();
            intent.setClassName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.FloatWindowManager");
            intent.putExtra("packagename", context.getPackageName());
            if (hasIntent(context, intent)) return intent;

            intent.setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.safeguard.SoftPermissionDetailActivity"));
            return intent;
        }

        private static Intent meizu(Context context) {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.putExtra("packageName", context.getPackageName());
            intent.setComponent(new ComponentName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity"));
            return intent;
        }

        private static Intent sony(Context context) {
            Intent intent = new Intent();
            intent.putExtra("packageName", context.getPackageName());
            ComponentName comp = new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
            intent.setComponent(comp);
            return intent;
        }

        private static Intent lg(Context context) {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.putExtra("packageName", context.getPackageName());
            ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
            intent.setComponent(comp);
            return intent;
        }

        private static Intent letv(Context context) {
            Intent intent = new Intent();
            intent.putExtra("packageName", context.getPackageName());
            ComponentName comp = new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.PermissionAndApps");
            intent.setComponent(comp);
            return intent;
        }

        private static boolean hasIntent(Context context, Intent intent) {
            return context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
        }

    }

    /**
     * 权限请求回调
     */
    public interface Callback {
        void accepted(List<String> acceptedPermissions);

        void askAgain(List<String> askAgainPermissions);

        void foreverDenied(List<String> foreverDeniedPermissions);
    }

    public static class SimpleCallback implements Callback {

        @Override
        public void accepted(List<String> acceptedPermissions) {
        }

        @Override
        public void askAgain(List<String> askAgainPermissions) {
        }

        @Override
        public void foreverDenied(List<String> foreverDeniedPermissions) {
        }

    }

    /**
     * DO NOT USE THIS FRAGMENT DIRECTLY!
     * It's only here because fragments have to be public
     */
    public static class InternalFragment extends Fragment {
        private static final String INTENT_KEY_PERMISSIONS = "intent_key_permissions";
        private List<String> permissionsList = new ArrayList<>();
        private Callback callback;

        public static InternalFragment newInstance(List<String> permissions) {
            Bundle args = new Bundle();
            args.putStringArrayList(INTENT_KEY_PERMISSIONS, new ArrayList<>(permissions));
            InternalFragment fragment = new InternalFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);// 禁止横竖屏切换时的Fragment的重建
            Bundle arguments = getArguments();
            if (arguments != null) {
                final List<String> permissionsArgs = arguments.getStringArrayList(INTENT_KEY_PERMISSIONS);
                if (permissionsArgs != null) {
                    permissionsList.addAll(permissionsArgs);
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            int size = permissionsList.size();
            if (size > 0) {
                String[] perms = new String[size];
                permissionsList.toArray(perms);
                requestPermissions(perms, REQUEST_CODE);
            } else {
                // this shouldn't happen, but just to be sure
                requireFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (callback == null || requestCode != REQUEST_CODE) {
                return;
            }
            List<String> acceptedPermissions = new ArrayList<>();
            List<String> askAgainPermissions = new ArrayList<>();
            List<String> foreverDeniedPermissions = new ArrayList<>();
            if (grantResults.length == 0 && permissionsList.size() > 0) {
                for (String permissionName : permissionsList) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permissionName)) {
                        askAgainPermissions.add(permissionName);
                    } else {
                        foreverDeniedPermissions.add(permissionName);
                    }
                }
            } else {
                for (int i = 0, n = permissions.length; i < n; i++) {
                    String permissionName = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        acceptedPermissions.add(permissionName);
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permissionName)) {
                            askAgainPermissions.add(permissionName);
                        } else {
                            foreverDeniedPermissions.add(permissionName);
                        }
                    }
                }
            }
            if (askAgainPermissions.size() == 0 && foreverDeniedPermissions.size() == 0) {
                callback.accepted(acceptedPermissions);
            }
            if (askAgainPermissions.size() > 0) {
                callback.askAgain(askAgainPermissions);
            }
            if (foreverDeniedPermissions.size() > 0) {
                callback.foreverDenied(foreverDeniedPermissions);
            }
            requireFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
        }

        public void setCallback(Callback callback) {
            this.callback = callback;
        }

    }

}