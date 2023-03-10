package com.donews.middle.adutils

import android.app.Application
import com.donews.middle.BuildConfig
import com.donews.yfsdk.YfAdSdk
import com.donews.yfsdk.bean.AdConfigBean

object DnSdkInit {

    private var s_is_init_before_load_ad = false

    fun init(application: Application) {
        if (BuildConfig.HTTP_DEBUG) {
            // 测试
            YfAdSdk.init(
                application,
                AdConfigBean("158535", "158592", "1484", "1478", "1477", "1476", "1478", ""))
        } else {
            // 正式
            YfAdSdk.init(
                application,
                AdConfigBean("1827", "1828", "158536", "1832", "", "", "", ""));
        }
    }

    fun initBeforeLoadAd(application: Application) {
        if (!s_is_init_before_load_ad) {
            init(application)
            s_is_init_before_load_ad = true
        }
    }
}