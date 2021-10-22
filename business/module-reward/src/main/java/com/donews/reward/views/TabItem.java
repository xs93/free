package com.donews.front.views;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.donews.front.R;
import com.donews.utilslibrary.utils.LogUtil;

public class TabItem extends FrameLayout {

    private final TextView mTitleTv;
    private final View mBgView;

    public TabItem(@NonNull Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.front_tab_item, this, true);
        mTitleTv = findViewById(R.id.front_tab_item_title);
        mBgView = findViewById(R.id.front_tab_item_bg);
        LayoutParams layoutParams = (LayoutParams) mBgView.getLayoutParams();
        layoutParams.width = mTitleTv.getWidth();
        mBgView.setLayoutParams(layoutParams);
    }

    public void setTitle(String title) {
        mTitleTv.setText(title);
        mBgView.setVisibility(GONE);
        LogUtil.e("xx:" + title);
    }

    public void selected() {
        mTitleTv.setTextSize(17);
        mTitleTv.setTextColor(Color.parseColor("#F64A43"));
        mBgView.setVisibility(VISIBLE);
        LayoutParams layoutParams = (LayoutParams) mBgView.getLayoutParams();
        layoutParams.width = mTitleTv.getWidth();
        mBgView.setLayoutParams(layoutParams);
    }

    public void unSelected() {
        mTitleTv.setTextSize(16);
        mTitleTv.setTextColor(Color.parseColor("#3A3A3A"));
        mBgView.setVisibility(GONE);
    }

}