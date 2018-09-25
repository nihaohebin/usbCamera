package cn.usbfacedetect.util;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.FaceDetector;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.usbfacedetect.Global;


/**
 * 图片处理工具
 */
public class ImageUtil {

    /**
     * 转换图片成圆形
     */
    public static int mBorderWidth = 0;
    public static int mRadius = 0;
    public static int mCircleX = 0;
    public static int mCircleY = 0;

    public static Bitmap compressSize(Bitmap bitmap, double be) {
//        TLog.i("com:"+bitmap.getWidth()+"*"+bitmap.getHeight()+"-"+be);
        Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * be), (int) (bitmap.getHeight() * be), true);
//        TLog.i("com:"+bitmap1.getWidth()+"*"+bitmap1.getHeight()+"-"+be);
        return bitmap1;
    }

    /**
     * 头像bitmap转为base64   压缩一半输出  通过Base64  将Bitmap转String
     */
    public static String bitmap2StringHalf(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        String result;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 50, baos);                          // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {                                // 循环判断如果压缩后图片是否大于(maxkb)50kb,大于继续压缩
            baos.reset();                                                               // 重置baos即清空baos
            options -= 10;                                                              // 每次都减少10
            bitmap.compress(CompressFormat.JPEG, options, baos);                 // 这里压缩options%，把压缩后的数据存放到baos中
        }
        byte[] bitmapBytes = baos.toByteArray();
        result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
        return result;
    }

    /**
     * bitmap转为base64    不压缩原封转完输出
     */
    public static String bitmap2StringComplete(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * bitmap转为base64    不压缩原封转完输出
     */
    public static byte[] bitmap2Byte(Bitmap bitmap) {

        byte[] bitmapBytes = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                bitmapBytes = baos.toByteArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmapBytes;
    }

    /**
     * 将Base64字符串转换成Bitmap
     */
    public static Bitmap string2Bitmap(String st) {
        try {
            byte[] bitmapArray = Base64.decode(st, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 字节数组转Bitmap
     */
    public static Bitmap bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    /**
     * Bitmap转字节数组
     */
    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 保存图片到根目录+文件名
     */
    public static boolean saveBitmap2file(Bitmap bmp, String filename) {
        return saveBitmap2file(bmp, new File(Environment.getExternalStorageDirectory(), filename));
    }

    /**
     * 保存图片到文件
     */
    public static boolean saveBitmap2file(Bitmap bmp, File file) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            OutputStream stream = null;
            try {
                stream = new FileOutputStream(file);
                Log.i("TAG", "path = " + file.getAbsolutePath());
                Global.Varibale.path = file.getAbsolutePath();
                if (bmp.compress(CompressFormat.JPEG, 100, stream)) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    stream.flush();
                    stream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 网络图片转bitmap
     */
    public static Bitmap getBitmapFromUrl(String path) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(25000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 直接压缩的bitmap  图片按比例大小压缩方法
     */
    public static Bitmap compressImage(Bitmap image, int maxkb) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(CompressFormat.JPEG, 100, baos);

        if (baos.toByteArray().length / 1024 > maxkb) {                 //判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();                                               //重置baos即清空baos
            image.compress(CompressFormat.JPEG, 50, baos);       //这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        int ww = 480;
        int hh = 800;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;//降低图片从ARGB888到RGB565
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressBitmap(bitmap, maxkb);//压缩好比例大小后再进行质量压缩
    }

    /**
     * 从路径获取压缩的bitmap  图片按比例大小压缩方法
     */
    public static Bitmap compressImageFromPath(String srcPath, int maxkb) {

        Logger.i("压缩图片：文件" + srcPath);
        BitmapFactory.Options newOpts = new BitmapFactory.Options(); // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        int ww = 480;
        int hh = 800;
        int be = 1;                            // 竖形图片   缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可  be=1表示不缩放
        if (w > h && w > ww) {                 // 如果宽度大的话根据宽度固定大小缩放
            be = newOpts.outWidth / ww;
        } else if (w < h && h > hh) {          // 如果高度高的话根据高度固定大小缩放
            be = newOpts.outHeight / hh;
        }
        if (be <= 0) {
            be = 1;
        }
        newOpts.inSampleSize = be;                                       // 设置缩放比例
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);             // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        return compressBitmap(bitmap, maxkb);                               // 压缩好比例大小后再进行质量压缩
    }

    /**
     * 质量压缩  压缩一半
     */
    public static Bitmap compressBitmap(Bitmap image, int maxkb) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(CompressFormat.JPEG, 50, baos);                         // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 50;
//        Logger.i("原始大小" + baos.toByteArray().length);
        while ((baos.toByteArray().length / 1024) > maxkb && maxkb > 0) {              // 循环判断如果压缩后图片是否大于(maxkb)50kb,大于继续压缩
            Logger.i("压缩一次!");
            baos.reset();                                                             // 重置baos即清空baos
            options -= 10;                                                            // 每次都减少10
            image.compress(CompressFormat.JPEG, options, baos);                // 这里压缩options%，把压缩后的数据存放到baos中
        }
        Logger.i("压缩后大小" + baos.toByteArray().length);
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());      // 把压缩后的数据baos存放到ByteArrayInputStream中
        return BitmapFactory.decodeStream(isBm, null, null);
    }

    /**
     * 获取bitmap大小
     */
    @SuppressLint("NewApi")
    public static int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {                      // API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {               // API 12
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();                               // earlier version
    }

    /**
     * 从路径获取原始的Bitmap
     */
    public static Bitmap getBitmapFromPath(String path) {

        Bitmap bitmap = null;
        FileInputStream fis;
        try {
            fis = new FileInputStream(path);
            bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 从APPres资源中获取图片进行压缩
     * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
     * 官网：获取压缩后的图片     把相机照的图片压缩到手机的分辨率
     *
     * @param reqWidth  所需图片压缩尺寸最小宽度
     * @param reqHeight 所需图片压缩尺寸最小高度
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * 从文件路径中拿到图片压缩
     *
     * @param file      文件
     * @param reqWidth  最小宽度
     * @param reqHeight 最小高度
     */
    public static Bitmap decodeSampledBitmapFromFile(File file, int reqWidth, int reqHeight) {
        if (file == null) {
            return null;
        }
        return decodeSampledBitmapFromFile(file.getAbsolutePath(), reqWidth, reqHeight);
    }

    public static Bitmap decodeSampledBitmapFromFile(String filepath, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(filepath, options);
    }

    /**
     * 直接压缩一张bitmap图片资源
     */
    public static Bitmap decodeSampledBitmapFromBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
//        Logger.i("解压前大小:" + FileUtil.formetFileSize(ImageUtil.getBitmapSize(bitmap)));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap1 = BitmapFactory.decodeByteArray(data, 0, data.length, options);
//        Logger.i("解压后大小:" + FileUtil.formetFileSize(ImageUtil.getBitmapSize(bitmap1)));
        return bitmap1;
    }

    /**
     * 压缩图片，处理某些手机拍照角度旋转的问题
     */
    public static void compressImage(String filePath, String toFilePath, int maxkb, int reqWidth, int reqHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, 50, baos);                  // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            int quality = 100;
            while (baos.toByteArray().length / 1024 > maxkb) {                      // 循环判断如果压缩后图片是否大于(maxkb)kb,大于继续压缩
                baos.reset();                                                       // 重置baos即清空baos
                quality -= 10;                                                      // 每次都减少10
                if (quality <= 10) {
                    break;
                }
                bitmap.compress(CompressFormat.JPEG, quality, baos);         // 这里压缩options%，把压缩后的数据存放到baos中
            }

            File saveFile = new File(toFilePath);
            if (!saveFile.exists()) {
                saveFile.mkdirs();
            }
            FileOutputStream fout = new FileOutputStream(saveFile);
            baos.close();
            fout.write(baos.toByteArray());
            fout.flush();
            fout.close();

            if (!bitmap.isRecycled()) {
                bitmap.recycle();                                                   // 记得释放资源，否则会内存溢出
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算压缩比例值(改进版 by touch_ping)
     * 原版2>4>8...倍压缩 当前2>3>4...倍压缩
     *
     * @param options   解析图片的配置信息
     * @param reqWidth  所需图片压缩尺寸最小宽度O
     * @param reqHeight 所需图片压缩尺寸最小高度
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int picheight = options.outHeight;
        final int picwidth = options.outWidth;

        int targetheight = picheight;
        int targetwidth = picwidth;
        int inSampleSize = 1;

        if (targetheight > reqHeight || targetwidth > reqWidth) {
            while (targetheight >= reqHeight && targetwidth >= reqWidth) {
                inSampleSize += 1;
                targetheight = picheight / inSampleSize;
                targetwidth = picwidth / inSampleSize;
            }
        }
        return inSampleSize;
    }

    /**
     * 把一张bitmap图片 压缩到90%存入到指定位置
     */
    public static void saveMyBitmap(Bitmap mBitmap, String bitName) {
        File f = new File(Global.FilePath.PIC + bitName + ".jpg");
        if (!f.exists()) {
            f.mkdirs();
        }
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(f);
            mBitmap.compress(CompressFormat.JPEG, 90, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);// 从指定路径下读取图片，并获取其EXIF信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);// 获取图片的旋转信息
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm;
        Matrix matrix = new Matrix();       // 根据旋转角度，生成旋转矩阵
        matrix.postRotate(degree);
        try {
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);  // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            returnBm = null;
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    /**
     * bitmap旋转角度
     */
    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            try {
                Matrix m = new Matrix();
                m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
                Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                ex.printStackTrace();
            }
        }
        return b;
    }

    /**
     * 图片压缩后保存为文件
     */
    public static void compressBmp2File(Bitmap bmp, File file) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 90;
        bmp.compress(CompressFormat.JPEG, options, baos);
        while (baos.toByteArray().length / 1024 > 150) {
            baos.reset();
            options -= 10;
            bmp.compress(CompressFormat.JPEG, options, baos);
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照图片压缩成文件
     */
    public static void compressPhoto2File(Bitmap bmp, File file) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 90;
        bmp.compress(CompressFormat.JPEG, options, baos);
        while (baos.toByteArray().length / 1024 > 100 && options > 0) {
            baos.reset();
            options -= 10;
            bmp.compress(CompressFormat.JPEG, options, baos);
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getFace(FaceDetector.Face face, Bitmap bitmap, int previewWidth, int previewHeight, int rotate) {
        Bitmap bmp;

        bitmap = rotate(bitmap, rotate);
        float scaleX = 1.0f;
        float scaleY = 1.0f;
        if (bitmap.getWidth() < bitmap.getHeight()) {
            scaleX = (float) bitmap.getWidth() / (float) previewHeight;
            scaleY = (float) bitmap.getHeight() / (float) previewWidth;
        } else {
            scaleX = (float) bitmap.getWidth() / (float) previewWidth;
            scaleY = (float) bitmap.getHeight() / (float) previewHeight;
        }


        float eyesDis = face.eyesDistance();
        PointF mid = new PointF();
        face.getMidPoint(mid);
        Rect rect = new Rect(
                (int) ((mid.x - eyesDis * 1.80f) * scaleX),
                (int) ((mid.y - eyesDis * 2.20f) * scaleY),
                (int) ((mid.x + eyesDis * 1.80f) * scaleX),
                (int) ((mid.y + eyesDis * 2.20f) * scaleY));

        Bitmap.Config config = Bitmap.Config.RGB_565;
        if (bitmap.getConfig() != null) {
            config = bitmap.getConfig();
        }
        bmp = bitmap.copy(config, true);
        bmp = cropBitmap(bmp, rect);
        return bmp;
    }

    public static Bitmap cropBitmap(Bitmap bitmap, Rect rect) {
        Bitmap ret = null;
        try {
            int w = rect.right - rect.left;
            int h = rect.bottom - rect.top;
            ret = Bitmap.createBitmap(w, h, bitmap.getConfig());
            Canvas canvas = new Canvas(ret);
            canvas.drawBitmap(bitmap, -rect.left, -rect.top, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        return ret;
    }

    /**
     * 检测图片格式是否符合本地检测，不符合自动转换
     */
    public static Bitmap checkBit(Bitmap bitmap) {
        Bitmap bit = bitmap;
        if (bitmap.getConfig() != Bitmap.Config.RGB_565) {
            bit = bitmap.copy(Bitmap.Config.RGB_565, true);
        }

        if (bitmap.getWidth() % 2 != 0) {
            bit = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() - 1, bitmap.getHeight(), true);
        }
        return bit;
    }

    //Rotate Bitmap
    public static Bitmap rotate(Bitmap b, float degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2,
                    (float) b.getHeight() / 2);

            Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
                    b.getHeight(), m, true);
            if (b != b2) {
                b.recycle();
                b = b2;
            }

        }
        return b;
    }

    public static Bitmap getBitmap(String filePath, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        if (bitmap != null) {
            try {
                ExifInterface ei = new ExifInterface(filePath);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        bitmap = rotate(bitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        bitmap = rotate(bitmap, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        bitmap = rotate(bitmap, 270);
                        break;
                    // etc.
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 按比例压缩
     *
     * @param image
     * @param width
     * @param height
     * @return
     */
    public static Bitmap comp(Bitmap image, int width, int height) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(CompressFormat.JPEG, 100, baos);
        int quity = 100;
        if (baos.toByteArray().length / 1024 > 1024 && quity > 0) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            quity = quity - 10;
            baos.reset();//重置baos即清空baos
            image.compress(CompressFormat.JPEG, quity, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是1280*720分辨率，所以高和宽建议设置为
        float hh = height;//这里设置高度为1280f
        float ww = width;//这里设置宽度为720f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        float be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (float) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (float) (newOpts.outHeight / hh);
        }
        if (be <= 0) {
            be = 1;
        } else if (be % 1 > 0.6) {
            be = be + 1;
        }
        newOpts.inSampleSize = (int) be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return bitmap;//压缩好比例大小后再进行质量压缩
    }

    public static Bitmap getFace(FaceDetector.Face face, Bitmap bitmap) {
        Bitmap bmp = null;
        float eyesDis = face.eyesDistance();
        PointF mid = new PointF();
        face.getMidPoint(mid);

        Rect rect = new Rect(
                (int) (mid.x - eyesDis * 1.50f),
                (int) (mid.y - eyesDis * 2.00f),
                (int) (mid.x + eyesDis * 1.50f),
                (int) (mid.y + eyesDis * 2.00f));

        Bitmap.Config config = Bitmap.Config.RGB_565;
        if (bitmap.getConfig() != null) config = bitmap.getConfig();
        bmp = bitmap.copy(config, true);
        bmp = cropBitmap(bmp, rect);
        Matrix matrix = new Matrix();
//        matrix.setScale(-1,1);        /*翻转180度*/
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bit = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        return bit;
    }

    /**
     * @param bitmap
     * @param flag   0水平翻转，1垂直翻转
     * @return
     */
    public static Bitmap reverseBitmap(Bitmap bitmap, int flag) {
        float[] floats = null;
        switch (flag) {
            case 0:
                floats = new float[]{-1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};

                break;
            case 1:
                floats = new float[]{1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f};
                break;
        }
        if (floats != null) {
            Matrix matrix = new Matrix();
            matrix.setValues(floats);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return null;
    }

    public static Drawable toRoundDrawable(Bitmap bitmap, int borderWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);

        if (borderWidth != 0) {
            drawBorder(canvas, width, height, borderWidth);
        }

        return new BitmapDrawable(output);
    }

    public static void drawBorder(Canvas canvas, int w, int h, int borderWith) {
        mRadius = (w - borderWith) / 2;
        mCircleX = (w) / 2;
        mCircleY = (h) / 2;
        Paint mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(borderWith);
        mBorderPaint.setColor(0xffffffff);
        mBorderPaint.setStrokeJoin(Paint.Join.ROUND);
        mBorderPaint.setStrokeCap(Paint.Cap.ROUND);

        canvas.drawCircle(mCircleX, mCircleY, mRadius, mBorderPaint);
    }

    public static String compressSizeBase64(Bitmap bitmap) {
        double be = 1;
        int max = Math.max(bitmap.getWidth(), bitmap.getHeight());
        if (max > 720) {
            be = (double) 720 / max;
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * be), (int) (bitmap.getHeight() * be), true);
        }

        String result;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 70, baos);                          // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {                                // 循环判断如果压缩后图片是否大于(maxkb)50kb,大于继续压缩
            baos.reset();                                                               // 重置baos即清空baos
            options -= 10;                                                              // 每次都减少10
            bitmap.compress(CompressFormat.JPEG, options, baos);                 // 这里压缩options%，把压缩后的数据存放到baos中
        }
        byte[] bitmapBytes = baos.toByteArray();
        result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
        return result;
    }

}
