package com.github.cqrframe.popup;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.cqrframe.toolkit.CqrConvertUtils;

/**
 * 通用的确认/取消弹窗
 * <p>
 * Created by liyujiang on 2018/11/23 17:20
 */
@SuppressWarnings("WeakerAccess")
public abstract class CqrConfirmPopup<P extends CqrConfirmPopup> extends CqrBottomPopup<P> {
    protected TopStyle topStyle = TopStyle.DEFAULT;
    protected int contentLeftAndRightPadding = 0;//dp
    protected int contentTopAndBottomPadding = 0;//dp
    protected int backgroundColor = Color.WHITE;
    private View headerView, centerView, footerView;

    public CqrConfirmPopup(FragmentActivity activity) {
        super(activity);
    }

    public P setTopStyle(TopStyle topStyle) {
        this.topStyle = topStyle;
        return self();
    }

    /**
     * 设置内容上下左右边距（单位为dp）
     */
    public P setContentPadding(int leftAndRight, int topAndBottom) {
        this.contentLeftAndRightPadding = leftAndRight;
        this.contentTopAndBottomPadding = topAndBottom;
        return self();
    }

    /**
     * 设置选择器主体背景颜色
     */
    public P setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return self();
    }

    public P setHeaderView(View headerView) {
        this.headerView = headerView;
        return self();
    }

    public P setFooterView(View footerView) {
        this.footerView = footerView;
        return self();
    }

    /**
     * @see #createHeaderView(FragmentActivity)
     * @see #createCenterView(FragmentActivity)
     * @see #createFooterView(FragmentActivity)
     */
    @Override
    protected View createContentView(@NonNull FragmentActivity activity) {
        LinearLayout rootLayout = new LinearLayout(activity);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        rootLayout.setBackgroundColor(backgroundColor);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setGravity(Gravity.CENTER);
        rootLayout.setPadding(0, 0, 0, 0);
        rootLayout.setClipToPadding(false);
        View headerView = createHeaderView(activity);
        if (headerView != null) {
            rootLayout.addView(headerView);
        }
        if (topStyle.lineVisible) {
            View lineView = new View(activity);
            lineView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, topStyle.lineHeightPixels));
            lineView.setBackgroundColor(topStyle.lineColor);
            rootLayout.addView(lineView);
        }
        if (centerView == null) {
            centerView = createCenterView(activity);
        }
        int lr = 0;
        int tb = 0;
        if (contentLeftAndRightPadding > 0) {
            lr = CqrConvertUtils.dp2px(activity, contentLeftAndRightPadding);
        }
        if (contentTopAndBottomPadding > 0) {
            tb = CqrConvertUtils.dp2px(activity, contentTopAndBottomPadding);
        }
        centerView.setPadding(lr, tb, lr, tb);
        ViewGroup vg = (ViewGroup) centerView.getParent();
        if (vg != null) {
            //IllegalStateException: The specified child already has a parent
            vg.removeView(centerView);
        }
        rootLayout.addView(centerView, new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1.0f));
        View footerView = createFooterView(activity);
        if (footerView != null) {
            rootLayout.addView(footerView);
        }
        return rootLayout;
    }

    @Nullable
    protected View createHeaderView(@NonNull FragmentActivity activity) {
        if (null != headerView) {
            return headerView;
        }
        RelativeLayout topButtonLayout = new RelativeLayout(activity);
        int height = CqrConvertUtils.dp2px(this.activity, topStyle.height);
        topButtonLayout.setLayoutParams(new RelativeLayout.LayoutParams(MATCH_PARENT, height));
        topButtonLayout.setBackgroundColor(topStyle.backgroundColor);
        topButtonLayout.setGravity(Gravity.CENTER_VERTICAL);
        topButtonLayout.addView(createCancelButton(activity));
        View titleView = createTitleView(activity);
        if (titleView != null) {
            topButtonLayout.addView(titleView);
        }
        topButtonLayout.addView(createSubmitButton(activity));
        return topButtonLayout;
    }

    @NonNull
    protected View createSubmitButton(@NonNull FragmentActivity activity) {
        final TextView submitButton = new TextView(activity);
        RelativeLayout.LayoutParams submitParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
        submitParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        submitParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        submitButton.setLayoutParams(submitParams);
        submitButton.setBackgroundColor(Color.TRANSPARENT);
        submitButton.setGravity(Gravity.CENTER);
        int padding = CqrConvertUtils.dp2px(activity, topStyle.padding);
        submitButton.setPadding(padding, 0, padding, 0);
        if (!TextUtils.isEmpty(topStyle.submitText)) {
            submitButton.setText(topStyle.submitText);
        }
        submitButton.setTextColor(CqrConvertUtils.toColorStateList(topStyle.submitTextColor, topStyle.pressedTextColor));
        if (topStyle.submitTextSize != 0) {
            submitButton.setTextSize(topStyle.submitTextSize);
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onSubmit(submitButton);
            }
        });
        return submitButton;
    }

    @Nullable
    protected View createTitleView(@NonNull FragmentActivity activity) {
        if (!TextUtils.isEmpty(topStyle.titleText)) {
            TextView titleView = new TextView(activity);
            RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            int margin = CqrConvertUtils.dp2px(activity, topStyle.padding);
            titleParams.leftMargin = margin;
            titleParams.rightMargin = margin;
            titleParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            titleParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            titleView.setLayoutParams(titleParams);
            titleView.setGravity(Gravity.CENTER);
            titleView.setText(topStyle.titleText);
            titleView.setTextColor(topStyle.titleTextColor);
            titleView.setTextSize(topStyle.titleTextSize);
            topStyle.titleView = titleView;
        }
        return topStyle.titleView;
    }

    @NonNull
    protected View createCancelButton(@NonNull FragmentActivity activity) {
        final TextView cancelButton = new TextView(activity);
        cancelButton.setVisibility(topStyle.cancelVisible ? View.VISIBLE : View.GONE);
        RelativeLayout.LayoutParams cancelParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
        cancelParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        cancelParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        cancelButton.setLayoutParams(cancelParams);
        cancelButton.setBackgroundColor(Color.TRANSPARENT);
        cancelButton.setGravity(Gravity.CENTER);
        int padding = CqrConvertUtils.dp2px(activity, topStyle.padding);
        cancelButton.setPadding(padding, 0, padding, 0);
        if (!TextUtils.isEmpty(topStyle.cancelText)) {
            cancelButton.setText(topStyle.cancelText);
        }
        cancelButton.setTextColor(CqrConvertUtils.toColorStateList(topStyle.cancelTextColor, topStyle.pressedTextColor));
        if (topStyle.cancelTextSize != 0) {
            cancelButton.setTextSize(topStyle.cancelTextSize);
        }
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onCancel(cancelButton);
            }
        });
        return cancelButton;
    }

    @NonNull
    protected abstract View createCenterView(@NonNull FragmentActivity activity);

    @Nullable
    protected View createFooterView(@NonNull FragmentActivity activity) {
        if (null != footerView) {
            return footerView;
        }
        return null;
    }

    protected void onSubmit(TextView view) {
        if (topStyle.submitClickListener != null) {
            topStyle.submitClickListener.onClick(view);
        }
    }

    protected void onCancel(TextView view) {
        if (topStyle.cancelClickListener != null) {
            topStyle.cancelClickListener.onClick(view);
        }
    }

    public static class TopStyle {
        public static final TopStyle DEFAULT = new TopStyle();
        public boolean lineVisible = true;
        @ColorInt
        public int lineColor = 0xFF33B5E5;
        public int lineHeightPixels = 1;//px
        @ColorInt
        public int backgroundColor = Color.WHITE;
        public int height = 40;//dp
        public int padding = 15;//dp
        public boolean cancelVisible = true;
        public CharSequence cancelText = "取消";
        public CharSequence submitText = "确定";
        public CharSequence titleText = "";
        @ColorInt
        public int cancelTextColor = 0xFF33B5E5;
        @ColorInt
        public int submitTextColor = 0xFF33B5E5;
        @ColorInt
        public int titleTextColor = Color.BLACK;
        @ColorInt
        public int pressedTextColor = 0XFF0288CE;
        public int cancelTextSize = 14;//sp
        public int submitTextSize = 14;//sp
        public View.OnClickListener submitClickListener = null;
        public View.OnClickListener cancelClickListener = null;
        public int titleTextSize = 13;//sp
        public View titleView = null;
    }

}
