package com.github.cqrframe.toolkit;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 */
@SuppressWarnings("WeakerAccess")
public class CqrStringUtils {
    private final static String NULL = "null";//服务端API返回默认值可能为null
    private final static String FALSE = "false";//服务端API返回默认值可能为false

    protected CqrStringUtils() {
        throw new UnsupportedOperationException("You can't instantiate me");
    }

    /**
     * 判断字符串是否有值，如果为null或者是空字符串或者只有空格或者为NULL字符串，则返回true，否则则返回false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0 ||
                NULL.equalsIgnoreCase(str) || FALSE.equalsIgnoreCase(str)/* ||
                str.equals("[]") || str.equals("{}")*/;
    }

    /**
     * 判断多个字符串是否相等，如果其中有一个为空字符串或者null，则返回false，只有全相等才返回true
     */
    public static boolean isEquals(String... agrs) {
        String last = null;
        for (String str : agrs) {
            if (isEmpty(str)) {
                return false;
            }
            if (last != null && !str.equalsIgnoreCase(last)) {
                return false;
            }
            last = str;
        }
        return true;
    }

    public static String capitalize(String str) {
        int strLen;
        return str != null && (strLen = str.length()) != 0 ? (new StringBuffer(strLen)).append(Character.toTitleCase(str.charAt(0))).append(str.substring(1)).toString() : str;
    }

    public static String uncapitalize(String str) {
        int strLen;
        return str != null && (strLen = str.length()) != 0 ? (new StringBuffer(strLen)).append(Character.toLowerCase(str.charAt(0))).append(str.substring(1)).toString() : str;
    }

    public static String swapCase(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            StringBuilder buffer = new StringBuilder(strLen);
            for (int i = 0; i < strLen; ++i) {
                char ch = str.charAt(i);
                if (Character.isUpperCase(ch)) {
                    ch = Character.toLowerCase(ch);
                } else if (Character.isTitleCase(ch)) {
                    ch = Character.toLowerCase(ch);
                } else if (Character.isLowerCase(ch)) {
                    ch = Character.toUpperCase(ch);
                }
                buffer.append(ch);
            }
            return buffer.toString();
        } else {
            return str;
        }
    }

    public static boolean isMatch(String str, String pattern, int flags) {
        return !isEmpty(str) && Pattern.compile(pattern, flags).matcher(str).matches();
    }

    public static boolean isMatch(String str, String pattern) {
        return isMatch(str, pattern, 0);
    }

    public static boolean isMatchMultiline(String str, String pattern) {
        return isMatch(str, pattern, Pattern.MULTILINE);
    }

    public static boolean isMatchIgnoreCase(String str, String pattern) {
        return isMatch(str, pattern, Pattern.CASE_INSENSITIVE);
    }

    public static boolean isMatchIgnoreLine(String str, String pattern) {
        return isMatch(str, pattern, Pattern.MULTILINE);
    }

    public static boolean isMobileNumber(String str) {
        if (isEmpty(str)) {
            return false;
        }
        boolean isRight;
        if (str.startsWith("852")) {
            //香港：字段长度L为11；以852开头；
            isRight = str.length() == 11;
        } else if (str.startsWith("853")) {
            //澳门：字段长度L为11；以853开头；
            isRight = str.length() == 11;
        } else if (str.startsWith("886")) {
            //台湾：字段长度L为12；以886开头。
            isRight = str.length() == 12;
        } else {
            //大陆：字段长度为11；首位为1，第二位为3-9任意数字，其他位为0-9任意数字；
            //正则表达式校验手机号，号码段参见BUG#634
            str = str.replaceAll("\\s", "");
            String tel = "[1][3456789]\\d{9}";
            isRight = str.matches(tel);
        }
        return isRight;
    }

    public static boolean isTelNumber(String str) {
        if (isEmpty(str)) {
            return false;
        }
        //正则表达式校验座机号、400及800电话（忽略中划线）
        str = str.replaceAll("\\s|-|—", "");
        String tel = "(((\\d{3,4}-?)|)\\d{7,8}(|([-转]\\d{1,5})?))|(400[0-9]{7})|(800[0-9]{7})";
        return str.trim().matches(tel);
    }

    public static boolean isNumber(String str) {
        //是否整数
        return isMatch(str, "\\d+");
    }

    public static boolean isDecimal(String str) {
        //是否小数
        return isMatch(str, "^([1-9]\\d*|0)(\\.\\d+)?$");
    }

    /**
     * 是否邮政编码
     */
    public static boolean isZipCode(String str) {
        return isMatch(str, "\\d{6}");
    }

    public static boolean isIdCardNumber(String str) {
        //是否大陆身份证号码
        return isMatch(str, "(\\d{14}|\\d{17})(\\d|x|X)");
    }

    public static boolean isBankCardNumber(String str) {
        //是否银行卡: 8-21位数字，例: 15位(招商企业卡)、16位(民生卡)、19位(建行卡)、18位（兴业卡）、20位（浦发卡）、21位（银联借记卡）
        return isMatch(str, "\\d{8,21}");
    }

    public static boolean isZhongShiYouCardNumber(String str) {
        //是否中石油加油卡: 9开头16位数
        return isMatch(str, "^9\\d{15}");
    }

    public static boolean isZhongShiHuaCardNumber(String str) {
        //是否中石化加油卡: 1开头19位数
        return isMatch(str, "^1\\d{18}");
    }

    public static boolean isPassword(String str) {
        //是否合格的密码: 由6到32位字母、数字及特定字符组成
        String password = "[a-zA-Z0-9~!@#$%^&*\\-_.]{6,32}";
        return isMatch(str, password);
    }

    public static boolean isEmail(String str) {
        //是否邮箱: 含字母、数字、下划线、中划线、小数点及@
        String email = "[\\w\\-.]+@[\\w\\-.]+\\.[\\w\\-.]+";
        return isMatch(str, email);
    }

    public static boolean isAlipayAccount(String str) {
        //是否支付宝账号: 手机号码或邮箱
        return isMobileNumber(str) || isEmail(str);
    }

    public static boolean isChinese(String str) {
        //是否中文
        return isMatch(str, "[\u4e00-\u9fa5•·]+");
    }

    public static String obtainPhoneCarrier(String str) {
        if (isEmpty(str)) {
            return "";
        }
        //根据手机号码段获取运营商类型，参见BUG#634
        String[] chinaMobile = new String[]{"134", "135", "136", "137", "138", "139", "147", "148",
                "150", "151", "152", "157", "158", "159", "172", "178", "182", "183", "184", "187",
                "188", "198"};
        String[] chinaUnicom = new String[]{"130", "131", "132", "145", "146", "155", "156", "175",
                "166", "176", "185", "186"};
        String[] china189 = new String[]{"133", "153", "173", "177", "180", "181", "189", "199"};
        for (String s : chinaMobile) {
            if (str.startsWith(s)) {
                return "中国移动";
            }
        }
        for (String s : chinaUnicom) {
            if (str.startsWith(s)) {
                return "中国联通";
            }
        }
        for (String s : china189) {
            if (str.startsWith(s)) {
                return "中国电信";
            }
        }
        return "";
    }

    public static String hideEmail(String str) {
        if (isEmpty(str)) {
            return "";
        }
        //隐藏邮箱中间几位
        return str.replaceAll("([\\w\\-.]{3})([\\w\\-.]+)@([\\w\\-.]+)", "$1****$3");
    }

    public static String hideMobileNumber(String str) {
        if (isEmpty(str)) {
            return "";
        }
        //隐藏手机号中间四位
        return str.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    public static String hideIdCardNumber(String str) {
        if (isEmpty(str)) {
            return "";
        }
        //隐藏身份证号中间位（保留前四位后三位）
        return str.replaceAll("(\\d{4})(\\d+)(\\d{3}|\\d{2}X|\\d{2}x)", "$1**********$3");
    }

    public static String hideBankCardNumber(String str) {
        if (isEmpty(str)) {
            return "";
        }
        //只显示银行卡后四位
        return str.replaceAll(".*(\\d{4})", "**** **** **** $1");
    }

    public static String formatIdCardNumber(String str) {
        if (isEmpty(str)) {
            return "";
        }
        //格式化身份证号: 地址码+生日码+顺序码+校验码
        return str.replaceAll("(\\d{6})(\\d{8})(\\d{4}|\\d{3}X|\\d{3}x)", "$1 $2 $3");
    }

    public static String formatBankCardNumber(String str) {
        if (isEmpty(str)) {
            return "";
        }
        //格式化银行卡号，每四位一格
        return divideBy4Space(str);
    }

    public static String keepBankCardNumber(String str) {
        if (isEmpty(str)) {
            return "";
        }
        //只保留银行卡号最后四位
        return str.replaceAll("(\\d+)(\\d{4})", "**** **** **** $2");
    }

    public static String keepLast4Chars(String str) {
        if (isEmpty(str)) {
            return "";
        }
        int length = str.length();
        if (length > 4) {
            return "****" + str.substring(length - 4);
        }
        return str;
    }

    /**
     * 每隔四位插入一个空格
     */
    public static String divideBy4Space(String editStr) {
        char[] cardArray = editStr.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = cardArray.length; i < len; i++) {
            if (i % 4 == 0 && i > 0) {
                sb.append(" ");
            }
            sb.append(cardArray[i]);
        }
        return sb.toString();
    }

    public static String clearSpecialChars(String str) {
        String regExp = "[`~!@#$%&=':;,/<>\\*\\^\\(\\)\\+\\|\\{\\}\\[\\]\\.\\?\\\\]";
        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(str);
        str = matcher.replaceAll("");
        return str.trim();
    }

    public static String clearHtmlTag(String str) {
        String regEx_script = "<script[^>]*?>[\\s\\S]*?</script>"; //定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?</style>"; //定义style的正则表达式
        String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式
        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(str);
        str = m_script.replaceAll(""); //过滤script标签
        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(str);
        str = m_style.replaceAll(""); //过滤style标签
        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(str);
        str = m_html.replaceAll(""); //过滤html标签
        return str.trim(); //返回文本字符串
    }

    /**
     * 金钱格式化，使用银行家舍入法
     */
    public static String formatMoney(double money) {
        return formatMoney(String.valueOf(money));
    }

    /**
     * 金钱格式化，使用银行家舍入法
     */
    public static String formatMoney(String money) {
        return formatMoney(money, false);
    }

    /**
     * 金钱格式化，使用银行家舍入法
     */
    public static String formatMoney(String money, boolean includeSymbol) {
        return formatMoney(money, 2, includeSymbol);
    }

    /**
     * 金钱格式化，使用银行家舍入法
     */
    public static String formatMoney(String money, int dotLength) {
        return formatMoney(money, dotLength, false);
    }

    /**
     * 金钱格式化，使用银行家舍入法
     */
    public static String formatMoney(String money, int dotLength, boolean includeSymbol) {
        if (TextUtils.isEmpty(money)) {
            return "";
        }
        //位数不足才能使用UNNECESSARY模式，否则出发ArithmeticException: Rounding necessary
        int dotIndex = money.lastIndexOf(".");
        boolean needRound = dotIndex != -1 && money.length() - dotIndex - 1 > dotLength;
        //RoundingMode.CEILING：取右边最近的整数
        //RoundingMode.DOWN：去掉小数部分取整，也就是正数取左边，负数取右边，相当于向原点靠近的方向取整
        //RoundingMode.FLOOR：取左边最近的正数
        //RoundingMode.HALF_DOWN:五舍六入，负数先取绝对值再五舍六入再负数
        //RoundingMode.HALF_UP:四舍五入，负数原理同上
        //RoundingMode.HALF_EVEN:银行家舍入法，整数位若是奇数则四舍五入，若是偶数则五舍六入，参阅 https://blog.csdn.net/w1014074794/article/details/53633818?utm_source=blogxgwz5
        RoundingMode mode = needRound ? RoundingMode.HALF_EVEN : RoundingMode.UNNECESSARY;
        BigDecimal num = new BigDecimal(money);
        String s = num.setScale(dotLength, mode).toString();
        if (includeSymbol) {
            s = String.format("¥%s", s);
        }
        return s;
    }

    /**
     * @see TextUtils#join(CharSequence, Iterable)
     */
    public static String implode(List<String> strs, String tag) {
        if (strs == null || strs.size() == 0) {
            return "";
        }
        if (strs.size() == 1) {
            return strs.get(0);
        }
        return implode(strs.toArray(new String[strs.size()]), tag);
    }

    /**
     * @see TextUtils#join(CharSequence, Object[])
     */
    public static String implode(String[] strs, String tag) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String str : strs) {
            sb.append(str);
            if (i != strs.length - 1) {
                sb.append(tag);
            }
            i++;
        }
        return sb.toString();
    }

}
