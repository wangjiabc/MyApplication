package com.safety.android.qmuidemo.view;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.section.QMUIDefaultStickySectionAdapter;
import com.qmuiteam.qmui.widget.section.QMUISection;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;


public class QDGridSectionAdapter extends QMUIDefaultStickySectionAdapter<SectionHeader, SectionItem> {

    protected Map map;

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
    protected void onBindSectionItem(ViewHolder holder, int position, QMUISection<SectionHeader, SectionItem> section, final int itemIndex) {
        String s=section.getItemAt(itemIndex).getText();
       /* System.out.println("itemIndex======"+itemIndex);

        System.out.println("s==========="+s);
        final Spanned sp = Html.fromHtml(String.valueOf(s),Html.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE);

        Drawable defaultDrawable = new getGradientDrawable(Color.YELLOW,100).getGradientDrawable();
        final Html.ImageGetter imgGetter = new HtmlImageGetter((TextView) holder.itemView, MainActivity.dataUrl, defaultDrawable);


        ((TextView) holder.itemView).setText(Html.fromHtml(s,Html.FROM_HTML_MODE_COMPACT, imgGetter,null));*/


        LinearLayout linearLayout=((LinearLayout) holder.itemView);

        CheckBox checkBox=linearLayout.findViewById(R.id.checkBox);



        try {
            final JSONArray jsonArray=new JSONArray(s);

            int id=0;

            if(jsonArray.get(0)!=null){
                id=jsonArray.getInt(0);
                if(map.get(id)!=null)
                    checkBox.setChecked(true);
                else
                    checkBox.setChecked(false);
            }

            final int fId=id;

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("click======"+itemIndex);
                    if(map.get(fId)==null)
                        map.put(fId,jsonArray);
                    else
                        map.remove(fId);
                }
            });

            if(jsonArray.get(1)!=null){
                TextView cId=linearLayout.findViewById(R.id.id);
                cId.setText(String.valueOf(jsonArray.get(1)));
            }

            if(jsonArray.get(1)!=null) {
                TextView tvApplicationName = linearLayout.findViewById(R.id.tvApplicationName);
                tvApplicationName.setText(String.valueOf(jsonArray.get(1)));
            }

            if(jsonArray.get(2)!=null) {
                TextView tvApplicationName = linearLayout.findViewById(R.id.tvApplicationName2);
                tvApplicationName.setText(String.valueOf(jsonArray.get(2)));
            }

            if(jsonArray.get(3)!=null) {
                TextView tvApplicationName = linearLayout.findViewById(R.id.tvApplicationName3);
                tvApplicationName.setText(String.valueOf(jsonArray.get(3)));
            }
            if(jsonArray.get(4)!=null) {
                TextView tvApplicationName = linearLayout.findViewById(R.id.tvApplicationName4);
                tvApplicationName.setText(String.valueOf(jsonArray.get(4)));
            }
            if(jsonArray.get(5)!=null) {
                TextView tvApplicationName = linearLayout.findViewById(R.id.tvApplicationName5);
                tvApplicationName.setText(String.valueOf(jsonArray.get(5)));
            }
            if(jsonArray.get(6)!=null) {
                TextView tvApplicationName = linearLayout.findViewById(R.id.tvApplicationName6);
                tvApplicationName.setText(String.valueOf(jsonArray.get(6)));
            }

        } catch (JSONException e) {

        }

    }



}

