package com.donews.home.dialogs;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.app.hubert.guide.NewbieGuide;
import com.app.hubert.guide.core.Controller;
import com.app.hubert.guide.listener.OnLayoutInflatedListener;
import com.app.hubert.guide.model.GuidePage;
import com.app.hubert.guide.model.HighLight;
import com.app.hubert.guide.model.HighlightOptions;
import com.app.hubert.guide.model.RelativeGuide;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ViewUtils;
import com.dn.sdk.listener.rewardvideo.SimpleRewardVideoListener;
import com.donews.base.utils.ToastUtil;
import com.donews.common.base.MvvmBaseLiveDataActivity;
import com.donews.common.router.RouterActivityPath;
import com.donews.common.router.RouterFragmentPath;
import com.donews.home.R;
import com.donews.home.databinding.HomeExchangeGoodNotDialogBinding;
import com.donews.home.viewModel.ExchangeViewModel;
import com.donews.home.viewModel.HomeViewModel;
import com.donews.middle.adutils.FeedNativeAndTemplateAd;
import com.donews.middle.bean.home.HomeCoinCritConfigBean;
import com.donews.middle.bean.home.HomeEarnCoinReq;
import com.donews.middle.dialog.BaseBindingFragmentDialog;
import com.donews.middle.dialog.qbn.DoingResultDialog;
import com.donews.middle.utils.ActivityGuideMaskUtil;
import com.donews.middle.viewmodel.BaseMiddleViewModel;
import com.donews.utilslibrary.utils.AppInfo;
import com.donews.yfsdk.loader.AdManager;

/**
 * ?????? -> ???????????? -> ?????????????????????
 */
@Route(path = RouterFragmentPath.Home.PAGER_EXCHANGE_GOOD_JB_FRAGMENT_DIALOG)
public class ExchangeGoodJbFragmentDialog extends BaseBindingFragmentDialog<HomeExchangeGoodNotDialogBinding> {

    /**
     * ??????????????????
     *
     * @param type  ?????? 0:?????????????????????1:??????????????????
     * @param diffe ????????????
     * @return
     */
    public static ExchangeGoodJbFragmentDialog getInstance(int type, int diffe) {
        ExchangeGoodJbFragmentDialog dialog = new ExchangeGoodJbFragmentDialog();
        if (dialog.getArguments() == null) {
            dialog.setArguments(new Bundle());
        }
        dialog.getArguments().putInt("uiType", type);
        dialog.getArguments().putInt("diffe", diffe);
        return dialog;
    }

    // ViewModel ??????
    private HomeViewModel homeViewModel;
    // ViewModel ??????
    private ExchangeViewModel exchanViewMovdel;

    public HomeCoinCritConfigBean config;

    private Activity act;
    private Handler handler = new Handler();

    // ?????? 0:?????????????????????1:??????????????????
    @Autowired(name = "uiType")
    public int modeType = -1;

    // ????????????
    @Autowired(name = "diffe")
    public int diffe = 0;

    public ExchangeGoodJbFragmentDialog() {
        super(R.layout.home_exchange_good_not_dialog);
    }

    /**
     * ???????????????
     */
    @Override
    protected void initView() {
        //??????ViewModel??????
        ARouter.getInstance().inject(this);
        if (modeType == -1) {
            //??????????????????
            modeType = getArguments().getInt("uiType", 0);
            diffe = getArguments().getInt("diffe", 0);
        }
        homeViewModel = createViewModel(getActivity(), HomeViewModel.class);
        exchanViewMovdel = createViewModel(getActivity(), ExchangeViewModel.class);
        if (modeType == 0) {
            config = exchanViewMovdel.querySaveCoinCritConfig();
        }
        initDatabinding();
        showClose();
        //?????????????????????
        showFeed();
    }

    //??????????????????
    private void showClose() {
        ValueAnimator alAm = ValueAnimator.ofFloat(0, 1);
        alAm.addUpdateListener(animation -> {
            float valu = (float) animation.getAnimatedValue();
            if (valu > 1) {
                dataBinding.tvClose.setAlpha(1);
                return;
            }
            dataBinding.tvClose.setAlpha(valu);
        });
        alAm.setDuration(3500).start();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        act = (Activity) context;
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        initDatabinding();

        handler.post(() -> {
            checkNewUserGuide(0);
        });
    }

