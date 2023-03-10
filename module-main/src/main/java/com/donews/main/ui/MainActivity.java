package com.donews.main.ui;

import static com.donews.common.config.CritParameterConfig.CRIT_STATE;
import static com.donews.utilslibrary.utils.KeySharePreferences.NOTIFY_RANDOM_RED_AMOUNT;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.app.hubert.guide.core.Controller;
import com.dn.events.ad.HotStartEvent;
import com.dn.events.events.DoubleRpEvent;
import com.dn.events.events.EnterShowDialogEvent;
import com.dn.events.events.LoginUserStatus;
import com.dn.events.events.LotteryBackEvent;
import com.dn.events.events.RedPackageStatus;
import com.dn.events.events.WalletRefreshEvent;
import com.dn.integral.jdd.IntegralComponent;
import com.dn.integral.jdd.integral.ProxyIntegral;
import com.dn.sdk.listener.banner.SimpleBannerListener;
import com.donews.base.base.AppManager;
import com.donews.base.base.AppStatusConstant;
import com.donews.base.base.AppStatusManager;
import com.donews.base.utils.ToastUtil;
import com.donews.common.adapter.ScreenAutoAdapter;
import com.donews.common.base.MvvmBaseLiveDataActivity;
import com.donews.common.bean.CritMessengerBean;
import com.donews.common.config.CritParameterConfig;
import com.donews.common.router.RouterActivityPath;
import com.donews.common.router.RouterFragmentPath;
import com.donews.common.updatedialog.UpdateManager;
import com.donews.common.updatedialog.UpdateReceiver;
import com.donews.common.views.CountdownView;
import com.donews.main.BuildConfig;
import com.donews.main.R;
import com.donews.main.adapter.MainPageAdapter;
import com.donews.main.common.CommonParams;
import com.donews.main.databinding.MainActivityMainBinding;
import com.donews.main.databinding.MainPopWindowProgressBarBinding;
import com.donews.main.dialog.AnAdditionalDialog;
import com.donews.main.dialog.DrawDialog;
import com.donews.main.dialog.EnterShowDialog;
import com.donews.main.dialog.MainRpDialog;
import com.donews.main.dialog.MoreAwardDialog;
import com.donews.main.dialog.RemindDialogExt;
import com.donews.main.dialog.ext.CritWelfareDialogFragment;
import com.donews.main.listener.RetentionTaskListener;
import com.donews.main.utils.ExitInterceptUtils;
import com.donews.main.utils.ExtDialogUtil;
import com.donews.main.viewModel.MainViewModel;
import com.donews.main.views.CornerMarkUtils;
import com.donews.main.views.MainBottomTanItem;
import com.donews.middle.IMainParams;
import com.donews.middle.bean.mine2.emuns.Mine2TaskType;
import com.donews.middle.bean.mine2.resp.DailyTaskResp;
import com.donews.middle.centralDeploy.ABSwitch;
import com.donews.middle.adutils.BannerAd;
import com.donews.middle.adutils.DnSdkInit;
import com.donews.middle.adutils.InterstitialFullAd;
import com.donews.middle.adutils.RewardVideoAd;
import com.donews.middle.bean.HighValueGoodsBean;
import com.donews.middle.bean.RedEnvelopeUnlockBean;
import com.donews.middle.bean.TaskActionBean;
import com.donews.middle.cache.GoodsCache;
import com.donews.middle.events.SigninCloseEvent;
import com.donews.middle.request.RequestUtil;
import com.donews.middle.utils.ActivityGuideMaskUtil;
import com.donews.middle.utils.CriticalModelTool;
import com.donews.middle.utils.HotStartCacheUtils;
import com.donews.middle.viewmodel.BaseMiddleViewModel;
import com.donews.middle.views.FrontFloatingBtn;
import com.donews.utilslibrary.analysis.AnalysisParam;
import com.donews.utilslibrary.analysis.AnalysisUtils;
import com.donews.utilslibrary.utils.AppInfo;
import com.donews.utilslibrary.utils.DateManager;
import com.donews.utilslibrary.utils.DensityUtils;
import com.donews.utilslibrary.utils.HttpConfigUtilsKt;
import com.donews.utilslibrary.utils.KeySharePreferences;
import com.donews.utilslibrary.utils.LogUtil;
import com.donews.utilslibrary.utils.SPUtils;
import com.donews.yfsdk.manager.AdConfigManager;
import com.donews.yfsdk.monitor.LotteryAdCheck;
import com.gyf.immersionbar.ImmersionBar;
import com.vmadalin.easypermissions.EasyPermissions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import me.majiajie.pagerbottomtabstrip.NavigationController;

/**
 * app ?????????
 *
 * @author darryrzhoong
 */

