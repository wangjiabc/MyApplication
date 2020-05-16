package com.safety.android.qmuidemo.view;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.R;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class QDListSectionAdapter extends QDGridSectionAdapter {

    @NonNull
    @Override
    protected QMUIStickySectionAdapter.ViewHolder onCreateSectionItemViewHolder(@NonNull ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        int paddingHor = QMUIDisplayHelper.dp2px(context, 24);
        int paddingVer = QMUIDisplayHelper.dp2px(context, 16);
        TextView tv = new TextView(context);
        tv.setTextSize(20);
        tv.setBackgroundColor(ContextCompat.getColor(context, R.color.qmui_config_color_gray_9));
        tv.setTextColor(Color.DKGRAY);
        tv.setPadding(paddingHor, paddingVer, paddingHor, paddingVer);
        tv.setGravity(Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK);
        tv.setTextAppearance(0);
        return new QMUIStickySectionAdapter.ViewHolder(tv);
    }
}