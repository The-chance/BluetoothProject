package com.chenjimou.braceletdemo.uitls;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.RequiresApi;

public class DisplayUtils
{

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth(Activity activity) {
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight(Activity activity) {
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getHeight();
    }

    /**
     * 获取设备操作系统版本
     *
     * @return
     */
    public static String getOsVersion()
    {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取设备型号
     *
     * @return
     */
    public static String getDevice()
    {
        return android.os.Build.MODEL;
    }

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     *
     * @param pxValue 需要转换的值
     * @return 转换后的值
     */
    public static int px2dip(Activity activity, float pxValue)
    {
        final float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue 需要转换的值
     * @return 转换后的值
     */
    public static int dip2px(Activity activity, float dipValue)
    {
        final float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue 需要转换的值
     * @return 转换后的值
     */
    public static int px2sp(Activity activity, float pxValue)
    {
        final float fontScale = activity.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue 需要转换的值
     * @return 转换后的值
     */
    public static int sp2px(Activity activity, float spValue)
    {
        final float fontScale = activity.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     *
     * @param pxValue
     * @param scale
     *            （DisplayMetrics类中属性density）
     * @return
     */
    public static int px2dip(float pxValue, float scale)
    {
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @param scale
     *            （DisplayMetrics类中属性density）
     * @return
     */
    public static int dip2px(float dipValue, float scale)
    {
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @param fontScale
     *            （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public int px2sp(float pxValue, float fontScale) {
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @param fontScale
     *            （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public int sp2px(float spValue, float fontScale)
    {
        return (int) (spValue * fontScale + 0.5f);
    }

    public int getScreenWidthctx(Activity activity)
    {
        return activity.getResources().getDisplayMetrics().widthPixels;
    }

    public int getScreenHeightctx(Activity activity)
    {
        return activity.getResources().getDisplayMetrics().heightPixels;
    }

    public float getScreenDensity(Activity activity)
    {
        return activity.getResources().getDisplayMetrics().density;
    }

    /**
     * 利用反射获取状态栏高度
     */
    public static int getStatusBarHeight(Activity activity)
    {
        int result = 0;
        //获取状态栏高度的资源id
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void setStatusBar(Activity activity)
    {
        if (Build.VERSION.SDK_INT >= 21) {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
