package com.safety.android.tools;

import android.util.Log;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.safety.android.PhotoGallery.FlickrFetchr;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class UpFileToQiniu {

    public UpFileToQiniu(){
        new Thread(new Runnable(){

            @Override
            public void run() {

                String json= null;
                try {
                    json = new FlickrFetchr().getUrlString("http://203.0.104.65:8080/a/test/token.do");
                } catch (IOException e) {
                    e.printStackTrace();
                }


                String token="";
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    token=jsonObject.getString("token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                UploadManager uploadManager=new UploadManager();



                String key= UUID.randomUUID().toString()+".jpg";
                System.out.println("key========="+key+ "     token"+token);

                File file=TakePictures.getFile();

                uploadManager.put(file, key, token,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if(info.isOK()) {
                                    Log.i("qiniu", "Upload Success");
                                } else {
                                    Log.i("qiniu", "Upload Fail");
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                            }
                        }, null);
            }
        }).start();
    }


}
