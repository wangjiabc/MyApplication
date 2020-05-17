package com.safety.android.tools;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;

public class TakePictures {

    public static final int REQUEST_PHOTO=2;

    public static Intent getCaptureImage(String path,String fileName){

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        final Intent captureImage=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file=new File(path,fileName);
        Uri uri=Uri.fromFile(file);
        captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        Log.d("tag uri camera = ",uri.toString());

        return captureImage;
    }


    public static Bitmap getScaledBitmap(String path, Integer x,Integer y){

        return getScaledBitmap0(path,x,y);
    }

    private static Bitmap getScaledBitmap0(String path, Integer destWidth, Integer destHeight){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(path,options);

        float srcWidth=options.outWidth;
        float srcHeight=options.outHeight;


        int inSampleSize = 1;
        if(destWidth!=null&&destHeight!=null) {

            if (srcHeight > destHeight || srcWidth > destWidth) {
                if (srcWidth > srcHeight) {
                    inSampleSize = Math.round(srcHeight / destHeight);
                } else {
                    inSampleSize = Math.round(srcWidth / destWidth);
                }
            }

        }
        options=new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        return BitmapFactory.decodeFile(path,options);
    }

}
