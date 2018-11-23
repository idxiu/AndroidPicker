package com.github.cqrframe.imagepicker.adapter;

import android.support.annotation.IntRange;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.cqrframe.imagepicker.ImagePicker;
import com.github.cqrframe.imagepicker.R;
import com.github.cqrframe.imagepicker.interfaces.ImageLoader;
import com.github.cqrframe.imagepicker.interfaces.OnMarkCheckedChangeListener;
import com.github.cqrframe.imagepicker.ui.MarkCheckBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 类描述
 * <p>
 * Created by liyujiang on 2018/11/12 16:55
 */
public class ImageAdapter extends BaseQuickAdapter<File, BaseViewHolder> {
    private int maxCount;
    private List<File> pickedImages;
    private OnMarkCheckedChangeListener onMarkCheckedChangeListener;

    public ImageAdapter(@IntRange(from = 1) int maxCount) {
        super(R.layout.adapter_image_pick_list);
        this.maxCount = maxCount;
        this.pickedImages = new ArrayList<>();
    }

    @Override
    protected void convert(final BaseViewHolder helper, final File item) {
        ImageView imageView = helper.getView(R.id.image_pick_item_thumb);
        final MarkCheckBox checkBox = helper.getView(R.id.image_pick_item_checked);
        ImageLoader loader = ImagePicker.getImageLoader();
        if (loader != null) {
            loader.display(imageView, item);
        }
        checkBox.setOnClickListener(null);
        checkBox.setVisibility(View.VISIBLE);
        int index = pickedImages.indexOf(item);
        if (index == -1) {
            checkBox.setChecked(false);
            checkBox.setMarkText("");
        } else {
            checkBox.setChecked(true);
            if (maxCount == 1) {
                checkBox.setMarkText("√");
            } else {
                checkBox.setMarkText(String.valueOf(index + 1));
            }
        }
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maxCount == 1) { //单选
                    pickedImages.clear();
                }
                boolean isChecked = checkBox.isChecked();
                if (isChecked) {
                    if (maxCount > 1 && pickedImages.size() >= maxCount) { //多选达到最大限制数
                        checkBox.setChecked(false);
                        if (onMarkCheckedChangeListener != null) {
                            onMarkCheckedChangeListener.onSizeMaxLimit(maxCount);
                        }
                        return;
                    }
                    pickedImages.add(item);
                } else {
                    pickedImages.remove(item);
                }
                notifyDataSetChanged();
                if (onMarkCheckedChangeListener != null) {
                    onMarkCheckedChangeListener.onCheckedChanged(isChecked, helper.getAdapterPosition());
                }
            }
        });
    }

    public List<File> getPickedImages() {
        return pickedImages;
    }

    public void setMarkCheckedChangeListener(OnMarkCheckedChangeListener onMarkCheckedChangeListener) {
        this.onMarkCheckedChangeListener = onMarkCheckedChangeListener;
    }

}