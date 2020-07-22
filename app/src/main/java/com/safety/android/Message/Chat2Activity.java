package com.safety.android.Message;

import android.os.Bundle;
import android.view.MotionEvent;

import com.safety.android.SingleFragmentActivity;
import com.safety.android.tools.SwipeBackController;

import androidx.fragment.app.Fragment;

/**
 * Created by WangJing on 2017/10/11.
 */

public class Chat2Activity extends SingleFragmentActivity{

    private SwipeBackController swipeBackController;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        swipeBackController = new SwipeBackController(this);
    }

    @Override
    protected Fragment createFragment() {

        return new ChatFragment();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (swipeBackController.processEvent(ev)) {
            return true;
        } else {
            return super.onTouchEvent(ev);
        }
    }

}
