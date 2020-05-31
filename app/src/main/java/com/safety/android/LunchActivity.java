package com.safety.android;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.safety.android.SQLite3.UserInfo;
import com.safety.android.SQLite3.UserLab;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.http.login;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

import static java.lang.Thread.sleep;

public class LunchActivity extends AppCompatActivity {

    public String token;

    public static Map sysUser=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        List<UserInfo> list= UserLab.get(getApplication()).getUserInfo();

        try {
            if(list.size()>0) {
                UserInfo userInfo = list.get(0);
                token = userInfo.getToken();
                Log.d("user====",userInfo.toString());
            }else {

            }
        }catch (Exception e) {

            e.printStackTrace();

            new Thread( new Runnable( ) {
                @Override
                public void run() {
                    //耗时任务，比如加载网络数据
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 这里可以睡几秒钟，如果要放广告的话
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(getApplicationContext(), login.class);
                            startActivity(intent);
                            LunchActivity.this.finish();
                        }
                    });
                }
            } ).start();

        }

        new FetchItemsTask().execute();

    }


    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            System.out.println("lunchtoken===="+token);

            return new OKHttpFetch(getApplication()).get(FlickrFetch.base+"/sys/user/getUserSectionInfoByToken?token="+token);
        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    JSONObject json=new JSONObject(items);

                    JSONObject jsonObject=json.getJSONObject("result");

                    String sysUserId=jsonObject.getString("sysUserId");
                    String sysUserCode=jsonObject.getString("sysUserCode");
                    String sysUserName=jsonObject.getString("sysUserName");
                    String sysOrgCode=jsonObject.getString("sysOrgCode");

                    sysUser=new HashMap();
                    sysUser.put("sysUserId", sysUserId);
                    sysUser.put("sysUserCode", sysUserCode); // 当前登录用户登录账号
                    sysUser.put("sysUserName", sysUserName); // 当前登录用户真实名称
                    sysUser.put("sysOrgCode", sysOrgCode); // 当前登录用户部门编号

                    Intent intent = new Intent(getApplication(), MainActivity.class);
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();

                    Intent intent = new Intent(getApplicationContext(), login.class);
                    startActivity(intent);
                    LunchActivity.this.finish();
                }

            }else {

                Toast.makeText(getApplication(),"登陆失败",Toast.LENGTH_SHORT).show();

            }

        }

    }



}
