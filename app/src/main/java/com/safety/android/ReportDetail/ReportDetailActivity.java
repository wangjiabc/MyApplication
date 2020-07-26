package com.safety.android.ReportDetail;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.myapplication.R;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.tools.MyTestUtil;
import com.safety.android.tools.SwipeBackController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ReportDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView report_detail_recyclerview;
    private List<Map<Integer,String>> mList;

    private View view;

    private SwipeBackController swipeBackController;

    private String search="";

    private String startDate;

    private String endDate;

    private Spinner spinner;

    private TextView mDateButton;

    private TextView mDateButton2;

    private AutoCompleteTextView atv_content;

    private JSONArray jsonArray;

    private Integer arg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_report_detail, null);
        setContentView(view);
        swipeBackController = new SwipeBackController(this);

        spinner = view.findViewById(R.id.Spinner01);

        String[] m={"今年","本月","上半年","下半年","一季度","二季度","三季度","四季度"};

        ArrayAdapter<String> adapter2;

        //将可选内容与ArrayAdapter连接起来
        adapter2 = new ArrayAdapter<String>(ReportDetailActivity.this,android.R.layout.simple_spinner_item,m);

        //设置下拉列表的风格
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //将adapter 添加到spinner中
        spinner.setAdapter(adapter2);

        //添加事件Spinner事件监听
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener2());

        //设置默认值
        spinner.setVisibility(View.VISIBLE);

        Calendar calendar= Calendar.getInstance();
        calendar.setTime(new Date());
        final int year=calendar.get(Calendar.YEAR);
        final int month=calendar.get(Calendar.MONTH);
        final int day=calendar.get(Calendar.DAY_OF_MONTH);

        mDateButton= view.findViewById(R.id.time_picker);

        String startText = String.format("%d-%d", year,1);

        mDateButton.setText(startText);

        startDate=startText;

        String endText = String.format("%d-%d", year,12);

        mDateButton2= view.findViewById(R.id.time_picker2);

        mDateButton2.setText(endText);

        endDate=endText;

        final FragmentManager manager=getSupportFragmentManager();

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new com.safety.android.util.DatePickerDialog(ReportDetailActivity.this, 0, new com.safety.android.util.DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker startDatePicker, int startYear, int startMonthOfYear,
                                          int startDayOfMonth) {

                        String textString = String.format("%d-%d", startYear,
                                startMonthOfYear + 1);
                        System.out.println(textString);
                        startDate=textString;
                        mDateButton.setText(textString);
                        new FetchItemsTask().execute();
                    }
                },year,month,day).show();

            }
        });



        final FragmentManager manager2=getSupportFragmentManager();


        mDateButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new com.safety.android.util.DatePickerDialog(ReportDetailActivity.this, 0, new com.safety.android.util.DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker startDatePicker, int startYear, int startMonthOfYear,
                                          int startDayOfMonth) {
                        String textString = String.format("%d-%d", startYear,
                                startMonthOfYear + 1);
                        System.out.println(textString);
                        endDate=textString;
                        mDateButton2.setText(textString);
                        new FetchItemsTask().execute();
                    }
                },year,month,day).show();
            }
        });

        new FetchItemsTask().execute();

        new FetchItemsTaskSupplier().execute();
    }

    class SpinnerSelectedListener2 implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {

            Calendar calendar= Calendar.getInstance();
            calendar.setTime(new Date());
            final int year=calendar.get(Calendar.YEAR);
            final int month=calendar.get(Calendar.MONTH);

            mDateButton= view.findViewById(R.id.time_picker);

            int start=1;
            int end=12;

            if(arg2==0){
                start=1;
                end=12;
            }else if(arg2==1){
                start=month+1;
                end=month+1;
            }else if(arg2==2){
                start=1;
                end=6;
            }else if(arg2==3){
                start=6;
                end=12;
            }else if(arg2==4){
                start=1;
                end=3;
            }else if(arg2==5){
                start=3;
                end=6;
            }else if(arg2==6){
                start=6;
                end=9;
            }else if(arg2==6){
                start=9;
                end=12;
            }

            String startText = String.format("%d-%d", year,start);

            mDateButton.setText(startText);

            startDate=startText;

            String endText = String.format("%d-%d", year,end);

            mDateButton2= view.findViewById(R.id.time_picker2);

            mDateButton2.setText(endText);

            endDate=endText;

            new FetchItemsTask().execute();

        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
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



    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            String cSearch = "";

            if (search != null && !search.equals("")) {
                cSearch += search;
            }

            if (startDate != null && !startDate.equals("")) {
                cSearch += "&startDate=" + startDate + "-01";
            }
            if (endDate != null && !endDate.equals("")) {
                cSearch += "&endDate=" + endDate + "-01";
            }


            System.out.println("cSearch====" + cSearch);

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/accounthead/accounthead/getReportDetail?" + cSearch);

        }


        @Override
        protected void onPostExecute(String res) {

            Double allAccount = 0.0;

            mList = new ArrayList<>();
            Map<Integer, String> map = new HashMap<>();
            mList.add(map);

            System.out.println("res="+res);

            JSONObject json = null;
            try {
                json = new JSONObject(res);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            MyTestUtil.print(json);

            JSONObject jsonObject = null;
            try {
                jsonObject = json.getJSONObject("result");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject jsonObject1 = null;
            try {
                jsonObject1 = jsonObject.getJSONObject("0");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            map = new HashMap<>();
            map.put(0, "销售总额");
            try {
                map.put(1, jsonObject1.getString("c"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            double all = 0;
            try {
                all = jsonObject1.getDouble("ALLACCOUNT");
            } catch (JSONException e) {
                    e.printStackTrace();
            }
            map.put(2, String.valueOf(all));
            mList.add(map);

            JSONObject jsonObject2 = null;
            try {
                jsonObject2 = jsonObject.getJSONObject("1");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            map = new HashMap<>();
            map.put(0, "已收帐款");
            try {
                map.put(1, jsonObject2.getString("c"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            double already = 0;
            try {
                already=jsonObject2.getDouble("ALLACCOUNT");
            } catch (JSONException e) {

            }
            map.put(2, String.valueOf(already));
            mList.add(map);
            JSONObject jsonObject3 = null;
            try {
                jsonObject3 = jsonObject.getJSONObject("2");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            map = new HashMap<>();
            map.put(0, "应收帐款");
            try {
                map.put(1, jsonObject3.getString("c"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            double not = 0;
            try {
                not=jsonObject3.getDouble("ALLACCOUNT");
            } catch (JSONException e) {

            }
            map.put(2, String.valueOf(not));
            mList.add(map);
            JSONObject jsonObject4 = null;
            try {
                jsonObject4 = jsonObject.getJSONObject("3");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            map = new HashMap<>();
            map.put(0, "商品成本");
            try {
                map.put(1, jsonObject4.getString("c"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            double allCost = 0;
            try {
                allCost=jsonObject4.getDouble("allCost");
            } catch (JSONException e) {
                e.printStackTrace();
            }
                BigDecimal bigDecimal = new BigDecimal(allCost);
                allCost = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                map.put(2, String.valueOf(allCost));
                mList.add(map);
                JSONObject jsonObject5 = null;
                try {
                    jsonObject5 = jsonObject.getJSONObject("4");
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                map = new HashMap<>();
                map.put(0, "开支成本");
                try {
                    map.put(1, jsonObject5.getString("c"));
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                double inout = 0;
                try {
                    inout = jsonObject5.getDouble("inoutitem_detail");
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                bigDecimal = new BigDecimal(inout);
                inout = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                map.put(2, String.valueOf(inout));
                mList.add(map);

                map = new HashMap<>();
                map.put(0, "利润");
                double profit = already-allCost-inout;
                map.put(2, String.valueOf(profit));
                mList.add(map);

                System.out.println("allAccount============" + allAccount);


                report_detail_recyclerview = (RecyclerView) findViewById(R.id.fragment_safe_box_recycler_view);
                report_detail_recyclerview.setLayoutManager(new LinearLayoutManager(ReportDetailActivity.this));
                report_detail_recyclerview.setAdapter(new ReportDetailAdapter(ReportDetailActivity.this, mList));

        }

    }

    private class FetchItemsTaskSupplier extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... params) {

                return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/supplier/supplier/selectGroupUser");

            }


            @Override
            protected void onPostExecute(String json) {

                try {

                    JSONObject jsonObject = new JSONObject(json);
                    String success = jsonObject.optString("success", null);

                    if (success.equals("true")) {

                        jsonArray = jsonObject.getJSONArray("result");

                        final String[] data = new String[jsonArray.length()];

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);

                            String name = jsonObject1.getString("SUPPLIER");

                            data[i] = name;

                        }

                        atv_content = (AutoCompleteTextView) view.findViewById(R.id.atv_content);

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ReportDetailActivity.
                                this, android.R.layout.simple_dropdown_item_1line, data);
                        atv_content.setAdapter(adapter);

                        System.out.println(data);

                        atv_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                Object obj = parent.getItemAtPosition(position);

                                String username = (String) obj;

                                for (int i = 0; i < data.length; i++) {

                                    if (username.equals(data[i])) {
                                        arg = i;

                                        try {

                                            JSONObject jsonObject = (JSONObject) jsonArray.get(arg);

                                            int supplierId = jsonObject.getInt("ID");
                                            String supplier = jsonObject.getString("SUPPLIER");

                                            search = "&supplierId=" + supplierId;

                                            new FetchItemsTask().execute();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        continue;
                                    }

                                }
                            }
                        });
                    /*
                    //将可选内容与ArrayAdapter连接起来
                    adapter = new ArrayAdapter<String>(SaleActivity.this,android.R.layout.simple_spinner_item,m);

                    //设置下拉列表的风格
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    //将adapter 添加到spinner中
                    spinner.setAdapter(adapter);

                    //添加事件Spinner事件监听
                    spinner.setOnItemSelectedListener(new SpinnerSelectedListener());

                    //设置默认值
                    spinner.setVisibility(View.VISIBLE);
                    */

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }


    @Override
    public void onClick(View v) {

    }
}


