package com.safety.android.Sale;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SaleActivity extends AppCompatActivity {

    //private Spinner spinner;
    //private Spinner spinner2;
    private ArrayAdapter<String> adapter;

    private TextView sale_title;
    private TextView saleall2;
    private TextView saleall3;
    private TextView saleall4;

    private JSONArray jsonArray;

    private TextView phoneNum;
    private TextView orderNumber;

    private Button saleButon;

    private Integer arg;

   // private int type=2;

    private AutoCompleteTextView atv_content;
    private MultiAutoCompleteTextView matv_content;

    private View view;

    private JSONArray returnArray;

    private TextView atvNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sale_activity, null);

        sale_title=view.findViewById(R.id.sale_title);
        saleall2=view.findViewById(R.id.saleall2);
        saleall3=view.findViewById(R.id.saleall3);
        saleall4=view.findViewById(R.id.saleall4);

        phoneNum=view.findViewById(R.id.phoneNum);
        orderNumber=view.findViewById(R.id.orderNumber);

        saleButon=view.findViewById(R.id.sale_button);

        atvNew=view.findViewById(R.id.atv_new);

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
        final String newsNo = df.format(new Date())+String.valueOf((int)(Math.random()*9+1)*1000);

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
                        map.put("id",jsonObject1.getInt("id"));
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


            saleButon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(arg==null){

                        Toast.makeText(getApplication(),"请选择客户",Toast.LENGTH_SHORT).show();

                    }else {

                        com.alibaba.fastjson.JSONArray jsonArray1=new com.alibaba.fastjson.JSONArray();

                        Iterator<Map<String, Object>> iterator = list.iterator();

                        returnArray=new JSONArray();

                        while (iterator.hasNext()) {

                            Map<String, Object> map = iterator.next();
                            int id = (int) map.get("id");
                            EditText et_validatePrice = (EditText) map.get("retailprice");
                            EditText et_validateCount = (EditText) map.get("count");
                            TextView tv_valiateAll= (TextView) map.get("allPrice");


                            float totalAllPrice=0;

                            float sprice = 0;

                            if (et_validatePrice.getText().toString() != null && !et_validatePrice.getText().toString().equals(""))
                                sprice = Float.parseFloat(et_validatePrice.getText().toString());

                            String  number = "";

                            if (et_validateCount.getText().toString() != null && !et_validateCount.getText().toString().equals(""))
                                number = et_validateCount.getText().toString();


                            if(tv_valiateAll.getText().toString()!=null&&!tv_valiateAll.getText().toString().equals(""))
                                totalAllPrice=Float.parseFloat(tv_valiateAll.getText().toString());

                            DecimalFormat fnum = new DecimalFormat("##0.00");
                            String price = fnum.format(sprice);
                            String totalPrice=fnum.format(totalAllPrice);

                            String ordernumber=orderNumber.getText().toString();

                            int supplierId=0;
                            String supplier="";

                            try {
                                JSONObject jsonObject= (JSONObject) jsonArray.get(arg);
                                supplierId=jsonObject.getInt("ID");
                                supplier=jsonObject.getString("SUPPLIER");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            com.alibaba.fastjson.JSONObject jsonObject=new com.alibaba.fastjson.JSONObject();
                            
                            jsonObject.put("id",id);
                            jsonObject.put("number",number);
                            jsonObject.put("retailprice",price);
                            jsonObject.put("totalprice",totalPrice);
                            jsonObject.put("orderNumber",ordernumber);
                            jsonObject.put("supplierId",supplierId);
                            jsonObject.put("supplier",supplier);
                           // jsonObject.put("type",String.valueOf(type));


                            jsonArray1.add(jsonObject);

                            JSONObject jsonObject1=new JSONObject();

                            try {
                                jsonObject1.put("id",id);
                                jsonObject1.put("number",number);
                                returnArray.put(jsonObject1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }



                        }

                        new FetchItemsTaskSale().execute(jsonArray1);
                    }
                }
            });


        }
