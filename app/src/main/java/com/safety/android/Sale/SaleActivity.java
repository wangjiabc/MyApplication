package com.safety.android.Sale;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.qmuiteam.qmui.widget.section.QMUISection;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionLayout;
import com.safety.android.qmuidemo.view.QDListSectionAdapter;
import com.safety.android.qmuidemo.view.SectionHeader;
import com.safety.android.qmuidemo.view.SectionItem;
import com.safety.android.tools.MyTestUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SaleActivity extends AppCompatActivity {


    QMUIStickySectionLayout mSectionLayout;

    private Map<Integer,JSONObject> itemMap=new HashMap<>();

    private RecyclerView.LayoutManager mLayoutManager;
    protected QMUIStickySectionAdapter<SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> mAdapter;

    private int page=0;

    private TextView saleall2;
    private TextView saleall3;
    private TextView saleall4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sale_activity, null);

        saleall2=view.findViewById(R.id.saleall2);
        saleall3=view.findViewById(R.id.saleall3);
        saleall4=view.findViewById(R.id.saleall4);

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

            totalAllPrice+=allPrice*allCount;

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

    protected QMUIStickySectionAdapter<
            SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> createAdapter() {
        return new QDListSectionAdapter();
    }

    protected RecyclerView.LayoutManager createLayoutManager() {
       /* final GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int i) {
                return mAdapter.getItemIndex(i) < 0 ? layoutManager.getSpanCount() : 1;
            }
        });*/

        final LinearLayoutManager layoutManager =new LinearLayoutManager(getApplicationContext()) {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        };

        return layoutManager;
    }

    protected void initStickyLayout() {
        mLayoutManager = createLayoutManager();
        mSectionLayout.setLayoutManager(mLayoutManager);
    }

    private void initData() {
        mAdapter = createAdapter();
        mAdapter.setCallback(new QMUIStickySectionAdapter.Callback<SectionHeader, SectionItem>() {
            @Override
            public void loadMore(final QMUISection<SectionHeader, SectionItem> section, final boolean loadMoreBefore) {

            }

            @Override
            public void onItemClick(QMUIStickySectionAdapter.ViewHolder holder, int position) {
                Toast.makeText(getApplicationContext(), "click item " + position, Toast.LENGTH_SHORT).show();
                try {
                    String text = (String) ((TextView) holder.itemView).getText();

                    //holder.itemView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.qmui_config_color_gray_4));
                    final Spanned sp = Html.fromHtml("<font color='red' size='20'>" + text + "</font>", null, null);
                    ((TextView) holder.itemView).setText(sp);
                    Log.d("ddddddddd", "onItemClick: " + text + "  " + holder.getAdapterPosition());
                }catch (java.lang.ClassCastException e){

                    ((TextView) holder.itemView).setText("");
                }
            }

            @Override
            public boolean onItemLongClick(QMUIStickySectionAdapter.ViewHolder holder, int position) {
                Toast.makeText(getApplicationContext(), "long click item " + position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        mSectionLayout.setAdapter(mAdapter, true);
        ArrayList<QMUISection<SectionHeader, SectionItem>> list = new ArrayList<>();
        /*for (int i = 0; i < 1; i++) {
            list.add(createSection("header " + i, false,i));
        }*/

        ArrayList<SectionItem> contents = new ArrayList<>();
        for (int i = 0; i < 10; i++) {

            contents.add(new SectionItem("qmuiFloatLayout"+(i+page*10)));
            Log.d("aaaa", "createSection: "+i);
        }
        page++;
        SectionHeader header = new SectionHeader("1");
        QMUISection<SectionHeader, SectionItem> section = new QMUISection<>(header, contents, false);
        // if test load more, you can open the code
        section.setExistAfterDataToLoad(true);

        list.add(section);

        mAdapter.setData(list);
    }



}
