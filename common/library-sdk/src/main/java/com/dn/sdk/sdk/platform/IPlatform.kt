package com.dn.sdk.sdk.platform

import com.dn.sdk.sdk.interfaces.loader.IRealLoader

/**
 *
 * 平台配置接口
 *
 * @author XuShuai
 * @version v1.0
 * @date 2021/10/25 16:36
 */
interface IPlatform {
    /** 返回平台对应的Loader */
    fun getLoader(): IRealLoader

    /** 通过标记key返回平台 对应的广告id */
    fun getAdIdByKey(key: String): String
}