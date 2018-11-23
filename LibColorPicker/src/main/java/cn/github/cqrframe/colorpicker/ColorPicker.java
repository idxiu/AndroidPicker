package cn.github.cqrframe.colorpicker;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.cqrframe.popup.CqrConfirmPopup;
import com.github.cqrframe.toolkit.CqrConvertUtils;

import java.util.Locale;

import cn.github.cqrframe.icons.ColorPickerIcon;
import cn.github.cqrframe.widget.ColorPanelView;
import cn.github.cqrframe.widget.StrokeTextView;

/**
 * 颜色选择器。
 *
 * @author 李玉江[QQ:1032694760]
 * @since 2015/9/29
 */
public class ColorPicker extends CqrConfirmPopup<ColorPicker> {
    @SuppressLint("ResourceType")
    @IdRes
    private static final int MULTI_ID = 0x1;
    @SuppressLint("ResourceType")
    @IdRes
    private static final int BLACK_ID = 0x2;
    @ColorInt
    private int initColor = Color.WHITE;
    private ColorPanelView multiColorView, blackColorView;
    private StrokeTextView hexValView;
    //private ColorStateList hexValDefaultColor;
    private OnColorPickListener onColorPickListener;

    public ColorPicker(FragmentActivity activity) {
        super(activity);
    }

    @Nullable
    @Override
    protected View createTitleView(@NonNull FragmentActivity activity) {
        hexValView = new StrokeTextView(activity, true);//文字描边，以便背景色和文字色一样时时仍然看得见
        int height = CqrConvertUtils.dp2px(activity, 28);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(WRAP_CONTENT, height);
        int margin = CqrConvertUtils.dp2px(activity, 10);
        params.leftMargin = margin;
        params.rightMargin = margin;
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        hexValView.setLayoutParams(params);
        hexValView.setGravity(Gravity.CENTER);
        hexValView.setBackgroundColor(initColor);
        //hexValView.setTextColor(Color.BLACK);
        hexValView.setBorderColor(CqrConvertUtils.toDarkenColor(initColor, 0.6f));
        hexValView.setTextColor(initColor);
        hexValView.setShadowLayer(3, 0, 2, Color.WHITE);//设置阴影，以便背景色为黑色系列时仍然看得见
        hexValView.setMinEms(6);
        hexValView.setMaxEms(8);
        hexValView.setPadding(0, 0, 0, 0);
        hexValView.setSingleLine(true);
        hexValView.setEnabled(false);
        //hexValDefaultColor = hexValView.getTextColors();
        return hexValView;
    }

    @Override
    @NonNull
    protected LinearLayout createCenterView(@NonNull FragmentActivity activity) {
        LinearLayout rootLayout = new LinearLayout(this.activity);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        multiColorView = new ColorPanelView(this.activity);
        multiColorView.setId(MULTI_ID);
        multiColorView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1.0f));
        Drawable cursorTopDrawable = CqrConvertUtils.toDrawable(ColorPickerIcon.getCursorTop());
        multiColorView.setPointerDrawable(cursorTopDrawable);
        multiColorView.setOnColorChangedListener(new ColorPanelView.OnColorChangedListener() {
            @Override
            public void onColorChanged(ColorPanelView view, int color) {
                updateCurrentColor(color);
            }
        });
        rootLayout.addView(multiColorView);

        blackColorView = new ColorPanelView(this.activity);
        blackColorView.setId(BLACK_ID);
        blackColorView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, CqrConvertUtils.dp2px(this.activity, 30)));
        Drawable cursorBottomDrawable = CqrConvertUtils.toDrawable(ColorPickerIcon.getCursorBottom());
        blackColorView.setPointerDrawable(cursorBottomDrawable);
        blackColorView.setOnColorChangedListener(new ColorPanelView.OnColorChangedListener() {
            @Override
            public void onColorChanged(ColorPanelView view, int color) {
                updateCurrentColor(color);
            }
        });
        rootLayout.addView(blackColorView);

        return rootLayout;
    }

    @Override
    public void onViewCreated(@NonNull View contentView) {
        multiColorView.setColor(initColor);//将触发onColorChanged，故必须先待其他控件初始化完成后才能调用
        multiColorView.setBrightnessGradientView(blackColorView);
    }

    @Override
    protected void onSubmit(TextView view) {
        super.onSubmit(view);
        if (onColorPickListener != null) {
            onColorPickListener.onColorPicked(getCurrentColor());
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        //multiColorView.recycle();
        //blackColorView.recycle();
    }

    @ColorInt
    public int getCurrentColor() {
        try {
            return Color.parseColor("#" + hexValView.getText());
        } catch (Throwable ignore) {
            return Color.TRANSPARENT;
        }
    }

    private void updateCurrentColor(int color) {
        String hexColorString = CqrConvertUtils.toColorHexString(color, false).toUpperCase(Locale.getDefault());
        hexValView.setText(hexColorString);
        //hexValView.setTextColor(hexValDefaultColor);
        hexValView.setBorderColor(CqrConvertUtils.toDarkenColor(color, 0.6f));
        hexValView.setTextColor(color);
        hexValView.setBackgroundColor(color);
    }

    /**
     * 设置初始默认颜色
     *
     * @param initColor 颜色值，如：0xFFFF00FF
     */
    public void setInitColor(@ColorInt int initColor) {
        this.initColor = initColor;
    }

    public void setOnColorPickListener(OnColorPickListener onColorPickListener) {
        this.onColorPickListener = onColorPickListener;
    }

    /**
     * 颜色选择回调
     */
    public interface OnColorPickListener {

        void onColorPicked(@ColorInt int pickedColor);

    }

}
