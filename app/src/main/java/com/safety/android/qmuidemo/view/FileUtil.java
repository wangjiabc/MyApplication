package com.safety.android.qmuidemo.view;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
public class FileUtil {
    private static int FILE_SIZE = 4*1024;
    private static String TAG = "FileUtil";
    public static boolean hasSdcard(){
        String status = Environment.getExternalStorageState();
        if(status.equals(Environment.MEDIA_MOUNTED)){
            return true;
        }
        return false;
    }
    public static boolean createPath(String path){
        File f = new File(path);
        if(!f.exists()){
            Boolean o = f.mkdirs();
            Log.i(TAG, "create dir:"+path+":"+o.toString());
            return o;
        }
        return true;
    }
    public static boolean exists(String file){
        return new File(file).exists();
    }

    public static Drawable getImageDrawable(String file){
        if(!exists(file)) return null;
        try{
            InputStream inp = new FileInputStream(new File(file));
            return BitmapDrawable.createFromStream(inp, "img");
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}