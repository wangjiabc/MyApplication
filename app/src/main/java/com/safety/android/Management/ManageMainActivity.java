package com.safety.android.Management;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.myapplication.R;
import com.safety.android.SQLite3.UserInfo;
import com.safety.android.SQLite3.UserLab;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.http.login;
import com.safety.android.mqtt.connect.MqttClientService;
import com.safety.android.mqtt.event.MessageEvent;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class ManageMainActivity extends AppCompatActivity {

    private NotificationManager notificationManager ;
    private NotificationCompat.Builder notificationBuilder ;

    public static String token="";

    public static String dataUrl="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);

        dataUrl=getDiskCacheDir(getApplicationContext());

        getSupportFragmentManager().beginTransaction()              //添加fragment
                .add(R.id.activity_container_main, BoxFragment.newInstance())
                .commit();

        notificationManager = (NotificationManager) getSystemService(getBaseContext().NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this,"default");

        List<UserInfo> list= UserLab.get(getApplication()).getUserInfo();

        Iterator iterator=list.iterator();

        if(login.username!=null&&!login.username.equals("")) {
            while (iterator.hasNext()) {
                UserInfo userInfo = (UserInfo) iterator.next();
                if (userInfo.getName().equals(login.username)) {
                    token = userInfo.getToken();
                    System.out.println("username===" + login.username + "token==" + token);
                    continue;
                }
            }
        }else {
            try {
                if (list.size() > 0) {
                    UserInfo userInfo = list.get(0);
                    token = userInfo.getToken();
                } else {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        Intent intent = new Intent(this, MqttClientService.class);
        //开启服务兼容
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }




    private String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            //cachePath = context.getExternalCacheDir().getPath();
            cachePath = getExternalFilesDir("").getAbsolutePath();
        } else {
            //cachePath = context.getCacheDir().getPath();
            cachePath = getFilesDir().getAbsolutePath();
        }
        return cachePath;
    }

    @Override
    public void onBackPressed() {

            super.onBackPressed();

    }

    /* 利用反射机制调用MenuBuilder的setOptionalIconsVisible方法设置mOptionalIconsVisible为true，给菜单设置图标时才可见
     * 让菜单同时显示图标和文字
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.add(Menu.NONE, Menu.FIRST + 3, 3, "关于").setIcon(android.R.drawable.ic_menu_info_details);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        System.out.println("click");

        return super.onOptionsItemSelected(item);
    }



    /**
     * 运行在主线程
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        String string = event.getString();
        /**接收到服务器推送的信息*/
        if("".equals(string)){

            String topic = event.getTopic();
            MqttMessage mqttMessage = event.getMqttMessage();
            String s = new String(mqttMessage.getPayload());
            topic=topic+" : "+s;
            Log.d("tag MainActivity topic=",topic);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            return new OKHttpFetch(getApplication()).get(FlickrFetch.base+"/sys/logout");
        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    JSONObject jsonObject=new JSONObject(items);
                    token=null;
                    Toast.makeText(getApplication(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplication(), login.class);
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }

    }


}
