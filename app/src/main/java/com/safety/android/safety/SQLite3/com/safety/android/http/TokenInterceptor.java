package com.safety.android.safety.SQLite3.com.safety.android.http;


import com.example.myapplication.MainActivity;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor  implements Interceptor {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    @Override
    public Response intercept(Chain chain) throws IOException {

        String token= MainActivity.token;

        Request originalRequest = chain.request();
        Request updateRequest = originalRequest.newBuilder().header("X-Access-Token", token).build();
        return chain.proceed(updateRequest);
    }
}