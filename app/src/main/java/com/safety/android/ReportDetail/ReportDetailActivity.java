package com.safety.android.ReportDetail;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class ReportDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView report_detail_recyclerview;
    private List<String> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);
        initView();
    }

    private void initView() {
        mList = new ArrayList<>();
        mList.add("省亿元以上项目");
        mList.add("合肥市“大新专”项目");
        mList.add("市产业类投资项目");
        mList.add("市大建设项目");
        report_detail_recyclerview = (RecyclerView) findViewById(R.id.fragment_safe_box_recycler_view);
        report_detail_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        report_detail_recyclerview.setAdapter(new ReportDetailAdapter(this,mList));

    }

    @Override
    public void onClick(View v) {

    }
}


