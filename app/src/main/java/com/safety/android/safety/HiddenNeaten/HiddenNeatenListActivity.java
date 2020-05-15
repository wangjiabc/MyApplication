package com.safety.android.safety.HiddenNeaten;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.safety.android.qmuidemo.view.QMUIFragment;
import com.safety.android.qmuidemo.view.QMUIFragmentActivity;
import com.safety.android.safety.SafeBoxFragment;


public class HiddenNeatenListActivity extends QMUIFragmentActivity {


    @Override
    protected int getContextViewId() {
        return new HiddenNeatenListFragment().getTargetRequestCode();
    }
}
