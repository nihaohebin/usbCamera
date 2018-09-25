package cn.usbfacedetect.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.thinkjoy.face.imp.FaceSearchListener;
import cn.thinkjoy.face.manage.SearchManage;
import cn.thinkjoy.face.model.ErrorMsg;
import cn.thinkjoy.face.model.FaceSearchInfo;
import cn.usbfacedetect.Global;
import cn.usbfacedetect.R;
import cn.usbfacedetect.imp.OnCameraDataImp;
import cn.usbfacedetect.util.ImageUtil;
import cn.usbfacedetect.util.SPUtil;
import cn.usbfacedetect.util.ScreenUtil;
import cn.usbfacedetect.util.threadpool.ServiceThreadPoolManager;
import cn.usbfacedetect.view.FaceOverlayViewCompare;
import cn.usbfacedetect.view.FaceResult;
import cn.usbfacedetect.view.MyVideoView;


public class UsbCameraActivity extends AppCompatActivity implements OnCameraDataImp, MediaPlayer.OnCompletionListener {


    @Bind(R.id.img_web)
    ImageView imgWeb;
    @Bind(R.id.overlay)
    FaceOverlayViewCompare overlay;
    @Bind(R.id.iv_face)
    ImageView ivFace;
    @Bind(R.id.videoview)
    MyVideoView videoview;
    @Bind(R.id.btn_open)
    Button btn_open;


    private FaceResult faces[];     //人脸结果
    private FaceDetector fdet;      //人脸检测类
    private int MAX_FACE = 1;       //最大人脸检测数量
    private int previewHeight = 225, previewWidth = 400, prevSettingWidth = 360, prevSettingHeight = 222;       //预览高度、宽度   预览设置宽度、高度

    private int Id = 0;
    private Bitmap faceCroped = null;             //截取人脸图片
    private boolean isPosting = true;             //控制是否可以进行上传图片
    private boolean isContinue_check = true;      //控制是否执行5S的是否持续存在人脸的run程序  只在识别成功或者失败时停止检测
    private boolean isContinue_fail = true;       //控制是否执行15S失败计时器的run程序
    private boolean isContinue_error = true;      //控制是否执行请求超时
    private boolean flag_facecheck = true;        //控制是否进入启动4S检测器   只在识别成功或者失败时停止检测
    private boolean start_faceCheck = true;       //控制多次不停的发送过来的通知2   只启动一次
    private int counter = 0;                      //用于记录截取的图片数量，计算每秒的帧数
    private long start, end;                      //开始检测时间与结束时间
    private double fps;                           //通过start 、end计算fps
    private int result = 0;
    private int resultNum = 0;
    private int countSameName = 0;

    private List<Long> timeS = new ArrayList<>();
    private List<Long> timeE = new ArrayList<>();
    private int httpCount = 0;
    private String lastName = "";
    private List<Bitmap> faceNumber = new ArrayList<>();               //4S内人脸数量

    private boolean isThreadWorking = false;
    private Bitmap bitmap = null;

