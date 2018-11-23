package com.github.cqrframe.toolkit;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.webkit.WebView;

import com.github.cqrframe.logger.CqrLog;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * 数据类型转换工具类
 * <p>
 * Created by liyujiang on 2018/8/18 15:32.
 */
@SuppressWarnings("WeakerAccess")
public class CqrConvertUtils {

    protected CqrConvertUtils() {
        throw new UnsupportedOperationException("You can't instantiate me");
    }

    /**
     * 返回一个高亮spannable
     *
     * @param content 文本内容
     * @param color   高亮颜色
     * @param start   起始位置
     * @param end     结束位置
     * @return 高亮spannable
     */
    public static CharSequence toHighLightText(String content, int color, int start, int end) {
        if (CqrStringUtils.isEmpty(content)) {
            return "";
        }
        start = start >= 0 ? start : 0;
        end = end <= content.length() ? end : content.length();
        SpannableString spannable = new SpannableString(content);
        spannable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public static CharSequence toHighLightText(String content, String subContent, int color) {
        if (CqrStringUtils.isEmpty(content)) {
            return "";
        }
        String colorHex = toColorHexString(color);
        content = content.replace(subContent, String.format("<font color='#%s'>%s</font>", colorHex, subContent));
        return Html.fromHtml(content);
    }

    /**
     * int占4字节
     *
     * @param i the
     * @return byte [ ]
     */
    public static byte[] toByteArray(int i) {
        // byte[] bytes = new byte[4];
        // bytes[0] = (byte) (0xff & i);
        // bytes[1] = (byte) ((0xff00 & i) >> 8);
        // bytes[2] = (byte) ((0xff0000 & i) >> 16);
        // bytes[3] = (byte) ((0xff000000 & i) >> 24);
        // return bytes;
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    public static byte[] toByteArray(String hexData, boolean isHex) {
        if (hexData == null || hexData.equals("")) {
            return null;
        }
        if (!isHex) {
            return hexData.getBytes();
        }
        hexData = hexData.replaceAll("\\s+", "");
        String hexDigits = "0123456789ABCDEF";
        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                hexData.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < hexData.length(); i += 2) {
            baos.write((hexDigits.indexOf(hexData.charAt(i)) << 4 | hexDigits
                    .indexOf(hexData.charAt(i + 1))));
        }
        byte[] bytes = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            CqrLog.debug(e);
        }
        return bytes;
    }

    public static String toHexString(String str) {
        if (CqrStringUtils.isEmpty(str))
            return "";
        StringBuilder builder = new StringBuilder();
        byte[] bytes = str.getBytes();
        for (byte aByte : bytes) {
            builder.append(Integer.toHexString(0xFF & aByte));
            builder.append(" ");
        }
        return builder.toString();
    }

    /**
     * To hex string string.
     *
     * @param bytes the bytes
     * @return the string
     */
    public static String toHexString(byte... bytes) {
        char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6',
                '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        // 参见：http://www.oschina.net/code/snippet_116768_9019
        char[] buffer = new char[bytes.length * 2];
        for (int i = 0, j = 0; i < bytes.length; ++i) {
            int u = bytes[i] < 0 ? bytes[i] + 256 : bytes[i];//转无符号整型
            buffer[j++] = DIGITS[u >>> 4];
            buffer[j++] = DIGITS[u & 0xf];
        }
        return new String(buffer);
    }

    /**
     * To hex string string.
     *
     * @param num the num
     * @return the string
     */
    public static String toHexString(int num) {
        String hexString = Integer.toHexString(num);
        CqrLog.debug(String.format("%s to hex string is %s", num, hexString));
        return hexString;
    }

    /**
     * To binary string string.
     *
     * @param bytes the bytes
     * @return the string
     */
    public static String toBinaryString(byte... bytes) {
        char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6',
                '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        // 参见：http://www.oschina.net/code/snippet_116768_9019
        char[] buffer = new char[bytes.length * 8];
        for (int i = 0, j = 0; i < bytes.length; ++i) {
            int u = bytes[i] < 0 ? bytes[i] + 256 : bytes[i];//转无符号整型
            buffer[j++] = DIGITS[(u >>> 7) & 0x1];
            buffer[j++] = DIGITS[(u >>> 6) & 0x1];
            buffer[j++] = DIGITS[(u >>> 5) & 0x1];
            buffer[j++] = DIGITS[(u >>> 4) & 0x1];
            buffer[j++] = DIGITS[(u >>> 3) & 0x1];
            buffer[j++] = DIGITS[(u >>> 2) & 0x1];
            buffer[j++] = DIGITS[(u >>> 1) & 0x1];
            buffer[j++] = DIGITS[u & 0x1];
        }
        return new String(buffer);
    }

