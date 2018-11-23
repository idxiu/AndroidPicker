package cn.qqtheme.androidpicker.picker;

import android.support.v4.app.FragmentActivity;

import com.github.cqrframe.wheelpicker.CqrTextPicker;

import java.util.Locale;

/**
 * 类描述
 * <p>
 * Created by liyujiang on 2018/11/23 18:54
 */
public class ConstellationPicker extends CqrTextPicker {

    public ConstellationPicker(FragmentActivity activity) {
        super(activity);
        boolean isChinese = Locale.getDefault().getDisplayLanguage().contains("中文");
        setItems(isChinese ? new String[]{
                "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座",
                "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"
        } : new String[]{
                "Aquarius", "Pisces", "Aries", "Taurus", "Gemini", "Cancer",
                "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn"
        }, 0);
    }

}
