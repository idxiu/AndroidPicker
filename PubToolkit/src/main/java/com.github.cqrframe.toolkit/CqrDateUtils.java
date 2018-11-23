package com.github.cqrframe.toolkit;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.github.cqrframe.logger.CqrLog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 日期时间工具类
 * <p>
 * Created by liyujiang on 2015/8/5
 */
@SuppressWarnings("WeakerAccess")
public class CqrDateUtils extends android.text.format.DateUtils {
    /**
     * 秒与毫秒的倍数
     */
    public static final long SEC_MILLIS = 1000;
    /**
     * 分与毫秒的倍数
     */
    public static final long MIN_MILLIS = SEC_MILLIS * 60;
    /**
     * 时与毫秒的倍数
     */
    public static final long HOUR_MILLIS = MIN_MILLIS * 60;
    /**
     * 天与毫秒的倍数
     */
    public static final long DAY_MILLIS = HOUR_MILLIS * 24;

    /**
     * 周与毫秒的倍数
     */
    public static final long WEEK_MILLIS = DAY_MILLIS * 7;

    /**
     * 月与毫秒的倍数
     */
    public static final long MONTH_MILLIS = DAY_MILLIS * 30;
    /**
     * 年与毫秒的倍数
     */
    public static final long YEAR_MILLIS = DAY_MILLIS * 365;

