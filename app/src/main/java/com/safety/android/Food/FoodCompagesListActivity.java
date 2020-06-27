package com.safety.android.Food;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.qmuiteam.qmui.widget.section.QMUISection;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionLayout;
import com.safety.android.SQLite3.PermissionInfo;
import com.safety.android.SQLite3.PermissionLab;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.qmuidemo.view.HtmlImageGetter;
import com.safety.android.qmuidemo.view.QDListSectionAdapter;
import com.safety.android.qmuidemo.view.SectionHeader;
import com.safety.android.qmuidemo.view.SectionItem;
import com.safety.android.qmuidemo.view.getGradientDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.safety.android.MainActivity.dataUrl;

public class FoodCompagesListActivity extends AppCompatActivity {
    QMUIPullRefreshLayout mPullRefreshLayout;

    QMUIStickySectionLayout mSectionLayout;

    private RecyclerView.LayoutManager mLayoutManager;
    protected QMUIStickySectionAdapter<SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> mAdapter;

    private int page=1;

    private int size=10;

    private int total=0;

    private BlockingQueue<ArrayList<SectionItem>> queue;

    private View view;

    private SearchView mSearchView;

    private String search="";

    private Map<Integer, JSONObject> itemMap=new HashMap<>();

    private Map<Integer,JSONObject> selectMap=new HashMap();

    private QMUIStickySectionAdapter.ViewHolder viewHolder;

    private boolean isCost=false;

