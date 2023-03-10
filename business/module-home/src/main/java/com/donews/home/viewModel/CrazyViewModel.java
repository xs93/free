package com.donews.home.viewModel;

import androidx.lifecycle.MutableLiveData;

import com.donews.base.viewmodel.BaseLiveDataViewModel;
import com.donews.home.model.CrazyModel;
import com.donews.middle.bean.home.FactorySaleBean;
import com.donews.middle.bean.home.HomeGoodsBean;
import com.donews.middle.bean.home.RealTimeBean;

/**
 * <p> </p>
 * 作者： created by honeylife<br>
 * 日期： 2020/12/7 10:59<br>
 * 版本：V1.0<br>
 */
public class CrazyViewModel extends BaseLiveDataViewModel<CrazyModel> {

    @Override
    public CrazyModel createModel() {
        return new CrazyModel();
    }

    public MutableLiveData<HomeGoodsBean> getCrazyListData(int pageId, String src) {
        return mModel.getRealTimeData(pageId, src);
    }

    public MutableLiveData<FactorySaleBean> getFactorySale(String page_id,String page_size,String url) {
        return mModel.getFactorySale(page_id,page_size,url);
    }


}
