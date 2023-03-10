package com.donews.common.router;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.launcher.ARouter;
import com.donews.base.fragmentdialog.AbstractFragmentDialog;

/**
 * 应用模块: 组件化路由
 * <p>
 * 类描述: 用于组件化开发中,ARouter Fragment路径统一注册, 注册的路径请写好注释、标注业务功能
 * <p>
 * <p>
 * 作者： created by honeylife<br>
 * 日期： 2020-02-25
 */
public class RouterFragmentPath {

    /**
     * 9.9包邮的组件
     */
    public static class Mail {
        private static final String Mail = "/mail";

        /**
         * 包邮的页面组件Fragment
         */
        public static final String PAGE_MAIL_PACKAGE = Mail + "/mailPackageFragment";

    }

    /**
     * 首页组件
     */
    public static class Home {
        private static final String HOME = "/home";

        /**
         * 首页
         */
        public static final String PAGER_HOME = HOME + "/Home";

        /**
         * 兑换页
         */
        public static final String PAGER_EXCHANGE_FRAGMENT = HOME + "/ExchangeFragment";

        /**
         * 兑换金币不足的弹窗
         */
        public static final String PAGER_EXCHANGE_GOOD_JB_FRAGMENT_DIALOG = HOME + "/ExchangeGoodJbFragmentDialog";
    }

    /**
     * 首页组件
     **/
    public static class Front {
        private static final String FRONT = "/front";

        /**
         * 首页
         */
        public static final String PAGER_FRONT = FRONT + "/Front";

    }

    public static class Blank {
        private static final String BLANK = "/blank";

        /**
         * 首页
         */
        public static final String PAGER_BLANK = BLANK + "/Blank";

    }

    public static class Face {
        private static final String FACE = "/face";

        /**
         * 首页
         */
        public static final String PAGER_FACE = FACE + "/Face";

    }

    /**
     * 秒杀组件
     */
    public static class Spike {
        private static final String SPIKE = "/spike";

        /**
         * 秒杀
         */
        public static final String PAGER_SPIKE = SPIKE + "/Spike";
    }


    /**
     * 首页抽奖页面组件
     */
    public static class HomeLottery {
        private static final String LOTTERY = "/lottery_page";

        /**
         * 秒杀
         */
        public static final String PAGER_LOTTERY = LOTTERY + "/lottery";
    }


    /**
     * 晒单页组件
     */
    public static class Unboxing {
        private static final String UNBOXING = "/unboxing";

        public static final String PAGER_UNBOXING_FRAGMENT = UNBOXING + "/unboxing";
        public static final String PAGER_UNBOXING_ACTIVITY = UNBOXING + "/UnboxingActivity";

        public static Fragment getUnboxingFragment() {
            return (Fragment) ARouter.getInstance().build(PAGER_UNBOXING_FRAGMENT).navigation();
        }

        public static void goUnboxingActivity() {
            ARouter.getInstance().build(PAGER_UNBOXING_ACTIVITY).navigation();
        }
    }


    /**
     * 抽奖组件
     */
    public static class Lottery {
        private static final String LOTTERY = "/lottery";

        /**
         * 抽奖
         */
        public static final String PAGER_LOTTERY = LOTTERY + "/lottery";

    }


    /**
     * 积分墙
     */
    public static class Integral {
        private static final String INTEGRAL = "/integral";

        /**
         * 积分墙
         */
        public static final String PAGER_INTEGRAL = INTEGRAL + "/integral";

        /**
         * 没有积分任务。但是存在次留任务
         */
        public static final String PAGER_INTEGRAL_NOT_TASK = INTEGRAL + "/WelfareNotTaskActivity";

    }


    /**
     * sdkTest
     */
    public static class TestSdk {
        private static final String TEST = "/test";

        /**
         * 首页
         */
        public static final String PAGER_TEST_SDK = TEST + "/testSdk";

        public static final String PAGER_TEST_AD_SDK = TEST + "/adSdk";


    }

    public static class User {
        private static final String USER = "/user";

        /**
         * 个人中心
         */
        public static final String PAGER_USER = USER + "/UserInfo";
        /**
         * 新版个人中心
         */
        public static final String PAGER_USER_NEW = USER + "/Mine2Fragment";

