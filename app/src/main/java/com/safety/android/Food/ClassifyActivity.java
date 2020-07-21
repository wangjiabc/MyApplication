package com.safety.android.Food;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.lang.reflect.Method;
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

public class ClassifyActivity extends AppCompatActivity {

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

    QDListSectionAdapter qdListSectionAdapter;

    private SwipeBackController swipeBackController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.class_compages_list, null);

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

                LayoutInflater inflater = getLayoutInflater();
                View validateView = inflater.inflate(
                        R.layout.dialog_validate, null);
                final LinearLayout layout_validate = (LinearLayout) validateView.findViewById(R.id.layout_validate);
                layout_validate.removeAllViews();
                final List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();


                Map<String,Object> map = new HashMap<String, Object>();
                View validateItem = inflater.inflate(R.layout.item_validate_enter2, null);
                validateItem.setTag(0);
                layout_validate.addView(validateItem);
                TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                TextView et_validateText=validateItem.findViewById(R.id.et_validate_text);
                et_validateText.setText("");

                tv_validateName.setText("目录名称");

                map.put("name", tv_validateName);
                map.put("value", et_validate);

                list.add(map);


                AlertDialog dialog = new AlertDialog.Builder(ClassifyActivity.this).setTitle("新建目录")
                        .setView(validateView)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                StringBuffer stringBuffer = new StringBuffer();

                                MyTestUtil.print(list);

                                Map map=list.get(0);

                                String username = ((EditText) list.get(0).get("value")).getText().toString();

                                map.put("value",username);

                                new FetchItemsTaskAdd().execute(map);

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
                        initData();

                    }
                }, 1000);
            }
        });
    }

    protected QMUIStickySectionAdapter<
            SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> createAdapter() {
        qdListSectionAdapter=new QDListSectionAdapter(3);
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
                                String cSearch="";
                                if(search!=null&&!search.equals(""))
                                    cSearch=search;
                                String json = new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/tree/tree/list?order=asc&pageNo=" + page + "&pageSize="+size+cSearch);

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
                    try {

                        JSONObject jsonObject = null;

                        final int n=holder.getAdapterPosition();

                        jsonObject = selectMap.get(holder.getAdapterPosition());
                        String buttonText;
                        if (jsonObject == null) {
                            jsonObject = itemMap.get(holder.getAdapterPosition());
                        }

                        final JSONObject finalJsonObject = jsonObject;

                        new AlertDialog.Builder(ClassifyActivity.this)
                                .setTitle(finalJsonObject.getString("username"))
                                .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        AlertDialog builder;

                                        try {
                                            builder = new AlertDialog.Builder(ClassifyActivity.this)
                                                    .setTitle("删除分类"+ finalJsonObject.getString("username"))
                                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    })
                                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                            try {
                                                                int id=finalJsonObject.getInt("id");
                                                                new FetchItemsTaskDel().execute(id);
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }

                                                            dialogInterface.dismiss();
                                                        }
                                                    })
                                                    .show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        dialogInterface.dismiss();
                                    }
                                })
                                .setPositiveButton("添加子目录", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        dialogInterface.dismiss();

                                        LayoutInflater inflater = getLayoutInflater();
                                        View validateView = inflater.inflate(
                                                R.layout.dialog_validate, null);
                                        final LinearLayout layout_validate = (LinearLayout) validateView.findViewById(R.id.layout_validate);
                                        layout_validate.removeAllViews();
                                        final List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();


                                        Map<String,Object> map = new HashMap<String, Object>();
                                        View validateItem = inflater.inflate(R.layout.item_validate_enter, null);
                                        validateItem.setTag(0);
                                        layout_validate.addView(validateItem);
                                        TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                                        EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                                        TextView et_validateText=validateItem.findViewById(R.id.et_validate_text);
                                        et_validateText.setVisibility(View.GONE);

                                        tv_validateName.setText("目录名称");

                                        map.put("name", tv_validateName);
                                        map.put("value", et_validate);

                                        list.add(map);


                                        AlertDialog dialog = new AlertDialog.Builder(ClassifyActivity.this).setTitle("新建目录")
                                                .setView(validateView)
                                                .setPositiveButton("确定", new DialogInterface.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which)
                                                    {
                                                        StringBuffer stringBuffer = new StringBuffer();

                                                        MyTestUtil.print(list);

                                                        Map map=new HashMap();

                                                        String username = ((EditText) list.get(0).get("value")).getText().toString();

                                                        map.put("username",username);

                                                        try {
                                                            map.put("PId",finalJsonObject.get("id"));
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }

                                                        new FetchItemsTaskAddPId().execute(map);

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
                                })
                                .create()
                                .show();



                    } catch (ClassCastException | JSONException e) {
                        e.printStackTrace();

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

    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            String cSearch="";
            if(search!=null&&!search.equals(""))
                cSearch=search;
            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/tree/tree/list?order=asc&pageNo=" + page + "&pageSize"+size+"&combination=0"+cSearch);
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

        Map pName=new HashMap();

        for (int i = 0; i < records.length(); i++) {

            int order=i+1+(page-1)*10;

            JSONObject jsonObject1 = (JSONObject) records.get(i);

            int id=jsonObject1.getInt("id");

            String name=jsonObject1.getString("username");

            pName.put(id,name);

            try{
                int pId=jsonObject1.getInt("pid");
                name= (String) pName.get(pId);
                jsonObject1.put("pName",name);
            }catch (Exception e){
                e.printStackTrace();
            }

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

        String name = jsonObject1.getString("username");
        jsonObject2.put("name",name);

        try{
            String pName=jsonObject1.getString("pName");
            jsonObject2.put("2","上级目录:");
            jsonObject2.put("4",pName);
        }catch (Exception e){

        }

        return jsonObject2;

    }


    private class FetchItemsTaskAdd extends AsyncTask<Map,Void,String> {

        @Override
        protected String doInBackground(Map... params) {

            Map map=params[0];

            String username= (String) map.get("value");


            return new OKHttpFetch(getApplication()).get(FlickrFetch.base+"/tree/tree/addRootTree?userName="+username);
        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    JSONObject jsonObject=new JSONObject(items);


                    String success = jsonObject.getString("success");
                    String message = jsonObject.getString("message");


                    if(success.equals("true")) {


                        mSearchView.clearFocus();
                        mPullRefreshLayout.finishRefresh();
                        itemMap=new HashMap<>();
                        selectMap=new HashMap<>();
                        page=1;
                        total=0;
                        search="";
                        initData();

                    }

                    Toast.makeText(getApplication(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplication(),"失败",Toast.LENGTH_SHORT).show();
                }

            }

        }

    }


    private class FetchItemsTaskAddPId extends AsyncTask<Map,Void,String> {

        @Override
        protected String doInBackground(Map... params) {

            Map map=params[0];

            String username= (String) map.get("username");

            Integer pId= (Integer) map.get("PId");

            return new OKHttpFetch(getApplication()).get(FlickrFetch.base+"/tree/tree/addTree?userName="+username+"&pId="+pId);
        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    JSONObject jsonObject=new JSONObject(items);


                    String success = jsonObject.getString("success");
                    String message = jsonObject.getString("message");


                    if(success.equals("true")) {


                        mSearchView.clearFocus();
                        mPullRefreshLayout.finishRefresh();
                        itemMap=new HashMap<>();
                        selectMap=new HashMap<>();
                        page=1;
                        total=0;
                        search="";
                        initData();

                    }

                    Toast.makeText(getApplication(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplication(),"失败",Toast.LENGTH_SHORT).show();
                }

            }

        }

    }



    private class FetchItemsTaskDel extends AsyncTask<Integer,Void,String> {

        @Override
        protected String doInBackground(Integer... params) {


            Integer id= params[0];

            return new OKHttpFetch(getApplication()).get(FlickrFetch.base+"/tree/tree/delChildTree?id="+id);
        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    JSONObject jsonObject=new JSONObject(items);


                    String success = jsonObject.getString("success");

                    if(success.equals("true")) {


                        mSearchView.clearFocus();
                        mPullRefreshLayout.finishRefresh();
                        itemMap=new HashMap<>();
                        selectMap=new HashMap<>();
                        page=1;
                        total=0;
                        search="";
                        initData();

                    }

                    Toast.makeText(getApplication(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplication(),"失败",Toast.LENGTH_SHORT).show();
                }

            }



        }

    }


}
