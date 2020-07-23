package com.safety.android.Inoutitem;

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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.qmuiteam.qmui.widget.section.QMUISection;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionLayout;
import com.safety.android.AccountheadList.AccountheadActivity;
import com.safety.android.SQLite3.PermissionInfo;
import com.safety.android.SQLite3.PermissionLab;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.qmuidemo.view.QDListSectionAdapter;
import com.safety.android.qmuidemo.view.SectionHeader;
import com.safety.android.qmuidemo.view.SectionItem;
import com.safety.android.tools.SwipeBackController;
import com.safety.android.util.DatePickerFragment2;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class InoutitemActivity extends AppCompatActivity implements OnLoginInforCompleted {

    QMUIPullRefreshLayout mPullRefreshLayout;

    QMUIStickySectionLayout mSectionLayout;

    private RecyclerView.LayoutManager mLayoutManager;
    protected QMUIStickySectionAdapter<SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> mAdapter;

    private int page=1;

    private int size=10;

    private int total=0;

    private BlockingQueue<ArrayList<SectionItem>> queue;

    private View view;

    // private SearchView mSearchView;

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

    private QDListSectionAdapter qdListSectionAdapter;

    private Map positionId=new HashMap();

    private Integer type;

    private Integer income=null;

    private AutoCompleteTextView atv_content;

    private JSONArray jsonArray;

    private Integer arg;


    private SwipeBackController swipeBackController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_second3, null);

        mPullRefreshLayout=view.findViewById(R.id.pull_to_refresh);
        mSectionLayout=view.findViewById(R.id.section_layout);

        initRefreshLayout();
        initStickyLayout();

        initData();

        queue=new ArrayBlockingQueue<>(3);

        setContentView(view);

        swipeBackController = new SwipeBackController(this);

      /*  mSearchView = findViewById(R.id.search);
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
                mPullRefreshLayout.finishRefresh();
                page=1;
                total=0;
                itemMap=new HashMap<>();
                selectMap=new HashMap<>();
                initData();
                return true;
            }
        });
*/

        mDateButton= (Button) view.findViewById(R.id.time_picker);

        final FragmentManager manager=getSupportFragmentManager();
        final DatePickerFragment2 dialog= DatePickerFragment2.newInstance("1",new Date());
        dialog.setOnLoginInforCompleted(this);

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new com.safety.android.util.DatePickerDialog(InoutitemActivity.this, 0, new com.safety.android.util.DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker startDatePicker, int startYear, int startMonthOfYear,
                                          int startDayOfMonth) {
                        String textString = String.format("选择年月：%d-%d\n", startYear,
                                startMonthOfYear + 1);
                        System.out.println(textString);

                    }
                },1,1,1).show();

            }
        });

        mDateButton2= (Button) view.findViewById(R.id.time_picker2);

        final FragmentManager manager2=getSupportFragmentManager();
        final DatePickerFragment2 dialog2=DatePickerFragment2.newInstance("2",new Date());
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
    public boolean onTouchEvent(MotionEvent ev) {
        System.out.println("ev====="+ev);
        if (swipeBackController.processEvent(ev)) {
            return true;
        } else {
            return super.onTouchEvent(ev);
        }
    }


    class SpinnerSelectedListener2 implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {

            if(arg2==0){
                income=null;
            }else if(arg2==1){
                income=1;
            }else if(arg2==2){
                income=0;
            }
            System.out.println("income="+income);

            mPullRefreshLayout.finishRefresh();
            itemMap=new HashMap<>();
            selectMap=new HashMap<>();
            page=1;
            total=0;
            initData();

        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        List<PermissionInfo> list= PermissionLab.get(getApplicationContext()).getPermissionInfo();

        Iterator<PermissionInfo> iterator=list.iterator();

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;

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

                tv_validateName.setText("项目");

                map.put("name", tv_validateName);
                map.put("value", et_validate);

                list.add(map);

                Map<String,Object> map2 = new HashMap<String, Object>();
                View validateItem2 = inflater.inflate(R.layout.item_validate_enter2, null);
                validateItem2.setTag(1);
                layout_validate.addView(validateItem2);
                TextView tv_validateName2 = (TextView) validateItem2.findViewById(R.id.tv_validateName);
                EditText et_validate2 = (EditText) validateItem2.findViewById(R.id.et_validate);
                TextView et_validateText2=validateItem2.findViewById(R.id.et_validate_text);
                et_validateText2.setText("");

                tv_validateName2.setText("金额");

                map2.put("name", tv_validateName2);
                map2.put("value", et_validate2);

                list.add(map2);


                View spinnerView = inflater.inflate(R.layout.spinner_accept, null);

                layout_validate.addView(spinnerView);

                TextView textView=spinnerView.findViewById(R.id.spinner_text);

                textView.setText("类型");

                Spinner spinner;
                //private Spinner spinner2;
                spinner = validateView.findViewById(R.id.Spinner01);

                final String[] m = {"支出", "收入"};

                ArrayAdapter<String> adapter;

                //将可选内容与ArrayAdapter连接起来
                adapter = new ArrayAdapter<String>(InoutitemActivity.this, android.R.layout.simple_spinner_item, m);

                //设置下拉列表的风格
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                //将adapter 添加到spinner中
                spinner.setAdapter(adapter);

                //添加事件Spinner事件监听
                spinner.setOnItemSelectedListener(new SpinnerSelectedListener());

                //设置默认值
                spinner.setVisibility(View.VISIBLE);

                String title = "新建开支";
                AlertDialog dialog = new AlertDialog.Builder(InoutitemActivity.this)
                        .setView(validateView)
                        .setPositiveButton(title, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Map map = new HashMap();


                                dialog.dismiss();
                            }

                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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

                        //mSearchView.clearFocus();
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
        qdListSectionAdapter=new QDListSectionAdapter(4);
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
                                if(startDate!=null&&!startDate.equals(""))
                                    cSearch+="&startDate="+startDate;
                                if(endDate!=null&&!endDate.equals(""))
                                    cSearch+="&endDate="+endDate;

                                if(income!=null) {
                                    cSearch += "&income=" + income;
                                }

                                String json = new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/inoutitem/inoutitem/list?column=createTime&order=desc&pageNo=" + page + "&pageSize="+size+cSearch);

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
                        if(total<=(page*10)) {
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





                    } catch (ClassCastException | JSONException e) {
                        e.printStackTrace();

                    }
                }
            }

            @Override
            public boolean onItemLongClick(QMUIStickySectionAdapter.ViewHolder holder, int position) {
                JSONObject jsonObject = null;

                final int n=holder.getAdapterPosition();

                jsonObject=itemMap.get(n);

                final JSONObject finaljsonObject=jsonObject;

                Integer id= null;
                try {
                    id = jsonObject.getInt("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(getApplicationContext(), AccountheadActivity.class);
                intent.putExtra("jsonString", id);
                startActivityForResult(intent, 2);
                return true;
            }
        });

        mSectionLayout.setAdapter(mAdapter, true);

        new FetchItemsTask().execute();


    }

    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {

            System.out.println("arg2="+arg2);

            type=arg2;

        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("requestCode===="+requestCode+"         resultCode==="+resultCode);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }else if(requestCode==1||requestCode==2){

            // mSearchView.clearFocus();
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

        //mSearchView.clearFocus();
        mPullRefreshLayout.finishRefresh();
        itemMap=new HashMap<>();
        page=1;
        total=0;
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

            if(income!=null) {
                cSearch += "&income=" + income;
                aSearch += "&income=" + income;
            }

            System.out.println("aSearch===="+aSearch);
            System.out.println("cSearch===="+cSearch);
            String res=new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/inoutitem/inoutitem/getAllAccount?"+aSearch);

            try {
                JSONObject jsonObject=new JSONObject(res);
                JSONObject jsonObject1=jsonObject.getJSONObject("result");
                allAccount=jsonObject1.getDouble("ALLACCOUNT");
                System.out.println("allAccount============"+allAccount);
            } catch (JSONException e) {
                e.printStackTrace();
                allAccount=0;
            }

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/inoutitem/inoutitem/list?column=createTime&order=desc&pageNo=" + page + "&pageSize"+size+cSearch);
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

                    boolean existMoreData=true;
                    if(total<=(page*10)) {
                        existMoreData=false;
                    }
                    section.setExistAfterDataToLoad(existMoreData);
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

        for (int i = 0; i < records.length(); i++) {

            int order=i+1+(page-1)*10;

            JSONObject jsonObject = (JSONObject) records.get(i);

            JSONObject jsonObject2=process(order,jsonObject);

            contents.add(new SectionItem(jsonObject2.toString()));

            itemMap.put(order,jsonObject2);
        }



        return contents;
    }


    private class FetchItemsTaskDel extends AsyncTask<Integer,Void,String> {

        @Override
        protected String doInBackground(Integer... params) {

            selectMap=qdListSectionAdapter.getSelectMap();

            JSONArray jsonArray=new JSONArray();

            for(Map.Entry<Integer,org.json.JSONObject> sMap:selectMap.entrySet()) {
                JSONObject json = sMap.getValue();
                try {
                    jsonArray.put(json.getInt("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            String ids= Uri.encode(jsonArray.toString());

            System.out.println("ids====="+ids);

            JSONObject jsonObject=new JSONObject();

            return new OKHttpFetch(getApplication()).post(FlickrFetch.base+"/inoutitem/inoutitem/deleteBatch?ids="+ids,jsonObject,"delete");
        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    JSONObject jsonObject=new JSONObject(items);


                    String success = jsonObject.optString("success", null);
                    String message = jsonObject.optString("message", null);
                    if(success.equals("true")) {

                        selectMap=qdListSectionAdapter.getSelectMap();

                        positionId=qdListSectionAdapter.getPositionID();

                        for(Integer k:selectMap.keySet()){
                            JSONObject jsonObject1=selectMap.get(k);
                            try {

                                int id=jsonObject1.getInt("id");

                                if(positionId.get(id)!=null){
                                    int position= (int) positionId.get(id);
                                    itemMap.remove(position);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        qdListSectionAdapter.setSelectMap();

                        new FetchItemsUpdate().execute();
                    }

                    Toast.makeText(getApplication(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplication(),"失败",Toast.LENGTH_SHORT).show();
                }

            }

        }

    }

    private class FetchItemsUpdate extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            String aSearch="";

            if(search!=null&&!search.equals("")) {
                aSearch += search2;
            }

            if(startDate!=null&&!startDate.equals("")) {
                aSearch+="&startDate=" + startDate;
            }
            if(endDate!=null&&!endDate.equals("")) {
                aSearch+="&endDate=" + endDate;
            }

            if(income!=null){
                aSearch += "&income=" + income;
            }

            System.out.println("aSearch===="+aSearch);

            return  new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/inoutitem/inoutitem/getAllAccount?"+aSearch);


        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    try {
                        JSONObject jsonObject=new JSONObject(items);
                        JSONObject jsonObject1=jsonObject.getJSONObject("result");
                        allAccount=jsonObject1.getDouble("ALLACCOUNT");
                        System.out.println("allAccount============"+allAccount);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        allAccount=0;
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

                    BigDecimal bigDecimal = new BigDecimal(allAccount/10000);
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

    private JSONObject process(int order,JSONObject jsonObject) throws JSONException {

        JSONObject jsonObject2=new JSONObject();

        try {
            int id=jsonObject.getInt("id");
            jsonObject2.put("id",id);
        }catch (Exception e){

        }

        String name ="";
        try {
            name=jsonObject.getString("name");
        }catch (Exception e){

        }


        jsonObject2.put("materialName",name);
        jsonObject2.put("name",name);
        jsonObject2.put("0",order);
        String  price = jsonObject.getString("price");
        Integer type=jsonObject.getInt("type");
        if(type==0){
            price="-"+price;
        }else if(type==1){
            price="+"+price;
        }
        jsonObject2.put("totalprice",price);
        String startTime=jsonObject.getString("startTime");
        jsonObject2.put("2",startTime);
        jsonObject2.put("startTime",startTime+"至");
        jsonObject2.put("3","金额:"+price+"元");
        String endTime=jsonObject.getString("endTime");
        jsonObject2.put("4",endTime);
        jsonObject2.put("endTime",endTime);


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
