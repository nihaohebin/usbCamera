package cn.usbfacedetect.main;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import cn.usbfacedetect.Global;
import cn.usbfacedetect.imp.OnCameraDataImp;


/**
 * Author: hebin
 * Time : 2017/2/14
 */


public class USBCameraUtil {

    static {
        System.loadLibrary("camera_util");
    }

    private int width = 640;
    private int height = 480;
    private int numbuf = 4;
    private int devid = 4;
    private int index = 0;
    private byte[] mdata;
    private int[] rgb;
    private int ret = 0;

    private OnCameraDataImp mListener = null;
    private Bitmap bitmap;


    @SuppressLint("StaticFieldLeak")
    private static USBCameraUtil usbCameraUtil = null;

    public static synchronized USBCameraUtil getInstance() {
        if (usbCameraUtil == null) {
            usbCameraUtil = new USBCameraUtil();
        }
        return usbCameraUtil;
    }

    //保证在主线程，Handler对象与其调用者在同一线程中，如果在Handler中设置了延时操作，则调用线程也会堵塞。每个Handler对象都会绑定一个Looper对象，每个Looper对象对应一个消息队列（MessageQueue）。
    // 如果在创建Handler时不指定与其绑定的Looper对象，系统默认会将当前线程的Looper绑定到该Handler上。
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == 1) {
                if (mListener != null) {
                    bitmap = Bitmap.createBitmap(rgb, width, height, Bitmap.Config.RGB_565);
                    if (bitmap != null) {
                        mListener.onCameraData(bitmap);
                    }
                }
            }
        }
    };

    public void clear(){

        if (!bitmap.isRecycled()){
            bitmap.recycle();
        }
        bitmap = null;

//        if (handler!=null){
//            handler.removeCallbacksAndMessages(null);
//        }
//        handler = null;

        mdata = null;

        rgb = null;

    }



    public void openCamera() {
        Global.Varibale.isContinueGetBitmap = true;

        this.width = Global.Const.width;
        this.height = Global.Const.height;
        this.devid = Global.Const.devId;

        mdata = new byte[width * height * numbuf];
        rgb = new int[width * height * numbuf];

        ret = open(devid);
        if (ret < 0) {
            Log.i("TAG", "摄像头设备打开失败，请检查摄像头是否连接与设备ID是否正确");
            return;
        }
        ret = init(width, height, numbuf);
        if (ret < 0) {
            Log.i("TAG", "摄像头设备打开失败，请检查摄像头是否连接与设备ID是否正确");
            return;
        }
        ret = streamon();
        if (ret < 0) {
            Log.i("TAG", "摄像头设备打开失败，请检查摄像头是否连接与设备ID是否正确");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {

                    if (Global.Varibale.isContinueGetBitmap) {
                        index = dqbuf(mdata);

                        if ((index < 0) || (mdata == null)) {
                            break;
                        }

                        yuvtorgb2(mdata, rgb);

                        Message msg = Message.obtain();
                        msg.arg1 = 1;
                        handler.sendMessage(msg);

                        qbuf(index);
                    }
                }
            }
        }).start();
    }

    public void closeCamera() {
        Global.Varibale.isContinueGetBitmap = false;
        streamoff(index);
        release();
    }

    public void setDataListener(OnCameraDataImp listener) {
        this.mListener = listener;
    }

    private final static native int open(int devId);

    private final static native int open2(byte[] devId);

    private final static native int init(int width, int height, int numbuf);

    private final static native int streamon();

    private final static native void pixeltobmp(Bitmap bitmap);

    private final static native void yuvtorgb2(byte[] yuvData, int[] rgbDat);

    private final static native int dqbuf(byte[] data);

    private final static native int qbuf(int index);

    private final static native int streamoff(int index);

    private final static native int release();

}
