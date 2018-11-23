package com.github.cqrframe.imagepicker.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.cqrframe.imagepicker.R;
import com.github.cqrframe.imagepicker.adapter.ImageAdapter;
import com.github.cqrframe.imagepicker.bean.AlbumBean;
import com.github.cqrframe.imagepicker.interfaces.ImagePreviewCallback;
import com.github.cqrframe.imagepicker.interfaces.ImageQueryCallback;
import com.github.cqrframe.imagepicker.interfaces.OnAlbumChooseListener;
import com.github.cqrframe.imagepicker.interfaces.OnMarkCheckedChangeListener;
import com.github.cqrframe.imagepicker.interfaces.ImagePickCallback;
import com.github.cqrframe.imagepicker.task.ImageQueryTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImagePickFragment extends DialogFragment implements View.OnClickListener, OnAlbumChooseListener {
    private static final String ALBUM_ALL = "所有图片";
    private ImageAdapter adapter;
    private boolean fullScreen = false;
    private int count = 1;
    private Map<File, List<File>> images = new HashMap<>();
    private List<File> allImages = new ArrayList<>();
    private List<File> albumImages = new ArrayList<>();
    private List<AlbumBean> allAlbums = new ArrayList<>();
    private AlbumChooser albumChooser;

    private TextView tvAlbumName;
    private ImageView ivAlbumArrow;
    private TextView tv_confirm;

    private ImagePickCallback imagePickCallback;
    private ImagePreviewCallback bigImagePreviewCallback;

    public static ImagePickFragment newInstance(boolean fullScreen, int count) {
        ImagePickFragment fragment = new ImagePickFragment();
        Bundle args = new Bundle();
        args.putBoolean("fullScreen", fullScreen);
        args.putInt("count", count);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            count = arguments.getInt("count", count);
            fullScreen = arguments.getBoolean("fullScreen", fullScreen);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        if (window == null) {
            return;
        }
        window.requestFeature(Window.FEATURE_NO_TITLE);
        super.onActivityCreated(savedInstanceState);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int height = WindowManager.LayoutParams.MATCH_PARENT;
        if (!fullScreen) {
            height = (int) (getDialog().getContext().getResources().getDisplayMetrics().heightPixels * 0.7);
        }
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.height = height;
        attributes.gravity = Gravity.BOTTOM;
        window.setAttributes(attributes);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image_pick, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initToolbar(view);
        initAlbumChooser(view);
        initRecyclerView(view);
        initAlbumAndImages(view);
    }

    private void initToolbar(View view) {
        view.findViewById(R.id.image_pick_back).setOnClickListener(this);
        view.findViewById(R.id.image_pick_album_container).setOnClickListener(this);
        tvAlbumName = view.findViewById(R.id.image_pick_album_text);
        ivAlbumArrow = view.findViewById(R.id.image_pick_album_image);
        tv_confirm = view.findViewById(R.id.image_pick_confirm);
        tv_confirm.setText(String.format("确定(0/%s)", count));
        tv_confirm.setOnClickListener(this);
    }

    private void initAlbumChooser(View view) {
        albumChooser = new AlbumChooser(view.getContext());
        albumChooser.setOnAlbumChooseListener(this);
    }

    private void initRecyclerView(final View contentView) {
        RecyclerView recyclerView = contentView.findViewById(R.id.image_pick_thumb_list);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new ImageAdapter(count);
        recyclerView.setAdapter(adapter);
        adapter.setMarkCheckedChangeListener(new OnMarkCheckedChangeListener() {
            @Override
            public void onSizeMaxLimit(int count) {
                Toast.makeText(contentView.getContext(), "最多只能选择" + count + "张图", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCheckedChanged(boolean isChecked, int position) {
                tv_confirm.setText(String.format("确定(%s/%s)", adapter.getPickedImages().size(), count));
            }
        });
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter a, View view, int position) {
                File file = adapter.getData().get(position);
                if (bigImagePreviewCallback != null) {
                    bigImagePreviewCallback.onBigImagePreview(getChildFragmentManager(), file);
                } else {
                    BigImagePreviewer imagePreviewer = new BigImagePreviewer(view.getContext(), file);
                    imagePreviewer.showAtLocation(contentView, Gravity.CENTER, 0, 0);
                }
            }
        });
    }

    private void initAlbumAndImages(final View view) {
        new ImageQueryTask(new ImageQueryCallback() {
            @NonNull
            @Override
            public Context getContext() {
                return view.getContext();
            }

            @Override
            public void onImageQueried(@NonNull Map<File, List<File>> images) {
                initAlbumAndImages(images);
            }
        }).execute();
    }

    private void initAlbumAndImages(Map<File, List<File>> images) {
        this.images = images;
        allAlbums.clear();
        allImages.clear();
        for (File album : images.keySet()) {
            List<File> list = images.get(album);
            if (list != null && list.size() > 0) {
                AlbumBean bean = new AlbumBean(album, list.get(0), list.size());
                allAlbums.add(bean);
                allImages.addAll(list);
            }
        }
        if (allImages.size() > 0) {
            allAlbums.add(0, new AlbumBean(new File(ALBUM_ALL), allImages.get(0), allImages.size()));
        }
        albumChooser.setData(allAlbums);
        initRecyclerViewData(allImages);
    }

    private void initRecyclerViewData(List<File> images) {
        albumImages = images;
        adapter.setNewData(albumImages);
    }

    @Override
    public void onShowing() {
        //箭头旋转90度
        ivAlbumArrow.animate().rotation(-90).setDuration(300).start();
    }

    @Override
    public void onHidden() {
        //箭头复原
        ivAlbumArrow.animate().rotation(0).setDuration(300).start();
    }

    @Override
    public void onChoose(AlbumBean bean) {
        File file = bean.getDir();
        String name = file.getName();
        tvAlbumName.setText(name);
        if (name.equals(ALBUM_ALL)) {
            albumImages = allImages;
        } else {
            albumImages = images.get(file);
        }
        initRecyclerViewData(albumImages);
    }

    public void setImagePickCallback(ImagePickCallback selectImagesCallBack) {
        this.imagePickCallback = selectImagesCallBack;
    }

    public void setBigImagePreviewCallback(ImagePreviewCallback bigImagePreviewCallback) {
        this.bigImagePreviewCallback = bigImagePreviewCallback;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.image_pick_back) {
            dismiss();
        } else if (i == R.id.image_pick_album_container) {
            if (!albumChooser.isShowing()) {
                albumChooser.showAsDropDown(v);
            }
        } else if (i == R.id.image_pick_confirm) {
            dismiss();
            if (imagePickCallback != null) {
                imagePickCallback.pickedImages(adapter.getPickedImages());
            }
        }
    }

}
