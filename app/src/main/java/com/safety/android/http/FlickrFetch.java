package com.safety.android.http;

import android.net.Uri;
import android.util.Log;
import android.util.LruCache;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FlickrFetch {

    public static final String URL="http://223.86.150.188:8891";

    public static final String base="/jeecg-boot";

    private static final String TAG = "FlickrFetch";

    private static final String API_KEY = "REPLACE_ME_WITH_A_REAL_KEY";

    private LruCache lruCache=Lru.getLruCache();

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        Log.i(TAG, "s urlspec=" + urlSpec);
        try {
            byte[] value = (byte[]) lruCache.get(urlSpec);
            Log.i(TAG, "value=" + value.toString());
            return value;
        }catch (RuntimeException e) {
            URL url = new URL(urlSpec);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                InputStream in = connection.getInputStream();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException(connection.getResponseMessage() +
                            ": with " +
                            urlSpec);
                }
                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                lruCache.put(urlSpec,out.toByteArray());
                Log.i(TAG, "out.toByteArray=" + out.toByteArray().toString());
                return out.toByteArray();
            } finally {
                connection.disconnect();
            }
        }
    }


    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public String fetchItems(String uri) {

        System.out.print("jsonBody==");

        JSONObject jsonBody = new JSONObject();

        try {
            String url = Uri.parse(URL+uri)
                    .buildUpon()
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            jsonBody = new JSONObject(jsonString);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }

        System.out.print("jsonBody="+jsonBody);

        return jsonBody.toString();
    }

}
