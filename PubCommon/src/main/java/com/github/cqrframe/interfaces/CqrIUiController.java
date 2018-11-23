package com.github.cqrframe.interfaces;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * 视图控制器接口，一般为{@link FragmentActivity}或{@link Fragment}。
 * <p>
 * 在{@link CqrIUiController}中获取{@link CqrIViewModel}对象，通过{@link CqrIViewModel}回调更新界面
 * <p>
 * Created by liyujiang on 2018/09/18 21:36
 */
public interface CqrIUiController {

    <VM extends CqrIViewModel> VM obtainViewModel(Class<VM> vmClass);

}
