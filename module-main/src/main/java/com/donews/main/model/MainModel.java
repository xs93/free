package com.donews.main.model;

import androidx.lifecycle.MutableLiveData;

import com.donews.base.model.BaseLiveDataModel;
import com.donews.main.BuildConfig;
import com.donews.main.entitys.resps.HistoryPeopleLottery;
import com.donews.main.entitys.resps.RewardHistoryBean;
import com.donews.network.EasyHttp;
import com.donews.network.cache.model.CacheMode;
import com.donews.network.callback.SimpleCallBack;
import com.donews.network.exception.ApiException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * @author lcl
 * Date on 2021/11/22
 * Description:
 */
public class MainModel extends BaseLiveDataModel {

    /**
     * 请求个人参与记录的数据（参与记录）
     *
     * @param livData 通知数据
     * @return
     */
    public Disposable requestPeopleLottery(MutableLiveData<List<HistoryPeopleLottery.Period>> livData) {
        Disposable disop = EasyHttp.get(BuildConfig.API_LOTTERY_URL + "v1/history-people-lottery")
                .params("offset", "1")
                .params("limit", "2")
                .isShowToast(false)
                .cacheMode(CacheMode.NO_CACHE)
                .execute(new SimpleCallBack<HistoryPeopleLottery>() {
                    @Override
                    public void onError(ApiException e) {
                        livData.postValue(null);
                    }

                    @Override
                    public void onSuccess(HistoryPeopleLottery queryBean) {
                        if (queryBean.list == null) {
                            livData.postValue(new ArrayList());
                        } else {
                            livData.postValue(queryBean.list);
                        }
                    }
                });
        addDisposable(disop);
        return disop;
    }

    /**
     * 往期开奖。则可以获取到当前最新一期的(往期开奖)
     *
     * @param livData 通知数据
     * @return
     */
    public Disposable requestRewarHistory(MutableLiveData<List<RewardHistoryBean.RewardBean>> livData) {
        Disposable disop = EasyHttp.get(BuildConfig.API_LOTTERY_URL + "/v1/list-open-lottery-record")
                .params("offset", "1")
                .params("limit", "1")
                .cacheMode(CacheMode.NO_CACHE)
                .execute(new SimpleCallBack<RewardHistoryBean>() {
                    @Override
                    public void onError(ApiException e) {
                        livData.postValue(null);
                    }

                    @Override
                    public void onSuccess(RewardHistoryBean queryBean) {
                        if (queryBean.getList() == null) {
                            livData.postValue(new ArrayList());
                        } else {
                            livData.postValue(queryBean.getList());
                        }
                    }
                });
        addDisposable(disop);
        return disop;
    }

}