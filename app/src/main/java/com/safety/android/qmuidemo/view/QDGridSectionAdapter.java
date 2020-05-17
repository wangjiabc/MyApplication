package com.safety.android.qmuidemo.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.safety.android.MainActivity;
import com.example.myapplication.R;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.section.QMUIDefaultStickySectionAdapter;
import com.qmuiteam.qmui.widget.section.QMUISection;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;


public class QDGridSectionAdapter extends QMUIDefaultStickySectionAdapter<SectionHeader, SectionItem> {

    @NonNull
    @Override
    protected ViewHolder onCreateSectionHeaderViewHolder(@NonNull ViewGroup viewGroup) {
        return new ViewHolder(new QDSectionHeaderView(viewGroup.getContext()));
    }

    @NonNull
    @Override
    protected ViewHolder onCreateSectionItemViewHolder(@NonNull ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        int paddingHor = QMUIDisplayHelper.dp2px(context, 24);
        int paddingVer = QMUIDisplayHelper.dp2px(context, 16);
        TextView tv = new TextView(context);
        tv.setTextSize(14);
        tv.setBackgroundColor(ContextCompat.getColor(context, R.color.qmui_config_color_gray_9));
        tv.setTextColor(Color.DKGRAY);
        tv.setPadding(paddingHor, paddingVer, paddingHor, paddingVer);
        tv.setGravity(Gravity.CENTER);
        return new ViewHolder(tv);
    }

    @NonNull
    @Override
    protected ViewHolder onCreateSectionLoadingViewHolder(@NonNull ViewGroup viewGroup) {
        return new ViewHolder(new QDLoadingItemView(viewGroup.getContext()));
    }

    @Override
    protected void onBindSectionHeader(final ViewHolder holder, final int position, QMUISection<SectionHeader, SectionItem> section) {
        QDSectionHeaderView itemView = (QDSectionHeaderView) holder.itemView;
        itemView.render(section.getHeader(), section.isFold());
        itemView.getArrowView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.isForStickyHeader ? position : holder.getAdapterPosition();
                toggleFold(pos, false);
            }
        });
    }

    @Override
    protected void onBindSectionItem(ViewHolder holder, int position, QMUISection<SectionHeader, SectionItem> section, int itemIndex) {
        String s=section.getItemAt(itemIndex).getText();
        //final Spanned sp = Html.fromHtml(String.valueOf(s),Html.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE);

        Drawable defaultDrawable = new getGradientDrawable(Color.YELLOW,100).getGradientDrawable();
        final Html.ImageGetter imgGetter = new HtmlImageGetter((TextView) holder.itemView, MainActivity.dataUrl, defaultDrawable);


        ((TextView) holder.itemView).setText(Html.fromHtml(s,Html.FROM_HTML_MODE_COMPACT, imgGetter,null));
    }



}

