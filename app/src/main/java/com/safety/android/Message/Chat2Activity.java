package com.safety.android.Message;

import androidx.fragment.app.Fragment;

import com.safety.android.SingleFragmentActivity;

/**
 * Created by WangJing on 2017/10/11.
 */

public class Chat2Activity extends SingleFragmentActivity{
    @Override
    protected Fragment createFragment() {
        return new ChatFragment();
    }
}