    /**
     * To binary string string.
     *
     * @param num the num
     * @return the string
     */
    public static String toBinaryString(int num) {
        String binaryString = Integer.toBinaryString(num);
        CqrLog.debug(String.format("%s to binary string is %s", num, binaryString));
        return binaryString;
    }

    public static String toSlashString(String str) {
        String result = "";
        char[] chars = str.toCharArray();
        for (char chr : chars) {
            if (chr == '"' || chr == '\'' || chr == '\\') {
                result += "\\";//符合“"”“'”“\”这三个符号的前面加一个“\”
            }
            result += chr;
        }
        return result;
    }

    public static <T> T[] toArray(List<T> list) {
        //noinspection unchecked
        return (T[]) list.toArray();
    }

    public static <T> List<T> toList(T[] array) {
        return Arrays.asList(array);
    }

    public static String toString(Object[] objects) {
        return Arrays.deepToString(objects);
    }

    public static String toString(Object[] objects, String tag) {
        StringBuilder sb = new StringBuilder();
        for (Object object : objects) {
            sb.append(object);
            sb.append(tag);
        }
        return sb.toString();
    }

    public static byte[] toByteArray(InputStream is) {
        if (is == null) {
            return null;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            byte[] buff = new byte[100];
            while (true) {
                int len = is.read(buff, 0, 100);
                if (len == -1) {
                    break;
                } else {
                    os.write(buff, 0, len);
                }
            }
            return os.toByteArray();
        } catch (IOException e) {
            CqrLog.debug(e);
        } finally {
            CqrFileUtils.closeSilently(os);
            CqrFileUtils.closeSilently(is);
        }
        return null;
    }