/*
        spinner = view.findViewById(R.id.Spinner01);
        spinner2 = view.findViewById(R.id.Spinner02);

        String[] m={"微信支付","现金","支付宝", "银联"};

        ArrayAdapter<String> adapter2;

        //将可选内容与ArrayAdapter连接起来
        adapter2 = new ArrayAdapter<String>(SaleActivity.this,android.R.layout.simple_spinner_item,m);

        //设置下拉列表的风格
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //将adapter 添加到spinner中
        spinner2.setAdapter(adapter2);

        //添加事件Spinner事件监听
        spinner2.setOnItemSelectedListener(new SpinnerSelectedListener2());

        //设置默认值
        spinner2.setVisibility(View.VISIBLE);
*/


        atvNew.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                LayoutInflater inflater = getLayoutInflater();
                View validateView = inflater.inflate(
                        R.layout.dialog_validate, null);
                final LinearLayout layout_validate = (LinearLayout) validateView.findViewById(R.id.layout_validate);
                layout_validate.removeAllViews();
                final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

                View validateItem = inflater.inflate(R.layout.item_validate_enter3, null);
                validateItem.setTag(0);
                layout_validate.addView(validateItem);
                TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                Map<String, Object> map = new HashMap<String, Object>();

                    tv_validateName.setText("客户名称");
                    et_validate.setText("");

                    map.put("name", tv_validateName);
                    map.put("value", et_validate);



                list.add(map);

                View validateItem2 = inflater.inflate(R.layout.item_validate_enter3, null);
                validateItem2.setTag(1);
                layout_validate.addView(validateItem2);
                TextView tv_validateName2 = (TextView) validateItem2.findViewById(R.id.tv_validateName);
                EditText et_validate2 = (EditText) validateItem2.findViewById(R.id.et_validate);
                Map<String, Object> map2 = new HashMap<String, Object>();
                tv_validateName2.setText("客户电话");
                et_validate2.setText("");

                map2.put("name", tv_validateName2);
                map2.put("value", et_validate2);

                list.add(map2);

                View validateItem3 = inflater.inflate(R.layout.item_validate_enter3, null);
                validateItem3.setTag(2);
                layout_validate.addView(validateItem3);
                TextView tv_validateName3 = (TextView) validateItem3.findViewById(R.id.tv_validateName);
                EditText et_validate3 = (EditText) validateItem3.findViewById(R.id.et_validate);
                Map<String, Object> map3 = new HashMap<String, Object>();
                tv_validateName3.setText("客户地址");
                et_validate3.setText("");

                map3.put("name", tv_validateName3);
                map3.put("value", et_validate3);

                list.add(map3);

                AlertDialog dialog = new AlertDialog.Builder(SaleActivity.this).setTitle("新建客户")
                        .setView(validateView)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String name = ((EditText) list.get(0).get("value")).getText().toString();
                                String phone = ((EditText) list.get(1).get("value")).getText().toString();
                                String address = ((EditText) list.get(2).get("value")).getText().toString();

                                if(name==null||name.equals("")){

                                    Toast.makeText(SaleActivity.this, "客户名称不能空", Toast.LENGTH_LONG).show();

                                }else if(phone==null||phone.equals("")){

                                    Toast.makeText(SaleActivity.this, "客户电话不能空", Toast.LENGTH_LONG).show();

                                }else {

                                    JSONObject jsonObject = new JSONObject();
                                    try {
                                        jsonObject.put("supplier", name);
                                        jsonObject.put("phonenum", phone);
                                        jsonObject.put("address", address);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    new FetchItemsTaskAdd().execute(jsonObject);

                                    dialog.dismiss();

                                }


                            }

                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();

            }
        });


        new FetchItemsTask().execute();

        setContentView(view);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // TODO Auto-generated method stub
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

                    final String[] data =new String[jsonArray.length()];

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);

                        String name=jsonObject1.getString("SUPPLIER");

                        if(jsonObject1.getString("EMAIL")!=null&&!jsonObject1.getString("EMAIL").equals("null")) {
                            System.out.println("email======="+jsonObject1.getString("EMAIL"));
                            sale_title.setText(jsonObject1.getString("EMAIL") + "销售单");
                        }

                        data[i]=name;

                    }

                    atv_content = (AutoCompleteTextView) view.findViewById(R.id.atv_content);

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SaleActivity.
                            this, android.R.layout.simple_dropdown_item_1line, data);
                    atv_content.setAdapter(adapter);

                    System.out.println(data);

                    atv_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            Object obj = parent.getItemAtPosition(position);

                            String username= (String) obj;

                            for(int i=0;i<data.length;i++){

                                if(username.equals(data[i])){
                                    arg=i;

                                    try {

                                        JSONObject jsonObject = (JSONObject) jsonArray.get(arg);

                                        phoneNum.setText(jsonObject.getString("PHONENUM"));

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


    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {

            System.out.println("arg2="+arg2);

            arg=arg2;



        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
/*
    class SpinnerSelectedListener2 implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {

            if(arg2==1){
                type=2;
            }else if(arg2==2){
                type=1;
            }else {
                type = arg2 + 1;
            }
            System.out.println("type="+type);

        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
*/
    private class FetchItemsTaskSale extends AsyncTask<com.alibaba.fastjson.JSONArray,Void,String> {

        @Override
        protected String doInBackground(com.alibaba.fastjson.JSONArray... params) {

            MyTestUtil.print(params);

            com.alibaba.fastjson.JSONArray jsonArray=params[0];

            MyTestUtil.print(jsonArray);

            String items=Uri.encode(jsonArray.toJSONString());

            System.out.println("items========="+items);

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/food/material/sale?items="+items);

        }


        @Override
        protected void onPostExecute(String json) {

            try {

                JSONObject jsonObject = new JSONObject(json);
                String success = jsonObject.optString("success", null);

                    Toast.makeText(SaleActivity.this, jsonObject.optString("message"), Toast.LENGTH_LONG).show();

                    if (success.equals("true")) {

                        Intent intent = new Intent();
                        intent.putExtra("value", returnArray.toString());
                        setResult(SaleListActivity.RESULT_OK, intent);
                        finish();

                    }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private class FetchItemsTaskAdd extends AsyncTask<JSONObject,Void,String> {

        @Override
        protected String doInBackground(JSONObject... params) {

            MyTestUtil.print(params);

            JSONObject jsonObject=params[0];


            return new OKHttpFetch(getApplicationContext()).post(FlickrFetch.base + "/supplier/supplier/add",jsonObject,"post");

        }


        @Override
        protected void onPostExecute(String json) {

            try {

                JSONObject jsonObject = new JSONObject(json);
                String success = jsonObject.optString("success", null);

                Toast.makeText(SaleActivity.this, jsonObject.optString("message"), Toast.LENGTH_LONG).show();

                if(success.equals("true")){
                    System.out.println("success==="+success);
                    new FetchItemsTask().execute();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

}
