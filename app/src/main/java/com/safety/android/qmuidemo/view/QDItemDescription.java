package com.safety.android.qmuidemo.view;

public class QDItemDescription {
    private Class<? extends BaseFragment> mKitDemoClass;
    private String mKitName;
    private int mIconRes;
    private String mDocUrl;

    public QDItemDescription(Class<? extends BaseFragment> kitDemoClass, String kitName){
        this(kitDemoClass, kitName, 0, "");
    }


    public QDItemDescription(Class<? extends BaseFragment> kitDemoClass, String kitName, int iconRes, String docUrl) {
        mKitDemoClass = kitDemoClass;
        mKitName = kitName;
        mIconRes = iconRes;
        mDocUrl = docUrl;
    }

    public Class<? extends BaseFragment> getDemoClass() {
        return mKitDemoClass;
    }

    public String getName() {
        return mKitName;
    }

    public int getIconRes() {
        return mIconRes;
    }

    public String getDocUrl() {
        return mDocUrl;
    }
}

