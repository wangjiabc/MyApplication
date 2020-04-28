package com.safety.android.http;

import android.util.LruCache;

/**
 * Created by WangJing on 2017/6/26.
 */

public class Lru {
    private static LruCache<String ,byte[]> lruCache;

    public Lru(){

    }

    public static LruCache getLruCache(){
        if(lruCache==null){
             lruCache=new LruCache<>(2 * 1024 * 1024);
            return lruCache;
        }else {
            return lruCache;
        }
    }
}
