package com.donews.mine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.BarUtils;
import com.dn.drouter.ARouteHelper;
import com.dn.events.events.LoginUserStatus;
import com.dn.events.events.LotteryStatusEvent;
import com.dn.sdk.AdCustomError;
import com.dn.sdk.listener.interstitialfull.SimpleInterstitialFullListener;
import com.donews.base.utils.ToastUtil;
import com.donews.common.base.MvvmLazyLiveDataFragment;
import com.donews.common.contract.LoginHelp;
import com.donews.common.router.RouterActivityPath;
import com.donews.common.router.RouterFragmentPath;
import com.donews.middle.adutils.InterstitialFullAd;
import com.donews.middle.adutils.adcontrol.AdControlManager;
import com.donews.middle.views.TaskView;
import com.donews.mine.adapters.MineWinningCodeAdapter;
import com.donews.mine.bean.resps.RecommendGoodsResp;
import com.donews.mine.databinding.MineFragmentWinningCodeBinding;
import com.donews.mine.viewModel.MineOpenWinningViewModel;
import com.donews.utilslibrary.analysis.AnalysisUtils;
import com.donews.utilslibrary.dot.Dot;
import com.donews.utilslibrary.utils.AppInfo;
import com.donews.yfsdk.moniter.PageMonitor;
import com.donews.yfsdk.monitor.InterstitialFullAdCheck;
import com.donews.yfsdk.monitor.PageMoniterCheck;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


/**
 * 开奖页面的Framgnt
 */
