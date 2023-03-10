package com.donews.task

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.dn.sdk.AdCustomError
import com.dn.sdk.listener.interstitialfull.SimpleInterstitialFullListener
import com.dn.sdk.listener.rewardvideo.SimpleRewardVideoListener
import com.donews.base.utils.ToastUtil
import com.donews.base.utils.glide.GlideUtils
import com.donews.base.utils.glide.RoundCornersTransform
import com.donews.common.base.MvvmLazyLiveDataFragment
import com.donews.common.router.RouterActivityPath
import com.donews.common.router.RouterFragmentPath
import com.donews.middle.IMainParams
import com.donews.middle.adutils.InterstitialFullAd.showAd
import com.donews.middle.adutils.RewardVideoAd
import com.donews.middle.adutils.adcontrol.AdControlManager.adControlBean
import com.donews.middle.bean.LotteryEventUnlockBean
import com.donews.middle.mainShare.bean.BubbleBean
import com.donews.middle.mainShare.bean.TaskBubbleInfo
import com.donews.middle.mainShare.bus.CollectStartNewCardEvent
import com.donews.middle.mainShare.bus.ShareClickNotifyEvent
import com.donews.middle.mainShare.vm.MainShareViewModel
import com.donews.middle.views.TaskView
import com.donews.module_shareui.ShareUIBottomPopup
import com.donews.task.bean.BubbleReceiveInfo
import com.donews.task.bean.TaskConfigInfo
import com.donews.task.databinding.TaskFragmentBinding
import com.donews.middle.mainShare.extend.setOnClickListener
import com.donews.task.util.*
import com.donews.task.view.ColdDownTimerView
import com.donews.task.view.explosion.ExplodeParticleFactory
import com.donews.task.view.explosion.ExplosionField
import com.donews.task.vm.TaskViewModel
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.droidsonroids.gif.GifDrawable
import com.donews.middle.bean.globle.TurntableBean.ItemsDTO
import com.donews.middle.centralDeploy.OutherSwitchConfig
import com.donews.middle.events.TaskReportEvent
import com.donews.middle.utils.ActivityGuideMaskUtil
import com.donews.middle.viewmodel.BaseMiddleViewModel
import com.donews.task.bean.TaskCenterInfo
import com.donews.utilslibrary.utils.DensityUtils
import com.donews.yfsdk.moniter.PageMonitor
import com.donews.yfsdk.monitor.InterstitialFullAdCheck
import com.donews.yfsdk.monitor.PageMoniterCheck.showAdSuccess
import com.orhanobut.logger.Logger


/**
 *  make in st
 *  on 2022/5/7 10:37
 *  ????????????
 */
@Route(path = RouterFragmentPath.Task.PAGER_TASK)
class TaskFragment : MvvmLazyLiveDataFragment<TaskFragmentBinding, TaskViewModel>() {

    private var mContext: Context? = null

    override fun getLayoutId() = R.layout.task_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onFragmentFirstVisible() {
        super.onFragmentFirstVisible()
        activity?.apply {
            if (!ActivityGuideMaskUtil.getGuideShowRecord(
                    this, R.id.accessibility_custom_action_1
                )
            ) {
                //??????????????????????????????????????????????????????
                ActivityGuideMaskUtil.saveGuideShowRecord(
                    this, R.id.accessibility_custom_action_1, true
                )
            }
        }
        loadUserAssets()
        loadTaskBubbles()
    }

    private fun setBinding() {
        mDataBinding?.taskModel = mViewModel
    }

    private fun initView() {
        mContext = this.context
        setBinding()
        initEventBus()
        initClick()
        normalStart()
    }

    private fun normalStart() {
        initTaskControl()
        initShareViewModel()
        initLiveData()
        initMainGif()
        initBubble()
        initColdTimerView()
        initTaskView()
        startBubbleAnimation()
    }

    //region ??????????????????
    private fun initLiveData() {
        initUserAssets()
        initTaskBubbles()
        initBubbleReceive()
        initAdReport()
        initExchange()
        initOtherAssets()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PageMonitor().attach(this, object : PageMonitor.PageListener {
            override fun checkShowAd(): AdCustomError {
                return if (adControlBean.useInstlFullWhenSwitch) {
                    InterstitialFullAdCheck.isEnable()
                } else {
                    InterstitialFullAdCheck.isEnable()
                }
            }

            override fun getIdleTime(): Int {
                return adControlBean.noOperationDuration
            }

            override fun showAd() {
                val activity: Activity = requireActivity()
                if (activity.isFinishing) {
                    return
                }
                if (activity is IMainParams &&
                    !OutherSwitchConfig.Ins().checkMainTabInterstitial(
                        (activity as IMainParams).getThisFragmentCurrentPos(this@TaskFragment)
                    )
                ) {
                    //??????????????????Tab?????????????????????
                    return
                }
                if (!adControlBean.useInstlFullWhenSwitch) {
                    showAd(activity, object : SimpleInterstitialFullListener() {
                        override fun onAdError(code: Int, errprMsg: String) {
                            super.onAdError(code, errprMsg)
                            Logger.d("?????????????????????????????????---- code = \$code ,msg =  \$errprMsg ")
                        }

                        override fun onAdClose() {
                            super.onAdClose()
                            showAdSuccess("mine_fragment")
                        }
                    })
                } else {
                    showAd(activity, object : SimpleInterstitialFullListener() {
                        override fun onAdError(errorCode: Int, errprMsg: String) {
                            super.onAdError(errorCode, errprMsg)
                            Logger.d("????????????????????????????????????---- code = \$errorCode ,msg =  \$errprMsg ")
                        }

                        override fun onAdClose() {
                            super.onAdClose()
                            showAdSuccess("mine_fragment")
                        }
                    })
                }
            }
        })
    }

    //???????????????????????????????????????????????????
    private fun initOtherAssets() {
        BaseMiddleViewModel.getBaseViewModel().mine2JBCount.observe(viewLifecycleOwner, {
            it?.let {
                mViewModel?.goldCoinNum?.set(it.toString())
            }
        })
        BaseMiddleViewModel.getBaseViewModel().mine2JFCount.observe(viewLifecycleOwner, {
            it?.let {
                mViewModel?.activityNum?.set(it.toString())
            }
        })
    }

