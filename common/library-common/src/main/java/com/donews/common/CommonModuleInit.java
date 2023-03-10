package com.donews.common;


import com.donews.base.base.BaseApplication;
import com.donews.common.appconfig.AppCommonConfigUtils;
import com.donews.common.lifecycle.SimpleApplicationObServer;
import com.donews.utilslibrary.utils.AppStatusUtils;
import com.tencent.mmkv.MMKV;

/**
 * 应用模块:
 * <p>
 * 类描述: 通用库 & 基础库 自身初始化操作
 * <p>
 * <p>
 * 作者： created by honeylife<br>
 * 日期： 2020-02-25
 */
public class CommonModuleInit implements IModuleInit {
    @Override
    public boolean onInitAhead(BaseApplication application) {

        MMKV.initialize(application);

        new SimpleApplicationObServer().register();
        //app第一次打开时间
        AppStatusUtils.saveAppInstallTime();
        //app 公共配置
        AppCommonConfigUtils.INSTANCE.initConfig();

        //桌面通知模块数据结构配置项
        NotifyLuncherConfigManager.update();

        //重置插屏数据
//        InterstitialAdCount.INSTANCE.reset();

        return false;
    }

    @Override
    public boolean onInitLow(BaseApplication application) {
        return false;
    }

}
