package com.safety.android.safety.HiddenCheck;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.myapplication.R;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;
import com.safety.android.qmuidemo.view.SectionHeader;
import com.safety.android.qmuidemo.view.SectionItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by WangJing on 2018/5/15.
 */

public class HiddenCheckListActivity extends AppCompatActivity {

    ListView mListView_contact;


    QMUITopBarLayout mTopBar;

    QMUIPullRefreshLayout mPullRefreshLayout;

    private CheckBox cbEat;

    private RecyclerView.LayoutManager mLayoutManager;
    protected QMUIStickySectionAdapter<SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //QMUIStatusBarHelper.translucent(this);

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.simple_list_item1, null);

         mListView_contact=view.findViewById(R.id.listview_contact);
        mTopBar=view.findViewById(R.id.toolbar);
        mPullRefreshLayout=view.findViewById(R.id.pull_to_refresh);

        initListView();
        initRefreshLayout();
        setContentView(view);

    }


    private void initRefreshLayout() {
        mPullRefreshLayout.setOnPullListener(new QMUIPullRefreshLayout.OnPullListener() {
            @Override
            public void onMoveTarget(int offset) {
                Log.d("aaa","ddddddddddddddddd");
            }

            @Override
            public void onMoveRefreshView(int offset) {
                Log.d("TAG", "onMoveRefreshView: ");
            }

            @Override
            public void onRefresh() {
                Log.d("dddddddddddddd","bbbbbbbbbbbbbbbbb");
                mPullRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullRefreshLayout.finishRefresh();

                    }
                }, 2000);
            }
        });
    }

    private void initListView() {
        /*
        mListView_contact.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 20;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View vi = null;  //定义一个View
                if (convertView == null)  //如果缓存为空，我们生成新的布局作为1个item
                {
                    Log.i("info:", "没有缓存，重新生成" + position);
                    LayoutInflater inflater = LayoutInflater.from(getApplication());

                    //因为getView()返回的对象，adapter会自动赋给ListView,  R.layout.listview_item_layout是布局文件
                    vi = inflater.inflate(R.layout.simple_list_item_1, null);
                } else {
                    Log.i("info:", "有缓存" + position);
                    vi = convertView;
                }
                //获取集合
                //找到item里面的所有控件绑定数据
                TextView name = (TextView) vi.findViewById(R.id.textview_username);  //名字
                name.setText("ddddddddd");//赋值

                TextView name2 = (TextView) vi.findViewById(R.id.textview_phone);
                name2.setText("222222222");
                return vi; //最后返回
            }
        });*/

        String[] listItems = new String[]{
                "舒淇", "周杰伦", "古天乐", "姚明", "刘德华", "谢霆锋",
                "朱时茂", "朱军", "周迅", "赵忠祥", "赵薇", "张国立",
                "赵本山", "章子怡", "张艺谋", "张卫健", "张铁林", "袁泉",
                "彭丽媛", "杨丽萍", "杨澜", "汪峰", "刘仪伟", "孙楠",
                "宋祖英", "斯琴高娃", "撒贝宁", "秦海璐", "任泉", "周杰"
        };

        String[] phoneNums = new String[]{
                "13179209683", "13943301263", "13801006699", "13801010011", "13843835268", "13500943531",
                "13901221763", "13801548319", "13901182547", "13801396586", "13701829837", "13901158765",
                "13804095455", "13701106599", "13901008970", "13701157640", "13901363764", "13701316513",
                "13901036569", "13901359812", "13901953423", "13501298629", "13901374090", "13901156769",
                "13901223748", "13601264347", "13701065983", "13801652345", "13901875792", "13901189898"
        };
        String[] genders = new String[]{
                "女", "男", "男", "男", "男", "男",
                "男", "男", "女", "男", "女", "男",
                "男", "女", "男", "男", "男", "女",
                "女", "女", "女", "男", "男", "男",
                "女", "女", "男", "女", "男", "男"
        };

        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listItems.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("username", listItems[i]);
            map.put("phonenum", phoneNums[i]);
            map.put("gender", genders[i]);
            mapList.add(map);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(HiddenCheckListActivity.this,
                mapList, R.layout.simple_list_item_1,
                new String[]{"username","phonenum","gender"},
                new int[]{R.id.textview_username,R.id.textview_phone,R.id.textview_gender});
        mListView_contact.setAdapter(simpleAdapter);


        mListView_contact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(HiddenCheckListActivity.this, "点击的position是" + i + "，Id是" + l,
                        Toast.LENGTH_SHORT).show();
            }
        });

        mListView_contact.setAdapter(simpleAdapter);


    }

}
