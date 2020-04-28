package com.safety.android.safety.Message;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.safety.android.mqtt.adapter.SubcriberAdapter;
import com.safety.android.mqtt.bean.Message;
import com.safety.android.mqtt.callback.PublishCallBackHandler;
import com.safety.android.mqtt.callback.SubcribeCallBackHandler;
import com.safety.android.mqtt.connect.MqttClient;
import com.safety.android.mqtt.event.MessageEvent;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindFont;
import butterknife.ButterKnife;

/**
 * Created by WangJing on 2017/10/11.
 */

public class ChatFragment extends Fragment {
    /**订阅*/
    @BindFont(R.id.ed_topic)
    EditText edTopic;
    @BindFont(R.id.btn_start_sub)
    Button btnStartSub;
    /**发布*/
    @BindFont(R.id.ed_pub_topic)
    EditText edPubTopic;
    @BindFont(R.id.ed_pub_message)
    EditText edPubMessage;
    @BindFont(R.id.btn_start_pub)
    Button btnStartPub;
    /**记录*/
    @BindFont(R.id.lv_content)
    ListView lvContent;

    public static final String PA="PublishActivity";
    private SubcriberAdapter subcriberAdapter;
    public static final String Topic="general";
    private String pubTopic="";
    private String pubMessage;
    private MqttAndroidClient client;

    public static ChatFragment newInstance(){
        return new ChatFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        final View v=inflater.inflate(R.layout.activity_chat,container,false);

        btnStartPub= (Button) v.findViewById(R.id.btn_start_pub);
        edPubTopic= (EditText) v.findViewById(R.id.ed_topic);
        btnStartSub= (Button) v.findViewById(R.id.btn_start_sub);
        btnStartPub= (Button) v.findViewById(R.id.btn_start_pub);
        lvContent= (ListView) v.findViewById(R.id.lv_content);
        edPubMessage= (EditText) v.findViewById(R.id.ed_pub_message);

        ButterKnife.bind(getActivity());
        client= MqttClient.getMqttAndroidClientInstace(getContext());
        initDate();


        btnStartSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] split = Topic.split(",");
                /**一共有多少个主题*/
                int length = split.length;
                String [] topics=new String[length];//订阅的主题
                int [] qos =new int [length];// 服务器的质量
                for(int i=0;i<length;i++){
                    topics[i]=split[i];
                    qos[i]=0;
                }
                if (client != null) {
                    try {
                        if (length > 1) {
                            /**订阅多个主题,服务的质量默认为0*/
                            Log.d(PA, "topics=" + Arrays.toString(topics));
                            client.subscribe(topics, qos, null, new SubcribeCallBackHandler(getActivity()));
                        } else {
                            Log.d(PA, "topic=" + Topic);
                            /**订阅一个主题，服务的质量默认为0*/
                            client.subscribe(Topic, 0, null, new SubcribeCallBackHandler(getActivity()));
                        }
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(PA, "MqttAndroidClient==null");
                }
            }
        });



        /**2.开始发布*/
        btnStartPub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**获取发布的主题*/
                pubTopic = edPubTopic.getText().toString().trim();
                /**获取发布的消息*/
                pubMessage = edPubMessage.getText().toString().trim();
                /**消息的服务质量*/
                int qos=0;
                /**消息是否保持*/
                boolean retain=false;
                /**要发布的消息内容*/
                byte[] message=pubMessage.getBytes();
                if(pubTopic!=null&&!"".equals(pubTopic)){

                    if(client!=null){
                        try {
                            /**发布一个主题:如果主题名一样不会新建一个主题，会复用*/
                            client.publish(pubTopic,message,qos,retain,null,new PublishCallBackHandler(getActivity()));
                            Toast.makeText(getActivity(),"sussce",Toast.LENGTH_SHORT).show();
                        } catch (MqttException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(),"error",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Log.e(PA,"MqttAndroidClient==null");
                        Toast.makeText(getActivity(),"client不能为空",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(),"发布的主题不能为空",Toast.LENGTH_SHORT).show();
                }
            }
        });


        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * 初始化数据
     */
    private void initDate() {
        List<Message> list=new ArrayList<Message>();
        if(subcriberAdapter==null) {
            subcriberAdapter = new SubcriberAdapter(list, getContext());
            lvContent.setAdapter(subcriberAdapter);
        }else{
            subcriberAdapter.notifyDataSetChanged();
        }
    }


    /**
     * 运行在主线程
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        String string = event.getString();
        /**接收到服务器推送的信息，显示在右边*/
        if("".equals(string)){
            String topic = event.getTopic();
            MqttMessage mqttMessage = event.getMqttMessage();
            String s = new String(mqttMessage.getPayload());
            topic=topic+" : "+s;
            subcriberAdapter.addListDate(new Message(topic));
            /**接收到订阅成功的通知,订阅成功，显示在左边*/
        }else{
            subcriberAdapter.addListDate(new Message("Me : "+string));
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
}
