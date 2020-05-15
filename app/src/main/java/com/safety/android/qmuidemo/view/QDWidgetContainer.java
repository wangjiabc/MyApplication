package com.safety.android.qmuidemo.view;


import java.lang.Class;
import java.util.HashMap;
import java.util.Map;


class QDWidgetContainer {
    private static QDWidgetContainer sInstance = new QDWidgetContainer();

    private Map<Class<? extends BaseFragment>, QDItemDescription> mWidgets;

    private QDWidgetContainer() {
        mWidgets = new HashMap<>();
        //mWidgets.put(QDBottomSheetFragment.class, new QDItemDescription(QDBottomSheetFragment.class, "QMUIBottomSheet", 2131492875, ""));

    }

    public static QDWidgetContainer getInstance() {
        return sInstance;
    }

    public QDItemDescription get(Class<? extends BaseFragment> fragment) {
        return mWidgets.get(fragment);
    }
}
