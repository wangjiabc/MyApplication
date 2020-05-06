package com.safety.android.qmuidemo.view;

import android.view.View;

import com.qmuiteam.qmui.widget.section.QMUISection;

import java.util.ArrayList;

public class SectionItem extends ArrayList<View> implements QMUISection.Model<SectionItem> {
    private final String text;

    public SectionItem(String text) {
        this.text=text;
    }

    public String getText() {
        return text;
    }


    @Override
    public SectionItem cloneForDiff() {
        return new SectionItem(getText());
       // return  new SectionItem();
    }

    @Override
    public boolean isSameItem(SectionItem other) {
        return text == other.text || (text != null && text.equals(other.text));
    }

    @Override
    public boolean isSameContent(SectionItem other) {
        return true;
    }
}
