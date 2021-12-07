package com.donews.notify.launcher;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.donews.base.storage.MmkvHelper;
import com.donews.common.NotifyLuncherConfigManager;
import com.donews.utilslibrary.utils.KeySharePreferences;

import java.text.SimpleDateFormat;
import java.util.Date;


public class NotifyScreenDelegate {
    private static final String KEY_NOTIFYOPEN_TIME = KeySharePreferences.KEY_NOTIFYOPEN_TIME;
    private static final String KEY_NOTIFYCOUNT_LIMIT = KeySharePreferences.KEY_NOTIFYCOUNT_LIMIT;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final long HOUR = 60 * 60 * 1000;
    private static final long DAY = HOUR * 24;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean isLoaded;
    private boolean isOpen;

    private Runnable mShowNotifyRunnable = null;

    /**
     * 解决多次解锁时，间隙，cd时间默认为10秒内不出
     */
    private long mIntervalLockShowTime = 10 * 1000;
    private long mLastShowTime = 0;

    /**
     * 网络变换
     */
    private int mCurruntCount = 0;

    private Runnable getShowNotifyRunnable(Context context) {
        if (mShowNotifyRunnable == null) {
            mShowNotifyRunnable = () -> {
                Log.i(NotifyInitProvider.TAG, "NotifyScreenDelegate excute Runnable");
                NotifyActionActivity.destroy();
                if (canOpen(context) && isLoaded) {
                    NotifyActivity.actionStart(context);
                    isOpen = true;
                }
            };
        }
        return mShowNotifyRunnable;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(NotifyInitProvider.TAG, action);
        switch (action) {
            case Intent.ACTION_SCREEN_ON:
                /* 不做业务逻辑处理，只是为了辅助后续的解锁页面弹出,提升解锁弹出的成功率 */
                if (canShowNotify()) {
                    NotifyActionActivity.actionStart(context);
                } else {
                    Log.w(NotifyInitProvider.TAG, action + " can't show");
                }
                break;

            case Intent.ACTION_USER_PRESENT:
                NotifyActionActivity.destroy();

                if (canShowNotify()) {
                    tryLoadNewImg(context);
                    long delayTime = NotifyLuncherConfigManager.getInstance().getAppGlobalConfigBean().notifyDelayShowTime;
                    mLastShowTime = System.currentTimeMillis();
                    Log.w(NotifyInitProvider.TAG, action + " show , delayTime=" + delayTime);
                    mHandler.postDelayed(getShowNotifyRunnable(context), delayTime);
                } else {
                    Log.w(NotifyInitProvider.TAG, action + " can't show");
                }
                break;

            case Intent.ACTION_SCREEN_OFF:
                //  锁屏时注意关闭解锁和亮屏弹出来的透明页面，只是为了体验好些。
                NotifyActionActivity.destroy();
                NotifyActivity.destroy();
                break;
            case ConnectivityManager.CONNECTIVITY_ACTION://网络状态发生变化
                if (mCurruntCount > 2) {
                    NotifyActionActivity.destroy();
                    if (canShowNotify()) {
                        tryLoadNewImg(context);
                        long delayTime = NotifyLuncherConfigManager.getInstance().getAppGlobalConfigBean().notifyDelayShowTime;
                        Log.w(NotifyInitProvider.TAG, action + " show , delayTime=" + delayTime);
                        mHandler.postDelayed(getShowNotifyRunnable(context), delayTime);
                    } else {
                        Log.w(NotifyInitProvider.TAG, action + " can't show");
                    }
                }
                Log.i(NotifyInitProvider.TAG, action + ",currunt num = " + mCurruntCount);
                mCurruntCount++;
                break;
        }
    }

