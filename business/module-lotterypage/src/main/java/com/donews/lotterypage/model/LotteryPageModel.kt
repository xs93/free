package com.donews.lotterypage.model

import androidx.lifecycle.MutableLiveData
import com.doing.spike.bean.SpikeBean
import com.donews.base.model.BaseLiveDataModel
import com.donews.lotterypage.base.LotteryPageBean
import com.donews.network.EasyHttp
import com.donews.network.cache.model.CacheMode
import com.donews.network.callback.SimpleCallBack
import com.donews.network.exception.ApiException

class LotteryPageModel : BaseLiveDataModel() {
    private val URL = "https://qbna.dev.tagtic.cn/lottery/v1/goods-list"
    /**
     * 获取网路数据
     *
     * @return 返回 SpikeBean
     */
    fun getNetData(
        mutableLiveData: MutableLiveData<LotteryPageBean>,
    ) {
        unDisposable()
        addDisposable(
            EasyHttp.get(URL)
                .cacheMode(CacheMode.NO_CACHE)
                .execute(object : SimpleCallBack<LotteryPageBean?>() {
                    override fun onError(e: ApiException) {
                        mutableLiveData.postValue(null)
                    }


                    override fun onSuccess(t: LotteryPageBean?) {
                        if (t != null) {
                            mutableLiveData.postValue(t)
                        }
                    }
                })
        )
    }


}