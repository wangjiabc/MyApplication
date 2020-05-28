package com.safety.android.Food;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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

    private Map<Integer,JSONObject> itemMap=new HashMap<>();

    private RecyclerView.LayoutManager mLayoutManager;
    protected QMUIStickySectionAdapter<SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

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
                position=jsonObject.getInt("position");
                String name = jsonObject.getString("name");
                Double cost = jsonObject.getDouble("cost");
                Double retailprice = jsonObject.getDouble("retailprice");
                String remark = jsonObject.getString("remark");
                String thisImg=jsonObject.getString("img");
                if(thisImg!=null)
                    updatePhotoView("http://qiniu.lzxlzc.com/"+thisImg);

                editText1.setText(name);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        initRefreshLayout();
        initStickyLayout();
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
        return new QDListSectionAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("click");
        switch (item.getItemId()) {
            case R.id.menu_food_compages_add:
                Toast.makeText(this, "删除菜单被点击了", Toast.LENGTH_LONG).show();
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

                    new AlertDialog.Builder(FoodCompagesActivity.this)
                            .setTitle("删除"+finalJsonObject.getString("NAME")+"?")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).setNegativeButton("删除", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        itemMap.remove(n);

                                        initDataNew();

                                    }
                    }).create().show();

                }catch (ClassCastException | JSONException e){
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

                    System.out.println("n======"+n);

                    MyTestUtil.print(itemMap);

                    new AlertDialog.Builder(FoodCompagesActivity.this)
                            .setTitle("删除"+finalJsonObject.getString("NAME")+"?")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).setNegativeButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            itemMap.remove(n);

                            initDataNew();

                        }
                    }).create().show();

                }catch (ClassCastException | JSONException e){
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

    private ArrayList<SectionItem> addContents(ArrayList<SectionItem> contents,JSONObject jsonObject) throws JSONException {


        JSONArray records = jsonObject.getJSONArray("result");

        for (int i = 0; i < records.length()-1; i++) {

            int order=i+1;

            JSONObject jsonObject1 = (JSONObject) records.get(i);
            jsonObject1.put("order",order);
            String s=StringToHtml(jsonObject1);
            SpannableString spannableString = new SpannableString(s);

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
            String s=StringToHtml(jsonObject1);
            itemMap2.put(i,jsonObject1);
            i++;
        }

        MyTestUtil.print(itemMap);

        itemMap=itemMap2;

        MyTestUtil.print(itemMap2);

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
