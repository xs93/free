package com.donews.mine.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.donews.base.activity.MvvmBaseLiveDataActivity;
import com.donews.common.router.RouterActivityPath;
import com.donews.mine.R;
import com.donews.mine.adapters.MineWithdrawalRecordAdapter;
import com.donews.mine.databinding.MineActivityWithdrawalCenterBinding;
import com.donews.mine.databinding.MineActivityWithdrawalRecordBinding;
import com.donews.mine.viewModel.WithdrawalCenterViewModel;
import com.donews.mine.viewModel.WithdrawalRecordViewModel;
import com.gyf.immersionbar.ImmersionBar;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

/**
 * 提现记录
 */
@Route(path = RouterActivityPath.Mine.PAGER_ACTIVITY_WITHDRAWAL_RECORD)
public class WithdrawalRecordActivity extends
        MvvmBaseLiveDataActivity<MineActivityWithdrawalRecordBinding, WithdrawalRecordViewModel> {

    MineWithdrawalRecordAdapter adapter;
    private boolean isRefesh = false;

    @Override
    protected int getLayoutId() {
        return R.layout.mine_activity_withdrawal_record;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersionBar.with(this)
                .statusBarColor(R.color.white)
                .navigationBarColor(R.color.white)
                .fitsSystemWindows(true)
                .autoDarkModeEnable(true)
                .init();
        initView();
    }

    public void initView() {
        mViewModel.setDataBinDing(mDataBinding, this);
        mDataBinding.titleBar.setTitle("提现记录");
        adapter = new MineWithdrawalRecordAdapter();
        mDataBinding.mineRefeshIclLayout.getRecyclerView().setLayoutManager(new LinearLayoutManager(this));
        mDataBinding.mineRefeshIclLayout.getRecyclerView().setAdapter(adapter);
        mDataBinding.mineRefeshIclLayout.getRefeshLayout().setOnRefreshListener(refreshLayout -> {
            isRefesh = true;
            mViewModel.loadRecordList(1, adapter.pageSize);
            adapter.getLoadMoreModule().setEnableLoadMore(false);
        });
        adapter.setOnLoadMoreListener((page, pageSize) -> {
            isRefesh = false;
            mViewModel.loadRecordList(page, pageSize);
        });
        mViewModel.recordListLiveData.observe(this, items -> {
            if (isRefesh) {
                adapter.setNewData(items);
            } else {
                adapter.addData(items);
                adapter.loadMoreFinish(items != null, items != null && items.size() < adapter.pageSize);
            }
            adapter.getLoadMoreModule().setAutoLoadMore(items != null && items.size() >= adapter.pageSize);
            mDataBinding.mineRefeshIclLayout.getRefeshLayout().closeHeaderOrFooter();
        });
        adapter.getLoadMoreModule().setAutoLoadMore(false);
        mDataBinding.mineRefeshIclLayout.getRefeshLayout().autoRefresh();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }

    private void initData() {
    }
}