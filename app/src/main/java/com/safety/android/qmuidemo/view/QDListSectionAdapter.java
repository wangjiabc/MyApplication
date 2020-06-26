package com.safety.android.qmuidemo.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;

import java.util.HashMap;

import androidx.annotation.NonNull;

public class QDListSectionAdapter extends QDGridSectionAdapter {

    public QDListSectionAdapter(){
        super();
        map=new HashMap();
    }

    @NonNull
    @Override
    protected QMUIStickySectionAdapter.ViewHolder onCreateSectionItemViewHolder(@NonNull ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        View view= LayoutInflater.from(context).inflate(R.layout.qmui_grouplist_view, null);
        return new QMUIStickySectionAdapter.ViewHolder(view);
    }
}