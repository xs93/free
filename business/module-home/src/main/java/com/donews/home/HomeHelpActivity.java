package com.donews.home;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.donews.utilslibrary.analysis.AnalysisUtils;
import com.donews.utilslibrary.dot.Dot;
import com.gyf.immersionbar.ImmersionBar;

/**
 * <p> </p>
 * 作者： created by dw<br>
 * 日期： 2021/10/13 14:13<br>
 * 版本：V1.0<br>
 */
public class HomeHelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_jdd_help);

        ImmersionBar.with(this)
                .statusBarColor(R.color.white)
                .navigationBarColor(R.color.black)
                .fitsSystemWindows(true)
                .autoDarkModeEnable(true)
                .init();

        findViewById(R.id.home_help_back).setOnClickListener(v -> finish());

    }
}