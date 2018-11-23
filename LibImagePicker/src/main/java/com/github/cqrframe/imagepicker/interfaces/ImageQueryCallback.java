package com.github.cqrframe.imagepicker.interfaces;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 类描述
 * <p>
 * Created by liyujiang on 2018/11/12 16:25
 */
public interface ImageQueryCallback {
    @NonNull
    Context getContext();

    void onImageQueried(@NonNull Map<File, List<File>> images);
}
