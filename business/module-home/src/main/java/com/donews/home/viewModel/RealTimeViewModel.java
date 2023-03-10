package com.donews.home.viewModel;

import androidx.lifecycle.MutableLiveData;

import com.donews.base.viewmodel.BaseLiveDataViewModel;
import com.donews.home.model.RealTimeModel;
import com.donews.middle.bean.home.RealTimeBean;

/**
 * <p> </p>
 * 作者： created by honeylife<br>
 * 日期： 2020/12/7 10:59<br>
 * 版本：V1.0<br>
 */
public class RealTimeViewModel extends BaseLiveDataViewModel<RealTimeModel> {

    @Override
    public RealTimeModel createModel() {
        return new RealTimeModel();
    }

    public MutableLiveData<RealTimeBean> getRealTimeData(String search) {
        return mModel.getRealTimeData(search);
    }
}
