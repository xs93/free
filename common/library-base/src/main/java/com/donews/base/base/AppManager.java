package com.donews.base.base;

import android.app.Activity;

import java.util.Stack;

/**
 * 应用模块:
 * <p>
 * 类描述: Activity的堆栈管理者
 * <p>
 * <p>
 * 作者： created by honeylife<br>
 * 日期：2020-02-25
 */
public class AppManager {
    private static Stack<Activity> activityStack;

    private AppManager() {
    }

    private static class SingleHolder {
        private static AppManager instance = new AppManager();
    }

    public static AppManager getInstance() {
        return SingleHolder.instance;
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    /**
     * 移除指定的Activity
     */
    public void removeActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
        }
    }

    /**
     * 是否有activity
     */
    public boolean isActivity() {
        if (activityStack != null) {
            return !activityStack.isEmpty();
        }
        return false;
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity currentActivity() {
        Activity activity = activityStack.lastElement();
        return activity;
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    public void finishActivity() {
        Activity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
                break;
            }
        }
    }

    /**
     * 结束指定类名的Activity.队列中所以后的指定class的页面
     */
    public void finishAllActivity(Class<?> cls) {
        Stack<Activity> activityStackLo = new Stack<>();
        for (Activity activity : activityStack) {
            activityStackLo.add(activity);
        }
        for (Activity activity : activityStackLo) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * 获取activity数量
     */
    public int getActivitySize() {
        if (activityStack == null) {
            return 0;
        }

        return activityStack.size();
    }


    /**
     * 结束指定类名的Activity
     */
    public Activity getTopActivity() {
        if (activityStack != null && activityStack.size() > 0) {

            return activityStack.get(activityStack.size() - 1);
        }
        return null;
    }

    public Activity getSecondActivity() {
        if (activityStack != null && activityStack.size() > 1) {
            return activityStack.get(activityStack.size() - 2);
        }
        return null;
    }


    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                finishActivity(activityStack.get(i));
            }
        }
        activityStack.clear();
    }

    /**
     * 获取指定的Activity
     *
     * @author kymjs
     */
    public Activity getActivity(Class<?> cls) {
        if (activityStack != null) {
            for (Activity activity : activityStack) {
                if (activity.getClass().equals(cls)) {
                    return activity;
                }
            }
        }
        return null;
    }

    /**
     * 退出应用程序
     */
    public void AppExit() {
        try {
            finishAllActivity();
        } catch (Exception e) {
            activityStack.clear();
            e.printStackTrace();
        }
    }
}
