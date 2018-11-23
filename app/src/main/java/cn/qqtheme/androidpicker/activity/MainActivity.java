package cn.qqtheme.androidpicker.activity;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import com.github.cqrframe.toolkit.CqrConvertUtils;
import com.github.cqrframe.wheelpicker.CqrNumberPicker;
import com.github.cqrframe.wheelpicker.CqrOptionPicker;
import com.github.cqrframe.wheelpicker.CqrTextPicker;

import java.util.ArrayList;
import java.util.List;

import cn.qqtheme.androidpicker.MyPickerApp;
import cn.qqtheme.androidpicker.bean.GoodsCategory;
import cn.qqtheme.androidpicker.R;
import cn.qqtheme.androidpicker.base.BaseActivity;
import cn.github.cqrframe.colorpicker.ColorPicker;
import cn.qqtheme.androidpicker.picker.ConstellationPicker;
import cn.qqtheme.androidpicker.picker.MultiplePicker;

public class MainActivity extends BaseActivity {

    @Override
    protected View getContentView() {
        return inflateView(R.layout.activity_main);
    }

    @Override
    protected void setContentViewAfter(View contentView) {

    }

    @Override
    public void onBackPressed() {
        MyPickerApp.exitApp(true);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void onNumberPicker(View view) {
        final CqrNumberPicker picker = new CqrNumberPicker(this);
        picker.setItemWidth(200);
        picker.setAnimationStyle(R.style.Animation_CustomPopup);
        picker.setRange(10.5F, 20F, 1.5F);//数字范围
        picker.setLabel("℃");
        picker.setOnNumberPickListener(new CqrNumberPicker.OnNumberPickListener() {
            @Override
            public void onItemPicked(int position, Number item) {
                showToast(String.valueOf(item.floatValue()));
            }
        });
        picker.show();
        picker.getWheelView().setCyclic(false);
    }

    public void onOptionPicker(View view) {
        CqrTextPicker picker = new CqrTextPicker(this);
        picker.setItems(new String[]{
                "第一项", "第二项", "第三项", "第四项", "第五项", "第六项", "第七项",
                "这是一个很长很长很长很长很长很长很长很长很长的很长很长的很长很长的项"
        }, 0);
        picker.setOnTextPickListener(new CqrTextPicker.OnTextPickListener() {
            @Override
            public void onItemPicked(int position, String item) {
                showToast("position=" + position + ", item=" + item);
            }
        });
        picker.show();
        picker.getWheelView().setCyclic(true);
    }

    public void onConstellationPicker(View view) {
        ConstellationPicker picker = new ConstellationPicker(this);
        picker.setOnTextPickListener(new CqrTextPicker.OnTextPickListener() {
            @Override
            public void onItemPicked(int position, String item) {
                showToast("position=" + position + ", item=" + item);
            }
        });
        picker.show();
    }

    public void onSinglePicker(View view) {
        final List<GoodsCategory> data = new ArrayList<>();
        data.add(new GoodsCategory(1, "食品生鲜"));
        data.add(new GoodsCategory(2, "家用电器"));
        data.add(new GoodsCategory(3, "家居生活"));
        data.add(new GoodsCategory(4, "医疗保健"));
        data.add(new GoodsCategory(5, "酒水饮料"));
        data.add(new GoodsCategory(6, "图书音像"));
        CqrOptionPicker<GoodsCategory> picker = new CqrOptionPicker<>(this);
        picker.setAdapter(new CqrOptionPicker.OptionAdapter<GoodsCategory>(data) {
            @Override
            public GoodsCategory getDefaultItem() {
                return data.get(0);
            }
        });
        picker.setOnPickListener(new CqrOptionPicker.OnPickListener<GoodsCategory>() {
            @Override
            public void onItemPicked(int index, GoodsCategory item) {
                showToast("index=" + index + ", id=" + item.getId() + ", name=" + item.getName());
            }
        });
        picker.show();
    }

    public void onMultiplePicker(View view) {
        MultiplePicker picker = new MultiplePicker(this, new String[]{"穿青人", "少数民族", "已识别民族", "未定民族"});
        picker.setOnItemPickListener(new MultiplePicker.OnItemPickListener() {
            @Override
            public void onItemPicked(int count, List<String> items) {
                showToast("已选" + count + "项：" + items);
            }
        });
        picker.show();
    }

    public void onColorPicker(View view) {
        ColorPicker picker = new ColorPicker(this);
        picker.setInitColor(0xFFDD00DD);
        picker.setOnColorPickListener(new ColorPicker.OnColorPickListener() {
            @Override
            public void onColorPicked(int pickedColor) {
                showToast(CqrConvertUtils.toColorHexString(pickedColor));
            }
        });
        picker.show();
    }

    public void onContact(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:liyujiang_tk@yeah.net"));
        intent.putExtra(Intent.EXTRA_CC, new String[]
                {"1032694760@qq.com"});
        intent.putExtra(Intent.EXTRA_EMAIL, "");
        intent.putExtra(Intent.EXTRA_TEXT, "欢迎提供意您的见或建议");
        startActivity(Intent.createChooser(intent, "选择邮件客户端"));
    }

}
