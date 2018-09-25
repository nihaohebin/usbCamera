package cn.usbfacedetect.util;


import android.annotation.SuppressLint;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间处理工具
 * Created by Nereo on 2015/4/8.
 */

@SuppressLint("SimpleDateFormat")
public class TimeUtil {

    public static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
    private static Calendar mCalendar;
    private static String time;

    public static String getTime(long timeMillis) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分", Locale.CHINA);
        return sf.format(new Date(timeMillis));
    }

    public static String getNumberTime() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddhhmmss", Locale.CHINA);
        return sf.format(new Date());
    }

    public static String getPostTime() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return sf.format(new Date());
    }

    public static String timeFormat(long timeMillis, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
        return format.format(new Date(timeMillis));
    }

    public static String formatPhotoDate(long time) {
        return timeFormat(time, "yyyy-MM-dd HH:mm");
    }

    public static String formatPhotoDate(String path) {
        File file = new File(path);
        if (file.exists()) {
            long time = file.lastModified();
            return formatPhotoDate(time);
        }
        return "1970-01-01 00:00";
    }

    /**
     * 比较时间大小，end>start返回true
     */
    public static boolean compareTime(String startTime, String endTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH时mm分");
        try {
            Date start = formatter.parse(startTime);
            Date end = formatter.parse(endTime);
            if (start.getTime() <= end.getTime()) {
                return true;
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 星期几
     * Date 日期
     *
     * @return 星期一到星期日
     */
    public static String getWeekOfDate(Date date) {
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;

        return weekDays[w];
    }

    /**
     * 获取两个时间的时间差
     */
    public static String getDiffTime(String endDate, String nowDate) {

//        String dateStart = "2012/01/14/ 09:29:58";
//        String dateStop = "2012/01/15/ 10:31:48";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long diffDays = 0, diffHours = 0, diffMinutes = 0, diffSeconds = 0;
        try {
            Date d1 = format.parse(nowDate);
            Date d2 = format.parse(endDate);

            long diff = d2.getTime() - d1.getTime();

            diffDays = diff / (24 * 60 * 60 * 1000);
            diffHours = diff / (60 * 60 * 1000) % 24;
            diffMinutes = diff / (60 * 1000) % 60;
            diffSeconds = diff / 1000 % 60;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return diffDays + "天" + diffHours + "时" + diffMinutes + "分" + diffSeconds + "秒";
    }


    /**
     * 获取当前时间
     */
    public static String getCurrentTime() {
        mCalendar = Calendar.getInstance();

        String hour;
        if (mCalendar.get(Calendar.HOUR_OF_DAY) < 10) {
            hour = "0" + mCalendar.get(Calendar.HOUR_OF_DAY);
        } else {
            hour = mCalendar.get(Calendar.HOUR_OF_DAY) + "";
        }

        String min;
        if (mCalendar.get(Calendar.MINUTE) < 10) {
            min = "0" + mCalendar.get(Calendar.MINUTE);
        } else {
            min = mCalendar.get(Calendar.MINUTE) + "";
        }
        String sec;
        if (mCalendar.get(Calendar.SECOND) < 10) {
            sec = "0" + mCalendar.get(Calendar.SECOND);
        } else {
            sec = mCalendar.get(Calendar.SECOND) + "";
        }

        time = mCalendar.get(Calendar.YEAR) + "-" + (mCalendar.get(Calendar.MONTH) + 1) + "-" + mCalendar.get(Calendar.DAY_OF_MONTH) + " " + hour + ":" + min + ":" + sec;

        return time;
    }

    /**
     * 获取当前年月日
     *
     * @return
     */
    public static String getCurrentTimeofYear() {
        mCalendar = Calendar.getInstance();

        String hour;
        if (mCalendar.get(Calendar.HOUR_OF_DAY) < 10) {
            hour = "0" + mCalendar.get(Calendar.HOUR_OF_DAY);
        } else {
            hour = mCalendar.get(Calendar.HOUR_OF_DAY) + "";
        }

        String min;
        if (mCalendar.get(Calendar.MINUTE) < 10) {
            min = "0" + mCalendar.get(Calendar.MINUTE);
        } else {
            min = mCalendar.get(Calendar.MINUTE) + "";
        }

        time = mCalendar.get(Calendar.YEAR) + "年" + (mCalendar.get(Calendar.MONTH) + 1) + "月" + mCalendar.get(Calendar.DAY_OF_MONTH) + "日";

        return time;
    }

    /**
     * 获取当前时分
     */
    public static String getCurrentTimeofHour() {
        mCalendar = Calendar.getInstance();

        String hour;
        if (mCalendar.get(Calendar.HOUR_OF_DAY) < 10) {
            hour = "0" + mCalendar.get(Calendar.HOUR_OF_DAY);
        } else {
            hour = mCalendar.get(Calendar.HOUR_OF_DAY) + "";
        }

        String min;
        if (mCalendar.get(Calendar.MINUTE) < 10) {
            min = "0" + mCalendar.get(Calendar.MINUTE);
        } else {
            min = mCalendar.get(Calendar.MINUTE) + "";
        }

        time = hour + ":" + min;

        return time;
    }

    /**
     * 获取唯一标记
     */
    public static String getMsgId() {

        int a = (int) (Math.random() * (9999 - 1000 + 1)) + 1000;//四位随机数

        mCalendar = Calendar.getInstance();
        time = mCalendar.get(Calendar.YEAR) + (mCalendar.get(Calendar.MONTH) + 1) + mCalendar.get(Calendar.DAY_OF_MONTH)
                + mCalendar.get(Calendar.HOUR_OF_DAY) + mCalendar.get(Calendar.MINUTE) + mCalendar.get(Calendar.SECOND)
                + a + "";
        return time;
    }


    /**
     * 获取处理date传过来的时间解码
     */
    public static String getDealTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        return sdf.format(date);
    }

    public static Date getCurrentDate() {

        Date date = new Date(System.currentTimeMillis());
        return date;
    }


}
