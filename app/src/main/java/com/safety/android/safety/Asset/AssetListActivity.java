package com.safety.android.safety.Asset;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.qmuiteam.qmui.layout.QMUIPriorityLinearLayout;
import com.qmuiteam.qmui.widget.QMUIFloatLayout;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.qmuiteam.qmui.widget.section.QMUISection;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionLayout;
import com.safety.android.qmuidemo.view.QDListSectionAdapter;
import com.safety.android.qmuidemo.view.QDListWithDecorationSectionAdapter;
import com.safety.android.qmuidemo.view.SectionHeader;
import com.safety.android.qmuidemo.view.SectionItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AssetListActivity extends AppCompatActivity {

    //@BindView(R.id.topbar)
    //QMUITopBar mTopBar;
    //@BindView(R.id.listview_contact)
    ListView mListView_contact;


    QMUITopBarLayout mTopBar;

    QMUIPullRefreshLayout mPullRefreshLayout;

    QMUIStickySectionLayout mSectionLayout;

    private RecyclerView.LayoutManager mLayoutManager;
    protected QMUIStickySectionAdapter<SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //QMUIStatusBarHelper.translucent(this);

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.simple_list_item, null);
        //mListView_contact=view.findViewById(R.id.listview_contact);
        mTopBar=view.findViewById(R.id.toolbar);
        mPullRefreshLayout=view.findViewById(R.id.pull_to_refresh);
        mSectionLayout=view.findViewById(R.id.section_layout);
        //ButterKnife.bind(this, root);
        //初始化状态栏
        //initTopBar();
        //初始化列表
       // initListView();

//        initTopBar();
        initRefreshLayout();
        initStickyLayout();
        initData();

        setContentView(view);

    }


    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showBottomSheet();
                    }
                });
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
                           ArrayList<SectionItem> list = new ArrayList<>();
                            ArrayList<SectionItem> contents = new ArrayList<>();
                           int i=0;
                            for (i = 0; i < 10; i++) {
                               // list.add(new SectionItem("load more item hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh" + i));
                              //  contents.add(new SectionItem("item " + i));
                                QMUIPriorityLinearLayout qmuiPriorityLinearLayout=new QMUIPriorityLinearLayout(getApplicationContext());
                                QMUIFloatLayout qmuiFloatLayout=new QMUIFloatLayout(getApplicationContext());
                                list.add(new SectionItem("qmuiFloatLayout"));
                            }
                            mAdapter.finishLoadMore(section, list, loadMoreBefore, true);

                    }
                }, 1000);
            }

            @Override
            public void onItemClick(QMUIStickySectionAdapter.ViewHolder holder, int position) {
                Toast.makeText(getApplicationContext(), "click item " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onItemLongClick(QMUIStickySectionAdapter.ViewHolder holder, int position) {
                Toast.makeText(getApplicationContext(), "long click item " + position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        mSectionLayout.setAdapter(mAdapter, true);
        ArrayList<QMUISection<SectionHeader, SectionItem>> list = new ArrayList<>();
        /*for (int i = 0; i < 1; i++) {
            list.add(createSection("header " + i, false,i));
        }*/

        ArrayList<SectionItem> contents = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            QMUIPriorityLinearLayout qmuiPriorityLinearLayout=new QMUIPriorityLinearLayout(getApplicationContext());
            QMUIFloatLayout qmuiFloatLayout=new QMUIFloatLayout(getApplicationContext());
            TextView textView=new TextView(getApplicationContext());
            textView.setText("aaaaaaaaaaaaaaaa");
            qmuiFloatLayout.addView(textView);
            contents.add(new SectionItem(i+"qmuiFloatLayout"));
            Log.d("aaaa", "createSection: "+i);
        }
        SectionHeader header = new SectionHeader("1");
        QMUISection<SectionHeader, SectionItem> section = new QMUISection<>(header, contents, false);
        // if test load more, you can open the code
        section.setExistAfterDataToLoad(true);

        list.add(section);

        mAdapter.setData(list);
    }

    private QMUISection<SectionHeader, SectionItem> createSection(String headerText, boolean isFold,int n) {
        SectionHeader header = new SectionHeader(headerText);
        ArrayList<SectionItem> contents = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            //contents.add(new SectionItem("item " + i*n));
            Log.d("aaaa", "createSection: "+i);
        }
        QMUISection<SectionHeader, SectionItem> section = new QMUISection<>(header, contents, isFold);
        // if test load more, you can open the code
        section.setExistAfterDataToLoad(true);
//        section.setExistBeforeDataToLoad(true);
        return section;
    }


    private void showBottomSheet() {
        new QMUIBottomSheet.BottomListSheetBuilder(getApplicationContext())
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
                                    Toast.makeText(getApplicationContext(), "find position: " + targetPosition, Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(getApplicationContext(), "failed to find position", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                            case 3: {
                                int targetPosition = mAdapter.findCustomPosition(QMUISection.SECTION_INDEX_UNKNOWN, QDListWithDecorationSectionAdapter.ITEM_INDEX_LIST_FOOTER, false);
                                if (targetPosition != RecyclerView.NO_POSITION) {
                                    Toast.makeText(getApplicationContext(), "find position: " + targetPosition, Toast.LENGTH_SHORT).show();
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