    private fun initUserAssets() {
        mShareVideModel.userAssets.observe(viewLifecycleOwner, {
            it?.let {
                mViewModel?.goldCoinNum?.set(it.coin.toString())
                mViewModel?.activityNum?.set(it.active.toString())
            }
        })
    }

    //?????????????????????????????????
    private fun loadUserAssets() {
        mShareVideModel.requestUserAssets()
    }

    private lateinit var taskBubbleBean: TaskBubbleInfo
    private var bubbleIsLeftOrRight = true//?????????????????????

    private fun initTaskBubbles() {
        mShareVideModel.taskBubbles.observe(viewLifecycleOwner, {
            it?.let {
                taskBubbleBean = it
                handleTaskBubbles()
            }
        })
    }

    //????????????????????????
    private fun loadTaskBubbles() {
        mShareVideModel.requestTaskBubbles()
    }

    //??????????????????????????????
    private var mCurWhichBubbleType = ""

    private fun initBubbleReceive() {
        mViewModel.bubbleReceive.observe(viewLifecycleOwner, {
            it?.let {
                handleBubblesReceive(it)
            }
        })
    }

    private fun loadBubbleReceive(mId: Int, mType: String) {
        mViewModel?.requestBubbleReceive(mId, mType)
    }

    //???????????????,??????,??????,??????????????????,???????????????????????????
    private fun initAdReport() {
        mShareVideModel.adReport.observe(viewLifecycleOwner, {
            it?.let {
                loadTaskBubbles()
            }
        })
    }

    private fun loadAdReport(mId: Int, mType: String) {
        mShareVideModel.requestAdReport(mId, mType)
    }

    //???????????????
    private fun initExchange() {
        mViewModel.exchange.observe(viewLifecycleOwner, {
            it?.let {
                loadUserAssets()
                ToastUtil.show(mContext, "????????????!")
            }
        })
    }

    private fun loadExchange(exchangeActiveNum: Int) {
        mViewModel?.requestExchange(exchangeActiveNum)
    }
    //endregion

    //region ??????????????????????????????
    companion object {
        //???????????? 0 ????????? 1 ??????????????? 2 ?????????
        private const val BUBBLE_NO_FINISH = 0
        private const val BUBBLE_NO_RECEIVE = 1
        private const val BUBBLE_HAVE_FINISH = 2

        //???????????????????????????????????????????????????????????????????????????
        private const val TURNTABLE = "turntable"
        private const val SIGN = "sign"
        private const val LOTTERY = "lottery"
        private const val SHARE = "share"
        private const val COLLECT = "collect"
        private const val VIDEO = "video"
        private const val GIFT_BOX = "giftbox"
        private const val NONE = "none"
    }

    //??????????????????????????????(????????????)
    private fun handleTaskBubbles() {
        var canReceiveBubbleNum = 0//??????????????????
        for (index in taskBubbleBean.list.indices) {
            bubbleIsShow(index)
            if (taskBubbleBean.list[index].type != GIFT_BOX
                && taskBubbleBean.list[index].status == BUBBLE_NO_RECEIVE
            ) {
                canReceiveBubbleNum++
            }
        }
        if (canReceiveBubbleNum > 0) startFingerAnimation() else cancelFingerAnimation()
    }

    private var taskBubbleSignBean: BubbleBean? = null
    private var taskBubbleLuckPanBean: BubbleBean? = null
    private var taskBubbleCollectBean: BubbleBean? = null
    private var taskBubbleShareBean: BubbleBean? = null
    private var taskBubbleLuckDrawBean: BubbleBean? = null
    private var taskBubbleVideoBean: BubbleBean? = null
    private var taskBubbleBoxBean: BubbleBean? = null

