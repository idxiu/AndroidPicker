package com.github.cqrframe.imagepicker.interfaces;

import com.github.cqrframe.imagepicker.bean.AlbumBean;

/**
 * 类描述
 * <p>
 * Created by liyujiang on 2018/11/13 10:05
 */
public interface OnAlbumChooseListener {

    void onShowing();

    void onHidden();

    void onChoose(AlbumBean bean);

}
