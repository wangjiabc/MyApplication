package com.safety.android.safety.SafeList;

import android.content.Intent;
import androidx.fragment.app.Fragment;

import com.safety.android.safety.SQLite3.SafeInfo;
import com.safety.android.safety.SingleFragmentActivity;

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
