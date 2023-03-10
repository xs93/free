package com.donews.notify.launcher;

import static com.donews.utilslibrary.utils.KeySharePreferences.NOTIFY_RANDOM_RED_AMOUNT;
import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.SPUtils;
import com.donews.base.base.AppManager;
import com.donews.base.base.BaseApplication;
import com.donews.base.utils.ToastUtil;
import com.donews.common.NotifyLuncherConfigManager;
import com.donews.keepalive.LaunchStart;
import com.donews.notify.R;
import com.donews.notify.launcher.configs.Notify2ConfigManager;
import com.donews.notify.launcher.configs.baens.Notify2DataConfigBean;
import com.donews.notify.launcher.utils.AbsNotifyInvokTask;
import com.donews.notify.launcher.utils.AppNotifyForegroundUtils;
import com.donews.notify.launcher.utils.NotifyItemUtils;
import com.donews.notify.launcher.utils.NotifyLog;
import com.donews.notify.launcher.utils.fix.FixTagUtils;
import com.donews.utilslibrary.analysis.AnalysisParam;
import com.donews.utilslibrary.analysis.AnalysisUtils;
import com.donews.utilslibrary.dot.Dot;
import com.donews.utilslibrary.utils.AppInfo;
import com.gyf.immersionbar.ImmersionBar;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

public class NotifyActivity extends FragmentActivity {
    public static final String TAG = NotifyInitProvider.TAG;

    private static WeakReference<NotifyActionActivity> sPopActionRefer = null;
    private static final LaunchStart launchStart = new LaunchStart();

    public static void actionStart(Context context) {
        //???????????????????????????????????????????????????
        FixTagUtils.updateLocalRandomRangNumber();
        //????????????????????????????????????????????????????????????????????????????????????
        Log.i(TAG, "NotifyActivity actionStart");
        destroy();
        if(AppUtils.isAppForeground()){
            NotifyLog.logNotToast("????????????????????????????????????");
            launchStart.cancel();
            return;//???????????????????????????
        }
        List<Notify2DataConfigBean.UiTemplat> uiList = NotifyItemUtils.getMeetConditionalUiTemplats();
        if(uiList == null || uiList.isEmpty()){
            NotifyLog.logNotToast("??????????????????UI?????????????????????");
            return; //??????????????????UI????????????????????????
        }
        NotifyLog.logNotToast("?????????????????????????????????");
        try {
            Intent intent = new Intent();
            ComponentName componentName = new ComponentName(context, NotifyLuncherConfigManager.getInstance().getAppGlobalConfigBean().notifyAlias);
            intent.setComponent(componentName);
            //???????????????????????????
//            intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            launchStart.doStart(context, intent);
        } catch (Throwable t) {
            t.printStackTrace();
            Intent intent = new Intent(context, NotifyActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchStart.doStart(context, intent);
        }
    }

    public static void destroy() {
        Log.d(TAG, "NotifyActivity destroy");
        Activity activity = sPopActionRefer != null ? sPopActionRefer.get() : null;
        if (activity != null && !activity.isDestroyed()) {
            Log.d(TAG, "NotifyActivity finish");
            activity.finish();
        }
        NotifyActionActivity.destroy();
    }

    public static void browserOpen(Activity activity, String uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(uri));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            //????????????????????????????????????
            SPUtils.getInstance().put("notifyClickLastOpenInterval", System.currentTimeMillis());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private ViewGroup mLayoutNotifyRoot = null;
    private NotifyAnimationView mNotifyAnimationView = null;

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener touch = (view, motionEvent) -> {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            //if else
            int action = NotifyLuncherConfigManager.getInstance().getAppGlobalConfigBean().notifyActionType;
            Log.w(TAG, "open action = " + action);
            switch (action) {
                case 0:
                    mNotifyAnimationView.hide();
                    break;
                case 1:
                    destroy();
                    break;
                case 2:
                    schemeOpen(true,false);
                    break;
            }
        }
        return false;
    };

