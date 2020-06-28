package com.safety.android.qmuidemo.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public class QDListSectionAdapter extends QDGridSectionAdapter {

    int type=0;

    public QDListSectionAdapter(int type){
        super();
        this.type=type;
        map=new HashMap();
    }

    public Map getSelectMap(){
        return map;
    }

    @NonNull
    @Override
    protected QMUIStickySectionAdapter.ViewHolder onCreateSectionItemViewHolder(@NonNull ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        View view;
        if(type==0){
            view= LayoutInflater.from(context).inflate(R.layout.item_grouplist_view, null);
        }else {
             view= LayoutInflater.from(context).inflate(R.layout.item_img_grouplist_view, null);
        }
        return new QMUIStickySectionAdapter.ViewHolder(view);
    }
}