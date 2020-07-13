package com.safety.android.AccountheadList;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;

import com.example.myapplication.R;
import com.safety.android.tools.SwipeBackController;

public class SecondActivity extends Activity {

	private SwipeBackController swipeBackController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_second);

        swipeBackController = new SwipeBackController(this);
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