    private Button foodButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.food_compages_list, null);

        mPullRefreshLayout=view.findViewById(R.id.pull_to_refresh);
        mSectionLayout=view.findViewById(R.id.section_layout);

        initRefreshLayout();
        initStickyLayout();

        initData();

        queue=new ArrayBlockingQueue<>(3);

        setContentView(view);

        mSearchView = findViewById(R.id.search);
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setFocusable(false);
        mSearchView.clearFocus();

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String queryText) {

                System.out.println("onQueryTextChange:"+queryText);

                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String queryText) {

                search="&name=*"+queryText+"*";
                mSearchView.clearFocus();
                mPullRefreshLayout.finishRefresh();
                page=1;
                total=0;
                itemMap=new HashMap<>();
                selectMap=new HashMap<>();
                initData();
                return true;
            }
        });

        foodButton=view.findViewById(R.id.food_button);

        foodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONArray jsonArray=new JSONArray();

                for(Map.Entry<Integer,org.json.JSONObject> map:selectMap.entrySet()) {
                    JSONObject jsonObject = map.getValue();
                    jsonArray.put(jsonObject);
                }

                Intent intent = new Intent();
                intent.putExtra("value", jsonArray.toString());
                setResult(Activity.RESULT_OK, intent);
                finish();

            }
        });

        List<PermissionInfo> list= PermissionLab.get(getApplicationContext()).getPermissionInfo();

        Iterator<PermissionInfo> iterator=list.iterator();

        while (iterator.hasNext()){

            PermissionInfo permissionInfo=iterator.next();

            String action=permissionInfo.getAction();
            String component=permissionInfo.getComponent();

            if(action!=null){
                if(action.equals("materialList:cost")){
                    isCost=true;
                    continue;
                }
            }

        }

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

    private void initRefreshLayout() {
        mPullRefreshLayout.setOnPullListener(new QMUIPullRefreshLayout.OnPullListener() {
            @Override
            public void onMoveTarget(int offset) {

            }

            @Override
            public void onMoveRefreshView(int offset) {

            }

            @Override
            public void onRefresh() {

                mPullRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        mSearchView.clearFocus();
                        mPullRefreshLayout.finishRefresh();
                        itemMap=new HashMap<>();
                        selectMap=new HashMap<>();
                        page=1;
                        total=0;
                        search="";
                        initData();

                    }
                }, 1000);
            }
        });
    }

    protected QMUIStickySectionAdapter<
            SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> createAdapter() {
        return new QDListSectionAdapter(1);
    }

    protected RecyclerView.LayoutManager createLayoutManager() {

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

                        new Thread(new Runnable(){

                            @Override
                            public void run() {

                                ArrayList<SectionItem> contents = new ArrayList<>();
                                String cSearch="";
                                if(search!=null&&!search.equals(""))
                                    cSearch=search;
                                String json = new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/food/material/list?column=storage&order=asc&pageNo=" + page + "&pageSize="+size+"&combination=0"+cSearch);

                                try {
                                    JSONObject jsonObject = new JSONObject(json);
                                    String success = jsonObject.optString("success", null);

                                    if (success.equals("true")) {

                                        contents=addContents(contents,jsonObject);
                                    }
                                    queue.put(contents);
                                } catch (JSONException | InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }

                        }).start();

                        ArrayList<SectionItem> contents = null;
                        try {
                            contents =queue.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        boolean existMoreData=true;

                        //System.out.println("total="+total+"   page="+page);
                        if(total<(page*10)) {
                            existMoreData=false;
                        }

                        page++;

                        mAdapter.finishLoadMore(section, contents, loadMoreBefore, existMoreData);


                    }
                }, 300);
            }

            @Override
            public void onItemClick(final QMUIStickySectionAdapter.ViewHolder holder, final int position) {
                Toast.makeText(getApplicationContext(), "click item " + position, Toast.LENGTH_SHORT).show();
                viewHolder=holder;
                if(position!=0) {
                    try {

                        JSONObject jsonObject = null;

                        final int n=holder.getAdapterPosition();

                        jsonObject = selectMap.get(holder.getAdapterPosition());
                        String buttonText;
                        if (jsonObject == null) {
                            jsonObject = itemMap.get(holder.getAdapterPosition());
                            buttonText="选择";
                        }else{
                            buttonText="取消选择";
                        }

                        final JSONObject finalJsonObject = jsonObject;

                        new AlertDialog.Builder(FoodCompagesListActivity.this)
                                .setTitle(finalJsonObject.getString("name"))
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(FoodCompagesListActivity.this, "点击了取消按钮", Toast.LENGTH_SHORT).show();
                                        dialogInterface.dismiss();
                                    }
                                })
                                .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        JSONObject jsonObject = null;
                                        jsonObject = selectMap.get(n);
                                        String s = "";
                                        if (jsonObject == null) {
                                            jsonObject = itemMap.get(n);
                                            try {
                                                s = StringToHtml2(jsonObject);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            selectMap.put(n, jsonObject);
                                        } else {
                                            try {
                                                s = StringToHtml(jsonObject);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            selectMap.remove(n);
                                        }
                                        Drawable defaultDrawable = new getGradientDrawable(Color.YELLOW, 100).getGradientDrawable();
                                        final Html.ImageGetter imgGetter = new HtmlImageGetter((TextView) holder.itemView, dataUrl, defaultDrawable);


                                        final Spanned sp = Html.fromHtml(s, Html.FROM_HTML_MODE_COMPACT, imgGetter, null);
                                        ((TextView) holder.itemView).setText(sp);
                                        dialogInterface.dismiss();
                                    }
                                })
                                .create()
                                .show();



                    } catch (ClassCastException | JSONException e) {
                        e.printStackTrace();
                        ((TextView) holder.itemView).setText("");
                    }
                }
            }

            @Override
            public boolean onItemLongClick(QMUIStickySectionAdapter.ViewHolder holder, int position) {
                Toast.makeText(getApplicationContext(), "long click item " + position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mSectionLayout.setAdapter(mAdapter, true);

        new FetchItemsTask().execute();


    }

    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            String cSearch="";
            if(search!=null&&!search.equals(""))
                cSearch=search;
            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/food/material/list?column=storage&order=asc&pageNo=" + page + "&pageSize"+size+"&combination=0"+cSearch);
        }


        @Override
        protected void onPostExecute(String json) {

            ArrayList<QMUISection<SectionHeader, SectionItem>> list = new ArrayList<>();

            ArrayList<SectionItem> contents = new ArrayList<>();

            try {

                JSONObject jsonObject = new JSONObject(json);
                String success = jsonObject.optString("success", null);

                if(success.equals("true")){

                    contents=addContents(contents,jsonObject);

                    SectionHeader header = new SectionHeader("共"+total+"条");
                    QMUISection<SectionHeader, SectionItem> section = new QMUISection<>(header, contents, false);

                    list.add(section);

                    section.setExistAfterDataToLoad(true);
                    System.out.println("page="+page);

                    mAdapter.setData(list);

                    page++;

                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private ArrayList<SectionItem> addContents(ArrayList<SectionItem> contents,JSONObject jsonObject) throws JSONException {

        JSONObject result= (JSONObject) jsonObject.get("result");

        JSONArray records = result.getJSONArray("records");

        total=result.getInt("total");

        for (int i = 0; i < records.length(); i++) {

            int order=i+1+(page-1)*10;

            JSONObject jsonObject1 = (JSONObject) records.get(i);
            jsonObject1.put("order",order);
            String s=StringToHtml(jsonObject1);
            //String s="<p>(1).Name:&nbsp;Toking Hazard by Joking Hazard</p><p>(2).Material: Paper</p><p>(3).Package: Box</p><p><br/></p><p>50 Marijuana themed cards to heighten your Joking Hazard experience.<br/></p><p>This is an expansion pack. It requires Joking Hazard to play</p><p>In addition to the cards, there is a secret in each box!</p><p>The box is OVERSIZED to fit the surprise</p><p><br/></p>";
            SpannableString spannableString = new SpannableString(s);

            itemMap.put(order,jsonObject1);

            contents.add(new SectionItem(s));
        }

        return contents;
    }

    private String StringToHtml(JSONObject jsonObject) throws JSONException {
        Integer order=jsonObject.getInt("order");
        String first="";
        if(order<10)
            first="<span><font color='blue'　size='30'>&nbsp;&nbsp;"+order+"&nbsp;&nbsp;</font></span>";
        else if(10<order&&order<100)
            first="<span><font color='blue'　size='30'>"+order+"&nbsp;&nbsp;</font></span>";
        else
            first="<span><font color='blue'　size='30'>"+order+"</font></span>";
        String name = jsonObject.getString("name");
        Integer storage = jsonObject.getInt("storage");
        Double cost = jsonObject.getDouble("cost");
        Double retailprice=jsonObject.getDouble("retailprice");
        String img=jsonObject.getString("img");
        String costText="";
        if(isCost)
            costText="<span>成本:" + cost + "</span>";
        if(img!=null&&!img.equals(""))
            img="<img src='http://qiniu.lzxlzc.com/compress/"+img+"'/>";
        String s = "<p>"+first+img+"&nbsp;&nbsp;<big><font size='20'><b>" + name + "</b></font></big></p>" +
                "<block quote>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;库存:" + storage + "</span>&nbsp;&nbsp;"+costText+ "</span>&nbsp;&nbsp;<span>售价:" + retailprice + "</block quote>";
        return s;
    }

    private String StringToHtml2(JSONObject jsonObject) throws JSONException {
        Integer order=jsonObject.getInt("order");
        String first="";
        if(order<10)
            first="<span>&nbsp;&nbsp;<font color='red'　size='30'>"+order+"&nbsp;&nbsp;</font></span>";
        else if(10<order&&order<100)
            first="<span><font color='red'　size='30'>"+order+"&nbsp;&nbsp;</font></span>";
        else
            first="<span><font color='red'　size='30'>"+order+"</font></span>";
        String name = jsonObject.getString("name");
        Integer storage = jsonObject.getInt("storage");
        Double cost = jsonObject.getDouble("cost");
        Double retailprice=jsonObject.getDouble("retailprice");
        String img=jsonObject.getString("img");
        String costText="";
        if(isCost)
            costText="<span><font color='red' size='20'>成本:" + cost + "</span>";
        if(img!=null&&!img.equals(""))
            img="<img src='http://qiniu.lzxlzc.com/compress/"+img+"'/>";
        String s ="<p>"+first+img+"&nbsp;&nbsp;<span><big><font color='red'　size='20'><b>" + name + "</b></font></big></p>" +
                "<block quote>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;<font color='red' size='20'>库存:" + storage + "</font>&nbsp;&nbsp;"+costText+ "</span>&nbsp;&nbsp;<span><font color='red' size='20'>售价:" + retailprice + "</block quote>";
        return s;
    }



}
