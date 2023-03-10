package com.module.lottery.ui;

import static com.donews.common.config.CritParameterConfig.CRIT_STATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingParent2;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieOnCompositionLoadedListener;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.SimpleLottieValueCallback;
import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.dn.events.events.LotteryBackEvent;
import com.dn.events.events.LotteryStatusEvent;
import com.dn.integral.jdd.integral.ProxyIntegral;
import com.donews.base.utils.ToastUtil;
import com.donews.middle.bean.LotteryEventUnlockBean;
import com.donews.middle.utils.JsonValueListUtils;
import com.donews.middle.utils.PlayAdUtilsTool;
import com.donews.utilslibrary.utils.KeySharePreferences;
import com.donews.yfsdk.manager.AdConfigManager;
import com.donews.yfsdk.monitor.LotteryAdCheck;
import com.donews.common.bean.CritMessengerBean;
import com.donews.common.provider.IDetailProvider;
import com.donews.common.router.RouterActivityPath;
import com.donews.common.router.RouterFragmentPath;
import com.donews.middle.centralDeploy.ABSwitch;
import com.donews.middle.bean.RedEnvelopeUnlockBean;
import com.donews.middle.utils.CommonUtils;
import com.donews.middle.utils.CriticalModelTool;
import com.donews.utilslibrary.analysis.AnalysisUtils;
import com.donews.utilslibrary.dot.Dot;
import com.donews.utilslibrary.utils.DateManager;
import com.donews.utilslibrary.utils.SPUtils;
import com.module.lottery.adapter.GuessAdapter;
import com.module.lottery.bean.CommodityBean;
import com.module.lottery.bean.CritCodeBean;
import com.module.lottery.bean.GenerateCodeBean;
import com.module.lottery.bean.LotteryCodeBean;
import com.module.lottery.dialog.CongratulationsDialog;
import com.module.lottery.dialog.ExhibitCodeStartsDialog;
import com.module.lottery.dialog.GenerateCodeDialog;
import com.module.lottery.dialog.LessMaxDialog;
import com.module.lottery.dialog.LotteryCodeStartsDialog;
import com.module.lottery.dialog.LotteryCritCodeDialog;
import com.module.lottery.dialog.LotteryCritCommodityDialog;
import com.module.lottery.dialog.LotteryCritOverDialog;
import com.module.lottery.dialog.NotDrawnDialog;
import com.module.lottery.dialog.ReceiveLotteryDialog;
import com.module.lottery.dialog.UnlockMaxCodeDialog;
import com.module.lottery.model.LotteryModel;
import com.module.lottery.utils.ClickDoubleUtil;
import com.module.lottery.utils.GridSpaceItemDecoration;
import com.module.lottery.viewModel.LotteryViewModel;
import com.module_lottery.R;
import com.module_lottery.databinding.GuesslikeHeadLayoutBinding;
import com.module_lottery.databinding.LotteryMainLayoutBinding;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * ????????????
 *
 * @author hegai
 * @version v1.0
 * @date 2021/12/8
 */
@Route(path = RouterFragmentPath.Lottery.PAGER_LOTTERY)
public class LotteryActivity extends BaseActivity<LotteryMainLayoutBinding, LotteryViewModel> {
    private static final String TAG = "LotteryActivity";
    private static final String LOTTERY_ACTIVITY = "LOTTERY_ACTIVITY";
    private static final String FIRST_SHOW = "first_show";
    private static final String LOTTERY_ROUND = "lottery_round.json";
    private static final String CRIT_ROUND = "cruel_time.json";
    private static final String CRITICAL_BT_TITLE_0 = "??????";
    private static final String CRITICAL_BT_TITLE_1 = "??????";
    private static final String CRITICAL_BT_TITLE_2 = "????????????????????????X6";
    private static final String CRITICAL_BT_TITLE_3 = "???????????????????????????";
    private static final String CRITICAL_BT_TITLE_4 = "????????????????????????????????????";
    private static final String CRITICAL_BT_TITLE_5 = "??????";
    @Autowired(name = "goods_id")
    public String mGoodsId;
    private SharedPreferences mSharedPreferences;
    // ???????????????????????? true ???????????????????????? false  ???????????????
    @Autowired(name = "start_lottery")
    public boolean mStart_lottery = false;
    //?????????????????? ??????????????????
    @Autowired(name = "privilege")
    public boolean privilege = false;


