package cn.usbfacedetect.imp;

import android.graphics.Bitmap;

/**
 *相机获取数据返回图像回调
 */

public interface OnCameraDataImp {

   // void onCameraData(int[] rgb);
    void onCameraData(Bitmap bitmap);
}
