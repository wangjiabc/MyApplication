package com.safety.android;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.safety.android.SQLite3.UserInfo;
import com.safety.android.SQLite3.UserLab;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.mqtt.connect.MqttClient;
import com.safety.android.mqtt.event.MessageEvent;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity{

    private NotificationManager notificationManager ;
    private NotificationCompat.Builder notificationBuilder ;

    public static String token="";

    public static String dataUrl="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "修改密码").setIcon(android.R.drawable.ic_menu_delete);
        // setIcon()方法为菜单设置图标，这里使用的是系统自带的图标，同学们留意一下,以
        // android.R开头的资源是系统提供的，我们自己提供的资源是以R开头的
        menu.add(Menu.NONE, Menu.FIRST + 2, 2, "退出").setIcon(android.R.drawable.ic_menu_edit);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        System.out.println("click");
        switch (item.getItemId()) {
            case Menu.FIRST + 1:
                LayoutInflater inflater = getLayoutInflater();
                View validateView = inflater.inflate(
                        R.layout.dialog_validate, null);
                final LinearLayout layout_validate = (LinearLayout) validateView.findViewById(R.id.layout_validate);
                layout_validate.removeAllViews();
                final List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();


                Map<String,Object> map = new HashMap<String, Object>();
                View validateItem = inflater.inflate(R.layout.item_validate_enter2, null);
                validateItem.setTag(1);
                layout_validate.addView(validateItem);
                TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                TextView et_validateText=validateItem.findViewById(R.id.et_validate_text);

                tv_validateName.setText("原密码");

                map.put("name", tv_validateName);
                map.put("value", et_validate);

                list.add(map);

                tv_validateName.setText("新密码");

                map.put("name", tv_validateName);
                map.put("value", et_validate);

                list.add(map);

                tv_validateName.setText("确认新密码");

                map.put("name", tv_validateName);
                map.put("value", et_validate);

                list.add(map);


                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle("设置组合数量")
                        .setView(validateView)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                StringBuffer stringBuffer = new StringBuffer();

                                Map sysUser=LunchActivity.sysUser;

                                String username= (String) sysUser.get("sysUserName");

                                String oldpassword = ((EditText)list.get(0).get("value")).getText().toString();

                                String password = ((EditText)list.get(0).get("value")).getText().toString();

                                String confirmpassword = ((EditText)list.get(0).get("value")).getText().toString();

                                JSONObject jsonObject=new JSONObject();

                                try {
                                    jsonObject.put("username",username);
                                    jsonObject.put("oldpassword",oldpassword);
                                    jsonObject.put("password",password);
                                    jsonObject.put("confirmpassword",confirmpassword);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                new FetchItemsTaskUpPassWord().execute(jsonObject);

                                dialog.dismiss();
                            }

                        }).setNegativeButton("取消", new DialogInterface.OnClickListener()
                        {

                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();
                break;
            case Menu.FIRST + 2:
                new FetchItemsTask().execute();
                //Toast.makeText(this, "添加被点击了", Toast.LENGTH_LONG).show();
                break;
        }

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

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }

    }


    private class FetchItemsTaskUpPassWord extends AsyncTask<JSONObject,Void,String> {

        @Override
        protected String doInBackground(org.json.JSONObject... params) {

            return new OKHttpFetch(getApplication()).post(FlickrFetch.base+"/sys/user/updatePassword",params[0],"PUT");
        }


        @Override
        protected void onPostExecute(String items) {

            if(items!=null){
                try {

                    JSONObject jsonObject=new JSONObject(items);
                    token=null;
                    Toast.makeText(getApplication(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }

    }

}
