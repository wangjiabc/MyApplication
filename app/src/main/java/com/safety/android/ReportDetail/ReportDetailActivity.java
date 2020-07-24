package com.safety.android.ReportDetail;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView report_detail_recyclerview;
    private List<Map<Integer,String>> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);
        initView();
        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initView2();
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


