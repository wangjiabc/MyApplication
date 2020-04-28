package com.safety.android.safety.LocalFile;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import static com.safety.android.safety.LocalFile.FileTypeTest.getFileHexString;

/**
 * Created by WangJing on 2017/10/6.
 */

public class SdCard {

    // 用于存放sdcard卡上的所有文件路径
    public static ArrayList<String> dirAllStrArr = new ArrayList<String>();

    public static String getFilePath() {
    /* 遍历sdcard旗下的所有文件夹开始 */
        String sdpath = Environment.getExternalStorageDirectory()
                .getAbsolutePath();// 获取sdcard的根路径
        Log.i("Qiniu sdpath=", sdpath);
        File dirFile = new File(sdpath);
        try {
            DirAll(dirFile);
            Log.i("Qiniu File=", dirAllStrArr.toString());
        } catch (Exception e) {
            Log.i("Qiniu error=", e.getMessage());
        }

        Iterator<String> iterator = dirAllStrArr.iterator();

        while (iterator.hasNext())

        {
            String filePath = iterator.next();
            File file = new File(filePath);
            Log.i("Qiniu PutBytes", file.toString());
            File putData = file;
            //mime type 检测图片文件类型
            String mimeType = "";
            Map<String, String> map = FileTypeTest.getFileType();
            Iterator<Map.Entry<String, String>> entryiterator = map.entrySet().iterator();
            String filetypeHex = String.valueOf(getFileHexString(file));
            while (entryiterator.hasNext()) {
                Map.Entry<String, String> entry = entryiterator.next();
                String fileTypeHexValue = entry.getValue();
                if (filetypeHex.toUpperCase().startsWith(fileTypeHexValue)) {
                    mimeType = entry.getKey();
                    break;
                }
                Log.i("Qiniu mimetype=", mimeType);
            }
        }
        return  "";
    }

    public static void DirAll(File dirFile) throws Exception {
        if (dirFile.exists()) {
            File[] files = dirFile.listFiles();
            for (File file : files) {
                //  Log.i("Qiniu file[]",file.toString());
                if (file.isDirectory()) {
                    String fileName = file.getName();
                    // 除sdcard上Android这个文件夹以外。
                    if (!fileName.endsWith("Android")) {
                        // 如果遇到文件夹则递归调用。
                        DirAll(file);
                    }
                } else {
                    // 如果是图片文件压入数组
                    String fileName = file.getName();
                    if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
                            || fileName.endsWith(".bmp")
                            || fileName.endsWith(".gif")
                            || fileName.endsWith(".png")) {
                        // 如果遇到文件则放入数组
                        if (dirFile.getPath().endsWith(File.separator)) {
                            dirAllStrArr
                                    .add(dirFile.getPath() + file.getName());
                        } else {
                            dirAllStrArr.add(dirFile.getPath() + File.separator
                                    + file.getName());
                        }
                      }
                    }
                  }
                }
        }
}