    //?????????????????????Text
    public CharSequence getStatusText() {
        if (modeType == 0) {
            //????????????
            if (config != null && config.open) {
                return Html.fromHtml("??????" + config.open_times + "???????????????<font color='#F5562A'>?????????????????????</font>");
            } else {
                return Html.fromHtml("??????<font color='#F5562A'>" + diffe + "??????</font>??????????????????~");
            }
        } else {
            //????????????
            return Html.fromHtml("???????????????<font color='#F5562A'>" + diffe + "???????????????</font>??????");
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    /**
     * ???????????????????????????
     */
    public void rightGoTo(View view) {
        if (modeType == 0) {
            //?????????????????????
            loadAdVideo();
        } else {
            //???????????????????????????
            ARouter.getInstance().build(RouterActivityPath.Main.PAGER_MAIN)
                    .withInt("position", 2)
                    .navigation();
            dismiss();
        }
    }

    /**
     * ???????????????????????????
     */
    public void leftGoTo(View view) {
        if (dataBinding.tvClose.getAlpha() < 1) {
            return;
        }
        dismiss();
        if (!ActivityGuideMaskUtil.INSTANCE.getGuideShowRecord(act, R.id.accessibility_custom_action_0)) {
            //??????????????????????????????
            checkNewUserGuide(1);
        } else if (!ActivityGuideMaskUtil.INSTANCE.getGuideShowRecord(act, R.id.accessibility_custom_action_1)) {
            //??????????????????????????????
            checkNewUserGuide(2);
        }
    }

    //?????????dataBinding????????????
    private void initDatabinding() {
        dataBinding.setThiz(this);
    }

    //????????????
    private void loadAdVideo() {
        if (getActivity() == null) {
            ToastUtil.showShort(getContext(), "??????????????????,???????????????");
            return;
        }
        dataBinding.butNext.setEnabled(false);
        FragmentActivity activity = getActivity();
        if (activity instanceof MvvmBaseLiveDataActivity) {
            ((MvvmBaseLiveDataActivity<?, ?>) activity).showLoading("?????????");
        }
        AdManager.INSTANCE.loadRewardVideoAd(activity, new SimpleRewardVideoListener() {
            //?????????????????????
            boolean isVerifyReward = false;

            @Override
            public void onVideoCached() {
                super.onVideoCached();
                if (activity instanceof MvvmBaseLiveDataActivity) {
                    ((MvvmBaseLiveDataActivity<?, ?>) activity).hideLoading();
                }
            }

            @Override
            public void onAdShow() {
                super.onAdShow();
                dismiss();
            }

            @Override
            public void onAdError(int code, @Nullable String errorMsg) {
                ToastUtil.showShort(activity, "??????????????????,???????????????!");
                super.onAdError(code, errorMsg);
                if (activity instanceof MvvmBaseLiveDataActivity) {
                    ((MvvmBaseLiveDataActivity<?, ?>) activity).showLoading("?????????");
                }
                dismiss();
            }

            @Override
            public void onRewardVerify(boolean result) {
                isVerifyReward = result;
                if (isVerifyReward) {
                    HomeEarnCoinReq req = new HomeEarnCoinReq();
                    req.user_id = AppInfo.getUserId();
                    exchanViewMovdel.getEarnCoin(req)
                            .observe(activity, (item) -> {
                                if (item != null) {
                                    if (BaseMiddleViewModel.getBaseViewModel().mine2JBCount.getValue() != null) {
                                        BaseMiddleViewModel.getBaseViewModel().mine2JBCount.postValue(
                                                BaseMiddleViewModel.getBaseViewModel().mine2JBCount.getValue() + item.coin);
                                    } else {
                                        BaseMiddleViewModel.getBaseViewModel().mine2JBCount.postValue(item.coin);
                                    }
                                    //??????????????????
                                    showDoingResultDialog(activity, item.coin);
                                } else {
                                    ToastUtil.showShort(activity, "??????????????????,???????????????!");
                                }
                            });
                }
            }

            @Override
            public void onAdClose() {
                if (!isVerifyReward) {
                    ToastUtil.showLong(activity, "?????????????????????????????????????????????");
                }
            }
        });
    }

    // ????????????????????????
    private void showDoingResultDialog(FragmentActivity activity, int count) {
        DoingResultDialog dialog = new DoingResultDialog(activity, count, R.drawable.sign_reward_mine_dialog_djb);
        dialog.setStateListener(() -> {
        });
        dialog.show(activity);
    }

    //???????????????
    private void showFeed() {
        Activity act = getActivity();
        FeedNativeAndTemplateAd.INSTANCE.loadFeedTemplateAdNew(act, dataBinding.adLayout, null);
    }

    /**
     * ?????????????????????
     *
     * @param type 0:?????????????????????????????????
     *             1: ????????????????????????????????????Tab?????????
     *             2: ????????????????????????????????????Tab?????????
     */
    private void checkNewUserGuide(int type) {
        if (0 == type) {
            if (ActivityGuideMaskUtil.INSTANCE.getGuideShowRecord(this, R.id.but_next)) {
                return; //?????????????????????????????????
            }
            RectF rectF = new RectF();
            ActivityGuideMaskUtil.INSTANCE.getRectInScreen(dataBinding.butNext, rectF);
            HighlightOptions options = new HighlightOptions.Builder()
                    .setOnClickListener(this::rightGoTo)
                    .build();
            NewbieGuide.with(this)
                    .setLabel("relative1")
                    .anchor(getDialog().getWindow().getDecorView())
                    .alwaysShow(true) //????????????????????????????????????
                    .addGuidePage(new GuidePage()
                            .setLayoutRes(R.layout.home_guide_exchanage_zjb_dialog) //??????????????????????????????
                            .addHighLightWithOptions(
                                    rectF, HighLight.Shape.ROUND_RECTANGLE, 100, options)
                            .setOnLayoutInflatedListener((view, controller1) -> {
                                TextView tv = view.findViewById(R.id.guide_dialog_text);
                                String str = "????????????????????????????????????<br></br><font color='#F5562A'>??????????????????</font>";
                                tv.setText(Html.fromHtml(str));
                            })
                    )
                    .show();
            ActivityGuideMaskUtil.INSTANCE.saveGuideShowRecord(this, R.id.but_next, true);
        } else if (1 == type) {
            if (ActivityGuideMaskUtil.INSTANCE.getGuideShowRecord(act, R.id.accessibility_custom_action_0)) {
                return; //?????????????????????????????????
            }
            ActivityGuideMaskUtil.INSTANCE.showGuide(act,
                    R.layout.home_guide_exchanage_main_tab1,
                    R.id.accessibility_custom_action_0,
                    controller -> {
                        controller.remove();
                        //?????????
                        ARouter.getInstance().build(RouterActivityPath.Main.PAGER_MAIN)
                                .withInt("position", 1)
                                .navigation();
                        return null;
                    }, (view, controller) -> {
                        TextView tv = view.findViewById(R.id.guide_dialog_text);
                        String str = "????????????<font color='#F5562A'>??????????????????</font><br></br>????????????";
                        tv.setText(Html.fromHtml(str));
                    });
        } else if (type == 2) {
            if (ActivityGuideMaskUtil.INSTANCE.getGuideShowRecord(act, R.id.accessibility_custom_action_1)) {
                return; //?????????????????????????????????
            }
            ActivityGuideMaskUtil.INSTANCE.showGuide(act,
                    R.layout.home_guide_exchanage_main_tab2,
                    R.id.accessibility_custom_action_1,
                    controller -> {
                        controller.remove();
                        //?????????
                        ARouter.getInstance().build(RouterActivityPath.Main.PAGER_MAIN)
                                .withInt("position", 2)
                                .navigation();
                        return null;
                    }, (view, controller) -> {
                        TextView tv = view.findViewById(R.id.guide_dialog_text);
                        String str = "??????+??????<br></br><font color='#F5562A'>?????????????????????</font>";
                        tv.setText(Html.fromHtml(str));
                    });
        }
    }
}