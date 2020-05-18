package com.safety.android.HiddenNeaten;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.qmuiteam.qmui.widget.section.QMUISection;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionLayout;
import com.safety.android.MainActivity;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.qmuidemo.view.HtmlImageGetter;
import com.safety.android.qmuidemo.view.QDListSectionAdapter;
import com.safety.android.qmuidemo.view.SectionHeader;
import com.safety.android.qmuidemo.view.SectionItem;
import com.safety.android.qmuidemo.view.getGradientDrawable;
import com.safety.android.tools.MyTestUtil;
import com.safety.android.tools.TakePictures;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.safety.android.MainActivity.dataUrl;
import static com.safety.android.tools.TakePictures.REQUEST_PHOTO;

public class HiddenNeatenListActivity extends AppCompatActivity {

    QMUIPullRefreshLayout mPullRefreshLayout;

    QMUIStickySectionLayout mSectionLayout;

    private RecyclerView.LayoutManager mLayoutManager;
    protected QMUIStickySectionAdapter<SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> mAdapter;

    private int page=1;

    private int total=0;

    private BlockingQueue<ArrayList<SectionItem>> queue;

    private View view;

    private SearchView mSearchView;

    private String search="";

    private Map<Integer,JSONObject> itemMap=new HashMap<>();

    private Map<Integer,JSONObject> selectMap=new HashMap();

    private String imagePath;

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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.add(Menu.NONE, Menu.FIRST + 1, 5, "删除").setIcon(android.R.drawable.ic_menu_delete);
        // setIcon()方法为菜单设置图标，这里使用的是系统自带的图标，同学们留意一下,以
        // android.R开头的资源是系统提供的，我们自己提供的资源是以R开头的
        menu.add(Menu.NONE, Menu.FIRST + 2, 2, "保存").setIcon(android.R.drawable.ic_menu_edit);
        menu.add(Menu.NONE, Menu.FIRST + 3, 6, "帮助").setIcon(android.R.drawable.ic_menu_help);
        menu.add(Menu.NONE, Menu.FIRST + 4, 1, "添加").setIcon(android.R.drawable.ic_menu_add);
        menu.add(Menu.NONE, Menu.FIRST + 5, 4, "详细").setIcon(android.R.drawable.ic_menu_info_details);
        menu.add(Menu.NONE, Menu.FIRST + 6, 3, "发送").setIcon(android.R.drawable.ic_menu_send);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("click");
        switch (item.getItemId()) {
            case Menu.FIRST + 1:
                Toast.makeText(this, "删除菜单被点击了", Toast.LENGTH_LONG).show();
                break;
            case Menu.FIRST + 2:
                UUID uuid=UUID.randomUUID();
                imagePath=MainActivity.dataUrl+"/image/"+uuid+".jpg";
                File file=new File(MainActivity.dataUrl+"/image/",uuid+".jpg");
                Uri uri=getImageContentUri(file);
                Intent captureImage=TakePictures.getCaptureImage(uri);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 100);
                    startActivityForResult(captureImage, REQUEST_PHOTO);
                }else{
                    startActivityForResult(captureImage, REQUEST_PHOTO);
                }
                Toast.makeText(this, "保存菜单被点击了", Toast.LENGTH_LONG).show();
                break;
            case Menu.FIRST + 3:
                Toast.makeText(this, "帮助菜单被点击了", Toast.LENGTH_LONG).show();
                break;
            case Menu.FIRST + 4:
                Toast.makeText(this, "添加菜单被点击了", Toast.LENGTH_LONG).show();
                break;
            case Menu.FIRST + 5:
                Toast.makeText(this, "详细菜单被点击了", Toast.LENGTH_LONG).show();
                break;
            case Menu.FIRST + 6:
                Toast.makeText(this, "发送菜单被点击了", Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_item_add:
                Toast.makeText(this, "添加被点击了", Toast.LENGTH_LONG).show();
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
                               String json = new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/food/material/list?pageNo=" + page + "&pageSize=10"+cSearch);

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
            public void onItemClick(QMUIStickySectionAdapter.ViewHolder holder, int position) {
                Toast.makeText(getApplicationContext(), "click item " + position, Toast.LENGTH_SHORT).show();
                try {

                    JSONObject jsonObject=null;

                    String s="";

                    jsonObject=selectMap.get(holder.getAdapterPosition());
                    if(jsonObject==null){
                        jsonObject=itemMap.get(holder.getAdapterPosition());
                        s=StringToHtml2(jsonObject);
                        selectMap.put(holder.getAdapterPosition(),jsonObject);
                    }else{
                        s=StringToHtml(jsonObject);
                        selectMap.remove(holder.getAdapterPosition());
                    }

                    Drawable defaultDrawable = new getGradientDrawable(Color.YELLOW,100).getGradientDrawable();
                    final Html.ImageGetter imgGetter = new HtmlImageGetter((TextView) holder.itemView, dataUrl, defaultDrawable);


                    final Spanned sp = Html.fromHtml(s,Html.FROM_HTML_MODE_COMPACT, imgGetter,null);
                    ((TextView) holder.itemView).setText(sp);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("requestCode===="+requestCode+"         resultCode==="+resultCode);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }else if(requestCode==REQUEST_PHOTO){
            System.out.println("REQUEST_PHOTO");
            Bitmap bitmap=TakePictures.getScaledBitmap(imagePath,null,null);
            MyTestUtil.print(bitmap);

            String json=new OKHttpFetch(getApplicationContext()).get("http://203.0.104.65:8080/a/test/token.do");

            String token="";
            try {
                JSONObject jsonObject = new JSONObject(json);
                token=jsonObject.getString("token");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            UploadManager uploadManager=new UploadManager();

            File file=new File(imagePath);

            String key=UUID.randomUUID().toString()+".jpg";
            System.out.println("key========="+key+ "     token"+token);
            uploadManager.put(file, key, token,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if(info.isOK()) {
                                Log.i("qiniu", "Upload Success");
                            } else {
                                Log.i("qiniu", "Upload Fail");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);

        }

    }

    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            String cSearch="";
            if(search!=null&&!search.equals(""))
                cSearch=search;
            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/food/material/list?pageNo=" + page + "&pageSize=10"+cSearch);
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
        String name = jsonObject.getString("name");
        Integer storage = jsonObject.getInt("storage");
        Integer cost = jsonObject.getInt("cost");
        String s = "<span>"+order+"<img src='http://pic004.cnblogs.com/news/201211/20121108_091749_1.jpg'/></span>&nbsp;&nbsp;<span display='inline-block' width='60px'><font color='red' size='20'>" + name + "</font></span>&nbsp;&nbsp;<span>" + storage + "</span>&nbsp;&nbsp;<span>" + cost + "</span>";
        return s;
    }

    private String StringToHtml2(JSONObject jsonObject) throws JSONException {
        Integer order=jsonObject.getInt("order");
        String name = jsonObject.getString("name");
        Integer storage = jsonObject.getInt("storage");
        Integer cost = jsonObject.getInt("cost");
        String s = "<span>"+order+"<img src='http://pic004.cnblogs.com/news/201211/20121108_091749_1.jpg'/></span>&nbsp;&nbsp;<span><font color='red' size='20'>" + name + "</font></span>&nbsp;&nbsp;<span><font color='red' size='20'>" + storage + "</font></span>&nbsp;&nbsp;<span><font color='red' size='20'>" + cost + "</font></span>";
        return s;
    }


    public Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

}
