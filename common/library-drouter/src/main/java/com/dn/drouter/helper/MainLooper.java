package com.dn.drouter.helper;

import android.os.Handler;
import android.os.Looper;
/**
 * @author by SnowDragon
 * Date on 2020/11/17
 * Description:
 */
public class MainLooper extends Handler {
    private static MainLooper instance = new MainLooper(Looper.getMainLooper());

    protected MainLooper(Looper looper) {
        super(looper);
    }

    public static MainLooper getInstance() {
        return instance;
    }

    public static void runOnUiThread(Runnable runnable) {
        if (Looper.getMainLooper().equals(Looper.myLooper())) {
            runnable.run();
        } else {
            instance.post(runnable);
        }

    }
}
