package com.donews.middle.bean.home;

import com.donews.common.contract.BaseCustomViewModel;

/**
 * 赚金币的返回参数
 */
public class HomeEarnCoinResp extends BaseCustomViewModel {
    // 获得的金币数量
    public int coin;
    // 剩余暴击次数
    public int rest_times;
    // 开启暴击还需要多少次广告
    public int open_times;
}
