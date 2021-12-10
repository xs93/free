package com.donews.main.utils

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import com.dn.sdk.utils.AdLoggerUtils
import com.donews.base.base.AppManager
import com.donews.common.base.MvvmBaseLiveDataActivity
import com.donews.main.dialog.HotStartDialog
import com.orhanobut.logger.Logger

/**
 *
 *
 * @author XuShuai
 * @version v1.0
 * @date 2021/12/9 10:10
 */
object HotStartCacheUtils {

    private var mHotStartDialog: HotStartDialog? = null

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

    fun loadAd() {
        if (mHotStartDialog != null && mHotStartDialog!!.isAdded) {
            mHotStartDialog!!.preloadFirstAd()
        } else {
            clear()
        }
    }

    fun showAd() {
        if (mHotStartDialog == null || mHotStartDialog?.dialog == null || mHotStartDialog?.dialog?.isShowing == true) {
            return
        }
        mHotStartDialog!!.showAd()
    }

    fun dismiss() {
        if (mHotStartDialog == null) {
            return
        }
        mHotStartDialog!!.dismissAllowingStateLoss()
    }

    fun clear() {
        mHotStartDialog = null
    }

    fun checkActivity(activity: Activity) {
        val name: String = activity::class.java.name
        if (name.equals("com.donews.notify.launcher.NotifyActivity", true)) {
            activity.finish()
        }
        if (name.equals("com.donews.notify.launcher.NotifyActionActivity", true)) {
            activity.finish()
        }
        if (name.equals("com.donews.keepalive.DazzleActivity", true)) {
            activity.finish()
        }
    }
}