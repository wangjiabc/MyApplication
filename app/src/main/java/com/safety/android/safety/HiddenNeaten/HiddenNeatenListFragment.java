package com.safety.android.safety.HiddenNeaten;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.qmuiteam.qmui.widget.section.QMUISection;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionLayout;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.qmuidemo.view.BaseFragment;
import com.safety.android.qmuidemo.view.QDDataManager;
import com.safety.android.qmuidemo.view.QDListSectionAdapter;
import com.safety.android.qmuidemo.view.QDListWithDecorationSectionAdapter;
import com.safety.android.qmuidemo.view.SectionHeader;
import com.safety.android.qmuidemo.view.SectionItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class HiddenNeatenListFragment extends BaseFragment {
    QMUIPullRefreshLayout mPullRefreshLayout;

    QMUIStickySectionLayout mSectionLayout;

    QMUITopBarLayout mTopBar;

    private RecyclerView.LayoutManager mLayoutManager;
    protected QMUIStickySectionAdapter<SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> mAdapter;

    private int page=1;

    private int total=0;

    private BlockingQueue<ArrayList<SectionItem>> queue;

    private View view;

    private SearchView mSearchView;

    @Override
    protected View onCreateView() {
        view = LayoutInflater.from(getContext()).inflate(R.layout.simple_list_item, null);

        mPullRefreshLayout=view.findViewById(R.id.pull_to_refresh);
        mSectionLayout=view.findViewById(R.id.section_layout);
        mTopBar.findViewById(R.id.toolbar);

        initRefreshLayout();
        initStickyLayout();
        initData();

        queue=new ArrayBlockingQueue<>(3);

        return view;
    }


    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.setTitle(QDDataManager.getInstance().getDescription(this.getClass()).getName());

        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showBottomSheet();
                    }
                });
    }


    private boolean checkStateLoss(String logName) {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager == null) {
            QMUILog.d("", logName + " can not be invoked because fragmentManager == null");
            return false;
        }
        if (fragmentManager.isStateSaved()) {
            QMUILog.d("", logName + " can not be invoked after onSaveInstanceState");
            return false;
        }
        return true;
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

                        page=1;
                        total=0;

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

        final LinearLayoutManager layoutManager =new LinearLayoutManager(getContext()) {
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
                                String json = new OKHttpFetch(getContext()).get(FlickrFetch.base + "/food/material/list?pageNo=" + page + "&pageSize=10");

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

                        System.out.println("total="+total+"   page="+page);
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
                Toast.makeText(getContext(), "click item " + position, Toast.LENGTH_SHORT).show();
                try {
                    String text = (String) ((TextView) holder.itemView).getText();

                    holder.itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.qmui_config_color_gray_4));

                    Log.d("ddddddddd", "onItemClick: " + text + "  " + holder.getAdapterPosition());
                }catch (java.lang.ClassCastException e){

                }
            }

            @Override
            public boolean onItemLongClick(QMUIStickySectionAdapter.ViewHolder holder, int position) {
                Toast.makeText(getContext(), "long click item " + position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mSectionLayout.setAdapter(mAdapter, true);

        new FetchItemsTask().execute();


    }


    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            return new OKHttpFetch(getContext()).get(FlickrFetch.base+"/food/material/list?pageNo="+page+"&pageSize=10");
        }


        @Override
        protected void onPostExecute(String json) {

            ArrayList<QMUISection<SectionHeader, SectionItem>> list = new ArrayList<>();

            ArrayList<SectionItem> contents = new ArrayList<>();

            try {

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

    private void showBottomSheet() {
        new QMUIBottomSheet.BottomListSheetBuilder(getContext())
                .addItem("test scroll to section header")
                .addItem("test scroll to section item")
                .addItem("test find position")
                .addItem("test find custom position")
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        switch (position) {
                            case 0: {
                                QMUISection<SectionHeader, SectionItem> section = mAdapter.getSectionDirectly(3);
                                if (section != null) {
                                    mAdapter.scrollToSectionHeader(section, true);
                                }
                                break;
                            }
                            case 1: {
                                QMUISection<SectionHeader, SectionItem> section = mAdapter.getSectionDirectly(3);
                                if (section != null) {
                                    SectionItem item = section.getItemAt(10);
                                    if (item != null) {
                                        mAdapter.scrollToSectionItem(section, item, true);
                                    }
                                }
                                break;
                            }
                            case 2: {
                                int targetPosition = mAdapter.findPosition(new QMUIStickySectionAdapter.PositionFinder<SectionHeader, SectionItem>() {
                                    @Override
                                    public boolean find(@NonNull QMUISection<SectionHeader, SectionItem> section, @Nullable SectionItem item) {
                                        return "header 4".equals(section.getHeader().getText()) && (item != null && "item 13".equals(item.getText()));
                                    }
                                }, true);
                                if (targetPosition != RecyclerView.NO_POSITION) {
                                    Toast.makeText(getContext(), "find position: " + targetPosition, Toast.LENGTH_SHORT).show();
                                    QMUISection<SectionHeader, SectionItem> section = mAdapter.getSection(targetPosition);
                                    SectionItem item = mAdapter.getSectionItem(targetPosition);
                                    if (item != null) {
                                        mAdapter.scrollToSectionItem(section, item, true);
                                    } else if (section != null) {
                                        mAdapter.scrollToSectionHeader(section, true);
                                    } else {
                                        mLayoutManager.scrollToPosition(targetPosition);
                                    }

                                } else {
                                    Toast.makeText(getContext(), "failed to find position", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                            case 3: {
                                int targetPosition = mAdapter.findCustomPosition(QMUISection.SECTION_INDEX_UNKNOWN, QDListWithDecorationSectionAdapter.ITEM_INDEX_LIST_FOOTER, false);
                                if (targetPosition != RecyclerView.NO_POSITION) {
                                    Toast.makeText(getContext(), "find position: " + targetPosition, Toast.LENGTH_SHORT).show();
                                    mLayoutManager.scrollToPosition(targetPosition);
                                }
                            }
                        }
                        dialog.dismiss();
                    }
                })
                .build().show();
    }

}
