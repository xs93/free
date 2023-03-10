package com.donews.task.util

import com.donews.network.EasyHttp
import com.donews.network.cache.model.CacheMode
import com.donews.network.callback.SimpleCallBack
import com.donews.network.exception.ApiException
import com.donews.task.BuildConfig
import com.donews.task.bean.TaskConfigInfo
import com.google.gson.Gson
import com.orhanobut.logger.Logger

/**
 *  make in st
 *  on 2022/5/11 09:26
 */
object TaskImgControlUtil {

    //https://monetization.dev.tagtic.cn/rule/v1/calculate/free-taskConfig-dev
    private const val TASK_CONFIG = "-taskConfig"
    private const val TASK_CONFIG_URL =
        BuildConfig.BASE_CONFIG_URL + BuildConfig.APP_IDENTIFICATION + TASK_CONFIG + BuildConfig.BASE_RULE_URL

    fun init(){
        getCenterConfig()
    }

    private const val taskJson = "{\"img\":{\"luckCollectImg\":\"http://ad-static-xg.tagtic.cn/wangzhuan/file/1695e1f64da5a2ceae457d4077b0af15.jpg\",\"luckPanImg\":\"http://ad-static-xg.tagtic.cn/wangzhuan/file/f0279d2298c6573e25ee267be12edb05.jpg\"}}"
    /**
     * 连对奖励配置
     */
    private var taskControlConfig: TaskConfigInfo? = Gson().fromJson(taskJson,TaskConfigInfo::class.java)

    private fun getCenterConfig(){
        EasyHttp.get(TASK_CONFIG_URL)
            .cacheMode(CacheMode.NO_CACHE)
            .execute(object : SimpleCallBack<TaskConfigInfo>() {
                override fun onError(e: ApiException?) {
                    Logger.e(e, "")
                }

                override fun onSuccess(t: TaskConfigInfo?) {
                    t?.let {
                        taskControlConfig = it
                    }
                }
            })
    }

    fun getTaskControlConfig() = taskControlConfig

}