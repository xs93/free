package com.dn.sdk.count

import com.dn.sdk.bean.AdRequest
import com.dn.sdk.bean.AdType
import com.dn.sdk.loader.SdkType
import com.dn.sdk.manager.sdk.AdSdkManager
import com.dn.sdk.utils.SdkLogUtils
import com.donews.network.down.MD5Util
import com.donews.utilslibrary.analysis.AnalysisHelp.onEvent

class CountTrackImpl(private val adRequest: AdRequest) : ITrack {

    private val adType: AdType = adRequest.mAdType
    private val sdkType: SdkType = adRequest.mPlatform.getLoader().getSdkType()
    private val requestId: String

    init {
        val key: String = AdSdkManager.getContext().packageName + System.currentTimeMillis()
        requestId = MD5Util.getFileMD5(key.toByteArray())

    }

    override fun onAdStartLoad() {
        event(if (adRequest.mAdPreload) AnalysisEvent.AD_LOADING else AnalysisEvent.AD_ACTIVITY)
    }

    override fun onAdClick() {
        event(AnalysisEvent.AD_CLICK)
    }

    override fun onAdShow() {
        event(AnalysisEvent.AD_SHOW)
    }

    override fun onAdClose() {
        event(AnalysisEvent.AD_CLOSE)
    }

    override fun onRewardVerify(verify: Boolean) {
        event(AnalysisEvent.AD_REWARD_VERIFY, verify)
    }

    override fun onVideoComplete() {
        event(AnalysisEvent.AD_COMPLETE)
    }

    private fun event(eventName: String) {
        val msg = "EventName:$eventName , sdk:${sdkType.msg} , adType: ${adType.msg},adId:${adRequest.mAdId}"
        SdkLogUtils.i(SdkLogUtils.TAG, msg)

        onEvent(AdSdkManager.getContext(), eventName, sdkType.msg, adType.msg, adRequest.mAdId, requestId)
    }

    private fun event(eventName: String, verify: Boolean) {
        val msg = "EventName:$eventName , sdk:${sdkType.msg} , adType: ${adType.msg},adId:${adRequest.mAdId}"
        SdkLogUtils.i(SdkLogUtils.TAG, msg)
        onEvent(
            AdSdkManager.getContext(),
            eventName,
            sdkType.msg,
            adType.msg,
            adRequest.mAdId,
            requestId,
            verify.toString()
        )
    }
}