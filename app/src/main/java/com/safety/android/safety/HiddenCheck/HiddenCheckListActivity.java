package com.safety.android.safety.HiddenCheck;

import com.safety.android.safety.SingleFragmentActivity;

import androidx.fragment.app.Fragment;

/**
 * Created by WangJing on 2018/5/15.
 */

public class HiddenCheckListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new HiddenCheckListFragment();
    }
}