    @SuppressLint("SetTextI18n")
    private fun bubbleIsShow(index: Int) {
        exchangeActiveNum = taskBubbleBean.ex?.active ?: 15
        when (taskBubbleBean.list[index].type) {
            TURNTABLE -> {
                taskBubbleLuckPanBean = taskBubbleBean.list[index]
                when (taskBubbleLuckPanBean?.status) {
                    BUBBLE_NO_FINISH -> {
                        mDataBinding?.iconLuckPanBubble?.alpha = 0.45f
                        mDataBinding?.iconLuckPanTv?.alpha = 0.45f
                        mDataBinding?.iconLuckPanTv?.text = "??????"
                    }
                    BUBBLE_NO_RECEIVE -> {
                        mDataBinding?.iconLuckPanBubble?.alpha = 1f
                        mDataBinding?.iconLuckPanTv?.alpha = 1f
                        mDataBinding?.iconLuckPanTv?.text = "?????????"
                    }
                    BUBBLE_HAVE_FINISH -> {
                        mDataBinding?.iconLuckPanBubble?.alpha = 0f
                        mDataBinding?.iconLuckPanTv?.alpha = 0f
                    }
                }
            }
            SIGN -> {
                taskBubbleSignBean = taskBubbleBean.list[index]
                when (taskBubbleSignBean?.status) {
                    BUBBLE_NO_FINISH -> {
                        mDataBinding?.iconSignBubble?.alpha = 0.45f
                        mDataBinding?.iconSignTv?.alpha = 0.45f
                        mDataBinding?.iconSignTv?.text = "??????"
                    }
                    BUBBLE_NO_RECEIVE -> {
                        mDataBinding?.iconSignBubble?.alpha = 1f
                        mDataBinding?.iconSignTv?.alpha = 1f
                        mDataBinding?.iconSignTv?.text = "?????????"
                    }
                    BUBBLE_HAVE_FINISH -> {
                        mDataBinding?.iconSignBubble?.alpha = 0f
                        mDataBinding?.iconSignTv?.alpha = 0f
                    }
                }
            }
            LOTTERY -> {
                taskBubbleLuckDrawBean = taskBubbleBean.list[index]
                when (taskBubbleLuckDrawBean?.status) {
                    BUBBLE_NO_FINISH -> {
                        mDataBinding?.iconLuckDrawBubble?.alpha = 0.45f
                        mDataBinding?.iconLuckDrawTv?.alpha = 0.45f
                        mDataBinding?.iconLuckDrawTv?.text = "??????"
                    }
                    BUBBLE_NO_RECEIVE -> {
                        mDataBinding?.iconLuckDrawBubble?.alpha = 1f
                        mDataBinding?.iconLuckDrawTv?.alpha = 1f
                        mDataBinding?.iconLuckDrawTv?.text = "?????????"
                    }
                    BUBBLE_HAVE_FINISH -> {
                        mDataBinding?.iconLuckDrawBubble?.alpha = 0f
                        mDataBinding?.iconLuckDrawTv?.alpha = 0f
                    }
                }
            }
            SHARE -> {
                taskBubbleShareBean = taskBubbleBean.list[index]
                when (taskBubbleShareBean?.status) {
                    BUBBLE_NO_FINISH -> {
                        mDataBinding?.iconShareBubble?.alpha = 0.45f
                        mDataBinding?.shareTv?.alpha = 0.45f
                        mDataBinding?.shareTv?.text = "??????"
                    }
                    BUBBLE_NO_RECEIVE -> {
                        mDataBinding?.iconShareBubble?.alpha = 1f
                        mDataBinding?.shareTv?.alpha = 1f
                        mDataBinding?.shareTv?.text = "?????????"
                    }
                    BUBBLE_HAVE_FINISH -> {
                        mDataBinding?.iconShareBubble?.alpha = 0f
                        mDataBinding?.shareTv?.alpha = 0f
                    }
                }
            }
            COLLECT -> {
                taskBubbleCollectBean = taskBubbleBean.list[index]
                when (taskBubbleCollectBean?.status) {
                    BUBBLE_NO_FINISH -> {
                        mDataBinding?.iconCollectBubble?.alpha = 0.45f
                        mDataBinding?.iconCollectTv?.alpha = 0.45f
                        mDataBinding?.iconCollectTv?.text = "??????"
                    }
                    BUBBLE_NO_RECEIVE -> {
                        mDataBinding?.iconCollectBubble?.alpha = 1f
                        mDataBinding?.iconCollectTv?.alpha = 1f
                        mDataBinding?.iconCollectTv?.text = "?????????"
                    }
                    BUBBLE_HAVE_FINISH -> {
                        mDataBinding?.iconCollectBubble?.alpha = 0f
                        mDataBinding?.iconCollectTv?.alpha = 0f
                    }
                }
            }
            VIDEO -> {
                taskBubbleVideoBean = taskBubbleBean.list[index]
                when (taskBubbleVideoBean?.status) {
                    BUBBLE_NO_FINISH -> {
                        if (taskBubbleVideoBean?.cd ?: 0 > 0) {
                            //?????????cd=180,status=0
                            mDataBinding?.coldDownTimer?.alpha = 0.45f
                            mDataBinding?.countDownTimeTv?.alpha = 0.6f
                            mDataBinding?.seeAdTv?.alpha = 0.45f
                            mDataBinding?.seeAdTv?.text = "?????????(${taskBubbleVideoBean?.done ?: 0}/3)"
                            mDataBinding?.coldDownTimer?.apply {
                                setCurCountTime(taskBubbleVideoBean?.cd ?: 0)
                                startCountdown()
                            }
                        } else {
                            //???????????????cd=0,status=0
                            mDataBinding?.coldDownTimer?.alpha = 0.45f
                            mDataBinding?.countDownTimeTv?.alpha = 0.6f
                            mDataBinding?.seeAdTv?.alpha = 0.45f
                            mDataBinding?.seeAdTv?.text = "?????????(${taskBubbleVideoBean?.done ?: 0}/3)"
                        }
                    }
                    BUBBLE_NO_RECEIVE -> {
                        mDataBinding?.coldDownTimer?.alpha = 1f
                        mDataBinding?.countDownTimeTv?.alpha = 1f
                        mDataBinding?.seeAdTv?.text = "?????????(${taskBubbleVideoBean?.done ?: 0}/3)"
                        mDataBinding?.seeAdTv?.alpha = 1f
                    }
                    BUBBLE_HAVE_FINISH -> {
                        mDataBinding?.coldDownTimer?.alpha = 0.45f
                        mDataBinding?.countDownTimeTv?.alpha = 0.6f
                        mDataBinding?.seeAdTv?.alpha = 0.45f
                        mDataBinding?.seeAdTv?.text = "????????????"
                    }
                }
            }
            GIFT_BOX -> {
                taskBubbleBoxBean = taskBubbleBean.list[index]
                boxMaxOpenNum = taskBubbleBoxBean?.total ?: 5
                initBox()
            }
        }
    }

