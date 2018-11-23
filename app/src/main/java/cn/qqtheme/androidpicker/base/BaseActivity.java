package cn.qqtheme.androidpicker.base;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.github.cqrframe.logger.CqrLog;
import com.jaeger.library.StatusBarUtil;

/**
 * Activity的基类
 *
 * @author 李玉江[QQ:1023694760]
 * @since 2014-4-20
 */
public abstract class BaseActivity extends FragmentActivity {
    protected Context context;
    protected BaseActivity activity;
    protected String className = getClass().getSimpleName();

    protected abstract View getContentView();

    protected abstract void setContentViewAfter(View contentView);

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CqrLog.debug(className + " onCreate");
        context = getApplicationContext();
        activity = this;
        //不显示标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= 21) {
            setTheme(android.R.style.Theme_Material_Light_NoActionBar);
        } else if (Build.VERSION.SDK_INT >= 14) {
            setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
        } else {
            setTheme(android.R.style.Theme_Light_NoTitleBar);
        }
        //被系统回收后重启恢复
        if (savedInstanceState != null) {
            CqrLog.debug("savedInstanceState is not null");
            onStateRestore(savedInstanceState);
        }
        //显示界面布局
        View contentView = getContentView();
        if (contentView == null) {
            TextView textView = new TextView(this);
            textView.setBackgroundColor(Color.RED);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            textView.setGravity(Gravity.CENTER);
            textView.setText("请先初始化内容视图");
            textView.setTextColor(Color.WHITE);
            contentView = textView;
        }
        CqrLog.debug(className + " setContentView before");
        setContentViewBefore();
        setContentView(contentView);
        if (isTranslucentStatusBar()) {
            StatusBarUtil.setTransparent(activity);
        }
        setContentViewAfter(contentView);
        CqrLog.debug(className + " setContentView after");
    }

    protected void onStateRestore(@NonNull Bundle savedInstanceState) {

    }

    protected void setContentViewBefore() {
        CqrLog.debug(className + " setContentView before");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        CqrLog.debug(className + " onBackPressed");
    }

    @CallSuper
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        CqrLog.debug(className + " onSaveInstanceState");
    }

    @CallSuper
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        CqrLog.debug(className + " onRestoreInstanceState");
    }

    @CallSuper
    @Override
    public void onRestart() {
        super.onRestart();
        CqrLog.debug(className + " onRestart");
    }

    @CallSuper
    @Override
    public void onStart() {
        super.onStart();
        CqrLog.debug(className + " onStart");
    }

    @CallSuper
    @Override
    public void onResume() {
        super.onResume();
        CqrLog.debug(className + " onResume");
    }

    @CallSuper
    @Override
    public void onPause() {
        super.onPause();
        CqrLog.debug(className + " onPause");
    }

    @CallSuper
    @Override
    public void onStop() {
        super.onStop();
        CqrLog.debug(className + " onStop");
    }

    @CallSuper
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CqrLog.debug(className + " onSaveInstanceState");
    }

    @CallSuper
    @Override
    public void onDestroy() {
        super.onDestroy();
        CqrLog.debug(className + " onDestroy");
    }

    @CallSuper
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CqrLog.debug(className + " onActivityResult");
    }

    @CallSuper
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        CqrLog.debug(className + " onLowMemory");
    }

    @CallSuper
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        CqrLog.debug(className + " onConfigurationChanged");
        if (newConfig.fontScale != 1f) {
            getResources();
        }
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res == null) {
            return null;
        }
        //强制字体大小不随系统改变而改变：https://blog.csdn.net/xuxian361/article/details/74909602
        if (res.getConfiguration().fontScale != 1f) {
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                createConfigurationContext(newConfig);
            } else {
                res.updateConfiguration(newConfig, res.getDisplayMetrics());
            }
        }
        return res;
    }

    protected boolean isTranslucentStatusBar() {
        return true;
    }

    protected <T> T inflateView(@LayoutRes int layoutResource) {
        CqrLog.debug(className + " inflate view by layout resource");
        //noinspection unchecked
        return (T) LayoutInflater.from(activity).inflate(layoutResource, null);
    }

    protected <T> T findView(@IdRes int id) {
        //noinspection unchecked
        return (T) findViewById(id);
    }

}
