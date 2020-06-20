package com.safety.android.AccountheadList;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.safety.android.Sale.SaleListActivity;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.tools.MyTestUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AccountheadActivity extends AppCompatActivity {

    //private Spinner spinner;
    //private Spinner spinner2;
    private ArrayAdapter<String> adapter;

    private TextView sale_title;
    private TextView saleall2;
    private TextView saleall3;
    private TextView saleall4;

    private TextView phoneNum;
    private TextView orderNumber;

    private Button saleButon;

    private Integer arg;

    // private int type=2;

    private TextView atv_content;
    private MultiAutoCompleteTextView matv_content;

    private View view;

    private Integer id=0;

    private String billNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.accounthead_activity, null);

        sale_title=view.findViewById(R.id.sale_title);
        saleall2=view.findViewById(R.id.saleall2);
        saleall3=view.findViewById(R.id.saleall3);
        saleall4=view.findViewById(R.id.saleall4);

        phoneNum=view.findViewById(R.id.phoneNum);
        orderNumber=view.findViewById(R.id.orderNumber);

        saleButon=view.findViewById(R.id.sale_button);

        atv_content=view.findViewById(R.id.atv_content);

        Intent intent=getIntent();

        Integer jsonInt=intent.getIntExtra("jsonString",0);

        System.out.println("jsonString======"+jsonInt);

        id=jsonInt;

        saleButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        new FetchItemsTask().execute();

        setContentView(view);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(Menu.NONE,Menu.FIRST+1,1,"删除订单").setIcon(android.R.drawable.ic_menu_delete);

        return true;

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

        System.out.println("click");
        switch (item.getItemId()) {
            case Menu.FIRST + 1:

                new AlertDialog.Builder(AccountheadActivity.this)
                        .setTitle("删除"+billNo+"订单?")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                new FetchItemsTaskDel().execute();

                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/accounthead/accounthead/queryById?id="+id);

        }


        @Override
        protected void onPostExecute(String json) {

            try {

                JSONObject jsonObject = new JSONObject(json);
                String success = jsonObject.optString("success", null);

                if(success.equals("true")){

                    JSONObject jsonObject0=jsonObject.getJSONObject("result");


                        final List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();

                        try {

                            JSONArray jsonArray=jsonObject0.getJSONArray("accountheads");

                            System.out.println("jsonArray==============");
                            MyTestUtil.print(jsonArray);

                            LayoutInflater inflater = getLayoutInflater();
                            LinearLayout layout_validate = (LinearLayout) view.findViewById(R.id.layout_sale);
                            layout_validate.removeAllViews();

                            int allCount=0;

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

                                    tv_validateName.setText(jsonObject1.getString("materialName"));
                                    tv_validateName.setGravity(Gravity.CENTER);
                                    et_validatePrice.setText(jsonObject1.getString("changeamount"));
                                    et_validatePrice.setGravity(Gravity.CENTER);
                                    Integer count=jsonObject1.getInt("count");
                                    allCount+=count;
                                    et_validateCount.setText(String.valueOf(count));
                                    et_validateCount.setGravity(Gravity.CENTER);
                                    tv_valiateAll.setText(jsonObject1.getString("totalprice"));
                                    tv_valiateAll.setGravity(Gravity.CENTER);



                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }


                            saleall3.setText(String.valueOf(allCount));
                            saleall4.setText(jsonObject0.getString("allTotalprice"));
                            phoneNum.setText(jsonObject0.getString("phone"));
                            orderNumber.setText(jsonObject0.getString("billno"));
                            atv_content.setText(jsonObject0.getString("realname"));
                            sale_title.setText(jsonObject0.getString("email") + "销售单");

                            billNo=jsonObject0.getString("billno");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }



            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    private class FetchItemsTaskDel extends AsyncTask<Integer,Void,String> {

        @Override
        protected String doInBackground(Integer... params) {

            JSONObject jsonObject=new JSONObject();

            return new OKHttpFetch(getApplication()).post(FlickrFetch.base+"/accounthead/accounthead/delete?id="+id,jsonObject,"delete");
        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    JSONObject jsonObject=new JSONObject(items);


                    String success = jsonObject.optString("success", null);
                    String message = jsonObject.optString("message", null);

                    Toast.makeText(getApplication(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();

                    if(success.equals("true")) {

                        Intent intent = new Intent();
                        intent.putExtra("value", jsonObject.toString());
                        setResult(SaleListActivity.RESULT_OK, intent);
                        finish();

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplication(),"添加库存失败",Toast.LENGTH_SHORT).show();
                }

            }

        }

    }



}