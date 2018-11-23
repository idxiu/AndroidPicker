package com.github.cqrframe.imagepicker.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.cqrframe.imagepicker.adapter.AlbumAdapter;
import com.github.cqrframe.imagepicker.bean.AlbumBean;
import com.github.cqrframe.imagepicker.interfaces.OnAlbumChooseListener;

import java.util.List;

/**
 * 相册选择器
 * <p>
 * Created by liyujiang on 2018/11/13 10:00
 */
public class AlbumChooser extends PopupWindow implements PopupWindow.OnDismissListener {
    private RecyclerView recyclerView;
    private AlbumAdapter adapter;
    private OnAlbumChooseListener onAlbumChooseListener;

    AlbumChooser(Context context) {
        super();
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(context.getResources().getDisplayMetrics().heightPixels / 2);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(initContentView(context));
        setOnDismissListener(this);
    }

    private RecyclerView initContentView(Context context) {
        if (recyclerView != null) {
            return recyclerView;
        }
        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setBackgroundColor(Color.WHITE);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        adapter = new AlbumAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter a, View view, int position) {
                dismiss();
                if (onAlbumChooseListener != null) {
                    onAlbumChooseListener.onChoose(adapter.getData().get(position));
                }
            }
        });
        return recyclerView;
    }

    public void setData(@NonNull List<AlbumBean> imagePackages) {
        adapter.setNewData(imagePackages);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        super.showAsDropDown(anchor, xoff, yoff, gravity);
        if (onAlbumChooseListener != null) {
            onAlbumChooseListener.onShowing();
        }
    }

    @Override
    public void onDismiss() {
        if (onAlbumChooseListener != null) {
            onAlbumChooseListener.onHidden();
        }
    }

    public void setOnAlbumChooseListener(OnAlbumChooseListener onAlbumChooseListener) {
        this.onAlbumChooseListener = onAlbumChooseListener;
    }

}
