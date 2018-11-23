package com.github.cqrframe.imagepicker.interfaces;

import android.support.v4.app.FragmentManager;

import java.io.File;

/**
 * 大图预览回调
 * <p>
 * Created by liyujiang on 2018/11/12 16:07
 */
public interface ImagePreviewCallback {
    void onBigImagePreview(FragmentManager fragmentManager, File file);
}
