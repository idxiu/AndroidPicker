package com.github.cqrframe.imagepicker.ui;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.github.cqrframe.imagepicker.ImagePicker;
import com.github.cqrframe.imagepicker.interfaces.ImageLoader;

import java.io.File;

/**
 * 大图预览
 * <p>
 * Created by liyujiang on 2018/11/13 11:10
 */
public class BigImagePreviewer extends PopupWindow {
    private ImageView imageView;
    private File file;

    BigImagePreviewer(Context context, File file) {
        super();
        this.file = file;
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(0x80333333));
        setContentView(initContentView(context));
    }

    private ImageView initContentView(Context context) {
        if (imageView != null) {
            return imageView;
        }
        imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setPadding(80, 80, 80, 80);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ImageLoader loader = ImagePicker.getImageLoader();
        if (loader != null) {
            loader.display(imageView, file);
        }
        return imageView;
    }

}
