package com.safety.android.safety.HiddenNeaten;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HiddenNeatenListActivity extends AppCompatActivity {

    QMUIPullRefreshLayout mPullRefreshLayout;

    QMUIStickySectionLayout mSectionLayout;


    private RecyclerView.LayoutManager mLayoutManager;
    protected QMUIStickySectionAdapter<SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> mAdapter;

    private int page=1;

    private int total=0;

    private BlockingQueue<ArrayList<SectionItem>> queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.simple_list_item, null);

        mPullRefreshLayout=view.findViewById(R.id.pull_to_refresh);
        mSectionLayout=view.findViewById(R.id.section_layout);

        initRefreshLayout();
        initStickyLayout();
        initData();

        queue=new ArrayBlockingQueue<>(3);

        setContentView(view);

    }


    private void initRefreshLayout() {
        mPullRefreshLayout.setOnPullListener(new QMUIPullRefreshLayout.OnPullListener() {
            @Override
            public void onMoveTarget(int offset) {

            }

            @Override
            public void onMoveRefreshView(int offset) {
                    mLayoutManager = createLayoutManager();
                    mSectionLayout.setLayoutManager(mLayoutManager);
                    page=1;
                    total=0;
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
                               String json = new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/food/material/list?pageNo=" + page + "&pageSize=10");

                               try {
                                   JSONObject jsonObject = new JSONObject(json);
                                   String success = jsonObject.optString("success", null);

                                   if (success.equals("true")) {

                                       JSONObject result = (JSONObject) jsonObject.get("result");

                                       JSONArray records = result.getJSONArray("records");

                                       for (int i = 0; i < records.length(); i++) {

                                           JSONObject jsonObject1 = (JSONObject) records.get(i);
                                           String name = jsonObject1.getString("name");
                                           contents.add(new SectionItem(name));
                                       }
                                   }
                                   queue.put(contents);
                               } catch (JSONException | InterruptedException e) {
                                   e.printStackTrace();
                               }

                           }

                       }).start();

                       ArrayList<SectionItem> list= new ArrayList<>();

                       int i=0;
                        ArrayList<SectionItem> contents = null;
                        try {
                            contents =queue.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        boolean existMoreData=true;
<<<<<<< HEAD
                        System.out.println("total="+total+"   page="+page);
                        if(total<(page*10)) {
                            existMoreData=false;
                        }

                        page++;
=======
                        /*if(Integer.parseInt(total)>(page*10)) {
                            existMoreData=false;
                        }*/
>>>>>>> origin/master

                        mAdapter.finishLoadMore(section, contents, loadMoreBefore, existMoreData);



                       //mAdapter.finishLoadMore(section, list, loadMoreBefore, true);



                    }
                }, 1000);
            }

            @Override
            public void onItemClick(QMUIStickySectionAdapter.ViewHolder holder, int position) {
                Toast.makeText(getApplicationContext(), "click item " + position, Toast.LENGTH_SHORT).show();
                try {
                    String text = (String) ((TextView) holder.itemView).getText();

                    //holder.itemView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.qmui_config_color_gray_4));
                    final Spanned sp = Html.fromHtml("<font color='red' size='20'>" + text + "</font>", null, null);
                    ((TextView) holder.itemView).setText(sp);
                    Log.d("ddddddddd", "onItemClick: " + text + "  " + holder.getAdapterPosition());
                }catch (java.lang.ClassCastException e){

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


    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base+"/food/material/list?pageNo="+page+"&pageSize=10");
        }


        @Override
        protected void onPostExecute(String json) {

            ArrayList<QMUISection<SectionHeader, SectionItem>> list = new ArrayList<>();

            ArrayList<SectionItem> contents = new ArrayList<>();

            try {

<<<<<<< HEAD
=======
                System.out.println("json="+json);

                if(json==null||json.equals("")){

                }

>>>>>>> origin/master
                JSONObject jsonObject = new JSONObject(json);
                String success = jsonObject.optString("success", null);

                if(success.equals("true")){

                    JSONObject result= (JSONObject) jsonObject.get("result");

                    JSONArray records = result.getJSONArray("records");

                    total=result.getInt("total");

                    for (int i = 0; i < records.length(); i++) {

                        JSONObject jsonObject1= (JSONObject) records.get(i);
                        String name=jsonObject1.getString("name");
                        contents.add(new SectionItem(name));
                    }

                    String total=result.getString("total");

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


}
