package com.donews.notify.launcher.utils.funs;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.arch.core.util.Function;

import com.donews.base.utils.glide.GlideUtils;
import com.donews.common.NotifyLuncherConfigManager;
import com.donews.notify.R;
import com.donews.notify.launcher.NotifyAnimationView;
import com.donews.notify.launcher.configs.baens.Notify2DataConfigBean;
import com.donews.notify.launcher.utils.AbsNotifyInvokTask;
import com.donews.notify.launcher.utils.JumpActionUtils;
import com.donews.notify.launcher.utils.fix.FixTagUtils;
import com.donews.notify.launcher.utils.fix.covert.ResConvertUtils;

import java.util.List;
import java.util.Random;

/**
 * @author lcl
 * Date on 2022/1/5
 * Description:
 * ui模板1的处理逻辑
 */
public class NotifyItemTypeLottTop1Impl extends AbsNotifyInvokTask {

    @Override
    public boolean itemClick(NotifyAnimationView targetView, Notify2DataConfigBean.UiTemplat uiTemplat) {
        return JumpActionUtils.jump((Activity) targetView.getContext(), uiTemplat);
    }

    @Override
    public void bindTypeData(NotifyAnimationView targetView, Runnable lastBindTask) {
        if (targetView.getChildCount() <= 0 || targetView.getTag() == null) {
            targetView.setHideDuration(NotifyLuncherConfigManager.getInstance().getAppGlobalConfigBean().notifyShowTime);
            targetView.start();
            if (lastBindTask != null) {
                lastBindTask.run(); //调用外部更新视图
            }
            return; //没有添加视图
        }
        //获取视图
        //通知的图标
        ImageView icon = targetView.findViewById(R.id.notify_item_lott_icon);
        //通知的title
        TextView title = targetView.findViewById(R.id.notify_item_lott_title);
        //昵称(微信昵称)
        TextView name = targetView.findViewById(R.id.notify_item_lott_name);
        //描述信息
        TextView desc = targetView.findViewById(R.id.notify_item_lott_desc);
        //右侧商品icon
        ImageView goodIcon = targetView.findViewById(R.id.notify_item_lott_jp);

        Notify2DataConfigBean.UiTemplat uiTemplat = (Notify2DataConfigBean.UiTemplat) targetView.getTag();

        //设置值
        if (uiTemplat.getIconLeftTopMin() == null || uiTemplat.getIconLeftTopMin().isEmpty()) {
            icon.setVisibility(View.GONE);
        } else {
            ResConvertUtils.buidIcon(icon, uiTemplat.getIconLeftTopMin());
        }
        title.setText(FixTagUtils.convertHtml(uiTemplat.getTitle()));
        name.setText(FixTagUtils.convertHtml(uiTemplat.getName()));
        desc.setText(FixTagUtils.convertHtml(uiTemplat.getDesc()));
        List<Notify2DataConfigBean.UiTemplatImageItem> ls = uiTemplat.getGoodImages();
        if (ls == null || ls.isEmpty()) {
            goodIcon.setVisibility(View.GONE);
        } else {
            Notify2DataConfigBean.UiTemplatImageItem item = ls.get(new Random().nextInt(ls.size()));
            GlideUtils.loadImageView(targetView.getContext(), item.goodIcon, goodIcon);
        }

        //回调视图任务
        if (lastBindTask != null) {
            lastBindTask.run(); //调用外部更新视图
        }
        //开始显示
        targetView.setHideDuration(NotifyLuncherConfigManager.getInstance().getAppGlobalConfigBean().notifyShowTime);
        targetView.start();
    }
}
