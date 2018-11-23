package com.github.cqrframe.interfaces;

import android.arch.lifecycle.Observer;

/**
 * 视图模型接口，负责处理业务逻辑。
 * <p>
 * 在{@link CqrIViewModel}中获取{@link CqrIRepository}对象，通过{@link CqrIRepository}更新数据；
 * 操作数据的方法中，提供一个{@link Observer}，用来在{@link CqrIUiController}中回调更新界面；
 * 注意：不允许持有Activity/Fragment/Dialog/PopupWindow/View的引用；
 * <p>
 * Created by liyujiang on 2018/09/18 21:40
 */
public interface CqrIViewModel {

    <R extends CqrIRepository> R obtainRepository(Class<R> repositoryClass);

}
