package com.github.cqrframe.imagepicker;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.github.cqrframe.imagepicker.interfaces.ImagePreviewCallback;
import com.github.cqrframe.imagepicker.interfaces.ImagePickCallback;
import com.github.cqrframe.imagepicker.ui.ImagePickFragment;
import com.github.cqrframe.imagepicker.interfaces.ImageLoader;

/**
 * 通用的图片选择器
 * <p>
 * Created by liyujiang on 2018/11/12 16:32
 */
public class ImagePicker {
    private static ImageLoader imageLoader;

    private ImagePicker() {
        super();
    }

    public static ImageLoader getImageLoader() {
        return imageLoader;
    }

    public static Builder pickImages(ImageLoader loader) {
        imageLoader = loader;
        return new Builder();
    }

    public static class Builder {
        private boolean fullScreen = false;
        private int count = 1;
        private ImagePickCallback pickCallback;
        private ImagePreviewCallback previewCallback;

        public Builder setFullScreen(boolean fullScreen) {
            this.fullScreen = fullScreen;
            return this;
        }

        public Builder setCount(@IntRange(from = 1) int count) {
            this.count = count;
            return this;
        }

        public Builder setPickCallback(ImagePickCallback pickCallback) {
            this.pickCallback = pickCallback;
            return this;
        }

        public Builder setPreviewCallback(ImagePreviewCallback previewCallback) {
            this.previewCallback = previewCallback;
            return this;
        }

        public void start(@NonNull FragmentManager manager) {
            if (count < 1) {
                throw new RuntimeException("需要选择的图片不能少于1张");
            }
            ImagePickFragment fragment = ImagePickFragment.newInstance(fullScreen, count);
            fragment.setImagePickCallback(pickCallback);
            fragment.setBigImagePreviewCallback(previewCallback);
            fragment.show(manager, "");
        }
    }

}
