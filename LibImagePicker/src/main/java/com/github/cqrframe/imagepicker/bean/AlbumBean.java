package com.github.cqrframe.imagepicker.bean;

import java.io.File;

/**
 * 类描述
 * <p>
 * Created by liyujiang on 2018/11/13 9:55
 */
public class AlbumBean {
    private File dir;
    private File thumb;
    private int count;

    public AlbumBean(File dir, File thumb, int count) {
        this.dir = dir;
        this.thumb = thumb;
        this.count = count;
    }

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public File getThumb() {
        return thumb;
    }

    public void setThumb(File thumb) {
        this.thumb = thumb;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
