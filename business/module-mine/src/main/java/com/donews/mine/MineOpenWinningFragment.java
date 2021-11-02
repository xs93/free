package com.donews.mine;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.dn.drouter.ARouteHelper;
import com.dn.events.events.LoginUserStatus;
import com.donews.common.ad.business.monitor.PageMonitor;
import com.donews.common.base.MvvmLazyLiveDataFragment;
import com.donews.base.utils.ToastUtil;
import com.donews.common.router.RouterActivityPath;
import com.donews.common.router.RouterFragmentPath;
import com.donews.mine.adapters.MineWinningCodeAdapter;
import com.donews.mine.databinding.MineFragmentWinningCodeBinding;
import com.donews.mine.viewModel.MineOpenWinningViewModel;
import com.donews.mine.views.scrollview.BarrageView;
import com.donews.utilslibrary.analysis.AnalysisUtils;
import com.donews.utilslibrary.dot.Dot;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


/**
 * 开奖页面的Framgnt
 */
@Route(path = RouterFragmentPath.User.PAGER_USER_OPEN_WINNING)
public class MineOpenWinningFragment extends
        MvvmLazyLiveDataFragment<MineFragmentWinningCodeBinding, MineOpenWinningViewModel> {

    @Autowired(name = "period")
    public int period = 0;
    //是否显示返回按钮
    @Autowired(name = "isShowBack")
    public boolean isShowBack = true;
    //是否显示往期
    @Autowired(name = "isShowMore")
    public boolean isShowMore = true;

    private int headRes = R.layout.mine_frm_winning_code_list_head;
    private int headNotOpenWinRes = R.layout.mine_frm_winning_code_list_not_open_win_head;
    //adapter的headView(已开奖)
    private ViewGroup adapterOpenWinHead = null;
    //adapter的headView(未开奖)
    private ViewGroup adapterNotOpenWinHead = null;
    MineWinningCodeAdapter adapter;
    private TextView timeHH;
    private TextView timeMM;
    private TextView timeSS;
    private BarrageView barrageView;
    private boolean isLoadStart = false;
    private boolean isRefesh = false;
    private int scrollTop0Count = 0; //是否初始加载数据
    private boolean isInitCommData = false; //是否初始加载了数据

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new PageMonitor().attach(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.mine_fragment_winning_code;
    }

    @Override
    protected void onFragmentFirstVisible() {
        EventBus.getDefault().register(this);
        super.onFragmentFirstVisible();
        initView();
    }

    @Subscribe
    public void userLoginStatus(LoginUserStatus event) {
        if (event.getStatus() == 1 ||
                event.getStatus() == 2) {
            if (mViewModel.detailLivData.getValue() != null) {
                mDataBinding.mainWinCodeRefresh.autoRefresh();
            }
        }
    }

    /**
     * 设置指定期数
     *
     * @param period
     */
    public void setPeriod(int period) {
        this.period = period;
        try {
            //刷新一次数据
            mDataBinding.mainWinCodeRefresh.autoRefresh();
        } catch (Exception e) {
        }
    }

    /**
     * 设置指定期数
     *
     * @param isBack 是否显示返回按钮,T:显示  F:不显示
     */
    public MineOpenWinningFragment setIsShowBack(boolean isBack) {
        this.isShowBack = isBack;
        try {
            if (isShowBack) {
                mDataBinding.mainWinCodeTitleBack.setVisibility(View.VISIBLE);
            } else {
                mDataBinding.mainWinCodeTitleBack.setVisibility(View.GONE);
            }
        } catch (
                Exception e) {
        }
        return this;
    }

    /**
     * 是否显示往期
     *
     * @param isMore 是否显示往期 ,T:显示  F:不显示
     */
    public MineOpenWinningFragment setIsShowMore(boolean isMore) {
        this.isShowMore = isMore;
        try {
            if (isShowMore) {
                mDataBinding.mainWinCodeTitleRight.setVisibility(View.VISIBLE);
            } else {
                mDataBinding.mainWinCodeTitleRight.setVisibility(View.GONE);
            }
        } catch (
                Exception e) {
        }
        return this;
    }

    @Override
    public void onResume() {
        super.onResume();
        barrageView.resumeScroll();
        onRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        barrageView.pauseScroll();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (barrageView != null) {
            barrageView.stopScroll();
        }
    }

    private void onRefresh() {
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        mViewModel.destroy();
        super.onDestroy();
    }

    @SuppressLint("WrongConstant")
    private void initView() {
        ARouter.getInstance().inject(this);
        if (period == 0) {
            mViewModel.isAutoPeriod = true;
            AnalysisUtils.onEventEx(this.getActivity(), Dot.Page_Lottery); //开奖事件
        }
        mViewModel.setDataBinDing(mDataBinding, getBaseActivity());
        adapterOpenWinHead = (ViewGroup) View.inflate(getBaseActivity(), headRes, null);
        barrageView = adapterOpenWinHead.findViewById(R.id.mine_win_code_scan_scroll_v);
        adapterNotOpenWinHead = (ViewGroup) View.inflate(getBaseActivity(), headNotOpenWinRes, null);
        timeHH = adapterNotOpenWinHead.findViewById(R.id.mine_frm_win_h);
        timeMM = adapterNotOpenWinHead.findViewById(R.id.mine_frm_win_m);
        timeSS = adapterNotOpenWinHead.findViewById(R.id.mine_frm_win_s);
        adapterOpenWinHead.findViewById(R.id.mine_win_code_sele_rules).setOnClickListener((v) -> {
            Bundle bundle = new Bundle();
            bundle.putString("url",
                    "https://recharge-web.xg.tagtic.cn/jdd/index.html#/rule");
            bundle.putString("title", "中奖规则");
            ARouteHelper.routeSkip(RouterActivityPath.Web.PAGER_WEB_ACTIVITY, bundle);
            AnalysisUtils.onEventEx(getActivity(), Dot.Page_DetailRule);
        });
        adapterOpenWinHead.findViewById(R.id.mine_win_code_scan_all).setOnClickListener((v) -> {
            //去往晒单页
            ARouter.getInstance().build(RouterActivityPath.Main.PAGER_MAIN)
                    .withInt("position", 1)
                    .navigation();
        });
        adapter = new MineWinningCodeAdapter();
        //设置没有更多数据
        adapter.getLoadMoreModule().loadMoreEnd();
        adapter.setOnLoadMoreListener((page, pageSize) -> {
            adapter.loadMoreFinish(true, true);
        });
        setIsShowMore(isShowMore);
        mDataBinding.mainWinCodeTitleRight.setOnClickListener(v -> {
            ARouter.getInstance()
                    .build(RouterActivityPath.Mine.PAGE_MINE_REWARD_HISTORY)
                    .navigation();
        });
        mDataBinding.mainWinCodeRefresh.setOnRefreshListener(refreshLayout -> {
//            mViewModel.loadData(period,false);
            isRefesh = true;
            isLoadStart = true;
            mViewModel.loadData(period, false);
            if (isInitCommData) {
                mViewModel.loadRecommendData(adapter.pageSize);
            }
        });
        adapter.setOnLoadMoreListener((page, pageSize) -> {
            isRefesh = false;
            mViewModel.loadRecommendData(adapter.pageSize);
            mDataBinding.mainWinCodeRefresh.setEnableRefresh(false);
        });
        mDataBinding.mineWinCodeList.setLayoutManager(new GridLayoutManager(getBaseActivity(), 2));
        mDataBinding.mineWinCodeList.setAdapter(adapter);
        setIsShowBack(isShowBack);
        mDataBinding.mainWinCodeTitleBack.setOnClickListener((v) -> {
            getBaseActivity().finish();
        });
        mViewModel.openWinPeriod.observe(this, peri -> {
            if (!isLoadStart) {
                return;
            }
            if (peri != null && peri > 0) {
                //获取服务器时间
                updateUI(-1);
                mViewModel.getServiceTime();
            } else {
                mDataBinding.mineWinCodeListStatus.showError();
                mDataBinding.mainWinCodeRefresh.finishRefresh();
                ToastUtil.showShort(getBaseActivity(), "数据获取异常");
            }
        });
        mViewModel.serviceTimeLiveData.observe(this, time -> {
            if (!isLoadStart) {
                return;
            }
            if (time != null && time.length() > 0) {
                //计算服务器视图
                mViewModel.calculateServiceTime();
            } else {
                mDataBinding.mineWinCodeListStatus.showError();
                mDataBinding.mineWinCodeListStatus.findViewById(R.id.error_view)
                        .setOnClickListener(v -> {
                            mDataBinding.mainWinCodeRefresh.autoRefresh();
                        });
                mDataBinding.mineWinCodeListStatus.findViewById(R.id.error_view)
                        .setOnClickListener(v -> {
                            mDataBinding.mainWinCodeRefresh.autoRefresh();
                        });
                mDataBinding.mainWinCodeRefresh.finishRefresh();
            }
        });
        //最终由此回调。显示为页面状态。此回调中才能确定页面是否开奖状态
        mViewModel.openWinCountdown.observe(this, countDown -> {
            if (!isLoadStart) {
                return;
            }
            if (countDown == null) {
                mDataBinding.mainWinCodeRefresh.finishRefresh();
            } else {
                if (countDown > 0) { //未开奖
                    //更新未开奖的UI
                    updateUI(0);
                    mDataBinding.mainWinCodeRefresh.finishRefresh();
                } else { //已开奖
                    //强制调用。否则将会地柜调用
                    mViewModel.cancelNotOpenWinCountDownTimer();
                    mViewModel.loadData(
                            mViewModel.openWinPeriod.getValue(), true);
                }
            }
        });
        mViewModel.detailLivData.observe(this, (data) -> {
            if (!isLoadStart) {
                return;
            }
            mDataBinding.mainWinCodeRefresh.finishRefresh();
            //更新已开奖的UI
            updateUI(1);
            if (isRefesh) {
                if (scrollTop0Count < 2) {
                    mDataBinding.mineWinCodeList.scrollToPosition(0);
                }
                scrollTop0Count++;
            }
        });
        mViewModel.recommendLivData.observe(this, (data) -> {
            if (!isLoadStart) {
                return;
            }
            //更新推荐数据
            mDataBinding.mainWinCodeRefresh.setEnableRefresh(true);
            adapter.getLoadMoreModule().setAutoLoadMore(data != null && !data.isEmpty());
            adapter.loadMoreFinish(true, data != null && data.size() < adapter.pageSize);
            if (data != null) {
                if (isRefesh) {
                    adapter.setNewData(data);
                    scrollTop0Count++;
                } else {
                    adapter.addData(data);
                }
                if (scrollTop0Count < 2) {
                    mDataBinding.mineWinCodeList.scrollToPosition(0);
                }
            }
        });
        mViewModel.awardLiveData.observe(this, data -> {
            if (data != null) {
                barrageView.refreshData(data.getList());
            }
        });
        adapter.getLoadMoreModule().setAutoLoadMore(false);
        mDataBinding.mainWinCodeRefresh.autoRefresh();
    }

    //添加列表的已开奖头
    private void addListHead() {
        if (adapterOpenWinHead.getParent() != null) {
            return; //已经添加了
        }
        ViewGroup.LayoutParams lp = adapterOpenWinHead.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        adapterOpenWinHead.setLayoutParams(lp);
        adapter.addHeaderView(adapterOpenWinHead);
    }

    //添加列表的未开奖头
    private void addListNotOpenWinHead() {
        if (adapterNotOpenWinHead.getParent() != null) {
            return; //已经添加了
        }
        ViewGroup.LayoutParams lp = adapterNotOpenWinHead.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        adapterNotOpenWinHead.setLayoutParams(lp);
        adapter.addHeaderView(adapterNotOpenWinHead);
    }

    /**
     * 更新UI
     *
     * @param type -1:为止类型只更新通用部分，0:未开奖UI，1：已开奖UI
     */
    private void updateUI(int type) {
        if (period > 0) {
            mDataBinding.mainWinCodeTitleName.setText(period + "期");
        } else {
            Integer autoPeriod = mViewModel.openWinPeriod.getValue();
            if (autoPeriod != null && autoPeriod > 0) {
                mDataBinding.mainWinCodeTitleName.setText(autoPeriod + "期");
            }
        }
        if (type == -1) {
            return; //以下是私有部分。如果只是更新公用部分。那么直接跳过
        }
        if (type == 0) {
            mDataBinding.mineWinCodeListStatus.showContent();
            adapterOpenWinHead.setVisibility(View.GONE);
            adapterNotOpenWinHead.setVisibility(View.VISIBLE);
            addListNotOpenWinHead();
            mViewModel.updateCountDownUI(timeHH, timeMM, timeSS);
            if (!isInitCommData) {
                isInitCommData = true;
                mViewModel.loadRecommendData(adapter.pageSize); //加载推荐数据
            }
        } else if (type == 1) {
            adapterNotOpenWinHead.setVisibility(View.GONE);
            adapterOpenWinHead.setVisibility(View.VISIBLE);
            addListHead();
            mViewModel.updateData( //更新其他数据
                    adapterOpenWinHead);
            if (mViewModel.detailLivData.getValue() == null) {
                mDataBinding.mineWinCodeListStatus.showError();
                mDataBinding.mineWinCodeListStatus.findViewById(R.id.error_view)
                        .setOnClickListener(v -> {
                            mDataBinding.mainWinCodeRefresh.autoRefresh();
                        });
                return;
            }
            mDataBinding.mineWinCodeListStatus.showContent();
            mViewModel.loadAwardData(); //加载滚动通知
            mViewModel.uiPosResetBuild( //UI位置顺序构建
                    adapterOpenWinHead);
            mViewModel.addSelectGoods( //添加中奖商品
                    adapterOpenWinHead);
            mViewModel.addAddToGoods( //添加参与商品
                    adapterOpenWinHead, true);
            mViewModel.addSelectToNames( //添加中奖名单
                    adapterOpenWinHead, true);
            if (!isInitCommData) {
                isInitCommData = true;
                mViewModel.loadRecommendData(adapter.pageSize); //加载推荐数据
            }
        }
    }
}
