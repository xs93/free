package com.donews.module_shareui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.ScreenUtils
import com.donews.middle.mainShare.bus.ShareClickNotifyEvent
import com.donews.share.ShareItem
import com.donews.share.ShareManager
import com.lxj.xpopup.core.PositionPopupView
import org.greenrobot.eventbus.EventBus

/**
 *  make in st
 *  on 2022/5/10 09:55
 */
class ShareUIBottomPopup(context:Context): PositionPopupView(context) {

    override fun getImplLayoutId() = R.layout.shareui_bottom_popup

    private val mHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        findViewById<View>(R.id.tv_wechat).setOnClickListener { v: View? ->
            mHandler.removeCallbacksAndMessages(null)
            mHandler.postDelayed({
                EventBus.getDefault().post(ShareClickNotifyEvent())
            },3000L)
            val shareManager = ShareManager()
            val shareItem = ShareItem()
            shareItem.title = "送你88元新人红包，立即领取>>"
            shareItem.content = "上快乐派边看视频边赚钱，看得多赚得多，动动手指轻松赚取零花钱！"
            shareItem.type = ShareItem.TYPE_H5
            shareItem.webUrl = "https://recharge-web.xg.tagtic.cn/qbn/index.html#/download"
            shareManager.share(
                ShareManager.SHARE_COMMAND_WX,
                shareItem,
                context as FragmentActivity
            )
            dismiss()
        }
        findViewById<View>(R.id.tv_moment).setOnClickListener { v: View? ->
            mHandler.removeCallbacksAndMessages(null)
            mHandler.postDelayed({
                EventBus.getDefault().post(ShareClickNotifyEvent())
            },3000L)
            val shareManager = ShareManager()
            val shareItem = ShareItem()
            shareItem.title = "送你88元新人红包，立即领取>>"
            shareItem.content = "上快乐派边看视频边赚钱，看得多赚得多，动动手指轻松赚取零花钱！"
            shareItem.type = ShareItem.TYPE_H5
            shareItem.webUrl = "https://recharge-web.xg.tagtic.cn/qbn/index.html#/download"
            shareManager.share(
                ShareManager.SHARE_COMMAND_WX_FRIEND,
                shareItem,
                context as FragmentActivity
            )
            dismiss()
        }
        findViewById<View>(R.id.rootView).setOnClickListener { v: View? -> dismiss() }
    }

    override fun getPopupWidth() =  ScreenUtils.getScreenWidth()

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
    }

}