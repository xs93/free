package com.donews.mine.viewModel;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.SPUtils;
import com.donews.base.utils.GsonUtils;
import com.donews.base.viewmodel.BaseLiveDataViewModel;
import com.donews.common.contract.LoginHelp;
import com.donews.common.router.RouterActivityPath;
import com.donews.middle.bean.mine2.reqs.DailyTasksReceiveReq;
import com.donews.middle.bean.mine2.reqs.DailyTasksReportReq;
import com.donews.middle.bean.mine2.reqs.SignReq;
import com.donews.middle.bean.mine2.resp.DailyTaskResp;
import com.donews.middle.bean.mine2.resp.DailyTasksReceiveResp;
import com.donews.middle.bean.mine2.resp.DailyTasksReportResp;
import com.donews.middle.bean.mine2.resp.SignListResp;
import com.donews.middle.bean.mine2.resp.SignResp;
import com.donews.middle.bean.mine2.resp.UserAssetsResp;
import com.donews.middle.events.TaskReportEvent;
import com.donews.middle.viewmodel.BaseMiddleViewModel;
import com.donews.mine.Api.MineHttpApi;
import com.donews.mine.BR;
import com.donews.mine.BuildConfig;
import com.donews.mine.bean.QueryBean;
import com.donews.mine.bean.resps.RecommendGoodsResp;
import com.donews.mine.bean.resps.WithdraWalletResp;
import com.donews.mine.bean.resps.WithdrawConfigResp;
import com.donews.mine.databinding.MineFragmentBinding;
import com.donews.mine.model.MineModel;
import com.donews.network.EasyHttp;
import com.donews.network.cache.model.CacheMode;
import com.donews.network.callback.SimpleCallBack;
import com.donews.network.exception.ApiException;
import com.donews.utilslibrary.utils.HttpConfigUtilsKt;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * <p> </p>
 * 作者： created by honeylife<br>
 * 日期： 2020/11/16 15:30<br>
 * 版本：V1.0<br>
 */
public class MineViewModel extends BaseLiveDataViewModel<MineModel> {


    public LifecycleOwner lifecycleOwner;
    private MineFragmentBinding dataBinding;
    private FragmentActivity baseActivity;

    public MutableLiveData<List<RecommendGoodsResp.ListDTO>> recommendGoodsLiveData = new MutableLiveData<>();
    //钱包详情
    public MutableLiveData<WithdraWalletResp> withdrawDatilesLivData =
            new MutableLiveData<>();

    //--------------- 新版个人中心相关属性(start) ------------------
    // 个人中心刷新数据的标志，通过更改标记来表示刷新数据，T:刷新数据
    public MutableLiveData<Boolean> mine2RefeshDataLive = new MutableLiveData<>();
    // 用户昵称
    public MutableLiveData<String> mine2UserName = new MutableLiveData<>("未登录");
    // 用户头像
    public MutableLiveData<String> mine2UserHead = new MutableLiveData<>();
    // 签到列表数据
    public MutableLiveData<SignListResp> mineSignLists = new MutableLiveData<>();
    // 签到结果数据(最新的签到数据)
    public MutableLiveData<SignResp> mineSignResult = new MutableLiveData<>();
    // 签到双倍领取结果数据(最新的签到数据)
    public MutableLiveData<SignResp> mineSignDounleResult = new MutableLiveData<>();
    // 完成任务的接口结果数据通知对象
    public MutableLiveData<DailyTasksReceiveResp> mineDailyTaskReceiveResult = new MutableLiveData<>();
    // 上报任务的接口结果数据通知对象
    public MutableLiveData<DailyTasksReportResp> mineDailyTaskReportResult = new MutableLiveData<>();

    //--------------- 新版个人中心相关属性(end) ------------------

    @Override
    public MineModel createModel() {
        return new MineModel();
    }

    public void setDataBinDing(MineFragmentBinding dataBinding, FragmentActivity baseActivity) {
        this.dataBinding = dataBinding;
        this.baseActivity = baseActivity;
    }

