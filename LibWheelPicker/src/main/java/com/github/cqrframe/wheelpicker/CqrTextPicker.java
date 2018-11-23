package com.github.cqrframe.wheelpicker;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.github.cqrframe.widget.CqrWheelView;

import java.util.ArrayList;

/**
 * 文本单项选择器
 * <p>
 * Created by liyujiang on 2018/11/5 17:30
 */
public class CqrTextPicker extends CqrOptionPicker<CqrTextPicker.StringItem> {

    public CqrTextPicker(FragmentActivity activity) {
        super(activity);
    }

    public void setItems(String[] items, final int position) {
        final ArrayList<StringItem> list = new ArrayList<>();
        for (String item : items) {
            list.add(new StringItem(item));
        }
        super.setAdapter(new OptionAdapter<StringItem>(list) {
            @Override
            public StringItem getDefaultItem() {
                return list.get(position);
            }
        });
    }

    public void setOnTextPickListener(final OnTextPickListener onPickListener) {
        super.setOnPickListener(new OnPickListener<StringItem>() {
            @Override
            public void onItemPicked(int position, StringItem item) {
                onPickListener.onItemPicked(position, item.getItemText());
            }
        });
    }

    public interface OnTextPickListener {
        void onItemPicked(int position, String item);
    }

    static class StringItem implements CqrWheelView.Item {
        private String name;

        public StringItem(String name) {
            this.name = name;
        }

        @Override
        public String getItemText() {
            return name;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }

    }

}
