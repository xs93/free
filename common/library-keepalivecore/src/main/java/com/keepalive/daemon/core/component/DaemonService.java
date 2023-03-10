package com.keepalive.daemon.core.component;

import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.keepalive.daemon.core.notification.NotifyResidentService;
import com.keepalive.daemon.core.utils.Logger;

public class DaemonService extends DaemonBaseService {

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"DaemonService onCreate");
        try {
            ContextCompat.startForegroundService(this,new Intent().setClassName(getPackageName(), NotifyResidentService.class.getName()));
        } catch (Throwable th) {
            Logger.e(Logger.TAG, "failed to start foreground service: " + th.getMessage());
        }
        startService(new Intent().setClassName(getPackageName(), AssistService1.class.getName()));
        startService(new Intent().setClassName(getPackageName(), AssistService2.class.getName()));
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"DaemonService onDestroy , pid = "+ Process.myPid());
    }
}