    @Override
    public void onBackPressed() {
        try {
            mNotifyAnimationView.hide();
        }catch (Exception e){
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notify_top_banner);
        AppNotifyForegroundUtils.updateBackgroundTime();
        //????????????
        try {
            AnalysisUtils.onEvent(this, AnalysisParam.DESKTOP_DISPLAY);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        NotifyScreenDelegate.putTodayShowCount(this);

        sPopActionRefer = new WeakReference(this);
        mLayoutNotifyRoot = findViewById(R.id.layout_notifyroot);

        mNotifyAnimationView = findViewById(R.id.layout_notifyanimation);
        mLayoutNotifyRoot.setBackgroundColor(Color.TRANSPARENT);
        mNotifyAnimationView.setViewDimissListener(new NotifyAnimationView.ViewStatusListener() {
            @Override
            public void onNotifyClose(View view) {
                Log.d(TAG, "NotifyActivity onNotifyClose");
                destroy();
            }

            @Override
            public void onNotifyShow(View view) {
                Log.d(TAG, "NotifyActivity onNotifyShow");
            }
        });
        mLayoutNotifyRoot.setOnTouchListener(touch);
        mNotifyAnimationView.setOnClickListener(v -> {
            schemeOpen(true,true);
        });
        setNotifyAttrs();
        launchStart.cancel();

        ImmersionBar.with(this)
                .statusBarColor(R.color.transparent)
                .fitsSystemWindows(true)
                .autoDarkModeEnable(false)
                .init();

    }

    //??????????????????????????????
    private void setNotifyAttrs() {
        //???????????????,??????????????????
        NotifyItemUtils.initNotifyParams(this, mNotifyAnimationView, () -> {
        });

//        //????????????????????????
//        int pos = a % NotifyItemType.values().length;
//        NotifyItemType type = NotifyItemType.values()[pos];
//        mNotifyAnimationView.orientation = a % 3;
//        mNotifyAnimationView.notifyType = type;
//        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mNotifyAnimationView.getLayoutParams();
//        if(mNotifyAnimationView.orientation == 0){
//            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
//        }if (mNotifyAnimationView.orientation == 1) {
//            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//        } else if (mNotifyAnimationView.orientation >= 2) {
//            lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//        }
//        mNotifyAnimationView.setLayoutParams(lp);
//        //????????????????????????????????????????????????
//        NotifyItemUtils.addItemView(this, mNotifyAnimationView, type, () -> {
//            //??????????????????UI?????????????????????????????????
//        });
//        a++;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "NotifyActivity onResume");
    }

    @Override
    protected void onDestroy() {
        AppNotifyForegroundUtils.updateBackgroundTime();
        super.onDestroy();
        //???????????????????????????????????????????????????
        AppManager.getInstance().finishAllActivity(getClass());
        Log.d(TAG, "NotifyActivity onDestroy");
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,0);
    }

    /**
     * @param isProbability T:????????????
     * @param isPerformed   ?????????????????????????????????????????????????????????
     */
    private void schemeOpen(boolean isProbability, boolean isPerformed) {
        AbsNotifyInvokTask task = NotifyItemUtils.notifyTypeInvokList.get(mNotifyAnimationView.notifyType);
        //?????????????????????
        int po = NotifyLuncherConfigManager.getInstance().getAppGlobalConfigBean().notifyProbabilityOpen;
        int gernPo = new Random().nextInt(100);
        if (!isPerformed && gernPo > po) {
            mNotifyAnimationView.hide();
            return; //??????????????????????????????????????????????????????????????????
        }
        if(task == null){
            //???????????????????????????????????????
            if(isPerformed){
            }else{
            }
            oldClickInvok(isProbability && !isPerformed);
            return;
        }
        boolean isClickSucce = task.itemClick(mNotifyAnimationView,
                (Notify2DataConfigBean.UiTemplat) mNotifyAnimationView.getTag());
        if(isClickSucce){
            //?????????????????????????????????????????????????????????????????????????????????????????????
            mNotifyAnimationView.hide();
        }else{
            //???????????????????????????????????????????????????
            oldClickInvok(false);
        }
    }

    //?????????????????????
    private void oldClickInvok(boolean isProbability) {
        //?????????????????????
        int po = NotifyLuncherConfigManager.getInstance().getAppGlobalConfigBean().notifyProbabilityOpen;
        if (isProbability) {
            int gernPo = new Random().nextInt(100);
            if (gernPo > po) {
                mNotifyAnimationView.hide();
                return; //??????????????????????????????????????????????????????????????????
            }
        }
        try {
            AnalysisUtils.onEvent(NotifyActivity.this, AnalysisParam.DESKTOP_DISPLAY_CLICK);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        String scheme = NotifyLuncherConfigManager.getInstance().getAppGlobalConfigBean().notifySchemeOpen;
        browserOpen(NotifyActivity.this, scheme);
        mNotifyAnimationView.hide();
    }

}