    /**
     * 获取个人中心的金币数量的订阅数据
     *
     * @return
     */
    public MutableLiveData<Integer> getMine2JBCount() {
        return BaseMiddleViewModel.getBaseViewModel().mine2JBCount;
    }

    /**
     * 获取个人中心的活跃(积分)数量的订阅数据
     *
     * @return
     */
    public MutableLiveData<Integer> getMine2JFCount() {
        return BaseMiddleViewModel.getBaseViewModel().mine2JFCount;
    }

    /**
     * 上报任务进度,将完成的任务上报给服务器进行处理
     *
     * @param req              请求参数
     * @param isUpdateLoclCoin 是否更新本地金币数据(T:更新，F:不更新)
     */
    public void requestTaskReport(DailyTasksReportReq req, boolean isUpdateLoclCoin) {
        String url = (BuildConfig.BASE_QBN_API + "activity/v1/report");
        EasyHttp.post(url)
                .upObject(req)
                .cacheMode(CacheMode.NO_CACHE)
                .execute(new SimpleCallBack<DailyTasksReportResp>() {
                    @Override
                    public void onError(ApiException e) {
                        mineDailyTaskReportResult.postValue(null);
                    }

                    @Override
                    public void onSuccess(DailyTasksReportResp resp) {
                        if (resp == null) {
                            mineDailyTaskReportResult.postValue(null);
                            return;
                        }
                        //通知上报事件成功
                        EventBus.getDefault().post(new TaskReportEvent(req.type));
                        mine2RefeshDataLive.postValue(true);
                        mineDailyTaskReportResult.postValue(resp);
                        if (isUpdateLoclCoin) {
                            if (BaseMiddleViewModel.getBaseViewModel().mine2JBCount.getValue() != null) {
                                BaseMiddleViewModel.getBaseViewModel().mine2JBCount.postValue(BaseMiddleViewModel.getBaseViewModel().mine2JBCount.getValue() + resp.coin);
                            } else {
                                BaseMiddleViewModel.getBaseViewModel().mine2JBCount.postValue(resp.coin);
                            }
                        }
                    }
                });
    }

    /**
     * 请求任务领取接口,表示任务完成了。需要领取奖励
     *
     * @param req              请求参数
     * @param isUpdateLoclCoin 是否更新本地金币数据(T:更新，F:不更新)
     */
    public void requestDailyTasksReceive(DailyTasksReceiveReq req, boolean isUpdateLoclCoin) {
        String url = (BuildConfig.BASE_QBN_API + "activity/v1/daily-tasks-receive");
        EasyHttp.post(url)
                .upObject(req)
                .cacheMode(CacheMode.NO_CACHE)
                .execute(new SimpleCallBack<DailyTasksReceiveResp>() {
                    @Override
                    public void onError(ApiException e) {
                        mineDailyTaskReceiveResult.postValue(null);
                    }

                    @Override
                    public void onSuccess(DailyTasksReceiveResp resp) {
                        if (resp == null) {
                            mineDailyTaskReceiveResult.postValue(null);
                            return;
                        }
                        if (isUpdateLoclCoin) {
                            if (BaseMiddleViewModel.getBaseViewModel().mine2JBCount.getValue() != null) {
                                BaseMiddleViewModel.getBaseViewModel().mine2JBCount.postValue(BaseMiddleViewModel.getBaseViewModel().mine2JBCount.getValue() + resp.coin);
                            } else {
                                BaseMiddleViewModel.getBaseViewModel().mine2JBCount.postValue(resp.coin);
                            }
                        }
                        mine2RefeshDataLive.postValue(true);
                        mineDailyTaskReceiveResult.postValue(resp);
                    }
                });
    }