    private void tryLoadNewImg(Context context) {
        isLoaded = false;
        isOpen = false;
        String url = NotifyLuncherConfigManager.getInstance().getAppGlobalConfigBean().notifyLauncherImg;
        Glide.with(context).asBitmap().load(url).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                isLoaded = true;
                if (!isOpen) {
                    getShowNotifyRunnable(context);
                }
                Log.d(NotifyInitProvider.TAG, "tryLoadNewImg success , url = " + url);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                isLoaded = true;
                Log.d(NotifyInitProvider.TAG, "tryLoadNewImg onLoadCleared , url = " + url);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                isLoaded = true;
                Log.d(NotifyInitProvider.TAG, "tryLoadNewImg onLoadFailed , url = " + url);
            }
        });

    }

    private boolean canOpen(Context context) {
        boolean alwaysShow = NotifyLuncherConfigManager.getInstance().getAppGlobalConfigBean().notifyAlwaysShow;
        boolean showCount = showCountLimit(context);
        if (alwaysShow && showCount) {
            return true;
        } else {
            if (!MmkvHelper.isInit()) {
                MmkvHelper.init(context);
            }
            // 初次没写入则有可能为0
            long lastOpenTime = MmkvHelper.getInstance().getMmkv().decodeLong(KEY_NOTIFYOPEN_TIME, 1);
            long todayZero = getTodayZeroTime();
            boolean result = (todayZero > lastOpenTime) && lastOpenTime > 0;
            Log.w(NotifyInitProvider.TAG, "lastOpenTime result = " + result);
            result &= showCount;

            Log.w(NotifyInitProvider.TAG, "final result = " + result);
            return result;
        }
    }

    private boolean showCountLimit(Context context) {
        long notifyShowMaxCount = NotifyLuncherConfigManager.getInstance().getAppGlobalConfigBean().notifyShowMaxCount;
        long curruntNotifyCount = getTodayShowCount(context);
        Log.w(NotifyInitProvider.TAG, "curruntNotifyCount = " + curruntNotifyCount + "，notifyShowMaxCount = " + notifyShowMaxCount);
        return curruntNotifyCount < notifyShowMaxCount;
    }

    /**
     * 时间对的上
     */
    private boolean canShowNotify() {
        //时间，比如12代表12点
        int time1 = NotifyLuncherConfigManager.getInstance().getAppGlobalConfigBean().notifyTimeStart1;
        int time2 = NotifyLuncherConfigManager.getInstance().getAppGlobalConfigBean().notifyTimeStart2;
        Log.w(NotifyInitProvider.TAG, "canShowNotify time1 = " + time1);
        Log.w(NotifyInitProvider.TAG, "canShowNotify time2 = " + time2);

        boolean result = isRangeTime(time1) || isRangeTime(time2);

        long now = System.currentTimeMillis();
        boolean canShow = (now - mLastShowTime > mIntervalLockShowTime);

        Log.w(NotifyInitProvider.TAG, "result = " + result + ",canShow = " + canShow);
        result = result && canShow;
        return result;
    }

    private boolean isRangeTime(int time) {
        //范围
        int startTime1 = time - 2;
        int endTime1 = time + 2;
        if (startTime1 <= 0) {
            startTime1 = 0;
        }
        long todayZero = getTodayZeroTime();
        long todayStartTime1 = todayZero + HOUR * startTime1;
        long todayEndTime1 = todayZero + HOUR * endTime1;
        long now = System.currentTimeMillis();

        return now >= todayStartTime1 && now <= todayEndTime1;
    }

    public long getTodayZeroTime() {
        long currentTimestamps = System.currentTimeMillis();
        String today = SIMPLE_DATE_FORMAT.format(new Date());
        try {
            return SIMPLE_DATE_FORMAT.parse(today).getTime();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        //加上8小时的毫秒数是因为毫秒时间戳是从北京时间1970年01月01日08时00分00秒开始算的
        return currentTimestamps - (currentTimestamps + HOUR * 8) % DAY;
    }

    public static void putTodayShowCount(Context context) {
        if (!MmkvHelper.isInit()) {
            MmkvHelper.init(context);
        }
        String today = SIMPLE_DATE_FORMAT.format(new Date());

        long curruntNotifyCount = MmkvHelper.getInstance().getMmkv().decodeLong(today + "_" + KEY_NOTIFYCOUNT_LIMIT, 0);
        curruntNotifyCount += 1;

        Log.w(NotifyInitProvider.TAG, "putTodayShowCount =" + curruntNotifyCount);
        MmkvHelper.getInstance().getMmkv().encode(today + "_" + KEY_NOTIFYCOUNT_LIMIT, curruntNotifyCount);
    }

    public static long getTodayShowCount(Context context) {
        if (!MmkvHelper.isInit()) {
            MmkvHelper.init(context);
        }
        String today = SIMPLE_DATE_FORMAT.format(new Date());

        return MmkvHelper.getInstance().getMmkv().decodeLong(today + "_" + KEY_NOTIFYCOUNT_LIMIT, 0);
    }


}
