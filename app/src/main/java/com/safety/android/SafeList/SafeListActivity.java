package com.safety.android.SafeList;

import android.content.Intent;

import com.safety.android.SQLite3.SafeInfo;
import com.safety.android.SingleFragmentActivity;

import androidx.fragment.app.Fragment;

public class SafeListActivity extends SingleFragmentActivity
        implements SafeListFragment.Callbacks{

    @Override
    protected Fragment createFragment() {
        return new SafeListFragment();
    }


    @Override
    public void onSafeSelected(SafeInfo safeInfo) {
            Intent intent=SafePagerActivity.newIntent(this,safeInfo.getmId());
            startActivity(intent);
    }
}
