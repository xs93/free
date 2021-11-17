package com.donews.home.model;

import android.annotation.SuppressLint;

import androidx.lifecycle.MutableLiveData;

import com.donews.base.model.BaseLiveDataModel;
import com.donews.home.api.HomeApi;
import com.donews.middle.bean.home.SearchGoodsBeanV2;
import com.donews.middle.bean.home.SearchResultTbBean;
import com.donews.network.EasyHttp;
import com.donews.network.cache.model.CacheMode;
import com.donews.network.callback.SimpleCallBack;
import com.donews.network.exception.ApiException;

/**
 * <p> </p>
 * 作者： created by dw<br>
 * 日期： 2020/12/7 11:12<br>
 * 版本：V1.0<br>
 */
public class TbModel extends BaseLiveDataModel {


    /**
     * 获取网路数据
     *
     * @return 返回 homeBean的数据
     */
    @SuppressLint("DefaultLocale")
    public MutableLiveData<SearchGoodsBeanV2> getSearchResultData(String keyWord, int pageId, int src) {
        MutableLiveData<SearchGoodsBeanV2> mutableLiveData = new MutableLiveData<>();

        EasyHttp.get(String.format(HomeApi.searchGoodsListUrl, pageId, keyWord, src))
                .cacheMode(CacheMode.NO_CACHE)
                .execute(new SimpleCallBack<SearchGoodsBeanV2>() {

                    @Override
                    public void onError(ApiException e) {
                        mutableLiveData.postValue(null);
                    }

                    @Override
                    public void onSuccess(SearchGoodsBeanV2 searchGoodsBeanV2) {
                        mutableLiveData.postValue(searchGoodsBeanV2);
                    }
                });

        return mutableLiveData;
    }

}