@Route(path = RouterActivityPath.Main.PAGER_MAIN)
public class MainActivity
        extends MvvmBaseLiveDataActivity<MainActivityMainBinding, MainViewModel> implements
        RetentionTaskListener, IMainParams {

    //??????????????????????????????
    private static boolean s_IsShowSigninDialog = false;

    private boolean isShowSigninDialog = false;

    private List<Fragment> fragments;

    private MainPageAdapter adapter;

    private NavigationController mNavigationController;

    private long mFirstClickBackTime = 0;
    //?????? ???????????????????????? 5??????
    private final long CruelDuration = 5 * 60 * 1000;
    //??????????????????????????????
    DialogFragment appDownDialog;
    private Handler mHandler = new Handler();

    private EnterShowDialog mEnterShowDialog = null;

    @Autowired
    String from;

    /**
     * ????????????tab
     */
    @Autowired(name = "position")
    int mPosition = 0;
    private final CornerMarkUtils maskUtils = new CornerMarkUtils(this);

    private MainBottomTanItem lotteryItem;
    private final ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            mViewModel.checkMaskData(MainActivity.this);
            lotteryItem.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
        }
    };

    public static void start(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ScreenAutoAdapter.match(this, 375.0f);
        super.onCreate(savedInstanceState);
        ImmersionBar.with(this)
                .statusBarColor(R.color.transparent)
                .navigationBarColor(R.color.black)
                .fitsSystemWindows(false)
                .autoDarkModeEnable(true)
                .init();

        DnSdkInit.INSTANCE.init(this.getApplication());

        int nInAppCount = SPUtils.getInformain(KeySharePreferences.IS_FIRST_IN_APP, 0);
        nInAppCount++;
        SPUtils.setInformain(KeySharePreferences.IS_FIRST_IN_APP, nInAppCount);

        EventBus.getDefault().register(this);
        if (isShowSigninDialog) {
            showDrawDialog();
        } else {
            boolean isShowSingDialog = false;
            if (BaseMiddleViewModel.getBaseViewModel().mine2DailyTask.getValue() == null) {
                //?????????????????????????????????
                isShowSigninDialog = true;
                showDrawDialog();
            } else {
                //????????????????????????????????????
                for (int i = 0; i < BaseMiddleViewModel.getBaseViewModel().mine2DailyTask.getValue().size(); i++) {
                    DailyTaskResp.DailyTaskItemResp task = BaseMiddleViewModel.getBaseViewModel().mine2DailyTask.getValue().get(i);
                    if (Mine2TaskType.sign.type.equals(task.type) && task.status == 0) {
                        isShowSingDialog = true; //????????????????????????????????????????????????????????????
                        break;
                    }
                }
            }
            if (isShowSingDialog && !s_IsShowSigninDialog) {
                //??????????????????
                s_IsShowSigninDialog = true;
                mHandler.postDelayed(() -> {
                    RouterFragmentPath.User.getSingDialog()
                            .show(getSupportFragmentManager(), "alksdjfklj");
                }, 500);
            } else {
                //??????????????????????????????,???????????????????????????????????????????????????????????????
                isShowSigninDialog = true;
                showDrawDialog();
            }
        }
        //????????????
        mViewModel.openWindFastNewPeriod.observe(this, result -> {
            if (result == null) {
                return;
            }
            if (result) {
                maskUtils.addMark(lotteryItem);
            } else {
                maskUtils.removeMark();
            }
        });
        if (!ABSwitch.Ins().isOpenAB()) {
//            lotteryItem.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        }

        new Handler().postDelayed(AdConfigManager.INSTANCE::updateRewardId, 1000);
        new Handler().postDelayed(() -> {
            //???????????????????????????
            RewardVideoAd.INSTANCE.preloadAd(this, false);
        }, 3000);

        //???????????????????????????????????????
        testUMMuliParams();
        mDataBinding.occupyPosition.post(this::initializeCritState);

        showCriticalBtn();
        initScoreTaskFloatingBtn();
        if (!BuildConfig.DEBUG) {
            mDataBinding.occupyNotify.setVisibility(View.GONE);
        }
        //??????????????????
        HotStartCacheUtils.INSTANCE.setDebugNotify(true); //??????debug???????????????
        mDataBinding.occupyNotify.setOnClickListener(v -> {
            /*if (!HotStartCacheUtils.INSTANCE.isDebugNotify()) {
                return;
            }
            testGotoNotify();*/
//            InterstitialAd.INSTANCE.showAd(this, null);
//            RewardVideoAd.INSTANCE.showPreloadRewardVideo(this, null);
            showDrawDialog();
            /*InterstitialFullAd.INSTANCE.loadAndShowInterstitialFullAd(this, new IAdInterstitialFullScreenListener() {
                @Override
                public void onAdStatus(int code, @Nullable Object any) {

                }

                @Override
                public void onAdLoad() {

                }

                @Override
                public void onAdCached() {

                }

                @Override
                public void onAdError(int errorCode, @NonNull String errprMsg) {

                }

                @Override
                public void onAdShow() {

                }

                @Override
                public void onAdClicked() {

                }

                @Override
                public void onAdComplete() {

                }

                @Override
                public void onAdClose() {

                }

                @Override
                public void onSkippedVideo() {

                }

                @Override
                public void onRewardVerify(boolean reward) {

                }

                @Override
                public void onAdShowFail(int errCode, @NonNull String errMsg) {

                }

                @Override
                public void onAdVideoError(int errCode, @NonNull String errMsg) {

                }

                @Override
                public void onAdStartLoad() {

                }
            });*/
        });

        if (!HotStartCacheUtils.INSTANCE.isShowing()) {
            isFromNotify();
        }
    }

    @Override
    public int getThisFragmentCurrentPos(@NonNull Fragment f) {
        for (int i = 0; i < fragments.size(); i++) {
            if (f == fragments.get(i)) {
                return i;
            }
        }
        return -1;
    }

    //??????????????????
    private void testGotoNotify() {
        try {
            Class czz = Class.forName("com.donews.notify.launcher.NotifyActivity");
            Method method = czz.getMethod("actionStart", Context.class);
            method.invoke(null, this);
        } catch (Exception e) {
            ToastUtil.showShort(this, "???????????????????????????");
            e.printStackTrace();
        }
    }

    private void initScoreTaskFloatingBtn() {
        mDataBinding.mainFloatingRp.setListener(this);
        mDataBinding.mainFloatingRp.reLoadTask();
    }

    private void showCriticalBtn() {
        if (CriticalModelTool.canShowCriticalBtn()) {
            mDataBinding.mainFloatingBtn.showCriticalBtn();
            mDataBinding.mainFloatingBtn.setOnClickListener(v -> {
                toggleStatusBar(0);
                mDataBinding.cvContentView.setCurrentItem(0);
                mPosition = 0;
                bjClick();
            });
        } else {
//            mDataBinding.mainFloatingBtn.setYywInfo(FrontConfigManager.Ins().getConfigBean().getFloatingItems());
            mDataBinding.mainFloatingBtn.refreshYywItem();
        }
    }

    /**
     * ????????????????????????????????????
     * ??????????????????????????????????????????app??????
     */
    private void initializeCritState() {
        int critState = SPUtils.getInformain(CritParameterConfig.CRIT_STATE, 0);
        //????????????????????????
        if (critState == 1) {
            //?????????????????????????????????
            long critStartTime = SPUtils.getLongInformain(CritParameterConfig.CRIT_START_TIME, 0);
            if (critStartTime != 0) {
                //????????????
                long time = SystemClock.elapsedRealtime();
                if (Math.abs(time - critStartTime) >= CruelDuration) {
                    cleanCrit();
                } else {
                    showPopWindow(Math.abs(CruelDuration - (time - critStartTime)), CruelDuration);
                    //????????????????????????
                }
            }
        }
    }

    //???????????????????????????
    private void cleanCrit() {
        //?????????????????????
        SPUtils.setInformain(CRIT_STATE, 0);
        SPUtils.setInformain(CritParameterConfig.CRIT_REMAINING_TIME, 0);
        SPUtils.setInformain(CritParameterConfig.CRIT_START_TIME, 0);
        //??????????????????   false???????????????
        SPUtils.setInformain(CritParameterConfig.LOTTERY_MARK, false);
        //?????????????????????????????????????????????????????????????????????
        LotteryAdCheck.INSTANCE.resetCriticalModelNumber();
        mDataBinding.mainFloatingBtn.setModel(FrontFloatingBtn.CRITICAL_MODEL);
        showCriticalBtn();
        EventBus.getDefault().post(new CritMessengerBean(300));
    }

    @Subscribe
    public void bind(SigninCloseEvent event) {
        if (isShowSigninDialog) {
            return; //???????????????????????????????????????
        }
        isShowSigninDialog = true;
        //??????????????????
        showDrawDialog();
    }

    @Subscribe
    public void UnlockEvent(CritMessengerBean critMessenger) {
        if (critMessenger != null && critMessenger.mStatus == 200) {
            //??????????????????
            mDataBinding.mainFloatingBtn.setModel(FrontFloatingBtn.CRITICAL_MODEL);
            showCriticalBtn();
            showPopWindow(CruelDuration, CruelDuration);
            if (appDownDialog != null) {
                runOnUiThread(() -> {
                    appDownDialog.dismiss();
                    if (appDownDialog.getDialog() != null) {
                        appDownDialog.getDialog().dismiss();
                    }
                });
            }
        }
    }

    private void showPopWindow(long time, long sumTime) {
        Log.d("=====%%   ", time + "");
        MainPopWindowProgressBarBinding viewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.main_pop_window_progress_bar, null, false);
        PopupWindow mPopWindow = new PopupWindow(viewDataBinding.getRoot());
        mPopWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mPopWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopWindow.setFocusable(false);
        mPopWindow.setClippingEnabled(false);
        mPopWindow.showAtLocation(getWindow().getDecorView(), Gravity.TOP, 0, 0);
        viewDataBinding.countdownView.start(time);
        viewDataBinding.countdownView.setCountdownViewListener(new CountdownView.ICountdownViewListener() {
            @Override
            public void onProgressValue(long max, long value) {
                viewDataBinding.progressBar.setMax((int) sumTime);
                viewDataBinding.progressBar.setProgress((value - 12000) > 0 ? (int) (value - 12000) : 0);
                viewDataBinding.progressBar.setSecondaryProgress((int) value);
                //???????????????????????????
                SPUtils.setInformain(CRIT_STATE, 1);
                SPUtils.setInformain(CritParameterConfig.CRIT_REMAINING_TIME, value);
            }

            @Override
            public void onCountdownCompleted() {
                mPopWindow.dismiss();
                cleanCrit();
                ToastUtil.showShort(getApplicationContext(), "?????????????????????");
            }
        });
    }

    private boolean mBannerAdShow = false, mBannerLoading = false;
    private int mBannerHeight;

    private void loadBannerAd() {
//        if (!mBannerAdShow && !mBannerLoading) {
//            mBannerLoading = true;
        mDataBinding.rlAdContainer.post(new Runnable() {
            @Override
            public void run() {
                mDataBinding.rlAdContainer.removeAllViews();
                int width = DensityUtils.getScreenWidth();
                int height = (int) (width / 300f * 45f);
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mDataBinding.rlAdContainer.getLayoutParams();
                params.width = width;
                params.height = FrameLayout.LayoutParams.WRAP_CONTENT;
                mDataBinding.rlAdContainer.setLayoutParams(params);
                mBannerHeight = height;

                BannerAd.INSTANCE.loadAndShowBannerAd(MainActivity.this, mDataBinding.rlAdContainer,
                        DensityUtils.px2dp(width),
                        DensityUtils.px2dp(height),
                        new SimpleBannerListener() {
                            @Override
                            public void onAdExposure() {
                                super.onAdExposure();
                                mBannerAdShow = true;
                                mBannerLoading = false;
                            }

                            @Override
                            public void onAdClosed() {
                                super.onAdClosed();
                                mBannerAdShow = false;
                            }

                            @Override
                            public void onAdError(int code, @Nullable String errorMsg) {
                                super.onAdError(code, errorMsg);
                                mBannerAdShow = false;
                                mBannerLoading = false;
                            }
                        });
            }
        });
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        loadBannerAd();

        if (SPUtils.getInformain(KeySharePreferences.FIRST_RP_IS_OPEN, false)) {
            return;
        }
        if (SPUtils.getInformain(KeySharePreferences.FIRST_RP_CAN_OPEN, false)) {
//            SPUtils.setInformain(KeySharePreferences.FIRST_RP_CAN_OPEN, false);
            String preId = SPUtils.getInformain(KeySharePreferences.FIRST_RP_OPEN_PRE_ID, "");
            if (preId.equalsIgnoreCase("")) {
                ToastUtil.showShort(MainActivity.this, "??????????????????");
                return;
            }

            mViewModel.postDoubleRp("", preId).observe(this, doubleRedPacketBean -> {
                if (doubleRedPacketBean == null) {
                    ToastUtil.showShort(MainActivity.this, "????????????????????????");
                    return;
                }

                EventBus.getDefault().post(
                        new DoubleRpEvent(10, doubleRedPacketBean.getScore(), doubleRedPacketBean.getRestId(), "", 0f));
            });
        }

        mDataBinding.mainFloatingRp.reLoadTask();
        if (!HotStartCacheUtils.INSTANCE.isShowing()) {
            checkRetentionTask();
        }
        showCriticalBtn();
    }

    //???????????????????????????
    private void testUMMuliParams() {
        if (BuildConfig.DEBUG) {
            HashMap<String, Object> para = new HashMap<>();
            String paramsValue = "testParams" + new Random().nextInt(10);
            para.put("source", paramsValue);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginUserStatus status) {
        if (status.getStatus() == 1 && AppInfo.checkIsWXLogin()) {
            //??????????????????id
            String url = HttpConfigUtilsKt.withConfigParams(BuildConfig.AD_ID_CONFIG, false);
            AdConfigManager.INSTANCE.init();
        }

        showCriticalBtn();
    }

    /**
     * ??????????????????
     */
    private void showDrawDialog() {
        if (ABSwitch.Ins().isOpenAB()) {
            return;
        }

        if (DateManager.getInstance().ifFirst(DateManager.SHOW_DIALOG_WHEN_LAUNCH)
                && SPUtils.getInformain(KeySharePreferences.SHOW_DIALOG_WHEN_LAUNCH, true)) {
            SPUtils.setInformain(KeySharePreferences.SHOW_DIALOG_WHEN_LAUNCH, true);
        }

        boolean logType = AppInfo.checkIsWXLogin();
        if (logType) {
            DrawDialog mDrawDialog = new DrawDialog(this);
            mDrawDialog.setEventListener(new DrawDialog.EventListener() {
                @Override
                public void switchPage() {
                    if (mNavigationController != null) {
                        mPosition = 2;
                        mNavigationController.setSelect(mPosition);
                    }
                }

                @Override
                public void dismiss() {
                    if (mDrawDialog.isAdded()) {
                        mDrawDialog.dismiss();
                    }
                    if (SPUtils.getInformain(KeySharePreferences.SHOW_DIALOG_WHEN_LAUNCH, true)) {
                        if (mEnterShowDialog == null) {
                            mEnterShowDialog = new EnterShowDialog(MainActivity.this);
                            mEnterShowDialog.setOwnerActivity(MainActivity.this);
                        }
                        if (!mEnterShowDialog.isShowing()) {
                            mEnterShowDialog.showEx();
                        }
                    } else {
//                        InterstitialAd.INSTANCE.showAd(MainActivity.this, null);
                        InterstitialFullAd.INSTANCE.showAd(MainActivity.this, null);
                    }
                }

                @Override
                public void show() {
                    if (!MainActivity.this.isFinishing()) {
                        mDrawDialog.showEx(getSupportFragmentManager(), "DrawDialog");
                    }
                }
            });
            mDrawDialog.requestGoodsInfo(getApplicationContext());
        } else {
//            if (SPUtils.getInformain(KeySharePreferences.SHOW_DIALOG_WHEN_LAUNCH, true)) {
//                        new EnterShowDialog(MainActivity.this).showEx();
//                if (mEnterShowDialog == null) {
//                    mEnterShowDialog = new EnterShowDialog(MainActivity.this);
//                    mEnterShowDialog.setOwnerActivity(MainActivity.this);
//                }
//                if (!mEnterShowDialog.isShowing()) {
//                    mEnterShowDialog.showEx();
//                }
//            }
//            FreePanicBuyingDialog mFreePanicBuyingDialog = new FreePanicBuyingDialog(MainActivity.this);
//            mFreePanicBuyingDialog.setFinishListener(new FreePanicBuyingDialog.OnFinishListener() {
//                @Override
//                public void onDismiss() {
//                    if (SPUtils.getInformain(KeySharePreferences.SHOW_DIALOG_WHEN_LAUNCH, true)) {
////                        new EnterShowDialog(MainActivity.this).showEx();
//                        if (mEnterShowDialog == null) {
//                            mEnterShowDialog = new EnterShowDialog(MainActivity.this);
//                            mEnterShowDialog.setOwnerActivity(MainActivity.this);
//                        }
//                        if (!mEnterShowDialog.isShowing()) {
//                            mEnterShowDialog.showEx();
//                        }
//                    }
//                }
//
//                @Override
//                public void onShow() {
//                    if (mFreePanicBuyingDialog != null && !MainActivity.this.isFinishing()) {
//                        mFreePanicBuyingDialog.show();
//                    }
//                }
//            });
//            mFreePanicBuyingDialog.requestGoodsInfo(getApplicationContext());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        ARouter.getInstance().inject(this);
        if (mNavigationController != null) {
            mNavigationController.setSelect(mPosition);
        }

        if (!HotStartCacheUtils.INSTANCE.isShowing()) {
            isFromNotify();
        }
    }


    private void initView(int position) {
        ARouter.getInstance().inject(this);
        int checkColor = getResources().getColor(R.color.common_btn_color_sec);
        int defaultColor = getResources().getColor(R.color.common_D5D5D5);

        if (!ABSwitch.Ins().isOpenAB()) {
            MainBottomTanItem homeItem = new MainBottomTanItem(this);
            homeItem.initialization("??????", R.drawable.main_home_checked, defaultColor, checkColor,
                    R.drawable.main_home_checked_secect);

            MainBottomTanItem lotteryItem = new MainBottomTanItem(this);
            //??????????????????id?????????View?????????????????????????????????????????????????????????
            lotteryItem.setId(R.id.accessibility_custom_action_0);
            lotteryItem.initialization("??????", R.drawable.main_lottery_icon, defaultColor, checkColor, R.drawable.main_lottery_icon_seclect);

            MainBottomTanItem taskItem = new MainBottomTanItem(this);
            //??????????????????id?????????View?????????????????????????????????????????????????????????
            taskItem.setId(R.id.accessibility_custom_action_1);
            taskItem.initialization("??????", R.drawable.main_action, defaultColor, checkColor, R.drawable.main_action_select);
            MainBottomTanItem mineItem = new MainBottomTanItem(this);
            mineItem.initialization("??????", R.drawable.main_mine, defaultColor, checkColor, R.drawable.main_mine_select);
            mNavigationController = mDataBinding.bottomView.custom()
                    .addItem(homeItem)
                    .addItem(lotteryItem)
                    .addItem(taskItem)
                    .addItem(mineItem)
                    .enableAnimateLayoutChanges()
                    .build();
        } else {
            MainBottomTanItem homeItem = new MainBottomTanItem(this);
            homeItem.initialization("??????", R.drawable.main_home_checked, defaultColor, checkColor,
                    R.drawable.main_home_checked_secect);
            MainBottomTanItem lotteryItem = new MainBottomTanItem(this);
            lotteryItem.initialization("?????????", R.drawable.main_lottery, defaultColor, checkColor,
                    R.drawable.main_lottery_icon_seclect);

            MainBottomTanItem mineItem = new MainBottomTanItem(this);
            mineItem.initialization("??????", R.drawable.main_mine, defaultColor, checkColor, R.drawable.main_mine_select);

            mNavigationController = mDataBinding.bottomView.custom()
                    .addItem(homeItem)
                    .addItem(lotteryItem)
                    .addItem(mineItem)
                    .enableAnimateLayoutChanges()
                    .build();
        }
        mNavigationController.showBottomLayout();
        mDataBinding.cvContentView.setOffscreenPageLimit(4);
        mDataBinding.cvContentView.setAdapter(adapter);
        mNavigationController.setupWithViewPager(mDataBinding.cvContentView);

        mDataBinding.cvContentView.setCurrentItem(position);
        mNavigationController.addSimpleTabItemSelectedListener((index, old) -> {
            toggleStatusBar(index);
            mPosition = index;
        });
        AppStatusManager.getInstance().setAppStatus(AppStatusConstant.STATUS_NORMAL);

        showCriticalBtn();
        mDataBinding.mainFloatingBtn.setOnClickListener(v -> {
            toggleStatusBar(0);
            mDataBinding.cvContentView.setCurrentItem(0);
            mPosition = 0;
            bjClick();
        });

        int intoFrontCounts = SPUtils.getInformain(KeySharePreferences.INTO_FRONT_COUNTS, 0);
        if (!SPUtils.getInformain(KeySharePreferences.HAS_DO_INTO_FRONT, false)) {
            intoFrontCounts++;
            SPUtils.setInformain(KeySharePreferences.INTO_FRONT_COUNTS, intoFrontCounts);
        }

        if (ABSwitch.Ins().isOpenHomeGuid() != 0
                && SPUtils.getInformain(KeySharePreferences.INTO_FRONT_COUNTS, 0) >= ABSwitch.Ins().isOpenHomeGuid()
                && !SPUtils.getInformain(KeySharePreferences.HAS_DO_INTO_FRONT, false)) {
            mDataBinding.mainHomeGuidCl.setVisibility(View.VISIBLE);
            mDataBinding.mainHomeBtn.setOnClickListener(v -> {
                mDataBinding.mainHomeGuidCl.setVisibility(View.GONE);
                toggleStatusBar(3);
                mDataBinding.cvContentView.setCurrentItem(3);
                mPosition = 3;
            });
        } else {
            mDataBinding.mainHomeGuidCl.setVisibility(View.GONE);
        }
    }

    //?????????????????????
    private void bjClick() {
        HighValueGoodsBean t = GoodsCache.readGoodsBean(HighValueGoodsBean.class, "exit");
        if (t.getList() == null ||
                t.getList().isEmpty()) {
            ToastUtil.showShort(this, "??????????????????????????????");
            RequestUtil.requestHighValueGoodsInfo();
            return;
        }
        HighValueGoodsBean.GoodsInfo info = t.getList().get(0);
        //??????????????????????????????
        int count = CriticalModelTool.getCurrentUserModulCount();
        //????????????????????????
        int currCount = LotteryAdCheck.INSTANCE.getCriticalModelLotteryNumber();
        CritWelfareDialogFragment.OnSurListener surListener = (int type, int curJd, int totalJd) -> {
//            if(ABSwitch.Ins().getOpenAutoLotteryCount() >= )

            ARouter.getInstance()
                    .build(RouterFragmentPath.Lottery.PAGER_LOTTERY)
                    .withString("goods_id", info.getGoodsId())
                    .withBoolean("start_lottery", ABSwitch.Ins().isOpenAutoLottery())
//                            .withBoolean("start_lottery", ABSwitch.Ins().isOpenAutoLottery())
//                            .withBoolean("privilege", true)
                    .navigation();

            //??????????????????
            RequestUtil.requestHighValueGoodsInfo();
        };
        //?????????????????????????????????T:???????????????F:????????????
        boolean mark = SPUtils.getInformain(CritParameterConfig.LOTTERY_MARK, true);
        //??????????????????????????????????????????(???????????????????????????????????????)???T:???????????????F:??????????????????
        boolean critModelIsAllowAdd = DateManager.getInstance().isAllowCritical();
        if (mark && !critModelIsAllowAdd) {
            return; //????????????????????????????????????????????????????????????
        }
        if (!mark && CriticalModelTool.isNewUser()) {
            //?????????????????????(????????????????????????+?????????+?????????)???????????????????????????
            ExtDialogUtil.showCritWelfareDialog(
                    this, 0, currCount, count, surListener
            );
            return;
        }
        //??????????????????????????????T:????????????F:?????????
        boolean isOpenJFModel = ABSwitch.Ins().getOpenCritModel();
        if (!isOpenJFModel) {
            //???????????????????????????????????????????????????????????????
            ExtDialogUtil.showCritWelfareDialog(
                    this, 1, currCount, count, surListener
            );
            return;
        }
        if (currCount > 0) {
            //????????????????????????????????????????????????????????????
            ExtDialogUtil.showCritWelfareDialog(
                    MainActivity.this, 1, currCount, count, surListener
            );
            return;
        }
        showLoading("?????????");
        //??????????????????
        mDataBinding.mainFloatingBtn.setEnabled(false);
        IntegralComponent.getInstance().getIntegralList(new IntegralComponent.IntegralHttpCallBack() {
            @Override
            public void onSuccess(List<ProxyIntegral> list) {
                runOnUiThread(() -> {
                    mDataBinding.mainFloatingBtn.setEnabled(true);
                    if (appDownDialog != null) {
                        try {
                            appDownDialog.dismiss();
                            if (appDownDialog.getDialog() != null) {
                                appDownDialog.getDialog().dismiss();
                            }
                        } catch (Exception e) {
                            appDownDialog.dismiss();
                        }
                    }
                    hideLoading();
                    //??????????????????
                    appDownDialog = ExtDialogUtil.showCritDownAppDialog(MainActivity.this, list, null);
                });
            }

            @Override
            public void onError(String var1) {
                runOnUiThread(() -> {
                    mDataBinding.mainFloatingBtn.setEnabled(true);
                    //????????????,??????????????????
                    hideLoading();
                    ExtDialogUtil.showCritWelfareDialog(
                            MainActivity.this, 1, currCount, count, surListener
                    );
                });
            }

            @Override
            public void onNoTask() {
                runOnUiThread(() -> {
                    mDataBinding.mainFloatingBtn.setEnabled(true);
                    hideLoading();
                    //????????????,??????????????????
                    ExtDialogUtil.showCritWelfareDialog(
                            MainActivity.this, 1, currCount, count, surListener
                    );
                });
            }
        });
    }

    /**
     * ???????????????????????????
     *
     * @param position
     */
    private void toggleStatusBar(int position) {
        switch (position) {
//            case 0:
//                AnalysisUtils.onEventEx(this, Dot.Page_Home);
//                AnalysisUtils.onEventEx(this, Dot.Btn_Home);
//                if (mViewModel.openWindFastNewPeriod.getValue() == null) {
//                    //??????????????????
//                    mViewModel.checkMaskData(this);
//                }
//                break;
//            case 1:
//                AnalysisHelp.onEvent(this, AnalysisParam.TO_BENEFIT_BOTTOM_NAV);
//                AnalysisUtils.onEventEx(this, Dot.Page_ShowTime);
//                AnalysisUtils.onEventEx(this, Dot.Btn_ShowTime);
//                break;
//            case 2:
//                AnalysisUtils.onEventEx(this, Dot.Page_Lottery);
//                AnalysisUtils.onEventEx(this, Dot.Btn_Lottery);
//                if (mViewModel.getFastOpenWindPeriod() != null) {
//                    mViewModel.updateLocalOpenWindPeriod();
//                }
//                break;
//
//            case 3:
//                AnalysisUtils.onEventEx(this, Dot.Page_Lottery);
//                AnalysisUtils.onEventEx(this, Dot.Btn_Lottery);
//                if (mViewModel.getFastOpenWindPeriod() != null) {
//                    mViewModel.updateLocalOpenWindPeriod();
//                }
//                break;
//            default:
        }
    }

    private void initFragment() {
        fragments = new ArrayList<>();
        //??????ARouter ???????????????????????????fragment
        if (ABSwitch.Ins().isOpenAB()) {
            fragments.add((Fragment) ARouter.getInstance().build(RouterFragmentPath.Home.PAGER_HOME).navigation());
            fragments.add((Fragment) ARouter.getInstance().build(RouterFragmentPath.Spike.PAGER_SPIKE).navigation());
            fragments.add(
                    (Fragment) ARouter.getInstance().build(RouterFragmentPath.User.PAGER_USER_SETTING).navigation());
        } else {
            //????????????
            fragments.add((Fragment) ARouter.getInstance()
                    .build(RouterFragmentPath.Home.PAGER_EXCHANGE_FRAGMENT)
                    .navigation());
//            fragments.add((Fragment) ARouter.getInstance()
//                    .build(RouterFragmentPath.Front.PAGER_FRONT)
//                    .navigation());
            //????????????
            fragments.add((Fragment) ARouter.getInstance().build(RouterFragmentPath.HomeLottery.PAGER_LOTTERY).navigation());
            //????????????
            fragments.add((Fragment) ARouter.getInstance().build(RouterFragmentPath.Task.PAGER_TASK).navigation());
            //??????????????????
            fragments.add((Fragment) ARouter.getInstance().build(RouterFragmentPath.User.PAGER_USER_NEW).navigation());


        }

        adapter = new MainPageAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.setData(fragments);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.main_activity_main;
    }

    @Override
    public void initView() {
        int statusBarColor = R.color.main_color_bar;
        if (ABSwitch.Ins().isOpenAB()) {
            statusBarColor = R.color.white;
        }
        ImmersionBar.with(this)
                .statusBarColor(statusBarColor)
                .navigationBarColor(R.color.black)
                .fitsSystemWindows(true)
                .autoDarkModeEnable(true)
                .init();
        initFragment();
        initView(mPosition);
        // ????????????deviceId???????????????refresh????????????
        CommonParams.setNetWork();

        checkAppUpdate();
    }

    /**
     * ????????????
     */
    private void checkAppUpdate() {
        UpdateManager.getInstance().checkUpdate(this, false);
        //??????????????????
        UpdateReceiver updateReceiver = new UpdateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        getApplication().registerReceiver(updateReceiver, intentFilter);
    }

    @Override
    public void onBackPressed() {
        if (!ABSwitch.Ins().isOpenAB()) {
            ExitInterceptUtils.INSTANCE.intercept(this);
        } else {
            long duration = System.currentTimeMillis() - mFirstClickBackTime;
            if (duration < 2000) {
                // ????????????
                AppStatusManager.getInstance().setAppStatus(AppStatusConstant.STATUS_FORCE_KILLED);
                AnalysisUtils.onEvent(this, AnalysisParam.SHUTDOWN);
                AppManager.getInstance().AppExit();
                finish();
            } else {
                Toast.makeText(this, "?????????????????????", Toast.LENGTH_SHORT).show();
                mFirstClickBackTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.e("MainActivity onStop");
        if (mEnterShowDialog != null) {
            mEnterShowDialog.dismissEx();
            mEnterShowDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        LogUtil.e("MainActivity onDestroy");

        ExitInterceptUtils.resetFinishBackStatus();
        ImmersionBar.destroy(this, null);
        AppStatusManager.getInstance().setAppStatus(AppStatusConstant.STATUS_FORCE_KILLED);
        EventBus.getDefault().unregister(this);
        if (fragments != null) {
            fragments.clear();
            fragments = null;
        }
        if (adapter != null) {
            adapter.clear();
            adapter = null;
        }

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,
                    ExitInterceptUtils.INSTANCE.getRemindDialog());
        } catch (Exception e) {
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetMessage(RedPackageStatus redPackageStatus) {
        if (redPackageStatus.getStatus() == 0) {
            mDataBinding.mainFloatingBtn.setProgress(redPackageStatus.getCounts());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDoubleRpEvent(DoubleRpEvent event) {
        if (event.getEvent() == 1) {
            //??????????????????ok
            postGotDoubleRp(event.getRestId(), event.getPreId(), event.getScore());
        } else if (event.getEvent() == 2) {
            postGotDoubleRp(event.getRestId(), event.getPreId(), event.getScore());
        } else if (event.getEvent() == 3) {
            RemindDialogExt remindDialog = new RemindDialogExt();
            remindDialog.setOnCloseListener(remindDialog::dismiss);
            remindDialog.setOnSureListener(remindDialog::dismiss);
            remindDialog.setOnLaterListener(remindDialog::dismiss);
            remindDialog.show(this.getSupportFragmentManager(), "");
        } else if (event.getEvent() == 4) { //????????????????????????
            postGotDoubleRp(event.getRestId(), event.getPreId(), event.getScore());
        } else if (event.getEvent() == 5) { //5: ??????????????????->??????->??????
            showMoreAwardDialog(event);
        } else if (event.getEvent() == 6) { //6?????????????????????->??????->????????????
            new Handler().postDelayed(() -> showAnAdditionalDialog(event), 200);
        } else if (event.getEvent() == 7) { // 7: ??????????????????->??????->??????->??????????????????->??????
//            InterstitialAd.INSTANCE.showAd(this, null);
            InterstitialFullAd.INSTANCE.showAd(MainActivity.this, null);
        } else if (event.getEvent() == 8) { // 8??? ??????????????????->??????->??????->??????????????????->????????????/??????????????????->??????->??????????????????
            new Handler().postDelayed(() -> showAnAdditionalDialog(event), 200);
        } else if (event.getEvent() == 9) { // 9: ????????????????????????????????????????????????
            new Handler().postDelayed(() -> {
                if (getSupportFragmentManager().isDestroyed()) {
                    return;
                }
                if (getSupportFragmentManager().isStateSaved()) {
                    return;
                }
                MainRpDialog dialog = new MainRpDialog("privilege", 0f, "", "");
                dialog.show(getSupportFragmentManager(), "mainRpDialog");
            }, 200);
        } else if (event.getEvent() == 10) {    // 10: ???????????????(??????5????????????)
            new Handler().postDelayed(() -> {
                if (getSupportFragmentManager().isDestroyed()) {
                    return;
                }
                if (getSupportFragmentManager().isStateSaved()) {
                    return;
                }
                MainRpDialog dialog = new MainRpDialog("front", event.getScore(), event.getRestId(), "");
                dialog.show(getSupportFragmentManager(), "mainRpDialog");
            }, 200);
        } else if (event.getEvent() == 12) {    // 12: ????????????->??????
            showMoreAwardDialog(event);
        } else if (event.getEvent() == 13) {    // 13: ????????????->??????->??????????????????
            showMoreAwardDialog(event);
        }
    }

    private void showMoreAwardDialog(DoubleRpEvent event) {
        MoreAwardDialog moreAwardDialog = new MoreAwardDialog(event.getEvent(), event.getRestId(), event.getPreId(),
                event.getScore(), event.getRestScore());
        moreAwardDialog.setEventListener(() -> {
            try {
                if (moreAwardDialog.isAdded() && !this.isFinishing()) {
                    moreAwardDialog.dismiss();
                }
            } catch (Exception e) {
            }
        });
        if (!MainActivity.this.isFinishing() && !MainActivity.this.isDestroyed()) {
            if (getSupportFragmentManager().isDestroyed()) {
                return;
            }
            if (getSupportFragmentManager().isStateSaved()) {
                return;
            }
            moreAwardDialog.show(getSupportFragmentManager(), "MoreAwardDialog");
        }
    }

    private void showAnAdditionalDialog(DoubleRpEvent event) {
        AnAdditionalDialog mDrawDialog = new AnAdditionalDialog(1, event.getRestId(), event.getPreId(),
                event.getScore(),
                event.getRestScore(), 3);
        mDrawDialog.setEventListener(() -> {
            try {
                if (mDrawDialog.isAdded() && !MainActivity.this.isFinishing()) {
                    mDrawDialog.dismiss();
                }
            } catch (Exception e) {
            }
        });
        if (!MainActivity.this.isFinishing() && !MainActivity.this.isDestroyed()) {
            if (getSupportFragmentManager().isDestroyed()) {
                return;
            }
            if (getSupportFragmentManager().isStateSaved()) {
                return;
            }
            mDrawDialog.show(getSupportFragmentManager(), "doublerp");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetDoubleRpFromLottery(RedEnvelopeUnlockBean event) {
        if (event.getMStatus() != 200) {
            return;
        }

        if (SPUtils.getInformain(KeySharePreferences.FIRST_RP_IS_OPEN, false)) {
            return;
        }

        SPUtils.setInformain(KeySharePreferences.FIRST_RP_CAN_OPEN, true);
    }

    private void postGotDoubleRp(String restId, String preId, float score) {
        mViewModel.postDoubleRp(restId, preId).observe(this, doubleRedPacketBean -> {
            if (doubleRedPacketBean == null) {
                ToastUtil.showShort(MainActivity.this, "????????????????????????");
                return;
            }

            AnAdditionalDialog mDrawDialog = new AnAdditionalDialog(0, restId, preId, score,
                    doubleRedPacketBean.getScore(), 3);
            mDrawDialog.setEventListener(() -> {
                try {
                    if (mDrawDialog.isAdded() && !MainActivity.this.isFinishing()) {
                        mDrawDialog.dismiss();
                    }
                } catch (Exception e) {
                }
            });
            mDrawDialog.show(getSupportFragmentManager(), "AnAddDialog");
            EventBus.getDefault().post(new WalletRefreshEvent(0));
        });
    }

    private int mRetryCount = 0;

    @Override
    public void onTaskClick(String srcReqId, String wallReqId) {
        LogUtil.e("reqId: " + srcReqId + "  wallReqId: " + wallReqId);

        SPUtils.setInformain(KeySharePreferences.RETENTION_TASK_SRC_REQUEST_ID, srcReqId);
        SPUtils.setInformain(KeySharePreferences.RETENTION_TASK_WALL_REQUEST_ID, wallReqId);
    }

    private void checkRetentionTask() {
        String wallReqId = SPUtils.getInformain(KeySharePreferences.RETENTION_TASK_WALL_REQUEST_ID, "");
        if (TextUtils.isEmpty(wallReqId)) {
            return;
        }

        LogUtil.e("onHotStartEvent2:");
        mViewModel.getRetentionTask(wallReqId).observe(this, retentionTaskBean -> {
            if (retentionTaskBean == null || !retentionTaskBean.getHandout()) {
                if (mRetryCount > 2) {
                    LogUtil.e("onHotStartEvent3:");
                    return;
                }
                mRetryCount++;
                new Handler().postDelayed(this::checkRetentionTask, 3000);
                return;
            }

            LogUtil.e("onHotStartEvent4:");
            mRetryCount = 0;
            mViewModel.getWallTaskRp(wallReqId).observe(this, wallTaskRpBean -> {
                if (wallTaskRpBean == null) {
                    LogUtil.e("onHotStartEvent5:");
                    return;
                }


                LogUtil.e("onHotStartEvent6:");
                SPUtils.setInformain(KeySharePreferences.RETENTION_TASK_WALL_REQUEST_ID, "");
                /*ARouter.getInstance().build(RouterActivityPath.Rp.PAGE_RP)
                        .withString("from", "wallTask")
                        .withFloat("score", wallTaskRpBean.getScore())
                        .withString("restId", wallTaskRpBean.getRestId())
                        .navigation();*/
                MainRpDialog dialog = new MainRpDialog("wallTask", wallTaskRpBean.getScore(),
                        wallTaskRpBean.getRestId(), "");
                dialog.show(getSupportFragmentManager(), "mainRpDialog");
            });
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetTaskAction(TaskActionBean bean) {
        if (bean == null || TextUtils.isEmpty(bean.getPage())) {
            return;
        }

        if (bean.getPage().equalsIgnoreCase(TaskActionBean.WINNER)) {
            mDataBinding.cvContentView.setCurrentItem(2);
            toggleStatusBar(2);
            mPosition = 2;
        } else if (bean.getPage().equalsIgnoreCase(TaskActionBean.SHOW)) {
            mDataBinding.cvContentView.setCurrentItem(1);
            toggleStatusBar(1);
            mPosition = 1;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHotStartEvent(HotStartEvent event) {
        LogUtil.e("onHotStartEvent:" + event.getShow());
        if (!event.getShow()) {
            LogUtil.e("onHotStartEvent1:" + event.getShow());
            mRetryCount = 0;
            checkRetentionTask();           //??????????????????
            if (!isFromNotify()) {
//                InterstitialFullAd.INSTANCE.showAd(MainActivity.this, null);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)    //??????????????????????????????
    public void onEnterShowDialogEvent(EnterShowDialogEvent event) {
        if (event.getEvent() == 1 || event.getEvent() == 2) {
//            InterstitialAd.INSTANCE.showAd(this, null);
            InterstitialFullAd.INSTANCE.showAd(MainActivity.this, null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLotteryBackEvent(LotteryBackEvent event) {
        if (event.getEvent() == 1) {
//            InterstitialAd.INSTANCE.showAd(this, null);
            InterstitialFullAd.INSTANCE.showAd(MainActivity.this, null);
        }
    }

    private boolean isFromNotify() {
        if (!TextUtils.isEmpty(from) && from.equalsIgnoreCase("notify")) {
            mViewModel.getLandingRpTimesBean().observe(this, landingRpTimesBean -> {
                if (landingRpTimesBean == null) {
                    return;
                }
                if (landingRpTimesBean.getTimes() <= 0) {
                    MainRpDialog dialog = new MainRpDialog(from, SPUtils.getInformain(NOTIFY_RANDOM_RED_AMOUNT, 0.5f),
                            "", "");
                    dialog.show(getSupportFragmentManager(), "mainRpDialog");
                    from = "";
                    return;
                }

                EventBus.getDefault().post(new DoubleRpEvent(11, 0f, "", "", 0f));
            });
            return true;
        }

        return false;
    }
}