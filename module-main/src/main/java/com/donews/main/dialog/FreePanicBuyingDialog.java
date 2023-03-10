package com.donews.main.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.VibrateUtils;
import com.dn.drouter.ARouteHelper;
import com.dn.events.events.LoginUserStatus;
import com.donews.base.utils.ToastUtil;
import com.donews.common.router.RouterActivityPath;
import com.donews.main.BuildConfig;
import com.donews.main.R;
import com.donews.main.databinding.FreePanicDialogLayoutBinding;
import com.donews.main.utils.ExitInterceptUtils;
import com.donews.middle.centralDeploy.ABSwitch;
import com.donews.middle.dialog.BaseDialog;
import com.donews.middle.utils.JsonValueListUtils;
import com.donews.utilslibrary.analysis.AnalysisUtils;
import com.donews.utilslibrary.dot.Dot;
import com.donews.utilslibrary.utils.AppInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;

//抽奖页返回拦截dialog
public class FreePanicBuyingDialog extends BaseDialog<FreePanicDialogLayoutBinding> implements DialogInterface.OnDismissListener, View.OnClickListener {

    private String FATHER_URL = BuildConfig.API_LOTTERY_URL;
    String RECENT_FREE = "https://lottery.xg.tagtic.cn/lottery/v1/get-now-time";
    private Context mContext;
    private int limitNumber = 1;
    private LotteryHandler mLotteryHandler;
    private long fastVibrateTime = 0;
    OnFinishListener mOnFinishListener;

    public FreePanicBuyingDialog(Context context) {
        super(context, R.style.dialogTransparent);//内容样式在这里引入
        this.mContext = context;
        mLotteryHandler = new LotteryHandler(this);
    }

    @Override
    public int setLayout() {
        return R.layout.free_panic_dialog_layout;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding.closure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(getContext() instanceof Activity) {
                    ExitInterceptUtils.closeExitDialog(getOwnerActivity());
                }
            }
        });
        mDataBinding.protocolLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataBinding.checkBox.setChecked(!mDataBinding.checkBox.isChecked());
            }
        });
        EventBus.getDefault().register(this);
        initView();
        setOnDismissListener(this);
        //延迟一秒出现关闭按钮
        Message message = new Message();
        message.what = 1;
        mLotteryHandler.sendMessageDelayed(message, 1000);
    }


    //查询服务器时间

    public void requestGoodsInfo(Context context) {
        if (mOnFinishListener != null) {
            mOnFinishListener.onShow();
        }
//        //判断今天是否弹起过
//        if (DateManager.getInstance().ifFirst(DateManager.FREE_PANIC_DIALOG_KEY)) {
//            Disposable disposable = EasyHttp.get(RECENT_FREE)
//                    .cacheMode(CacheMode.NO_CACHE)
//                    .execute(new SimpleCallBack<NowTimeBean>() {
//                        @Override
//                        public void onError(ApiException e) {
//                        }
//
//                        @Override
//                        public void onSuccess(NowTimeBean time) {
//                            long t = Long.parseLong(time.getNow() + "") * 1000;
//                            Calendar calendar = Calendar.getInstance();
//                            calendar.setTimeInMillis(t);
//                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
//                            int minute = calendar.get(Calendar.MINUTE);
//                            //判断是否是今天首次
//                            if (hour > 9 || (hour >= 9 && minute >= 58)) {
//                                if (mOnFinishListener != null) {
//                                    mOnFinishListener.onShow();
//                                }
//                            }
//                        }
//                    });
//
//            addDisposable(disposable);
//        } else {
//            if (AppInfo.checkIsWXLogin()) {
//                if (SPUtils.getInformain(KeySharePreferences.SHOW_DIALOG_WHEN_LAUNCH, true)) {
//                    new EnterShowDialog(mContext).show();
//                }
//            }
//        }
    }


    @Subscribe
    public void WeChatLoginEvent(LoginUserStatus loginUserStatus) {
        if (loginUserStatus.getStatus() == 1 && AppInfo.checkIsWXLogin()) {
            dismiss();
        }
    }


    private boolean isSendCloseEvent = true;

    // 1 表示未登录 2 表示登录未抽奖
    private void initView() {
        //未登录时
        mDataBinding.userProtocol.setOnClickListener(this);
        mDataBinding.privacyProtocol.setOnClickListener(this);
        boolean protocol = getSharedPreferences().getBoolean("Free", false) ||
                ABSwitch.Ins().isOpenAutoAgreeProtocol();
        mDataBinding.checkBox.setChecked(protocol);
        mDataBinding.jumpButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                //判断是否同意了隐私协议
                if (mDataBinding.checkBox.isChecked()) {
                    //存储状态
                    isSendCloseEvent = false;
                    getEditor().putBoolean("Free", true).commit();
                    RouterActivityPath.LoginProvider.getLoginProvider()
                            .loginWX(null, "首页>未登陆弹窗");
                } else {
                    //檢查是否勾选协议
                    if (System.currentTimeMillis() - fastVibrateTime > 1500) {
                        fastVibrateTime = System.currentTimeMillis();
                        VibrateUtils.vibrate(100); //震动50毫秒
                    }
                    ToastUtil.showShort(mContext, "请先同意相关协议");

                    mDataBinding.protocolLayout.clearAnimation();
                    Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.anim_not_select);
                    mDataBinding.protocolLayout.startAnimation(anim);
                }
            }
        });

        //手
        mDataBinding.maskingHand.setImageAssetsFolder("images");
        mDataBinding.maskingHand.setAnimation(JsonValueListUtils.LOTTERY_FINGER);
        mDataBinding.maskingHand.loop(true);
        mDataBinding.maskingHand.playAnimation();

    }

    @Override
    public float setSize() {
        return 0.9f;
    }


    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        return true;
    }


    public void setFinishListener(OnFinishListener l) {
        mOnFinishListener = l;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (isSendCloseEvent) {
        }
        if (mLotteryHandler != null) {
            mLotteryHandler.removeMessages(0);
            mLotteryHandler.removeMessages(1);
            mLotteryHandler.removeCallbacksAndMessages(null);
            mLotteryHandler = null;
        }
        EventBus.getDefault().unregister(this);
        if (mOnFinishListener != null) {
            mOnFinishListener.onDismiss();
        }
    }

    @Override
    public void onClick(View v) {
        //用户协议
        if (v.getId() == R.id.user_protocol) {
            Bundle bundle = new Bundle();
            bundle.putString("url", BuildConfig.USER_PROCOTOL);
            bundle.putString("title", "用户协议");
            ARouteHelper.routeSkip(RouterActivityPath.Web.PAGER_WEB_ACTIVITY, bundle);
        }

        //隐私政策
        if (v.getId() == R.id.privacy_protocol) {

            Bundle bundle = new Bundle();
            bundle.putString("url", BuildConfig.PRIVATE_POLICY_URL);
            bundle.putString("title", "隐私政策");
            ARouteHelper.routeSkip(RouterActivityPath.Web.PAGER_WEB_ACTIVITY, bundle);
        }
    }

    public interface OnFinishListener {
        /**
         * 此时可以关闭Activity了
         */
        void onDismiss();

        void onShow();

    }

    private static class LotteryHandler extends Handler {
        private WeakReference<FreePanicBuyingDialog> reference;   //

        LotteryHandler(FreePanicBuyingDialog context) {
            reference = new WeakReference(context);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (reference.get() != null) {
                        reference.get().mDataBinding.closure.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    }


}