@Route(path = RouterFragmentPath.User.PAGER_USER_OPEN_WINNING)
public class MineOpenWinningFragment extends
        MvvmLazyLiveDataFragment<MineFragmentWinningCodeBinding, MineOpenWinningViewModel> {

    @Autowired(name = "period")
    public int period = 0;
    //是否为首页加载
    @Autowired(name = "isMainLoad")
    public boolean isMainLoad = false;
    //是否显示返回按钮
    @Autowired(name = "isShowBack")
    public boolean isShowBack = true;
    //是否显示往期
    @Autowired(name = "isShowMore")
    public boolean isShowMore = true;
    //是否显示往期
    @Autowired(name = "from")
    public int from = -1;

    private int headRes = R.layout.mine_frm_winning_code_list_head;
    private int notOpenRecordHeadRes = R.layout.mine_frm_winning_code_list_not_record_head;
    private int headNotOpenWinRes = R.layout.mine_frm_winning_code_list_not_open_win_head;
    //adapter的headView(已开奖)
    private ViewGroup adapterOpenWinHead = null;
    //adapter的headView(未开奖)
    private ViewGroup adapterNotOpenWinHead = null;
    //adapter的headView(未开奖时候查看我的参与记录视图)
    private ViewGroup adapterNotOpenMyAddRecordHead = null;
    MineWinningCodeAdapter adapter;
    private TextView timeHH;
    private TextView timeHH1;
    private TextView timeMM;
    private TextView timeMM1;
    private TextView timeSS;
    private TextView timeSS1;
    private boolean isLoadStart = false;
    private boolean isRefesh = false;
    private int scrollTop0Count = 0; //是否初始加载数据
    private boolean isInitCommData = false; //是否初始加载了数据

    private TaskView mTaskView = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new PageMonitor().attach(this, new PageMonitor.PageListener() {
            @NonNull
            @Override
            public AdCustomError checkShowAd() {
                if (AdControlManager.INSTANCE.getAdControlBean().getUseInstlFullWhenSwitch()) {
                    return InterstitialFullAdCheck.INSTANCE.isEnable();
                } else {
                    return InterstitialFullAdCheck.INSTANCE.isEnable();
                }
            }

            @Override
            public int getIdleTime() {
                return AdControlManager.INSTANCE.getAdControlBean().getNoOperationDuration();
            }

            @Override
            public void showAd() {
                Activity activity = requireActivity();
                if (activity == null || activity.isFinishing()) {
                    return;
                }
                if (!AdControlManager.INSTANCE.getAdControlBean().getUseInstlFullWhenSwitch()) {
                    InterstitialFullAd.INSTANCE.showAd(activity, new SimpleInterstitialFullListener() {
                        @Override
                        public void onAdError(int code, String errorMsg) {
                            super.onAdError(code, errorMsg);
                            Logger.d("晒单页插屏加载广告错误---- code = $code ,msg =  $errorMsg ");
                        }

                        @Override
                        public void onAdClose() {
                            super.onAdClose();
                            PageMoniterCheck.INSTANCE.showAdSuccess("mine_open_fragment");
                        }
                    });
                } else {
                    InterstitialFullAd.INSTANCE.showAd(activity, new SimpleInterstitialFullListener() {
                        @Override
                        public void onAdError(int errorCode, String errprMsg) {
                            super.onAdError(errorCode, errprMsg);
                            Logger.d("晒单页插全屏加载广告错误---- code = $errorCode ,msg =  $errprMsg ");
                        }

                        @Override
                        public void onAdClose() {
                            super.onAdClose();
                            PageMoniterCheck.INSTANCE.showAdSuccess("mine_open_fragment");
                        }
                    });
                }
            }
        });
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
        loginStatus = AppInfo.checkIsWXLogin();
        if (event.getStatus() == 1 ||
                event.getStatus() == 2) {
            if (mViewModel.detailLivData.getValue() != null) {
                mDataBinding.mainWinCodeRefresh.autoRefresh();
            } else {
                if (fastType == 0 || mViewModel.openWinCountdown.getValue() > 0) {
                    //当前状态为未开奖。那么刷新UI
                    updateUI(fastType);
                    try {
                        mDataBinding.mainWinCodeRefresh.autoRefresh();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            mDataBinding.mainWinCodeRefresh.autoRefresh();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void lotteryStatusEvent(LotteryStatusEvent event) {
        if (event.object == null || event.goodsId == null) {
            return;
        }
        List<String> list = (List<String>) event.object;
        int lotteryStatus = 0;
        if (list.size() <= 0) {
            lotteryStatus = 0;
        } else if (list.size() < 6) {
            lotteryStatus = 1;
        } else {
            lotteryStatus = 2;
        }
        RecommendGoodsResp.ListDTO item = adapter.getItem(event.position);
        if (!event.goodsId.equals(item.goodsId)) {
            return;
        }
        item.lotteryStatus = (lotteryStatus);
        adapter.notifyItemChanged(event.position);
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
        onRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
        }
        mViewModel.isMainLoad = isMainLoad;
        mViewModel.from = from;
        mViewModel.setDataBinDing(mDataBinding, getBaseActivity());
        adapterOpenWinHead = (ViewGroup) View.inflate(getBaseActivity(), headRes, null);
        adapterNotOpenMyAddRecordHead = (ViewGroup) View.inflate(getBaseActivity(), notOpenRecordHeadRes, null);
        adapterNotOpenWinHead = (ViewGroup) View.inflate(getBaseActivity(), headNotOpenWinRes, null);
        adapterOpenWinHead.findViewById(R.id.mine_win_code_sele_rules).setOnClickListener((v) -> {
            Bundle bundle = new Bundle();
            bundle.putString("url",
                    "https://recharge-web.xg.tagtic.cn/jdd/index.html#/rule");
            bundle.putString("title", "中奖规则");
            ARouteHelper.routeSkip(RouterActivityPath.Web.PAGER_WEB_ACTIVITY, bundle);
        });
        adapterOpenWinHead.findViewById(R.id.mine_win_code_scan_all).setOnClickListener((v) -> {
            //去往晒单页
            ARouter.getInstance().build(RouterActivityPath.Main.PAGER_MAIN)
                    .withInt("position", 1)
                    .navigation();
        });
        adapterNotOpenMyAddRecordHead.findViewById(R.id.mine_win_code_scan_all).setOnClickListener((v) -> {
            //去往晒单页
            adapterOpenWinHead.findViewById(R.id.mine_win_code_scan_all).performClick();
        });
        mTaskView = adapterOpenWinHead.findViewById(R.id.mine_win_code_task_layout);
        if (mTaskView != null) {
            mTaskView.refreshYyw(TaskView.Place_Win_Code);
        }
        timeHH = adapterNotOpenWinHead.findViewById(R.id.mine_frm_win_h);
        timeHH1 = adapterNotOpenWinHead.findViewById(R.id.mine_frm_win_h1);
        timeMM = adapterNotOpenWinHead.findViewById(R.id.mine_frm_win_m);
        timeMM1 = adapterNotOpenWinHead.findViewById(R.id.mine_frm_win_m1);
        timeSS = adapterNotOpenWinHead.findViewById(R.id.mine_frm_win_s);
        timeSS1 = adapterNotOpenWinHead.findViewById(R.id.mine_frm_win_s1);
        adapter = new MineWinningCodeAdapter();
        adapter.from = from;
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
                } else { //已开奖
                    //强制调用。否则将会递归调用
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
            if (mViewModel.openWinCountdown.getValue() > 0) {
                updateUI(0); //还在计时阶段。那么直接刷新一次未开奖视图
            } else {
                updateUI(1);
            }
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
            }
        });
        if (mViewModel.isAutoPeriod) {
            //显示顶部距离,达到侵入式状态栏
            mDataBinding.mainWinCodeTitleLayout.setFitsSystemWindows(false);
            mDataBinding.mainWinCodeTitleLayout.setPadding(
                    mDataBinding.mainWinCodeTitleLayout.getPaddingLeft(),
                    mDataBinding.mainWinCodeTitleLayout.getPaddingTop() + BarUtils.getStatusBarHeight(),
                    mDataBinding.mainWinCodeTitleLayout.getPaddingRight(),
                    mDataBinding.mainWinCodeTitleLayout.getPaddingBottom()
            );
        }
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

    //添加列表的未开奖时候的参与记录
    private void addListNotOpenMyAddHead() {
        if (adapterNotOpenMyAddRecordHead.getParent() != null) {
            return; //已经添加了
        }
        ViewGroup.LayoutParams lp = adapterNotOpenMyAddRecordHead.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        adapterNotOpenMyAddRecordHead.setLayoutParams(lp);
        adapter.addHeaderView(adapterNotOpenMyAddRecordHead);
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


    //上一次更新的UI类型
    private int fastType = -1;
    private boolean loginStatus = AppInfo.checkIsWXLogin(); //登录状态

    /**
     * 更新UI
     *
     * @param type -1:为止类型只更新通用部分，0:未开奖 UI，1：已开奖UI
     */
    private void updateUI(int type) {
        fastType = type;
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
            adapterNotOpenMyAddRecordHead.setVisibility(View.GONE);
            adapterNotOpenWinHead.setVisibility(View.VISIBLE);
            addListNotOpenWinHead();

            TextView lgoinOkTv = adapterNotOpenWinHead.findViewById(R.id.mine_tv_djgb);
            TextView lgoinBut = adapterNotOpenWinHead.findViewById(R.id.mine_tv_login);
            LinearLayout myAddll = adapterNotOpenWinHead.findViewById(R.id.mine_win_code_win_connect_layout);
            loginStatus = AppInfo.checkIsWXLogin();
            if (loginStatus) {
                myAddll.setVisibility(View.VISIBLE);
                lgoinOkTv.setVisibility(View.VISIBLE);
                lgoinBut.setVisibility(View.GONE);
                //显示我的参与记录
                if (mViewModel.detailLivData.getValue() != null &&
                        mViewModel.detailLivData.getValue().record != null &&
                        mViewModel.detailLivData.getValue().record.size() > 0) {
                    myAddll.setVisibility(View.VISIBLE); //有参与记录
                    lgoinOkTv.setText("大奖即将公布");
                    if (adapterNotOpenWinHead.getTag() == null ||
                            adapterNotOpenWinHead.getTag() != mViewModel.detailLivData.getValue()) {
                        adapterNotOpenWinHead.setTag(mViewModel.detailLivData.getValue());
                        mViewModel.addAddToGoods( //添加参与商品
                                adapterNotOpenWinHead, true);
                    }
                } else {
                    lgoinOkTv.setText("快去抽奖当锦鲤王");
                    myAddll.setVisibility(View.GONE); //没有参与
                }
            } else {
                myAddll.setVisibility(View.GONE);
                lgoinOkTv.setVisibility(View.GONE);
                lgoinBut.setVisibility(View.VISIBLE);
                lgoinBut.setOnClickListener(v -> {
                    RouterActivityPath.LoginProvider.getLoginProvider()
                            .loginWX(this.toString(), "开奖页>登录按钮");
                });
            }
            mViewModel.updateCountDownUI(timeHH, timeMM, timeSS, timeHH1, timeMM1, timeSS1);
            if (!isInitCommData) {
                isInitCommData = true;
                mViewModel.loadRecommendData(adapter.pageSize); //加载推荐数据
            }
        } else if (type == 1) {
            adapterNotOpenWinHead.setVisibility(View.GONE);
            adapterOpenWinHead.setVisibility(View.GONE);
            //还未开奖。但是不需要显示倒计时而是显示个人参与记录
            if (mViewModel.openWinCountdown.getValue() <= -2) {
                addListNotOpenMyAddHead();
                adapterNotOpenMyAddRecordHead.setVisibility(View.VISIBLE);
                mViewModel.loadAwardData(); //加载滚动通知
                if (mViewModel.detailLivData.getValue() == null) {
                    mDataBinding.mineWinCodeListStatus.showError();
                    mDataBinding.mineWinCodeListStatus.findViewById(R.id.error_view)
                            .setOnClickListener(v -> {
                                mDataBinding.mainWinCodeRefresh.autoRefresh();
                            });
                    return;
                }
                mViewModel.addAddToGoods( //添加参与商品
                        adapterNotOpenMyAddRecordHead, true);
                if (!isInitCommData) {
                    isInitCommData = true;
                    mViewModel.loadRecommendData(adapter.pageSize); //加载推荐数据
                }
                return; //表示未开奖状态下查看参与记录。那么只显示参与记录相关数据
            }
            //正在的已开奖所显示的内容
            adapterNotOpenMyAddRecordHead.setVisibility(View.GONE);
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
                    adapterOpenWinHead, mDataBinding.llLogin);
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

    @Override
    public void onDetach() {
        super.onDetach();
        if (mTaskView != null) {
            mTaskView.release();
        }
    }
}
