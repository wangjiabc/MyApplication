package com.safety.android.mqtt.connect;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.safety.android.AssistService;
import com.safety.android.LunchActivity;
import com.safety.android.MainActivity;
import com.safety.android.mqtt.callback.ConnectCallBackHandler;
import com.safety.android.mqtt.callback.MqttCallbackHandler;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttService;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

/**
 * Created by WangJing on 2017/10/11.
 */

public class MqttClientService extends Service {

    private Context context;
    private final static String ClientID= LunchActivity.username;
    private final static String ServerIP="223.86.150.188";
    private final static String PORT="61616";
    private static MqttAndroidClient client;
    private final static String userName = "admin";
    private final static String passWord = "admin";
    private  MyHandler handler;

    private final static int NOTIFICATION_ID = android.os.Process.myPid();
    private AssistServiceConnection mServiceConnection;

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

    /**
     * 获取MqttAndroidClient实例
     * @return
     */
    public  MqttAndroidClient getMqttAndroidClientInstace(Context context){
        this.context=context;
        if(client!=null) {
            return client;
        }else {
            client=startConnect(context,ClientID,ServerIP,PORT);
            return client;
        }
    }

    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //数字是随便写的“40”，
            nm.createNotificationChannel(new NotificationChannel("40", "App Service", NotificationManager.IMPORTANCE_DEFAULT));
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "40");

            //其中的2，是也随便写的，正式项目也是随便写
            startForeground(2 ,builder.build());

        }

    }


    private  MqttAndroidClient startConnect(Context context, String clientID, String serverIP, String port) {
        MqttAndroidClient Client;
        //服务器地址
        String  uri ="tcp://";
        uri=uri+serverIP+":"+port;

        /**
         * 连接的选项
         */
        MqttConnectOptions conOpt = new MqttConnectOptions();
        /**设计连接超时时间*/
        conOpt.setConnectionTimeout(3000);
        /**设计心跳间隔时间300秒*/
        conOpt.setKeepAliveInterval(300);
        /**自动重连*/
        conOpt.setAutomaticReconnect(true);
        // 用户名
        conOpt.setUserName(userName);
        // 密码
        conOpt.setPassword(passWord.toCharArray());
        /**
         * 创建连接对象
         */
        Client = new MqttAndroidClient(context,uri, clientID);
        /**
         * 连接后设计一个回调
         */
        Client.setCallback(new MqttCallbackHandler(Client,context, clientID));
        /**
         * 开始连接服务器，参数：ConnectionOptions,  IMqttActionListener
         */
        try {
            Client.connect(conOpt, null, new ConnectCallBackHandler(context));
        } catch (MqttException e) {
            e.printStackTrace();
        }
            return  Client;
    }


    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            stopSelf(msg.what);
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
        @Override
        public void run() {

            System.out.println("start message service");
            client=startConnect(MainActivity.getContext(),ClientID,ServerIP,PORT);

            while (true)
            {
                Log.e("thrad ", "" + System.currentTimeMillis());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

}
