package cn.usbfacedetect.util.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.module.GlideModule;

/**
 * Created by Anan on 2017/3/5.
 * (1)清单文件注册
 * <application ...>
 * <meta-data
 * android:name="com.mypackage.MyGlideModule"
 * android:value="GlideModule" />
 * </application>
 * (2)混淆
 * -keepnames class com.mypackage.MyGlideModule
 * (3)多个glideModule冲突
 * <meta-data android:name=”com.mypackage.MyGlideModule” tools:node=”remove” />
 */
public class ProjectGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        //设置图片解码格式
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
//        File cacheDir = context.getExternalCacheDir();//指定的是数据的缓存地址
        int diskCacheSize = 1024 * 1024 * 30;//最多可以缓存多少字节的数据
        //存放在data/data/xxxx/cache/
//        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, "image", diskCacheSize));
        //存放在外置文件浏览器
        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, "image", diskCacheSize));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
