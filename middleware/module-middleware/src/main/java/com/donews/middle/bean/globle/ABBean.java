package com.donews.middle.bean.globle;

import com.google.gson.annotations.SerializedName;

public class ABBean {
    @SerializedName("openAB")
    private boolean openAB;
    private boolean openVideoToast = true;
    private boolean openAutoLottery = true;

    public void setOpenAB(boolean openAB) {
        this.openAB = openAB;
    }

    public boolean isOpenAutoLottery() {
        return openAutoLottery;
    }

    public void setOpenAutoLottery(boolean openAutoLottery) {
        this.openAutoLottery = openAutoLottery;
    }

    public boolean isOpenVideoToast() {
        return openVideoToast;
    }

    public void setOpenVideoToast(boolean openVideoToast) {
        this.openVideoToast = openVideoToast;
    }

    @Override
    public String toString() {
        return "ABBean{" +
                "ab='" + openAB + '\'' +
                '}';
    }

    public boolean isOpenAB() {
        return openAB;
    }

    public boolean getAb() {
        return openAB;
    }

    public void setAb(boolean openAB) {
        this.openAB = openAB;
    }
}
