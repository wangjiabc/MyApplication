package com.safety.android.mqtt.callback;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;


/**
 * Description :
 * Author : liujun
 * Email  : liujin2son@163.com
 * Date   : 2016/10/25 0025
 */

public class ConnectCallBackHandler implements IMqttActionListener {

    private Context context;

    public ConnectCallBackHandler(Context context) {
        this.context=context;
    }

    @Override
    public void onSuccess(IMqttToken iMqttToken) {
        Log.d("ConnectCallBackHandler","ConnectCallBackHandler/onSuccess");
    }

    @Override
    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
        Log.d("ConnectCallBackHandler","ConnectCallBackHandler/onFailure");
    }
}