    @Autowired(name = "position")
    int mPosition;
    @Autowired(name = "needLotteryEvent")
    public boolean mMeedLotteryEvent;
    //    String id = "tb:655412572200";
    public GuessAdapter guessAdapter;
    @Autowired(name = "action")
    public String mAction;
    private CommodityBean mCommodityBean;
    //????????????????????????????????????
    private LotteryCodeBean mLotteryCodeBean;
    private boolean dialogShow = false;
    @Autowired(name = RouterActivityPath.GoodsDetail.GOODS_DETAIL_PROVIDER)
    public IDetailProvider detailProvider;
    PlayAdUtilsTool mPlayAdUtilsTool;


    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    private boolean mCritTime;

    @Override
    protected int getLayoutId() {
        return R.layout.lottery_main_layout;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = this.getSharedPreferences(LOTTERY_ACTIVITY, 0);
        ARouter.getInstance().inject(this);
        EventBus.getDefault().register(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        mPlayAdUtilsTool = PlayAdUtilsTool.getInstance();
        guessAdapter = new GuessAdapter(LotteryActivity.this);
        guessAdapter.getLayout(R.layout.guesslike_item_layout);
        mDataBinding.lotteryGridview.setLayoutManager(gridLayoutManager);
        mDataBinding.lotteryGridview.setAdapter(guessAdapter);
        mDataBinding.lotteryGridview.addItemDecoration(new GridSpaceItemDecoration(1));
        mDataBinding.lotteryGridview.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        mDataBinding.returnIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnIntercept();
            }
        });
        mDataBinding.maskingLayout.setOnClickListener(v -> {
            mDataBinding.maskingLayout.setVisibility(View.GONE);
            SPUtils.getInformain(FIRST_SHOW, false);
            SPUtils.setInformain(KeySharePreferences.KEY_NEW_USER_MODEL_DIALOG_SHOW_LOTTERY_OPEN, true);
        });
        initViewData();
        setHeaderView(mDataBinding.lotteryGridview);
        setObserveList();
        //??????????????????????????????
        lotteryInfo();
        //??????????????????
        if (mStart_lottery && ABSwitch.Ins().isOpenAutoLottery() || privilege) {
            ifOpenAutoLotteryAndCount();
        }
        mStart_lottery = false;

    }


    int mLastTimeRandomId = -1;

    /**
     *
     */
    public void jumpUrl(List<String> urlList, int urlValueId, String typePath) {
        Uri uri = null;
        String rulValue = "";

        if (urlList != null) {
            if (urlValueId == -1) {
                //??????
                int id = getRandomNumber(urlList);
                rulValue = urlList.get(id);
                uri = Uri.parse(rulValue);// ????????????
            } else {
                //????????????
                if (urlValueId < urlList.size()) {
                    rulValue = urlList.get(urlValueId);
                    uri = Uri.parse(rulValue);// ????????????
                }
            }
        }
        if (uri != null) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                if (typePath.equals("1")) {
                    //????????????
                }
                if (typePath.equals("2")) {
                    //????????????
                }
            } catch (Exception e) {
                if (rulValue != null && !rulValue.equals("")) {
                    String splitValueList[] = rulValue.split("//");
                    if (splitValueList != null && splitValueList.length >= 2) {
                        String url = "https://" + splitValueList[1];
                        uri = Uri.parse(url);// ????????????
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        if (typePath.equals("1")) {
                            //????????????
                        }
                        if (typePath.equals("2")) {
                            //????????????
                        }
                    }
                }
            }
        }
    }


    /**
     * ?????????????????????????????????id
     */
    private int getRandomNumber(List<String> urlList) {
        if (urlList != null && urlList.size() > 1) {
            //??????Intent.ACTION_SCREEN_ON
            Random rand = new Random();
            int id = rand.nextInt(urlList.size());
            if (mLastTimeRandomId == id) {
                getRandomNumber(urlList);
            } else {
                mLastTimeRandomId = id;
                return id;
            }
        }
        return 0;
    }


    private void showLotteryCritCodeDialog(GenerateCodeBean generateCodeBean) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LotteryCritCodeDialog lotteryCritDialog = new LotteryCritCodeDialog(LotteryActivity.this,
                        generateCodeBean);
                lotteryCritDialog.setStateListener(new LotteryCritCodeDialog.OnStateListener() {
                    @Override
                    public void onStartCrit() {
                        if (lotteryCritDialog != null && lotteryCritDialog.isShowing()) {
                            lotteryCritDialog.dismiss();
                        }
                        luckyDrawEntrance();
                    }
                });
                lotteryCritDialog.show(LotteryActivity.this);
            }
        });
    }


    private void showLotteryCritCommodityDialog() {
        LotteryCritCommodityDialog lotteryCritDialog = new LotteryCritCommodityDialog(mGoodsId, this);
        lotteryCritDialog.setOwnerActivity(this);
        lotteryCritDialog.show(this);
    }


    private void showLotteryCritOverDialog() {
        LotteryCritOverDialog lotteryCritDialog = new LotteryCritOverDialog(mGoodsId, this);

        lotteryCritDialog.setStateListener(new LotteryCritOverDialog.OnStateListener() {
            @Override
            public void onFinish() {
                if (lotteryCritDialog != null && lotteryCritDialog.isShowing()) {
                    lotteryCritDialog.dismiss();
                }
            }

            @Override
            public void onCritJump(CritCodeBean critCodeBean) {
                //????????????  ???????????????
                lotteryInfo();
                showUnlockMaxCodeDialog(critCodeBean);
            }
        });

        lotteryCritDialog.setOwnerActivity(this);
        lotteryCritDialog.show(this);
    }


    private void showUnlockMaxCodeDialog(CritCodeBean critCodeBean) {
        UnlockMaxCodeDialog UnlockMaxCodeDialog = new UnlockMaxCodeDialog(mLotteryCodeBean, critCodeBean, mGoodsId,
                this);
        UnlockMaxCodeDialog.setOwnerActivity(this);
        UnlockMaxCodeDialog.show(this);

    }

    /**
     * ??????????????????????????????,???????????????????????????????????????
     */
    private void ifOpenAutoLotteryAndCount() {
        if (privilege) {
            luckyDrawEntrance();
            return;
        }
        if (ABSwitch.Ins().isOpenAutoLottery()) {
            //???????????????????????????
            int configurationCount = ABSwitch.Ins().getOpenAutoLotteryCount();
            //????????????????????????
            int frequency = DateManager.getInstance().getLotteryCount(DateManager.LOTTERY_COUNT);
            if (frequency >= configurationCount) {
                luckyDrawEntrance();
            }
        }
    }


    /**
     * ????????????
     * ??????????????????0????????? ???????????????????????????
     */
    private void ifDialogAutomaticDraw() {
        //??????????????????
        if (ABSwitch.Ins().isOpenAutoLottery()) {
            luckyDrawEntrance();
        }
    }


    /**
     * ???????????? ???????????????
     * ??????????????????0????????? ???????????????????????????
     */
    private void ifInterceptDialogAutomaticDraw() {
        //??????????????????
        if (ABSwitch.Ins().isOpenAutoLottery()) {
            luckyDrawEntrance();
        }
    }


    public void luckyDrawEntrance() {
        if (mLotteryCodeBean != null && mLotteryCodeBean.getCodes().size() >= 6) {
            return;
        }
        //????????????????????????
        startLottery();
    }

    RotateAnimation mRotateAnimation;

    private void initViewData() {

        //?????? ??????????????????tips ??????
        if (mRotateAnimation == null) {
            mRotateAnimation = new RotateAnimation(0, 8, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            mRotateAnimation.setInterpolator(new CycleInterpolator(2));
            mRotateAnimation.setRepeatMode(Animation.REVERSE);
            mRotateAnimation.setRepeatCount(Animation.INFINITE);
            mRotateAnimation.setStartOffset(2500);
            mRotateAnimation.setRepeatMode(Animation.REVERSE);
            mRotateAnimation.setDuration(400);
        }
        mDataBinding.lotteryTips.setAnimation(mRotateAnimation);
        mDataBinding.jsonAnimationRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ClickDoubleUtil.isFastClick()) {
                    luckyDrawEntrance();
                }
            }
        });


        //????????????????????????6?????????????????????
        if (mLotteryCodeBean != null && mLotteryCodeBean.getCodes().size() >= 5) {
            setLottieAnimation(false);
        } else {
            setLottieAnimation(true);
        }
        mSharedPreferences.edit().putBoolean(FIRST_SHOW, false).apply();

        boolean first_show = SPUtils.getInformain(FIRST_SHOW, true);
        if (first_show && !privilege) {
            SPUtils.setInformain(FIRST_SHOW, false);
            mDataBinding.maskingLayout.setVisibility(View.VISIBLE);
            //??? ?????????????????????
            initLottie(mDataBinding.maskingButton, "lottery_round.json");
            //?????? ?????????????????????
            initLottie(mDataBinding.maskingHand, JsonValueListUtils.LOTTERY_FINGER);
        } else {
            mDataBinding.maskingLayout.setVisibility(View.GONE);
        }
    }

    private void setLottieAnimation(boolean play) {
        if (play) {
            //????????????
            //??????
            initLottie(mDataBinding.jsonAnimation, JsonValueListUtils.LOTTERY_FINGER);
            mDataBinding.jsonAnimation.setVisibility(View.VISIBLE);
            //???
            if (CriticalModelTool.ifCriticalStrike()) {
                mDataBinding.tips.setText(CRITICAL_BT_TITLE_2);
                mDataBinding.label01.setText(CRITICAL_BT_TITLE_0);
                mDataBinding.jsonAnimationRound.clearAnimation();
                initLottie(mDataBinding.jsonAnimationRound, CRIT_ROUND);
            } else {
                mDataBinding.tips.setText(CRITICAL_BT_TITLE_4);
                initLottie(mDataBinding.jsonAnimationRound, LOTTERY_ROUND);
            }

        } else {
            mDataBinding.jsonAnimation.setVisibility(View.GONE);
            mDataBinding.jsonAnimationRound.pauseAnimation();
            mDataBinding.jsonAnimationRound.setProgress(0);
        }
    }

    private void initLottie(LottieAnimationView view, String json) {
        if ((view != null && !view.isAnimating())) {
            view.setImageAssetsFolder("images");
            view.clearAnimation();
            view.setAnimation(json);
            view.loop(true);
            view.playAnimation();
        }
    }

    //????????????
    private void startLottery() {
        //?????????????????????????????????
        if (AdConfigManager.INSTANCE.getMNormalAdBean().getEnable()) {
            //??????????????????
            //????????????????????????
            mCritTime = CriticalModelTool.ifCriticalStrike();

            //????????????
            //?????????????????????dialog
            LotteryCodeStartsDialog lotteryCodeStartsDialog = new LotteryCodeStartsDialog(LotteryActivity.this);
            lotteryCodeStartsDialog.setStateListener(new LotteryCodeStartsDialog.OnStateListener() {
                @Override
                public void onLoadAd() {
                    loadAdAndStatusCallback(lotteryCodeStartsDialog);
                }
            });
            lotteryCodeStartsDialog.create();
            lotteryCodeStartsDialog.show(LotteryActivity.this);
        } else {
            //????????????????????????
            if (DateManager.getInstance().timesLimit(DateManager.LOTTERY_KEY, DateManager.NUMBER_OF_DRAWS, 24)) {
                generateLotteryCode();
            } else {
                ToastUtil.showShort(this, "????????????????????????????????????");
            }
        }

    }


    /**
     * ?????????????????????????????????????????????????????????
     */

    private void loadAdAndStatusCallback(final Dialog dialog) {
        if (mPlayAdUtilsTool != null) {
            mPlayAdUtilsTool.showRewardVideo(this);
            mPlayAdUtilsTool.setIStateListener(new PlayAdUtilsTool.IStateListener() {
                @Override
                public void onComplete() {
                    //??????????????????????????????????????????????????????
                    Logger.d(TAG + "showGenerateCodeDialog");
                    if (ABSwitch.Ins().getOpenCritModel()) {
                        //??????????????????0???
                        int number = LotteryAdCheck.INSTANCE.getCriticalModelLotteryNumber();
                        if (number == 0) {
                            Logger.d(TAG + "???????????????????????????????????????????????????");
                            //?????????????????????????????????
                            if (ABSwitch.Ins().isOpenScoreModelCrit() && !CriticalModelTool.isNewUser()) {
                                CriticalModelTool.getScenesSwitch(new CriticalModelTool.IScenesSwitchListener() {
                                    @Override
                                    public void onIntegralNumber(ProxyIntegral integralBean) {
                                        if (integralBean == null) {
                                            //??????????????????????????????
                                            addCriticalLotteryNumber();
                                            Logger.d(TAG + "???????????????????????? ???????????????");
                                        } else {
                                            Logger.d(TAG + "???????????????????????????");
                                            LotteryAdCheck.INSTANCE.resetCriticalModelNumber();
                                        }

                                    }
                                });
                            } else if (CriticalModelTool.isNewUser()) {
                                addCriticalLotteryNumber();
                            }
                        } else {
                            //??????????????????????????????
                            Logger.d(TAG + "???????????????????????? ????????????????????????");
                            addCriticalLotteryNumber();
                        }
                    }
                    generateLotteryCode();
                }

                @Override
                public void onFinish() {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }

                @Override
                public void onError(int code, @Nullable String errorMsg) {

                }
            });
        }
    }

    private void addCriticalLotteryNumber() {
        if (SPUtils.getInformain(CRIT_STATE,
                0) == 0 && !mCritTime && ABSwitch.Ins().getOpenCritModel() && DateManager.getInstance().isAllowCritical()) {
            LotteryAdCheck.INSTANCE.putCriticalModelLotteryNumber();
        }
    }


    //???????????????
    private void generateLotteryCode() {
        if (mCritTime) {
            //??????????????????????????????
            showLotteryCritOverDialog();
        } else {
            showGenerateCodeDialog();
        }

    }


    //??????????????????Dialog
    private void showGenerateCodeDialog() {
        try {
            GenerateCodeDialog generateCodeDialog = new GenerateCodeDialog(LotteryActivity.this, mGoodsId);
            generateCodeDialog.setStateListener(new GenerateCodeDialog.OnStateListener() {
                @Override
                public void onFinish() {
                    privilege = false;
                    if (generateCodeDialog != null && !LotteryActivity.this.isFinishing()) {
                        generateCodeDialog.dismiss();
                    }
                }

                @Override
                public void onJump(GenerateCodeBean generateCodeBean) {
                    if (generateCodeBean == null) {
                        privilege = false;
                        Toast.makeText(LotteryActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                        //??????????????? ??????????????????????????????
                        if (generateCodeDialog != null && !LotteryActivity.this.isFinishing()) {
                            generateCodeDialog.dismiss();
                        }
                        return;
                    }
                    //????????????  ???????????????
                    lotteryInfo();
                    //?????????????????????????????????
                    if (ABSwitch.Ins().getOpenCritModel()) {
                        //??????????????????
                        if (DateManager.getInstance().isAllowCritical()) {
                            taskJudgment(generateCodeBean, generateCodeDialog);
                        } else {
                            //???????????????????????????
                            ordinaryTask(generateCodeBean, generateCodeDialog);
                        }
                    } else {
                        //????????????????????????
                        ordinaryTask(generateCodeBean, generateCodeDialog);
                    }
                }

//                @Override
//                public void onExclusiveBulletFrame() {
//                    if (generateCodeDialog != null && !LotteryActivity.this.isFinishing()) {
//                        generateCodeDialog.dismiss();
//
//                    }
//                }

            });
            generateCodeDialog.create();
            if (!LotteryActivity.this.isFinishing()) {
                generateCodeDialog.show(LotteryActivity.this);
            }
        } catch (Exception e) {
            Logger.e("" + e.getMessage());
        }
    }


    /**
     * ????????????????????????????????????
     */
    private void taskJudgment(GenerateCodeBean generateCodeBean, GenerateCodeDialog generateCodeDialog) {

        //?????????????????????????????????
        if (ABSwitch.Ins().isOpenScoreModelCrit() && !CriticalModelTool.isNewUser()) {
            //???????????????????????????
            CriticalModelTool.getScenesSwitch(new CriticalModelTool.IScenesSwitchListener() {
                @Override
                public void onIntegralNumber(ProxyIntegral integralBean) {
                    int number = LotteryAdCheck.INSTANCE.getCriticalModelLotteryNumber();
                    //?????????
                    //??????????????? ??????????????????????????????
                    if (generateCodeDialog != null && !LotteryActivity.this.isFinishing()) {
                        generateCodeDialog.dismiss();
                    }
                    if (integralBean != null && number == 0) {
                        //??????????????????(???????????????)
                        showExhibitCodeDialog(generateCodeBean);
                    } else {
                        ordinaryTask(generateCodeBean, generateCodeDialog);
                    }
                }
            });
        } else {
            //?????????
            //??????????????? ??????????????????????????????
            if (generateCodeDialog != null && !LotteryActivity.this.isFinishing()) {
                generateCodeDialog.dismiss();
            }
            ordinaryTask(generateCodeBean, generateCodeDialog);
        }
    }


    //??????????????????
    private void ordinaryTask(GenerateCodeBean generateCodeBean, GenerateCodeDialog generateCodeDialog) {
        //??????????????? ??????????????????????????????
        if (generateCodeDialog != null && !LotteryActivity.this.isFinishing()) {
            generateCodeDialog.dismiss();
        }
        //???????????????????????? ???????????????
        if (CriticalModelTool.ifCoincide()) {
            //????????????????????????
            startCriticalMoment();
            //?????????????????????
            if (generateCodeBean.getRemain() == 0) {
                //??????6??????????????????????????????
                showExhibitCodeDialog(generateCodeBean);
            } else {
                //?????????????????????
                showLotteryCritCodeDialog(generateCodeBean);
            }
        } else {
            showExhibitCodeDialog(generateCodeBean);
        }

    }


    private void showLessMaxDialog() {
        LessMaxDialog lessMaxDialog = new LessMaxDialog(LotteryActivity.this, mLotteryCodeBean);
        lessMaxDialog.setFinishListener(new LessMaxDialog.OnFinishListener() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onFinishAd() {
                luckyDrawEntrance();
            }
        });
        lessMaxDialog.show(LotteryActivity.this);
    }


    private void showNotDrawnDialog() {
        NotDrawnDialog notDrawnDialog = new NotDrawnDialog(LotteryActivity.this, mLotteryCodeBean);
        notDrawnDialog.setFinishListener(new NotDrawnDialog.OnFinishListener() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onFinishAd() {
                luckyDrawEntrance();
            }
        });
        notDrawnDialog.show(LotteryActivity.this);
    }


    //??????????????????????????????Dialog
    private void showReceiveLotteryDialog(boolean ifQuit) {
        if (mLotteryCodeBean != null) {
            ReceiveLotteryDialog receiveLottery = new ReceiveLotteryDialog(LotteryActivity.this, mLotteryCodeBean,
                    ifQuit);
            receiveLottery.setStateListener(new ReceiveLotteryDialog.OnStateListener() {
                @Override
                public void onFinish() {
                }

                @Override
                public void onJumpAd() {


                }

                @Override
                public void onLottery() {
                    //????????????
                    startLottery();
                }
            });
            receiveLottery.create();
            receiveLottery.show(LotteryActivity.this);
        }
    }


    //??????6????????????????????? dialog
    private void showCongratulationsDialog() {
        CongratulationsDialog mNoDrawDialog = new CongratulationsDialog(LotteryActivity.this, mGoodsId);
        mNoDrawDialog.setFinishListener(new CongratulationsDialog.OnFinishListener() {
            @Override
            public void onFinish() {
                mNoDrawDialog.dismiss();
            }
        });
        mNoDrawDialog.create();
        mNoDrawDialog.show(LotteryActivity.this);
    }


    private void finishReturn() {
//        EventBus.getDefault().post(new LotteryBackEvent(1));
        if (!mMeedLotteryEvent) {
            return;
        }
        if (mLotteryCodeBean == null || mLotteryCodeBean.getCodes() == null) {
            return;
        }

        EventBus.getDefault().post(new LotteryStatusEvent(mPosition, mGoodsId, mLotteryCodeBean.getCodes()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mDataBinding.lotteryTips.clearAnimation();
        mCritTime = false;
        if (mPlayAdUtilsTool != null) {
            mPlayAdUtilsTool.setIStateListener(null);
            mPlayAdUtilsTool = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //
    @Override
    protected void onPause() {
        super.onPause();
    }

    //????????????????????????
    private void showExhibitCodeDialog(GenerateCodeBean generateCodeBean) {
        if (generateCodeBean == null) {
            privilege = false;
            Toast.makeText(LotteryActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
            return;
        }
        //????????????????????????
        //??????????????????
        EventBus.getDefault().post(new LotteryEventUnlockBean());
        ExhibitCodeStartsDialog exhibitCodeStartsDialog = new ExhibitCodeStartsDialog(LotteryActivity.this, mGoodsId,
                generateCodeBean);
        exhibitCodeStartsDialog.setOwnerActivity(LotteryActivity.this);
        exhibitCodeStartsDialog.setStateListener(new ExhibitCodeStartsDialog.OnStateListener() {
            @Override
            public void onFinish() {
                exhibitCodeStartsDialog.dismiss();
                //???????????????????????? ???????????????dialog
                //????????????6??????
                if (generateCodeBean != null) {
                    if (generateCodeBean.getRemain() == 0) {
                        if (mCritTime) {
                            showLotteryCritCommodityDialog();
                        } else {
                            //??????????????????????????????dialog
                            showCongratulationsDialog();
                        }
                    } else {
                        //?????????????????????dialog
                        showReceiveLotteryDialog(false);
                    }
                }
            }

            @Override
            public void onLottery() {
                //????????????
                startLottery();
            }

            @Override
            public void onChangeState() {
                mCritTime = true;
            }

            @Override
            public void onStartCritMode(GenerateCodeBean generateCodeBean, ProxyIntegral integralBean) {
                if (ClickDoubleUtil.isFastClick()) {
                    CommonUtils.startCritService(LotteryActivity.this, integralBean);
                }
            }
        });
        try {
            exhibitCodeStartsDialog.create();
            if (!this.isFinishing() && !this.isDestroyed()) {
                if (privilege) {
                    Toast.makeText(LotteryActivity.this, "???????????????", Toast.LENGTH_SHORT).show();
                    //??????????????????
                    EventBus.getDefault().post(new RedEnvelopeUnlockBean(200));
                    privilege = false;
                }
                exhibitCodeStartsDialog.show(LotteryActivity.this);
            }
        } catch (Exception ignored) {
        }
    }


    /**
     * ??????????????????????????????
     */
    private void startCriticalMoment() {
        CommonUtils.startCrit();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        privilege = false;
        mCritTime = false;
        setIntent(intent);
        ARouter.getInstance().inject(this);
        if (mAction != null && mAction.equals("newAction")) {
            clearState();
            //??????????????????????????????
            lotteryInfo();
        }
    }


    public void setObserveList() {
        //????????????????????????
        mViewModel.getMutableLiveData().observe(this, CommodityBean -> {
            // ????????????
            if (CommodityBean == null) {
                return;
            }
            //??????????????????
            guessAdapter.setCommodityBean(CommodityBean);
            guessAdapter.notifyDataSetChanged();
            mCommodityBean = CommodityBean;
            //?????????
            requestLotteryCode(CommodityBean.getPeriod());
            //????????????????????????????????????viewPager ??????
            winLotteryInfo();

        });
        //?????????
        mViewModel.getmLotteryCodeBean().observe(this, MaylikeBean -> {
            // ????????????
            if (MaylikeBean == null) {
                return;
            }
            mLotteryCodeBean = MaylikeBean;
            //????????????3+
            setButtonValue(mLotteryCodeBean);
            if (mLotteryCodeBean.getCodes() != null && mLotteryCodeBean.getCodes().size() >= 6) {
                ToastUtil.showShort(getApplicationContext(), "????????????????????????????????????????????????");
            }
            //????????????????????????
            if (MaylikeBean.getCodes() != null) {
                CommodityBean commodityBean = guessAdapter.getCommodityBean();
                commodityBean.setLotteryCodeBean(MaylikeBean);
                guessAdapter.notifyDataSetChanged();
            }
        });
        //??????????????????????????????
        mViewModel.getWinLotteryBean().observe(this, winLotteryBean -> {
            if (winLotteryBean != null) {
                guessAdapter.setScrollListData(winLotteryBean);
            }
        });
    }


    @Subscribe
    public void UnlockEvent(CritMessengerBean critMessenger) {
        //????????????
        if (critMessenger != null && critMessenger.mStatus == 300 || critMessenger.mStatus == 200) {
            if (mLotteryCodeBean != null) {
                mDataBinding.jsonAnimationRound.pauseAnimation();
                mDataBinding.jsonAnimationRound.clearAnimation();
                setButtonValue(mLotteryCodeBean);
            }
        }
    }


    //?????????????????????????????????
    private void setButtonValue(LotteryCodeBean lotteryCodeBean) {
        mDataBinding.tips.setVisibility(View.VISIBLE);
        //????????????==0 ??????0?????????
        if (lotteryCodeBean != null && lotteryCodeBean.getCodes().size() == 0) {
            mDataBinding.label02.setVisibility(View.VISIBLE);
            if (CriticalModelTool.ifCriticalStrike()) {
                mDataBinding.label01.setText(CRITICAL_BT_TITLE_0);
                mDataBinding.label02.setText(CRITICAL_BT_TITLE_1);
                mDataBinding.tips.setText(CRITICAL_BT_TITLE_2);
            } else {
                mDataBinding.label01.setText(CRITICAL_BT_TITLE_5);
                mDataBinding.label02.setText(CRITICAL_BT_TITLE_1);
                mDataBinding.tips.setText(CRITICAL_BT_TITLE_3);
            }
            setLottieAnimation(true);
        } else
            //??????????????????6??????1?????? ??????????????????
            if (lotteryCodeBean != null && lotteryCodeBean.getCodes().size() > 0 && lotteryCodeBean.getCodes().size() < 6) {
                mDataBinding.label02.setVisibility(View.VISIBLE);
                if (CriticalModelTool.ifCriticalStrike()) {
                    mDataBinding.label01.setText(CRITICAL_BT_TITLE_0);
                    mDataBinding.label02.setText(CRITICAL_BT_TITLE_1);
                    mDataBinding.tips.setText(CRITICAL_BT_TITLE_2);
                } else {
                    mDataBinding.label01.setText(getResources().getString(R.string.continue_value));
                    mDataBinding.label02.setText(getResources().getString(R.string.lottery_value));
                    mDataBinding.tips.setText(CRITICAL_BT_TITLE_4);
                }
                setLottieAnimation(true);
            } else
                //????????????????????????6?????????????????????
                if (lotteryCodeBean != null && lotteryCodeBean.getCodes().size() >= 6) {
                    mDataBinding.label01.setText(getResources().getString(R.string.wait_dollar_draw));
                    mDataBinding.label02.setVisibility(View.GONE);
                    mDataBinding.label02.setText("");
                    mDataBinding.tips.setText("??????10:00?????????");
                    //
                    setLottieAnimation(false);
                }
        final LottieAnimationView mirror = mDataBinding.jsonAnimationRound;
        if (mirror != null) {
            mirror.addLottieOnCompositionLoadedListener(new LottieOnCompositionLoadedListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onCompositionLoaded(LottieComposition composition) {
                    if (composition != null) {
                        setAnimationRoundColor(lotteryCodeBean, mirror);
                    }
                }
            });
        }
    }


    //????????????????????????
    @SuppressLint("RestrictedApi")
    private void setAnimationRoundColor(LotteryCodeBean lotteryCodeBean, LottieAnimationView mirror) {
        if (lotteryCodeBean == null || mirror == null) {
            return;
        }
        //???????????????keypath
        List<KeyPath> list = mirror.resolveKeyPath(
                new KeyPath("**"));
        //???????????????????????????????????????????????????????????????keypath
        for (KeyPath path : list) {
            if (path.keysToString().indexOf("btn_inner") != -1) {
                mirror.addValueCallback(path, LottieProperty.COLOR, new SimpleLottieValueCallback<Integer>() {
                    @Override
                    public Integer getValue(LottieFrameInfo<Integer> frameInfo) {
                        if (lotteryCodeBean != null && lotteryCodeBean.getCodes().size() >= 6) {
                            return getResources().getColor(R.color.policec_lick);
                        }
                        return getResources().getColor(R.color.policec_lick_red);
                    }

                });

            }
        }

    }


    @Override
    public void initView() {

    }


    //??????????????????????????????
    public void lotteryInfo() {
        Map<String, String> params = BaseParams.getMap();
        params.put("goods_id", mGoodsId);
        mViewModel.getNetLotteryData(LotteryModel.LOTTERY_GUESS_INFO, params);
    }


    //???????????????
    public void requestLotteryCode(int numberPeriods) {
        Map<String, String> params = BaseParams.getMap();
        params.put("goods_id", mGoodsId);
        params.put("period", numberPeriods + "");
        mViewModel.getLotteryCodeData(LotteryModel.LOTTERY_LOTTERY_CODE, params);
    }

    //?????????????????????
    private void setHeaderView(RecyclerView view) {
        GuesslikeHeadLayoutBinding guesslikeHeadLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.guesslike_head_layout, view, false);
        guessAdapter.setHeaderView(guesslikeHeadLayoutBinding);
    }


    //????????????????????????????????????????????????
    public void winLotteryInfo() {
        Map<String, String> params = BaseParams.getMap();
        params.put("goods_id", mGoodsId);
        mViewModel.getWinLotteryList(LotteryModel.LOTTERY_WIN_LOTTERY, params);
    }


    private void clearState() {
        mLotteryCodeBean = null;
    }


    private void returnIntercept() {
        if (CriticalModelTool.ifCriticalStrike()) {
            finishReturn();
            finish();
            return;
        }
        if (mLotteryCodeBean == null || dialogShow) {
            finishReturn();
            finish();
            return;
        }
        //??????????????????6??? ???????????????
        if (mLotteryCodeBean != null && mLotteryCodeBean.getCodes().size() < 6 && mLotteryCodeBean.getCodes().size() > 0) {
            dialogShow = true;
            //?????????????????????dialog
            showLessMaxDialog();
            return;
        }
        if (mLotteryCodeBean != null && mLotteryCodeBean.getCodes().size() == 0) {
            dialogShow = true;
            //???????????????
            showNotDrawnDialog();
            return;
        }

        //????????????????????????
        if (mLotteryCodeBean != null && mLotteryCodeBean.getCodes().size() >= 6) {
            finishReturn();
            finish();
            return;
        }
    }


    @SuppressLint("ResourceType")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            returnIntercept();
        }
        return true;
    }

}