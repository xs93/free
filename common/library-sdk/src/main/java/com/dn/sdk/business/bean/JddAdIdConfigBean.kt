package com.dn.sdk.business.bean

import com.google.gson.annotations.SerializedName


/**
 * 奖多多id配置
 *
 * @author XuShuai
 * @version v1.0
 * @date 2021/10/27 10:12
 */
class JddAdIdConfigBean(
    @SerializedName("openAd")
    var openAd: Boolean = true,
    @SerializedName("bjcsj")
    var bjcsj: Int = 10,
    @SerializedName("bjdn")
    var bjdn: Int = 0,
    @SerializedName("csjAdIdConfigBean")
    var csjAdIdConfigBean: CsjAdIdConfigBean = CsjAdIdConfigBean(),
    @SerializedName("dnAdIdConfigBean")
    var dnAdIdConfigBean: DnAdIdConfigBean = DnAdIdConfigBean()
) : JddBaseAdIdConfigBean()