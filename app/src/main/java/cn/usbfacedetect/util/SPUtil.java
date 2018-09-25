package cn.usbfacedetect.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

import cn.usbfacedetect.Global;
import cn.usbfacedetect.bean.UserInfo;


/**
 * Author：hebin on 2016/9/14 0014
 * <p/>
 * Annotations：1.apply没有返回值而commit返回boolean表明修改是否提交成功
 * 2.apply是将修改数据原子提交到内存, 而后异步真正提交到硬件磁盘, 而commit是同步的提交到硬件磁盘
 * 3.apply方法不会提示任何失败的提示 apply的效率高一些，如果没有必要确认是否提交成功建议使用apply。
 */
public class SPUtil {

    /**
     * 存储后的文件路径：/data/data/<package name>/shares_prefs + 文件名.xml
     */
    public static final String PATH = "/data/data/code.sharedpreferences/shared_prefs/Database.xml";
    private String spName = "appData";
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Gson gson= new Gson();

    /**
     * 用于发广播的上下文
     */
    private static SPUtil spUtil = null;

    /**
     * SPUtils构造函数
     */
    public SPUtil(Context context) {
        sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.apply();
    }

    public static synchronized SPUtil getInstance(Context context) {
        if (spUtil == null) {
            spUtil = new SPUtil(context);
        }
        return spUtil;
    }
    
    

    /**
     * SP中写入String类型value
     */
    public void putString(String key, String value) {
        editor.putString(key, value).apply();
    }
    public String getString(String key) {
        return getString(key, null);
    }
    public String getString(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    /**
     * SP中写入int类型value
     */
    public void putInt(String key, int value) {
        editor.putInt(key, value).apply();
    }
    public int getInt(String key) {
        return getInt(key, -1);
    }
    public int getInt(String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    /**
     * SP中写入long类型value
     */
    public void putLong(String key, long value) {
        editor.putLong(key, value).apply();
    }
    public long getLong(String key) {
        return getLong(key, -1L);
    }
    public long getLong(String key, long defaultValue) {
        return sp.getLong(key, defaultValue);
    }

    /**
     * SP中写入float类型value
     */
    public void putFloat(String key, float value) {
        editor.putFloat(key, value).apply();
    }
    public float getFloat(String key) {
        return getFloat(key, -1f);
    }
    public float getFloat(String key, float defaultValue) {
        return sp.getFloat(key, defaultValue);
    }

    /**
     * SP中写入boolean类型value
     */
    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value).apply();
    }
    public boolean getBoolean(String key) {
        return getBoolean(key, true);
    }
    public boolean getBoolean(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    /**
     * 获取SP中所有键值对
     */
    public Map<String, ?> getAll() {
        return sp.getAll();
    }

    /**
     * 从SP中移除该key
     */
    public void remove(String key) {
        editor.remove(key).apply();
    }

    /**
     * 判断SP中是否存在该key
     */
    public boolean contains(String key) {
        return sp.contains(key);
    }

    /**
     * 清除SP中所有数据
     */
    public void clear() {
        editor.clear().apply();
    }

    /**
     * 获取userInfo
     */
    public UserInfo getUserInfo(){
        String userinfo = getString(Global.Const.userInfo);
        return gson.fromJson(userinfo,UserInfo.class);
    }

    /**
     * 获取userInfoList
     */

    public List<UserInfo> getUserInfoList() {
        String weather = getString(Global.Const.userInfoList);
        return gson.fromJson(weather, new TypeToken<List<UserInfo>>() {
        }.getType());
    }


}
