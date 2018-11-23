package com.github.cqrframe.imagepicker.interfaces;

import android.widget.ImageView;

import java.io.File;

public interface ImageLoader {
    void display(ImageView imageView, File file);
}
