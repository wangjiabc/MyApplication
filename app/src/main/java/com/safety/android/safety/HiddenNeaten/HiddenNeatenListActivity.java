package com.safety.android.safety.HiddenNeaten;

import com.safety.android.safety.SingleFragmentActivity;

import androidx.fragment.app.Fragment;

public class HiddenNeatenListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new HiddenNeatenListFragment();
    }
}
