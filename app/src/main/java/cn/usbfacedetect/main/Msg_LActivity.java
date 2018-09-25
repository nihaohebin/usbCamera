package cn.usbfacedetect.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.usbfacedetect.Global;
import cn.usbfacedetect.R;
import cn.usbfacedetect.bean.UserInfo;
import cn.usbfacedetect.util.SPUtil;
import cn.usbfacedetect.util.ScreenUtil;
import cn.usbfacedetect.util.TextUtil;
import cn.usbfacedetect.util.TimeUtil;
import cn.usbfacedetect.util.ToastUtil;


public class Msg_LActivity extends Activity {

    @Bind(R.id.iv_head)
    ImageView ivHead;
    @Bind(R.id.tv_week)
    TextView tvWeek;
    @Bind(R.id.tv_hour)
    TextView tvHour;
    @Bind(R.id.success_name)
    TextView successName;
    @Bind(R.id.ll_name)
    LinearLayout llName;
    @Bind(R.id.success_dept)
    TextView successDept;
    @Bind(R.id.ll_dept)
    LinearLayout llDept;
    @Bind(R.id.success_position)
    TextView successPosition;
    @Bind(R.id.ll_position)
    LinearLayout llPosition;
    @Bind(R.id.ll_success)
    LinearLayout llSuccess;
    @Bind(R.id.ll_fail)
    LinearLayout llFail;
    @Bind(R.id.tv_sec)
    TextView tvSec;

    private int count = 0;

    private Handler handlerCount = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            int message = msg.arg1;

            if (message == 1) {
                String s = count + "";
                if (!TextUtil.isEmpty(s) && tvSec != null) {
                    tvSec.setText(s);
                }
            } else if (message == 2) {
                String s = count + "";
                if (!TextUtil.isEmpty(s) && tvSec != null) {
                    tvSec.setText(s);
                }
            }
        }
    };
    private boolean isContinueRun = true;  //控制强行返回退出时是否执行


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtil.setFullScreen(this);//全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_bl);
        ButterKnife.bind(this);

        List<UserInfo> userInfos = SPUtil.getInstance(Msg_LActivity.this).getUserInfoList();
        tvWeek.setText(TimeUtil.getCurrentTimeofYear() + TimeUtil.getWeekOfDate(TimeUtil.getCurrentDate()));
        tvHour.setText(TimeUtil.getCurrentTimeofHour());

        if (Global.Varibale.isSuccess) {

            llSuccess.setVisibility(View.VISIBLE);
            llFail.setVisibility(View.GONE);

            if (userInfos != null) {
                if (userInfos.size() != 0) {
                    for (UserInfo userInfo : userInfos) {
                        if (userInfo.getEmployeeName().equals(Global.Varibale.successName)) {
                            successName.setText(userInfo.getEmployeeName());
                            successDept.setText(userInfo.getDepartment());
                            successPosition.setText(userInfo.getPosition());
                            Glide.with(this).load(userInfo.getAvatar()).diskCacheStrategy(DiskCacheStrategy.RESULT).override(360, 480).into(ivHead);
                        }
                    }
                } else {
                    ToastUtil.showMessage(Msg_LActivity.this, "请补全该职员信息");
                }

                count = Global.Const.display_success / 1000;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (count != 0) {
                                Thread.sleep(1000);
                                --count;
                                Message msgSuccess = Message.obtain();
                                msgSuccess.arg1 = 1;
                                handlerCount.sendMessage(msgSuccess);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                //五秒展示完后跳回去
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isContinueRun) {
                            startActivity(new Intent(Msg_LActivity.this, UsbCameraActivity.class));
                            finish();
                        }
                    }
                }, Global.Const.display_success);

            } else {
                ToastUtil.showMessage(Msg_LActivity.this, "请补全该职员信息");
            }
        } else {
            llSuccess.setVisibility(View.GONE);
            llFail.setVisibility(View.VISIBLE);

            count = Global.Const.display_fail / 1000;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (count != 0) {
                            Thread.sleep(1000);
                            --count;
                            Message msgFail = Message.obtain();
                            msgFail.arg1 = 2;
                            handlerCount.sendMessage(msgFail);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            //五秒展示完后跳回去
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isContinueRun) {
                        startActivity(new Intent(Msg_LActivity.this, UsbCameraActivity.class));
                        finish();
                    }
                }
            }, Global.Const.display_fail);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isContinueRun = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        Glide.get(this).clearMemory();
        if (handlerCount != null) {
            handlerCount.removeCallbacksAndMessages(null);
        }
        handlerCount = null;
    }
}