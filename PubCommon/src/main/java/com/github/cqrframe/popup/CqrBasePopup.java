package com.github.cqrframe.popup;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.github.cqrframe.logger.CqrLog;
import com.github.cqrframe.toolkit.CqrCompatUtils;

/**
 * 弹出层基类
 * <p>
 * Created by liyujiang on 2018/9/22 19:46
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public abstract class CqrBasePopup<P extends CqrBasePopup> implements PopupWindow.OnDismissListener,
        GenericLifecycleObserver, View.OnTouchListener, View.OnClickListener {
    protected static final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
    protected static final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;
    protected FragmentActivity activity;
    protected PopupWindow popupWindow;
    private Drawable backgroundDrawable = new ColorDrawable(Color.TRANSPARENT);
    private int animationStyle = -1;
    private OnPrepareShowListener onPrepareShowListener;
    private PopupWindow.OnDismissListener onDismissListener;
    private boolean focusable = true;
    private int width = MATCH_PARENT;
    private int height = WRAP_CONTENT;
    private View contentView;
    private boolean isApply = false;
    private boolean showMaskAlpha = false;

    public CqrBasePopup(FragmentActivity activity) {
        this.activity = activity;
        activity.getLifecycle().addObserver(this);
        popupWindow = new PopupWindow(activity);
        popupWindow.setTouchInterceptor(this);
    }

    protected abstract View createContentView(@NonNull FragmentActivity activity);

    public void onViewCreated(@NonNull View contentView) {

    }

    protected P self() {
        //noinspection unchecked
        return (P) this;
    }

    public P setAnimationStyle(@StyleRes int animationStyle) {
        this.animationStyle = animationStyle;
        return self();
    }

    public P setBackgroundDrawable(Drawable drawable) {
        this.backgroundDrawable = drawable;
        return self();
    }

    public P setBackgroundColor(@ColorInt int color) {
        this.backgroundDrawable = new ColorDrawable(color);
        return self();
    }

    public P setBackgroundResource(@DrawableRes int drawableRes) {
        this.backgroundDrawable = CqrCompatUtils.getDrawable(activity, drawableRes);
        return self();
    }

    public P setFocusable(boolean focusable) {
        this.focusable = focusable;
        return self();
    }

    public P setShowMaskAlpha(boolean showMaskAlpha) {
        this.showMaskAlpha = showMaskAlpha;
        return self();
    }

    public P setWidth(int width) {
        this.width = width;
        return self();
    }

    public P setHeight(int height) {
        this.height = height;
        return self();
    }

    public P setContentView(View contentView) {
        this.contentView = contentView;
        return self();
    }

    public P setContentView(@LayoutRes int layoutRes) {
        this.contentView = LayoutInflater.from(activity).inflate(layoutRes, null);
        return self();
    }

    public P setOnClickListener(@IdRes int idRes, View.OnClickListener listener) {
        if (contentView == null) {
            throw new RuntimeException("Please set content view at first");
        }
        contentView.findViewById(idRes).setOnClickListener(listener);
        return self();
    }

    public P setOnPrepareShowListener(OnPrepareShowListener onPrepareShowListener) {
        this.onPrepareShowListener = onPrepareShowListener;
        return self();
    }

    public P setOnDismissListener(PopupWindow.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        return self();
    }

    public P apply() {
        popupWindow.setWidth(width);
        popupWindow.setHeight(height);
        if (animationStyle != -1) {
            popupWindow.setAnimationStyle(animationStyle);
        }
        popupWindow.setBackgroundDrawable(backgroundDrawable);
        popupWindow.setFocusable(focusable);
        popupWindow.setOnDismissListener(this);
        contentView = createContentView(activity);
        popupWindow.setContentView(contentView);
        onViewCreated(contentView);
        isApply = true;
        return self();
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }

    private void checkApplyAndWillShow() {
        onPrepareShow();
        if (!isApply) {
            apply();
        }
    }

    public void showAsDropDown(View anchor) {
        showAsDropDown(anchor, 0, 0);
    }

    public void showAsDropDown(final View anchor, final int xoff, final int yoff) {
        if (popupWindow.isShowing()) {
            return;
        }
        checkApplyAndWillShow();
        try {
            popupWindow.showAsDropDown(anchor, xoff, yoff);
        } catch (Throwable ignore) {
            //...not attached to window manager
            //...Unable to add window...is your activity running?
            //...Activity...has leaked window...that was originally added here
        }
    }

    public void showAsAnchorLeftTop(final View anchor) {
        if (popupWindow.isShowing()) {
            return;
        }
        checkApplyAndWillShow();
        try {
            popupWindow.showAsDropDown(anchor, 0, -anchor.getMeasuredHeight());
        } catch (Throwable ignore) {
            //...not attached to window manager
        }
    }

    public void showAsAnchorLeftBottom(final View anchor) {
        if (popupWindow.isShowing()) {
            return;
        }
        checkApplyAndWillShow();
        try {
            int contentPadding = 5;
            popupWindow.showAsDropDown(anchor, 0, -1 * contentPadding);
        } catch (Throwable ignore) {
            //...not attached to window manager
        }
    }

    public void showAsAnchorRightTop(final View anchor) {
        if (popupWindow.isShowing()) {
            return;
        }
        checkApplyAndWillShow();
        try {
            popupWindow.showAsDropDown(anchor,
                    anchor.getMeasuredWidth() / 2 + getContentViewWidth() / 2,
                    -anchor.getMeasuredHeight());
        } catch (Throwable ignore) {
            //...not attached to window manager
        }
    }

    public void showAsAnchorRightBottom(final View anchor) {
        if (popupWindow.isShowing()) {
            return;
        }
        checkApplyAndWillShow();
        try {
            int contentPadding = 5;
            popupWindow.showAsDropDown(anchor,
                    anchor.getMeasuredWidth() / 2 + getContentViewWidth() / 2,
                    -1 * contentPadding);
        } catch (Throwable ignore) {
            //...not attached to window manager
        }
    }

    public void showAtCenter(final View anchor) {
        if (popupWindow.isShowing()) {
            return;
        }
        checkApplyAndWillShow();
        try {
            popupWindow.showAtLocation(anchor, Gravity.CENTER, 0, 0);
        } catch (Throwable ignore) {
            //...not attached to window manager
        }
    }

    public void showAsAnchorCenter(final View anchor) {
        if (popupWindow.isShowing()) {
            return;
        }
        checkApplyAndWillShow();
        try {
            popupWindow.showAsDropDown(anchor,
                    anchor.getMeasuredWidth() / 2 - getContentViewWidth() / 2,
                    -anchor.getMeasuredHeight() / 2 - getContentViewHeight() / 2);
        } catch (Throwable ignore) {
            //...not attached to window manager
        }
    }

    public void showAtBottom() {
        showAtLocation(activity.findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
    }

    public void showAtLocation(final View parent, final int gravity, final int x, final int y) {
        if (popupWindow.isShowing()) {
            return;
        }
        checkApplyAndWillShow();
        try {
            popupWindow.showAtLocation(parent, gravity, x, y);
        } catch (Throwable ignore) {
            //...not attached to window manager
        }
    }

    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            activity.getLifecycle().removeObserver(this);
            CqrLog.debug(source.getClass().getName() + " ON_DESTROY, check popup dismiss");
            dismiss();
        }
    }

    public void dismiss() {
        if (!popupWindow.isShowing()) {
            return;
        }
        popupWindow.dismiss();
        isApply = false;
    }

    public void onPrepareShow() {
        if (showMaskAlpha) {
            WindowManager.LayoutParams params = activity.getWindow().getAttributes();
            params.alpha = 0.5F;
            activity.getWindow().setAttributes(params);
        }
        if (onPrepareShowListener != null) {
            onPrepareShowListener.onPrepareShow();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            popupWindow.dismiss();
            return true;
        }
        return false;
    }

    @Override
    public void onDismiss() {
        if (showMaskAlpha) {
            WindowManager.LayoutParams params = activity.getWindow().getAttributes();
            params.alpha = 1.0F;
            activity.getWindow().setAttributes(params);
        }
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
    }

    @Override
    public void onClick(View v) {
    }

    public final FragmentActivity getActivity() {
        return activity;
    }

    public final PopupWindow getWindow() {
        return popupWindow;
    }

    public final View getContentView() {
        return contentView;
    }

    public final int getContentViewWidth() {
        View contentView = popupWindow.getContentView();
        int width = contentView.getWidth();
        if (width == 0) {
            contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            return contentView.getMeasuredWidth();
        } else {
            return width;
        }
    }

    public final int getContentViewHeight() {
        View contentView = popupWindow.getContentView();
        int height = contentView.getHeight();
        if (height == 0) {
            contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            return contentView.getMeasuredHeight();
        } else {
            return height;
        }
    }

    public interface OnPrepareShowListener {
        void onPrepareShow();
    }

}