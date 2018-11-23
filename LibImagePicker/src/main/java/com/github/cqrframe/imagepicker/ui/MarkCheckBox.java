package com.github.cqrframe.imagepicker.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.github.cqrframe.imagepicker.R;

/**
 * Created by Administrator on 2018/9/25.
 */

public class MarkCheckBox extends CompoundButton {

    private Paint paint;
    private int textColor = Color.BLACK;
    private String markText = "";
    private float markTextSize = 6;

    public MarkCheckBox(Context context) {
        this(context, null);
    }

    public MarkCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.checkboxStyle);
    }

    public MarkCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MarkCheckBox);
        textColor = array.getColor(R.styleable.MarkCheckBox_mcb_textColor, textColor);
        markText = array.getString(R.styleable.MarkCheckBox_mcb_markText);
        markTextSize = array.getDimension(R.styleable.MarkCheckBox_mcb_markTextSize, dip2px(context, 6));
        array.recycle();


        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(textColor);
        paint.setTextSize(markTextSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void toggle() {
        // we override to prevent toggle when the radio is already
        // checked (as opposed to check boxes widgets)
        setChecked(!isChecked());
        invalidate();
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isChecked() && !TextUtils.isEmpty(markText)) {
            Rect bounds = new Rect();
            paint.getTextBounds(markText, 0, markText.length(), bounds);
            Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
            int dy = (Math.abs(fontMetrics.bottom) + Math.abs(fontMetrics.top)) / 2 - Math.abs(fontMetrics.bottom);
            int baseLine = ((getHeight() - getPaddingTop() - getPaddingBottom())/2 + getPaddingTop())+dy;
            canvas.drawText(markText,  (getWidth()-getPaddingLeft()-getPaddingLeft())/2-dip2px(getContext(),0.5f) +getPaddingLeft(), baseLine, paint);
        }
    }

    public void setMarkText(String markText) {
        this.markText = markText;
        invalidate();
    }


    /**
     * 根据手机分辨率从DP转成PX
     *
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
