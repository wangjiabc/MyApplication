package com.safety.android.http;


import com.safety.android.MainActivity;
import com.safety.android.SQLite3.UserInfo;
import com.safety.android.SQLite3.UserLab;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor  implements Interceptor {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    @Override
    public Response intercept(Chain chain) throws IOException {

        List<UserInfo> list= UserLab.get(MainActivity.getContext()).getUserInfo();

        String token="";

        try {
            if(list.size()>0) {
                UserInfo userInfo = list.get(0);
                token = userInfo.getToken();
            }else {

            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("token================"+token);

        Request originalRequest = chain.request();
        Request updateRequest = originalRequest.newBuilder().header("X-Access-Token", token).build();
        return chain.proceed(updateRequest);
    }
}