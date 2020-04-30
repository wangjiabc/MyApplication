package com.safety.android.qmuidemo.view;

import android.content.Context;

import com.qmuiteam.qmui.layout.QMUIPriorityLinearLayout;
import com.qmuiteam.qmui.widget.section.QMUISection;

public class SectionItem implements QMUISection.Model<SectionItem> {
    private final String text;

    private QMUIPriorityLinearLayout qmuiPriorityLinearLayout;

    public SectionItem(String text, Context context){
        this.text = text;
        qmuiPriorityLinearLayout=new QMUIPriorityLinearLayout(context);

    }

    public SectionItem(String text) {
        this.text=text;
    }

    public String getText() {
        return text;
    }


    @Override
    public SectionItem cloneForDiff() {
        return new SectionItem(getText());
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
