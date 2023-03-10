package com.donews.main.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.dn.events.events.LoginUserStatus;
import com.dn.events.events.NetworkChanageEvnet;
import com.dn.sdk.listener.interstitialfull.SimpleInterstitialFullListener;
import com.dn.sdk.listener.rewardvideo.SimpleRewardVideoListener;
import com.dn.sdk.listener.splash.IAdSplashListener;
import com.dn.sdk.listener.splash.SimpleSplashListener;
import com.donews.base.base.AppManager;
import com.donews.base.base.AppStatusConstant;
import com.donews.base.base.AppStatusManager;
import com.donews.base.utils.ToastUtil;
import com.donews.base.viewmodel.BaseLiveDataViewModel;
import com.donews.common.base.MvvmBaseLiveDataActivity;
import com.donews.common.contract.LoginHelp;
import com.donews.main.R;
import com.donews.main.databinding.MainActivitySplashBinding;
import com.donews.main.dialog.PersonGuideDialog;
import com.donews.main.utils.SplashUtils;
import com.donews.middle.adutils.DnSdkInit;
import com.donews.middle.adutils.InterstitialFullAd;
import com.donews.middle.adutils.RewardVideoAd;
import com.donews.middle.adutils.SplashAd;
import com.donews.middle.adutils.adcontrol.AdControlBean;
import com.donews.middle.adutils.adcontrol.AdControlManager;
import com.donews.middle.centralDeploy.ABSwitch;
import com.donews.middle.centralDeploy.TurntableSwitch;
import com.donews.middle.front.FrontConfigManager;
import com.donews.middle.viewmodel.BaseMiddleViewModel;
import com.donews.utilslibrary.analysis.AnalysisHelp;
import com.donews.utilslibrary.base.SmSdkConfig;
import com.donews.utilslibrary.base.UtilsConfig;
import com.donews.utilslibrary.datacenter.YfDcHelper;
import com.donews.utilslibrary.utils.DeviceUtils;
import com.donews.utilslibrary.utils.KeySharePreferences;
import com.donews.utilslibrary.utils.LogUtil;
import com.donews.utilslibrary.utils.NetworkUtils;
import com.donews.utilslibrary.utils.SPUtils;
import com.donews.yfsdk.manager.AdConfigManager;
import com.donews.yfsdk.monitor.LotteryAdCheck;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * ????????????: ???????????????
 * <p>
 * ?????????: ????????????
 * <p>
 *
 * @author darryrzhoong
 * @since 2020-02-26
 */
public class SplashActivity extends MvvmBaseLiveDataActivity<MainActivitySplashBinding, BaseLiveDataViewModel> {

    private static final String TAG = "SplashActivity";
    private static final String DEAL = "main_agree_deal";

    /**
     * ????????????????????????????????????
     *
     * @param act activity
     */
    public static void toForeGround(Activity act) {
        Log.e("SplashActivity", "toForeGround");
        Log.e("SplashActivity", act.getClass().getName());
        if (act.getClass() == SplashActivity.class
                || act.getClass().getName().equalsIgnoreCase("com.donews.notify.launcher.NotifyActivity")
                || act.getClass().getName().equalsIgnoreCase("com.donews.notify.launcher.NotifyActionActivity")
                || act.getClass().getName().equalsIgnoreCase("com.donews.keepalive.DazzleActivity")) {
            return; //??????????????????????????????????????????????????????????????????????????????????????????????????????
        }
        Intent intent = new Intent(act, SplashActivity.class);
        intent.putExtra(toForeGroundKey, true);
        act.startActivity(intent);
    }


    /**
     * ????????????code
     */
    private static final int REQUEST_PERMISSIONS_CODE = 1024;

    //??????????????????????????????????????????????????????????????????????????????
    private static final String toForeGroundKey = "toForeGround";

    private PersonGuideDialog personGuideDialog;
    //??????????????????????????????
    public static final long PROGRESS_DURATION = 10 * 1000;
    //??????????????????????????????:??????????????????????????????????????????????????????T:?????????F:?????????????????????
    private boolean mIsBackgroundToFore = false;

    private ValueAnimator mLoadAdAnimator;