    //子线程控制UI线程 控件变化
    private Handler handlerMain = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.arg1) {
                case 1:

                    if (faceCroped != null) {
                        ivFace.setImageBitmap(faceCroped);
                    }

                    if (isPosting) {
                        //控制是否启动图片上传  每启动五条上传线程 等待拿到三个响应后 才继续上传
                        if (++result == 5) {
                            isPosting = false;
                        }

                        timeS.add(System.currentTimeMillis());

                        //人脸识别
                        SearchManage.newInstance(UsbCameraActivity.this).faceSearch(faceCroped, SPUtil.getInstance(UsbCameraActivity.this).getUserInfo().getFaceSetId(), 1, new FaceSearchListener() {

                            @Override
                            public void onFaceSearchListener(FaceSearchInfo search, ErrorMsg error) {

                                timeE.add(System.currentTimeMillis());
                                String httpTime = String.valueOf(timeE.get(httpCount) - timeS.get(httpCount));
                                httpCount++;
//                                Logger.i("HTTP请求时间:" + httpTime + "ms  ");
                                Global.Varibale.httpTime = Double.parseDouble(httpTime);

                                if (++resultNum == 5) {
                                    resultNum = 0;
                                    result = 0;
                                    isPosting = true;
                                }

                                if (error.getCode() == 0) {

                                    double confidence = search.getResultFace().get(0).getConfidence();
                                    String name = search.getResultFace().get(0).getPersonId();
                                    Global.Varibale.confidence = confidence;
                                    Global.Varibale.Comparename = name;
                                    Logger.i("识别成功  名 字:" + name + "-----相似度：" + confidence);
//                                    FileUtil.saveDataToFile(UsbCameraActivity.this, " 名 字:" + name + "-----相似度：" + confidence + "  ---msgId：" + error.getMsg_id() + "<br>", Global.FilePath.compareResult);
//                                    Logger.i("上一个名字" + lastName + "   现在名字：" + name);
                                    // 三次响应中 如果有三次识别率高于0 .93 且三次同名字 则认为识别成功。
                                    if (confidence >= Global.Const.recognitionRate) {
                                        if (name.equals(lastName)) {
                                            ++countSameName;
                                            if (countSameName >= 3) {
                                                USBCameraUtil.getInstance().closeCamera();
                                                isPosting = false;
                                                isThreadWorking = true;
                                                countSameName = 0;
                                                isContinue_error = false;
                                                isContinue_fail = false;
                                                isContinue_check = false;
                                                flag_facecheck = false;
                                                Global.Varibale.successName = lastName;
                                                Global.Varibale.isSuccess = true;
                                                Logger.e("识别成功，关闭图片上传、检测器、计时器和识别算法");
                                                startActivity(new Intent(UsbCameraActivity.this, Msg_LActivity.class));
                                                finish();

                                            }
                                        }
                                    }
                                    lastName = name;
                                } else {
                                    Logger.e("人脸识别错误！" + error.getMsg());
                                    if (error.getCode() == 1800) {
                                        if (isContinue_error) {
                                            USBCameraUtil.getInstance().closeCamera();
                                            isContinue_error = false;
                                            isThreadWorking = true;
                                            isPosting = false;
                                            flag_facecheck = false;
                                            isContinue_fail = false;
                                            isContinue_check = false;
                                            faceNumber.clear();

                                            startActivity(new Intent(UsbCameraActivity.this, ErrorActivity.class));
                                            finish();
                                        }
                                    }
                                }
                            }
                        });
                    }
                    break;
                case 2:
                    if (start_faceCheck) {
                        start_faceCheck = false;
//                    Logger.e("进入启动  是否持续有人脸存在检测器线程   和   失败计时器线程");
                        /**
                         * 界面转换
                         */
                        videoview.setVisibility(View.GONE);
                        overlay.setVisibility(View.VISIBLE);
                        ivFace.setVisibility(View.VISIBLE);

                        /**
                         * 是否持续有人脸存在检测器线程
                         */
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (flag_facecheck) {
                                        Logger.e("启动是否持续有人脸存在检测器线程");
                                        Thread.sleep(Global.Const.time_check);
                                        if (isContinue_check) {
                                            if (faceNumber.size() < 3) {
                                                isThreadWorking = true;
                                                isPosting = false;
                                                isContinue_fail = false;
                                                isContinue_error = false;
                                                flag_facecheck = false;
                                                isContinue_check = false;
                                                Logger.e("持续五秒没有人脸，关闭图片上传、检测器、计时器和识别算法");
                                                Message msg = handlerMain.obtainMessage();
                                                msg.arg1 = 4;
                                                handlerMain.sendMessage(msg);
                                            } else {
                                                faceNumber.clear();
                                            }
                                        }
                                    }
                                    Logger.e("是否持续有人脸存在检测器-->线程终止");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                        /**
                         *
                         * 失败计时器线程
                         */
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Logger.e("启动识别失败计时器");
                                    Thread.sleep(Global.Const.time_fail);
                                    if (isContinue_fail) {
                                        USBCameraUtil.getInstance().closeCamera();
                                        isThreadWorking = true;
                                        isPosting = false;
                                        isContinue_error = false;
                                        isContinue_fail = false;
                                        flag_facecheck = false;
                                        isContinue_check = false;
                                        Logger.e("识别失败，关闭图片上传、检测器、计时器和识别算法");
                                        Global.Varibale.isSuccess = false;
                                        startActivity(new Intent(UsbCameraActivity.this, Msg_LActivity.class));
                                        finish();
                                    }
                                    Logger.e("识别失败计时器-->线程终止");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                    break;
                case 3:
                    //预览界面画框
                    try {
                        if (faces != null) {
                            overlay.setFaces(faces);

                            //计算FPS
                            end = System.currentTimeMillis();
                            counter++;
                            double time = (double) (end - start) / 1000;
                            if (time != 0)
                                fps = counter / time;

                            overlay.setFPS(fps);

                            if (counter == (Integer.MAX_VALUE - 1000))
                                counter = 0;
                        }
                        isThreadWorking = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 4:
                    videoview.setVisibility(View.VISIBLE);
                    overlay.setVisibility(View.GONE);
                    ivFace.setVisibility(View.GONE);
                    startPlayVideo();
                    isThreadWorking = false;
                    start_faceCheck = true;
                    flag_facecheck = true;
                    isContinue_check = true;
                    isContinue_error = false;
                    isPosting = true;
                    result = 0;
                    resultNum = 0;
                    countSameName = 0;
                    faceNumber.clear();
                    break;
                case 5:

                    imgWeb.setImageBitmap(bitmap);
                    break;

                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtil.setFullScreen(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usbcamera);
        ButterKnife.bind(this);

        // 初始化人脸检测工具
        faces = new FaceResult[MAX_FACE];

        for (int i = 0; i < MAX_FACE; i++) {
            faces[i] = new FaceResult();
        }


        float aspect = (float) previewHeight / (float) previewWidth;
        fdet = new FaceDetector(prevSettingWidth, (int) (prevSettingWidth * aspect), MAX_FACE);

        //初始化画框
        overlay.setPreviewWidth(previewWidth);
        overlay.setPreviewHeight(previewHeight);
        if (overlay != null) {
            overlay.setDisplayOrientation(0);
        }

        //打开摄像头
        isThreadWorking = false;
        USBCameraUtil.getInstance().openCamera();
        USBCameraUtil.getInstance().setDataListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        startPlayVideo();
    }

    @Override
    public void onCameraData(final Bitmap bitmap) {

        this.bitmap = bitmap;

        Message msg = Message.obtain();
        msg.arg1 = 5;
        handlerMain.sendMessage(msg);

        if (bitmap != null) {

            if (!isThreadWorking) {
                isThreadWorking = true;

                ServiceThreadPoolManager.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {

                        float aspect = (float) previewHeight / (float) previewWidth;
                        int w = prevSettingWidth;
                        int h = (int) (prevSettingWidth * aspect);

                        Bitmap bmp = Bitmap.createScaledBitmap(bitmap, w, h, false);
                        bmp = ImageUtil.checkBit(bmp);

                        float xScale = (float) previewWidth / (float) prevSettingWidth;
                        float yScale = (float) previewHeight / (float) h;

                        //检测人脸
                        fdet = new FaceDetector(bmp.getWidth(), bmp.getHeight(), MAX_FACE);
                        final FaceDetector.Face[] fullResults = new FaceDetector.Face[MAX_FACE];
                        fdet.findFaces(bmp, fullResults);

                        if (!bmp.isRecycled()) {
                            bmp.recycle();
                        }

                        for (int i = 0; i < MAX_FACE; i++) {
                            if (fullResults[i] == null) {
                                faces[i].clear();
                            } else {
                                PointF mid = new PointF();
                                fullResults[i].getMidPoint(mid);

                                mid.x *= xScale;
                                mid.y *= yScale;

                                float eyesDis = fullResults[i].eyesDistance() * xScale;
                                float confidence = fullResults[i].confidence();
                                float pose = fullResults[i].pose(FaceDetector.Face.EULER_Y);//以Y轴定位左右
                                int idFace = Id;

                                Rect rect = new Rect(
                                        (int) (mid.x - eyesDis * 1.50f),
                                        (int) (mid.y - eyesDis * 2.00f),
                                        (int) (mid.x + eyesDis * 1.50f),
                                        (int) (mid.y + eyesDis * 2.00f));

                                /**
                                 * Only detect face size > 100x100
                                 */
                                if (rect.height() * rect.width() > Global.Const.detectSize * Global.Const.detectSize) {
                                    if (idFace == Id) {
                                        Id++;
                                    }

                                    faces[i].setFace(idFace, mid, eyesDis, confidence, pose, System.currentTimeMillis());
                                    faceCroped = ImageUtil.getFace(fullResults[i], bitmap, w, h, 0);
                                    //根据要求，最小边设置在200
                                    int min = Math.min(faceCroped.getWidth(), faceCroped.getHeight());
                                    double be = 1;
                                    if (min > 175) {
                                        be = (double) 175 / min;
                                        faceCroped = ImageUtil.compressSize(faceCroped, be);
                                    }

                                    //如果截图截到人脸进行和数据库比对
                                    if (faceCroped != null) {
                                        faceNumber.add(faceCroped);

                                        //通知跳转界面
                                        Message messageStartDetect = Message.obtain();
                                        messageStartDetect.arg1 = 2;
                                        handlerMain.sendMessage(messageStartDetect);

                                        //通知上传图片对比
                                        Message msgPost = Message.obtain();
                                        msgPost.arg1 = 1;
                                        handlerMain.sendMessage(msgPost);
                                    }
                                }
                            }
                        }
                        //通知画框
                        Message messageSetFace = Message.obtain();
                        messageSetFace.arg1 = 3;
                        handlerMain.sendMessage(messageSetFace);
                    }
                });
            }
        }
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        startPlayVideo();
    }

    private void startPlayVideo() {

        videoview.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.thinkjoy));
        videoview.requestFocus();
        videoview.start();  //开始播放
        videoview.setOnCompletionListener(this); //循环播放
    }

    @Override
    protected void onPause() {
//        Logger.i("onPause");
        super.onPause();

        Global.Varibale.httpTime = 0.0;
        Global.Varibale.confidence = 0.0;
        Global.Varibale.Comparename = "无";

        isPosting = false;
        isContinue_error = false;
        flag_facecheck = false;
        isThreadWorking = true;
        isContinue_fail = false;
        isContinue_check = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Global.Varibale.httpTime = 0.0;
        Global.Varibale.confidence = 0.0;
        Global.Varibale.Comparename = "正在识别中...";

        isPosting = false;
        isContinue_error = false;
        isThreadWorking = true;
        flag_facecheck = false;
        isContinue_fail = false;
        isContinue_check = false;

    }

    @Override
    protected void onStop() {
//        Logger.i("onStop");
        super.onStop();
        USBCameraUtil.getInstance().closeCamera();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);

        USBCameraUtil.getInstance().clear();

        try {
            if (!faceCroped.isRecycled()) {
                faceCroped.recycle();
            }
            faceCroped = null;

            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            bitmap = null;

        } catch (Exception e) {
            Logger.e("e = " + e.getMessage());
        }
    }
}