    /**
     * SimpleDateFormat不是线程安全的，以下是线程安全实例化操作
     */
    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("", Locale.PRC);
        }
    };

    private CqrDateUtils() {
        throw new UnsupportedOperationException("You can't instantiate me");
    }

    public static long calculateDifferentSecond(Date startDate, Date endDate) {
        return calculateDifference(startDate, endDate, DifferenceMode.SECOND);
    }

    public static long calculateDifferentMinute(Date startDate, Date endDate) {
        return calculateDifference(startDate, endDate, DifferenceMode.MINUTE);
    }

    public static long calculateDifferentHour(Date startDate, Date endDate) {
        return calculateDifference(startDate, endDate, DifferenceMode.HOUR);
    }

    public static long calculateDifferentDay(Date startDate, Date endDate) {
        return calculateDifference(startDate, endDate, DifferenceMode.DAY);
    }

    public static long calculateDifferentSecond(long startTimeMillis, long endTimeMillis) {
        return calculateDifference(startTimeMillis, endTimeMillis, DifferenceMode.SECOND);
    }

    public static long calculateDifferentMinute(long startTimeMillis, long endTimeMillis) {
        return calculateDifference(startTimeMillis, endTimeMillis, DifferenceMode.MINUTE);
    }

    public static long calculateDifferentHour(long startTimeMillis, long endTimeMillis) {
        return calculateDifference(startTimeMillis, endTimeMillis, DifferenceMode.HOUR);
    }

    public static long calculateDifferentDay(long startTimeMillis, long endTimeMillis) {
        return calculateDifference(startTimeMillis, endTimeMillis, DifferenceMode.DAY);
    }

    /**
     * 计算两个时间戳之间相差的时间戳数
     */
    public static long calculateDifference(long startTimeMillis, long endTimeMillis, @DifferenceMode int mode) {
        return calculateDifference(new Date(startTimeMillis), new Date(endTimeMillis), mode);
    }

    /**
     * 计算两个日期之间相差的时间戳数
     */
    public static long calculateDifference(Date startDate, Date endDate, @DifferenceMode int mode) {
        long[] different = calculateDifference(startDate, endDate);
        if (mode == DifferenceMode.MINUTE) {
            return different[2];
        } else if (mode == DifferenceMode.HOUR) {
            return different[1];
        } else if (mode == DifferenceMode.DAY) {
            return different[0];
        } else {
            return different[3];
        }
    }

    private static long[] calculateDifference(Date startDate, Date endDate) {
        return calculateDifference(endDate.getTime() - startDate.getTime());
    }

    private static long[] calculateDifference(long differentMilliSeconds) {
        long secondsInMilli = 1000;//1s==1000ms
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        long elapsedDays = differentMilliSeconds / daysInMilli;
        differentMilliSeconds = differentMilliSeconds % daysInMilli;
        long elapsedHours = differentMilliSeconds / hoursInMilli;
        differentMilliSeconds = differentMilliSeconds % hoursInMilli;
        long elapsedMinutes = differentMilliSeconds / minutesInMilli;
        differentMilliSeconds = differentMilliSeconds % minutesInMilli;
        long elapsedSeconds = differentMilliSeconds / secondsInMilli;
        CqrLog.debug(String.format("different: %s ms, %s days, %s hours, %s minutes, %s seconds",
                differentMilliSeconds, elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds));
        return new long[]{elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds};
    }

    /**
     * 计算每月的天数
     */
    public static int calculateDaysInMonth(int month) {
        return calculateDaysInMonth(0, month);
    }

    /**
     * 根据年份及月份计算每月的天数
     */
    public static int calculateDaysInMonth(int year, int month) {
        // 添加大小月月份并将其转换为list,方便之后的判断
        String[] bigMonths = {"1", "3", "5", "7", "8", "10", "12"};
        String[] littleMonths = {"4", "6", "9", "11"};
        List<String> bigList = Arrays.asList(bigMonths);
        List<String> littleList = Arrays.asList(littleMonths);
        // 判断大小月及是否闰年,用来确定"日"的数据
        if (bigList.contains(String.valueOf(month))) {
            return 31;
        } else if (littleList.contains(String.valueOf(month))) {
            return 30;
        } else {
            if (year <= 0) {
                return 29;
            }
            // 是否闰年
            if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                return 29;
            } else {
                return 28;
            }
        }
    }

    /**
     * 月日时分秒，0-9前补0
     */
    @NonNull
    public static String fillZero(int number) {
        return number < 10 ? "0" + number : "" + number;
    }

    /**
     * 截取掉前缀0以便转换为整数
     *
     * @see #fillZero(int)
     */
    public static int trimZero(@NonNull String text) {
        try {
            if (text.startsWith("0")) {
                text = text.substring(1);
            }
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            CqrLog.debug(e.toString());
            return 0;
        }
    }

    /**
     * 功能：判断日期是否和当前date对象在同一天。
     * 参见：http://www.cnblogs.com/myzhijie/p/3330970.html
     *
     * @param date 比较的日期
     * @return boolean 如果在返回true，否则返回false。
     */
    public static boolean isSameDay(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("date is null");
        }
        Calendar nowCalendar = Calendar.getInstance();
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTime(date);
        return (nowCalendar.get(Calendar.ERA) == newCalendar.get(Calendar.ERA) &&
                nowCalendar.get(Calendar.YEAR) == newCalendar.get(Calendar.YEAR) &&
                nowCalendar.get(Calendar.DAY_OF_YEAR) == newCalendar.get(Calendar.DAY_OF_YEAR));
    }

    /**
     * 判断是否闰年
     *
     * @param year 年份
     * @return {@code true}: 闰年<br>{@code false}: 平年
     */
    public static boolean isLeapYear(int year) {
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0;
    }

    /**
     * 判断是否闰年
     *
     * @param date Date类型时间
     * @return {@code true}: 闰年<br>{@code false}: 平年
     */
    public static boolean isLeapYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        return isLeapYear(year);
    }

    /**
     * 将yyyy-MM-dd HH:mm:ss字符串转换成日期<br/>
     *
     * @param dateStr 时间字符串
     * @param pattern 当前时间字符串的格式。
     * @return Date 日期 ,转换异常时返回null。
     */
    public static Date parseDate(String dateStr, String pattern) {
        try {
            SimpleDateFormat dateFormat = obtainDateFormat(pattern);
            Date date = dateFormat.parse(dateStr);
            return new Date(date.getTime());
        } catch (ParseException e) {
            CqrLog.debug(e.toString());
            return null;
        }
    }

    /**
     * 将yyyy-MM-dd HH:mm:ss字符串转换成日期<br/>
     *
     * @param dateStr yyyy-MM-dd HH:mm:ss字符串
     * @return Date 日期 ,转换异常时返回null。
     */
    public static Date parseDate(String dateStr) {
        return parseDate(dateStr, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 将指定的日期转换为一定格式的字符串
     */
    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat dateFormat = obtainDateFormat(pattern);
        return dateFormat.format(date);
    }

    /**
     * 将当前日期转换为一定格式的字符串
     */
    public static String formatDate(String pattern) {
        return formatDate(Calendar.getInstance(Locale.PRC).getTime(), pattern);
    }

    /**
     * 得到13位时间戳（精确到毫秒）
     */
    public static long parseStamp(String stamp) {
        if (TextUtils.isEmpty(stamp)) {
            return 0L;
        }
        if (stamp.length() == 10) {
            stamp += "000";//10位时间戳精确到秒，13位时间戳精确到毫秒
        }
        return CqrConvertUtils.toLong(stamp);
    }

    public static String stampToDate(String stamp, String pattern) {
        return stampToDate(parseStamp(stamp), pattern);
    }

    public static String stampToDate(long stamp, String pattern) {
        if (String.valueOf(stamp).length() == 10) {
            stamp *= 1000;//转为13位时间戳
        }
        SimpleDateFormat dateFormat = obtainDateFormat(pattern);
        return dateFormat.format(new Date(stamp));
    }

    @NonNull
    private static SimpleDateFormat obtainDateFormat(String pattern) {
        SimpleDateFormat dateFormat = THREAD_LOCAL.get();
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(pattern, Locale.CHINA);
        } else {
            dateFormat.applyPattern(pattern);
        }
        return dateFormat;
    }

    /**
     * 此方法输入所要转换的时间输入例如（"2017-07-01 16:30:00"）返回时间戳
     */
    public static long dateToStamp(String date, String pattern) {
        if (TextUtils.isEmpty(date)) {
            return 0;
        }
        try {
            SimpleDateFormat dateFormat = obtainDateFormat(pattern);
            return dateFormat.parse(date).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    public static int stampDiffDay(long startTimestamp, long endTimestamp) {
        double oneDay = 1000 * 60 * 60 * 24;
        double diffDay = (endTimestamp - startTimestamp) / oneDay;
        if (diffDay > 0 && diffDay < 1) {
            diffDay = 1;
        }
        return (int) diffDay;
    }

    public static String stampToWeekFriendly(long timestamp) {
        String today = stampToDate(System.currentTimeMillis(), "yyy-MM-dd");
        String tomorrow = stampToDate(System.currentTimeMillis() + DAY_MILLIS, "yyy-MM-dd");
        String target = stampToDate(timestamp, "yyy-MM-dd");
        if (target.equals(today)) {
            return "今天";
        } else if (target.equals(tomorrow)) {
            return "明天";
        }
        return stampToWeek(timestamp);
    }

    public static String stampToWeek(long stamp, boolean useWeek) {
        String str;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(stamp);
        str = stampToDate(stamp, "EEEE");
        if (useWeek) {
            str = str.replace("星期", "周");
        }
        return str;
    }

    public static String stampToWeek(long stamp) {
        String str = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(stamp);
        //范围为1-7， 1=周日、7=周六，其他类推
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        switch (week) {
            case Calendar.SUNDAY:
                str = "周日";
                break;
            case Calendar.MONDAY:
                str = "周一";
                break;
            case Calendar.TUESDAY:
                str = "周二";
                break;
            case Calendar.WEDNESDAY:
                str = "周三";
                break;
            case Calendar.THURSDAY:
                str = "周四";
                break;
            case Calendar.FRIDAY:
                str = "周五";
                break;
            case Calendar.SATURDAY:
                str = "周六";
                break;
        }
        return str;
    }

    /**
     * 获取生肖
     *
     * @param date Date类型时间
     * @return 生肖
     */
    public static String getChineseZodiac(Date date) {
        final String[] CHINESE_ZODIAC = {"猴", "鸡", "狗", "猪", "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return CHINESE_ZODIAC[cal.get(Calendar.YEAR) % 12];
    }

    /**
     * 获取生肖
     *
     * @param year 年
     * @return 生肖
     */
    public static String getChineseZodiac(int year) {
        final String[] CHINESE_ZODIAC = {"猴", "鸡", "狗", "猪", "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊"};
        return CHINESE_ZODIAC[year % 12];
    }

    /**
     * 获取星座
     *
     * @param month 月
     * @param day   日
     * @return 星座
     */
    public static String getZodiac(int month, int day) {
        final String[] ZODIAC = {"水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "魔羯座"};
        final int[] ZODIAC_FLAGS = {20, 19, 21, 21, 21, 22, 23, 23, 23, 24, 23, 22};
        return ZODIAC[day >= ZODIAC_FLAGS[month - 1]
                ? month - 1
                : (month + 10) % 12];
    }

    /**
     * 获取星座
     *
     * @param date Date类型时间
     * @return 星座
     */
    public static String getZodiac(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return getZodiac(month, day);
    }

    /**
     * 格式化友好的时间差显示方式
     *
     * @param millis 开始时间戳
     */
    @SuppressWarnings("ConstantConditions")
    public static String getTimeSpanByNow1(long millis) {
        long now = System.currentTimeMillis();
        long span = now - millis;
        if (span < 1000) {
            return "刚刚";
        } else if (span < MIN_MILLIS) {
            return String.format("%s秒前", span / SEC_MILLIS);
        } else if (span < HOUR_MILLIS) {
            return String.format("%s分钟前", span / MIN_MILLIS);
        } else if (span < DifferenceMode.DAY) {
            return String.format("%s小时前", span / HOUR_MILLIS);
        } else if (span < WEEK_MILLIS) {
            return String.format("%s天前", span / DAY_MILLIS);
        } else if (span < MONTH_MILLIS) {
            return String.format("%s周前", span / WEEK_MILLIS);
        } else if (span < YEAR_MILLIS) {
            return String.format("%s月前", span / MONTH_MILLIS);
        } else {
            return String.format("%s年前", span / YEAR_MILLIS);
        }
    }

    /**
     * 格式化友好的时间差显示方式
     *
     * @param millis 开始时间戳
     */
    @SuppressWarnings("ConstantConditions")
    public static String getTimeSpanByNow2(long millis) {
        long now = System.currentTimeMillis();
        long span = now - millis;
        long day = span / DAY_MILLIS;
        if (day == 0) {// 今天
            long hour = span / HOUR_MILLIS;
            if (hour <= 4) {
                return String.format("凌晨%tR", millis);
            } else if (hour > 4 && hour <= 6) {
                return String.format("早上%tR", millis);
            } else if (hour > 6 && hour <= 11) {
                return String.format("上午%tR", millis);
            } else if (hour > 11 && hour <= 13) {
                return String.format("中午%tR", millis);
            } else if (hour > 13 && hour <= 18) {
                return String.format("下午%tR", millis);
            } else if (hour > 18 && hour <= 19) {
                return String.format("傍晚%tR", millis);
            } else if (hour > 19 && hour <= 24) {
                return String.format("晚上%tR", millis);
            } else {
                return String.format("今天%tR", millis);
            }
        } else if (day == 1) {// 昨天
            return String.format("昨天%tR", millis);
        } else if (day == 2) {// 前天
            return String.format("前天%tR", millis);
        } else {
            return String.format("%tF", millis);
        }
    }

    @IntDef(value = {DifferenceMode.SECOND, DifferenceMode.MINUTE,
            DifferenceMode.HOUR, DifferenceMode.DAY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DifferenceMode {
        int SECOND = 0;
        int MINUTE = 1;
        int HOUR = 2;
        int DAY = 3;
    }

}
