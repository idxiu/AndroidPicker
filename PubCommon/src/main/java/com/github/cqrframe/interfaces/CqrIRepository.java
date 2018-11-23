package com.github.cqrframe.interfaces;

import android.arch.lifecycle.LiveData;

/**
 * 数据仓库接口。
 * <p>
 * 将数据包装到{@link LiveData}并暴露相关方法给{@link CqrIViewModel}；
 * {@link CqrIViewModel}通过{@link LiveData}更新{@link CqrIUiController}及{@link CqrIModel}；
 * 数据源可以是Memory、Database、Preferences、File、Remote等；
 * <p>
 * Created by liyujiang on 2018/9/19 11:04
 */
public interface CqrIRepository {

}
