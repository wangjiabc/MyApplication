package com.safety.android.qmuidemo.view;


public interface FirstFragmentFinder {
    int NO_ID = -1;
    Class<? extends QMUIFragment> getFragmentClassById(int id);
    int getIdByFragmentClass(Class<? extends QMUIFragment> clazz);
}