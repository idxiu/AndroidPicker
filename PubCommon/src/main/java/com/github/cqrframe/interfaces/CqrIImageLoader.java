package com.github.cqrframe.interfaces;

import android.app.Application;
import android.content.Context;
import android.support.annotation.MainThread;
import android.widget.ImageView;

/**
 * 设计思想是使用接口对各模块解耦规范化，不强依赖某些明确的三方库，使得三方库可自由搭配组装。
 * <p>
 * 集成第三方图片加载框架（如：Glide、Picasso、Universal-Image-Loader、Fresco），
 * <p>
 * Glide：http://github.com/bumptech/glide
 * UIL：https://github.com/nostra13/Android-Universal-Image-Loader
 * Picasso：https://github.com/square/picasso
 * Fresco：https://github.com/facebook/fresco
 * <p>
 * Created by liyujiang on 2015/12/9.
 */
public interface CqrIImageLoader {

    @MainThread
    void init(Application application);

    /**
     * 图片加载方法
     * <p>
     * （默认图片在实现类中实现，此方法主要是全局调用，默认图片统一，避免每次都要传入默认图片）
     *
     * @param imageView 图片视图
     * @param urlOrPath 泛型，图片链接可以是string、uri、file、assets、res等多中类型
     */
    @MainThread
    <T> void display(ImageView imageView, T urlOrPath);

    /**
     * 图片加载方法
     * <p>
     * （默认图片可以自己每次单独设置，主要满足软件一些地方可能默认图片不一样的情况）
     *
     * @param imageView   图片视图
     * @param urlOrPath   泛型，图片地址，可以是string、uri、file、assets、res等多中类型
     * @param placeholder 默认占位图
     */
    @MainThread
    <T> void display(ImageView imageView, T urlOrPath, int placeholder);

    void clearCache(Context context);

    long getCacheSize(Context context);

}