    //??????????????????????????????
    private fun handleBubblesReceive(it: BubbleReceiveInfo) {
        when (mCurWhichBubbleType) {
            SIGN -> {
                makeBubbleExplosion(mDataBinding?.iconSignBubble as View)
                makeBubbleExplosion(mDataBinding?.iconSignTv as View)
                //????????????????????????
                loadUserAssets()
                loadTaskBubbles()
            }
            COLLECT -> {
                bubbleIsLeftOrRight = true
                makeBubbleExplosion(mDataBinding?.iconCollectBubble as View)
                makeBubbleExplosion(mDataBinding?.iconCollectTv as View)
                startCoinGif()
                loadUserAssets()
                loadTaskBubbles()
            }
            LOTTERY -> {
                bubbleIsLeftOrRight = true
                makeBubbleExplosion(mDataBinding?.iconLuckDrawBubble as View)
                makeBubbleExplosion(mDataBinding?.iconLuckDrawTv as View)
                startCoinGif()
                loadUserAssets()
                loadTaskBubbles()
            }
            TURNTABLE -> {
                bubbleIsLeftOrRight = false
                makeBubbleExplosion(mDataBinding?.iconLuckPanBubble as View)
                makeBubbleExplosion(mDataBinding?.iconLuckPanTv as View)
                startCoinGif()
                loadUserAssets()
                loadTaskBubbles()
            }
            SHARE -> {
                bubbleIsLeftOrRight = false
                makeBubbleExplosion(mDataBinding?.iconShareBubble as View)
                makeBubbleExplosion(mDataBinding?.shareTv as View)
                startCoinGif()
                loadUserAssets()
                loadTaskBubbles()
            }
            VIDEO -> {
                bubbleIsLeftOrRight = true
                startCoinGif()
                loadUserAssets()
                loadTaskBubbles()
            }
            GIFT_BOX -> {
                if (activity != null) {
                    DialogUtil.showBoxDialog(requireActivity(), it.active > 0) {
                        loadUserAssets()
                        loadTaskBubbles()
                    }
                }
            }
            NONE -> {
                bubbleIsLeftOrRight = true
                for (index in taskBubbleBean.list.indices) {
                    if (taskBubbleBean.list[index].status == BUBBLE_NO_RECEIVE) {
                        when (taskBubbleBean.list[index].type) {
                            SIGN -> {
                                makeBubbleExplosion(mDataBinding?.iconSignBubble as View)
                                makeBubbleExplosion(mDataBinding?.iconSignTv as View)
                            }
                            COLLECT -> {
                                makeBubbleExplosion(mDataBinding?.iconCollectBubble as View)
                                makeBubbleExplosion(mDataBinding?.iconCollectTv as View)
                            }
                            LOTTERY -> {
                                makeBubbleExplosion(mDataBinding?.iconLuckDrawBubble as View)
                                makeBubbleExplosion(mDataBinding?.iconLuckDrawTv as View)
                            }
                            TURNTABLE -> {
                                makeBubbleExplosion(mDataBinding?.iconLuckPanBubble as View)
                                makeBubbleExplosion(mDataBinding?.iconLuckPanTv as View)
                            }
                            SHARE -> {
                                makeBubbleExplosion(mDataBinding?.iconShareBubble as View)
                                makeBubbleExplosion(mDataBinding?.shareTv as View)
                            }
                            VIDEO -> {
                            }
                            GIFT_BOX -> {
                            }
                        }
                    }
                }
                startCoinGif()
                loadUserAssets()
                loadTaskBubbles()
            }
        }
    }
    //endregion

    //region ???????????????????????????
    //????????????????????????10s?????????
    private var mMaxCountTime = 180

    @SuppressLint("SetTextI18n")
    private fun initColdTimerView() {
        mDataBinding?.coldDownTimer?.apply {
            setCountTime(mMaxCountTime)
            setOnCountDownTimeListener(object : ColdDownTimerView.CountDownTimeListener {
                override fun getCurCountDownTime(time: Int) {
                    if (time > 0) {
                        mDataBinding?.countDownTimeTv?.text =
                            TimeUtils.stringForTimeNoHour(time * 1000.toLong())
                        mDataBinding?.countDownTimeTv?.visibility = View.VISIBLE
                    } else {
                        mDataBinding?.countDownTimeTv?.visibility = View.GONE
                    }
                }

                override fun countDownFinish() {
                    mDataBinding?.countDownTimeTv?.visibility = View.GONE
                    //??????????????????,????????????????????????
                    loadTaskBubbles()
                }

            })
        }
    }
    //endregion

    //region ????????????????????????????????????????????????????????????
    private fun initTaskView() {
        mDataBinding?.taskBgRunning?.refreshYyw(TaskView.Place_Task)
        GlideUtils.loadImageRoundCorner(
            context, taskControlConfig?.img?.luckPanImg, mDataBinding.taskBgLuckPan,
            RoundCornersTransform(
                DensityUtils.dip2px(15f).toFloat(),
                DensityUtils.dip2px(15f).toFloat(),
                DensityUtils.dip2px(15f).toFloat(),
                DensityUtils.dip2px(15f).toFloat()
            )
        )
        GlideUtils.loadImageRoundCorner(
            context, taskControlConfig?.img?.luckCollectImg, mDataBinding.taskBgCollect,
            RoundCornersTransform(
                DensityUtils.dip2px(15f).toFloat(),
                DensityUtils.dip2px(15f).toFloat(),
                DensityUtils.dip2px(15f).toFloat(),
                DensityUtils.dip2px(15f).toFloat()
            )
        )
    }
    //endregion

    //region ??????????????????
    private fun initClick() {
        setOnClickListener(
            mDataBinding?.ruleClick,
            mDataBinding?.activityTxBtn,
            mDataBinding?.iconCanGet,
            mDataBinding?.iconBox,
            mDataBinding?.coldDownTimer,
            mDataBinding?.iconSignBubble,
            mDataBinding?.iconSignTv,//????????????
            mDataBinding?.iconLuckPanBubble,
            mDataBinding?.iconLuckPanTv,
            mDataBinding?.taskBgLuckPan,//????????????
            mDataBinding?.iconCollectBubble,
            mDataBinding?.iconCollectTv,
            mDataBinding?.taskBgCollect,//????????????
            mDataBinding?.iconShareBubble,
            mDataBinding?.shareTv,//????????????
            mDataBinding?.iconLuckDrawBubble,
            mDataBinding?.iconLuckDrawTv,//????????????
            mDataBinding?.iconBtn,
            mDataBinding?.fingerAnimation
        ) {
            when (this) {
                mDataBinding?.ruleClick -> {
                    if (activity != null) {
                        DialogUtil.showRuleDialog(requireActivity())
                    }
                }
                mDataBinding?.activityTxBtn -> {
                    if (activity != null) {
                        DialogUtil.showExchangeDialog(requireActivity()) {
                            loadExchange(exchangeActiveNum)
                        }
                    }
                }
                mDataBinding?.iconCanGet, mDataBinding?.iconBox -> {
                    //????????????
                    clickBox()
                }
                mDataBinding?.coldDownTimer -> {
                    //?????????????????????
                    clickAdVideo()
                }
                mDataBinding?.iconSignBubble, mDataBinding?.iconSignTv -> {
                    //??????????????????
                    clickSign()
                }
                mDataBinding?.iconLuckPanBubble, mDataBinding?.iconLuckPanTv -> {
                    //????????????????????????
                    clickLuckPan()
                }
                mDataBinding?.taskBgLuckPan -> {
                    //????????????????????????
                    ARouter.getInstance()
                        .build(RouterActivityPath.Turntable.TURNTABLE_ACTIVITY)
                        .navigation()
                }
                mDataBinding?.iconCollectBubble, mDataBinding?.iconCollectTv -> {
                    //????????????????????????
                    clickCollect()
                }
                mDataBinding?.taskBgCollect -> {
                    //????????????????????????
                    ARouter.getInstance()
                        .build(RouterFragmentPath.Collect.PAGER_COLLECT)
                        .navigation()
                }
                mDataBinding?.iconShareBubble, mDataBinding?.shareTv -> {
                    //??????????????????
                    clickShare()
                }
                mDataBinding?.iconLuckDrawBubble, mDataBinding?.iconLuckDrawTv -> {
                    //??????????????????
                    clickLottery()
                }
                mDataBinding?.iconBtn, mDataBinding?.fingerAnimation -> {
                    //??????????????????
                    clickAllBubble()
                }
            }
        }
    }

