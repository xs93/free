package com.donews.home.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.donews.base.widget.CenterImageSpan;
import com.donews.home.R;
import com.donews.home.bean.NorGoodsBean;
import com.donews.home.bean.SearchResultTbBean;
import com.donews.home.listener.GoodsDetailListener;

import java.util.ArrayList;
import java.util.List;

public class SearchSugTbAdapter extends RecyclerView.Adapter<SearchSugTbAdapter.ResultViewHolder> implements View.OnClickListener {
    private final Context mContext;
    private final List<SearchResultTbBean.goodsInfo> mGoodsList = new ArrayList<>();
    private GoodsDetailListener mListener;

    public SearchSugTbAdapter(Context context, GoodsDetailListener listener) {
        mContext = context;
        mListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshData(List<SearchResultTbBean.goodsInfo> list) {
        mGoodsList.clear();
        mGoodsList.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_search_result_tb_item, parent, false);
        final ResultViewHolder holder = new ResultViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {

        SearchResultTbBean.goodsInfo gi = mGoodsList.get(position);
        if (gi == null) {
            return;
        }

        holder.itemView.setTag(gi);
        holder.itemView.setOnClickListener(this::onClick);

        Glide.with(mContext).load(gi.getMainPic()).into(holder.picIv);
        holder.desTv.setText(getTitleString(gi));
        float sales = gi.getMonthSales();
        if (sales >= 10000) {
            sales /= 10000;
            String strSales = String.format("%.1f", sales);
            holder.salesTv.setText("已售" + strSales + "万");
        } else {
            holder.salesTv.setText("已售" + gi.getMonthSales());
        }

        holder.priceTv.setText(gi.getActualPrice() + "");
        holder.shopTv.setText(gi.getShopName());
        holder.giftTv.setText(gi.getCouponPrice() + "元");
    }


    private SpannableString getTitleString(SearchResultTbBean.goodsInfo goodsInfo) {
        String span = "d ";
        int resId = goodsInfo.getShopType() == 1 ? R.drawable.home_logo_tm : R.drawable.home_logo_tb;
        SpannableString spannableString = new SpannableString(span + goodsInfo.getDtitle());

        Drawable drawable = ContextCompat.getDrawable(mContext, resId);
        if (drawable != null) {
            //计算大小，使其和文字高度一般一致
            float height = mContext.getResources().getDimension(R.dimen.home_title) * 0.85f;
            float width = height / drawable.getIntrinsicHeight() * drawable.getIntrinsicWidth();
            drawable.setBounds(0, 0, (int) width, (int) height);
        }
        CenterImageSpan imageSpan = new CenterImageSpan(drawable);
        spannableString.setSpan(imageSpan, 0, 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    @Override
    public int getItemCount() {
        return mGoodsList.size();
    }

    @Override
    public void onClick(View v) {
        SearchResultTbBean.goodsInfo gi = (SearchResultTbBean.goodsInfo) v.getTag();

        mListener.onClick(gi.getId(), gi.getGoodsId());
    }

    public static class ResultViewHolder extends RecyclerView.ViewHolder {

        private final ImageView picIv;
        private final TextView desTv;
        private final TextView priceTv;
        private final TextView giftTv;
        private final TextView salesTv;
        private final TextView shopTv;


        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);

            picIv = itemView.findViewById(R.id.home_search_goods_item_pic_iv);
            desTv = itemView.findViewById(R.id.home_search_goods_item_des);
            priceTv = itemView.findViewById(R.id.home_search_goods_item_price_atv);
            giftTv = itemView.findViewById(R.id.home_search_goods_item_gift_atv);
            salesTv = itemView.findViewById(R.id.home_search_goods_item_sales);
            shopTv = itemView.findViewById(R.id.home_search_goods_item_shop_atv);
        }
    }
}