    /**
     * （新版本）请求签到
     */
    public void requestSign(SignReq req, boolean isUpdateLoclCoin) {
        String url = (BuildConfig.BASE_QBN_API + "activity/v1/sign");
        EasyHttp.post(url)
                .upObject(req)
                .cacheMode(CacheMode.NO_CACHE)
                .execute(new SimpleCallBack<SignResp>() {
                    @Override
                    public void onError(ApiException e) {
                        mineSignResult.postValue(null);
                    }

                    @Override
                    public void onSuccess(SignResp resp) {
                        if (resp == null) {
                            if (req.double_) {
                                mineSignDounleResult.postValue(null);
                            } else {
                                mineSignResult.postValue(null);
                            }
                        } else {
                            mine2RefeshDataLive.postValue(true);
                            if (req.double_) {
                                mineSignDounleResult.postValue(resp);
                            } else {
                                mineSignResult.postValue(resp);
                            }
                            if (isUpdateLoclCoin) {
                                if (BaseMiddleViewModel.getBaseViewModel().mine2JBCount.getValue() != null) {
                                    BaseMiddleViewModel.getBaseViewModel().mine2JBCount.postValue(BaseMiddleViewModel.getBaseViewModel().mine2JBCount.getValue() + resp.coin);
                                } else {
                                    BaseMiddleViewModel.getBaseViewModel().mine2JBCount.postValue(resp.coin);
                                }
                            }
                        }
                    }
                });
    }

    /**
     * （新版本）获取签到列表
     */
    public void getsignList() {
        String url = (BuildConfig.BASE_QBN_API + "activity/v1/sign-list");
        EasyHttp.get(HttpConfigUtilsKt.withConfigParams(url, true))
                .cacheMode(CacheMode.NO_CACHE)
                .execute(new SimpleCallBack<SignListResp>() {
                    @Override
                    public void onError(ApiException e) {
                        mineSignLists.postValue(null);
                    }

                    @Override
                    public void onSuccess(SignListResp resp) {
                        if (resp.items != null) {
                            mineSignLists.postValue(resp);
                        } else {
                            mineSignLists.postValue(null);
                        }
                    }
                });
    }

    /**
     * （新版本）获取每日任务
     */
    public void getDailyTasks() {
        //调用中间件全局的对象
        BaseMiddleViewModel.getBaseViewModel()
                .getDailyTasks(null);
    }

    /**
     * （新版本）获取用户的金币、活跃度
     */
    public void getUserAssets() {
        String url = (BuildConfig.BASE_QBN_API + "activity/v1/user-assets");
        EasyHttp.get(HttpConfigUtilsKt.withConfigParams(url, true))
                .cacheMode(CacheMode.NO_CACHE)
                .execute(new SimpleCallBack<UserAssetsResp>() {
                    @Override
                    public void onError(ApiException e) {
                        BaseMiddleViewModel.getBaseViewModel().mine2JBCount.postValue(0);
                        BaseMiddleViewModel.getBaseViewModel().mine2JFCount.postValue(0);
                    }

                    @Override
                    public void onSuccess(UserAssetsResp resp) {
                        if (resp == null) {
                            BaseMiddleViewModel.getBaseViewModel().mine2JBCount.postValue(0);
                            BaseMiddleViewModel.getBaseViewModel().mine2JFCount.postValue(0);
                        } else {
                            BaseMiddleViewModel.getBaseViewModel().mine2JBCount.postValue(resp.coin);
                            BaseMiddleViewModel.getBaseViewModel().mine2JFCount.postValue(resp.active);
                        }
                    }
                });
    }

    /**
     * 钱包详情
     */
    public void getLoadWithdrawData() {
        String locJson = SPUtils.getInstance().getString("withdraw_detail");
        try {
            WithdraWalletResp queryBean = GsonUtils.fromLocalJson(locJson, WithdraWalletResp.class);
            if (queryBean == null) {
                withdrawDatilesLivData.postValue(queryBean);
            }
        } catch (Exception err) {
        }
        mModel.requestWithdraWallet(withdrawDatilesLivData);
        mModel.requestWithdrawCenterConfig(null);
    }

    /**
     * 加载精选推荐
     *
     * @param limit 需要加载多少条数据
     */
    public void loadRecommendGoods(int limit) {
        mModel.requestRecommendGoodsList(recommendGoodsLiveData, limit);
    }
}