    /**
     * ????????????????????????
     */
    //????????????????????????
    private var isBoxSortClick = false//????????????????????????
    private fun clickBox() {
        if (isBoxSortClick) return
        isBoxSortClick = true
        mHandler.postDelayed({
            isBoxSortClick = false
        }, 5000L)
        when (taskBubbleBoxBean?.status) {
            BUBBLE_NO_FINISH -> {
                ToastUtil.show(mContext, "???????????????????????????")
            }
            BUBBLE_NO_RECEIVE -> {
                if (activity != null) {
                    RewardVideoAd.loadRewardVideoAd(
                        requireActivity(),
                        object : SimpleRewardVideoListener() {
                            override fun onAdError(code: Int, errorMsg: String?) {
                                super.onAdError(code, errorMsg)
                                Log.i("adSee-->", "-onAdError->code:${code},errorMsg:${errorMsg}")
                                ToastUtil.show(mContext, "?????????????????????????????????")
                            }

                            override fun onAdShow() {
                                super.onAdShow()
                                isBoxSortClick = false
                            }

                            override fun onAdClose() {
                                super.onAdClose()
                                Log.i("adSee-->", "-onAdClose->")
                                mCurWhichBubbleType = GIFT_BOX
                                //??????????????????????????????,????????????
                                loadBubbleReceive(
                                    taskBubbleBoxBean?.id ?: MainShareViewModel.ID_GIFT_BOX,
                                    taskBubbleBoxBean?.type ?: GIFT_BOX
                                )
                            }
                        },
                        false
                    )
                }
            }
            BUBBLE_HAVE_FINISH -> {
                ToastUtil.show(mContext, "????????????????????????,????????????!")
            }
        }
    }

    private var isAdVideoSortClick = false//???????????????????????????????????????

    //?????????????????????????????????
    private fun clickAdVideo() {
        Log.i("adSee-->", "--status->${taskBubbleVideoBean?.status}")
        if (isAdVideoSortClick) return
        isAdVideoSortClick = true
        mHandler.postDelayed({
            isAdVideoSortClick = false
        }, 5000L)
        when (taskBubbleVideoBean?.status) {
            BUBBLE_NO_FINISH -> {
                Log.i("adSee-->", "--cd->${taskBubbleVideoBean?.cd}")
                //????????????????????????,cd=0????????????
                if (taskBubbleVideoBean?.cd ?: 0 > 0) {
                    ToastUtil.show(mContext, "?????????")
                } else {
                    //???????????????cd=0,????????????,???????????????
                    if (activity != null) {
                        RewardVideoAd.loadRewardVideoAd(
                            requireActivity(),
                            object : SimpleRewardVideoListener() {
                                override fun onAdError(code: Int, errorMsg: String?) {
                                    super.onAdError(code, errorMsg)
                                    Log.i(
                                        "adSee-->",
                                        "-onAdError->code:${code},errorMsg:${errorMsg}"
                                    )
                                    ToastUtil.show(mContext, "?????????????????????????????????")
                                }

                                override fun onAdShow() {
                                    super.onAdShow()
                                    isAdVideoSortClick = false
                                }

                                override fun onAdClose() {
                                    super.onAdClose()
                                    Log.i("adSee-->", "-onAdClose->")
                                    loadAdReport(
                                        taskBubbleVideoBean?.id ?: MainShareViewModel.ID_VIDEO,
                                        taskBubbleVideoBean?.type ?: VIDEO
                                    )
                                }
                            },
                            false
                        )
                    }
                }
            }
            BUBBLE_NO_RECEIVE -> {
                mCurWhichBubbleType = VIDEO
                loadBubbleReceive(
                    taskBubbleVideoBean?.id ?: MainShareViewModel.ID_VIDEO,
                    taskBubbleVideoBean?.type ?: VIDEO
                )
            }
            BUBBLE_HAVE_FINISH -> {
                ToastUtil.show(mContext, "????????????????????????,????????????")
            }
        }
    }

    //????????????????????????
    private fun clickSign() {
        when (taskBubbleSignBean?.status) {
            BUBBLE_NO_FINISH -> {
                if (activity != null && activity?.supportFragmentManager != null) {
                    RouterFragmentPath.User.getSingDialog()
                        .show(activity?.supportFragmentManager!!, "SignInMineDialog")
                }
            }
            BUBBLE_NO_RECEIVE -> {
                mCurWhichBubbleType = SIGN
                loadBubbleReceive(
                    taskBubbleSignBean?.id ?: MainShareViewModel.ID_SIGN,
                    taskBubbleSignBean?.type ?: SIGN
                )
            }
        }
    }

