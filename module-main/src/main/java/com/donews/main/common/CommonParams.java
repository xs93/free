package com.donews.main.common;

import android.text.TextUtils;

import com.dn.drouter.ARouteHelper;
import com.donews.main.BuildConfig;
import com.donews.common.contract.LoginHelp;
import com.donews.common.contract.PublicConfigBean;
import com.donews.common.contract.PublicHelp;
import com.donews.common.contract.UserInfoBean;
import com.donews.common.services.config.ServicesConfig;
import com.donews.network.EasyHttp;
import com.donews.network.cache.model.CacheMode;
import com.donews.network.callback.SimpleCallBack;
import com.donews.network.exception.ApiException;
import com.donews.utilslibrary.utils.AppInfo;
import com.donews.utilslibrary.utils.DeviceUtils;
import com.donews.utilslibrary.utils.JsonUtils;
import com.donews.utilslibrary.utils.LogUtil;

import org.json.JSONObject;

/**
 * <p> </p>
 * 作者： created by honeylife<br>
 * 日期： 2020/11/17 12:03<br>
 * 版本：V1.0<br>
 */
public class CommonParams {
    /**
     * 调用网络登录或者刷新token
     */
    public static void setNetWork() {
        if (TextUtils.isEmpty(AppInfo.getToken()) ||
                !AppInfo.checkIsWXLogin()) {

//            ARouteHelper.routeAccessServiceForResult(ServicesConfig.User.LONGING_SERVICE,
//                    "getLogin", null);
            String wxCode = AppInfo.getWXLoginCode();
            if (wxCode == null || "".equals(wxCode)) {
                wxCode = ""; //防止反射不知道类型。所以不设置为空
            }
            ARouteHelper.routeAccessServiceForResult(ServicesConfig.User.LONGING_SERVICE,
                    "getLoginWx", new Object[]{
                            wxCode
                    });
        } else {
            ARouteHelper.routeAccessServiceForResult(ServicesConfig.User.LONGING_SERVICE,
                    "getRefreshToken", null);
        }
    }


    public static void getCommonNetWork() {
        EasyHttp.get(BuildConfig.BASE_CONFIG_URL + BuildConfig.APP_IDENTIFICATION + "-adPopupConfig" + BuildConfig.BASE_RULE_URL + JsonUtils.getCommonJson(false))
                .cacheMode(CacheMode.NO_CACHE)
                .execute(new SimpleCallBack<PublicConfigBean>() {
                    @Override
                    public void onError(ApiException e) {
                        LogUtil.d(e.getCode() + e.getMessage());
                    }

                    @Override
                    public void onSuccess(PublicConfigBean publicConfigBean) {
                        LogUtil.d(publicConfigBean.toString());
                        PublicHelp.getInstance().setPublicConfigBean(publicConfigBean);
                    }
                });

        putInvCode();
    }


    private static void putInvCode() {
        UserInfoBean infoBean = LoginHelp.getInstance().getUserInfoBean();
        if (infoBean != null && infoBean.isInvited()) {
            return;
        }
        String code = DeviceUtils.getInvCode();
        if (TextUtils.isEmpty(code)) {
            return;
        }
        String data = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("invite_code", code);
            data = jsonObject.toString();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        EasyHttp.put(BuildConfig.LOGIN_URL + "share/v1/code")
                .upJson(data).cacheMode(CacheMode.NO_CACHE)
                .execute(new SimpleCallBack<Object>() {
                    @Override
                    public void onError(ApiException e) {

                    }

                    @Override
                    public void onSuccess(Object o) {

                    }

                    @Override
                    public void onCompleteOk() {
                        super.onCompleteOk();

                        if (infoBean != null) {
                            infoBean.setInvited(true);
                        }

                    }
                });
    }

}
