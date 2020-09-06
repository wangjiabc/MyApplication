package com.safety.android.Sale;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.safety.android.qmuidemo.view.QDListSectionAdapter;
import com.safety.android.qmuidemo.view.SectionHeader;
import com.safety.android.qmuidemo.view.SectionItem;
import com.safety.android.tools.MyTestUtil;
import com.safety.android.tools.SwipeBackController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
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

public class SaleListActivity extends AppCompatActivity {

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

    private String search2="";

    private Map<Integer, JSONObject> itemMap=new HashMap<>();

    private Map<Integer,JSONObject> selectMap=new HashMap();

    private QMUIStickySectionAdapter.ViewHolder viewHolder;

    private boolean isCost=false;

    private double allCost=0;

    Integer catalog = null;

    private String searchCatalog="";

    private QDListSectionAdapter qdListSectionAdapter;

    private boolean refurbish=true;

    private Integer currentPostion;

    private SwipeBackController swipeBackController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent=getIntent();

        String jsonString=intent.getStringExtra("jsonString");

        try {
            if(jsonString!=null) {
                JSONObject jsonObject = new JSONObject(jsonString);
                catalog = jsonObject.getInt("catalog");
            }else{
                catalog=null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(catalog!=null){
            searchCatalog="&catalog="+catalog;
        }

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_second, null);

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
                refurbish=false;
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String queryText) {

                search="&name=*"+queryText+"*";
                search2="&name="+queryText;
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
        System.out.println("ev====="+ev);
        if (swipeBackController.processEvent(ev)) {
            return true;
        } else {
            return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        List<PermissionInfo> list= PermissionLab.get(getApplicationContext()).getPermissionInfo();

        Iterator<PermissionInfo> iterator=list.iterator();

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;

    }

    /* 利用反射机制调用MenuBuilder的setOptionalIconsVisible方法设置mOptionalIconsVisible为true，给菜单设置图标时才可见
     * 让菜单同时显示图标和文字
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("click");
        switch (item.getItemId()) {
            case R.id.menu_item_add:
                selectMap=qdListSectionAdapter.getSelectMap();

                if(selectMap.size()>0) {
                    JSONArray jsonArray = new JSONArray();

                    for (Map.Entry<Integer, org.json.JSONObject> sMap : selectMap.entrySet()) {
                        JSONObject json = sMap.getValue();
                        jsonArray.put(json);
                    }

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("ids", jsonArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(getApplicationContext(), SaleActivity.class);
                    intent.putExtra("jsonString", jsonObject.toString());
                    startActivityForResult(intent, 1);
                }
                break;
        }

        return false;
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
                        refurbish=true;
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
        if(refurbish)
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
                                if(searchCatalog!=null&&!searchCatalog.equals(""))
                                    cSearch+=searchCatalog;
                                if(search!=null&&!search.equals(""))
                                    cSearch+=search;
                                String json = new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/food/material/list?column=storage&order=asc&pageNo=" + page + "&pageSize="+size+cSearch);

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
                    try {

                        JSONObject jsonObject = null;

                        final int n=holder.getAdapterPosition();
                        currentPostion=n;

                        jsonObject = selectMap.get(holder.getAdapterPosition());
                        if (jsonObject == null) {
                            jsonObject = itemMap.get(holder.getAdapterPosition());
                        }

                        final JSONObject finalJsonObject = jsonObject;

                        JSONArray jsonArray=new JSONArray();

                        jsonArray.put(itemMap.get(holder.getAdapterPosition()));

                        JSONObject jsonObject1=new JSONObject();
                        try {
                            jsonObject1.put("ids",jsonArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(getApplicationContext(), SaleActivity.class);
                        intent.putExtra("jsonString", jsonObject1.toString());
                        startActivityForResult(intent, 1);

                        /*new AlertDialog.Builder(SaleListActivity.this)
                                .setTitle(finalJsonObject.getString("name"))
                                .setMessage("零售价:"+finalJsonObject.getDouble("retailprice"))
                                .setPositiveButton("出售", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        dialogInterface.dismiss();



                                    }
                                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                })
                                .create()
                                .show();
                            */


                    } catch (ClassCastException e) {
                        e.printStackTrace();
                        ((TextView) holder.itemView).setText("");
                    }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("requestCode===="+requestCode+"         resultCode==="+resultCode);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }else if(requestCode==1){

            selectMap=qdListSectionAdapter.getSelectMap();

            if(currentPostion!=null) {
                JSONObject jsonObject1=itemMap.get(currentPostion);
                try {
                    JSONArray jsonArray=new JSONArray(data.getStringExtra("value"));
                    int storage = jsonObject1.getInt("storage");
                    JSONObject jsonObject11=jsonArray.getJSONObject(0);
                    jsonObject1.put("storage",storage-jsonObject11.getInt("number"));
                    itemMap.put(currentPostion,jsonObject1);
                    currentPostion=null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            try {

                JSONArray jsonArray=new JSONArray(data.getStringExtra("value"));
                System.out.println("jsonarray=========================================");
                MyTestUtil.print(jsonArray);

                for (Integer key : selectMap.keySet()) {

                    JSONObject jsonObject1 = selectMap.get(key);
                    int order = jsonObject1.getInt("0");
                    long id=0;
                    try {
                        id= (long) jsonObject1.get("id");
                    }catch (Exception e){
                        Integer s= (Integer) jsonObject1.get("id");
                        String ss=s.toString();
                        id=Long.valueOf(ss);
                    }
                    JSONObject jsonObject2 = itemMap.get(order);
                    System.out.println("order=============="+order);
                    System.out.println("id=============="+id);
                    MyTestUtil.print(jsonObject2);
                    int storage = jsonObject2.getInt("storage");
                    int addStorage=0;
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        long rid= 0;
                        try {
                            rid= (long) jsonObject.get("id");
                        }catch (Exception e){
                            Serializable s= (Serializable) jsonObject.get("id");
                            rid=Long.valueOf(s.toString());
                        }
                        System.out.println("rid========================"+rid);
                        if(rid==id){
                            addStorage=jsonObject.getInt("number");
                            continue;
                        }

                    }

                    System.out.println("addStorage========================"+addStorage);

                    jsonObject2.put("storage", storage - addStorage);

                    MyTestUtil.print(jsonObject2);

                    itemMap.put(order, jsonObject2);
                }
            }catch (Exception e){
                    e.printStackTrace();
            }

            new FetchItemsUpdate().execute();

        }

    }

    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            String aSearch="";
            String cSearch="";

            if(searchCatalog!=null&&!searchCatalog.equals("")) {
                cSearch += searchCatalog;
                aSearch+=searchCatalog;
            }

            if(search!=null&&!search.equals("")) {
                cSearch += search;
                aSearch += search2;
            }

            System.out.println("aSearch===="+aSearch);

            String res=new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/storageLog/storageLog/getAllCost?"+aSearch);

            try {
                JSONObject jsonObject=new JSONObject(res);
                JSONObject jsonObject1=jsonObject.getJSONObject("result");
                allCost=jsonObject1.getDouble("ALLCOST");
                System.out.println("allCost============"+allCost);
            } catch (JSONException e) {
                e.printStackTrace();
                allCost=0;
            }

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/food/material/list?column=sale&order=desc&pageNo=" + page + "&pageSize"+size+cSearch);
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


    private class FetchItemsUpdate extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            String aSearch="";

            if(searchCatalog!=null&&!searchCatalog.equals("")) {
                aSearch+=searchCatalog;
            }

            if(search!=null&&!search.equals("")) {
                aSearch += search2;
            }

            System.out.println("aSearch===="+aSearch);

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/storageLog/storageLog/getAllCost?"+aSearch);

        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    try {
                        JSONObject jsonObject=new JSONObject(items);
                        JSONObject jsonObject1=jsonObject.getJSONObject("result");
                        allCost=jsonObject1.getDouble("ALLCOST");
                        System.out.println("allCost============"+allCost);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        allCost=0;
                    }

                    ArrayList<QMUISection<SectionHeader, SectionItem>> list = new ArrayList<>();

                    ArrayList<SectionItem> contents = new ArrayList<>();

                    JSONArray records = new JSONArray();

                    for(int key:itemMap.keySet()) {
                        JSONObject jsonObject=itemMap.get(key);
                        records.put(jsonObject);
                    }

                    JSONObject result= new JSONObject();


                    result.put("records",records);

                    result.put("total",total);

                    JSONObject jsonObject1= new JSONObject();

                    jsonObject1.put("result",result);


                    contents = addContents2(contents, jsonObject1);

                    BigDecimal bigDecimal = new BigDecimal(allCost/10000);
                    double f1 = bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();//2.转换后的数字四舍五入保留小数点;
                    String rs = String.valueOf(f1);

                    SectionHeader header = new SectionHeader("共"+total+"条"+"        "+rs+"万元");
                    QMUISection<SectionHeader, SectionItem> section = new QMUISection<>(header, contents, false);
                    section.setExistAfterDataToLoad(true);
                    list.add(section);
                    qdListSectionAdapter.setData(list);
                    qdListSectionAdapter.notifyDataSetChanged();



                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplication(),"失败",Toast.LENGTH_SHORT).show();
                }

            }

        }

    }

    private JSONObject process(int order,JSONObject jsonObject1) throws JSONException {

        JSONObject jsonObject2=new JSONObject();

        try {
            Serializable id= (Serializable) jsonObject1.get("id");
            jsonObject2.put("id",id);
        }catch (Exception e){
            e.printStackTrace();
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

    private ArrayList<SectionItem> addContents2(ArrayList<SectionItem> contents,JSONObject jsonObject) throws JSONException {

        JSONObject result= (JSONObject) jsonObject.get("result");

        JSONArray records = result.getJSONArray("records");

        total=result.getInt("total");

        for (int i = 0; i < records.length(); i++) {

            int order=i+1;

            JSONObject jsonObject1 = (JSONObject) records.get(i);

            JSONObject jsonObject2=process(order,jsonObject1);

            contents.add(new SectionItem(jsonObject2.toString()));
        }

        return contents;
    }

}

