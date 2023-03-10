package com.donews.middle.utils

import android.app.Activity
import com.dn.events.ad.HotStartEvent
import com.donews.base.base.AppManager
import com.donews.common.base.MvvmBaseLiveDataActivity
import com.donews.middle.BuildConfig
import com.donews.middle.dialog.HotStartDialog
import org.greenrobot.eventbus.EventBus

/**
 *
 *
 * @author XuShuai
 * @version v1.0
 * @date 2021/12/9 10:10
 */
object HotStartCacheUtils {

    private var mHotStartDialog: HotStartDialog? = null

    /**
     * 是否开启debug的调试模式(开启之后。可以直接点击打开通知)
     */
    var isDebugNotify = false && BuildConfig.DEBUG

    fun addHotStartAdDialog() {
        val activity = AppManager.getInstance().topActivity
        if (activity !is MvvmBaseLiveDataActivity<*, *>) {
            return
        }

        if (mHotStartDialog != null && mHotStartDialog?.dialog != null && mHotStartDialog?.dialog?.isShowing == true) {
            return
        }
        mHotStartDialog = HotStartDialog.newInstance()
        mHotStartDialog!!.showAllowingStateLoss(activity.supportFragmentManager, "HotStartDialog")
    }

    fun isShowing(): Boolean {
        if (mHotStartDialog == null || mHotStartDialog?.dialog == null) {
            return false;
        }

        return mHotStartDialog!!.dialog!!.isShowing;
    }

    fun loadAd() {
        // 屏蔽预加载广告-by aaron.din
        /*if (mHotStartDialog != null && mHotStartDialog!!.isAdded) {
            mHotStartDialog!!.preloadFirstAd()
        } else {
            clear()
        }*/
    }

    fun showAd() {
        if (mHotStartDialog == null || mHotStartDialog?.dialog == null || mHotStartDialog?.dialog?.isShowing == true) {
            return
        }
        mHotStartDialog!!.showAd()
        EventBus.getDefault().post(HotStartEvent(true))
    }

    fun dismiss() {
        if (mHotStartDialog == null) {
            return
        }
        try {
            mHotStartDialog!!.dismissAllowingStateLoss()
        } catch (e: Exception) {
        }
    }

    fun clear() {
        mHotStartDialog = null
    }

    fun checkActivity(activity: Activity) {
        val name: String = activity::class.java.name
        if (!isDebugNotify) {
            if (name.equals("com.donews.notify.launcher.NotifyActivity", true)) {
                activity.finish()
            }
        }
        if (name.equals("com.donews.notify.launcher.NotifyActionActivity", true)) {
            activity.finish()
        }
        if (name.equals("com.donews.keepalive.DazzleActivity", true)) {
            activity.finish()
        }
    }

    /**
     * 扣除通知页面。防止通知页面被关闭
     * @param activity Activity
     */
    fun checkNotifyActivity(activity: Activity) {
        val name: String = activity::class.java.name
        if (name.equals("com.donews.notify.launcher.NotifyActionActivity", true)) {
            activity.finish()
        }
        if (name.equals("com.donews.keepalive.DazzleActivity", true)) {
            activity.finish()
        }
    }
}