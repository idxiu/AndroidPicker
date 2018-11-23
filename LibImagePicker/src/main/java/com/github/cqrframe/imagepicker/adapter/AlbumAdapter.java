package com.github.cqrframe.imagepicker.adapter;

import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.cqrframe.imagepicker.ImagePicker;
import com.github.cqrframe.imagepicker.R;
import com.github.cqrframe.imagepicker.bean.AlbumBean;
import com.github.cqrframe.imagepicker.interfaces.ImageLoader;

/**
 * 类描述
 * <p>
 * Created by liyujiang on 2018/11/13 9:56
 */
public class AlbumAdapter extends BaseQuickAdapter<AlbumBean, BaseViewHolder> {

    public AlbumAdapter() {
        super(R.layout.adapter_image_pick_album);
    }

    @Override
    protected void convert(BaseViewHolder helper, AlbumBean item) {
        ImageView imageView = helper.getView(R.id.image_pick_item_album_thumb);
        ImageLoader loader = ImagePicker.getImageLoader();
        if (loader != null) {
            loader.display(imageView, item.getThumb());
        }
        helper.setText(R.id.image_pick_item_album_text, item.getDir().getName() + "(" + item.getCount() + ")");
    }

}
