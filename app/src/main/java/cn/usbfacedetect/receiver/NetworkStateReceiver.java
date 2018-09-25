package cn.usbfacedetect.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.orhanobut.logger.Logger;

import cn.thinkjoy.face.imp.UserBindListener;
import cn.thinkjoy.face.manage.UserManage;
import cn.thinkjoy.face.model.ErrorMsg;
import cn.thinkjoy.sdk.SDKInitializer;
import cn.usbfacedetect.Global;
import cn.usbfacedetect.util.SPUtil;
import cn.usbfacedetect.util.ToastUtil;


/**
 * Author：hebin on 2016/10/18 0018
 * <p>
 * Annotations：网络监控广播
 */
@SuppressWarnings("deprecation")
public class NetworkStateReceiver extends BroadcastReceiver {

    private static final String TAG = "TAG";
    
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            //获取联网状态的NetworkInfo对象
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null) {
                //如果当前的网络连接成功并且网络连接可用
                if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI || info.getType() == ConnectivityManager.TYPE_MOBILE) {
                        Log.i(TAG,getConnectionType(info.getType()) + "连上");

                        if (Global.Varibale.isFirstConnect){
                            SDKInitializer.ReAuth(context.getApplicationContext());
                            bindUser();
                        }

                        Global.Varibale.isFirstConnect = true;

                    }
                } else {
                      Log.i(TAG,getConnectionType(info.getType()) + "断开");
                }
            }
        }else if (Global.Const.reConnect.equals(intent.getAction())){
            SDKInitializer.ReAuth(context.getApplicationContext());
            bindUser();
        }
    }

    @SuppressWarnings("AccessStaticViaInstance")
    private void bindUser() {

        if (SPUtil.getInstance(context).getUserInfo()==null){
            return;
        }

        UserManage.newInstance(context).bindUser(SPUtil.getInstance(context).getUserInfo().getFaceUserId(), new UserBindListener() {
            @Override
            public void onUserBind(String userId, ErrorMsg error) {
                if (error.getCode() == 0) {
                    ToastUtil.showMessage(context, "用户绑定成功");
                    Logger.i("用户绑定成功");
                } else {
                    SPUtil.getInstance(context).putBoolean(Global.Const.isLogin, true);
                    Logger.i("用户绑定失败" + error.getMsg());

                }
            }
        });
    }
    private String getConnectionType(int type) {
        String connType = "";
        if (type == ConnectivityManager.TYPE_MOBILE) {
            connType = "移动网络";
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            connType = "WIFI网络";
        }
        return connType;
    }
}