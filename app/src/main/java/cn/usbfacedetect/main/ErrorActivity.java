package cn.usbfacedetect.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.usbfacedetect.Global;
import cn.usbfacedetect.R;
import cn.usbfacedetect.util.ScreenUtil;
import cn.usbfacedetect.util.TextUtil;


/**
 * Author hebin
 * 2017/2/7
 */

public class ErrorActivity extends Activity {

    @Bind(R.id.tv_sec)
    TextView tvSec;

    private boolean isContinue = true;

    private int count = 0;
    Handler handlerCount = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int message = msg.arg1;
            if (message == 1) {
                String s = count + "";
                if (!TextUtil.isEmpty(s) && tvSec != null) {
                    tvSec.setText(s);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtil.setFullScreen(this);//全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        ButterKnife.bind(this);


        count = Global.Const.display_fail / 1000;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (count != 0) {
                        Thread.sleep(1000);
                        --count;
                        if (isContinue) {
                            Message msgSuccess = Message.obtain();
                            msgSuccess.arg1 = 1;
                            handlerCount.sendMessage(msgSuccess);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isContinue) {

                    startActivity(new Intent(ErrorActivity.this, UsbCameraActivity.class));
                    finish();
                }
            }
        }, Global.Const.display_fail);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isContinue = false;
    }
}
