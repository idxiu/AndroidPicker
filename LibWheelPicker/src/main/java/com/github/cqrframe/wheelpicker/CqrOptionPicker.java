package com.github.cqrframe.wheelpicker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.cqrframe.popup.CqrBottomPopup;
import com.github.cqrframe.popup.CqrConfirmPopup;
import com.github.cqrframe.toolkit.CqrScreenUtils;
import com.github.cqrframe.widget.CqrWheelView;

import java.util.List;

/**
 * 单项滑轮选择器
 * <p>
 * Created by liyujiang on 2018/11/5 15:13
 */
public class CqrOptionPicker<T extends CqrWheelView.Item> extends CqrBottomPopup<CqrOptionPicker> {
    private CqrWheelView wheelView;
    private TextView labelView;
    private OnPickListener<T> onPickListener;
    private OptionAdapter<T> adapter;
    private CharSequence label;
    private int itemWidth = MATCH_PARENT;

    public CqrOptionPicker(FragmentActivity activity) {
        super(activity);
    }

    @Override
    protected View createContentView(@NonNull FragmentActivity activity) {
        View view = LayoutInflater.from(activity).inflate(R.layout.cqr_option_picker, null);
        wheelView = view.findViewById(R.id.option_picker_wheel);
        labelView = view.findViewById(R.id.option_picker_label);
        view.findViewById(R.id.option_picker_cancel).setOnClickListener(this);
        view.findViewById(R.id.option_picker_confirm).setOnClickListener(this);
        return view;
    }

    public CqrWheelView getWheelView() {
        return wheelView;
    }

    public TextView getLabelView() {
        return labelView;
    }

    public void setAdapter(OptionAdapter<T> adapter) {
        this.adapter = adapter;
    }

    public void setLabel(CharSequence label) {
        this.label = label;
    }

    public void setItemWidth(int itemWidth) {
        this.itemWidth = itemWidth;
    }

    public void setOnPickListener(OnPickListener<T> onPickListener) {
        this.onPickListener = onPickListener;
    }

    @Override
    public void onViewCreated(@NonNull View contentView) {
        wheelView.setAdapter(adapter);
        wheelView.setDefault(adapter.getDefaultItem());
        ViewGroup.LayoutParams params = wheelView.getLayoutParams();
        params.width = itemWidth;
        wheelView.setLayoutParams(params);
        labelView.setText(label);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.option_picker_cancel) {
            dismiss();
        } else if (id == R.id.option_picker_confirm) {
            dismiss();
            if (onPickListener != null) {
                //noinspection unchecked
                onPickListener.onItemPicked(wheelView.getCurrentItemPosition(), (T) wheelView.getCurrentItem());
            }
        }
    }

    public interface OnPickListener<T extends CqrWheelView.Item> {
        void onItemPicked(int position, T item);
    }

    public static abstract class OptionAdapter<T extends CqrWheelView.Item> extends CqrWheelView.Adapter<T> {

        public OptionAdapter(List<T> data) {
            super(data);
        }

        public abstract T getDefaultItem();

    }

}
