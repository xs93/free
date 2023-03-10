package com.donews.middle.bean.front;

import com.donews.common.contract.BaseCustomViewModel;
import com.donews.middle.views.TaskView;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class FrontConfigBean extends BaseCustomViewModel {

    @SerializedName("banner")
    private Boolean banner = false;
    @SerializedName("lotteryWinner")
    private Boolean lotteryWinner = true;
    @SerializedName("redPackage")
    private Boolean redPackage = true;
    @SerializedName("refreshInterval")
    private int refreshInterval = 60;
    @SerializedName("task")
    private Boolean task = false;
    @SerializedName("floating")
    private Boolean floating = false;
    @SerializedName("showTime")
    private Boolean showTime = false;
    @SerializedName("showTimeMsg")
    private Boolean showTimeMsg = false;
    @SerializedName("mine")
    private Boolean mine = false;
    @SerializedName("withDrawal")
    private Boolean withDrawal = false;
    @SerializedName("winCode")
    private Boolean winCode = false;
    @SerializedName("taskYyw")
    private Boolean taskYyw = false;
    @SerializedName("lottery")
    private Boolean lottery = false;

    @SerializedName("bannerItems")
    private SingleItem bannerItems;
    @SerializedName("taskItems")
    private List<YywItem> taskItems = new ArrayList<>();
    @SerializedName("floatingItems")
    private SingleItem floatingItems = new FrontConfigBean.SingleItem();
    @SerializedName("withDrawalItems")
    private SingleItem withDrawalItems = new SingleItem();
    @SerializedName("frontTask")
    private TaskItem frontTask = new TaskItem();
    @SerializedName("showTask")
    private TaskItem showTask;
    @SerializedName("showMsgTask")
    private TaskItem showMsgTask;
    @SerializedName("winCodeTask")
    private TaskItem winCodeTask;
    @SerializedName("mineTask")
    private TaskItem mineTask;
    @SerializedName("taskItem")
    private TaskItem taskItem;
    @SerializedName("lotteryItem")
    private TaskItem lotteryItem;

    public Boolean getTaskYyw() {
        return taskYyw;
    }

    public TaskItem getTaskItem() {
        return taskItem;
    }

    public Boolean getBanner() {
        return banner;
    }

    public SingleItem getBannerItems() {
        return bannerItems;
    }

    public Boolean getLotteryWinner() {
        return lotteryWinner;
    }

    public Boolean getRedPackage() {
        return redPackage;
    }

    public Boolean getTask() {
        return task;
    }

    public Boolean getLottery() {
        return lottery;
    }

    public void setLottery(Boolean lottery) {
        this.lottery = lottery;
    }

    public Boolean getShowTimeMsg() {
        return showTimeMsg;
    }

    public Boolean getShowTime() {
        return showTime;
    }

    public Boolean getFloating() {
        return floating;
    }

    public Boolean getWithDrawal() {
        return withDrawal;
    }

    public Boolean getWinCode() {
        return winCode;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public List<YywItem> getTaskItems() {
        return taskItems;
    }

    public SingleItem getFloatingItems() {
        return floatingItems;
    }

    public SingleItem getWithDrawalItems() {
        return withDrawalItems;
    }

    public Boolean getMine() {
        return mine;
    }

    public TaskItem getFrontTask() {
        return frontTask;
    }

    public TaskItem getShowTask() {
        return showTask;
    }

    public TaskItem getShowMsgTask() {
        return showMsgTask;
    }

    public TaskItem getWinCodeTask() {
        return winCodeTask;
    }

    public TaskItem getMineTask() {
        return mineTask;
    }

    public TaskItem getLotteryItem() {
        return lotteryItem;
    }

    public void setLotteryItem(TaskItem lotteryItem) {
        this.lotteryItem = lotteryItem;
    }

    public static class YywItem extends BaseCustomViewModel {
        @SerializedName("action")
        private String action;
        @SerializedName("id")
        private int id;
        @SerializedName("img")
        private String img;
        @SerializedName("title")
        private String title;
        @SerializedName("model")
        private int model;

        public String getAction() {
            return action;
        }

        public int getId() {
            return id;
        }

        public String getImg() {
            return img;
        }

        public String getTitle() {
            return title;
        }

        public int getModel() {
            return model;
        }
    }

    public static class SingleItem extends BaseCustomViewModel {
        @SerializedName("switchInterval")
        private int switchInterval = 10;
        @SerializedName("items")
        List<YywItem> items = new ArrayList<>();

        public int getSwitchInterval() {
            return switchInterval;
        }

        public List<YywItem> getItems() {
            return items;
        }
    }

    public static class TaskItem extends BaseCustomViewModel {
        @SerializedName("switchInterval")
        private int switchInterval = 10;
        @SerializedName("taskGroup")
        private int taskGroup;
        @SerializedName("items")
        List<SubItems> items = new ArrayList<>();

        public int getSwitchInterval() {
            return switchInterval;
        }

        public int getTaskGroup() {
            return taskGroup;
        }

        public List<SubItems> getItems() {
            return items;
        }
    }

    public static class SubItems extends BaseCustomViewModel {
        @SerializedName("subItems")
        private List<YywItem> subItems = new ArrayList<>();

        public List<YywItem> getSubItems() {
            return subItems;
        }
    }
}
