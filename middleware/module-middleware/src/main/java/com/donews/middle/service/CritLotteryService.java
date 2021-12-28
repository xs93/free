package com.donews.middle.service;

import static com.donews.common.config.CritParameterConfig.CRIT_STATE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.AppUtils;
import com.dn.sdk.bean.integral.ProxyIntegral;
import com.donews.base.BuildConfig;
import com.donews.base.utils.ToastUtil;
import com.donews.middle.bean.DownloadStateDean;
import com.donews.middle.ui.GoodLuckDoubleDialog;
import com.donews.network.EasyHttp;
import com.donews.network.cache.model.CacheMode;
import com.donews.network.callback.SimpleCallBack;
import com.donews.network.exception.ApiException;
import com.donews.utilslibrary.utils.SPUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class CritLotteryService extends Service {

    private static String INTEGRAL_BASE = BuildConfig.API_INTEGRAL_URL;
    public static String INTEGRAL_REWARD = INTEGRAL_BASE + "v1/has-wall-reward";
    private Timer mTimer;
    //暴击体验时长
    private int mStartTime;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            ToastUtil.showShort(getApplicationContext(), msg.obj + "");
        }
    };
    private boolean ifTimerRun = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    private void startLotteryForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("100365", "测试奖多多", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(CritLotteryService.this, "100365").build();
            startForeground(100365, notification);
        }


    }

    DownloadStateDean mDownloadStateDean;
    String  mRequestId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLotteryForeground();
        boolean startCrit = intent.getBooleanExtra("start_crit", false);
        mStartTime = intent.getIntExtra("start_time", 0);
        mRequestId = intent.getStringExtra("requestId");
        if (startCrit) {
            //开始服务
            //判断暴击模式是否在运行
            int critState = SPUtils.getInformain(CRIT_STATE, 0);
            if (critState == 0) {
                startTask();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void test(String valsue) {

        Message mes = new Message();
        mes.obj = valsue;
        handler.sendMessage(mes);

    }


    public void getDownloadStatus() {
        if (mRequestId != null) {
            Map<String, String> params = new HashMap<>();
            params.put("req_id", mRequestId);
            unDisposable();
            addDisposable(EasyHttp.get(INTEGRAL_REWARD)
                    .cacheMode(CacheMode.NO_CACHE)
                    .isShowToast(false)
                    .params(params)
                    .execute(new SimpleCallBack<DownloadStateDean>() {
                        @Override
                        public void onError(ApiException e) {
                            mDownloadStateDean = null;
                            if (!ifTimerRun) {
                                ToastUtil.showShort(getApplicationContext(), "任务失败，请从本页面下载并打开对应APP");
                            }
                        }

                        @Override
                        public void onSuccess(DownloadStateDean stateDean) {
                            if (stateDean != null) {
                                mDownloadStateDean = stateDean;
                                if (!ifTimerRun) {
                                    jump();
                                }
                            }
                        }
                    }));
        }

    }


    private void startTask() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;


        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //只有应用不在前台才会继续倒计时
                boolean foreground = AppUtils.isAppForeground();
                test("开始倒计时");
                if (!foreground) {
                    if (mStartTime > 0) {
                        ifTimerRun = true;
                        test("没在前台,倒计时中" + mStartTime);
                        mStartTime = mStartTime - 1;

                        if (mStartTime == (mStartTime / 2)) {
                            //请求服务器处理结果
                            Log.d("startTask", "请求服务器获取结果");
                            getDownloadStatus();
                        }
                    } else {
                        test("可以开始暴击模式了");
                    }
                } else {
                    if (mStartTime <= 0 && foreground) {
                        mTimer.cancel();
                        mTimer = null;
                        ifTimerRun = false;
                        Log.d("startTask", "任务完成");
                        if (mDownloadStateDean == null || !mDownloadStateDean.getHandout()) {
                            //请求服务器处理结果
                            mStartTime = 0;
                            Log.d("startTask", "上次没有请求到或者请求失败");
                            getDownloadStatus();
                        } else {
                            Log.d("startTask", "上次请求成功");
                            jump();
                        }


                    } else {
                        test("在前台，体验体验时间不足" + mStartTime);
                    }
                }
            }
        }, 0, 1000);
    }


    private void jump() {
        //倒计时结束 可以打开开启暴击模式的弹框
        Intent intent = new Intent(CritLotteryService.this, GoodLuckDoubleDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        CritLotteryService.this.startActivity(intent);
    }


    private CompositeDisposable mCompositeDisposable;

    public void addDisposable(Disposable disposable) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);
    }

    public void unDisposable() {
        if (mCompositeDisposable != null && mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.clear();
        }
    }

}