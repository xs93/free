package com.dn.events.events

/**
 * @author dw
 * Date on 2021/10/8
 * Description:
 *  用户登录的状态通知
 */
class MainRpDialogEvent(
        /**
         * 翻倍领取事件:
         *  1:翻倍领取-完整观看视频
         *  2:v1.3.6新版翻倍领取
         *  3: 开启日历提醒
         *  4: 积分任务翻倍领取
         *  5: 桌面通知模块->红包->关闭
         *  6：桌面通知模块->红包->翻倍领取
         *  7: 桌面通知模块->红包->关闭->领取更多弹窗->关闭
         *  8： 桌面通知模块->红包->关闭->领取更多弹窗->领取更多/桌面通知模块->红包->翻倍领取成功
         */
        val event: Int,
        val score: Float,
        val restId: String,
        val preId: String,
        val restScore: Float,
)