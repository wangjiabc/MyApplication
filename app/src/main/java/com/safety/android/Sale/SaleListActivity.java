package com.safety.android.Sale;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import static com.safety.android.MainActivity.dataUrl;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.simple_list_item, null);

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
                JSONArray jsonArray=new JSONArray();
                System.out.println("selecmap==================");
                MyTestUtil.print(selectMap);
                for(Map.Entry<Integer,org.json.JSONObject> map:selectMap.entrySet()){
                    jsonArray.put(map.getValue());
                }
                JSONObject jsonObject=new JSONObject();
                try {
                    jsonObject.put("ids",jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getApplicationContext(), SaleActivity.class);
                intent.putExtra("jsonString", jsonObject.toString());
                startActivityForResult(intent, 1);
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

                        new Thread(new Runnable(){

                            @Override
                            public void run() {

                                ArrayList<SectionItem> contents = new ArrayList<>();
                                String cSearch="";
                                if(search!=null&&!search.equals(""))
                                    cSearch=search;
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

                        new AlertDialog.Builder(SaleListActivity.this)
                                .setTitle(finalJsonObject.getString("name"))
                                .setMessage("零售价:"+finalJsonObject.getDouble("retailprice"))
                                .setNegativeButton("出售", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        dialogInterface.dismiss();

                                        LayoutInflater inflater = getLayoutInflater();
                                        View validateView = inflater.inflate(
                                                R.layout.dialog_validate, null);
                                        final LinearLayout layout_validate = (LinearLayout) validateView.findViewById(R.id.layout_validate);
                                        layout_validate.removeAllViews();
                                        final List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();


                                        View validateItem = inflater.inflate(R.layout.item_validate_enter, null);
                                        validateItem.setTag(0);
                                        layout_validate.addView(validateItem);
                                        TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                                        EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                                        TextView et_validateText=validateItem.findViewById(R.id.et_validate_text);
                                        et_validateText.setText("价格");
                                        Map<String,Object> map = new HashMap<String, Object>();
                                        try {
                                            tv_validateName.setText(finalJsonObject.getString("name"));
                                            et_validate.setText(finalJsonObject.getString("cost"));

                                            map.put("id",finalJsonObject.getInt("id"));
                                            map.put("name", tv_validateName);
                                            map.put("value", et_validate);
                                            map.put("combination",finalJsonObject.getInt("combination"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }


                                        list.add(map);

                                        View validateItem2 = inflater.inflate(R.layout.item_validate_enter, null);
                                        validateItem2.setTag(1);
                                        layout_validate.addView(validateItem2);
                                        TextView tv_validateName2 = (TextView) validateItem2.findViewById(R.id.tv_validateName);
                                        EditText et_validate2 = (EditText) validateItem2.findViewById(R.id.et_validate);
                                        TextView et_validateText2=validateItem2.findViewById(R.id.et_validate_text);
                                        Map<String,Object> map2 = new HashMap<String, Object>();
                                        tv_validateName2.setText("数量");
                                        et_validateText2.setText("");
                                        et_validate2.setText("1");

                                        map2.put("name", tv_validateName2);
                                        map2.put("value", et_validate2);

                                        list.add(map2);

                                        AlertDialog dialog = new AlertDialog.Builder(SaleListActivity.this).setTitle("添加库存")
                                                .setView(validateView)
                                                .setPositiveButton("确定", new DialogInterface.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which)
                                                    {
                                                        JSONArray jsonArray=new JSONArray();
                                                        int amount=0;
                                                        int id= (int) list.get(0).get("id");
                                                        String name = ((TextView) list.get(0).get("name")).getText().toString();
                                                        String cost = ((EditText) list.get(0).get("value")).getText().toString();
                                                        int combination = (int) list.get(0).get("combination");
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

                                                        String amountString= ((EditText) list.get(1).get("value")).getText().toString();
                                                        amount=Integer.parseInt(amountString);

                                                        Map map=new HashMap();
                                                        map.put("jsonArray",jsonArray);
                                                        map.put("amount",amount);
                                                        new FetchItemsTaskAddStorage().execute(map);

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
                    String s = StringToHtml(jsonObject);
                    contents.add(new SectionItem(s));
                    boolean existMoreData = true;

                    if (total < (page * 10)) {
                        existMoreData = false;
                    }

                    mAdapter.finishLoadMore(mAdapter.getSection(mAdapter.getItemCount()), contents, true, existMoreData);
                }else {
                    int position=jsonObject.getInt("position");
                    jsonObject.put("order",position);
                    Drawable defaultDrawable = new getGradientDrawable(Color.YELLOW,100).getGradientDrawable();
                    final Html.ImageGetter imgGetter = new HtmlImageGetter((TextView) viewHolder.itemView, MainActivity.dataUrl, defaultDrawable);
                    String s = StringToHtml(jsonObject);
                    ((TextView) viewHolder.itemView).setText(Html.fromHtml(s,Html.FROM_HTML_MODE_COMPACT, imgGetter,null));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            String aSearch="";
            String cSearch="";

            if(search!=null&&!search.equals("")) {
                cSearch = search;
                aSearch = search2;
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


    private class FetchItemsTaskAddStorage extends AsyncTask<Map,Void,String> {

        @Override
        protected String doInBackground(Map... params) {

            Map map=params[0];

            JSONArray jsonArray= (JSONArray) map.get("jsonArray");

            int amount= (int) map.get("amount");

            String items= Uri.encode(jsonArray.toString());

            return new OKHttpFetch(getApplication()).get(FlickrFetch.base+"/food/material/storageAdd?items="+items+"&amount="+amount);
        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    JSONObject jsonObject=new JSONObject(items);
                    Toast.makeText(getApplication(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();

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

                    mSearchView.clearFocus();
                    mPullRefreshLayout.finishRefresh();
                    itemMap=new HashMap<>();
                    selectMap=new HashMap<>();
                    page=1;
                    total=0;
                    search="";
                    initData();


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplication(),"添加库存失败",Toast.LENGTH_SHORT).show();
                }

            }

        }

    }

}

