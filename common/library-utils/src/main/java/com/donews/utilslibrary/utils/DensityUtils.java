package com.donews.utilslibrary.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.donews.utilslibrary.base.UtilsConfig;

/**
 * <b>类名称：</b> DensityUtils <br/>
 * <b>类描述：</b> 常用单位转换的辅助类<br/>
 * <b>创建人：</b> 林肯 <br/>
 * <b>修改人：</b> 编辑人 <br/>
 * <b>修改时间：</b> 2015年07月27日 15:25:13 <br/>
 * <b>修改备注：</b> <br/>
 *
 * @version 1.0.0 <br/>
 */
public class DensityUtils {
    private DensityUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }


    public static int dip2px(Context context, float dpVal) {
        return dp2px(context, dpVal);
    }

    public static int dip2px(float dpVal) {
        return dp2px(UtilsConfig.getApplication(), dpVal);
    }

    public DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    /**
     * dp转px
     *
     * @param context
     * @param dpVal
     * @return pxVal
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    public static int dp2px(float dpVal) {
        return dp2px(UtilsConfig.getApplication(), dpVal);
    }


    /**
     * sp转px
     *
     * @param context
     * @param spVal
     * @return pxVal
     */
    public static int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }

    /**
     * px转dp
     *
     * @param context
     * @param pxVal
     * @return dpVal
     */
    public static float px2dp(Context context, float pxVal) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (pxVal / scale);
    }

    public static float px2dp(float pxVal) {
        return px2dp(UtilsConfig.getApplication(), pxVal);
    }


    /**
     * px转sp
     *
     * @param context
     * @param pxVal
     * @return spVal
     */
    public static float px2sp(Context context, float pxVal) {
        return (pxVal / context.getResources().getDisplayMetrics().scaledDensity);
    }

    /**
     * 获取屏幕分辨率
     *
     * @param context
     * @return
     */
    public static String getPhoneSize(final Context context) {
        DisplayMetrics dm;
        dm = context.getResources().getDisplayMetrics();

       /* int densityDPI = dm.densityDpi; // 屏幕密度（每寸像素：120/160/240/320）
        float xdpi = dm.xdpi;
        float ydpi = dm.ydpi;*/
        int screenWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
        int screenHeight = dm.heightPixels; // 屏幕高（像素，如：800px）

        return screenWidth + "*" + screenHeight;
    }

    /**
     * 获取屏幕宽度
     *
     * @return int, screen width
     */
    public static int getScreenWidth() {
        DisplayMetrics dm;
        dm = UtilsConfig.getApplication().getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获取屏幕宽度
     *
     * @return int, screen width
     */
    public static int getScreenHeight() {
        DisplayMetrics dm;
        dm = UtilsConfig.getApplication().getResources().getDisplayMetrics();
        return dm.heightPixels;
    }


}