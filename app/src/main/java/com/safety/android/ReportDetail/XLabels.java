package com.safety.android.ReportDetail;

import com.github.mikephil.charting.utils.LabelBase;

public class XLabels extends LabelBase {
    public int mLabelWidth = 1;
    public int mLabelHeight = 1;
    private int mSpaceBetweenLabels = 4;
    public int mXAxisLabelModulus = 1;
    private boolean mCenterXAxisLabels = false;
    private boolean mAvoidFirstLastClipping = false;
    protected boolean mAdjustXAxisLabels = true;
    private com.github.mikephil.charting.utils.XLabels.XLabelPosition mPosition;

    public XLabels() {
        this.mPosition = com.github.mikephil.charting.utils.XLabels.XLabelPosition.TOP;
    }

    public boolean isCenterXLabelsEnabled() {
        return this.mCenterXAxisLabels;
    }

    public void setCenterXLabelText(boolean enabled) {
        this.mCenterXAxisLabels = enabled;
    }

    public void setAdjustXLabels(boolean enabled) {
        this.mAdjustXAxisLabels = enabled;
    }

    public boolean isAdjustXLabelsEnabled() {
        return this.mAdjustXAxisLabels;
    }

    public com.github.mikephil.charting.utils.XLabels.XLabelPosition getPosition() {
        return this.mPosition;
    }

    public void setPosition(com.github.mikephil.charting.utils.XLabels.XLabelPosition pos) {
        this.mPosition = pos;
    }

    public void setSpaceBetweenLabels(int space) {
        this.mSpaceBetweenLabels = space;
    }

    public int getSpaceBetweenLabels() {
        return this.mSpaceBetweenLabels;
    }

    public void setAvoidFirstLastClipping(boolean enabled) {
        this.mAvoidFirstLastClipping = enabled;
    }

    public boolean isAvoidFirstLastClippingEnabled() {
        return this.mAvoidFirstLastClipping;
    }

    public static enum XLabelPosition {
        TOP,
        BOTTOM,
        BOTH_SIDED,
        TOP_INSIDE,
        BOTTOM_INSIDE;

        private XLabelPosition() {
        }
    }
}
