package com.safety.android.http;

import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OKHttpFetch {

    final static String URL=FlickrFetch.URL;

    private Context context;

    private static OkHttpClient client;

    public OKHttpFetch(Context context){
        this.context=context;
        client=new AsynHttp().getOkHttpClient(context,URL);
    }

    public static OkHttpClient getOkHttpClient() {

        return client;
    }

    public String get(String site) {

        Response response = null;

        Request request = new Request.Builder()
                .get()
                .tag(this)
                .url(URL+site)
                .build();

        String result = "";

        try {
            response = client.newCall(request).execute();

            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }

            String s=response.body().string();

            System.out.println("response1="+s);

            JSONObject jsonObject=new JSONObject(s);

            if(jsonObject==null){
                Intent intent=new Intent(context, login.class);
                context.startActivity(intent);
            }else if(jsonObject.get("message").equals("Token失效，请重新登录")){
                startUserActivity(context,login.class);
            }

            result=s;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            Intent intent=new Intent(context, login.class);
            context.startActivity(intent);
            e.printStackTrace();
        }

        return result;
    }

    public String post(String site) {

        Response response = null;

        RequestBody requestBody = RequestBody.create(null, new byte[]{});

        Request request = new Request.Builder()
                .url(URL+site)
                .post(requestBody)
                .build();

        String result = "";

        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }

            String s=response.body().string();

            System.out.println("response1="+s);

            JSONObject jsonObject=new JSONObject(s);
            System.out.println("jsonObject.get(message)"+jsonObject.get("message"));
            if(jsonObject==null){
                Intent intent=new Intent(context, login.class);
                context.startActivity(intent);
            }else if(jsonObject.get("message").equals("Token失效，请重新登录")){
                Intent intent=new Intent(context, login.class);
                context.startActivity(intent);
            }

            result=s;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            startUserActivity(context,login.class);
            e.printStackTrace();
        }

        return result;

    }

    public String post(String site,JSONObject object,String type) {

        Response response = null;

        Request request=null;

        final MediaType FORM_CONTENT_TYPE= MediaType.parse("application/json;charset=utf-8");

        RequestBody body=RequestBody.create(FORM_CONTENT_TYPE,object.toString());

        if(type.equals("post")) {
            request = new Request.Builder()
                    .url(URL + site)
                    .post(body)
                    .build();
        }else if(type.equals("put")) {
            request = new Request.Builder()
                    .url(URL + site)
                    .put(body)
                    .build();
        }else if(type.equals("delete")) {
            request = new Request.Builder()
                    .url(URL + site)
                    .delete(body)
                    .build();
        }

        String result = "";

        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }

            String s=response.body().string();

            System.out.println("response1="+s);

            JSONObject jsonObject=new JSONObject(s);

            if(jsonObject==null){
                Intent intent=new Intent(context, login.class);
                context.startActivity(intent);
            }else if(jsonObject.get("message").equals("Token失效，请重新登录")){
                Intent intent=new Intent(context, login.class);
                context.startActivity(intent);
            }

            result=s;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            Intent intent=new Intent(context, login.class);
            context.startActivity(intent);
            e.printStackTrace();
        }

        return result;

    }

    public static void startUserActivity(Context context , Class cls){
        Intent intent = new Intent();
        intent.setClass(context, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        context.startActivity(intent);
    }


}
