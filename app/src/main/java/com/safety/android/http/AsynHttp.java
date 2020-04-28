package com.safety.android.http;

import android.content.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class AsynHttp {

    private static OkHttpClient mOkHttpClient;

    private void AsynHttp(final Context context, final String URL) {
        mOkHttpClient = new OkHttpClient.Builder()
                .followRedirects(false)  //禁制OkHttp的重定向操作，我们自己处理重定向
                .followSslRedirects(false)
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {

                        for (Cookie cookie : cookies) {
                            new PersistentCookieStore(context).add(url,cookie);
                            //System.out.println("cookie Name:" + cookie.name());
                            //System.out.println("cookie Path:" + cookie.path());
                        }
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookieStore=new PersistentCookieStore(context).get(url);
                        if(cookieStore!=null)
                            System.out.println("cookieStore1="+cookieStore);
                        List<Cookie> cookies = cookieStore;
                        Date date=new Date();
                        if (cookies == null) {
                            System.out.println("没加载到cookie__"+date.getTime());
                        }else {
                            System.out.println("加载cookie__"+date.getTime());
                            for (Cookie cookie : cookies) {
                                System.out.println("cookie Name:" + cookie);
                                System.out.println("cookie Path:" + cookie.path());
                            }
                        }
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                }).addInterceptor(new TokenInterceptor())
                .build();



    }

    public OkHttpClient getOkHttpClient(Context context,final String URL) {


        if (mOkHttpClient==null){
            AsynHttp(context,URL);
        }
        return mOkHttpClient;
    }
}
