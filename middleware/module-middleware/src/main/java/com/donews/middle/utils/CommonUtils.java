package com.donews.middle.utils;

import static com.donews.common.config.CritParameterConfig.CRIT_STATE;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import com.dn.sdk.bean.integral.ProxyIntegral;
import com.donews.common.bean.CritMessengerBean;
import com.donews.common.config.CritParameterConfig;
import com.donews.middle.abswitch.ABSwitch;
import com.donews.middle.service.CritLotteryService;
import com.donews.utilslibrary.utils.DateManager;
import com.donews.utilslibrary.utils.SPUtils;

import org.greenrobot.eventbus.EventBus;

public class CommonUtils {
    //开始暴击模式
    public static void startCrit() {
        if (DateManager.getInstance().timesLimit(DateManager.CRIT_KEY, DateManager.CRIT_NUMBER,
                ABSwitch.Ins().getEnableOpenCritModelCount())) {
            //判断暴击模式是否处于开启中
            int critState = SPUtils.getInformain(CRIT_STATE, 0);
            if (critState == 0) {
                SPUtils.setInformain(CRIT_STATE, 1);
                if (SPUtils.getInformain(CRIT_STATE, 0) == 0) {
                    SPUtils.setInformain(CritParameterConfig.CRIT_START_TIME, SystemClock.elapsedRealtime());
                }
                //判断开启了多少次
                //通知开始暴击模式  模拟开启暴击模式
                EventBus.getDefault().post(new CritMessengerBean(200));
            }


        }
    }

    /**
     * 开启暴击体验监听服务
     *
     * @param context 需要的上下文
     */
    public static void startCritService(Context context, ProxyIntegral integralBean) {
        if (DateManager.getInstance().timesLimit(DateManager.CRIT_KEY, DateManager.CRIT_NUMBER,
                ABSwitch.Ins().getEnableOpenCritModelCount())) {
            //开启暴击校验  (开始服务)
            Intent intent = new Intent(context, CritLotteryService.class);
            intent.putExtra("start_crit", true);
            intent.putExtra("requestId", integralBean.getWallRequestId());
            intent.putExtra("start_time", ABSwitch.Ins().getScoreTaskPlayTime());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }


}