        /**
         * 用户设置
         */
        public static final String PAGER_USER_SETTING = USER + "/MineSetting";

        /**
         * 开奖详情、开奖页 的Fragment
         */
        public static final String PAGER_USER_OPEN_WINNING = USER + "/MineOpenWinningFragment";

        /**
         * 签到的Dialog弹窗
         */
        public static final String PAGER_USER_SIGN_DIALOG = USER + "/SignInMineDialog";

        /**
         * 任务奖励、激励奖励弹窗、领取弹窗 Dialog提示框
         */
        public static final String PAGER_USER_SIGN_REWARD_DIALOG = USER + "/SignInRewardMineDialog";

        /**
         * 获取开奖的Fragment
         *
         * @param period     期数。如果为 0 :表示自动计算期数
         * @param isMainLoad 是否为首页加载，T:是，F:否
         * @param isShowBack 是否显示返回按钮，T:显示按钮，F:不显示
         * @param isShowMore 是否显示往期的按钮，T:显示按钮，F:不显示
         * @param from       来源，
         *                   1：首页
         *                   2：往期开奖
         *                   3：个人参与记录
         * @return 开奖的Fragment
         */
        public static Fragment getMineOpenWinFragment(
                int period, boolean isMainLoad, boolean isShowBack, boolean isShowMore, int from) {
            return (Fragment) ARouter.getInstance()
                    .build(RouterFragmentPath.User.PAGER_USER_OPEN_WINNING)
                    .withInt("period", period)
                    .withBoolean("isMainLoad", isMainLoad)
                    .withBoolean("isShowBack", isShowBack)
                    .withBoolean("isShowMore", isShowMore)
                    .withInt("from", from)
                    .navigation();
        }

        /**
         * 获取签到弹窗
         *
         * @return
         */
        public static AbstractFragmentDialog<?> getSingDialog() {
            return (AbstractFragmentDialog<?>) ARouter.getInstance()
                    .build(User.PAGER_USER_SIGN_DIALOG)
                    .navigation();
        }

        /**
         * 个人中心任务,任务奖励、激励奖励弹窗、领取弹窗 提示框
         *
         * @param uiType UI模式：
         *               0:激励模式，1:领取模式(带自带倒计时关闭)，2：任务奖励模式
         * @return
         */
        public static AbstractFragmentDialog<?> getSingRewardDialog(int uiType) {
            return (AbstractFragmentDialog<?>) ARouter.getInstance()
                    .build(User.PAGER_USER_SIGN_REWARD_DIALOG)
                    .withInt("uiType", uiType)
                    .navigation();
        }
    }

    public static class Login {
        private static final String JDD_LOGIN = "/jdd_login";
        /**
         * 绑定手机弹窗
         */
        public static final String PAGER_BIND_PHONE_DIALOG_FRAGMENT = JDD_LOGIN + "/BindPhoneDialogFragment";
    }


    public static class Web {
        private static final String WEB = "/web";

        /**
         * 个人中心
         */
        public static final String PAGER_FRAGMENT = WEB + "/webFragment";
    }

    public static class ClassPath {
        public static final String ACTION_VIEW_MODEL = "com.donews.action.viewmodel.ActionViewModel";
        public static final String HOME_VIEW_MODEL = "com.donews.home.viewModel.HomeViewModel";
        public static final String WEB_VIEW_MODEL = "com.donews.web.viewmodel.WebViewModel";

    }

    public static class MethodPath {
        public static final String AD_LOAD_MANAGER_REFRESH_AD_CONFIG = "com.dn.sdk.AdLoadManager.refreshAdConfig";
    }

    /**
     * 任务组件
     */
    public static class Task {
        private static final String TASK = "/task";

        /**
         * 任务
         */
        public static final String PAGER_TASK = TASK + "/task";

    }

    /**
     * 集卡组件
     */
    public static class Collect {
        private static final String COLLECT = "/collect";

        /**
         * 集卡
         */
        public static final String PAGER_COLLECT = COLLECT + "/collect";

    }

}
