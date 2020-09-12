package com.safety.android.mqtt.connect;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.safety.android.AssistService;
import com.safety.android.LunchActivity;
import com.safety.android.MainActivity;
import com.safety.android.http.AsynHttp;
import com.safety.android.mqtt.callback.PublishCallBackHandler;
import com.safety.android.tools.RSAUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.safety.android.mqtt.callback.MqttCallbackHandler.UnicodeToCN;

/**
 * Created by WangJing on 2017/10/11.
 */

public class MqttClientService extends Service {

    private Context context;
    public final static String ClientID= LunchActivity.username+"_"+ (int)(Math.random() * 100);
    private final static String ServerIP="223.86.150.188";
    private final static String PORT="61616";
    private static MqttAndroidClient client;
    private final static String userName = "admin";
    private final static String passWord = "admin";

    private final static int NOTIFICATION_ID = android.os.Process.myPid();
    private AssistServiceConnection mServiceConnection;

    private Date date=new Date();

    PowerManager.WakeLock wakeLock;

    public MqttClientService(){

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int type = intent.getIntExtra("type",1);
        if(type == 1){
            createNotificationChannel();
        }else{
            createErrorNotification();
        }
        // 测试线程，判断Service是否在工作
        new Thread(mRunnable).start();
        // 设置为前台进程，降低oom_adj，提高进程优先级，提高存活机率
        setForeground();

        return START_STICKY;

    }

    // 要注意的是android4.3之后Service.startForeground() 会强制弹出通知栏，解决办法是再
    // 启动一个service和推送共用一个通知栏，然后stop这个service使得通知栏消失。
    private void setForeground() {
        if (Build.VERSION.SDK_INT < 18)
        {
            startForeground(NOTIFICATION_ID, getNotification());
            return;
        }

        if (mServiceConnection == null)
        {
            mServiceConnection = new AssistServiceConnection();
        }
        // 绑定另外一条Service，目的是再启动一个通知，然后马上关闭。以达到通知栏没有相关通知
        // 的效果
        bindService(new Intent(this, AssistService.class), mServiceConnection,
                Service.BIND_AUTO_CREATE);
    }


    public void onCreate() {
        super.onCreate();

        //拿到电源锁
        acquireWakeLock();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MqttClientService.class), 0);

       if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //数字是随便写的“40”，
            nm.createNotificationChannel(new NotificationChannel("40", "App Service", NotificationManager.IMPORTANCE_DEFAULT));
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "40");

            //其中的2，是也随便写的，正式项目也是随便写
            startForeground(2 ,builder.build());

        }

    }

    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock) {
                wakeLock.acquire();
            }
        }
    }

    //释放设备电源锁
    private void releaseWakeLock() {
        if (null != wakeLock) {
            wakeLock.release();
            wakeLock = null;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    private void createErrorNotification() {
        Notification notification = new Notification.Builder(this).build();
        startForeground(0, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {

    }


    private class AssistServiceConnection implements ServiceConnection
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Service assistService = ((AssistService.LocalBinder)service)
                    .getService();
            MqttClientService.this.startForeground(NOTIFICATION_ID, getNotification());
            assistService.startForeground(NOTIFICATION_ID, getNotification());
            assistService.stopForeground(true);

            MqttClientService.this.unbindService(mServiceConnection);
            mServiceConnection = null;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private Notification getNotification()
    {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "")
                .setContentTitle("服务运行于前台")
                .setContentText("service被设为前台进程")
                .setTicker("service正在后台运行...")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(System.currentTimeMillis())
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        return notification;
    }


    Runnable mRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {

            System.out.println("start message service");
           // client = startConnect(MainActivity.getContext(), ClientID, ServerIP, PORT);
            client=new MqttConnect().getMqttAndroidClientInstace(getApplicationContext());
            while (true)
            {
                Date date1=new Date();
                Log.e("thrad ", "date1=" + date1.getTime()+"  date="+date.getTime());

                long diff=(date1.getTime()-date.getTime())/(1000*60);

                Log.d("diff", String.valueOf(diff));

                if(diff>10) {
                    System.out.println("diff>10");
                    new FetchItemsTask().execute(0);
                    date=date1;
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    private class FetchItemsTask extends AsyncTask<Integer,Void,String> {

        @Override
        protected String doInBackground(Integer... prams) {

            Integer type=prams[0];

            Response response = null;

            Request request = new Request.Builder()
                    .get()
                    .tag(this)
                    .url("http://api.map.baidu.com/location/ip?ak=pQFgFpS0VnMXwCRN6cTc1jDOcBVi3XoD&coor=bd09ll")
                    .build();

            String result = "";

            try {
                OkHttpClient client=new AsynHttp().getOkHttpClient(context,"http://api.map.baidu.com");

                response = client.newCall(request).execute();

                String s=response.body().string();
                s= UnicodeToCN(s);
                //System.out.println("s="+s);
                result=s;
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                String ClientID=MqttClientService.ClientID;
                JSONObject jsonObject=new JSONObject(result);
                jsonObject.put("clientid",ClientID);
                if(type!=null) {
                    jsonObject.put("type",type);
                }
                result=jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }


        @Override
        protected void onPostExecute(String s) {

            Log.d("s=",s);

            try {
                s = RSAUtils.encrypt(s);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                /**发布一个主题:如果主题名一样不会新建一个主题，会复用*/
                client.publish("position.topic",s.getBytes("UTF-8"),2,false,null,new PublishCallBackHandler(MainActivity.getContext()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

}
