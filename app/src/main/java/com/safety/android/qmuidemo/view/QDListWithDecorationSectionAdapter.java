package com.safety.android.qmuidemo.view;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.section.QMUISection;
import com.qmuiteam.qmui.widget.section.QMUISectionDiffCallback;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;


import java.util.List;

public class QDListWithDecorationSectionAdapter extends QDListSectionAdapter {

    public static final int ITEM_INDEX_LIST_HEADER = -1;
    public static final int ITEM_INDEX_LIST_FOOTER = -2;
    public static final int ITEM_INDEX_SECTION_TIP_START = -3;
    public static final int ITEM_INDEX_SECTION_TIP_END = -4;

    public static final int ITEM_TYPE_LIST_HEADER = 1;
    public static final int ITEM_TYPE_LIST_FOOTER = 2;
    public static final int ITEM_TYPE_SECTION_TIP_START = 3;
    public static final int ITEM_TYPE_SECTION_TIP_END = 4;


    @NonNull
    @Override
    protected QMUIStickySectionAdapter.ViewHolder onCreateCustomItemViewHolder(@NonNull ViewGroup viewGroup, int type) {
        View view;
        Context context = viewGroup.getContext();
        if (type == ITEM_TYPE_LIST_HEADER) {
            ImageView iv = new ImageView(context);
            iv.setImageResource(R.mipmap.example_image2);
            view = iv;
        } else if (type == ITEM_TYPE_LIST_FOOTER) {
            TextView tv = new TextView(context);
            tv.setTextSize(12);
            tv.setBackgroundColor(ContextCompat.getColor(context, R.color.qmui_config_color_gray_9));
            tv.setTextColor(Color.DKGRAY);
            tv.setText(R.string.sticky_section_decoration_list_footer);
            tv.setGravity(Gravity.CENTER);
            int paddingVer = QMUIDisplayHelper.dp2px(context, 16);
            tv.setPadding(0, paddingVer, 0, paddingVer);
            view = tv;
        } else if (type == ITEM_TYPE_SECTION_TIP_START) {
            TextView tv = new TextView(context);
            tv.setTextSize(12);
            tv.setBackgroundColor(ContextCompat.getColor(context, R.color.qmui_config_color_gray_9));
            tv.setTextColor(Color.DKGRAY);
            tv.setText(R.string.sticky_section_decoration_section_top_tip);
            tv.setGravity(Gravity.CENTER);
            int paddingVer = QMUIDisplayHelper.dp2px(context, 16);
            tv.setPadding(0, paddingVer, 0, paddingVer);
            view = tv;
        } else if (type == ITEM_TYPE_SECTION_TIP_END) {
            TextView tv = new TextView(context);
            tv.setTextSize(12);
            tv.setBackgroundColor(ContextCompat.getColor(context, R.color.qmui_config_color_gray_9));
            tv.setTextColor(Color.DKGRAY);
            tv.setText(R.string.sticky_section_decoration_section_bottom_tip);
            tv.setGravity(Gravity.CENTER);
            int paddingVer = QMUIDisplayHelper.dp2px(context, 16);
            tv.setPadding(0, paddingVer, 0, paddingVer);
            view = tv;
        } else {
            view = new View(viewGroup.getContext());
        }
        return new QMUIStickySectionAdapter.ViewHolder(view);
    }

    @Override
    protected int getCustomItemViewType(int itemIndex, int position) {
        if (itemIndex == ITEM_INDEX_LIST_HEADER) {
            return ITEM_TYPE_LIST_HEADER;
        } else if (itemIndex == ITEM_INDEX_LIST_FOOTER) {
            return ITEM_TYPE_LIST_FOOTER;
        } else if (itemIndex == ITEM_INDEX_SECTION_TIP_START) {
            return ITEM_TYPE_SECTION_TIP_START;
        } else if (itemIndex == ITEM_INDEX_SECTION_TIP_END) {
            return ITEM_TYPE_SECTION_TIP_END;
        }
        return super.getCustomItemViewType(itemIndex, position);
    }

    @Override
    protected QMUISectionDiffCallback<SectionHeader, SectionItem> createDiffCallback(
            List<QMUISection<SectionHeader, SectionItem>> lastData,
            List<QMUISection<SectionHeader, SectionItem>> currentData) {
        return new QMUISectionDiffCallback<SectionHeader, SectionItem>(lastData, currentData) {

            @Override
            protected void onGenerateCustomIndexBeforeSectionList(IndexGenerationInfo generationInfo, List<QMUISection<SectionHeader, SectionItem>> list) {
                generationInfo.appendWholeListCustomIndex(ITEM_INDEX_LIST_HEADER);
            }

            @Override
            protected void onGenerateCustomIndexAfterSectionList(IndexGenerationInfo generationInfo, List<QMUISection<SectionHeader, SectionItem>> list) {
                generationInfo.appendWholeListCustomIndex(ITEM_INDEX_LIST_FOOTER);
            }

            @Override
            protected void onGenerateCustomIndexBeforeItemList(IndexGenerationInfo generationInfo,
                                                               QMUISection<SectionHeader, SectionItem> section,
                                                               int sectionIndex) {
                if (!section.isExistBeforeDataToLoad()) {
                    generationInfo.appendCustomIndex(sectionIndex, ITEM_INDEX_SECTION_TIP_START);
                }
            }

            @Override
            protected void onGenerateCustomIndexAfterItemList(IndexGenerationInfo generationInfo,
                                                              QMUISection<SectionHeader, SectionItem> section,
                                                              int sectionIndex) {
                if (!section.isExistAfterDataToLoad()) {
                    generationInfo.appendCustomIndex(sectionIndex, ITEM_INDEX_SECTION_TIP_END);
                }
            }

            @Override
            protected boolean areCustomContentsTheSame(@Nullable QMUISection<SectionHeader, SectionItem> oldSection, int oldItemIndex, @Nullable QMUISection<SectionHeader, SectionItem> newSection, int newItemIndex) {
                return true;
            }
        };
    }
}