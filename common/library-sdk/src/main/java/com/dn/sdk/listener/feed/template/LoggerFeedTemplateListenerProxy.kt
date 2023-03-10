package com.dn.sdk.listener.feed.template

import android.view.View
import com.dn.sdk.bean.AdRequest
import com.dn.sdk.utils.AdLoggerUtils

/**
 * 信息流模板日志打印
 *
 * @author XuShuai
 * @version v1.0
 * @date 2021/12/7 17:49
 */
class LoggerFeedTemplateListenerProxy(
    private val adRequest: AdRequest,
    private val listener: IAdFeedTemplateListener?
) : IAdFeedTemplateListener {


    override fun onAdStartLoad() {
        AdLoggerUtils.d(AdLoggerUtils.createMsg(adRequest, "NativeTemplate onAdStartLoad()"))
        listener?.onAdStartLoad()
    }

    override fun onAdStatus(code: Int, any: Any?) {
        AdLoggerUtils.d(AdLoggerUtils.createMsg(adRequest, "NativeTemplate onAdStatus($code,$any)"))
        listener?.onAdStatus(code, any)
    }

    override fun onAdLoad(views: MutableList<View>) {
        AdLoggerUtils.d(AdLoggerUtils.createMsg(adRequest, "NativeTemplate onAdLoad($views)"))
        listener?.onAdLoad(views)
    }

    override fun onAdShow() {
        AdLoggerUtils.d(AdLoggerUtils.createMsg(adRequest, "NativeTemplate onAdShow()"))
        listener?.onAdShow()
    }

    override fun onAdExposure() {
        AdLoggerUtils.d(AdLoggerUtils.createMsg(adRequest, "NativeTemplate onAdExposure()"))
        listener?.onAdExposure()
    }

    override fun onAdClicked() {
        AdLoggerUtils.d(AdLoggerUtils.createMsg(adRequest, "NativeTemplate onAdClicked()"))
        listener?.onAdClicked()
    }

    override fun onAdClose() {
        AdLoggerUtils.d(AdLoggerUtils.createMsg(adRequest, "NativeTemplate onAdClose()"))
        listener?.onAdClose()
    }

    override fun onAdError(code: Int, errorMsg: String?) {
        AdLoggerUtils.d(AdLoggerUtils.createMsg(adRequest, "NativeTemplate onAdError($code,$errorMsg)"))
        listener?.onAdError(code, errorMsg)
    }
}