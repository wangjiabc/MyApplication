package com.safety.android.ReportDetail;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.RecyclerView;

public class ReportDetailAdapter extends RecyclerView.Adapter {
    private List<Map<Integer,String>> list;
    private Context context;


    public ReportDetailAdapter(Context context, List<Map<Integer,String>> list) {
        this.context = context;
        this.list = list;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new OneViewHolder(View.inflate(context, R.layout.oneviewitem, null));
        } else {
            return new TwoViewHolder(View.inflate(context, R.layout.twoviewitem, null));
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 1) {
            OneViewHolder oneViewHolder = (OneViewHolder) holder;
            oneViewHolder.setData(position);
        } else {
            TwoViewHolder twoViewHolder = (TwoViewHolder) holder;
            twoViewHolder.setData(position);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 1;
        } else {
            return 2;
        }
    }

    class OneViewHolder extends RecyclerView.ViewHolder {
        public OneViewHolder(View itemview) {
            super(itemview);
        }

        public void setData(int position) {
            ImageView imageView = (ImageView) itemView.findViewById(R.id.switch_img);
        }
    }

    private class TwoViewHolder extends RecyclerView.ViewHolder {
        private TextView unit_tv;
        private TextView projectnum_tv;
        private TextView yearplaninvest_tv;
        private TextView nowmonthinvest_tv;
        private TextView onetonowinvest_tv;
        private TextView Investmentcompletion_tv;
        private TextView investmentgrowth_tv;

        public TwoViewHolder(View itemView) {
            super(itemView);
        }

        public void setData(int position) {
            unit_tv = (TextView) itemView.findViewById(R.id.unit_tv);
            projectnum_tv = (TextView) itemView.findViewById(R.id.projectnum_tv);
            yearplaninvest_tv = (TextView) itemView.findViewById(R.id.yearplaninvest_tv);
           // nowmonthinvest_tv = (TextView) itemView.findViewById(R.id.nowmonthinvest_tv);
          //  onetonowinvest_tv = (TextView) itemView.findViewById(R.id.onetonowinvest_tv);
          //  Investmentcompletion_tv = (TextView) itemView.findViewById(R.id.Investmentcompletion_tv);
          //  investmentgrowth_tv = (TextView) itemView.findViewById(R.id.investmentgrowth_tv);
            Map<Integer,String> map=list.get(position);
            unit_tv.setText(map.get(0));
            projectnum_tv.setText(map.get(1));
            yearplaninvest_tv.setText(map.get(2));
        }
    }
}
