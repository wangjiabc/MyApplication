package com.safety.android.http;


import com.safety.android.MainActivity;

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

        if(token==null||token.equals("")){
            token=login.token;
        }

        System.out.println("token================"+token);

        Request originalRequest = chain.request();
        Request updateRequest = originalRequest.newBuilder().header("X-Access-Token", token).build();
        return chain.proceed(updateRequest);
    }
}