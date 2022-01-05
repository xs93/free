package com.donews.middle.bean.front;

import com.donews.common.contract.BaseCustomViewModel;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class FrontConfigBean extends BaseCustomViewModel {

    @SerializedName("banner")
    private Boolean banner = false;
    @SerializedName("bannerItems")
    private List<BannerItem> bannerItems = new ArrayList<>();
    @SerializedName("lotteryWinner")
    private Boolean lotteryWinner = true;
    @SerializedName("redPackage")
    private Boolean redPackage = true;
    @SerializedName("refreshInterval")
    private int refreshInterval = 60;
    @SerializedName("task")
    private Boolean task = false;
    @SerializedName("taskItems")
    private List<TaskItem> taskItems = new ArrayList<>();

    @SerializedName("floatingItem")
    private FloatingItem floatingItem = new FloatingItem();

    public FloatingItem getFloatingItem() {
        return floatingItem;
    }

    public Boolean getBanner() {
        return banner;
    }

    public List<BannerItem> getBannerItems() {
        return bannerItems;
    }

    public Boolean getLotteryWinner() {
        return lotteryWinner;
    }

    public Boolean getRedPackage() {
        return redPackage;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public Boolean getTask() {
        return task;
    }

    public List<TaskItem> getTaskItems() {
        return taskItems;
    }

    public static class BannerItem extends BaseCustomViewModel {
        @SerializedName("action")
        private String action;
        @SerializedName("id")
        private int id;
        @SerializedName("img")
        private String img;
        @SerializedName("title")
        private String title;

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
    }

    public static class TaskItem extends BaseCustomViewModel {
        @SerializedName("action")
        private String action;
        @SerializedName("icon")
        private String icon;
        @SerializedName("id")
        private int id;
        @SerializedName("title")
        private String title;
        @SerializedName("model")
        private int model;

        public int getModel() {
            return model;
        }

        public String getAction() {
            return action;
        }

        public String getIcon() {
            return icon;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }
    }

    public static class FloatingItem extends BaseCustomViewModel {
        @SerializedName("img")
        private String img;
        @SerializedName("action")
        private String action;

        public String getImg() {
            return img;
        }

        public String getAction() {
            return action;
        }
    }
}