    private int mNetworkIsAvailable = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.main_activity_splash;
    }

    @Override
    public void initView() {
        AppStatusManager.getInstance().setAppStatus(AppStatusConstant.STATUS_FORCE_KILLED);
        mIsBackgroundToFore = getIntent().getBooleanExtra(toForeGroundKey, false);
        EventBus.getDefault().register(this);

        init();

        mDataBinding.splashNetworkErrBtn.setOnClickListener(v -> init());
        mDataBinding.splashNetworkErrCheck.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mDataBinding.splashNetworkErrCheck.setOnClickListener(v -> {
            if (mNetworkIsAvailable == 2) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else if (mNetworkIsAvailable == 1) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
    }

    private void init() {
        if (checkNetWork() != 0) {
            return;
        }
        showPersonGuideDialog();
        if (mIsBackgroundToFore) {
            goToMain();
        } else {
            //?????????,???app?????????????????????
            SmSdkConfig.initData(UtilsConfig.getApplication());
            //??????????????????
            checkDeal();
        }

        ABSwitch.Ins().addCallBack(new ABSwitch.CallBack() {
            @Override
            public void onSuccess() {
                if (ABSwitch.Ins().isShowSplashScaleBtn()) {
                    mDataBinding.splashScaleTv.setVisibility(View.VISIBLE);
                    startGuideViewAnim();
                } else {
                    mDataBinding.splashScaleTv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFail() {
                if (ABSwitch.Ins().isShowSplashScaleBtn()) {
                    mDataBinding.splashScaleTv.setVisibility(View.VISIBLE);
                    startGuideViewAnim();
                } else {
                    mDataBinding.splashScaleTv.setVisibility(View.GONE);
                }
            }
        });
    }

    private final Handler mAnimatorHandler = new Handler(Looper.getMainLooper());
    private ObjectAnimator mGuideViewAnim;

    private void startGuideViewAnim() {
        cancelGuideAnim();
        mGuideViewAnim = ObjectAnimator.ofFloat(mDataBinding.splashScaleTv, "rotation", 0, 3f, 0, -3f, 0);
        mGuideViewAnim.setInterpolator(new LinearInterpolator());
        mGuideViewAnim.setRepeatMode(ValueAnimator.RESTART);
        mGuideViewAnim.setRepeatCount(2);
        mGuideViewAnim.setDuration(150);
        mGuideViewAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimatorHandler.removeCallbacksAndMessages(null);
                mAnimatorHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startGuideViewAnim();
                    }
                }, 500);
            }
        });
        mGuideViewAnim.start();
    }


    private void cancelGuideAnim() {
        if (mGuideViewAnim != null) {
            mGuideViewAnim.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("SplashActivity", "onResume");
        if (mNetworkIsAvailable != 0) {
            init();
        } else {
            showPersonGuideDialog();
        }
    }

    @Subscribe //????????????????????????
    public void eventNetworkChanage(NetworkChanageEvnet event) {
        LogUtils.v("??????????????????????????????" + event.getType());
    }

    @Subscribe //?????????????????????
    public void eventUserLogin(LoginUserStatus event) {
        LogUtils.v("?????????????????????" + event.getStatus());
        if (event.getStatus() != 1 && LoginHelp.getInstance().isLogin()) {
            //??????????????????????????????????????????????????????????????????????????????????????????
            SplashUtils.INSTANCE.openLoginDeviceNotify();
        }
    }

    @Override
    protected void onDestroy() {
        Log.e("SplashActivity", "onDestroy");
        //??????????????????????????????????????????
//        SplashUtils.INSTANCE.setColdStart(false);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    /**
     * ??????????????????????????????
     */
    private void checkDeal() {
        if (mNetworkIsAvailable != 0) {
            return;
        }
        if (SPUtils.getInformain(KeySharePreferences.DEAL, false)) {
            initSdk();
            return;
        }
        //???????????????????????????????????????????????????app
        if (SPUtils.getInformain(KeySharePreferences.DEAL, false)) {
            checkAndRequestPermission();
        }
    }

    private void initPushAndDnDi() {
        AnalysisHelp.setAnalysisInitUmeng(getApplication());
        if (!AnalysisHelp.analysisRegister) {
            AnalysisHelp.register(getApplication());
        }
    }

    private boolean mIsInitSdk = false;

    private void initSdk() {
        if (mIsInitSdk || mNetworkIsAvailable != 0) {
            return;
        }
        mIsInitSdk = true;

        DnSdkInit.INSTANCE.init(this.getApplication());

        SplashUtils.INSTANCE.savePersonExit(true);
        SPUtils.setInformain(KeySharePreferences.DEAL, true);
        SPUtils.setInformain(KeySharePreferences.AGREEMENT, true);
        //??????????????????/??????sdk
        initPushAndDnDi();
        checkAndRequestPermission();
    }

    private void showPersonGuideDialog() {
        if (personGuideDialog != null && personGuideDialog.isAdded() && personGuideDialog.isVisible()) {
            return;
        }

        //????????????????????????????????????????????????
        if (SPUtils.getInformain(KeySharePreferences.DEAL, false)) {
            initSdk();
            return;
        }
        if (personGuideDialog == null) {
            Logger.d("personGuideDialog no isAdded");
            personGuideDialog = new PersonGuideDialog();
            personGuideDialog.setSureListener(this::initSdk).setCancelListener(() -> {
                loadDisagreePrivacyPolicyAd();
                moveTaskToBack(true);
                personGuideDialog.dismiss();
            }).show(getSupportFragmentManager(), "PersonGuideDialog");
        }
    }

    /**
     * ?????????????????????
     */
    private void loadClodStartAd() {
        startProgressAnim();
        if (!AdConfigManager.INSTANCE.getMNormalAdBean().getEnable()) {
            goToMain();
            return;
        }

        setHalfScreen();

        loadSplash();
    }

    /**
     * ????????????
     */
    private void loadSplash() {
        IAdSplashListener listener = new SimpleSplashListener() {
            @Override
            public void onAdShow() {
                super.onAdShow();
                stopProgressAnim();
            }

            @Override
            public void onAdError(int code, @Nullable String errorMsg) {
                super.onAdError(code, errorMsg);
                Logger.d("code = " + code + "  msg = " + errorMsg);
//                goToMain();
                loadCsjSplash(true, false);
            }

            @Override
            public void onAdDismiss() {
                super.onAdDismiss();
                goToMain();
            }
        };

        SplashAd.INSTANCE.loadSplashAd(this, false, mDataBinding.adHalfScreenContainer, listener, true);
    }

    private void loadCsjSplash(boolean halfScreen, boolean hotStart) {
        IAdSplashListener listener = new SimpleSplashListener() {
            @Override
            public void onAdShow() {
                super.onAdShow();
                stopProgressAnim();
            }

            @Override
            public void onAdError(int code, @Nullable String errorMsg) {
                super.onAdError(code, errorMsg);
                Logger.d("code = " + code + "  msg = " + errorMsg);
                goToMain();
            }

            @Override
            public void onAdDismiss() {
                super.onAdDismiss();
                goToMain();
            }
        };

        SplashAd.INSTANCE.loadCsjSplashAd(this, hotStart, mDataBinding.adHalfScreenContainer, listener, halfScreen);
    }

    //??????????????????
    private void setHalfScreen() {
        mDataBinding.adHalfScreenContainer.setVisibility(View.VISIBLE);
        mDataBinding.adFullScreenContainer.setVisibility(View.GONE);
    }

    private long mPreClickTime = 0;

    private void loadDisagreePrivacyPolicyAd() {
        long curClickTime = System.currentTimeMillis();
        if (curClickTime - mPreClickTime < 2000) {
            mPreClickTime = curClickTime;
            Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
            return;
        }
        mPreClickTime = curClickTime;

        startProgressAnim();
        AdControlBean configBean = AdControlManager.INSTANCE.getAdControlBean();
        if (configBean.getDisagreePrivacyPolicyAdEnable()) {
            if (configBean.getDisagreePrivacyPolicyAdType() == 1) {
                loadDisagreePrivacyPolicyInters();
            } else {
                loadDisagreePrivacyPolicyRewardVideo();
            }
        } else {
            finish();
        }
    }

    private void loadDisagreePrivacyPolicyInters() {
        InterstitialFullAd.INSTANCE.showAd(this, new SimpleInterstitialFullListener() {
            @Override
            public void onAdError(int errorCode, @NonNull String errprMsg) {
                super.onAdError(errorCode, errprMsg);
                finish();
            }

            @Override
            public void onAdShow() {
                super.onAdShow();
                stopProgressAnim();
            }

            @Override
            public void onAdClose() {
                super.onAdClose();
                finish();
            }
        });
    }

    private void loadDisagreePrivacyPolicyRewardVideo() {
        RewardVideoAd.INSTANCE.loadRewardVideoAd(this, new SimpleRewardVideoListener() {

            @Override
            public void onAdError(int code, @Nullable String errorMsg) {
                super.onAdError(code, errorMsg);
                finish();
            }

            @Override
            public void onAdShow() {
                super.onAdShow();
                stopProgressAnim();
            }

            @Override
            public void onRewardVerify(boolean result) {
                super.onRewardVerify(result);
                finish();
            }
        }, false);
    }

    private int checkNetWork() {
        if (!NetworkUtils.isConnected()) {
            mNetworkIsAvailable = 1;
        } else {
            if (!NetworkUtils.isAvailableByPing()) {
                mNetworkIsAvailable = 2;
            } else {
                mNetworkIsAvailable = 0;
                mDataBinding.splashNetworkError.setVisibility(View.GONE);
            }
        }

        if (mNetworkIsAvailable != 0) {
            ToastUtil.showShort(this, "??????????????????????????????????????????");
            mDataBinding.splashNetworkError.setVisibility(View.VISIBLE);
            stopProgressAnim();
        } else {
            mDataBinding.splashNetworkError.setVisibility(View.GONE);
        }
        return mNetworkIsAvailable;
    }

    private void goToMain() {
        stopProgressAnim();
        cancelGuideAnim();
        mDataBinding.splashScaleTv.clearAnimation();

        if (checkNetWork() != 0) {
            return;
        }

        if (!mIsBackgroundToFore) {
            if (AppStatusManager.getInstance().getAppStatus() != AppStatusConstant.STATUS_NORMAL) {
                LotteryAdCheck.INSTANCE.init();
                GuideActivity.start(this);
            }
        } else {
            if (AppManager.getInstance().getActivity(MainActivity.class) == null) {
                MainActivity.start(this);
            }
        }
        mHadPermissions = false;
        finish();
    }

    private void deviceLogin() {
        //????????????
        SplashUtils.INSTANCE.loginDevice();
    }

    private void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> lackedPermission = new ArrayList<String>();
            if ((checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.INTERNET);
            }

            if ((checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.READ_PHONE_STATE);
            }

            if ((checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            // ??????????????????????????????????????????SDK
            if (lackedPermission.size() == 0) {
                hadPermissions();
            } else {
                String[] requestPermissions = new String[lackedPermission.size()];
                lackedPermission.toArray(requestPermissions);
                requestPermissions(requestPermissions, REQUEST_PERMISSIONS_CODE);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //??????????????????????????????????????????????????????????????????
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            hadPermissions();
        }
    }

    private boolean mHadPermissions = false;

    private void hadPermissions() {
        if (mHadPermissions) {
            return;
        }

        // ????????????,by dw
        AdConfigManager.INSTANCE.init();
        //????????????????????????
        ABSwitch.Ins().init();
        //????????????????????????
        TurntableSwitch.Ins().init();
        //???????????????
        FrontConfigManager.Ins().init();
        //??????????????????????????????
        BaseMiddleViewModel.getBaseViewModel().getDailyTasks(null);
        mHadPermissions = true;
        if ((SPUtils.getInformain(KeySharePreferences.IS_FIRST_IN_APP, 0) <= 0 && ABSwitch.Ins().isSkipSplashAd4NewUser())
                || !NetworkUtils.isAvailableByPing()) {
            LogUtil.e("hadPermissions()??? gomain");
            goToMain();
        } else {
            LogUtil.e("hadPermissions()??? load ad");
            deviceLogin();
            loadClodStartAd();
        }

        YfDcHelper.setSuuid(DeviceUtils.getMyUUID());
        YfDcHelper.onDeviceEvent();
    }

    private boolean hasAllPermissionsGranted(int[] grantResults) {
        if (grantResults == null) {
            return true;
        }
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }


    private void startProgressAnim() {
        if (mLoadAdAnimator != null) {
            mLoadAdAnimator.cancel();
        }
        if (mDataBinding != null) {
            mDataBinding.pbProgress.setProgress(0);
        }

        mLoadAdAnimator = ValueAnimator.ofInt(0, 100);
        mLoadAdAnimator.setDuration(PROGRESS_DURATION);
        mLoadAdAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                if (mDataBinding != null) {
                    if (mDataBinding.groupProgress.getVisibility() != View.VISIBLE) {
//                        mDataBinding.groupProgress.setVisibility(View.VISIBLE);
                    }
                    mDataBinding.pbProgress.setProgress(progress);
                }
            }
        });
        mLoadAdAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        mLoadAdAnimator.start();
    }

    private void stopProgressAnim() {
        if (!this.isFinishing()) {
            runOnUiThread(() -> {
                try {
                    if (mLoadAdAnimator != null && mLoadAdAnimator.isRunning()) {
                        mLoadAdAnimator.cancel();
                    }
                    mLoadAdAnimator = null;
                } catch (Exception e) {
                }
            });
        }
    }
}
