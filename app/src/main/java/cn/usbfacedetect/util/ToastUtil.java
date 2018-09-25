package cn.usbfacedetect.util;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cn.usbfacedetect.R;


/**
 * 吐司信息打印类
 */
public class ToastUtil {
    private static Toast toast;

    /**
     * Hide the toast, if any.
     */
    public static void hideToast() {
        if (null != toast) {
            toast.cancel();
        }
    }

    /**
     * 显示自定义Toast提示(来自res)
     **/
    public static void showMessage(Context context, int resId) {
        showMessage(context, context.getString(resId));
    }

    /**
     * 显示自定义Toast提示(来自String)
     **/
    public static void showMessage(final Context context, final String text) {
        if (TextUtil.isEmpty(text)) {
            return;
        }
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (toast == null) {
                        toast = new Toast(context);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM, 0, 0);
                    }
                    View toastRoot = LayoutInflater.from(context).inflate(R.layout.common_toast, null);
                    ((TextView) toastRoot.findViewById(R.id.toast_text)).setText(text);
                    toast.setView(toastRoot);
                    toast.show();
                }
            });
        } else {
            if (toast == null) {
                toast = new Toast(context);
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                toast.setDuration(Toast.LENGTH_SHORT);
            }
            View toastRoot = LayoutInflater.from(context).inflate(R.layout.common_toast, null);
            ((TextView) toastRoot.findViewById(R.id.toast_text)).setText(text);
            toast.setView(toastRoot);
            toast.show();
        }
    }

    /**
     * 防止多次点击重复弹出TOAST
     */
    public static void showToast(Context context, String content) {
        if (toast == null) {
            toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

}
