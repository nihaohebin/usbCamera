package cn.usbfacedetect.util.glide;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;

import cn.usbfacedetect.util.glide.transformation.BlurTransformation;
import cn.usbfacedetect.util.glide.transformation.CircleTransformation;

/**
 * Created by Anan on 2017/3/5.
 * * Glide工具类
 */
public class GlideUtil {

    //默认加载
    public static void loadImageView(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).load(path).into(mImageView);
    }

    //加载指定大小
    public static void loadImageViewSize(Context mContext, String path, int width, int height, ImageView mImageView) {
        Glide.with(mContext).load(path).override(width, height).into(mImageView);
    }

    //填充图片
    public static void loadImageCrop(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).load(path).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).into(mImageView);
    }

    //设置加载中以及加载失败图片
    public static void loadImageView(Context mContext, String path, ImageView mImageView, int defaultImage, int errorImageView) {
        Glide.with(mContext).load(path).placeholder(defaultImage).error(errorImageView).into(mImageView);
    }

    //设置加载中以及加载失败图片并且指定大小
    public static void loadImageView(Context mContext, String path, int width, int height, ImageView mImageView, int lodingImage, int errorImageView) {
        Glide.with(mContext).load(path).override(width, height).placeholder(lodingImage).error(errorImageView).into(mImageView);
    }

    //设置跳过内存缓存
    public static void loadImageViewCache(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).load(path).skipMemoryCache(true).into(mImageView);
    }

    public static void loadImageViewAsBitmap(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).load(path).asBitmap().into(mImageView);
    }


    /**
     * 设置下载优先级
     * 优先级说明
     * Priority.LOW
     * Priority.NORMAL
     * Priority.HIGH
     * Priority.IMMEDIATE
     */

    public static void loadImageViewPriority(Context mContext, String path, Priority priority, ImageView mImageView) {
        Glide.with(mContext).load(path).priority(priority).into(mImageView);
    }

    /**
     * 策略解说：
     * all:缓存源资源和转换后的资源
     * none:不作任何磁盘缓存
     * source:缓存源资源
     * result：缓存转换后的资源
     */

    //设置缓存策略
    public static void loadImageViewDiskCache(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).load(path).diskCacheStrategy(DiskCacheStrategy.NONE).into(mImageView);
    }

    /**
     * 设置加载动画
     * api也提供了几个常用的动画：比如crossFade()
     */
    public static void loadImageViewAnim(Context mContext, String path, int anim, ImageView mImageView) {
        Glide.with(mContext).load(path).animate(anim).into(mImageView);
    }

    /**
     * 设置缩略图支持, 会先加载缩略图,然后加载全图
     * 默认为原图的1/10
     */
    public static void loadImageViewThumbnail(Context mContext, String path, ImageView mImageView) {
        loadImageViewThumbnail(mContext, path, 0.1f, mImageView);
    }

    /**
     * 设置缩略图:注意这里的图片会变小，设置好imageView ScaleType
     *
     * @param thumb 缩略图比例,值为0~1
     */
    public static void loadImageViewThumbnail(Context mContext, String path, float thumb, ImageView mImageView) {
        Glide.with(mContext).load(path).thumbnail(thumb).into(mImageView);
    }

    /**
     * 设置动态转换
     * api提供了比如：
     * centerCrop():均衡缩放头像,保持图像原始比例，图像位于视图中心
     * fitCenter():缩放头像，在视图中使图像居中
     */
    public static void loadImageViewCrop(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).load(path).centerCrop().into(mImageView);
    }

    public static void loadImageViewFit(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).load(path).fitCenter().into(mImageView);
    }

    //设置动态GIF加载方式
    public static void loadImageViewDynamicGif(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).load(path).asGif().into(mImageView);
    }

    //设置静态GIF加载方式:显示Gif的第一帧
    public static void loadImageViewStaticGif(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).load(path).asBitmap().into(mImageView);
    }

    /**
     * 设置监听请求接口
     * 设置监听的用处 可以用于监控请求发生错误来源，以及图片来源是内存还是磁盘
     */
    public static void loadImageViewListener(Context mContext, String path, ImageView mImageView, RequestListener<String, GlideDrawable> requestListener) {
        Glide.with(mContext).load(path).listener(requestListener).into(mImageView);
    }

    //项目中有很多需要先下载图片然后再做一些合成的功能，比如项目中出现的图文混排

    //设置要加载的内容
    public static void loadImageViewContent(Context mContext, String path, SimpleTarget<GlideDrawable> simpleTarget) {
        Glide.with(mContext).load(path).centerCrop().into(simpleTarget);
    }

    //清理磁盘缓存
    public static void GuideClearDiskCache(Context mContext) {
        //理磁盘缓存 需要在子线程中执行
        Glide.get(mContext).clearDiskCache();
    }

    //清理内存缓存
    public static void clearMemory(Context mContext) {
        //清理内存缓存  可以在UI主线程中进行
        Glide.get(mContext).clearMemory();
    }

    /**
     * 加载圆形图片
     * 注意：Transformation可以在transform()中叠加使用，自行实现。
     */
    public static void loadCircleImage(Context mContext, String path, ImageView imageView) {
        Glide.with(mContext).load(path).transform(new CircleTransformation(mContext)).into(imageView);
    }

    /**
     * 加载模糊图片
     */
    public static void loadBlurImage(Context mContext, String path, ImageView imageView) {
        Glide.with(mContext).load(path).transform(new BlurTransformation(mContext)).into(imageView);
    }

    /**
     * 后台获取图片
     */

    /**
     * 自定义Target
     */
    /**
     * 自定义数据源:ModelLoader
     * 根据不同的要求请求不同尺寸不同质量的图片
     */
}