    //????????????????????????
    private fun clickLuckPan() {
        when (taskBubbleLuckPanBean?.status) {
            BUBBLE_NO_FINISH -> {
                ARouter.getInstance()
                    .build(RouterActivityPath.Turntable.TURNTABLE_ACTIVITY)
                    .navigation()
            }
            BUBBLE_NO_RECEIVE -> {
                mCurWhichBubbleType = TURNTABLE
                loadBubbleReceive(
                    taskBubbleLuckPanBean?.id ?: MainShareViewModel.ID_TURNTABLE,
                    taskBubbleLuckPanBean?.type ?: TURNTABLE
                )
            }
        }
    }

    //????????????????????????
    private fun clickCollect() {
        when (taskBubbleCollectBean?.status) {
            BUBBLE_NO_FINISH -> {
                //?????????
                ARouter.getInstance()
                    .build(RouterFragmentPath.Collect.PAGER_COLLECT)
                    .navigation()
            }
            BUBBLE_NO_RECEIVE -> {
                mCurWhichBubbleType = COLLECT
                loadBubbleReceive(
                    taskBubbleCollectBean?.id ?: MainShareViewModel.ID_COLLECT,
                    taskBubbleCollectBean?.type ?: COLLECT
                )
            }
        }
    }

    //????????????????????????
    private fun clickShare() {
        when (taskBubbleShareBean?.status) {
            BUBBLE_NO_FINISH -> {
                if (context != null) {
                    XPopup.Builder(activity)
                        .isDestroyOnDismiss(true) //???????????????????????????????????????????????????
                        .popupAnimation(PopupAnimation.TranslateFromBottom)
                        .navigationBarColor(Color.BLACK)
                        .asCustom(ShareUIBottomPopup(requireContext()))
                        .show()
                }
            }
            BUBBLE_NO_RECEIVE -> {
                mCurWhichBubbleType = SHARE
                loadBubbleReceive(
                    taskBubbleShareBean?.id ?: MainShareViewModel.ID_SHARE,
                    taskBubbleShareBean?.type ?: SHARE
                )
            }
        }
    }

    //????????????????????????
    private fun clickLottery() {
        when (taskBubbleLuckDrawBean?.status) {
            BUBBLE_NO_FINISH -> {
                //?????????
                ARouter.getInstance().build(RouterActivityPath.Main.PAGER_MAIN)
                    .withInt("position", 1)
                    .navigation()
            }
            BUBBLE_NO_RECEIVE -> {
                mCurWhichBubbleType = LOTTERY
                loadBubbleReceive(
                    taskBubbleLuckDrawBean?.id ?: MainShareViewModel.ID_LOTTERY,
                    taskBubbleLuckDrawBean?.type ?: LOTTERY
                )
            }
        }
    }

    //????????????????????????????????????(????????????)
    private fun clickAllBubble() {
        var isHaveCanReceiveBubble = false
        for (index in taskBubbleBean.list.indices) {
            if (taskBubbleBean.list[index].type != GIFT_BOX
                && taskBubbleBean.list[index].status == BUBBLE_NO_RECEIVE
            ) {
                isHaveCanReceiveBubble = true
                break
            }
        }
        if (isHaveCanReceiveBubble) {
            mCurWhichBubbleType = NONE
            loadBubbleReceive(100, NONE)
        } else Toast.makeText(mContext, "???????????????????????????", Toast.LENGTH_SHORT).show()
    }
    //endregion

    //region ????????????
    /*????????????*/
    private var boxMaxTime = 120
    private var boxCurTime = boxMaxTime

    //?????????????????????
    private var boxMaxOpenNum = 5

    private val boxTimer = object : Runnable {
        override fun run() {
            if (boxCurTime > 0) {
                boxCurTime--
                mDataBinding?.boxTimeTv?.text = TimeUtils.stringForTimeNoHour(boxCurTime * 1000L)
                //?????????????????????????????????
                if (boxCurTime == 0) {
                    mViewModel?.isShowBoxTimeView?.set(false)
                    startBoxAnimation()
                    loadTaskBubbles()//?????????????????????????????????
                } else {
                    mHandler.postDelayed(this, 1000L)
                    cancelBoxAnimation()
                }
            }
        }
    }

    //???????????????????????????????????????
    private fun initBox() {
        if (taskBubbleBoxBean?.cd ?: 0 > 0) {
            //??????????????????
            cancelBoxAnimation()
            mViewModel?.isShowBoxTimeView?.set(true)
            mViewModel?.isShowIconCanGet?.set(false)
            boxCurTime = taskBubbleBoxBean?.cd!!
            mHandler.removeCallbacks(boxTimer)
            mHandler.postDelayed(boxTimer, 1000L)
        } else {
            when (taskBubbleBoxBean?.status) {
                BUBBLE_NO_RECEIVE -> {
                    startBoxAnimation()
                    mViewModel?.isShowBoxTimeView?.set(false)
                    mViewModel?.isShowIconCanGet?.set(true)
                }
                BUBBLE_HAVE_FINISH -> {
                    cancelBoxAnimation()
                    mViewModel?.isShowBoxTimeView?.set(true)
                    mViewModel?.isShowIconCanGet?.set(false)
                    mDataBinding?.boxTimeTv?.text = "????????????"
                }
            }
        }
    }
    //endregion

    //region gif??????
    private var gifDrawable: GifDrawable? = null

