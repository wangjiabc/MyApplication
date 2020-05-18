package com.safety.android.tools;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

public class TakePictures {

    private Context context;

    public TakePictures(Context context) {
        this.context = context;
    }

    public static final int REQUEST_PHOTO=2;

    //用于保存拍照图片的uri
    private static Uri mCameraUri;

    // 用于保存图片的文件路径，Android 10以下使用图片路径访问图片
    private static String mCameraImagePath;

    // 是否是Android 10以上手机
    private static boolean isAndroidQ = Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P;

    public Intent getCaptureImage(){

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        //final Intent captureImage=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile = null;
            Uri photoUri = null;

            if (isAndroidQ) {
                // 适配android 10
                photoUri = createImageUri();
                mCameraUri=photoUri;
            } else {
                try {
                    photoFile = createImageFile();
                    mCameraImagePath=photoFile.getPath();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (photoFile != null) {
                    mCameraImagePath = photoFile.getAbsolutePath();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                        photoUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", photoFile);
                    } else {
                        photoUri = Uri.fromFile(photoFile);
                    }
                }
            }

            mCameraUri = photoUri;
            if (photoUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
        //captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        //Log.d("tag uri camera = ",uri.toString());

        return captureIntent;
    }


    private  Uri createImageUri() {
        String status = Environment.getExternalStorageState();
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        } else {
            return context.getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new ContentValues());
        }
    }

    public static File getFile(){
        String path="";

        if (isAndroidQ) {
            // Android 10 使用图片uri加载
            path=mCameraUri.getPath();
        } else {
            // 使用图片路径加载
            path=mCameraImagePath;
        }

        File file=new File(path);

        return  file;
    }

    public static Bitmap getScaledBitmap(Integer x,Integer y){

        String path="";

        if (isAndroidQ) {
            // Android 10 使用图片uri加载
            path=mCameraUri.getPath();
        } else {
            // 使用图片路径加载
            path=mCameraImagePath;
        }

        System.out.println("path====="+path);
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


    /**
     * 创建保存图片的文件
     */
    private File createImageFile() throws IOException {
        String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageName);
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        return tempFile;
    }


}
