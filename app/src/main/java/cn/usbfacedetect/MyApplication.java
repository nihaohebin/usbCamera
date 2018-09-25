package cn.usbfacedetect;

import android.app.Application;
import android.content.Intent;

import com.orhanobut.logger.Logger;

import cn.bmob.v3.Bmob;
import cn.thinkjoy.sdk.SDKInitializer;
import cn.usbfacedetect.main.WelcomeActivity;
import cn.usbfacedetect.util.CarshHandlerUtil;

/**
 * Author：hebin on 2016/10/22 0022
 * Annotations：初始化APP配置类
 */
public class MyApplication extends Application implements Thread.UncaughtExceptionHandler {


    @Override
    public void onCreate() {
        super.onCreate();

        Bmob.initialize(this, "62340fa59424a661c84aa5b75d761b39");

        SDKInitializer.init(this);

        // setAppend是否为追加模式, setSimple是否是简单的log信息,
        CarshHandlerUtil.init(this, "CarshHandler").setAppend(true).setSimple(false);
        Thread.setDefaultUncaughtExceptionHandler(this);

        Logger.init("TAG").hideThreadInfo();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // 重启app ..上传日志等...
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
