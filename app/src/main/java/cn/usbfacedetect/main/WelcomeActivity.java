package cn.usbfacedetect.main;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.thinkjoy.face.imp.UserBindListener;
import cn.thinkjoy.face.manage.UserManage;
import cn.thinkjoy.face.model.ErrorMsg;
import cn.thinkjoy.sdk.SDKInitializer;
import cn.usbfacedetect.Global;
import cn.usbfacedetect.R;
import cn.usbfacedetect.bean.UsbLog;
import cn.usbfacedetect.bean.UserInfo;
import cn.usbfacedetect.netokhttp.NetOkhttp;
import cn.usbfacedetect.receiver.NetworkStateReceiver;
import cn.usbfacedetect.util.CarshHandlerUtil;
import cn.usbfacedetect.util.DialogUtil;
import cn.usbfacedetect.util.FileUtil;
import cn.usbfacedetect.util.SPUtil;
import cn.usbfacedetect.util.ScreenUtil;
import cn.usbfacedetect.util.TextUtil;
import cn.usbfacedetect.util.TimeUtil;
import cn.usbfacedetect.util.ToastUtil;


public class WelcomeActivity extends AppCompatActivity {

    private boolean isFirst = true;
    private NetworkStateReceiver mNetworkStateReceiver;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtil.setFullScreen(this);
        ScreenUtil.keepScreenLight(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //注册网络监控广播监听
        mNetworkStateReceiver = new NetworkStateReceiver();
        //网络广播过滤
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Global.Const.reConnect);
        registerReceiver(mNetworkStateReceiver, filter);

        //登陆样本库
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername("contact@zhthinkjoy.com");
        userInfo.setPassword("123456");
        userInfo.setMsgId(TimeUtil.getMsgId());
        userInfo.setReqTime(TimeUtil.getCurrentTime());

