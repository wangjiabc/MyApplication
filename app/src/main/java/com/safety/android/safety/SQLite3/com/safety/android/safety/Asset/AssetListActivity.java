package com.safety.android.safety.SQLite3.com.safety.android.safety.Asset;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

public class AssetListActivity extends AppCompatActivity {

    //@BindView(R.id.topbar)
    //QMUITopBar mTopBar;
    //@BindView(R.id.listview_contact)
    ListView mListView_contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        QMUIStatusBarHelper.translucent(this);

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.simple_list_item, null);
//        mListView_contact=view.findViewById(R.id.listview_contact);
        //ButterKnife.bind(this, root);
        //初始化状态栏
        //initTopBar();
        //初始化列表
        initListView();

        setContentView(view);

    }

    private void initTopBar() {
    }

    private void initListView() {
        String[] listItems = new String[]{
                "舒淇", "周杰伦", "古天乐", "姚明", "刘德华", "谢霆锋",
                "朱时茂", "朱军", "周迅", "赵忠祥", "赵薇", "张国立",
                "赵本山", "章子怡", "张艺谋", "张卫健", "张铁林", "袁泉",
                "彭丽媛", "杨丽萍", "杨澜", "汪峰", "刘仪伟", "孙楠",
                "宋祖英", "斯琴高娃", "撒贝宁", "秦海璐", "任泉", "周杰"
        };

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
                View vi=null;  //定义一个View
                if (convertView==null)  //如果缓存为空，我们生成新的布局作为1个item
                {
                    Log.i("info:","没有缓存，重新生成"+position);
                    LayoutInflater inflater=LayoutInflater.from(getApplication());

                    //因为getView()返回的对象，adapter会自动赋给ListView,  R.layout.listview_item_layout是布局文件
                    vi=inflater.inflate(R.layout.simple_list_item_1, null);
                }
                else
                {
                    Log.i("info:","有缓存"+position);
                    vi=convertView;
                }
                //获取集合
                //找到item里面的所有控件绑定数据
                TextView name=(TextView)vi.findViewById(R.id.textview_username);  //名字
                name.setText("ddddddddd");//赋值

                TextView name2=(TextView) vi.findViewById(R.id.textview_phone);
                name2.setText("222222222");
                return vi; //最后返回
            }
        });

    }

}
