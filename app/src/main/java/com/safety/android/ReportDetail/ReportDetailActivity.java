package com.safety.android.ReportDetail;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.example.myapplication.R;
import com.safety.android.tools.SwipeBackController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ReportDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView report_detail_recyclerview;
    private List<Map<Integer,String>> mList;

    private SwipeBackController swipeBackController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);
        swipeBackController = new SwipeBackController(this);
        initView();
        initView2();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        System.out.println("ev====="+ev);
        if (swipeBackController.processEvent(ev)) {
            return true;
        } else {
            return super.onTouchEvent(ev);
        }
    }


    private void initView() {
        mList = new ArrayList<>();
        Map<Integer,String> map=new HashMap<>();
        map.put(1,"aaaaaaaa");
        map.put(2,"bbbbbbbb");
        mList.add(map);
        mList.add(map);

        report_detail_recyclerview = (RecyclerView) findViewById(R.id.fragment_safe_box_recycler_view);
        report_detail_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        report_detail_recyclerview.setAdapter(new ReportDetailAdapter(this,mList));

    }

    private void initView2() {
        mList = new ArrayList<>();
        Map<Integer,String> map=new HashMap<>();
        map.put(1,"cccccc");
        map.put(2,"ddddddd");
        mList.add(map);
        mList.add(map);

        report_detail_recyclerview = (RecyclerView) findViewById(R.id.fragment_safe_box_recycler_view);
        report_detail_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        report_detail_recyclerview.setAdapter(new ReportDetailAdapter(this,mList));

    }

    @Override
    public void onClick(View v) {

    }
}


