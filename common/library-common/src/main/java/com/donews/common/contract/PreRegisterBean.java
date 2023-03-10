package com.donews.common.contract;

import com.google.gson.annotations.SerializedName;

public class PreRegisterBean extends BaseCustomViewModel{
    @SerializedName("code")
    public int code;
    @SerializedName("msg")
    public String msg;
    @SerializedName("user_id")
    public String userId;
    @SerializedName("register_time")
    public String registerTime;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String getUserId() {
        return userId;
    }

    public String getRegisterTime() {
        return registerTime;
    }
}
