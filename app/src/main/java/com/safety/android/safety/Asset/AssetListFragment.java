package com.safety.android.safety.Asset;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;

/**
 * Created by WangJing on 2018/5/15.
 */

public class AssetListFragment extends Fragment{
/*
    @BindView(R.id.topbar)
    private QMUITopBar mTopBar;
    @BindView(R.id.listview_contact)
    private ListView mListView_contact;
*/
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.simple_list_item,container,false);

        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        ButterKnife.bind(this, view);
        //mTopBar=view.findViewById(R.id.topbar);

        //mListView_contact=view.findViewById(R.id.listview_contact);

        initTopBar();
        //初始化列表
        initListView();

        return  view;

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

        //  mTopBar.setTitle("ddddddddddddd");

       //   mListView_contact.setAdapter(new ArrayAdapter<String >(this.getContext(),
          //      R.layout.simple_list_item_1,R.id.textview_username,listItems));
/*
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

        SimpleAdapter simpleAdapter = new SimpleAdapter(this.getContext(),
                mapList, R.layout.simple_list_item,
                new String[]{"username","phonenum","gender"},
                new int[]{R.id.textview_username});
        mListView_contact.setAdapter(simpleAdapter);*/

    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... params) {

            String url= FlickrFetch.base+"/hiddenCheck/hiddenCheck/list?pageNo=1&pageSize=10";

            return  new OKHttpFetch(getActivity()).get(url);

        }

        @Override
        protected void onPostExecute(String items) {

        }
    }

}