    public static byte[] toByteArray(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            return os.toByteArray();
        } catch (OutOfMemoryError e) {
            CqrLog.debug(e);
        } finally {
            CqrFileUtils.closeSilently(os);
        }
        return null;
    }

    public static Bitmap toBitmap(byte[] bytes, int width, int height) {
        if (bytes.length == 0) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            // 不进行图片抖动处理
            options.inDither = false;
            // 设置让解码器以最佳方式解码
            options.inPreferredConfig = null;
            if (width > 0 && height > 0) {
                options.outWidth = width;
                options.outHeight = height;
            }
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            bitmap.setDensity(96);// 96 dpi
        } catch (OutOfMemoryError e) {
            CqrLog.error(e);
        }
        return bitmap;
    }

    public static Bitmap toBitmap(byte[] bytes) {
        return toBitmap(bytes, -1, -1);
    }

    /**
     * 将Drawable转换为Bitmap
     * 参考：http://kylines.iteye.com/blog/1660184
     */
    public static Bitmap toBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof ColorDrawable) {
            //color
            Bitmap bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(((ColorDrawable) drawable).getColor());
            return bitmap;
        } else if (drawable instanceof NinePatchDrawable) {
            //.9.png
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 从第三方文件选择器获取路径。
     * 参见：http://blog.csdn.net/zbjdsbj/article/details/42387551
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String toPath(Context context, Uri uri) {
        if (uri == null) {
            CqrLog.debug("uri is null");
            return "";
        }
        CqrLog.debug("uri: " + uri.toString());
        String path = uri.getPath();
        String scheme = uri.getScheme();
        String authority = uri.getAuthority();
        //是否是4.4及以上版本
        boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            String[] split = docId.split(":");
            String type = split[0];
            Uri contentUri = null;
            switch (authority) {
                // ExternalStorageProvider
                case "com.android.externalstorage.documents":
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                    break;
                // DownloadsProvider
                case "com.android.providers.downloads.documents":
                    contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    return _queryPathFromMediaStore(context, contentUri, null, null);
                // MediaProvider
                case "com.android.providers.media.documents":
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    String selection = "_id=?";
                    String[] selectionArgs = new String[]{split[1]};
                    return _queryPathFromMediaStore(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else {
            if ("content".equalsIgnoreCase(scheme)) {
                // Return the remote address
                if (authority.equals("com.google.android.apps.photos.content")) {
                    return uri.getLastPathSegment();
                }
                return _queryPathFromMediaStore(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(scheme)) {
                return uri.getPath();
            }
        }
        CqrLog.debug("uri to path: " + path);
        return path;
    }

    private static String _queryPathFromMediaStore(Context context, Uri uri, String selection, String[] selectionArgs) {
        String filePath = null;
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                filePath = cursor.getString(column_index);
            }
        } catch (IllegalArgumentException e) {
            CqrLog.error(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return filePath;
    }

    /**
     * 把view转化为bitmap（截图）
     * 参阅：
     * http://www.cnblogs.com/lee0oo0/p/3355468.html
     * https://blog.csdn.net/u013205623/article/details/60872466
     *
     * @param view 视图及其子视图的背景图不能用{@code android:background}设置，
     *             需要改成{@link android.widget.ImageView}的{@code android:src}，
     *             否则华为荣耀机型截图背景会是黑色
     */
    @Nullable
    public static Bitmap toBitmap(View view) {
        if (view instanceof WebView) {
            WebView webView = (WebView) view;
            Picture snapShot = webView.capturePicture();
            Bitmap bitmap = Bitmap.createBitmap(snapShot.getWidth(),
                    snapShot.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            snapShot.draw(canvas);
            return bitmap;
        }
        //For screenshots the android.view.PixelCopy API is recommended.
        view.setDrawingCacheEnabled(true);
        view.clearFocus();
        // Reset the drawing cache background color to fully transparent for the duration of this operation
        int color = view.getDrawingCacheBackgroundColor();
        view.setDrawingCacheBackgroundColor(Color.WHITE);//截图去黑色背景(透明像素)
        if (color != Color.WHITE) {
            view.destroyDrawingCache();
        }
        view.buildDrawingCache();
        Bitmap drawingCache = view.getDrawingCache();
        if (drawingCache == null) {
            CqrLog.debug("drawingCache is null");
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(drawingCache.getWidth(), drawingCache.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(drawingCache, 0, 0, null);
        canvas.save();
        view.destroyDrawingCache();
        view.setDrawingCacheBackgroundColor(color);
        return bitmap;
    }

    public static Drawable toDrawable(Bitmap bitmap) {
        return bitmap == null ? null : new BitmapDrawable(Resources.getSystem(), bitmap);
    }

    public static byte[] toByteArray(Drawable drawable) {
        return toByteArray(toBitmap(drawable));
    }

    public static Drawable toDrawable(byte[] bytes) {
        return toDrawable(toBitmap(bytes));
    }

    /**
     * dp转换为px
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        int pxValue = (int) (dpValue * scale + 0.5f);
        CqrLog.debug(dpValue + " dp == " + pxValue + " px");
        return pxValue;
    }

    /**
     * px转换为dp
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        int dpValue = (int) (pxValue / scale + 0.5f);
        CqrLog.debug(pxValue + " px == " + dpValue + " dp");
        return dpValue;
    }

    /**
     * sp转换为px
     */
    public static int sp2px(Context context, float spValue) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        int pxValue = (int) (spValue * scale + 0.5f);
        CqrLog.debug(spValue + " sp == " + pxValue + " px");
        return pxValue;
    }

    /**
     * px转换为sp
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        int spValue = (int) (pxValue / fontScale + 0.5f);
        CqrLog.debug(pxValue + " px == " + spValue + " sp");
        return spValue;
    }

    public static String utf8toGbk(String str) {
        try {
            return new String(str.getBytes("UTF-8"), "gbk");
        } catch (UnsupportedEncodingException e) {
            CqrLog.debug(e);
            return str;
        }
    }

    /**
     * @see android.text.format.Formatter#formatFileSize(Context, long)
     */
    public static String toFileSizeString(double fileSize) {
        final long GB = 1073741824;
        final long MB = 1048576;
        final long KB = 1024;
        DecimalFormat df = new DecimalFormat("0.00");
        String fileSizeString;
        if (fileSize < KB) {
            fileSizeString = fileSize + "B";
        } else if (fileSize < MB) {
            fileSizeString = df.format(fileSize / KB) + "K";
        } else if (fileSize < GB) {
            fileSizeString = df.format(fileSize / MB) + "M";
        } else {
            fileSizeString = df.format(fileSize / GB) + "G";
        }
        return fileSizeString;
    }

    public static String toString(InputStream is, String charset) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is, charset));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                } else {
                    sb.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            CqrLog.error(e);
        } finally {
            CqrFileUtils.closeSilently(reader);
            CqrFileUtils.closeSilently(is);
        }
        return sb.toString();
    }

    public static String toString(InputStream is) {
        return toString(is, "utf-8");
    }

    public static int toDarkenColor(@ColorInt int color) {
        return toDarkenColor(color, 0.8f);
    }

    public static int toDarkenColor(@ColorInt int color, @FloatRange(from = 0f, to = 1f) float value) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= value;//HSV指Hue、Saturation、Value，即色调、饱和度和亮度，此处表示修改亮度
        return Color.HSVToColor(hsv);
    }

    /**
     * 将10进制颜色值转换成16进制，不含“#”
     */
    public static String toColorHexString(@ColorInt int color) {
        return toColorHexString(color, false);
    }

    /**
     * 将10进制颜色值转换成16进制，不含“#”
     */
    public static String toColorHexString(@ColorInt int color, boolean includeAlpha) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));
        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }
        if (red.length() == 1) {
            red = "0" + red;
        }
        if (green.length() == 1) {
            green = "0" + green;
        }
        if (blue.length() == 1) {
            blue = "0" + blue;
        }
        String colorString;
        if (includeAlpha) {
            colorString = alpha + red + green + blue;
            CqrLog.debug(String.format("%s to color string is %s", color, colorString));
        } else {
            colorString = red + green + blue;
            CqrLog.debug(String.format("%s to color string is %s%s%s%s, exclude alpha is %s", color, alpha, red, green, blue, colorString));
        }
        return colorString;
    }

    /**
     * 将所有的数字、字母及标点全部转为全角字符，避免由于占位导致的排版混乱问题
     * 参阅: http://www.cnblogs.com/android-blogs/p/4973866.html
     */
    public static String toFullWidthCharacter(String str) {
        if (CqrStringUtils.isEmpty(str)) {
            return "";
        }
        char c[] = str.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == ' ') {
                c[i] = '\u3000';
            } else if (c[i] < '\177') {
                c[i] = (char) (c[i] + 65248);
            }
        }
        return new String(c);
    }

    public static int toInt(byte[] bytes) {
        int result = 0;
        byte abyte;
        for (int i = 0; i < bytes.length; i++) {
            abyte = bytes[i];
            result += (abyte & 0xFF) << (8 * i);
        }
        return result;
    }

    public static int toShort(byte first, byte second) {
        return (first << 8) + (second & 0xFF);
    }

    /**
     * 字符串转为int类型
     */
    public static int toInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        String str = obj.toString();
        if (CqrStringUtils.isEmpty(str)) {
            return 0;
        }
        try {
            return Integer.parseInt(str);
        } catch (Throwable e) {
            CqrLog.debug(e);
            return 0;
        }
    }

    /**
     * 字符串转为long类型
     */
    public static long toLong(Object obj) {
        if (obj == null) {
            return 0L;
        }
        String str = obj.toString();
        if (CqrStringUtils.isEmpty(str)) {
            return 0L;
        }
        try {
            return Long.parseLong(str);
        } catch (Throwable e) {
            CqrLog.debug(e);
            return 0L;
        }
    }

    /**
     * 字符串转为float类型
     */
    public static float toFloat(Object obj) {
        if (obj == null) {
            return 0F;
        }
        String str = obj.toString();
        if (CqrStringUtils.isEmpty(str)) {
            return 0F;
        }
        try {
            return Float.parseFloat(str);
        } catch (Throwable e) {
            CqrLog.debug(e);
            return 0F;
        }
    }

    /**
     * 字符串转为double类型
     */
    public static double toDouble(Object obj) {
        if (obj == null) {
            return 0;
        }
        String str = obj.toString();
        if (CqrStringUtils.isEmpty(str)) {
            return 0d;
        }
        try {
            return Double.parseDouble(str);
        } catch (Throwable e) {
            CqrLog.debug(e);
            return 0d;
        }
    }

    /**
     * 字符串转为boolean类型
     */
    public static boolean toBoolean(Object obj) {
        if (obj == null) {
            return false;
        }
        String str = obj.toString();
        if (CqrStringUtils.isEmpty(str)) {
            return false;
        }
        try {
            return Boolean.parseBoolean(str);
        } catch (Throwable e) {
            CqrLog.debug(e);
            return false;
        }
    }

    /**
     * 对TextView、Button等设置不同状态时其文字颜色。
     * 参见：http://blog.csdn.net/sodino/article/details/6797821
     */
    public static ColorStateList toColorStateList(@ColorInt int normalColor, @ColorInt int pressedColor,
                                                  @ColorInt int focusedColor, @ColorInt int unableColor) {
        int[] colors = new int[]{pressedColor, focusedColor, normalColor, focusedColor, unableColor, normalColor};
        int[][] states = new int[6][];
        states[0] = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled};
        states[1] = new int[]{android.R.attr.state_enabled, android.R.attr.state_focused};
        states[2] = new int[]{android.R.attr.state_enabled};
        states[3] = new int[]{android.R.attr.state_focused};
        states[4] = new int[]{android.R.attr.state_window_focused};
        states[5] = new int[]{};
        return new ColorStateList(states, colors);
    }

    public static ColorStateList toColorStateList(@ColorInt int normalColor, @ColorInt int pressedColor) {
        return toColorStateList(normalColor, pressedColor, pressedColor, normalColor);
    }

}
