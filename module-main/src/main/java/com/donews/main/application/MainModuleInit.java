package com.donews.main.application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.donews.base.base.BaseApplication;
import com.donews.common.IModuleInit;
import com.donews.middle.utils.HotStartCacheUtils;
import com.donews.yfsdk.manager.AdConfigManager;
import com.donews.common.adapter.ScreenAutoAdapter;
import com.donews.main.BuildConfig;
import com.donews.main.ui.SplashActivity;
import com.donews.main.utils.ExitInterceptUtils;
import com.donews.middle.front.FrontConfigManager;
import com.donews.network.EasyHttp;
import com.donews.network.cache.converter.GsonDiskConverter;
import com.donews.network.cache.model.CacheMode;
import com.donews.network.cookie.CookieManger;
import com.donews.network.model.HttpHeaders;
import com.donews.utilslibrary.utils.AppInfo;
import com.donews.utilslibrary.utils.DeviceUtils;
import com.donews.utilslibrary.utils.LogUtil;

import org.jetbrains.annotations.NotNull;

/**
 * 应用模块: main
 * <p>
 * 类描述: main组件的业务初始化
 * <p>
 *
 * @author darryrzhoong
 * @since 2020-02-26
 */
public class MainModuleInit implements IModuleInit {
    private int appCount;
    private long stopTime;

    private final Handler mHandler = new Handler(Looper.myLooper());
    private Application.ActivityLifecycleCallbacks callbacks = new Application.ActivityLifecycleCallbacks() {


        @Override
        public void onActivityCreated(@NotNull Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(@NotNull Activity activity) {
            if (appCount <= 0) {
                toForeGround(activity);
                HotStartCacheUtils.INSTANCE.checkNotifyActivity(activity);
            } else {
                HotStartCacheUtils.INSTANCE.checkActivity(activity);
            }
            appCount++;
        }

        @Override
        public void onActivityResumed(@NotNull Activity activity) {

        }

        @Override
        public void onActivityPaused(@NotNull Activity activity) {
        }

        @Override
        public void onActivityStopped(@NotNull Activity activity) {
            appCount--;
            if (appCount == 0) {
                stopTime = System.currentTimeMillis();
                delayLoadAd(activity);
            }

        }

        @Override
        public void onActivitySaveInstanceState(@NotNull Activity activity, @NotNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NotNull Activity activity) {

        }
    };

    /**
     * 进入前台
     */
    private void toForeGround(Activity activity) {
        int backGroundInt = AdConfigManager.INSTANCE.getMNormalAdBean().getSplash().getPreload();
        if (activity instanceof SplashActivity || backGroundInt == 0) {
            return;
        }
        long currentTime = System.currentTimeMillis();

        long seconds = (currentTime - stopTime) / 1000;
        LogUtil.d("toForeGround: seconds:" + seconds);
        LogUtil.e(activity.getClass().getName());
        if (seconds > backGroundInt) {
            HotStartCacheUtils.INSTANCE.showAd();
//            SplashActivity.toForeGround(activity);
        } else {
            HotStartCacheUtils.INSTANCE.dismiss();
        }
    }


    private void delayLoadAd(Activity activity) {
        int backGroundInt = AdConfigManager.INSTANCE.getMNormalAdBean().getSplash().getPreload();
        if (activity instanceof SplashActivity || backGroundInt == 0) {
            return;
        }
        HotStartCacheUtils.INSTANCE.addHotStartAdDialog();
        //在满足后台时间的需求后再加载广告
        // 屏蔽预加载-by aaron.din
//        mHandler.removeCallbacksAndMessages(null);
        /*mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HotStartCacheUtils.INSTANCE.loadAd();
            }
        }, backGroundInt * 1000L);*/
    }

    @Override
    public boolean onInitAhead(BaseApplication application) {
        ScreenAutoAdapter.setup(application);
        EasyHttp.init(application);
        if (LogUtil.allow) {
            EasyHttp.getInstance().debug("HoneyLife", true);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.HEAD_PACKAGENMAE, DeviceUtils.getPackage());
        httpHeaders.put(HttpHeaders.HEAD_AUTHORIZATION, AppInfo.getToken(""));
        EasyHttp.getInstance()
                .setBaseUrl(BuildConfig.BASE_CONFIG_URL)
                .setReadTimeOut(15 * 1000)
                .setWriteTimeOut(15 * 1000)
                .setConnectTimeout(15 * 1000)
                .setRetryCount(3)
                .setCookieStore(new CookieManger(application))
                .setCacheDiskConverter(new GsonDiskConverter())
                .setCacheMode(CacheMode.FIRSTREMOTE)
                .addCommonHeaders(httpHeaders);

        application.registerActivityLifecycleCallbacks(callbacks);

        ExitInterceptUtils.INSTANCE.init();

        FrontConfigManager.Ins().init();
        return false;
    }

    @Override
    public boolean onInitLow(BaseApplication application) {
        return false;
    }
}
