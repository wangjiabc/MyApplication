package com.safety.android.mqtt.connect;

import android.content.Context;

import com.safety.android.LunchActivity;
import com.safety.android.MainActivity;
import com.safety.android.mqtt.callback.ConnectCallBackHandler;
import com.safety.android.mqtt.callback.MqttCallbackHandler;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Date;

public class MqttConnect {

    public final static String ClientID= LunchActivity.username+"_"+ (int)(Math.random() * 100);
    private final static String ServerIP="223.86.150.188";
    private final static String PORT="61616";
    private final static String userName = "admin";
    private final static String passWord = "admin";

    private static MqttAndroidClient Client;
    private static MqttConnectOptions conOpt;



    /**
     * 获取MqttAndroidClient实例
     * @return
     */
    public  MqttAndroidClient getMqttAndroidClientInstace(Context context){
        if(Client!=null) {
            return Client;
        }else {
            startConnect(context,ClientID,ServerIP,PORT);
            return Client;
        }
    }

    public  MqttAndroidClient startConnect(Context context, String clientID, String serverIP, String port) {

        //服务器地址
        String  uri ="tcp://";
        uri=uri+serverIP+":"+port;

        /**
         * 连接的选项
         */
        conOpt= new MqttConnectOptions();
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

        conOpt.setCleanSession(false);

        String testament=ClientID+" close "+new Date();

        conOpt.setWill("message.testament", testament.getBytes(), 2, true);

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


    public static void connect(){
        try {
            Client.connect(conOpt, null, new ConnectCallBackHandler(MainActivity.getContext()));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
