package cn.usbfacedetect;

import android.os.Environment;

/**
 * Author：hebin on 2016/10/27 0027
 * <p>
 * Annotations：存放全局变量值
 */
public class Global {

    /**
     * 常量
     */
    public static class Const {
        //贺斌用户
        public final static String userId = "hebin";

        //各个人脸集合
        public final static String faceSetName = "hebin";  //贺斌测试集合

        public final static int detectSize = 80;
        public final static double recognitionRate = 0.92;        //识别率
        public final static int PermissionCallBack = 5;          //权限回调
        public final static int PermissionSetting = 6;           //权限设置界面
        public final static int degree = 0;                         //普通手机参数
        public static final String reConnect = "com.tjdoorsystem.connect";

        /**
         * sp数据常量  key值
         */
        public final static String  isFlushStaffMessage = "isFlushStaffMessage";
        public final static String isLogin = "isLogin";
        public final static String userInfo = "userInfo";
        public final static String  userInfoList = "userInfoList";

        /**
         * 注意下面的时间根据不同的设备会用不同感官效果  影响大
         */
        public final static int time_check = 3000;         //识别检测器
        public final static int time_fail = 6000;         //识别失败定时器

        public final static int display_success = 5000;           //识别成功信息展示定时器
        public final static int display_fail = 5000;              //识别失败信息展示定时器


        public static final String faceName = "face.jpg";
        public static final String age = "age";
        public static final String genderMan = "genderMan";
        public static final String genderWoman = "genderWoman";

        public static final int width = 1280;
        public static final int height = 720;
        public static final int devId = 4;


    }


    /**
     * 变量
     */
    public static class Varibale {

        public static boolean isContinueGetBitmap = true;
        public static boolean isSuccess = true;            //展示成功界面还是失败界面
        public static boolean isFirstConnect = false;

        public static double httpTime = 0.0;        //用于传递http请求到响应时间
        public static double confidence = 0.0;      //用于传递对比结果相似度

        public static String Comparename = "";       //用于传递对比结果名字
        public static String successName = "";       //用于信息展示本地判断   拿的是最后一次成功时的名字作为判断依据



        public static String age = "";
        public static String gender_woman = "";
        public static String gender_man = "";
        public static String path = "";
    }

    /**
     * URL地址
     */
    public static class UrlPath {
        /**
         * HTTP接口
         */
        public static String MANAGER = "http://120.25.235.44:8080/appmanage";                //APP管理系统地址
        public static String PostError = MANAGER + "/app/appErrorInfo/add";                  //错误日志（本地）

        //        public static String ROOTPATH = "http://192.168.0.223:8085/door/api/";
        public static String ROOTPATH = "http://112.74.101.131:8780/door/api/";
        public static String login = ROOTPATH + "login";
        public static String checkWork = ROOTPATH + "checkWork";
        public static String getEmployee = ROOTPATH + "getEmployee";

    }

    /**
     * 文件路径地址
     */
    public static class FilePath {
        public final static boolean HASSDCARD = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        public final static String SDCARDPATH = Environment.getExternalStorageDirectory().getAbsolutePath();
        public final static String APPPATH = HASSDCARD ? SDCARDPATH + "/jiadu/doorsecurity" : "/data/data/cn.doorsecurity/files";
        public final static String AppCrash = APPPATH + "/crash";  //异常捕捉文件存储类
        public final static String PIC = APPPATH + "/pic";
        public final static String AppDown = APPPATH + "/down";

        /**
         * 保存数据文件名
         */
        public final static String httpTime = "httpTime.txt";
        public final static String compareResult = "compareResult.txt";
        public final static String bmpSize = "bmpSize.txt";
        public final static String androidDealTime = "androidDealTime.txt";

    }
}
