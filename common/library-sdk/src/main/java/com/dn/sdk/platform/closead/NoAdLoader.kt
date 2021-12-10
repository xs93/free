package com.dn.sdk.platform.closead


import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.dn.sdk.AdCustomError
import com.dn.sdk.bean.AdRequest
import com.dn.sdk.bean.PreloadAdState
import com.dn.sdk.listener.*
import com.dn.sdk.loader.IAdLoader
import com.dn.sdk.loader.SdkType
import com.dn.sdk.platform.closead.preload.NoAdPreloadRewardVideo
import com.dn.sdk.platform.closead.preload.NoAdPreloadSplashAd
import com.dn.sdk.bean.preload.PreloadRewardVideoAd
import com.dn.sdk.bean.preload.PreloadSplashAd


/**
 * 关闭广告加载器
 *
 * @author XuShuai
 * @version v1.0
 * @date 2021/9/27 11:03
 */
class NoAdLoader : IAdLoader {

    private val mDelayHandler = Handler(Looper.getMainLooper())

    /** 延迟执行 */
    private fun delayExec(runnable: () -> Unit) {
        mDelayHandler.postDelayed({
            runnable.invoke()
        }, 500)
    }

    override fun getSdkType(): SdkType {
        return SdkType.CLOSE_AD
    }

    override fun loadAndShowSplashAd(activity: Activity, adRequest: AdRequest, listener: IAdSplashListener?) {
        listener?.onAdError(AdCustomError.CloseAd.code, AdCustomError.CloseAd.errorMsg)
    }

    override fun preloadSplashAd(
        activity: Activity,
        adRequest: AdRequest,
        listener: IAdSplashListener?
    ): PreloadSplashAd {
        val preloadSplashAd = NoAdPreloadSplashAd()
        preloadSplashAd.setLoadState(PreloadAdState.Error)
        //延迟执行，防止错误先执行，然后才返回预加载对象
        delayExec {
            listener?.onAdError(AdCustomError.CloseAd.code, AdCustomError.CloseAd.errorMsg)
        }
        return preloadSplashAd
    }

    override fun loadAndShowBannerAd(activity: Activity, adRequest: AdRequest, listener: IAdBannerListener?) {
        listener?.onAdError(AdCustomError.CloseAd.code, AdCustomError.CloseAd.errorMsg)
    }

    override fun loadAndShowInterstitialAd(
        activity: Activity,
        adRequest: AdRequest,
        listener: IAdInterstitialListener?
    ) {
        listener?.onAdError(AdCustomError.CloseAd.code, AdCustomError.CloseAd.errorMsg)
    }

    override fun loadAndShowRewardVideoAd(activity: Activity, adRequest: AdRequest, listener: IAdRewardVideoListener?) {
        listener?.onAdError(AdCustomError.CloseAd.code, AdCustomError.CloseAd.errorMsg)
    }

    override fun preloadRewardVideoAd(
        activity: Activity,
        adRequest: AdRequest,
        listener: IAdRewardVideoListener?
    ): PreloadRewardVideoAd {
        val preloadRewardVideoAd = NoAdPreloadRewardVideo()
        preloadRewardVideoAd.setLoadState(PreloadAdState.Error)
        delayExec {
            listener?.onAdError(AdCustomError.CloseAd.code, AdCustomError.CloseAd.errorMsg)
        }
        return preloadRewardVideoAd
    }

    override fun loadNativeTemplateAd(activity: Activity, adRequest: AdRequest, listener: IAdNativeTemplateListener?) {
        listener?.onAdError(AdCustomError.CloseAd.code, AdCustomError.CloseAd.errorMsg)
    }

    override fun loadNativeAd(activity: Activity, adRequest: AdRequest, listener: IAdNativeLoadListener?) {
        listener?.onAdError(AdCustomError.CloseAd.code, AdCustomError.CloseAd.errorMsg)
    }
}