        NetOkhttp.doJsonPost(Global.UrlPath.login, userInfo, new NetOkhttp.ReceiveData() {
            @Override
            public void getData(JSONObject object, Gson gson) throws JSONException {

                if (object.getString("resCode").equals("0")) {
                    ToastUtil.showMessage(WelcomeActivity.this, "登录成功");
                    final UserInfo info = gson.fromJson(object.getString("resData"), UserInfo.class);
                    SPUtil.getInstance(WelcomeActivity.this).putString(Global.Const.userInfo, gson.toJson(info));
                    Logger.i("userid = " + info.getFaceUserId() + "\nfaceid = " + info.getFaceSetId());

                    Message msg = Message.obtain();
                    msg.arg1 = 1;
                    handler.sendMessage(msg);

                    getStaffData();
                } else {
                    ToastUtil.showMessage(WelcomeActivity.this, object.getString("resMsg"));
                }
            }

            @Override
            public void fail(JSONObject object, Gson gson) throws JSONException {
                ToastUtil.showMessage(WelcomeActivity.this, object.getString("resMsg"));
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //android 6.0以上包括6.0
                if (Build.VERSION.SDK_INT >= 23) {
                    CheckPermissions();
                } else {
                    //小于6.0直接登陆进去
                    startActivity(new Intent(WelcomeActivity.this, UsbCameraActivity.class));
                }
            }
        }, 3000);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.arg1 ==1){
                bindUser();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        //上传跟踪使用数据
//        postTimeLog();

        //启动异常捕捉监控  没有SD卡拿不到错误信息路径
        if (CarshHandlerUtil.getLogContent() != null) {
            CarshHandlerUtil.postError(CarshHandlerUtil.getLogContent());
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 300);
    }

    @SuppressWarnings("AccessStaticViaInstance")
    private void bindUser() {

        UserManage.newInstance(this).bindUser(SPUtil.getInstance(this).getUserInfo().getFaceUserId(), new UserBindListener() {
            @Override
            public void onUserBind(String userId, ErrorMsg error) {
                if (error.getCode() == 0) {
                    ToastUtil.showMessage(WelcomeActivity.this, "用户绑定成功");
                    Logger.i("用户绑定成功");
                } else {
                    SPUtil.getInstance(WelcomeActivity.this).putBoolean(Global.Const.isLogin, true);
                    Logger.i("用户绑定失败   error = " + error.getMsg());

                    Intent intent = new Intent(Global.Const.reConnect);
                    sendBroadcast(intent);
                }
            }
        });
    }

    private void getStaffData() {

        UserInfo info = new UserInfo();
        info.setCompanyId(SPUtil.getInstance(this).getUserInfo().getCompanyId());
        info.setMsgId(TimeUtil.getMsgId());
        info.setReqTime(TimeUtil.getCurrentTime());

        NetOkhttp.doJsonPost(Global.UrlPath.getEmployee, info, new NetOkhttp.ReceiveData() {
            @Override
            public void getData(JSONObject object, Gson gson) throws JSONException {

                if (object.getString("resCode").equals("0")) {

                    List<UserInfo> list = gson.fromJson(object.getString("resData"), new TypeToken<List<UserInfo>>() {
                    }.getType());
                    if (list != null) {
                        SPUtil.getInstance(WelcomeActivity.this).putString(Global.Const.userInfoList, new Gson().toJson(list));
                    }
                } else {
                    ToastUtil.showMessage(WelcomeActivity.this, object.getString("resMsg"));
                }

            }

            @Override
            public void fail(JSONObject object, Gson gson) throws JSONException {

            }
        });
    }

    private void postTimeLog() {


        if (!TextUtil.isEmpty(FileUtil.getDataFromFile(WelcomeActivity.this, Global.FilePath.androidDealTime))) {

            String content = "android系统版本: " + Build.VERSION.RELEASE +
                    "<br>android系统定制商：" + Build.BRAND +
                    "<br>androidSDK版本：" + Build.VERSION.SDK_INT +
                    "<br>android设备名称: " + Build.MODEL +
                    "<br>人脸识别应用使用时间: " + TimeUtil.getCurrentTime() +
                    "<br>人脸样本集合名称: " + Global.Const.faceSetName +
                    "<br>图片上传花费时间：:" + FileUtil.getDataFromFile(WelcomeActivity.this, Global.FilePath.httpTime) +
                    "<br><br>机子图片转换处理时间:" + FileUtil.getDataFromFile(WelcomeActivity.this, Global.FilePath.androidDealTime) +
                    "<br>预览图片分辨率：" + FileUtil.getDataFromFile(WelcomeActivity.this, Global.FilePath.bmpSize) +
                    "<br>识别结果返回：" + FileUtil.getDataFromFile(WelcomeActivity.this, Global.FilePath.compareResult);

            UsbLog testBomb = new UsbLog();
            testBomb.setContent(content);
            testBomb.save(new SaveListener<String>() {
                @Override
                public void done(String s, BmobException e) {

                    if (e == null) {
                        Logger.i("时间日志上传成功");

                        FileUtil.clearFile(WelcomeActivity.this, "", Global.FilePath.httpTime);
                        FileUtil.clearFile(WelcomeActivity.this, "", Global.FilePath.bmpSize);
                        FileUtil.clearFile(WelcomeActivity.this, "", Global.FilePath.androidDealTime);
                        FileUtil.clearFile(WelcomeActivity.this, "", Global.FilePath.compareResult);
                    } else {
                        Logger.i("创建数据失败：" + e.getMessage() + "------" + e.getErrorCode());
                    }
                }
            });
        }
    }


    /**
     * 权限检测回调
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Global.Const.PermissionCallBack) {

            if (grantResults.length != 0) {
                int n = 0;
                for (int grantResult : grantResults) {
                    if (grantResult == PermissionChecker.PERMISSION_GRANTED) {
                        n++;
                    }
                }
                if (n != grantResults.length) {
                    ToastUtil.showMessage(WelcomeActivity.this, "允许权限后应用才能运行！");
                    CheckPermissions();
                    return;
                }

                startActivity(new Intent(WelcomeActivity.this, UsbCameraActivity.class));

            } else {
                ToastUtil.showMessage(WelcomeActivity.this, "允许权限后应用才能运行！");
                CheckPermissions();
            }
        }

    }

    /**
     * 设置回调
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Global.Const.PermissionSetting) {
            CheckPermissions();
        }
    }

    /**
     * 权限检测
     */
    @TargetApi(Build.VERSION_CODES.M)
    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.M)
    public void CheckPermissions() {
        //检测权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!isFirst) {
                //检测权限是否拒绝过
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                        !shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH) ||
                        !shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE) ||
                        !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    DialogUtil.showYNDialog(this, "您已拒绝权限，请手动打开设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ToastUtil.showMessage(WelcomeActivity.this, "当前无权限，请授权！");
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", WelcomeActivity.this.getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, Global.Const.PermissionSetting);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    });
                    return;
                }
            }
            //请求权限
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, Global.Const.PermissionCallBack);
            isFirst = false;
        } else {
            startActivity(new Intent(WelcomeActivity.this, UsbCameraActivity.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mNetworkStateReceiver);
        SDKInitializer.onDestroy(getApplicationContext());
    }
}
