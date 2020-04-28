package com.safety.android.mqtt.connect;

import android.content.Context;

import com.safety.android.mqtt.callback.ConnectCallBackHandler;
import com.safety.android.mqtt.callback.MqttCallbackHandler;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Created by WangJing on 2017/10/11.
 */

public class MqttClient{

    private final static String ClientID="android";
    private final static String ServerIP="223.86.150.188";
    private final static String PORT="61613";
    private static MqttAndroidClient client;
    private final static String userName = "admin";
    private final static String passWord = "password";

    /**
     * 获取MqttAndroidClient实例
     * @return
     */
    public static MqttAndroidClient getMqttAndroidClientInstace(Context context){
        if(client!=null) {
            return client;
        }else {
            client=startConnect(context,ClientID,ServerIP,PORT);
            return client;
        }
    }

    private static MqttAndroidClient startConnect(Context context, String clientID, String serverIP, String port) {
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
}
