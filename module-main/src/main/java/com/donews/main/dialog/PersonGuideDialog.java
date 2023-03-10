package com.donews.main.dialog;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;

import com.alibaba.android.arouter.launcher.ARouter;
import com.donews.main.BuildConfig;
import com.donews.main.R;
import com.donews.main.databinding.MainDialogPeopleGuideBinding;
import com.donews.base.fragmentdialog.AbstractFragmentDialog;
import com.donews.common.router.RouterActivityPath;
import com.donews.main.utils.SplashUtils;
import com.donews.middle.centralDeploy.ABSwitch;
import com.donews.utilslibrary.analysis.AnalysisUtils;
import com.donews.utilslibrary.dot.Dot;
import com.donews.utilslibrary.utils.DeviceUtils;

/**
 * @author by SnowDragon
 * Date on 2020/12/15
 * Description:
 */
public class PersonGuideDialog extends AbstractFragmentDialog<MainDialogPeopleGuideBinding> {

    public PersonGuideDialog() {
        super(false, false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.main_dialog_people_guide;
    }

    @Override
    protected void initView() {
        //拒绝协议
        dataBinding.tvRefuse.setOnClickListener(v -> {
            dataBinding.llGuide.setVisibility(View.GONE);
            dataBinding.imgMainPeople.setVisibility(View.GONE);
            dataBinding.llRefuseHint.setVisibility(View.VISIBLE);
        });
        dataBinding.tvRefuseHint.setText(
                getString(R.string.main_str_people_guide_refuse_hint, getAppName(getContext())));

        //同意协议
        dataBinding.tvAgree.setOnClickListener(v -> {
            if (sureListener != null) {
                sureListener.onSure();
            }
            disMissDialog();
        });

        dataBinding.tvDeal.setText(getDealSpan());
        //必须设置才能响应点击事件
        dataBinding.tvDeal.setMovementMethod(LinkMovementMethod.getInstance());


        //退出应用
        dataBinding.tvExit.setOnClickListener(v -> {
            SplashUtils.INSTANCE.savePersonExit(false);
            if (getOnCancelListener() != null) {
                getOnCancelListener().onCancel();
            }
        });

        //查看指引
        dataBinding.tvLookGuide.setOnClickListener(v -> {
            if (sureListener != null) {
                sureListener.onSure();
            }
            disMissDialog();
        });


    }

    public PersonGuideDialog setSureListener(SureListener sureListener) {
        this.sureListener = sureListener;
        return this;
    }

    public PersonGuideDialog setCancelListener(CancelListener cancelListener) {
        setOnCancelListener(cancelListener);
        return this;
    }


    @Override
    protected boolean isUseDataBinding() {
        return true;
    }


    private SpannableString getDealSpan() {
        String useDealDescription = getString(R.string.main_str_people_guide_explain, getAppName(getContext()));
        int userDealIndex = useDealDescription.indexOf("《用户协议》");
        int userDealLength = userDealIndex + "《用户协议》".length();

        int privacyIndex = useDealDescription.indexOf("《隐私政策》");
        int privacyLength = privacyIndex + "《隐私政策》".length();

        SpannableString span = new SpannableString(useDealDescription);
        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                String url = "";
                if (ABSwitch.Ins().isOpenAB() && DeviceUtils.getChannelName().equalsIgnoreCase("huawei")) {
                    url = "https://ad-static-xg.tagtic.cn/wangzhuan/file/4fb165459cd438f9a3987841d7707d8a.html";
                } else {
                    url = BuildConfig.USER_PROCOTOL;
                }
                ARouter.getInstance().build(RouterActivityPath.Web.PAGER_WEB_ACTIVITY).withString("url", url)
                        .withString("title", "用户协议").navigation();

                //重新设置文字背景为透明色。否则会出现淡绿色背景
                dataBinding.tvDeal.setHighlightColor(Color.TRANSPARENT);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {

                //设置颜色
                ds.setColor(Color.parseColor("#8581FF"));
                ds.setFakeBoldText(true);
                //去掉下划线
                ds.setUnderlineText(false);
            }
        }, userDealIndex, userDealLength, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                String url = "";
                if (ABSwitch.Ins().isOpenAB() && DeviceUtils.getChannelName().equalsIgnoreCase("huawei")) {
                    url = "https://ad-static-xg.tagtic.cn/wangzhuan/file/739eaf3002ea80d1a2232c9cd5a8b9f4.html";
                } else {
                    url = BuildConfig.PRIVATE_POLICY_URL;
                }

                ARouter.getInstance().build(RouterActivityPath.Web.PAGER_WEB_ACTIVITY).withString("url", url)
                        .withString("title", "隐私政策").navigation();
                //重新设置文字背景为透明色。否则会出现淡绿色背景
                dataBinding.tvDeal.setHighlightColor(Color.TRANSPARENT);

            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {

                //设置颜色
                ds.setColor(Color.parseColor("#8581FF"));
                ds.setFakeBoldText(true);
                //去掉下划线
                ds.setUnderlineText(false);
            }
        }, privacyIndex, privacyLength, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;

    }

    public static String getAppName(Context context) {
        if (context == null) {
            return null;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            return String.valueOf(packageManager.getApplicationLabel(context.getApplicationInfo()));
        } catch (Throwable e) {
        }
        return null;
    }
}
