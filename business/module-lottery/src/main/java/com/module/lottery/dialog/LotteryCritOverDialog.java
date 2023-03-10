package com.module.lottery.dialog;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.airbnb.lottie.LottieAnimationView;
import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.donews.base.model.BaseLiveDataModel;
import com.donews.middle.dialog.BaseDialog;
import com.donews.yfsdk.monitor.LotteryAdCheck;
import com.donews.network.EasyHttp;
import com.donews.network.cache.model.CacheMode;
import com.donews.network.callback.SimpleCallBack;
import com.donews.network.exception.ApiException;
import com.donews.utilslibrary.analysis.AnalysisUtils;
import com.donews.utilslibrary.dot.Dot;
import com.donews.utilslibrary.utils.AppInfo;
import com.module.lottery.bean.CritCodeBean;
import com.module.lottery.model.LotteryModel;
import com.module.lottery.ui.BaseParams;
import com.module_lottery.R;
import com.module_lottery.databinding.LotteryCritOverDialogLayoutBinding;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class LotteryCritOverDialog extends BaseDialog<LotteryCritOverDialogLayoutBinding> implements DialogInterface.OnDismissListener {
    private CritOverHandler mCritOverHandler = new CritOverHandler(this);
    private String mGoodsId;
    private BaseLiveDataModel baseLiveDataModel;
    private OnStateListener mOnFinishListener;

    public LotteryCritOverDialog(String goodsId, Context context) {
        super(context, R.style.dialogTransparent);
        baseLiveDataModel = new BaseLiveDataModel();
        this.mGoodsId = goodsId;
    }

    @Override
    public int setLayout() {
        return R.layout.lottery_crit_over_dialog_layout;
    }

    @Override
    public float setDimAmount() {
        return 0.9f;
    }

    @Override
    public float setSize() {
        return 1.0f;
    }
    //??????????????????


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //??????????????????????????????
        Message message = new Message();
        message.what = 1;
        mCritOverHandler.sendMessageDelayed(message, 1000);
        //??????????????????????????????
        setOnDismissListener(this);
        initLottie(mDataBinding.overView, "over_animation.json");
    }

    private void initLottie(LottieAnimationView view, String json) {
        if ((view != null && !view.isAnimating())) {
            view.setImageAssetsFolder("images");
            view.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    multipleCode();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            view.clearAnimation();
            view.setAnimation(json);
            view.loop(false);
            view.playAnimation();
        }
    }


    private void multipleCode() {
        //?????????????????????
        if (baseLiveDataModel != null && mGoodsId != null) {
            Map<String, String> params = BaseParams.getMap();
            params.put("goods_id", mGoodsId);
            String time = TimeUtils.date2String(new Date(), new SimpleDateFormat("yyyyMMdd"));
            String md5 = mGoodsId + "." + AppInfo.getUserId() + "." + time + "." + "@lottery!$%^";
            String md5String = EncryptUtils.encryptMD5ToString(md5);
            params.put("code", md5String);
            getNeCritData(params, LotteryModel.LOTTERY_CRIT_CODE);
        }
    }


    private void getNeCritData(Map<String, String> params, String url) {
        JSONObject json = new JSONObject(params);
        baseLiveDataModel.unDisposable();
        baseLiveDataModel.addDisposable(EasyHttp.post(url)
                .cacheMode(CacheMode.NO_CACHE)
                .upJson(json.toString())
                .execute(new SimpleCallBack<CritCodeBean>() {
                    @Override
                    public void onError(ApiException e) {
                        //????????????
                        if (mOnFinishListener != null) {
                            mOnFinishListener.onFinish();
                        }
                        Toast.makeText(getContext(), "?????????????????????", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(CritCodeBean generateCode) {
                        if (generateCode != null) {
                            //????????????
                            LotteryAdCheck.INSTANCE.lotterySuccess();
                            if (mOnFinishListener != null) {
                                mOnFinishListener.onCritJump(generateCode);
                                mOnFinishListener.onFinish();
                            }
                        }
                    }
                }));


    }


    public void setStateListener(OnStateListener l) {
        mOnFinishListener = l;
    }

    public interface OnStateListener {
        /**
         * ??????????????????Activity???
         */
        void onFinish();

        void onCritJump(CritCodeBean critCodeBean);

    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mCritOverHandler != null) {
            mCritOverHandler.removeMessages(0);
            mCritOverHandler.removeMessages(1);
            mCritOverHandler.removeCallbacksAndMessages(null);
        }
    }


    private static class CritOverHandler extends Handler {
        private WeakReference<LotteryCritOverDialog> reference;   //

        CritOverHandler(LotteryCritOverDialog context) {
            reference = new WeakReference(context);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (reference.get() != null) {
                    }
                    break;

            }
        }
    }


}
