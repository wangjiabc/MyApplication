package com.safety.android.Sale;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.myapplication.R;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.tools.MyTestUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

public class SaleActivity extends AppCompatActivity {

    private Map<Integer,JSONObject> itemMap=new HashMap<>();

    private Spinner spinner;
    private ArrayAdapter<String> adapter;

    private TextView sale_title;
    private TextView saleall2;
    private TextView saleall3;
    private TextView saleall4;

    private JSONArray jsonArray;

    private TextView phoneNum;
    private TextView orderNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sale_activity, null);

        sale_title=view.findViewById(R.id.sale_title);
        saleall2=view.findViewById(R.id.saleall2);
        saleall3=view.findViewById(R.id.saleall3);
        saleall4=view.findViewById(R.id.saleall4);

        phoneNum=view.findViewById(R.id.phoneNum);
        orderNumber=view.findViewById(R.id.orderNumber);

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
        String newsNo = df.format(new Date())+String.valueOf((int)(Math.random()*9+1)*1000);

        orderNumber.setText(newsNo);

        Intent intent=getIntent();

        String jsonString=intent.getStringExtra("jsonString");

        if(jsonString!=null&&!jsonString.equals("")){

            final List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();

            try {

                JSONObject jsonObject = new JSONObject(jsonString);

                JSONArray jsonArray=jsonObject.getJSONArray("ids");

                System.out.println("jsonArray==============");
                MyTestUtil.print(jsonArray);

                LayoutInflater inflater = getLayoutInflater();
                LinearLayout layout_validate = (LinearLayout) view.findViewById(R.id.layout_sale);
                layout_validate.removeAllViews();

                for (int i = 0; i < jsonArray.length(); i++) {

                    int order=i+1;

                    JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                    JSONObject jsonObject2=new JSONObject();
                    jsonObject2.put("order",order);
                    jsonObject2.put("NAME",jsonObject1.get("name"));
                    jsonObject2.put("id",jsonObject1.getInt("id"));
                    jsonObject2.put("AMOUNT","1");
                    itemMap.put(order,jsonObject2);

                    View validateItem = inflater.inflate(R.layout.sale_item, null);
                    validateItem.setTag(i);
                    layout_validate.addView(validateItem);
                    TextView tv_validateName = (TextView) validateItem.findViewById(R.id.sale1);
                    EditText et_validatePrice = (EditText) validateItem.findViewById(R.id.sale2);
                    EditText et_validateCount = (EditText) validateItem.findViewById(R.id.sale3);
                    TextView tv_valiateAll=validateItem.findViewById(R.id.sale4);

                    try {

                        tv_validateName.setText(jsonObject1.getString("name"));
                        tv_validateName.setGravity(Gravity.CENTER);
                        et_validatePrice.setText(jsonObject1.getString("retailprice"));
                        et_validatePrice.setGravity(Gravity.CENTER);
                        et_validateCount.setText("1");
                        et_validateCount.setGravity(Gravity.CENTER);
                        tv_valiateAll.setText(jsonObject1.getString("retailprice"));
                        tv_valiateAll.setGravity(Gravity.CENTER);
                        Map<String,Object> map=new HashMap();
                        map.put("retailprice",et_validatePrice);
                        map.put("count",et_validateCount);
                        map.put("allPrice",tv_valiateAll);
                        list.add(map);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Iterator<Map<String,Object>> iterator=list.iterator();

            while (iterator.hasNext()){

                Map<String,Object> map=iterator.next();
                final EditText et_validatePrice = (EditText) map.get("retailprice");
                final EditText et_validateCount = (EditText) map.get("count");
                final TextView tv_valiateAll= (TextView) map.get("allPrice");

                et_validatePrice.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        float price=0;

                        if(s.toString()!=null&&!s.toString().equals(""))
                            price=Float.parseFloat(s.toString());

                        int amount=0;

                        if(et_validateCount.getText().toString()!=null&&!et_validateCount.getText().toString().equals(""))
                            amount=Integer.parseInt(et_validateCount.getText().toString());

                        float all=price*amount;

                        System.out.println("all="+all);

                        DecimalFormat fnum = new DecimalFormat("##0.00");
                        String a = fnum.format(all);

                        tv_valiateAll.setText(String.valueOf(a));

                        calculatePrice(list);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                et_validateCount.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        float price=0;

                        if(et_validatePrice.getText().toString()!=null&&!et_validatePrice.getText().toString().equals(""))
                            price=Float.parseFloat(et_validatePrice.getText().toString());

                        int amount=0;

                        if(s.toString()!=null&&!s.toString().equals(""))
                            amount=Integer.parseInt(s.toString());

                        float all=price*amount;

                        System.out.println("all="+all);

                        DecimalFormat fnum = new DecimalFormat("##0.00");
                        String a = fnum.format(all);

                        tv_valiateAll.setText(String.valueOf(a));

                        calculatePrice(list);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                float price=0;

                if(et_validatePrice.getText().toString()!=null&&!et_validatePrice.getText().toString().equals(""))
                    price=Float.parseFloat(et_validatePrice.getText().toString());

                int amount=0;

                if(et_validateCount.getText().toString()!=null&&!et_validateCount.getText().toString().equals(""))
                    amount=Integer.parseInt(et_validateCount.getText().toString());

                float all=price*amount;

                System.out.println("all="+all);

                DecimalFormat fnum = new DecimalFormat("##0.00");
                String a = fnum.format(all);

                tv_valiateAll.setText(a);
            }

            calculatePrice(list);

        }

        spinner = (Spinner) view.findViewById(R.id.Spinner01);

        new FetchItemsTask().execute();

        setContentView(view);

    }

    void calculatePrice(List<Map<String,Object>> list){

        Iterator<Map<String,Object>> iterator=list.iterator();

        float allPrice=0;

        int allCount=0;

        float totalAllPrice=0;

        while (iterator.hasNext()) {

            Map<String, Object> map = iterator.next();
            EditText et_validatePrice = (EditText) map.get("retailprice");
            EditText et_validateCount = (EditText) map.get("count");

            float price=0;

            if(et_validatePrice.getText().toString()!=null&&!et_validatePrice.getText().toString().equals(""))
                price=Float.parseFloat(et_validatePrice.getText().toString());

            int amount=0;

            if(et_validateCount.getText().toString()!=null&&!et_validateCount.getText().toString().equals(""))
                amount=Integer.parseInt(et_validateCount.getText().toString());

            allPrice+=price;

            allCount+=amount;

            totalAllPrice+=price*amount;

        }

        DecimalFormat fnum = new DecimalFormat("##0.00");
        String price = fnum.format(allPrice);
        String totalPrice=fnum.format(totalAllPrice);

        System.out.println("price="+price);
        System.out.println("allcouont="+allCount);
        System.out.println("totalprice="+totalPrice);

        saleall2.setText(price);
        saleall3.setText(String.valueOf(allCount));
        saleall4.setText(totalPrice);

    }


    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/supplier/supplier/selectGroupUser");

        }


        @Override
        protected void onPostExecute(String json) {

            try {

                JSONObject jsonObject = new JSONObject(json);
                String success = jsonObject.optString("success", null);

                if(success.equals("true")){

                    jsonArray=jsonObject.getJSONArray("result");

                    String[] m=new String[jsonArray.length()];

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);

                        String name=jsonObject1.getString("SUPPLIER");

                        if(jsonObject1.getString("EMAIL")!=null&&!jsonObject1.getString("EMAIL").equals("null")) {
                            System.out.println("email======="+jsonObject1.getString("EMAIL"));
                            sale_title.setText(jsonObject1.getString("EMAIL") + "销售单");
                        }

                        m[i]=name;

                    }

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


                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {

            System.out.println("arg2="+arg2);

            try {

                JSONObject jsonObject = (JSONObject) jsonArray.get(arg2);

                phoneNum.setText(jsonObject.getString("PHONENUM"));

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

}
