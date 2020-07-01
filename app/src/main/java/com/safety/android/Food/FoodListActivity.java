package com.safety.android.Food;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.qmuiteam.qmui.widget.section.QMUISection;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionLayout;
import com.safety.android.MainActivity;
import com.safety.android.SQLite3.PermissionInfo;
import com.safety.android.SQLite3.PermissionLab;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.qmuidemo.view.HtmlImageGetter;
import com.safety.android.qmuidemo.view.QDListSectionAdapter;
import com.safety.android.qmuidemo.view.SectionHeader;
import com.safety.android.qmuidemo.view.SectionItem;
import com.safety.android.qmuidemo.view.getGradientDrawable;
import com.safety.android.tools.MyTestUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

public class FoodListActivity extends AppCompatActivity {

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

    private String searchCatalog="";

    private Map<Integer,JSONObject> itemMap=new HashMap<>();

    private Map<Integer,JSONObject> selectMap=new HashMap();

    private QMUIStickySectionAdapter.ViewHolder viewHolder;

    private boolean isCost=false;

    private double allCost=0;

    private int currentPostion;
    private int addStorage;
    private int addCount;

    Integer catalog = null;

    private FrameLayout simple_date;

    QDListSectionAdapter qdListSectionAdapter;

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

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.simple_list_item, null);

        simple_date=view.findViewById(R.id.simple_date);
        simple_date.setVisibility(View.GONE);

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
    public boolean onCreateOptionsMenu(Menu menu) {

       List<PermissionInfo> list= PermissionLab.get(getApplicationContext()).getPermissionInfo();

        Iterator<PermissionInfo> iterator=list.iterator();

        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.getItem(0).setVisible(false);

        boolean storageAdd=false;
        boolean compages=false;
        boolean delete=false;

        while (iterator.hasNext()){

            PermissionInfo permissionInfo=iterator.next();

            String action=permissionInfo.getAction();
            String component=permissionInfo.getComponent();

            if(action!=null){
                if(action.equals("material:add")){
                    menu.getItem(0).setVisible(true);
                }
                if(action.equals("material:storageAdd")){
                    storageAdd=true;

                }
                if(action.equals("material:compages")){
                    compages=true;

                }
                if(action.equals("material:delete")){
                    delete=true;

                }
            }

        }

        if(storageAdd)
            menu.add(Menu.NONE, Menu.FIRST + 1, 1, "添加库存").setIcon(android.R.drawable.ic_input_add);

        if(compages)
            menu.add(Menu.NONE, Menu.FIRST + 2, 2, "组合商品").setIcon(android.R.drawable.ic_menu_manage);

        if(delete)
            menu.add(Menu.NONE, Menu.FIRST + 3, 3, "删除商品").setIcon(android.R.drawable.ic_delete);

        menu.add(Menu.NONE,Menu.FIRST+4,4,"添加商品到分类").setIcon(android.R.drawable.ic_menu_add);

        menu.add(Menu.NONE,Menu.FIRST+5,5,"从分类删除商品").setIcon(android.R.drawable.ic_menu_delete);

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
            case Menu.FIRST + 1:
                LayoutInflater inflater = getLayoutInflater();
                View validateView = inflater.inflate(
                        R.layout.dialog_validate, null);
                final LinearLayout layout_validate = (LinearLayout) validateView.findViewById(R.id.layout_validate);
                layout_validate.removeAllViews();
                final List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();

                int i=0;
                selectMap=qdListSectionAdapter.getSelectMap();
                for(Map.Entry<Integer,org.json.JSONObject> sMap:selectMap.entrySet()) {
                    Map<String,Object> map = new HashMap<String, Object>();
                    View validateItem = inflater.inflate(R.layout.item_validate_enter, null);
                    validateItem.setTag(i);
                    layout_validate.addView(validateItem);
                    TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                    EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                    TextView et_validateText=validateItem.findViewById(R.id.et_validate_text);
                    JSONObject jsonObject=sMap.getValue();
                    try {
                        tv_validateName.setText(jsonObject.getString("name"));
                        et_validate.setText(jsonObject.getString("cost"));
                        if(!isCost) {
                            et_validateText.setVisibility(View.GONE);
                            et_validate.setVisibility(View.GONE);
                        }
                        map.put("id",jsonObject.getInt("id"));
                        map.put("name", tv_validateName);
                        map.put("value", et_validate);
                        map.put("combination",jsonObject.getInt("combination"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    list.add(map);

                    i++;
                }

                View validateItem = inflater.inflate(R.layout.item_validate_enter, null);
                validateItem.setTag(i);
                layout_validate.addView(validateItem);
                TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                TextView et_validateText=validateItem.findViewById(R.id.et_validate_text);

                tv_validateName.setText("进货数量");
                et_validateText.setText("");
                et_validate.setText("1");

                Map<String,Object> map0 = new HashMap<String, Object>();
                map0.put("id",-1);
                map0.put("name", tv_validateName);
                map0.put("value", et_validate);

                list.add(map0);

                AlertDialog dialog = new AlertDialog.Builder(this).setTitle("批量添加库存")
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

                                    addCount=list.size();

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
                JSONArray jsonArray=new JSONArray();
                selectMap=qdListSectionAdapter.getSelectMap();
                System.out.println("selecmap==================");
                MyTestUtil.print(selectMap);
                for(Map.Entry<Integer,org.json.JSONObject> map:selectMap.entrySet()){
                    jsonArray.put(map.getValue());
                }
                JSONObject jsonObject=new JSONObject();
                try {
                    jsonObject.put("id","-1");
                    jsonObject.put("ids",jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent2 = new Intent(getApplicationContext(), FoodCompagesActivity.class);
                intent2.putExtra("jsonString", jsonObject.toString());
                startActivityForResult(intent2, 5);

                break;
            case Menu.FIRST + 3:

                String delName="";

                for(Map.Entry<Integer,org.json.JSONObject> sMap:selectMap.entrySet()) {
                    JSONObject json = sMap.getValue();
                    try {
                        delName+=json.getString("name")+",";
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                new AlertDialog.Builder(FoodListActivity.this)
                        .setTitle("删除"+delName.substring(0,delName.length()-1)+"?")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(FoodListActivity.this, "点击了取消按钮", Toast.LENGTH_SHORT).show();
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
            case Menu.FIRST + 4:
                Intent intent0 = new Intent(getApplicationContext(), FoodCatalogListActivity.class);
                intent0.putExtra("jsonString", String.valueOf(catalog));
                startActivityForResult(intent0, 5);
                break;
            case Menu.FIRST + 5:
                String delClassName="";

                for(Map.Entry<Integer,org.json.JSONObject> sMap:selectMap.entrySet()) {
                    JSONObject json = sMap.getValue();
                    try {
                        delClassName+=json.getString("name")+",";
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                new AlertDialog.Builder(FoodListActivity.this)
                        .setTitle("从当前分类中删除"+delClassName.substring(0,delClassName.length()-1)+"?")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(FoodListActivity.this, "点击了取消按钮", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                new FetchItemsUpdateTask().execute();

                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
                break;
            case R.id.menu_item_add:
                Intent intent = new Intent(getApplicationContext(), FoodDetailActivity.class);
                startActivityForResult(intent,1);
                //Toast.makeText(this, "添加被点击了", Toast.LENGTH_LONG).show();
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
        qdListSectionAdapter=new QDListSectionAdapter(1);
        return qdListSectionAdapter;
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

                        if(jsonObject.getInt("combination")==0) {
                            new AlertDialog.Builder(FoodListActivity.this)
                                    .setTitle(finalJsonObject.getString("name"))
                                    .setMessage("零售价:" + finalJsonObject.getDouble("retailprice"))
                                    .setNegativeButton("添加库存", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            dialogInterface.dismiss();

                                            LayoutInflater inflater = getLayoutInflater();
                                            View validateView = inflater.inflate(
                                                    R.layout.dialog_validate, null);
                                            final LinearLayout layout_validate = (LinearLayout) validateView.findViewById(R.id.layout_validate);
                                            layout_validate.removeAllViews();
                                            final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

                                            currentPostion = n;
                                            View validateItem = inflater.inflate(R.layout.item_validate_enter, null);
                                            validateItem.setTag(0);
                                            layout_validate.addView(validateItem);
                                            TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                                            EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                                            TextView et_validateText = validateItem.findViewById(R.id.et_validate_text);
                                            Map<String, Object> map = new HashMap<String, Object>();
                                            try {
                                                tv_validateName.setText(finalJsonObject.getString("name"));
                                                et_validate.setText(finalJsonObject.getString("cost"));
                                                if (!isCost) {
                                                    et_validateText.setVisibility(View.GONE);
                                                    et_validate.setVisibility(View.GONE);
                                                }
                                                map.put("id", finalJsonObject.getInt("id"));
                                                map.put("name", tv_validateName);
                                                map.put("value", et_validate);
                                                map.put("combination", finalJsonObject.getInt("combination"));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }


                                            list.add(map);

                                            View validateItem2 = inflater.inflate(R.layout.item_validate_enter, null);
                                            validateItem2.setTag(1);
                                            layout_validate.addView(validateItem2);
                                            TextView tv_validateName2 = (TextView) validateItem2.findViewById(R.id.tv_validateName);
                                            EditText et_validate2 = (EditText) validateItem2.findViewById(R.id.et_validate);
                                            TextView et_validateText2 = validateItem2.findViewById(R.id.et_validate_text);
                                            Map<String, Object> map2 = new HashMap<String, Object>();
                                            tv_validateName2.setText("数量");
                                            et_validateText2.setText("");
                                            et_validate2.setText("1");

                                            map2.put("name", tv_validateName2);
                                            map2.put("value", et_validate2);

                                            list.add(map2);

                                            AlertDialog dialog = new AlertDialog.Builder(FoodListActivity.this).setTitle("添加库存")
                                                    .setView(validateView)
                                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            JSONArray jsonArray = new JSONArray();
                                                            int amount = 0;
                                                            int id = (int) list.get(0).get("id");
                                                            String name = ((TextView) list.get(0).get("name")).getText().toString();
                                                            String cost = ((EditText) list.get(0).get("value")).getText().toString();
                                                            int combination = (int) list.get(0).get("combination");
                                                            JSONObject jsonObject = new JSONObject();
                                                            try {
                                                                jsonObject.put("id", id);
                                                                jsonObject.put("name", name);
                                                                jsonObject.put("cost", cost);
                                                                jsonObject.put("combination", combination);
                                                                jsonArray.put(jsonObject);
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }

                                                            String amountString = ((EditText) list.get(1).get("value")).getText().toString();
                                                            amount = Integer.parseInt(amountString);

                                                            addStorage = amount;
                                                            addCount = 1;
                                                            Map map = new HashMap();
                                                            map.put("jsonArray", jsonArray);
                                                            map.put("amount", amount);
                                                            new FetchItemsTaskAddStorage().execute(map);

                                                            dialog.dismiss();
                                                        }

                                                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    }).create();
                                            dialog.show();

                                        }
                                    })
                                    .setNeutralButton("编辑", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            final JSONObject json = itemMap.get(n);
                                            int combination = 0;
                                            try {
                                                json.put("position", position);
                                                combination = json.getInt("combination");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            if (combination == 0) {
                                                Intent intent = new Intent(getApplicationContext(), FoodDetailActivity.class);
                                                intent.putExtra("jsonString", json.toString());
                                                startActivityForResult(intent, 1);
                                            } else if (combination == 1) {
                                                Intent intent = new Intent(getApplicationContext(), FoodCompagesActivity.class);
                                                intent.putExtra("jsonString", json.toString());
                                                startActivityForResult(intent, 1);
                                            }
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .create()
                                    .show();
                        }else{

                            new FetchItemsDetail().execute(jsonObject);

                        }


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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("requestCode===="+requestCode+"         resultCode==="+resultCode);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }else if(requestCode==1){

            try {
                JSONObject jsonObject=new JSONObject(data.getStringExtra("value"));
                int type=jsonObject.getInt("type");
                System.out.println("type=========="+type);

                if(type==1) {
                    ArrayList<SectionItem> contents = new ArrayList<>();
                    jsonObject.put("order", String.valueOf(mAdapter.getItemCount()));
                    JSONObject jsonObject2=new JSONObject();

                    try {
                        int id=jsonObject.getInt("id");
                        jsonObject2.put("id",id);
                    }catch (Exception e){

                    }

                    //jsonObject2.put("0",order);

                    String name = jsonObject.getString("name");
                    jsonObject2.put("name",name);
                    Integer storage = jsonObject.getInt("storage");
                    jsonObject2.put("2",storage);
                    Double cost = 0.0;
                    try {
                        cost=jsonObject.getDouble("cost");
                    }catch (Exception e){
                        jsonObject.put("cost",0.00);
                    }
                    Double retailprice =0.00;
                    try {
                        retailprice=jsonObject.getDouble("retailprice");
                    }catch (Exception e){
                        jsonObject.put("retailprice",0.00);
                    }
                    jsonObject2.put("retailprice",retailprice);
                    String img=jsonObject.getString("img");
                    String costText="";
                    if(isCost) {
                        costText = "成本:" + cost;
                        jsonObject2.put("3",costText);
                    }
                    if(img!=null&&!img.equals("null")&&!img.equals("")) {
                        img = "http://qiniu.lzxlzc.com/compress/" + img;
                        jsonObject2.put("img",img);
                    }

                    contents.add(new SectionItem(jsonObject2.toString()));
                    boolean existMoreData = true;

                    if (total < (page * 10)) {
                        existMoreData = false;
                    }

                    mAdapter.finishLoadMore(mAdapter.getSection(mAdapter.getItemCount()), contents, true, existMoreData);
                }else {
                    int position=jsonObject.getInt("position");
                    jsonObject.put("order",position);

                    selectMap=qdListSectionAdapter.getSelectMap();

                    Map delMap=new HashMap();

                    for(Integer k:selectMap.keySet()){
                        JSONObject jsonObject1=selectMap.get(k);
                        try {

                            int id=jsonObject1.getInt("id");

                            delMap.put(id,id);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    int count=qdListSectionAdapter.getItemCount();

                    for(int i=0;i<count;i++){
                        SectionItem sectionItem=qdListSectionAdapter.getSectionItem(i);
                        String text=sectionItem.getText();
                        JSONObject jsonObject2=new JSONObject(text);
                        int id=jsonObject2.getInt("id");
                        if(delMap.get(id)!=null){
                            qdListSectionAdapter.getSectionItem(i).clear();
                            View view=LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_img_grouplist_view, null);
                            TextView name=view.findViewById(R.id.tvApplicationName);
                            name.setText(jsonObject.getString("name"));
                            qdListSectionAdapter.getSectionItem(i).add(view);
                        }
                    }

                    qdListSectionAdapter.setSelectMap();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else if(requestCode==5){
            System.out.println("is========5");

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

                    BigDecimal bigDecimal = new BigDecimal(allCost/10000);
                    double f1 = bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();//2.转换后的数字四舍五入保留小数点;
                    String rs = String.valueOf(f1);

                    SectionHeader header = new SectionHeader("共"+total+"条"+"        "+rs+"万元");
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
            jsonObject2.put("2",storage);
            Double cost = 0.0;
            try {
                cost=jsonObject1.getDouble("cost");
            }catch (Exception e){
                jsonObject1.put("cost",0.00);
            }
            Double retailprice =0.00;
            try {
                 retailprice=jsonObject1.getDouble("retailprice");
            }catch (Exception e){
                jsonObject1.put("retailprice",0.00);
            }
            jsonObject2.put("retailprice",retailprice);
            String img=jsonObject1.getString("img");
            String costText="";
            if(isCost) {
                costText = "成本:" + cost;
                jsonObject2.put("3",costText);
            }
            if(img!=null&&!img.equals("null")&&!img.equals("")) {
                img = "http://qiniu.lzxlzc.com/compress/" + img;
                jsonObject2.put("img",img);
            }

            itemMap.put(order,jsonObject1);

            contents.add(new SectionItem(jsonObject2.toString()));
        }

        return contents;
    }




    private class FetchItemsTaskAddStorage extends AsyncTask<Map,Void,String> {

        @Override
        protected String doInBackground(Map... params) {

            Map map=params[0];

            JSONArray jsonArray= (JSONArray) map.get("jsonArray");

            int amount= (int) map.get("amount");

            String items=Uri.encode(jsonArray.toString());

            return new OKHttpFetch(getApplication()).get(FlickrFetch.base+"/food/material/storageAdd?items="+items+"&amount="+amount);
        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    JSONObject jsonObject=new JSONObject(items);


                    String success = jsonObject.optString("success", null);
                    String message = jsonObject.optString("message", null);
                    if(success.equals("true")) {
                        if(message.equals("添加库存成功!")) {
                            Toast.makeText(getApplication(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
                            if (addCount == 1) {
                                int position = currentPostion;
                                JSONObject jsonObject1 = itemMap.get(position);
                                Integer storage = jsonObject1.getInt("storage");
                                jsonObject1.put("storage", storage + addStorage);
                                Drawable defaultDrawable = new getGradientDrawable(Color.YELLOW, 100).getGradientDrawable();
                                final Html.ImageGetter imgGetter = new HtmlImageGetter((TextView) viewHolder.itemView, MainActivity.dataUrl, defaultDrawable);
                             //   String s = StringToHtml(jsonObject1);
                             //   ((TextView) viewHolder.itemView).setText(Html.fromHtml(s, Html.FROM_HTML_MODE_COMPACT, imgGetter, null));
                            } else {
                                Toast.makeText(getApplication(), "批量添加库存后请刷新页面", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }else{
                        Toast.makeText(getApplication(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplication(),"添加库存失败",Toast.LENGTH_SHORT).show();
                }

            }

        }

    }

    private class FetchItemsTaskDel extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            JSONArray jsonArray=new JSONArray();

            for(Map.Entry<Integer,org.json.JSONObject> sMap:selectMap.entrySet()) {
                JSONObject json = sMap.getValue();
                try {
                    jsonArray.put(json.getInt("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            String ids=Uri.encode(jsonArray.toString());

            return new OKHttpFetch(getApplication()).get(FlickrFetch.base+"/food/material/deleteBatch2?ids="+ids);
        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    JSONObject jsonObject=new JSONObject(items);
                    Toast.makeText(getApplication(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
                    /*
                    mSearchView.clearFocus();
                    mPullRefreshLayout.finishRefresh();
                    itemMap=new HashMap<>();
                    selectMap=new HashMap<>();
                    page=1;
                    total=0;
                    search="";
                    initData();
                    */

                    selectMap=qdListSectionAdapter.getSelectMap();

                    Map delMap=new HashMap();

                    for(Integer k:selectMap.keySet()){
                        JSONObject jsonObject1=selectMap.get(k);
                        try {

                            int id=jsonObject1.getInt("id");

                            delMap.put(id,id);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    int count=qdListSectionAdapter.getItemCount();

                    for(int i=0;i<count;i++){
                        SectionItem sectionItem=qdListSectionAdapter.getSectionItem(i);
                        String text=sectionItem.getText();
                        JSONObject jsonObject2=new JSONObject(text);
                        int id=jsonObject2.getInt("id");
                        if(delMap.get(id)!=null){
                            qdListSectionAdapter.notifyItemRemoved(id);
                        }
                    }

                    qdListSectionAdapter.setSelectMap();


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplication(),"添加库存失败",Toast.LENGTH_SHORT).show();
                }

            }

        }

    }


    private class FetchItemsUpdateTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {


            JSONArray jsonArray=new JSONArray();

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

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/food/material/updateDelCatalog?"+value);

        }

        @Override
        protected void onPostExecute(String json) {

            JSONObject jsonObject = null;

            try {

                jsonObject = new JSONObject(json);

                String success = jsonObject.optString("success", null);

                Toast.makeText(FoodListActivity.this, jsonObject.optString("message"), Toast.LENGTH_LONG).show();

                if (success.equals("true")) {

                    mSearchView.clearFocus();
                    mPullRefreshLayout.finishRefresh();
                    itemMap=new HashMap<>();
                    selectMap=new HashMap<>();
                    page=1;
                    total=0;
                    search="";
                    initData();

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }


    private class FetchItemsDetail extends AsyncTask<JSONObject,Void,String> {

        @Override
        protected String doInBackground(JSONObject... params) {


            JSONObject jsonObject=params[0];
            int id=0;

            try {
                id=jsonObject.getInt("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/compages/materialCompages/queryByMaterialCompagesId?mCompagesId="+id);

        }

        @Override
        protected void onPostExecute(String json) {

            JSONObject jsonObject = null;

            try {

                jsonObject = new JSONObject(json);

                String success = jsonObject.optString("success", null);

               // Toast.makeText(FoodListActivity.this, jsonObject.optString("message"), Toast.LENGTH_LONG).show();

                if (success.equals("true")) {


                    JSONArray jsonArray=jsonObject.getJSONArray("result");

                    String s="";
                    String NAME="";
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject1= (JSONObject) jsonArray.get(i);

                        Integer MATERIAL_COMPAGES_ID=null;
                        try {
                            MATERIAL_COMPAGES_ID=jsonObject1.getInt("MATERIAL_COMPAGES_ID");
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        if(MATERIAL_COMPAGES_ID!=null){

                            String name=jsonObject1.getString("NAME");
                            Integer amount=jsonObject1.getInt("AMOUNT");
                            s+=i+"."+name+"  数量 "+amount+" ,\n";

                        }else{
                            NAME=jsonObject1.getString("NAME");
                        }

                    }

                    AlertDialog builder = new AlertDialog.Builder(FoodListActivity.this)
                            .setTitle(NAME+"(组合)")
                            .setMessage(s)
                            .setPositiveButton("确定", null)
                            .setNegativeButton("取消", null)
                            .show();
                    try {
                        //获取mAlert对象
                        Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
                        mAlert.setAccessible(true);
                        Object mAlertController = mAlert.get(builder);

                        //获取mTitleView并设置大小颜色
                        Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
                        mTitle.setAccessible(true);
                        TextView mTitleView = (TextView) mTitle.get(mAlertController);


                        //获取mMessageView并设置大小颜色
                        Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
                        mMessage.setAccessible(true);
                        TextView mMessageView = (TextView) mMessage.get(mAlertController);

                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }


}
