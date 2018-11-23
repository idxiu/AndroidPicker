package com.github.cqrframe.popup;

import android.support.v4.app.FragmentActivity;

import com.github.cqrframe.toolkit.CqrConvertUtils;

/**
 * 通用的底部弹窗
 * <p>
 * Created by liyujiang on 2018/10/19 12:19
 */
public abstract class CqrBottomPopup<P extends CqrBottomPopup> extends CqrBasePopup<P> {

    public CqrBottomPopup(FragmentActivity activity) {
        super(activity);
        setShowMaskAlpha(true);
        setHeight(CqrConvertUtils.dp2px(activity,320));
    }

    public void show() {
        super.showAtBottom();
    }

}
