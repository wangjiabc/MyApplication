package com.safety.android.Food;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
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
import com.safety.android.qmuidemo.view.QDListSectionAdapter;
import com.safety.android.qmuidemo.view.SectionHeader;
import com.safety.android.qmuidemo.view.SectionItem;
import com.safety.android.tools.SwipeBackController;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FoodCatalogListActivity extends AppCompatActivity {
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

    private Integer catalog;

    QDListSectionAdapter qdListSectionAdapter;

    private SwipeBackController swipeBackController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent=getIntent();

        String jsonString=intent.getStringExtra("jsonString");

        if(jsonString!=null) {
            catalog = Integer.parseInt(jsonString);
            System.out.println("catalog="+catalog);
        }else{
            finish();
            System.out.println("jsonString is null");
        }

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.food_compages_list, null);

        mPullRefreshLayout=view.findViewById(R.id.pull_to_refresh);
        mSectionLayout=view.findViewById(R.id.section_layout);

        initRefreshLayout();
        initStickyLayout();

        initData();

        queue=new ArrayBlockingQueue<>(3);

        setContentView(view);
        swipeBackController = new SwipeBackController(this);

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

                new FetchItemsUpdateTask().execute();

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
    public boolean onTouchEvent(MotionEvent ev) {
        if (swipeBackController.processEvent(ev)) {
            return true;
        } else {
            return super.onTouchEvent(ev);
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
        qdListSectionAdapter=new QDListSectionAdapter(1);
        return qdListSectionAdapter;
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
                                String cSearch="&catalog=0";
                                if(search!=null&&!search.equals(""))
                                    cSearch+=search;
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
                viewHolder=holder;
                if(position!=0) {

                }
            }

            @Override
            public boolean onItemLongClick(QMUIStickySectionAdapter.ViewHolder holder, int position) {
                //Toast.makeText(getApplicationContext(), "long click item " + position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mSectionLayout.setAdapter(mAdapter, true);

        new FetchItemsTask().execute();


    }

    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            String cSearch="&catalog=0";
            if(search!=null&&!search.equals(""))
                cSearch+=search;
            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/food/material/list?column=storage&order=asc&pageNo=" + page + "&pageSize"+size+cSearch);
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

            JSONObject jsonObject2=process(order,jsonObject1);

            itemMap.put(order,jsonObject1);

            contents.add(new SectionItem(jsonObject2.toString()));

        }

        return contents;
    }


    private JSONObject process(int order,JSONObject jsonObject1) throws JSONException {

        JSONObject jsonObject2=new JSONObject();

        try {
            int id=jsonObject1.getInt("id");
            jsonObject2.put("id",id);
        }catch (Exception e){

        }

        jsonObject2.put("0",order);

        String name = jsonObject1.getString("name");
        jsonObject2.put("name",name);
        Integer storage = jsonObject1.getInt("storage");
        jsonObject2.put("2","库存:"+storage);
        jsonObject2.put("storage",storage);
        Double cost = 0.0;
        try {
            cost=jsonObject1.getDouble("cost");
        }catch (Exception e){
            jsonObject1.put("cost",0.00);
        }
        jsonObject2.put("cost",cost);
        Double retailprice =0.00;
        try {
            retailprice=jsonObject1.getDouble("retailprice");
        }catch (Exception e){
            jsonObject1.put("retailprice",0.00);
        }
        jsonObject2.put("retailprice",retailprice);
        jsonObject2.put("3","价格:"+retailprice);
        String img=jsonObject1.getString("img");
        String costText="";
        if(isCost) {
            costText = "成本:" + cost;
            jsonObject2.put("4",costText);
        }
        try {
            int combination=jsonObject1.getInt("combination");
            jsonObject2.put("combination",combination);
        }catch (Exception e){

        }
        if(img!=null&&!img.equals("null")&&!img.equals("")) {
            img = "http://qiniu.lzxlzc.com/compress/" + img;
            jsonObject2.put("img",img);
        }

        return jsonObject2;

    }

    private class FetchItemsUpdateTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {


            JSONArray jsonArray=new JSONArray();

            selectMap=qdListSectionAdapter.getSelectMap();

            for(Map.Entry<Integer,org.json.JSONObject> sMap:selectMap.entrySet()) {
                JSONObject json = sMap.getValue();
                try {
                    jsonArray.put(json.getInt("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            String value="";

            String ids=Uri.encode(jsonArray.toString());

            value+="items="+ids;

            value+="&catalog="+catalog;

            System.out.println("value======="+value);

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/food/material/updateCatalog?"+value);

        }

        @Override
        protected void onPostExecute(String json) {

            JSONObject jsonObject = null;

            try {

                jsonObject = new JSONObject(json);

                String success = jsonObject.optString("success", null);

                Toast.makeText(FoodCatalogListActivity.this, jsonObject.optString("message"), Toast.LENGTH_LONG).show();

                if (success.equals("true")) {

                    JSONArray jsonArray=new JSONArray();

                    for(Map.Entry<Integer,org.json.JSONObject> map:selectMap.entrySet()) {
                        JSONObject json1 = map.getValue();
                        jsonArray.put(json1);
                    }

                    Intent intent = new Intent();
                    intent.putExtra("value", jsonArray.toString());
                    setResult(Activity.RESULT_OK, intent);
                    finish();

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

}