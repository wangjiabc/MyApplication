package com.safety.android.qmuidemo.view;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import static android.graphics.drawable.GradientDrawable.Orientation.TL_BR;

public class getGradientDrawable {

    private int color;
    private int cornerRadius;

    public getGradientDrawable(int color, int cornerRadius){
        this.color=color;
        this.cornerRadius=cornerRadius;
    }

   public Drawable getGradientDrawable(){
        GradientDrawable gradientDrawableN = new GradientDrawable();
        gradientDrawableN.setCornerRadius(cornerRadius);
        gradientDrawableN.setOrientation(TL_BR);//top left to bottom right
        gradientDrawableN.setColor(color);//colors的长度必须大于等于2
        return gradientDrawableN;
    }

}
