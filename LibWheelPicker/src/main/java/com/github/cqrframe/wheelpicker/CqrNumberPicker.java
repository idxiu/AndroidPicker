package com.github.cqrframe.wheelpicker;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.github.cqrframe.widget.CqrWheelView;

import java.util.ArrayList;

/**
 * 数字范围选择器
 * <p>
 * Created by liyujiang on 2018/11/5 17:40
 */
public class CqrNumberPicker extends CqrOptionPicker<CqrNumberPicker.NumberItem> {

    public CqrNumberPicker(FragmentActivity activity) {
        super(activity);
    }

    public void setRange(int start, int end) {
        setRange(start, end, 1);
    }

    public void setRange(int start, int end, int step) {
        final ArrayList<NumberItem> list = new ArrayList<>();
        for (int n = start; n < end; n = n + step) {
            list.add(new NumberItem(n));
        }
        super.setAdapter(new OptionAdapter<NumberItem>(list) {
            @Override
            public NumberItem getDefaultItem() {
                return list.get(0);
            }
        });
    }

    public void setRange(float start, float end) {
        setRange(start, end, 0.5F);
    }

    public void setRange(float start, float end, float step) {
        final ArrayList<NumberItem> list = new ArrayList<>();
        for (float n = start; n < end; n = n + step) {
            list.add(new NumberItem(n));
        }
        super.setAdapter(new OptionAdapter<NumberItem>(list) {
            @Override
            public NumberItem getDefaultItem() {
                return list.get(0);
            }
        });
    }

    public void setOnNumberPickListener(final OnNumberPickListener onPickListener) {
        super.setOnPickListener(new OnPickListener<NumberItem>() {
            @Override
            public void onItemPicked(int position, NumberItem item) {
                onPickListener.onItemPicked(position, item.number);
            }
        });
    }

    public interface OnNumberPickListener {
        void onItemPicked(int position, Number item);
    }

    static class NumberItem implements CqrWheelView.Item {
        private Number number;

        public NumberItem(Number number) {
            this.number = number;
        }

        @Override
        public String getItemText() {
            return number.toString();
        }

        @NonNull
        @Override
        public String toString() {
            return number.toString();
        }

    }

}
