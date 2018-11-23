package com.github.cqrframe.imagepicker.task;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import com.github.cqrframe.imagepicker.interfaces.ImageQueryCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询手机中的所有图片
 * <p>
 * Created by liyujiang on 2018/11/12 16:20
 */
public class ImageQueryTask extends AsyncTask<Void, Void, Map<File, List<File>>> {
    private ImageQueryCallback callback;

    public ImageQueryTask(ImageQueryCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Map<File, List<File>> doInBackground(Void... voids) {
        ContentResolver contentResolver = callback.getContext().getContentResolver();
        if (contentResolver == null) {
            return null;
        }
        Uri uri;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else {
            uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        }
        Cursor cursor = contentResolver.query(uri, null, null, null,
                MediaStore.Images.Media.DATE_MODIFIED + " DESC");
        if (cursor == null) {
            return null;
        }
        Map<File, List<File>> map = new LinkedHashMap<>();
        while (cursor.moveToNext()) {
            //获取图片的路径
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            File file = new File(path);
            if (file.exists()) {
                //获取相册（图片所在目录）
                File parentFile = file.getParentFile();
                List<File> list = map.get(parentFile);
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(file);
                map.put(parentFile, list);
            }
        }
        cursor.close();
        return map;
    }

    @Override
    protected void onPostExecute(Map<File, List<File>> map) {
        if (map == null) {
            map = new HashMap<>();
        }
        if (callback != null) {
            callback.onImageQueried(map);
        }
    }

}