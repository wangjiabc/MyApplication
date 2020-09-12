package com.safety.android.mqtt.callback;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.example.myapplication.R;
import com.safety.android.MainActivity;
import com.safety.android.http.AsynHttp;
import com.safety.android.mqtt.connect.MqttConnect;
import com.safety.android.tools.RSAUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.RequiresApi;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    public static final String POSITION="position";

    private NotificationManager manager;

    private MqttAndroidClient client;

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

        if(s.equals(POSITION)){

            new FetchItemsTask().execute(1);

        }else {

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

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        Log.d("MqttCallbackHandler","MqttCallbackHandler/deliveryComplete");
    }

    private void subscribeToTopic() {
        try {
            String[] split = {Topic,POSITION};
            /**一共有多少个主题*/
              int length = split.length;
                String [] topics=new String[length];//订阅的主题
                int [] qos =new int [length];// 服务器的质量
                for(int i=0;i<length;i++){
                    topics[i]=split[i];
                    qos[i]=1;
                }
            mqttAndroidClient.subscribe(topics, qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                 //   Log.e(TAG, "onFailure ---> " + asyncActionToken);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                 //   Log.e(TAG, "onFailure ---> " + exception);
                }
            });
            client= new MqttConnect().getMqttAndroidClientInstace(MainActivity.getContext());
        } catch (MqttException e) {
           // Log.e(TAG, "subscribeToTopic is error");
            e.printStackTrace();
        }
    }

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
                String ClientID=MqttConnect.ClientID;
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

    /**
     * unicode编码转换为汉字
     * @param unicodeStr 待转化的编码
     * @return 返回转化后的汉子
     */
    public static String UnicodeToCN(String unicodeStr) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(unicodeStr);
        char ch;
        while (matcher.find()) {
            //group
            String group = matcher.group(2);
            //ch:'李四'
            ch = (char) Integer.parseInt(group, 16);
            //group1
            String group1 = matcher.group(1);
            unicodeStr = unicodeStr.replace(group1, ch + "");
        }

        return unicodeStr.replace("\\", "").trim();
    }


}
