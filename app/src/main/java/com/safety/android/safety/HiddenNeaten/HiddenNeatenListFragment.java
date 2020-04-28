package com.safety.android.safety.HiddenNeaten;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.R;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;

import com.safety.android.safety.SQLite3.UserInfo;
import com.safety.android.safety.SQLite3.UserLab;

import java.util.List;

import androidx.fragment.app.Fragment;
import okhttp3.FormBody;

public class HiddenNeatenListFragment extends Fragment {
    TextView textView;

    TextView textView1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_assetlist, container, false);

        textView= (TextView) v.findViewById(R.id.tv_show);
        textView1= (TextView) v.findViewById(R.id.tv_show2);

        final Button btGet= (Button) v.findViewById(R.id.bt_meun);
        btGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FetchItemsTask2().execute();
            }
        });
        Button btPost= (Button) v.findViewById(R.id.bt_food);
        btPost.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new FetchItemsTask().execute();
            }
        });

        Button btPer= (Button) v.findViewById(R.id.bt_per);
        btPer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new FetchItemsTask3().execute();
            }
        });


        return v;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            FormBody formBody = new FormBody
                    .Builder()
                    .add("limit","1")//设置参数名称和参数值
                    .add("offset","0")
                    .build();

            return new OKHttpFetch(getActivity()).get(FlickrFetch.base+"/food/jshMaterial/list?pageNo=1&pageSize=10");
        }


        @Override
        protected void onPostExecute(String items) {
            textView.setText(items);
        }

    }

    private class FetchItemsTask2 extends AsyncTask<Void,Void,String> {

        List<UserInfo> list= UserLab.get(getContext()).getUserInfo();

        UserInfo userInfo = list.get(0);
        String token=userInfo.getToken();


        @Override
        protected String doInBackground(Void... params) {

            FormBody formBody = new FormBody
                    .Builder()
                    .add("limit", "1")//设置参数名称和参数值
                    .add("offset", "0")
                    .build();

            return new OKHttpFetch(getActivity()).get(FlickrFetch.base + "/sys/permission/getUserPermissionByToken?token="+token);
        }

        @Override
        protected void onPostExecute(String items) {
            textView.setText(items);
        }
    }

    private class FetchItemsTask3 extends AsyncTask<Void,Void,String> {

        List<UserInfo> list= UserLab.get(getContext()).getUserInfo();

        UserInfo userInfo = list.get(0);
        String token=userInfo.getToken();


        @Override
        protected String doInBackground(Void... params) {

            FormBody formBody = new FormBody
                    .Builder()
                    .add("limit", "1")//设置参数名称和参数值
                    .add("offset", "0")
                    .build();

            return new OKHttpFetch(getActivity()).get(FlickrFetch.base + "/sys/permission/list?token="+token);
        }

        @Override
        protected void onPostExecute(String items) {
            textView.setText(items);
        }
    }

}
