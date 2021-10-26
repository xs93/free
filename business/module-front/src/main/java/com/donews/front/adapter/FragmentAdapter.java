package com.donews.front.adapter;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.donews.front.NorFragment;
import com.donews.front.bean.LotteryCategoryBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentAdapter extends FragmentStateAdapter {

    private List<LotteryCategoryBean.categoryBean> list = new ArrayList<>();
    private Map<String, Fragment> mFragmentMap = new HashMap<>();

    @SuppressLint("NotifyDataSetChanged")
    public void refreshData(List<LotteryCategoryBean.categoryBean> list) {
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    public FragmentAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    private Fragment mkFragment(LotteryCategoryBean.categoryBean categoryItem) {
        if (mFragmentMap.get(categoryItem.getCategoryId()) == null) {
            mFragmentMap.put(categoryItem.getCategoryId(), new NorFragment(categoryItem));
        }
        return mFragmentMap.get(categoryItem.getCategoryId());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mkFragment(this.list.get(position));
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }

    public void clear() {
        if (this.list != null) {
            this.list.clear();
            this.list = null;
        }
        if (mFragmentMap != null) {
            mFragmentMap.clear();
            mFragmentMap = null;
        }
    }
}
