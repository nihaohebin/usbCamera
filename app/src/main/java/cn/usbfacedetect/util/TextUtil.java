package cn.usbfacedetect.util;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文字检验，转换，操作工具类
 */
public class TextUtil {

    /**
     * 正则：手机号（精确）
     * <p>移动：134(0-8)、135、136、137、138、139、147、150、151、152、157、158、159、178、182、183、184、187、188</p>
     * <p>联通：130、131、132、145、155、156、175、176、185、186</p>
     * <p>电信：133、153、173、177、180、181、189</p>
     * <p>全球星：1349</p>
     * <p>虚拟运营商：170</p>
     */
    public static final String REGEX_MOBILE_EXACT;
    /**
     * 正则：身份证号码18位
     */
    public static final String REGEX_IDCARD18;
    /**
     * 正则：邮箱
     */
    public static final String REGEX_EMAIL;
    /**
     * 正则：URL
     */
    public static final String REGEX_URL;
    /**
     * 正则：汉字
     */
    public static final String REGEX_CHZ;
    /**
     * 正则：用户名，取值范围为a-z,A-Z,0-9,"_",汉字，不能以"_"结尾,用户名必须是3-20位
     */
    public static final String REGEX_USERNAME;
    /**
     * 正则：yyyy-MM-dd格式的日期校验，已考虑平闰年
     */
    public static final String REGEX_DATE;
    /**
     * 正则：IP地址
     */
    public static final String REGEX_IP;

    static {
        REGEX_URL = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?";
        REGEX_EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        REGEX_IDCARD18 = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9Xx])$";
        REGEX_MOBILE_EXACT = "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|(147))\\d{8}$";
        REGEX_CHZ = "^[\\u4e00-\\u9fa5]+$";
        REGEX_USERNAME = "^[\\w\\u4e00-\\u9fa5]{3,20}(?<!_)$";
        REGEX_IP = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";
        REGEX_DATE = "^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)$";
    }

    /**
     * 检验是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.equals("");
    }

    /**
     * 检验是否为空
     */
    public static boolean isEmpty(CharSequence chars) {
        return chars == null || isEmpty(chars.toString());
    }

    /**
     * 判断是否全部有字母和数字组成
     */
    public static boolean isContainLettersAndNumbers(String name) {
        Pattern p = Pattern.compile("[a-zA-Z0-9_@]*");
        Matcher m = p.matcher(name);
        return m.matches();
    }

    /**
     * double转String（保留两位小数）
     */
    public static String doubleToString(double str) {
        if (str == 0) {
            return "0";
        }
        DecimalFormat df = new DecimalFormat("###.00");
        return df.format(str);
    }

    /**
     * 获取数据大小
     */
    public static String formatDataSize(int size) {
        String ret;
        if (size < (1024 * 1024)) {
            ret = String.format("%dK", size / 1024);
        } else {
            ret = String.format("%.1fM", size / (1024 * 1024.0));
        }
        return ret;
    }

    /**
     * 判断是否是IP正则表达式
     */
    public static boolean isIP(String ip) {

        Pattern p = Pattern.compile(REGEX_IP);
        Matcher m = p.matcher(ip);
        return m.matches();
    }

    /**
     * 判断是否是汉字正则表达式
     */
    public static boolean isCHZ(String chz) {

        Pattern p = Pattern.compile(REGEX_CHZ);
        Matcher m = p.matcher(chz);
        return m.matches();
    }


    /**
     * 判断是否是URL正则表达式
     */
    public static boolean isURL(String url) {

        Pattern p = Pattern.compile(REGEX_URL);
        Matcher m = p.matcher(url);
        return m.matches();
    }


    /**
     * 判断是否是邮箱正则表达式
     */
    public static boolean isEmail(String email) {

        Pattern p = Pattern.compile(REGEX_EMAIL);
        Matcher m = p.matcher(email);
        return m.matches();
    }


    /**
     * 判断是否是身份证号码正则表达式
     */
    public static boolean isIdentity(String identity) {

        Pattern p = Pattern.compile(REGEX_IDCARD18);
        Matcher m = p.matcher(identity);
        return m.matches();
    }


    /**
     * 判断是否是手机号码 正则表达式
     */
    public static boolean isPhoneNum(String mobiles) {

        Pattern p = Pattern.compile(REGEX_MOBILE_EXACT);
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 是否是用户名
     */
    public static boolean isUserName(String username) {

        Pattern p = Pattern.compile(REGEX_USERNAME);
        Matcher m = p.matcher(username);
        return m.matches();
    }

}
