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
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HiddenNeatenListActivity extends AppCompatActivity {

    QMUIPullRefreshLayout mPullRefreshLayout;

    QMUIStickySectionLayout mSectionLayout;


    private RecyclerView.LayoutManager mLayoutManager;
    protected QMUIStickySectionAdapter<SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> mAdapter;

    private int page=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.simple_list_item, null);

        mPullRefreshLayout=view.findViewById(R.id.pull_to_refresh);
        mSectionLayout=view.findViewById(R.id.section_layout);

        initRefreshLayout();
        initStickyLayout();
        initData();

        setContentView(view);

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
                       /* ArrayList<SectionItem> list = new ArrayList<>();
                        ArrayList<SectionItem> contents = new ArrayList<>();
                        int i=0;
                        for (i = 0; i < 10; i++) {
                            list.add(new SectionItem("load more item hhhhhhhhhhh" + (i+page*10)));
                            //  contents.add(new SectionItem("item " + i));
                            //list.add(new SectionItem("qmuiFloatLayout"));
                            // list.add(new SectionItem(qmuiPriorityLinearLayout));
                        }
                        page++;*/

                        boolean existMoreData=true;
                        if(Integer.parseInt(total)>(page*10)) {
                            existMoreData=false;
                        }

                       mAdapter.finishLoadMore(section, list, loadMoreBefore, true);

                        new FetchItemsTask().execute();

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

                System.out.println("json="+json);

                JSONObject jsonObject = new JSONObject(json);
                String success = jsonObject.optString("success", null);

                if(success.equals("true")){

                    JSONObject result= (JSONObject) jsonObject.get("result");

                    JSONArray records = result.getJSONArray("records");

                    for (int i = 0; i < records.length(); i++) {

                        JSONObject jsonObject1= (JSONObject) records.get(i);
                        String name=jsonObject1.getString("name");
                        contents.add(new SectionItem(name));
                        Log.d("aaaa", "createSection: "+i);
                    }

                    String total=result.getString("total");

                    SectionHeader header = new SectionHeader("共"+total+"条");
                    QMUISection<SectionHeader, SectionItem> section = new QMUISection<>(header, contents, false);
                    // if test load more, you can open the code

                    list.add(section);

                   /* if(Integer.parseInt(total)>(page*10)) {
                        section.setExistAfterDataToLoad(true);
                    }else{
                        section.setExistAfterDataToLoad(false);
                    }*/

                    section.setExistAfterDataToLoad(true);
                    System.out.println("page="+page);

                    if(page==1) {
                        mAdapter.setData(list);
                    }else {


                        System.out.println("Integer.parseInt(total)>(page*10)="+(Integer.parseInt(total)>(page*10)));

                    }

                    page++;

                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }


}
