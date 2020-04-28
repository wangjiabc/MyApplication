package com.safety.android.mqtt.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.myapplication.R;
import com.safety.android.mqtt.bean.Message;

import java.util.List;

import butterknife.BindFont;
import butterknife.ButterKnife;

/**
 * Description :
 * Author : liujun
 * Email  : liujin2son@163.com
 * Date   : 2016/10/26 0026
 */

public class SubcriberAdapter extends BaseAdapter {

    private List<Message> listDate = null;

    private Context context = null;

    public SubcriberAdapter(List<Message> listDate, Context context) {
        this.listDate = listDate;
        this.context = context;
    }

    public List<Message> getListDate() {
        return listDate;
    }

    public void setListDate(List<Message> listDate) {
        this.listDate = listDate;
        notifyDataSetChanged();
    }

    public void addListDate(Message message) {
        this.listDate.add(message);
        notifyDataSetChanged();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return listDate.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            view = View.inflate(context, R.layout.item_subscriber, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        /**
         * 操作数据
         */
        Message message = listDate.get(i);
        if(message!=null){

                viewHolder.rlLeft.setVisibility(View.VISIBLE);
                viewHolder.tvLeft.setText(message.string);
        }
        return view;
    }

    static class ViewHolder {
        @BindFont(R.id.tv_left)
        TextView tvLeft;
        @BindFont(R.id.rl_left)
        RelativeLayout rlLeft;

        ViewHolder(View view) {
            tvLeft= (TextView) view.findViewById(R.id.tv_left);
            rlLeft= (RelativeLayout) view.findViewById(R.id.rl_left);
            ButterKnife.bind(this, view);
        }
    }
}