    private fun initMainGif() {
        try {
            gifDrawable = GifDrawable(mContext!!.assets, "task_gif.gif")
            mDataBinding?.taskGif?.setImageDrawable(gifDrawable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startCoinGif() {
        if (bubbleIsLeftOrRight) {
            AnimationUtil.coinGifStart(this, mDataBinding?.leftCoinGif)
        } else AnimationUtil.coinGifStart(this, mDataBinding?.rightCoinGif)
    }
    //endregion

    //region ????????????????????????
    private fun initBubble() {
        mDataBinding?.iconSignBubble?.alpha = 0f
        mDataBinding?.iconSignTv?.alpha = 0f
        mDataBinding?.iconLuckPanBubble?.alpha = 0f
        mDataBinding?.iconLuckPanTv?.alpha = 0f
        mDataBinding?.iconCollectBubble?.alpha = 0f
        mDataBinding?.iconCollectTv?.alpha = 0f
        mDataBinding?.iconShareBubble?.alpha = 0f
        mDataBinding?.shareTv?.alpha = 0f
        mDataBinding?.iconLuckDrawBubble?.alpha = 0f
        mDataBinding?.iconLuckDrawTv?.alpha = 0f
        mDataBinding?.coldDownTimer?.alpha = 0f
        mDataBinding?.countDownTimeTv?.alpha = 0f
        mDataBinding?.seeAdTv?.alpha = 0f
    }

    private fun makeBubbleExplosion(bubbleView: View) {
        ExplosionField(mContext, ExplodeParticleFactory()).apply {
            explode(bubbleView)
        }
    }
    //endregion

    //region ????????????????????????(luckPanImg,luckCollectImg)
    private var taskControlConfig: TaskConfigInfo? = null
    private var taskCenterConfig: TaskCenterInfo? = null

    //????????????????????????
    private var exchangeActiveNum = 15

    private fun initTaskControl() {
        taskControlConfig = TaskImgControlUtil.getTaskControlConfig()
        taskCenterConfig = TaskControlUtil.getTaskControlConfig()
        if (taskCenterConfig != null) {
            for (index in taskCenterConfig!!.items.indices) {
                if (taskCenterConfig!!.items[index].taskType == VIDEO) {
                    mMaxCountTime = taskCenterConfig!!.items[index].cd//??????????????????
                }
                if (taskCenterConfig!!.items[index].taskType == GIFT_BOX) {
                    boxMaxTime = taskCenterConfig!!.items[index].cd
                    boxMaxOpenNum = taskCenterConfig!!.items[index].repeat
                }
            }
            exchangeActiveNum = taskCenterConfig!!.activeExchange.active
        }
    }
    //endregion

    //region ??????viewModel
    private lateinit var mShareVideModel: MainShareViewModel

    private fun initShareViewModel() {
        mShareVideModel = ViewModelProvider(requireActivity()).get(MainShareViewModel::class.java)
    }
    //endregion

    //region ????????????
    private var shakeAnimation: ObjectAnimator? = null
    private var shakeAnimation1: ObjectAnimator? = null

    private fun startBoxAnimation() {
        cancelBoxAnimation()
        shakeAnimation = AnimationUtil.startShakeAnimation(mDataBinding?.iconBox, 1f)
        shakeAnimation1 = AnimationUtil.startShakeAnimation(mDataBinding?.iconCanGet, 1f)
    }

    private fun cancelBoxAnimation() {
        if (shakeAnimation != null && shakeAnimation!!.isRunning) {
            shakeAnimation?.cancel()
            shakeAnimation = null
        }
        if (shakeAnimation1 != null && shakeAnimation1!!.isRunning) {
            shakeAnimation1?.cancel()
            shakeAnimation1 = null
        }
    }

    private fun startFingerAnimation() {
        AnimationUtil.startTaskFingerAnimation(mDataBinding?.fingerAnimation)
    }

    private fun cancelFingerAnimation() {
        AnimationUtil.cancelFingerAnimation(mDataBinding?.fingerAnimation)
    }
    //endregion

    val mHandler = Handler(Looper.getMainLooper())

    //region bus??????????????????
    private fun initEventBus() {
        EventBus.getDefault().register(this)
    }

    //????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun shareClickNotify(event: ShareClickNotifyEvent?) {
        mShareVideModel.requestAdReport(MainShareViewModel.ID_SHARE, MainShareViewModel.TYPE_SHARE)
    }

    //??????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun startNewCardNotify(event: CollectStartNewCardEvent?) {
        mShareVideModel.requestAdReport(
            MainShareViewModel.ID_COLLECT,
            MainShareViewModel.TYPE_COLLECT
        )
    }

    //??????????????????,???????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTurntableBeanEvent(event: ItemsDTO?) {
        mShareVideModel.requestAdReport(
            MainShareViewModel.ID_TURNTABLE,
            MainShareViewModel.TYPE_TURNTABLE
        )
    }

