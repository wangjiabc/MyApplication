package com.safety.android.Storage;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.safety.android.qmuidemo.view.HtmlImageGetter;
import com.safety.android.qmuidemo.view.QDListSectionAdapter;
import com.safety.android.qmuidemo.view.SectionHeader;
import com.safety.android.qmuidemo.view.SectionItem;
import com.safety.android.qmuidemo.view.getGradientDrawable;
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

import static com.safety.android.MainActivity.dataUrl;

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
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "修改密码").setIcon(android.R.drawable.ic_lock_lock);
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
        return new QDListSectionAdapter();
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

                        MyTestUtil.print(finalJsonObject);

                        new AlertDialog.Builder(StorageListActivity.this)
                                .setTitle(finalJsonObject.getString("name"))
                                .setMessage("库存:"+finalJsonObject.getDouble("currentstorage"))
                                .setNegativeButton("出售", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        dialogInterface.dismiss();

                                        JSONArray jsonArray=new JSONArray();

                                        jsonArray.put(itemMap.get(holder.getAdapterPosition()));

                                        JSONObject jsonObject=new JSONObject();
                                        try {
                                            jsonObject.put("ids",jsonArray);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        Intent intent = new Intent(getApplicationContext(), SaleActivity.class);
                                        intent.putExtra("jsonString", jsonObject.toString());
                                        startActivityForResult(intent, 1);

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

        List list=new ArrayList();
        Map map=new HashMap();
        for (int i = 0; i < records.length(); i++) {

            int order=i+1+(page-1)*10;

            JSONObject jsonObject1 = (JSONObject) records.get(i);
            jsonObject1.put("order",order);
            String s=StringToHtml(jsonObject1);

            itemMap.put(order,jsonObject1);

            JSONArray jsonArray=new JSONArray();

            try {
                int id=jsonObject1.getInt("id");
                jsonArray.put(id);
            }catch (Exception e){

            }

            jsonArray.put(order);

            String name ="";
            try {
                name=jsonObject1.getString("name");
                jsonArray.put(name);
            }catch (Exception e){

            }
            Integer stockstorage =0;
            try{
                stockstorage= jsonObject1.getInt("stockstorage");
                jsonArray.put(stockstorage);
            }catch (Exception e){

            }
            Integer currentstorage=0;
            try{
                currentstorage= jsonObject1.getInt("storage");
                jsonArray.put(currentstorage);
            }catch (Exception e){

            }
            Double cost =0.0;
            try{
                cost=jsonObject1.getDouble("ALLCOST");
                jsonArray.put(cost);
            }catch (Exception e){

            }
            Integer accountcount=0;
            try {
                accountcount=jsonObject1.getInt("accountcount");
                jsonArray.put(accountcount);
            }catch (Exception e){

            }
            Integer realStorage=0;
            try{
                realStorage=jsonObject1.getInt("real_storage");
                jsonArray.put(realStorage);
            }catch (Exception e){

            }
            String img="";
            String costText="";
            double accountcost= (int) (accountcount*(cost/stockstorage));
            String accountText="";
            if(isCost) {
                costText = "<span>成本:" + cost + "</span>";
                accountText= "<span>成本:" + accountcost + "</span>";
            }
            String diffString="";
            int diff=0;
            diff=stockstorage-currentstorage-accountcount;
            if(diff>0){
                diffString="<span><font color='red' size='20'>-" + diff + "</span>";
            }else if(diff<0){
                diffString="<span><font color='green' size='20'>+" + diff + "</span>";
            }
            String diff2String="";
            int diff2=0;
            diff2=currentstorage-realStorage;
            if(diff2>0){
                diff2String="<span><font color='red' size='20'>-" + diff2 + "</span>";
            }else if(diff2<0){
                diff2String="<span><font color='green' size='20'>+" + diff2 + "</span>";
            }


            contents.add(new SectionItem(jsonArray.toString()));
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
        Integer stockstorage =0;
        try{
           stockstorage= jsonObject.getInt("stockstorage");
        }catch (Exception e){

        }
        Integer currentstorage=0;
        try{
            currentstorage= jsonObject.getInt("storage");
        }catch (Exception e){

        }
        Double cost =0.0;
        try{
            cost=jsonObject.getDouble("ALLCOST");
        }catch (Exception e){

        }
        Integer accountcount=0;
        try {
            accountcount=jsonObject.getInt("accountcount");
        }catch (Exception e){

        }
        Integer realStorage=0;
        try{
            realStorage=jsonObject.getInt("real_storage");
        }catch (Exception e){

        }
        String img="";
        String costText="";
        double accountcost= (int) (accountcount*(cost/stockstorage));
        String accountText="";
        if(isCost) {
            costText = "<span>成本:" + cost + "</span>";
            accountText= "<span>成本:" + accountcost + "</span>";
        }
        String diffString="";
        int diff=0;
        diff=stockstorage-currentstorage-accountcount;
        if(diff>0){
            diffString="<span><font color='red' size='20'>-" + diff + "</span>";
        }else if(diff<0){
            diffString="<span><font color='green' size='20'>+" + diff + "</span>";
        }
        String diff2String="";
        int diff2=0;
        diff2=currentstorage-realStorage;
        if(diff2>0){
            diff2String="<span><font color='red' size='20'>-" + diff2 + "</span>";
        }else if(diff2<0){
            diff2String="<span><font color='green' size='20'>+" + diff2 + "</span>";
        }
        if(img!=null&&!img.equals(""))
            img="<img src='http://qiniu.lzxlzc.com/compress/"+img+"'/>";
        String s = "<p>"+first+img+"&nbsp;&nbsp;<big><font size='20'><b>" + name + "</b></font></big></p>" +
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;进货数量:" + stockstorage + "</span>&nbsp;&nbsp;"+costText
                +"<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;销售数量:" + accountcount +  "&nbsp;&nbsp;"+accountText+ "&nbsp;&nbsp;&nbsp;&nbsp;"+diffString
                +"<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;当前库存:" + currentstorage +"&nbsp;&nbsp;<span>实际库存:" + realStorage + "&nbsp;&nbsp;&nbsp;&nbsp;"+diff2String;
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
        Integer stockstorage =0;
        try{
            stockstorage= jsonObject.getInt("stockstorage");
        }catch (Exception e){

        }
        Integer currentstorage=0;
        try{
            currentstorage= jsonObject.getInt("storage");
        }catch (Exception e){

        }
        Double cost =0.0;
        try{
            cost=jsonObject.getDouble("ALLCOST");
        }catch (Exception e){

        }
        Integer accountcount=0;
        try {
            accountcount=jsonObject.getInt("accountcount");
        }catch (Exception e){

        }
        Integer realStorage=0;
        try{
            realStorage=jsonObject.getInt("real_storage");
        }catch (Exception e){

        }
        String img="";
        String costText="";
        double accountcost= (int) (accountcount*(cost/stockstorage));
        String accountText="";
        if(isCost) {
            costText="<span><font color='red' size='20'>成本:" + cost + "</span>";
            accountText= "<span><font color='red' size='20'>成本:" + accountcost + "</span>";
        }
        if(img!=null&&!img.equals(""))
            img="<img src='http://qiniu.lzxlzc.com/compress/"+img+"'/>";
        String s ="<p>"+first+img+"&nbsp;&nbsp;<span><big><font color='red'　size='20'><b>" + name + "</b></font></big></p>" +
                "<block quote>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;<font color='red' size='20'>进货数量:" + stockstorage + "</font>&nbsp;&nbsp;"+costText+ "</block quote>"
                +"<p></span>&nbsp;&nbsp;<span>已销售:" + accountcount + "</span>&nbsp;&nbsp;"+accountText+ "</block quote>"
                + "<p></span>&nbsp;&nbsp;<span><font color='red' size='20'></span>&nbsp;&nbsp;<span>当前库存:" + currentstorage +"</span>&nbsp;&nbsp;<span>实际库存:" + realStorage + "</block quote></p>";
        return s;
    }




}