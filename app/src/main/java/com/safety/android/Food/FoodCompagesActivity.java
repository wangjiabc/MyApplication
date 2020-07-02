package com.safety.android.Food;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.qmuiteam.qmui.widget.section.QMUISection;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionLayout;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.qmuidemo.view.QDListSectionAdapter;
import com.safety.android.qmuidemo.view.SectionHeader;
import com.safety.android.qmuidemo.view.SectionItem;
import com.safety.android.tools.MyTestUtil;
import com.safety.android.tools.TakePictures;
import com.safety.android.tools.UpFileToQiniu;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.safety.android.tools.TakePictures.REQUEST_PHOTO;

public class FoodCompagesActivity extends AppCompatActivity {

    private View view;

    QMUIPullRefreshLayout mPullRefreshLayout;

    QMUIStickySectionLayout mSectionLayout;

    private ImageButton mPhotoButton;

    private ImageView mPhotoView;

    private String img=null;

    private EditText editText1;

    private int id=-1;

    private int position=0;

    private Button foodButton;

    private boolean isEdit=false;

    private Map<Integer,JSONObject> itemMap=new HashMap<>();

    private RecyclerView.LayoutManager mLayoutManager;
    protected QMUIStickySectionAdapter<SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.food_compages, null);

        mPullRefreshLayout=view.findViewById(R.id.food_commpages_to_refresh);
        mSectionLayout=view.findViewById(R.id.food_commpages_section_layout);

        mPhotoView=view.findViewById(R.id.food_compages_photo);
        mPhotoButton=view.findViewById(R.id.food_compages_camera);

        editText1=view.findViewById(R.id.food_compages_name);

        Intent intent=getIntent();

        String jsonString=intent.getStringExtra("jsonString");

        if(jsonString!=null&&!jsonString.equals("")){

            try {

                JSONObject jsonObject = new JSONObject(jsonString);

                id=jsonObject.getInt("id");
                if(id!=-1) {
                    isEdit=true;
                    position = jsonObject.getInt("position");
                    String name = jsonObject.getString("name");
                    String thisImg = jsonObject.getString("img");
                    if (thisImg != null)
                        updatePhotoView("http://qiniu.lzxlzc.com/" + thisImg);

                    editText1.setText(name);
                }else{

                    JSONArray jsonArray=jsonObject.getJSONArray("ids");

                    System.out.println("jsonArray==============");
                    MyTestUtil.print(jsonArray);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        int order=i+1;

                        JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                        JSONObject jsonObject2=new JSONObject();
                        jsonObject2.put("order",order);
                        jsonObject2.put("NAME",jsonObject1.get("name"));
                        jsonObject2.put("id",jsonObject1.getInt("id"));
                        jsonObject2.put("AMOUNT","1");
                        itemMap.put(order,jsonObject2);

                    }

                    initDataNew();

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        initRefreshLayout();
        initStickyLayout();
        if(id!=-1)
            initData();

        mPhotoButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                int hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(),
                        Manifest.permission.CAMERA);
                if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
                    //有权限。
                } else {
                    //没有权限，申请权限。
                    requestPermissions(new String[]{Manifest.permission.CAMERA},100);
                }
                TakePictures takePictures=new TakePictures(getApplication());
                Intent captureImage=takePictures.getCaptureImage();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 100);
                    startActivityForResult(captureImage, REQUEST_PHOTO);
                }else{
                    startActivityForResult(captureImage, REQUEST_PHOTO);
                }

            }
        });

        foodButton=view.findViewById(R.id.food_compages_button);

        foodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("foddbout click");

                new FetchItemsUpdateTask().execute();

            }
        });

        setContentView(view);
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

    protected QMUIStickySectionAdapter<
            SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> createAdapter() {
        return new QDListSectionAdapter(3);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_food_compages_add:

                Intent intent = new Intent(getApplicationContext(), FoodCompagesListActivity.class);
                startActivityForResult(intent,1);

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
                        mPullRefreshLayout.finishRefresh();
                    }
                }, 2000);
            }
        });
    }

    private void initData() {
        mAdapter = createAdapter();
        mAdapter.setCallback(new QMUIStickySectionAdapter.Callback<SectionHeader, SectionItem>() {
            @Override
            public void loadMore(final QMUISection<SectionHeader, SectionItem> section, final boolean loadMoreBefore) {

            }

            @Override
            public void onItemClick(final QMUIStickySectionAdapter.ViewHolder holder, int position) {
                Toast.makeText(getApplicationContext(), "click item " + position, Toast.LENGTH_SHORT).show();
                try {

                    JSONObject jsonObject = null;

                    jsonObject = itemMap.get(holder.getAdapterPosition());

                    final JSONObject finalJsonObject = jsonObject;

                    final int n=holder.getAdapterPosition();

                    LayoutInflater inflater = getLayoutInflater();
                    View validateView = inflater.inflate(
                            R.layout.dialog_validate, null);
                    final LinearLayout layout_validate = (LinearLayout) validateView.findViewById(R.id.layout_validate);
                    layout_validate.removeAllViews();
                    final List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();


                    Map<String,Object> map = new HashMap<String, Object>();
                    View validateItem = inflater.inflate(R.layout.item_validate_enter2, null);
                    validateItem.setTag(1);
                    layout_validate.addView(validateItem);
                    TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                    EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                    TextView et_validateText=validateItem.findViewById(R.id.et_validate_text);
                    MyTestUtil.print(finalJsonObject);
                    try {
                        tv_validateName.setText(finalJsonObject.getString("NAME"));
                        et_validate.setText(finalJsonObject.getString("AMOUNT"));

                        map.put("id",finalJsonObject.getInt("id"));
                        map.put("name", tv_validateName);
                        map.put("value", et_validate);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    list.add(map);


                    AlertDialog dialog = new AlertDialog.Builder(FoodCompagesActivity.this).setTitle("设置组合数量")
                            .setView(validateView)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    StringBuffer stringBuffer = new StringBuffer();
                                    for(int i=0;i<list.size();i++){
                                        int id= (int) list.get(i).get("id");
                                        String name = ((TextView)list.get(i).get("name")).getText().toString();
                                        String value = ((EditText)list.get(i).get("value")).getText().toString();
                                        int amount=Integer.parseInt(value);
                                        try {
                                            finalJsonObject.put("AMOUNT",amount);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        itemMap.put(n,finalJsonObject);
                                        initDataNew();
                                        stringBuffer.append(id+"  "+name+"  "+value+",");
                                    }

                                    System.out.println(stringBuffer);

                                    dialog.dismiss();
                                }

                            }).setNegativeButton("删除", new DialogInterface.OnClickListener()
                            {

                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    itemMap.remove(n);
                                    initDataNew();
                                    dialog.dismiss();
                                }
                            }).create();
                    dialog.show();

                }catch (ClassCastException e){
                    e.printStackTrace();
                    ((TextView) holder.itemView).setText("");
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

    private void initDataNew() {
        mAdapter = createAdapter();
        mAdapter.setCallback(new QMUIStickySectionAdapter.Callback<SectionHeader, SectionItem>() {
            @Override
            public void loadMore(final QMUISection<SectionHeader, SectionItem> section, final boolean loadMoreBefore) {

            }

            @Override
            public void onItemClick(final QMUIStickySectionAdapter.ViewHolder holder, int position) {
                Toast.makeText(getApplicationContext(), "click item " + position, Toast.LENGTH_SHORT).show();
                try {

                    JSONObject jsonObject = null;

                    jsonObject = itemMap.get(holder.getAdapterPosition());

                    final JSONObject finalJsonObject = jsonObject;

                    final int n=holder.getAdapterPosition();

                    LayoutInflater inflater = getLayoutInflater();
                    View validateView = inflater.inflate(
                            R.layout.dialog_validate, null);
                    final LinearLayout layout_validate = (LinearLayout) validateView.findViewById(R.id.layout_validate);
                    layout_validate.removeAllViews();
                    final List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();


                    Map<String,Object> map = new HashMap<String, Object>();
                    View validateItem = inflater.inflate(R.layout.item_validate_enter2, null);
                    validateItem.setTag(1);
                    layout_validate.addView(validateItem);
                    TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                    EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                    TextView et_validateText=validateItem.findViewById(R.id.et_validate_text);
                    try {
                        tv_validateName.setText(finalJsonObject.getString("NAME"));
                        et_validate.setText(finalJsonObject.getString("AMOUNT"));

                        map.put("id",finalJsonObject.getInt("id"));
                        map.put("name", tv_validateName);
                        map.put("value", et_validate);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    list.add(map);


                    AlertDialog dialog = new AlertDialog.Builder(FoodCompagesActivity.this).setTitle("设置组合数量")
                            .setView(validateView)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    StringBuffer stringBuffer = new StringBuffer();
                                    for(int i=0;i<list.size();i++){
                                        int id= (int) list.get(i).get("id");
                                        String name = ((TextView)list.get(i).get("name")).getText().toString();
                                        String value = ((EditText)list.get(i).get("value")).getText().toString();
                                        int amount=Integer.parseInt(value);
                                        try {
                                            finalJsonObject.put("AMOUNT",amount);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        itemMap.put(n,finalJsonObject);
                                        initDataNew();
                                        stringBuffer.append(id+"  "+name+"  "+value+",");
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

                }catch (ClassCastException e){
                    e.printStackTrace();
                    ((TextView) holder.itemView).setText("");
                }
            }

            @Override
            public boolean onItemLongClick(QMUIStickySectionAdapter.ViewHolder holder, int position) {
                Toast.makeText(getApplicationContext(), "long click item " + position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mSectionLayout.setAdapter(mAdapter, true);

        ArrayList<QMUISection<SectionHeader, SectionItem>> list = new ArrayList<>();

        ArrayList<SectionItem> contents = new ArrayList<>();

        try {

                contents=addContentsNew(contents);

                int total=itemMap.size();

                SectionHeader header = new SectionHeader("共"+total+"个零件构成");
                QMUISection<SectionHeader, SectionItem> section = new QMUISection<>(header, contents, false);

                list.add(section);

                section.setExistAfterDataToLoad(true);

                mAdapter.setData(list);



        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/compages/materialCompages/queryByMaterialCompagesId?mCompagesId="+id);

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

                    int total=itemMap.size();

                    SectionHeader header = new SectionHeader("共"+total+"个部件");
                    QMUISection<SectionHeader, SectionItem> section = new QMUISection<>(header, contents, false);

                    list.add(section);

                    section.setExistAfterDataToLoad(true);

                    mAdapter.setData(list);


                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private class FetchItemsUpdateTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {


            com.alibaba.fastjson.JSONArray jsonArray=new com.alibaba.fastjson.JSONArray();

            int i=0;

            for(Map.Entry<Integer,org.json.JSONObject> map:itemMap.entrySet()) {

                JSONObject jsonObject=map.getValue();

                try {
                    jsonObject.put("number","1");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                com.alibaba.fastjson.JSONObject jsonObject1=new com.alibaba.fastjson.JSONObject();

                try {
                    jsonObject1.put("id",jsonObject.get("id"));
                    jsonObject1.put("number",jsonObject.get("AMOUNT"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonArray.add(jsonObject1);

                i++;
            }

            MyTestUtil.print(jsonArray);

            String value="";

            if(jsonArray.size()>0)
                value+="items="+ Uri.encode(jsonArray.toJSONString());

            System.out.println("jsonArray======"+jsonArray);
            System.out.println("value=========="+value);

            if(id!=-1)
                value+="&compagesId="+id;

            String text1=editText1.getText().toString();

            if(text1!=null&&!text1.equals(""))
                value+="&compagesName="+text1;

            if(img!=null&&!img.equals(""))
                value+="&img="+img;

            System.out.println("value======="+value);

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/food/material/compages?"+value);

        }

        @Override
        protected void onPostExecute(String json) {

            JSONObject jsonObject = null;

            try {

                jsonObject = new JSONObject(json);

                String success = jsonObject.optString("success", null);

                Toast.makeText(FoodCompagesActivity.this, jsonObject.optString("message"), Toast.LENGTH_LONG).show();

                if (success.equals("true")) {

                    String text1=editText1.getText().toString();

                    JSONObject jsonObject1 =new JSONObject();

                    jsonObject1.put("name",text1);

                    if(isEdit) {
                        jsonObject1.put("type", 0);
                        jsonObject1.put("position",position);
                    }else {
                        jsonObject1.put("type",1);
                    }

                    if(img!=null) {
                        jsonObject1.put("img", img);
                    }else{
                        jsonObject1.put("img","");
                    }

                    Integer storage=0;
                    Double cost=0.0;
                    Double retailprice=0.0;
                    for(Map.Entry<Integer,org.json.JSONObject> map:itemMap.entrySet()) {

                        JSONObject jsonObject11=map.getValue();

                        try {
                            jsonObject11.put("number","1");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            int amount=jsonObject11.getInt("AMOUNT");
                             storage= jsonObject11.getInt("storage")/amount;
                             cost+=jsonObject11.getDouble("cost")*amount;
                            retailprice+=jsonObject11.getDouble("retailprice")*amount;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    jsonObject1.put("storage",storage);
                    jsonObject1.put("cost",cost);
                    jsonObject1.put("retailprice",retailprice);

                    Intent intent = new Intent();
                    intent.putExtra("value", jsonObject1.toString());
                    setResult(Activity.RESULT_OK, intent);
                    finish();

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    private ArrayList<SectionItem> addContents(ArrayList<SectionItem> contents,JSONObject jsonObject) throws JSONException {


        JSONArray records = jsonObject.getJSONArray("result");

        for (int i = 0; i < records.length()-1; i++) {

            int order=i+1;

            JSONObject jsonObject1 = (JSONObject) records.get(i);
            jsonObject1.put("order",order);
            jsonObject1.put("id",jsonObject1.getInt("MATERIAL_ID"));
            String s=StringToHtml(jsonObject1);

            itemMap.put(order,jsonObject1);

            contents.add(new SectionItem(s));
        }

        return contents;
    }

    private ArrayList<SectionItem> addContentsNew(ArrayList<SectionItem> contents) throws JSONException {

        Map<Integer,JSONObject> itemMap2=new HashMap<>();

        int i=1;

        for(Map.Entry<Integer,org.json.JSONObject> map:itemMap.entrySet()){
            JSONObject jsonObject1=map.getValue();
            jsonObject1.put("order",String.valueOf(i));
            String s=StringToHtml(jsonObject1);
            contents.add(new SectionItem(s));
            itemMap2.put(i,jsonObject1);
            i++;
        }

        itemMap=itemMap2;

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
        String name = jsonObject.getString("NAME");
        Integer amount = jsonObject.getInt("AMOUNT");
       /* String img=jsonObject.getString("img");
        if(img!=null&&!img.equals(""))
            img="<img src='http://qiniu.lzxlzc.com/compress/"+img+"'/>";*/
        String s = ""+first+"&nbsp;&nbsp;<big><font size='20'><b>" + name + "</b></font></big>" +
                "<block quote>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;数量:"+amount;
        return s;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("requestCode===="+requestCode+"         resultCode==="+resultCode);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }else if(requestCode==REQUEST_PHOTO){
            System.out.println("REQUEST_PHOTO");
            Bitmap bitmap=TakePictures.getScaledBitmap(null,null);
            updatePhotoView(bitmap);
            String key= UUID.randomUUID().toString()+".jpg";
            img=key;
            new UpFileToQiniu(key,getApplicationContext());

        }else if(requestCode==1){
            try {

                JSONArray jsonArray=new JSONArray(data.getStringExtra("value"));
                ArrayList<SectionItem> contents = new ArrayList<>();
                int count=mAdapter.getItemCount()+1;
                for(int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject= (JSONObject) jsonArray.get(i);
                    itemMap.put(count+i,jsonObject);
                    jsonObject.put("order", String.valueOf(count+i));
                    jsonObject.put("NAME",jsonObject.get("name"));
                    jsonObject.put("AMOUNT","1");
                    itemMap.put(count+i,jsonObject);
                }

                initDataNew();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void updatePhotoView(String url){

        Picasso.with(getApplicationContext()).load(url).into(mPhotoView);

    }

    private void updatePhotoView(Bitmap bitmap0){
        ViewTreeObserver observer=mPhotoView.getViewTreeObserver();
        Log.i("tag sdkint=", String.valueOf(Build.VERSION.SDK_INT));
        final Bitmap bitmap=bitmap0;
        if(Build.VERSION.SDK_INT > 18) {

            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                int w,h;
                @Override
                public void onGlobalLayout() {

                    mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    w=mPhotoView.getMeasuredWidth();
                    h=mPhotoView.getMeasuredHeight();
                    int width=bitmap.getWidth();
                    int height=bitmap.getHeight()*w/width;
                    mPhotoView.setScaleType(ImageView.ScaleType.FIT_XY);
                    mPhotoView.setLayoutParams(new LinearLayout.LayoutParams(w,height));
                    mPhotoView.setImageBitmap(bitmap);

                }
            });
        }else {
            mPhotoView.setImageBitmap(bitmap);
        }
    }


}
