package com.safety.android.tools;

import android.content.Context;
import android.util.Log;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch2;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class UpFileToQiniu {

    public UpFileToQiniu(String key0, final Context context){
        final String key=key0;
        new Thread(new Runnable(){

            @Override
            public void run() {

                String json= null;

                    //json = new FlickrFetchr().getUrlString("http://203.0.104.65:8080/a/test/token.do");
                json=new OKHttpFetch2(context).get(FlickrFetch.base + "/inoutitem/inoutitem/token");

                String token="";
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    token=jsonObject.getString("token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                UploadManager uploadManager=new UploadManager();


                System.out.println("key========="+key+ "     token===="+token);

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

                try {
                    new PictureCompressUtil().compressByQuality(file.getPath(),file.getPath()+"compress",60);
                    uploadManager.put(file.getPath()+"compress", "compress/"+key, token,
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
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }


}
