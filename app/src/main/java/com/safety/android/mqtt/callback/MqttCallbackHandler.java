package com.safety.android.mqtt.callback;

import android.content.Context;
import android.util.Log;

import com.safety.android.mqtt.event.MessageEvent;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;


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

    public static final String Topic="general";

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
    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        Log.d("MqttCallbackHandler","MqttCallbackHandler/messageArrived="+s);
        Log.d("MqttCallbackHandler","message1="+new String(mqttMessage.getPayload()));
        EventBus.getDefault().post(new MessageEvent(s,mqttMessage));
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
