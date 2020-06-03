package com.safety.android.Sale;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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

import java.util.ArrayList;
import java.util.HashMap;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sale_activity, null);

        Intent intent=getIntent();

        String jsonString=intent.getStringExtra("jsonString");

        if(jsonString!=null&&!jsonString.equals("")){
            try {

                JSONObject jsonObject = new JSONObject(jsonString);

                JSONArray jsonArray=jsonObject.getJSONArray("ids");

                System.out.println("jsonArray==============");
                MyTestUtil.print(jsonArray);

                LayoutInflater inflater = getLayoutInflater();
                LinearLayout layout_validate = (LinearLayout) view.findViewById(R.id.layout_sale);
                layout_validate.removeAllViews();
                List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();

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
                    EditText et_validate = (EditText) validateItem.findViewById(R.id.sale2);
                    TextView et_validateText=validateItem.findViewById(R.id.sale3);
                    Map<String,Object> map = new HashMap<String, Object>();
                    try {
                        tv_validateName.setText(jsonObject1.getString("name"));
                        et_validate.setText(jsonObject1.getString("cost"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        setContentView(view);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
                mSectionLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                           ArrayList<SectionItem> list = new ArrayList<>();
                            ArrayList<SectionItem> contents = new ArrayList<>();
                           int i=0;
                            for (i = 0; i < 10; i++) {
                                list.add(new SectionItem("load more item hhhhhhhhhhh" + (i+page*10)));
                              //  contents.add(new SectionItem("item " + i));
                                //list.add(new SectionItem("qmuiFloatLayout"));
                               // list.add(new SectionItem(qmuiPriorityLinearLayout));
                            }
                            page++;
                            mAdapter.finishLoadMore(section, list, loadMoreBefore, true);

                    }
                }, 1000);
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
