package com.safety.android.Storage;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
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
import java.math.BigDecimal;
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

public class StorageListActivity extends AppCompatActivity {

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

    private Spinner spinner;

    private Integer diff;

    private boolean refurbish=true;

    private QDListSectionAdapter qdListSectionAdapter;

    private Map positionId=new HashMap();

    Integer catalog = null;

    private String searchCatalog="";

    private SwipeBackController swipeBackController;

    private int currentPostion;

    private JSONArray jsonArray;

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

        System.out.println("catalog11======="+catalog);

        if(catalog!=null){
            searchCatalog="&catalog="+catalog;
        }

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_second_storagelist, null);

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

                search="&name="+queryText;
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

        spinner=view.findViewById(R.id.diff);

        String[] m={"","总量与库存和销售相等","总量与库存和销售不相等", "库存与实际库存不相等"};

        ArrayAdapter<String> adapter;

        //将可选内容与ArrayAdapter连接起来
        adapter = new ArrayAdapter<String>(StorageListActivity.this,android.R.layout.simple_spinner_item,m);

        //设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //将adapter 添加到spinner中
        spinner.setAdapter(adapter);
        spinner.setSelection(0, true);
        //添加事件Spinner事件监听
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener());

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

    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {

            diff=arg2;

            mSearchView.clearFocus();
            mPullRefreshLayout.finishRefresh();
            page=1;
            total=0;
            refurbish=false;
            initData();

            System.out.println("diff="+diff);

        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    /* 利用反射机制调用MenuBuilder的setOptionalIconsVisible方法设置mOptionalIconsVisible为true，给菜单设置图标时才可见
     * 让菜单同时显示图标和文字
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "更新实库").setIcon(android.R.drawable.ic_lock_lock);
        // setIcon()方法为菜单设置图标，这里使用的是系统自带的图标，同学们留意一下,以
        // android.R开头的资源是系统提供的，我们自己提供的资源是以R开头的
        menu.add(Menu.NONE, Menu.FIRST + 2, 2, "一键对库").setIcon(android.R.drawable.ic_lock_power_off);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        System.out.println("click");

        selectMap=qdListSectionAdapter.getSelectMap();

        LayoutInflater inflater = getLayoutInflater();
        View validateView = inflater.inflate(
                R.layout.dialog_validate, null);
        final LinearLayout layout_validate = (LinearLayout) validateView.findViewById(R.id.layout_validate);
        layout_validate.removeAllViews();
        final List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();

        int i=0;

        switch (item.getItemId()) {
            case Menu.FIRST + 1:

                for(Map.Entry<Integer,org.json.JSONObject> sMap:selectMap.entrySet()) {
                    Map<String,Object> map = new HashMap<String, Object>();
                    View validateItem = inflater.inflate(R.layout.item_validate_storage, null);
                    validateItem.setTag(i);
                    layout_validate.addView(validateItem);
                    TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                    EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                    et_validate.setFocusable(false);
                    et_validate.setFocusableInTouchMode(false);
                    EditText et_validate2 = (EditText) validateItem.findViewById(R.id.et_validate2);
                    JSONObject jsonObject=sMap.getValue();
                    MyTestUtil.print(sMap);
                    try {
                        tv_validateName.setText(jsonObject.getString("name"));
                        et_validate.setText(jsonObject.getString("storage"));
                        et_validate2.setText(jsonObject.getString("realStorage"));

                        map.put("id",jsonObject.get("id"));
                        map.put("name", tv_validateName);
                        map.put("value", et_validate);
                        map.put("value2", et_validate2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    list.add(map);

                    i++;
                }


                AlertDialog dialog = new AlertDialog.Builder(this).setTitle("更新实库")
                        .setView(validateView)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                StringBuffer stringBuffer = new StringBuffer();
                                JSONArray jsonArray=new JSONArray();
                                int amount=0;
                                for(int i=0;i<list.size();i++){
                                    Serializable id= (Serializable) list.get(i).get("id");
                                    if(Long.valueOf(id.toString())!=-1) {
                                        String name = ((TextView) list.get(i).get("name")).getText().toString();
                                        String storage = ((EditText) list.get(i).get("value")).getText().toString();
                                        String realStorage = ((EditText) list.get(i).get("value2")).getText().toString();

                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            jsonObject.put("id",id);
                                            jsonObject.put("name",name);
                                            jsonObject.put("storage",Integer.valueOf(storage));
                                            jsonObject.put("realStorage",Integer.valueOf(realStorage));

                                            jsonArray.put(jsonObject);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    Map map=new HashMap();
                                    map.put("jsonArray",jsonArray);


                                    new FetchItemsTaskAddStorage().execute(map);
                                }

                                System.out.println(stringBuffer);

                                dialog.dismiss();
                            }

                        }).setNegativeButton("取消", new DialogInterface.OnClickListener()
                        {

                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();

                break;
            case Menu.FIRST + 2:

                for(Map.Entry<Integer,org.json.JSONObject> sMap:selectMap.entrySet()) {
                    Map<String,Object> map = new HashMap<String, Object>();
                    View validateItem = inflater.inflate(R.layout.item_validate_storage, null);
                    validateItem.setTag(i);
                    layout_validate.addView(validateItem);
                    TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                    EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                    et_validate.setFocusable(false);
                    et_validate.setFocusableInTouchMode(false);
                    EditText et_validate2 = (EditText) validateItem.findViewById(R.id.et_validate2);
                    et_validate2.setFocusable(false);
                    et_validate2.setFocusableInTouchMode(false);
                    JSONObject jsonObject=sMap.getValue();
                    MyTestUtil.print(sMap);
                    try {
                        tv_validateName.setText(jsonObject.getString("name"));
                        et_validate.setText(jsonObject.getString("storage"));
                        et_validate2.setText(jsonObject.getString("realStorage"));

                        map.put("id",jsonObject.get("id"));
                        map.put("name", tv_validateName);
                        map.put("value", et_validate);
                        map.put("value2", et_validate2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    list.add(map);

                    i++;
                }


                dialog = new AlertDialog.Builder(this).setTitle("一键对库")
                        .setView(validateView)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                StringBuffer stringBuffer = new StringBuffer();
                                JSONArray jsonArray=new JSONArray();
                                int amount=0;

                                Map map=new HashMap();

                                for(int i=0;i<list.size();i++){
                                    Serializable id= (Serializable) list.get(i).get("id");
                                    if(Long.valueOf(id.toString())!=-1) {
                                        String name = ((TextView) list.get(i).get("name")).getText().toString();
                                        String storage = ((EditText) list.get(i).get("value")).getText().toString();
                                        String realStorage = ((EditText) list.get(i).get("value2")).getText().toString();

                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            jsonObject.put("id",id);
                                            jsonObject.put("name",name);
                                            jsonObject.put("storage",Integer.valueOf(storage));
                                            jsonObject.put("realStorage",Integer.valueOf(realStorage));

                                            jsonArray.put(jsonObject);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    map.put("jsonArray",jsonArray);

                                }

                                new FetchItemsTaskCheckStorage().execute(map);

                                System.out.println(stringBuffer);

                                dialog.dismiss();
                            }

                        }).setNegativeButton("取消", new DialogInterface.OnClickListener()
                        {

                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();

                break;
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
                        diff=null;
                        refurbish=true;
                        spinner.setSelection(0);
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
                                if(search!=null&&!search.equals(""))
                                    cSearch+=search;
                                if(diff!=null)
                                    cSearch+="&diff="+diff;
                                if(searchCatalog!=null&&!searchCatalog.equals(""))
                                    cSearch+=searchCatalog;
                                String json = new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/storageLog/storageLog//getStorage?column=storage&order=asc&pageNo=" + page + "&pageSize="+size+cSearch);

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
                //Toast.makeText(getApplicationContext(), "click item " + position, Toast.LENGTH_SHORT).show();
                viewHolder=holder;
                if(position!=0) {


                        JSONObject jsonObject = null;

                        final int n=holder.getAdapterPosition();

                        currentPostion=n;

                        jsonObject=itemMap.get(n);
                        final JSONObject finalJsonObject = jsonObject;

                        MyTestUtil.print(finalJsonObject);

                        LayoutInflater inflater = getLayoutInflater();
                        View validateView = inflater.inflate(
                                R.layout.dialog_validate, null);
                        final LinearLayout layout_validate = (LinearLayout) validateView.findViewById(R.id.layout_validate);
                        layout_validate.removeAllViews();
                        final List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();



                            Map<String,Object> map = new HashMap<String, Object>();
                            View validateItem = inflater.inflate(R.layout.item_validate_storage, null);
                            validateItem.setTag(1);
                            layout_validate.addView(validateItem);
                            TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                            EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                            EditText et_validate2 = (EditText) validateItem.findViewById(R.id.et_validate2);

                            try {
                                tv_validateName.setText(jsonObject.getString("name"));
                                et_validate.setText(jsonObject.getString("storage"));
                                et_validate.setFocusable(false);
                                et_validate.setFocusableInTouchMode(false);
                                et_validate2.setText(jsonObject.getString("real_storage"));

                                map.put("id",jsonObject.get("id"));
                                map.put("name", tv_validateName);
                                map.put("value", et_validate);
                                map.put("value2", et_validate2);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            list.add(map);


                        AlertDialog dialog = new AlertDialog.Builder(StorageListActivity.this).setTitle("更新实库")
                                .setView(validateView)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        StringBuffer stringBuffer = new StringBuffer();
                                        JSONArray jsonArray=new JSONArray();
                                        int amount=0;
                                        for(int i=0;i<list.size();i++){
                                            long id=0;
                                            try {
                                                id= (long) list.get(i).get("id");
                                            }catch (Exception e){
                                                Serializable s= (Serializable) list.get(i).get("id");
                                                id=Long.valueOf(s.toString());
                                            }
                                            if(id!=-1) {
                                                String name = ((TextView) list.get(i).get("name")).getText().toString();
                                                String storage = ((EditText) list.get(i).get("value")).getText().toString();
                                                String realStorage = ((EditText) list.get(i).get("value2")).getText().toString();

                                                JSONObject jsonObject = new JSONObject();

                                                Integer storage1=0;
                                                Integer realStorag1=0;

                                                if(storage!=null&&!storage.equals("null"))
                                                    storage1=Integer.valueOf(storage);

                                                if(realStorage!=null&&!realStorage.equals("null"))
                                                    realStorag1=Integer.valueOf(realStorage);

                                                try {
                                                    jsonObject.put("id",id);
                                                    jsonObject.put("name",name);
                                                    jsonObject.put("storage",storage1);
                                                    jsonObject.put("realStorage",realStorag1);

                                                    jsonArray.put(jsonObject);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            System.out.println("id============="+id);
                                            MyTestUtil.print(jsonArray);
                                            Map map=new HashMap();
                                            map.put("jsonArray",jsonArray);


                                             new FetchItemsTaskAddStorage().execute(map);
                                        }

                                        System.out.println(stringBuffer);

                                        dialog.dismiss();
                                    }

                                }).setNegativeButton("取消", new DialogInterface.OnClickListener()
                                {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                    }
                                }).create();
                        dialog.show();




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

            mSearchView.clearFocus();
            mPullRefreshLayout.finishRefresh();
            itemMap=new HashMap<>();
            selectMap=new HashMap<>();
            page=1;
            total=0;
            search="";
            initData();

        }

    }

    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            String aSearch="";
            String cSearch="";

            if(search!=null&&!search.equals("")) {
                cSearch += search;
                aSearch += search2;
            }

            if(diff!=null) {
                cSearch += "&diff=" + diff;
                aSearch += "&diff=" + diff;
            }

            if(searchCatalog!=null&&!searchCatalog.equals("")) {
                cSearch += searchCatalog;
                aSearch+=searchCatalog;
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

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/storageLog/storageLog/getStorage?column=storage&order=asc&pageNo=" + page + "&pageSize="+size+cSearch);
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

    private ArrayList<SectionItem> addContents(ArrayList<SectionItem> contents, JSONObject jsonObject) throws JSONException {

        JSONObject result= (JSONObject) jsonObject.get("result");

        JSONArray records = result.getJSONArray("records");

        try {
            total = result.getInt("count");
        }catch (Exception e){

        }

        for (int i = 0; i < records.length(); i++) {

            int order=i+1+(page-1)*10;

            JSONObject jsonObject1 = (JSONObject) records.get(i);

            itemMap.put(order,jsonObject1);

            JSONObject jsonObject2=new JSONObject();

            try {
                long id=jsonObject1.getInt("id");
                jsonObject2.put("id",id);
            }catch (Exception e){

            }

            jsonObject2.put("0",order);

            String name ="";
            try {
                name=jsonObject1.getString("name");
                jsonObject2.put("name",name);
            }catch (Exception e){

            }
            Integer stockstorage =0;
            try{
                stockstorage= jsonObject1.getInt("stockstorage");
            }catch (Exception e){
               // e.printStackTrace();
            }
            jsonObject2.put("3","总数："+stockstorage);
            Integer storage=0;
            try{
                storage= jsonObject1.getInt("storage");

            }catch (Exception e){

            }
            jsonObject2.put("2","库存："+storage);
            Double cost =0.0;
         /*   if(isCost) {
                try {
                    cost = jsonObject1.getDouble("ALLCOST");
                    jsonArray.put("总成本:" + cost);
                } catch (Exception e) {

                }
            }*/

            jsonObject2.put("storage",storage);

            Integer accountcount=0;
            try {
                accountcount=jsonObject1.getInt("accountcount");

            }catch (Exception e){
              //  e.printStackTrace();
            }

            Integer realStorage=0;
            try{
                realStorage=jsonObject1.getInt("real_storage");

            }catch (Exception e){

            }
            jsonObject2.put("4","实库"+realStorage);
            jsonObject2.put("realStorage",realStorage);
            String img="";

            int diff=0;
            diff=stockstorage-storage-accountcount;
            jsonObject2.put("diff",diff);
            int diff2=0;
            diff2=storage-realStorage;
            jsonObject2.put("diff2",diff2);

            jsonObject2.put("type",1);
            System.out.println("name=="+name+"       stroge=="+storage+"             realstorge==="+realStorage+"   diff=="+diff);
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

        String name ="";
        try {
            name=jsonObject1.getString("name");
            jsonObject2.put("name",name);
        }catch (Exception e){

        }
        Integer stockstorage =0;
        try{
            stockstorage= jsonObject1.getInt("stockstorage");
        }catch (Exception e){
            // e.printStackTrace();
        }
        jsonObject2.put("3","总数："+stockstorage);
        Integer storage=0;
        try{
            storage= jsonObject1.getInt("storage");

        }catch (Exception e){

        }
        jsonObject2.put("2","库存："+storage);
        Double cost =0.0;
         /*   if(isCost) {
                try {
                    cost = jsonObject1.getDouble("ALLCOST");
                    jsonArray.put("总成本:" + cost);
                } catch (Exception e) {

                }
            }*/

        jsonObject2.put("storage",storage);

        Integer accountcount=0;
        try {
            accountcount=jsonObject1.getInt("accountcount");

        }catch (Exception e){
            //  e.printStackTrace();
        }

        Integer realStorage=0;
        try{
            realStorage=jsonObject1.getInt("real_storage");

        }catch (Exception e){

        }
        jsonObject2.put("4","实库"+realStorage);
        jsonObject2.put("realStorage",realStorage);
        String img="";

        int diff=0;
        diff=stockstorage-storage-accountcount;
        jsonObject2.put("diff",diff);
        int diff2=0;
        diff2=storage-realStorage;
        jsonObject2.put("diff2",diff2);

        jsonObject2.put("type",1);

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

    private class FetchItemsTaskAddStorage extends AsyncTask<Map,Void,String> {

        @Override
        protected String doInBackground(Map... params) {

            Map map=params[0];

            jsonArray= (JSONArray) map.get("jsonArray");

            MyTestUtil.print(map);

            String items= Uri.encode(jsonArray.toString());

            return new OKHttpFetch(getApplication()).get(FlickrFetch.base+"/storageLog/storageLog/upReal?items="+items);
        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    JSONObject jsonObject=new JSONObject(items);


                    String success = jsonObject.getString("success");
                    String message = jsonObject.getString("message");

                    System.out.println("success.equals(true)==="+success.equals("true"));
                    System.out.println("message.equals(\"添加库存成功!\")==="+message.equals("更新实库成功!"));

                    MyTestUtil.print(jsonArray);

                    if(success.equals("true")) {

                            selectMap=qdListSectionAdapter.getSelectMap();


                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject1= (JSONObject) jsonArray.get(i);

                                long id=0;
                                try {
                                    id=(long) jsonObject1.get("id");
                                }catch (Exception e){
                                    String s=jsonObject1.getString("id");
                                    id=Long.valueOf(s);
                                }

                                MyTestUtil.print(itemMap);

                                for(Integer key:itemMap.keySet()){

                                    JSONObject jsonObject2=itemMap.get(key);

                                    long id2=0;
                                    try {
                                        id2=(long) jsonObject2.get("id");
                                    }catch (Exception e){
                                        String s=jsonObject2.getString("id");
                                        id2=Long.valueOf(s);
                                    }

                                    int order=key;

                                    if(id==id2){
                                        System.out.println("order==="+order);
                                        int storage=jsonObject1.getInt("storage");
                                        jsonObject2.put("storage", storage);
                                        int realStorage=jsonObject1.getInt("realStorage");
                                        jsonObject2.put("real_storage",realStorage);

                                        MyTestUtil.print(jsonObject1);
                                        MyTestUtil.print(jsonObject2);

                                        itemMap.put(order,jsonObject2);

                                        continue;
                                    }

                                }

                                MyTestUtil.print(itemMap);

                            }

                            new FetchItemsUpdate().execute();


                    }

                    Toast.makeText(getApplication(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplication(),"添加库存失败",Toast.LENGTH_SHORT).show();
                }

            }

        }

    }

    private class FetchItemsTaskCheckStorage extends AsyncTask<Map,Void,String> {

        @Override
        protected String doInBackground(Map... params) {

            Map map=params[0];

            jsonArray= (JSONArray) map.get("jsonArray");

            String items= Uri.encode(jsonArray.toString());

            return new OKHttpFetch(getApplication()).get(FlickrFetch.base+"/storageLog/storageLog/check?items="+items);
        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    JSONObject jsonObject=new JSONObject(items);


                    String success = jsonObject.getString("success");
                    String message = jsonObject.getString("message");

                    System.out.println("success.equals(true)==="+success.equals("true"));
                    System.out.println("message.equals(\"添加库存成功!\")==="+message.equals("更新实库成功!"));

                    MyTestUtil.print(jsonArray);

                    if(success.equals("true")) {

                        selectMap=qdListSectionAdapter.getSelectMap();


                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject1= (JSONObject) jsonArray.get(i);

                            Serializable id= (Serializable) jsonObject1.get("id");

                            MyTestUtil.print(itemMap);

                            for(Integer key:itemMap.keySet()){

                                JSONObject jsonObject2=itemMap.get(key);

                                Serializable id2= (Serializable) jsonObject2.get("id");

                                int order=key;

                                if(id==id2){
                                    System.out.println("order==="+order);
                                    int realStorage=jsonObject1.getInt("realStorage");
                                    jsonObject2.put("storage", realStorage);
                                    jsonObject2.put("real_storage",realStorage);

                                    MyTestUtil.print(jsonObject1);
                                    MyTestUtil.print(jsonObject2);

                                    itemMap.put(order,jsonObject2);

                                    continue;
                                }

                            }

                            MyTestUtil.print(itemMap);

                        }

                        new FetchItemsUpdate().execute();


                    }

                    Toast.makeText(getApplication(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplication(),"添加库存失败",Toast.LENGTH_SHORT).show();
                }

            }

        }

    }

}