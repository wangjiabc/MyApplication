package com.safety.android;

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
import com.google.android.material.navigation.NavigationView;
import com.safety.android.SQLite3.UserInfo;
import com.safety.android.SQLite3.UserLab;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.http.login;
import com.safety.android.mqtt.connect.MqttClient;
import com.safety.android.mqtt.event.MessageEvent;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NotificationManager notificationManager ;
    private NotificationCompat.Builder notificationBuilder ;

    public static String token="";

    public static String dataUrl="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataUrl=getDiskCacheDir(getApplicationContext());

        getSupportFragmentManager().beginTransaction()              //添加fragment
                .add(R.id.activity_container_main, SafeBoxFragment.newInstance())
                .commit();

        notificationManager = (NotificationManager) getSystemService(getBaseContext().NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this,"default");
        MqttClient.getMqttAndroidClientInstace(getBaseContext());

        List<UserInfo> list= UserLab.get(getApplication()).getUserInfo();

        try {
            if(list.size()>0) {
                UserInfo userInfo = list.get(0);
                token = userInfo.getToken();
                Log.d("user====",userInfo.toString());
            }else {
                Intent intent=new Intent(getApplicationContext(), login.class);
               // startActivity(intent);
            }
        }catch (Exception e) {

            e.printStackTrace();
        }



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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_out) {

            new FetchItemsTask().execute();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

            return new OKHttpFetch(getApplication()).get(FlickrFetch.base+"/user/logout.do");
        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                Toast.makeText(getApplication(),"退出成功",Toast.LENGTH_SHORT).show();
            }

        }

    }

}
