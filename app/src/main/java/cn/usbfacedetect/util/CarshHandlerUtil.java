package cn.usbfacedetect.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.usbfacedetect.Global;
import cn.usbfacedetect.bean.UsbError;

public class CarshHandlerUtil implements UncaughtExceptionHandler {
    public static String VERSION = "Unknown";
    /**
     * android系统版本
     */
    private static String ANDROID = Build.VERSION.RELEASE;
    /**
     * 机型
     */
    private static String MODEL = Build.MODEL;
    /**
     * 手机牌子
     */
    private static String MANUFACTURER = Build.MANUFACTURER;
    private static CarshBuilder mBuilder;
    private UncaughtExceptionHandler mPrevious;
    private boolean isAppend;
    private boolean isSimple;

    private CarshHandlerUtil() {
        mPrevious = Thread.currentThread().getUncaughtExceptionHandler();
        Thread.currentThread().setUncaughtExceptionHandler(this);
    }

    public static CarshHandlerUtil init(Context context, String dirName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            VERSION = info.versionName + info.versionCode;
            mBuilder = CarshBuilder.build(context, dirName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new CarshHandlerUtil();
    }


    public static String getCurrentDate() {
        return TimeUtil.getCurrentTime();
    }

    /**
     * 获取log 日志路径
     */
    public static String getLogFilePath() {
        if (mBuilder == null)
            return "Unknown";
        else
            return mBuilder.getCarsh_log();
    }

    /**
     * 获取 LOG 记录的内容
     */
    public static String getLogContent() {
        if (TextUtils.isEmpty(getLogFilePath()))
            return null;

        File file = new File(getLogFilePath());
        if (file.exists() && file.isFile()) {
            BufferedReader bis = null;
            try {
                bis = new BufferedReader(new FileReader(file));
                String buffer = null;
                StringBuilder sb = new StringBuilder();
                while ((buffer = bis.readLine()) != null) {
                    sb.append(buffer);
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bis != null)
                        bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void postError( String content) {

        UsbError testBomb = new UsbError();
        testBomb.setContent(content);
        testBomb.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {

                if (e == null) {
                    Log.i("TAG", "错误日志上传成功");
                    //上传一次后删除
                    FileUtil.deleteFile(getLogFilePath());
                } else {
                    Log.i("TAG","错误日志上传失败：" + e.getMessage() + "------" + e.getErrorCode());
                }
            }
        });
    }

    /**
     * @param isSimple 是否为简单的日志记录模式
     */
    public void setSimple(boolean isSimple) {
        this.isSimple = isSimple;
    }

    /**
     * @param isAppend 是否为日志追加模式
     */
    public CarshHandlerUtil setAppend(boolean isAppend) {
        this.isAppend = isAppend;
        return this;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        File f = new File(mBuilder.getCarsh_log());
        if (f.exists()) {
            if (!isAppend)
                f.delete();
        } else {
            try {
                new File(mBuilder.getCarsh_dir()).mkdirs();
                f.createNewFile();
            } catch (Exception e) {
                return;
            }
        }

        PrintWriter p;
        try {
            p = new PrintWriter(new FileWriter(f, true));
        } catch (Exception e) {
            return;
        }
        p.write("*************---------异常设备信息------------****************<br>");
        p.write("异常发生时间: " + getCurrentDate() + "<br>");
        p.write("Android系统版本: " + ANDROID + "<br>");
        p.write("Android型号: " + MODEL + "<br>");
        p.write("Android品牌: " + MANUFACTURER + "<br>");
        p.write("androidSDK版本:" + Build.VERSION.SDK_INT + "<br>");
        p.write("使用人: " + Global.Const.faceSetName + "<br>");
        p.write("*************---------异常详细信息 ------------****************<br>");
        if (!isSimple)
            throwable.printStackTrace(p);
        else {
            p.write(throwable.getLocalizedMessage() + "\n");
        }
        p.close();
        try {
            new File(mBuilder.getCarsh_tag()).createNewFile();
        } catch (Exception e) {
            return;
        }

        if (mPrevious != null) {
            mPrevious.uncaughtException(thread, throwable);
        }
    }

    public static class CarshBuilder {
        private String carsh_dir;

        public CarshBuilder(Context context, String dirName) {
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                this.carsh_dir = context.getCacheDir().getPath() + File.separator + dirName;
            } else
                this.carsh_dir = Environment.getExternalStorageDirectory().getPath() + File.separator + dirName;
        }

        public static CarshBuilder build(Context context, String dirName) {
            return new CarshBuilder(context, dirName);
        }

        public String getCarsh_dir() {
            return carsh_dir;
        }

        public String getCarsh_log() {
            return getCarsh_dir() + File.separator + "carshRecord.log";
        }

        public String getCarsh_tag() {

            return getCarsh_dir() + File.separator + ".carshed";
        }

        @Override
        public String toString() {
            return "CarshBuilder [dir path: " + getCarsh_dir() + "-- log path:" + getCarsh_log() + "-- tag path:" + getCarsh_tag() + "]";
        }
    }
}
