/**
 * <p> </p>
 * 作者： created by hegai<br>
 * 日期： 2021/10/14 15:43<br>
 * 版本：V1.0<br>
 */


package com.doing.spike.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.doing.spike.R;
import com.doing.spike.bean.SpikeBean;
import com.doing.spike.util.CenterLayoutManager;

public class SpikeTimeAdapter extends RecyclerView.Adapter<SpikeTimeAdapter.SpikeViewHolder> {
    private SpikeBean mSpikeBeans;
    private int mLayoutId;
    private Context mContext;
    private IClickCallbackListener mClickListener;
    private CenterLayoutManager mCenterLayoutManager;
    private RecyclerView mRecyclerView;
    private SpikeViewHolder mLayoutHolder;
    private SpikeViewHolder mPanicBuying;

    public SpikeTimeAdapter(Context context, CenterLayoutManager centerLayoutManager, RecyclerView recyclerView) {
        this.mContext = context.getApplicationContext();
        this.mCenterLayoutManager = centerLayoutManager;
        mRecyclerView = recyclerView;
    }

    @NonNull
    @Override
    public SpikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        SpikeViewHolder holder = new SpikeViewHolder(view);
        return holder;
    }

    public void getLayout(int layoutId) {
        this.mLayoutId = layoutId;
    }

    @SuppressLint("RecyclerView")
    public void onBindViewHolder(@NonNull SpikeViewHolder holder, int position) {
        SpikeBean.RoundsListDTO roundsListDTO = mSpikeBeans.getRoundsList().get(position);
        holder.mtimeShow.setText(roundsListDTO.getRound()+"场");
        holder.mSpikeType.setText(roundsListDTO.getStatus() == 0 ? "已开抢" : roundsListDTO.getStatus() == 1 ? "疯抢中" :
                roundsListDTO.getStatus() == 2 ? "即将开始" : "");

        if (roundsListDTO.getStatus() == 1 && mLayoutHolder == null) {
            setSelectType(holder);
            mCenterLayoutManager.smoothScrollToPosition(mRecyclerView, new RecyclerView.State(), position);
            mPanicBuying = holder;
        } else if (mLayoutHolder != null && mLayoutHolder.mtimeShow.getText().toString().equals(roundsListDTO.getDdqTime())) {
            setSelectType(holder);
            mLayoutHolder = holder;
        } else {
            unSetSelectType(holder);
        }
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //防止重复点击
                if ((mLayoutHolder != null) && (mLayoutHolder == holder)) {
                    return;
                }
                setSelectType(holder);
                if (mLayoutHolder != null) {
                    unSetSelectType(mLayoutHolder);
                }  mLayoutHolder = holder;
                if (mPanicBuying != null) {
                    unSetSelectType(mPanicBuying);
                    mPanicBuying = null;
                }
                mLayoutHolder = holder;
                mCenterLayoutManager.smoothScrollToPosition(mRecyclerView, new RecyclerView.State(), position);
                //点击后的事件回调
                if (mClickListener != null) {
                    mClickListener.onClick(roundsListDTO);
                }
            }
        });
    }


    private void setSelectType(SpikeViewHolder holder) {
        holder.mtimeShow.setTextColor(mContext.getResources().getColor(R.color.selected_color));
        holder.mSpikeType.setTextColor(Color.WHITE);
        holder.mSpikeType.setBackground(mContext.getResources().getDrawable(R.drawable.spike_button_shape_corner));
    }

    private void unSetSelectType(SpikeViewHolder holder) {
        holder.mtimeShow.setTextColor(mContext.getResources().getColor(R.color.un_time_color));
        holder.mSpikeType.setTextColor(mContext.getResources().getColor(R.color.un_time_title_color));
        holder.mSpikeType.setBackgroundColor(mContext.getResources().getColor(R.color.bg_color));
    }

    public SpikeBean getSpikeBeans() {
        return mSpikeBeans;
    }

    public void setSpikeBeans(SpikeBean mSpikeBeans) {
        this.mSpikeBeans = mSpikeBeans;
    }

    public void setClickListener(IClickCallbackListener onClickListener) {
        this.mClickListener = onClickListener;
    }

    @Override
    public int getItemCount() {
        return mSpikeBeans != null && mSpikeBeans.getRoundsList() != null ? mSpikeBeans.getRoundsList().size() : 0;
    }

    class SpikeViewHolder extends RecyclerView.ViewHolder {
        TextView mtimeShow;
        TextView mSpikeType;
        LinearLayout rootView;

        public SpikeViewHolder(@NonNull View itemView) {
            super(itemView);
            mtimeShow = itemView.findViewById(R.id.time_show);
            mSpikeType = itemView.findViewById(R.id.spike_type);
            rootView = itemView.findViewById(R.id.root_view);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }
    }


    public interface IClickCallbackListener {
        void onClick(SpikeBean.RoundsListDTO roundsListDTO);
    }
}

