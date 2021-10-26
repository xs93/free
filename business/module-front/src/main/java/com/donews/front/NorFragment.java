package com.donews.front;


import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.donews.base.fragment.MvvmLazyLiveDataFragment;
import com.donews.common.router.RouterFragmentPath;
import com.donews.front.adapter.NorGoodsAdapter;
import com.donews.front.bean.LotteryCategoryBean;
import com.donews.front.bean.NorGoodsBean;
import com.donews.front.cache.GoodsCache;
import com.donews.front.databinding.FrontNorFragmentBinding;
import com.donews.front.viewModel.NorViewModel;

public class NorFragment extends MvvmLazyLiveDataFragment<FrontNorFragmentBinding, NorViewModel> implements NorClickListener {

    private NorGoodsAdapter mNorGoodsAdapter;
    LotteryCategoryBean.categoryBean mCategoryBean;
    private int mPageId = 0;

    public NorFragment(LotteryCategoryBean.categoryBean categoryBean) {
        mCategoryBean = categoryBean;
    }

    @Override
    public int getLayoutId() {
        return R.layout.front_nor_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDataBinding.frontNorLoadingLl.setVisibility(View.VISIBLE);
        mDataBinding.frontNorRv.setVisibility(View.GONE);
        mNorGoodsAdapter = new NorGoodsAdapter(this.getContext(), this);
        mDataBinding.frontNorRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = 10;
            }
        });
        mDataBinding.frontNorRv.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mDataBinding.frontNorRv.setAdapter(mNorGoodsAdapter);

        NorGoodsBean norGoodsBean = GoodsCache.readGoodsBean(NorGoodsBean.class, mCategoryBean.getCategoryId());
        showNorData(norGoodsBean, true);
        loadNorData();

        initSrl();
    }

    private void initSrl() {
        mDataBinding.frontNorSrl.setEnableRefresh(false);
        mDataBinding.frontNorSrl.setEnableAutoLoadMore(false);
        mDataBinding.frontNorSrl.setOnLoadMoreListener(refreshLayout -> {
            loadNorData();
            mDataBinding.frontNorSrl.finishLoadMore();
        });
    }

    private void loadNorData() {
        mPageId++;
        mViewModel.getNetData(mCategoryBean.getCategoryId(), mPageId).observe(getViewLifecycleOwner(), norGoodsBean -> {
            if (norGoodsBean == null || norGoodsBean.getList() == null || norGoodsBean.getList().size() <= 0) {
                mPageId--;
                mDataBinding.frontNorSrl.finishLoadMoreWithNoMoreData();
                return;
            }

            showNorData(norGoodsBean, false);
            GoodsCache.saveGoodsBean(norGoodsBean, mCategoryBean.getCategoryId());
        });
    }

    private void showNorData(NorGoodsBean norGoodsBean, boolean check) {
        if (check) {
            if (norGoodsBean == null || norGoodsBean.getList() == null || norGoodsBean.getList().size() <= 0) {
                return;
            }
        }

        mNorGoodsAdapter.refreshData(norGoodsBean.getList());
        mDataBinding.frontNorLoadingLl.setVisibility(View.GONE);
        mDataBinding.frontNorRv.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(String goodsId) {
        ARouter.getInstance()
                .build(RouterFragmentPath.Lottery.PAGER_LOTTERY).withString("goods_id", goodsId)
                .navigation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mNorGoodsAdapter != null) {
            mNorGoodsAdapter.clear();
        }
    }
}
