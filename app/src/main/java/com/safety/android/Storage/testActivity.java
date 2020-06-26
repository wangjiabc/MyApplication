package com.safety.android.Storage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.R;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.qmuiteam.qmui.widget.section.QMUISection;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionLayout;
import com.safety.android.qmuidemo.view.QDGridSectionAdapter;
import com.safety.android.qmuidemo.view.SectionHeader;
import com.safety.android.qmuidemo.view.SectionItem;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class testActivity extends AppCompatActivity {

    QMUIPullRefreshLayout mPullRefreshLayout;

    QMUIStickySectionLayout mSectionLayout;

    private RecyclerView.LayoutManager mLayoutManager;
    protected QMUIStickySectionAdapter<SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> mAdapter;

    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.storage_list_item, null);

        mPullRefreshLayout=view.findViewById(R.id.pull_to_refresh);
        mSectionLayout=view.findViewById(R.id.section_layout);


        setContentView(view);

        initStickyLayout();
        mAdapter = createAdapter();
        mSectionLayout.setAdapter(mAdapter, true);
        ArrayList<QMUISection<SectionHeader, SectionItem>> list = new ArrayList<>();

        list.add(createSection("header " , false));

        mAdapter.setData(list);

    }

    protected QMUIStickySectionAdapter<
            SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> createAdapter() {
        return new QDListSectionAdapter();
    }

    protected void initStickyLayout() {
        mLayoutManager = createLayoutManager();
        mSectionLayout.setLayoutManager(mLayoutManager);
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

    private QMUISection<SectionHeader, SectionItem> createSection(String headerText, boolean isFold) {
        SectionHeader header = new SectionHeader(headerText);
        ArrayList<SectionItem> contents = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            contents.add(new SectionItem("item " + i));
        }
        QMUISection<SectionHeader, SectionItem> section = new QMUISection<>(header, contents, isFold);
        // 如果 section 的 item 存在加载更多的需求，可通过以下两个调用告诉 section 是否需要加载更多
        // section.setExistAfterDataToLoad(true);
        // section.setExistBeforeDataToLoad(true);
        return section;
    }


    class QDListSectionAdapter extends QDGridSectionAdapter {

        @NonNull
        @Override
        protected ViewHolder onCreateSectionItemViewHolder(@NonNull ViewGroup viewGroup) {


            View view1=LayoutInflater.from(getApplicationContext()).inflate(R.layout.qmui_grouplist_view, null);

            TextView id=view1.findViewById(R.id.id);
            id.setText("1");

            TextView tvApplicationName=view1.findViewById(R.id.tvApplicationName);
            tvApplicationName.setText("33333333333333ppppppppppppppppppppppppppppppppppppppppppppppppppppp");

            TextView tvRating=view1.findViewById(R.id.tvApplicationName3);
            tvRating.setText("Aaaaaaaaaaaaaaa");

            return new QMUIStickySectionAdapter.ViewHolder(view1);
        }
    }

}
