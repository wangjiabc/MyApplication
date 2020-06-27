package com.safety.android.mqtt.callback;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.example.myapplication.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import androidx.annotation.RequiresApi;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Description :接收服务器推送过来的消息
 * Author : liujun
 * Email  : liujin2son@163.com
 * Date   : 2016/10/25 0025
 */

public class MqttCallbackHandler implements MqttCallbackExtended {
    private  MqttAndroidClient mqttAndroidClient;
    private Context context;
    private String clientId;

    public static final String Topic="topic";

    private NotificationManager manager;

    public MqttCallbackHandler(MqttAndroidClient mqttAndroidClient, Context context, String clientId) {
        this.context=context;
        this.clientId=clientId;
        this.mqttAndroidClient=mqttAndroidClient;
    }

    @Override
    public void connectComplete(boolean reconnect,String serverURI){
        /**自动订阅消息**/
        subscribeToTopic();
    }

    @Override
    public void connectionLost(Throwable throwable) {
        Log.d("MqttCallbackHandler","MqttCallbackHandler/connectionLost");
    }

    /**
     *
     * @param s  主题
     * @param mqttMessage  内容信息
     * @throws Exception
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        Log.d("MqttCallbackHandler","MqttCallbackHandler/messageArrived="+s);
        Log.d("MqttCallbackHandler","message1="+new String(mqttMessage.getPayload()));

        // 1. 创建一个通知(必须设置channelId)
        //Context context = getApplicationContext();
        String channelId = "ChannelId"; // 通知渠道
        Notification notification = new Notification.Builder(context)
                .setChannelId(channelId)
                .setSmallIcon(R.mipmap.icon_grid_qq_face_view)
                .setContentTitle("通知标题")
                .setContentText(new String(mqttMessage.getPayload()))
                .build();
// 2. 获取系统的通知管理器(必须设置channelId)
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "通知的渠道名称",
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
// 3. 发送通知(Notification与NotificationManager的channelId必须对应)
        notificationManager.notify(1, notification);

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        Log.d("MqttCallbackHandler","MqttCallbackHandler/deliveryComplete");
    }

    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(Topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                 //   Log.e(TAG, "onFailure ---> " + asyncActionToken);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                 //   Log.e(TAG, "onFailure ---> " + exception);
                }
            });
        } catch (MqttException e) {
           // Log.e(TAG, "subscribeToTopic is error");
            e.printStackTrace();
        }
    }

}
