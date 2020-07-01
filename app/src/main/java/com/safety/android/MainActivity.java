package com.safety.android;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.example.myapplication.R;
import com.safety.android.SQLite3.UserInfo;
import com.safety.android.SQLite3.UserLab;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.http.login;
import com.safety.android.mqtt.connect.MqttClientService;
import com.safety.android.mqtt.event.MessageEvent;
import com.safety.android.tools.MyTestUtil;
import com.squareup.picasso.Picasso;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

    private static Context mContext;

    private ConvenientBanner cbTest1;

    private boolean mCanLoop = true;

    private ArrayList<String> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=getApplicationContext();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dataUrl=getDiskCacheDir(getApplicationContext());

        getSupportFragmentManager().beginTransaction()              //添加fragment
                .add(R.id.activity_container_main, SafeBoxFragment.newInstance())
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

        initView();
        initBanner1();

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
        //开始执行轮播，并设置轮播时长
        cbTest1.startTurning(4000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //停止轮播
        cbTest1.stopTurning();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 初始化
     * 添加三张展示照片，网上随便找的，正常形式是调用接口从自己的后台服务器拿取
     */
    private void initView() {
        arrayList = new ArrayList<>();
        arrayList.add("https://t7.baidu.com/it/u=3204887199,3790688592&fm=79&app=86&size=h300&n=0&g=4n&f=jpeg?sec=1594192215&t=725fae58ddd8a3032656562d83f38e23");
        arrayList.add("https://t9.baidu.com/it/u=3363001160,1163944807&fm=79&app=86&size=h300&n=0&g=4n&f=jpeg?sec=1594192215&t=6958a4358bcc6534514ebd55779ddafd");
        arrayList.add("https://t9.baidu.com/it/u=1307125826,3433407105&fm=79&app=86&size=h300&n=0&g=4n&f=jpeg?sec=1594192215&t=b69f42df704cfa882fec85f3a9bcb376");
    }

    /**
     * 初始化轮播图1
     * setPageIndicator 设置指示器样式
     * setPageIndicatorAlign 设置指示器位置
     * setPointViewVisible 设置指示器是否显示
     * setCanLoop 设置是否轮播
     * setOnItemClickListener 设置每一张图片的点击事件
     */
    private void initBanner1() {

        // TODO: 2018/11/22 控制如果只有一张网络图片，不能滑动，不能轮播
        if(arrayList.size()<=1){
            mCanLoop=false;
        }

        cbTest1.setPages(new CBViewHolderCreator() {
            @Override
            public Holder createHolder(View itemView) {
                return new NetImageHolderView1(itemView);
            }

            @Override
            public int getLayoutId() {
                //设置加载哪个布局
                return R.layout.item_banner1;
            }
        }, arrayList)
                .setPageIndicator(new int[]{R.mipmap.ic_page_indicator, R.mipmap.ic_page_indicator_focused})
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
                .setPointViewVisible(mCanLoop)
                .setCanLoop(mCanLoop)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Toast.makeText(MainActivity.this, "你点击了cbTest1的第" + position + "张图片", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 轮播图1 对应的holder
     */
    public class NetImageHolderView1 extends Holder<String> {
        private ImageView mImageView;

        //构造器
        public NetImageHolderView1(View itemView) {
            super(itemView);
        }

        @Override
        protected void initView(View itemView) {
            //找到对应展示图片的imageview
            mImageView = itemView.findViewById(R.id.iv_banner1);
            //设置图片加载模式为铺满，具体请搜索 ImageView.ScaleType.FIT_XY
            mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        @Override
        public void updateUI(String data) {
            //使用ImageLoader加载图片
            Picasso.with(getContext()).load(data).into(mImageView);
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
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "修改密码").setIcon(android.R.drawable.ic_lock_lock);
        // setIcon()方法为菜单设置图标，这里使用的是系统自带的图标，同学们留意一下,以
        // android.R开头的资源是系统提供的，我们自己提供的资源是以R开头的
        menu.add(Menu.NONE, Menu.FIRST + 2, 2, "退出").setIcon(android.R.drawable.ic_lock_power_off);
        menu.add(Menu.NONE, Menu.FIRST + 3, 3, "关于").setIcon(android.R.drawable.ic_menu_info_details);
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
                validateItem.setTag(0);
                layout_validate.addView(validateItem);
                TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                TextView et_validateText=validateItem.findViewById(R.id.et_validate_text);
                et_validateText.setText("");

                tv_validateName.setText("原密码");

                map.put("name", tv_validateName);
                map.put("value", et_validate);

                list.add(map);

                Map<String,Object> map2 = new HashMap<String, Object>();
                View validateItem2 = inflater.inflate(R.layout.item_validate_enter2, null);
                validateItem2.setTag(1);
                layout_validate.addView(validateItem2);
                TextView tv_validateName2 = (TextView) validateItem2.findViewById(R.id.tv_validateName);
                EditText et_validate2 = (EditText) validateItem2.findViewById(R.id.et_validate);
                TextView et_validateText2=validateItem2.findViewById(R.id.et_validate_text);
                et_validateText2.setText("");

                tv_validateName2.setText("新密码");

                map2.put("name", tv_validateName2);
                map2.put("value", et_validate2);

                list.add(map2);

                Map<String,Object> map3 = new HashMap<String, Object>();
                View validateItem3 = inflater.inflate(R.layout.item_validate_enter2, null);
                validateItem3.setTag(2);
                layout_validate.addView(validateItem3);
                TextView tv_validateName3 = (TextView) validateItem3.findViewById(R.id.tv_validateName);
                EditText et_validate3 = (EditText) validateItem3.findViewById(R.id.et_validate);
                TextView et_validateText3=validateItem3.findViewById(R.id.et_validate_text);
                et_validateText3.setText("");

                tv_validateName3.setText("确认新密码");

                map3.put("name", tv_validateName3);
                map3.put("value", et_validate3);

                list.add(map3);


                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle("修改密码")
                        .setView(validateView)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                StringBuffer stringBuffer = new StringBuffer();

                                Map sysUser=LunchActivity.sysUser;

                                MyTestUtil.print(list);

                                String username= (String) sysUser.get("sysUserName");

                                String oldpassword = ((EditText)list.get(0).get("value")).getText().toString();

                                String password = ((EditText)list.get(1).get("value")).getText().toString();

                                String confirmpassword = ((EditText)list.get(2).get("value")).getText().toString();

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

                    Intent intent = new Intent(getApplication(), login.class);
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }

    }


    private class FetchItemsTaskUpPassWord extends AsyncTask<JSONObject,Void,String> {

        @Override
        protected String doInBackground(org.json.JSONObject... params) {

            return new OKHttpFetch(getApplication()).post(FlickrFetch.base+"/sys/user/updatePassword",params[0],"put");
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

    /**
     * 获取context
     * @return
     */
    public static Context getContext(){
        return mContext;
    }

}
