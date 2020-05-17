package com.safety.android.qmuidemo.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.safety.android.tools.MyTestUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
public class NetWork {
    private static String TAG = "NetWork";

    public static String getHttpData(String baseUrl) {
        return getHttpData(baseUrl, "GET", "", null);
    }

    public static String postHttpData(String baseUrl, String reqData) {
        return getHttpData(baseUrl, "POST", reqData, null);
    }

    public static String postHttpData(String baseUrl, String reqData, HashMap<String, String> propertys) {
        return getHttpData(baseUrl, "POST", reqData, propertys);
    }

    /**
     * 获取赛事信息
     *
     * @return
     */
    public static String getHttpData(String baseUrl, String method, String reqData, HashMap<String, String> propertys) {
        String data = "", str;
        PrintWriter outWrite = null;
        InputStream inpStream = null;
        BufferedReader reader = null;
        HttpURLConnection urlConn = null;
        try {
            URL url = new URL(baseUrl);
            urlConn = (HttpURLConnection) url.openConnection();
            //启用gzip压缩
            urlConn.addRequestProperty("Accept-Encoding", "gzip, deflate");
            urlConn.setRequestMethod(method);
            urlConn.setDoOutput(true);
            urlConn.setConnectTimeout(3000);
            if (propertys != null && !propertys.isEmpty()) {
                Iterator<Map.Entry<String, String>> props = propertys.entrySet().iterator();
                Map.Entry<String, String> entry;
                while (props.hasNext()) {
                    entry = props.next();
                    urlConn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            outWrite = new PrintWriter(urlConn.getOutputStream());
            outWrite.print(reqData);
            outWrite.flush();
            urlConn.connect();
            //获取数据流
            inpStream = urlConn.getInputStream();
            String encode = urlConn.getHeaderField("Content-Encoding");
            //如果通过gzip
            if (encode != null && encode.indexOf("gzip") != -1) {
                Log.v(TAG, "get data :" + encode);
                inpStream = new GZIPInputStream(inpStream);
            } else if (encode != null && encode.indexOf("deflate") != -1) {
                inpStream = new InflaterInputStream(inpStream);
            }
            reader = new BufferedReader(new InputStreamReader(inpStream));
            while ((str = reader.readLine()) != null) {
                data += str;
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null && urlConn != null) {
                try {
                    outWrite.close();
                    inpStream.close();
                    reader.close();
                    urlConn.disconnect();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        Log.d(TAG, "[Http data][" + baseUrl + "]:" + data);
        return data;
    }

    /**
     * 获取Image信息
     *
     * @return
     */
    public static Bitmap getBitmapData(String imgUrl) {
        Bitmap bmp = null;
        Log.d(TAG, "get imgage:" + imgUrl);
        InputStream inpStream = null;
        try {
            URL url = new URL(imgUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                InputStream in = connection.getInputStream();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException(connection.getResponseMessage() +
                            ": with " +
                            imgUrl);
                }
                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                //获取数据流
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (inpStream != null) {
                    try {
                        inpStream.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;
    }

    /**
     * 获取url的InputStream
     *
     * @param urlStr
     * @return
     */
    public static InputStream getInputStream(String urlStr,String file) {
        Log.d(TAG, "get http input:" + urlStr);
        InputStream inpStream = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                InputStream in = connection.getInputStream();
                System.out.println("in======");
                MyTestUtil.print(in);
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException(connection.getResponseMessage() +
                            ": with " +
                            urlStr);
                }
                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                File f = null;
                f = new File(file);
                String path = f.getParent();
                if(!createPath(path)){
                    Log.e(TAG, "can't create dir:"+path);
                    return null;
                }
                if(!f.exists()){
                    f.createNewFile();
                }
                OutputStream outSm = new FileOutputStream(f);
                while ((bytesRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                    outSm.write(buffer);
                    System.out.println("byteread="+bytesRead);
                }

                outSm.flush();
                outSm.close();
                out.close();
                //获取数据流
                inpStream = in;
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (inpStream != null) {
                    try {
                        inpStream.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inpStream;
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

}