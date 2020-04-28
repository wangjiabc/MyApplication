package com.safety.android.safety.SQLite3.com.safety.android.safety.HiddenNeaten;

import androidx.fragment.app.Fragment;

import com.safety.android.safety.SQLite3.com.safety.android.safety.SingleFragmentActivity;

public class HiddenNeatenListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new HiddenNeatenListFragment();
    }
}
