package com.safety.android.AccountheadList;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.safety.android.Sale.SaleActivity;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.qmuidemo.view.QDListSectionAdapter;
import com.safety.android.qmuidemo.view.SectionHeader;
import com.safety.android.qmuidemo.view.SectionItem;
import com.safety.android.tools.MyTestUtil;
import com.safety.android.util.DatePickerFragment;
import com.safety.android.util.OnLoginInforCompleted;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by WangJing on 2018/5/15.
 */

public class AccountheadListActivity extends AppCompatActivity implements OnLoginInforCompleted {

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

    private double allAccount=0;

    private TextView mDateTextView;

    private Button mDateButton;

    private Button mDateButton2;

    private static final int REQUEST_DATE=0;

    private String startDate;

    private String endDate;

    private boolean refurbish=true;

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

                search="&materialName=*"+queryText+"*";
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


        mDateButton= (Button) view.findViewById(R.id.time_picker);

        final FragmentManager manager=getSupportFragmentManager();
        final DatePickerFragment dialog=DatePickerFragment.newInstance("1",new Date());
        dialog.setOnLoginInforCompleted(this);

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show(manager,"");
            }
        });

        mDateButton2= (Button) view.findViewById(R.id.time_picker2);

        final FragmentManager manager2=getSupportFragmentManager();
        final DatePickerFragment dialog2=DatePickerFragment.newInstance("2",new Date());
        dialog2.setOnLoginInforCompleted(this);

        mDateButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog2.show(manager2,"");
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
        return new QDListSectionAdapter(0);
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
                                if(startDate!=null&&!startDate.equals(""))
                                    cSearch+="&startDate="+startDate;
                                if(endDate!=null&&!endDate.equals(""))
                                    cSearch+="&endDate="+endDate;
                                String json = new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/accounthead/accounthead/listUnite?column=createTime&order=desc&pageNo=" + page + "&pageSize="+size+cSearch);

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

                        jsonObject=itemMap.get(n);

                        final JSONObject finaljsonObject=jsonObject;

                        Integer id=jsonObject.getInt("id");

                        Intent intent = new Intent(getApplicationContext(), AccountheadActivity.class);
                        intent.putExtra("jsonString", id);
                        startActivityForResult(intent, 2);

                        /*
                        LayoutInflater inflater = getLayoutInflater();
                        View validateView = inflater.inflate(
                                R.layout.dialog_validate, null);
                        final LinearLayout layout_validate = (LinearLayout) validateView.findViewById(R.id.layout_validate);
                        layout_validate.removeAllViews();

                        Map<String,Object> sMap=new HashMap();

                        List list=new ArrayList();

                        try {
                            String name = jsonObject.getString("materialName");
                            sMap.put("商品名称:",name);
                            list.add(sMap);
                            String  billno = jsonObject.getString("billno");
                            sMap=new HashMap();
                            sMap.put("订单号:",billno);
                            list.add(sMap);
                            String supplier=jsonObject.getString("supplier");
                            sMap=new HashMap();
                            sMap.put("客户名称:",supplier);
                            list.add(sMap);
                            Double totalprice = jsonObject.getDouble("totalprice");
                            sMap=new HashMap();
                            sMap.put("销售金额:",totalprice);
                            list.add(sMap);
                            String  count=jsonObject.getString("count");
                            sMap=new HashMap();
                            sMap.put("数量:",count);
                            list.add(sMap);
                            String detail=jsonObject.getString("detail");
                            sMap=new HashMap();
                            sMap.put("详情",detail);
                            list.add(sMap);
                            String img=jsonObject.getString("img");
                            String createTime=jsonObject.getString("createTime");
                            sMap=new HashMap();
                            sMap.put("时间:",createTime);
                            list.add(sMap);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        int i=0;
                        Iterator iterator=list.iterator();
                        while (iterator.hasNext()) {
                            Map<String, Object> cMap= (Map) iterator.next();
                            for (Map.Entry<String, Object> map : cMap.entrySet()) {

                                View validateItem = inflater.inflate(R.layout.item_validate_enter, null);
                                validateItem.setTag(i);
                                layout_validate.addView(validateItem);
                                TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                                EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                                TextView et_validateText = validateItem.findViewById(R.id.et_validate_text);
                                et_validate.setVisibility(View.GONE);

                                tv_validateName.setText(map.getKey());
                                et_validateText.setText(map.getValue().toString());

                                i++;
                            }
                        }


                        AlertDialog dialog = new AlertDialog.Builder(AccountheadListActivity.this)
                                .setView(validateView)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {

                                        dialog.dismiss();
                                    }

                                }).setNegativeButton("删除", new DialogInterface.OnClickListener()
                                {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                        try {
                                            new AlertDialog.Builder(AccountheadListActivity.this)
                                                    .setTitle("删除商品"+finaljsonObject.getString("materialName")+"销售记录,订单号:"+finaljsonObject.getString("billno")+"?")
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
                                                                new FetchItemsTaskDel().execute(finaljsonObject.getInt("id"));
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }

                                                            dialogInterface.dismiss();
                                                        }
                                                    })
                                                    .create()
                                                    .show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }).create();
                        dialog.show();
                       */


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
        }else if(requestCode==1||requestCode==2){

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

    @Override
    public void inputLoginInforCompleted(String userName, String date) {
        if(userName.equals("1")){
            startDate=date;
            mDateButton.setText(date);
        }else if(userName.equals("2")){
            endDate=date;
            mDateButton2.setText(date);
        }

        mSearchView.clearFocus();
        mPullRefreshLayout.finishRefresh();
        itemMap=new HashMap<>();
        page=1;
        total=0;
        search="";
        refurbish=false;
        initData();

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

            if(startDate!=null&&!startDate.equals("")) {
                cSearch += "&startDate=" + startDate;
                aSearch+="&startDate=" + startDate;
            }
            if(endDate!=null&&!endDate.equals("")) {
                cSearch += "&endDate=" + endDate;
                aSearch+="&endDate=" + endDate;
            }

            System.out.println("aSearch===="+aSearch);

            String res=new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/accounthead/accounthead/getAllAccount?"+aSearch);

            try {
                JSONObject jsonObject=new JSONObject(res);
                JSONObject jsonObject1=jsonObject.getJSONObject("result");
                allAccount=jsonObject1.getDouble("ALLACCOUNT");
                System.out.println("allAccount============"+allAccount);
            } catch (JSONException e) {
                e.printStackTrace();
                allAccount=0;
            }

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/accounthead/accounthead/listUnite?column=createTime&order=desc&pageNo=" + page + "&pageSize"+size+cSearch);
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


                    BigDecimal bigDecimal = new BigDecimal(allAccount/10000);
                    double f1 = bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();//2.转换后的数字四舍五入保留小数点后一位;
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

    private ArrayList<SectionItem> addContents(ArrayList<SectionItem> contents,JSONObject jsonObject0) throws JSONException {

        JSONObject result= (JSONObject) jsonObject0.get("result");

        JSONArray records = result.getJSONArray("records");

        total=result.getInt("total");
/*
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
*/


        for (int i = 0; i < records.length(); i++) {

            int order=i+1+(page-1)*10;

            JSONObject jsonObject = (JSONObject) records.get(i);

            JSONObject jsonObject2=new JSONObject();

            try {
                int id=jsonObject.getInt("id");
                jsonObject2.put("id",id);
            }catch (Exception e){

            }

            String name ="";
            try {
                name=jsonObject.getString("materialName");
            }catch (Exception e){

            }
            String supplier=jsonObject.getString("supplier");

            jsonObject2.put("name",supplier);
            String  billno = jsonObject.getString("billno");
            jsonObject2.put("0",order);
            String  totalprice = jsonObject.getString("totalprice");
            jsonObject2.put("2",name);
            jsonObject2.put("3",totalprice);
            String  count=jsonObject.getString("count");
            jsonObject2.put("5",count);
            String createTime=jsonObject.getString("createTime");
            jsonObject2.put("4",createTime);


            contents.add(new SectionItem(jsonObject2.toString()));

            itemMap.put(order,jsonObject2);
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
        String name = jsonObject.getString("materialName");
        String  billno = jsonObject.getString("billno");
        Double totalprice = jsonObject.getDouble("totalprice");
        String  count=jsonObject.getString("count");
        String img=jsonObject.getString("img");
        String createTime=jsonObject.getString("createTime");
        String supplier=jsonObject.getString("supplier");
        if(img!=null&&!img.equals(""))
            img="<img src='http://qiniu.lzxlzc.com/compress/"+img+"'/>";
        String s = "<p>"+first+img+"&nbsp;&nbsp;<big><font size='20'><b>" + name + "</b></font></big></p>" +
                "<p><block quote>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;订单号:" + billno + "</p>"+
                "<p><block quote>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;数量"+count+ "</span>&nbsp;&nbsp;<span>金额:" + totalprice + "</block quote></p>"+
                "<block quote>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;客户"+supplier+ "</span>&nbsp;&nbsp"+"</span>&nbsp;&nbsp;<span>时间:" + createTime + "</block quote>";
        return s;
    }


    private class FetchItemsTaskDel extends AsyncTask<Integer,Void,String> {

        @Override
        protected String doInBackground(Integer... params) {

            int id=params[0];

            JSONObject jsonObject=new JSONObject();

            return new OKHttpFetch(getApplication()).post(FlickrFetch.base+"/accounthead/accounthead/delete?id="+id,jsonObject,"delete");
        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    JSONObject jsonObject=new JSONObject(items);


                    String success = jsonObject.optString("success", null);
                    String message = jsonObject.optString("message", null);
                    if(success.equals("true")) {

                        ((TextView) viewHolder.itemView).setText("");

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
