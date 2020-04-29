package com.safety.android.qmuidemo.view;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUILoadingView;

public class QDLoadingItemView extends FrameLayout {

    private QMUILoadingView mLoadingView;

    public QDLoadingItemView(@NonNull Context context) {
        this(context, null);
    }

    public QDLoadingItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mLoadingView = new QMUILoadingView(context,
                QMUIDisplayHelper.dp2px(context, 24), Color.LTGRAY);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        addView(mLoadingView, lp);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                QMUIDisplayHelper.dp2px(getContext(), 48), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mLoadingView.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mLoadingView.stop();
    }
}
