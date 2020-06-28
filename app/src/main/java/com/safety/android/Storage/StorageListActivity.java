package com.safety.android.Storage;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.safety.android.Sale.SaleActivity;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.qmuidemo.view.QDListSectionAdapter;
import com.safety.android.qmuidemo.view.SectionHeader;
import com.safety.android.qmuidemo.view.SectionItem;
import com.safety.android.tools.MyTestUtil;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.storage_list_item, null);

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
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "一键对库").setIcon(android.R.drawable.ic_lock_lock);
        // setIcon()方法为菜单设置图标，这里使用的是系统自带的图标，同学们留意一下,以
        // android.R开头的资源是系统提供的，我们自己提供的资源是以R开头的
        menu.add(Menu.NONE, Menu.FIRST + 2, 2, "退出").setIcon(android.R.drawable.ic_lock_power_off);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        System.out.println("click");
        switch (item.getItemId()) {
            case Menu.FIRST + 1:
                selectMap=qdListSectionAdapter.getSelectMap();

                LayoutInflater inflater = getLayoutInflater();
                View validateView = inflater.inflate(
                        R.layout.dialog_validate, null);
                final LinearLayout layout_validate = (LinearLayout) validateView.findViewById(R.id.layout_validate);
                layout_validate.removeAllViews();
                final List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();

                int i=0;
                for(Map.Entry<Integer,org.json.JSONObject> sMap:selectMap.entrySet()) {
                    Map<String,Object> map = new HashMap<String, Object>();
                    View validateItem = inflater.inflate(R.layout.item_validate_storage, null);
                    validateItem.setTag(i);
                    layout_validate.addView(validateItem);
                    TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                    EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                    EditText et_validate2 = (EditText) validateItem.findViewById(R.id.et_validate2);
                    JSONObject jsonObject=sMap.getValue();
                    MyTestUtil.print(sMap);
                    try {
                        tv_validateName.setText(jsonObject.getString("name"));
                        et_validate.setText(jsonObject.getString("storage"));
                        et_validate2.setText(jsonObject.getString("realStorage"));

                        map.put("id",jsonObject.getInt("id"));
                        map.put("name", tv_validateName);
                        map.put("value", et_validate);
                        map.put("value2", et_validate2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    list.add(map);

                    i++;
                }


                AlertDialog dialog = new AlertDialog.Builder(this).setTitle("一键对库存")
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
                                    int id= (int) list.get(i).get("id");
                                    if(id!=-1) {
                                        String name = ((TextView) list.get(i).get("name")).getText().toString();
                                        String cost = ((EditText) list.get(i).get("value")).getText().toString();
                                        int combination = (int) list.get(i).get("combination");
                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            jsonObject.put("id",id);
                                            jsonObject.put("name",name);
                                            jsonObject.put("cost",cost);
                                            jsonObject.put("combination",combination);
                                            jsonArray.put(jsonObject);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }else{
                                        String amountString= ((EditText) list.get(i).get("value")).getText().toString();
                                        amount=Integer.parseInt(amountString);
                                    }
                                    Map map=new HashMap();
                                    map.put("jsonArray",jsonArray);
                                    map.put("amount",amount);

                                   // addCount=list.size();

                                   // new FoodListActivity.FetchItemsTaskAddStorage().execute(map);
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
                                et_validate2.setText(jsonObject.getString("real_storage"));

                                map.put("id",jsonObject.getInt("id"));
                                map.put("name", tv_validateName);
                                map.put("value", et_validate);
                                map.put("value2", et_validate2);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            list.add(map);


                        AlertDialog dialog = new AlertDialog.Builder(StorageListActivity.this).setTitle("对库存")
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
                                            int id= (int) list.get(i).get("id");
                                            if(id!=-1) {
                                                String name = ((TextView) list.get(i).get("name")).getText().toString();
                                                String cost = ((EditText) list.get(i).get("value")).getText().toString();
                                                int combination = (int) list.get(i).get("combination");
                                                JSONObject jsonObject = new JSONObject();
                                                try {
                                                    jsonObject.put("id",id);
                                                    jsonObject.put("name",name);
                                                    jsonObject.put("cost",cost);
                                                    jsonObject.put("combination",combination);
                                                    jsonArray.put(jsonObject);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }else{
                                                String amountString= ((EditText) list.get(i).get("value")).getText().toString();
                                                amount=Integer.parseInt(amountString);
                                            }
                                            Map map=new HashMap();
                                            map.put("jsonArray",jsonArray);
                                            map.put("amount",amount);

                                            // addCount=list.size();

                                            // new FoodListActivity.FetchItemsTaskAddStorage().execute(map);
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
                Toast.makeText(getApplicationContext(), "long click item " + position, Toast.LENGTH_SHORT).show();
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

        total=result.getInt("count");

        for (int i = 0; i < records.length(); i++) {

            int order=i+1+(page-1)*10;

            JSONObject jsonObject1 = (JSONObject) records.get(i);

            itemMap.put(order,jsonObject1);

            JSONObject jsonObject2=new JSONObject();

            try {
                int id=jsonObject1.getInt("id");
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
                jsonObject2.put("3","总数："+stockstorage);
            }catch (Exception e){

            }
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
            contents.add(new SectionItem(jsonObject2.toString()));
        }


        return contents;
    }






}