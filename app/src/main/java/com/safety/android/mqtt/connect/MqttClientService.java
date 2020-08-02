package com.safety.android.mqtt.connect;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.safety.android.LunchActivity;
import com.safety.android.MainActivity;
import com.safety.android.mqtt.callback.ConnectCallBackHandler;
import com.safety.android.mqtt.callback.MqttCallbackHandler;

import org.eclipse.paho.android.service.MqttAndroidClient;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        int type = intent.getIntExtra("type",1);
        System.out.println("start message service");
        client=startConnect(MainActivity.getContext(),ClientID,ServerIP,PORT);
        if(type == 1){
            createNotificationChannel();
        }else{
            createErrorNotification();
        }
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }.start();
        return super.onStartCommand(intent, flags, startId);
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

}