    //??????????????????,???????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLotteryEvent(event: LotteryEventUnlockBean?) {
        mShareVideModel.requestAdReport(
            MainShareViewModel.ID_LOTTERY,
            MainShareViewModel.TYPE_LOTTERY
        )
    }

    //??????????????????,??????????????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSignEvent(event: TaskReportEvent?) {
        if (event?.eventType == SIGN) {
            mShareVideModel.requestTaskBubbles()
        }
    }
    //endregion

    private var bubbleFloatSignAnimation: ObjectAnimator? = null
    private var bubbleFloatSignTvAnimation: ObjectAnimator? = null
    private var bubbleFloatTimerAdTvAnimation: ObjectAnimator? = null
    private var bubbleFloatAdTimerTvAnimation: ObjectAnimator? = null
    private var bubbleFloatAdTimerTvTvAnimation: ObjectAnimator? = null
    private var bubbleFloatLuckPanAnimation: ObjectAnimator? = null
    private var bubbleFloatLuckPanTvAnimation: ObjectAnimator? = null
    private var bubbleFloatCollectAnimation: ObjectAnimator? = null
    private var bubbleFloatCollectTvAnimation: ObjectAnimator? = null
    private var bubbleFloatShareAnimation: ObjectAnimator? = null
    private var bubbleFloatShareTvAnimation: ObjectAnimator? = null
    private var bubbleFloatLuckDrawAnimation: ObjectAnimator? = null
    private var bubbleFloatLuckDrawTvAnimation: ObjectAnimator? = null
    private fun startBubbleAnimation() {
        bubbleFloatSignAnimation =
            AnimationUtil.bubbleFloat(mDataBinding?.iconSignBubble, 4000, 10f, -1)
        bubbleFloatSignAnimation?.start()
        bubbleFloatSignTvAnimation =
            AnimationUtil.bubbleFloat(mDataBinding?.iconSignTv, 4000, 10f, -1)
        bubbleFloatSignTvAnimation?.start()
        bubbleFloatTimerAdTvAnimation =
            AnimationUtil.bubbleFloat(mDataBinding?.coldDownTimer, 2000, 10f, -1)
        bubbleFloatTimerAdTvAnimation?.start()
        bubbleFloatAdTimerTvAnimation =
            AnimationUtil.bubbleFloat(mDataBinding?.countDownTimeTv, 2000, 10f, -1)
        bubbleFloatAdTimerTvAnimation?.start()
        bubbleFloatAdTimerTvTvAnimation =
            AnimationUtil.bubbleFloat(mDataBinding?.seeAdTv, 2000, 10f, -1)
        bubbleFloatAdTimerTvTvAnimation?.start()
        bubbleFloatLuckPanAnimation =
            AnimationUtil.bubbleFloat(mDataBinding?.iconLuckPanBubble, 3000, 10f, -1)
        bubbleFloatLuckPanAnimation?.start()
        bubbleFloatLuckPanTvAnimation =
            AnimationUtil.bubbleFloat(mDataBinding?.iconLuckPanTv, 3000, 10f, -1)
        bubbleFloatLuckPanTvAnimation?.start()
        bubbleFloatCollectAnimation =
            AnimationUtil.bubbleFloat(mDataBinding?.iconCollectBubble, 3500, 10f, -1)
        bubbleFloatCollectAnimation?.start()
        bubbleFloatCollectTvAnimation =
            AnimationUtil.bubbleFloat(mDataBinding?.iconCollectTv, 3500, 10f, -1)
        bubbleFloatCollectTvAnimation?.start()
        bubbleFloatShareAnimation =
            AnimationUtil.bubbleFloat(mDataBinding?.iconShareBubble, 2500, 10f, -1)
        bubbleFloatShareAnimation?.start()
        bubbleFloatShareTvAnimation =
            AnimationUtil.bubbleFloat(mDataBinding?.shareTv, 2500, 10f, -1)
        bubbleFloatShareTvAnimation?.start()
        bubbleFloatLuckDrawAnimation =
            AnimationUtil.bubbleFloat(mDataBinding?.iconLuckDrawBubble, 1500, 10f, -1)
        bubbleFloatLuckDrawAnimation?.start()
        bubbleFloatLuckDrawTvAnimation =
            AnimationUtil.bubbleFloat(mDataBinding?.iconLuckDrawTv, 1500, 10f, -1)
        bubbleFloatLuckDrawTvAnimation?.start()
    }

    private fun cancelBubbleAnimation() {
        if (bubbleFloatSignAnimation != null && bubbleFloatSignAnimation!!.isRunning) {
            bubbleFloatSignAnimation?.cancel()
            bubbleFloatSignAnimation = null
        }
        if (bubbleFloatSignTvAnimation != null && bubbleFloatSignTvAnimation!!.isRunning) {
            bubbleFloatSignTvAnimation?.cancel()
            bubbleFloatSignTvAnimation = null
        }
        if (bubbleFloatTimerAdTvAnimation != null && bubbleFloatTimerAdTvAnimation!!.isRunning) {
            bubbleFloatTimerAdTvAnimation?.cancel()
            bubbleFloatTimerAdTvAnimation = null
        }
        if (bubbleFloatAdTimerTvAnimation != null && bubbleFloatAdTimerTvAnimation!!.isRunning) {
            bubbleFloatAdTimerTvAnimation?.cancel()
            bubbleFloatAdTimerTvAnimation = null
        }
        if (bubbleFloatAdTimerTvTvAnimation != null && bubbleFloatAdTimerTvTvAnimation!!.isRunning) {
            bubbleFloatAdTimerTvTvAnimation?.cancel()
            bubbleFloatAdTimerTvTvAnimation = null
        }
        if (bubbleFloatLuckPanAnimation != null && bubbleFloatLuckPanAnimation!!.isRunning) {
            bubbleFloatLuckPanAnimation?.cancel()
            bubbleFloatLuckPanAnimation = null
        }
        if (bubbleFloatLuckPanTvAnimation != null && bubbleFloatLuckPanTvAnimation!!.isRunning) {
            bubbleFloatLuckPanTvAnimation?.cancel()
            bubbleFloatLuckPanTvAnimation = null
        }
        if (bubbleFloatCollectAnimation != null && bubbleFloatCollectAnimation!!.isRunning) {
            bubbleFloatCollectAnimation?.cancel()
            bubbleFloatCollectAnimation = null
        }
        if (bubbleFloatCollectTvAnimation != null && bubbleFloatCollectTvAnimation!!.isRunning) {
            bubbleFloatCollectTvAnimation?.cancel()
            bubbleFloatCollectTvAnimation = null
        }
        if (bubbleFloatShareAnimation != null && bubbleFloatShareAnimation!!.isRunning) {
            bubbleFloatShareAnimation?.cancel()
            bubbleFloatShareAnimation = null
        }
        if (bubbleFloatShareTvAnimation != null && bubbleFloatShareTvAnimation!!.isRunning) {
            bubbleFloatShareTvAnimation?.cancel()
            bubbleFloatShareTvAnimation = null
        }
        if (bubbleFloatLuckDrawAnimation != null && bubbleFloatLuckDrawAnimation!!.isRunning) {
            bubbleFloatLuckDrawAnimation?.cancel()
            bubbleFloatLuckDrawAnimation = null
        }
        if (bubbleFloatLuckDrawTvAnimation != null && bubbleFloatLuckDrawTvAnimation!!.isRunning) {
            bubbleFloatLuckDrawTvAnimation?.cancel()
            bubbleFloatLuckDrawTvAnimation = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (shakeAnimation != null && shakeAnimation!!.isRunning) {
            shakeAnimation?.cancel()
            shakeAnimation = null
        }
        if (shakeAnimation1 != null && shakeAnimation1!!.isRunning) {
            shakeAnimation1?.cancel()
            shakeAnimation1 = null
        }
        if (gifDrawable != null && !gifDrawable!!.isRecycled) {
            gifDrawable?.recycle()
            gifDrawable = null
        }
        cancelBubbleAnimation()
        mHandler.removeCallbacks(boxTimer)
    }

}