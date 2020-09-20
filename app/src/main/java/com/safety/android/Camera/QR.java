package com.safety.android.Camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.R;
import com.safety.android.Sale.SaleActivity;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.tools.TakePictures;
import com.safety.android.zxinglib.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class QR extends AppCompatActivity {

    private Button button;

    private View view;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        view= LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_qr, null);
        setContentView(view);

                int hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(),
                        Manifest.permission.CAMERA);
                if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
                    //有权限。
                } else {
                    //没有权限，申请权限。
                    requestPermissions(new String[]{Manifest.permission.CAMERA},100);
                }
                TakePictures takePictures=new TakePictures(getApplication());
                Intent intent=new Intent(getApplicationContext(), CaptureActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 100);
                    startActivityForResult(intent,1001);
                }else{
                    startActivityForResult(intent,1001);
                }


                button=view.findViewById(R.id.button);


                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(),
                                Manifest.permission.CAMERA);
                        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
                            //有权限。
                        } else {
                            //没有权限，申请权限。
                            requestPermissions(new String[]{Manifest.permission.CAMERA},100);
                        }
                        TakePictures takePictures=new TakePictures(getApplication());
                        Intent intent=new Intent(getApplicationContext(), CaptureActivity.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 100);
                            startActivityForResult(intent,1001);
                        }else{
                            startActivityForResult(intent,1001);
                        }
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1001 && resultCode== Activity.RESULT_OK)
        {
            String result=data.getStringExtra(CaptureActivity.KEY_DATA);
            Toast.makeText(this, "qrcode result is "+result, Toast.LENGTH_SHORT).show();
            new FetchItemsTask().execute(result);
        }
    }


    private class FetchItemsTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {

            String code=params[0];

            return new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/barCode/barCode/getFoodByCode?code="+code);
        }


        @Override
        protected void onPostExecute(String json) {

            Intent intent = new Intent(getApplicationContext(), SaleActivity.class);

            try {

                JSONObject jsonObject = new JSONObject(json);
                String success = jsonObject.optString("success", null);

                if(success.equals("true")){

                    JSONArray result= (JSONArray) jsonObject.get("result");

                    if(result!=null&&result.length()>0){

                        JSONObject jsonObject1=new JSONObject();


                        JSONObject jsonObject2= (JSONObject) result.get(0);

                        JSONObject jsonObject3=new JSONObject();

                        jsonObject3.put("name",jsonObject2.get("NAME"));
                        jsonObject3.put("retailprice",jsonObject2.get("RETAILPRICE"));
                        jsonObject3.put("id",jsonObject2.get("ID"));
                        jsonObject3.put("code",jsonObject2.get("CODE"));

                        JSONArray jsonArray=new JSONArray();

                        jsonArray.put(jsonObject3);

                        jsonObject1.put("ids",jsonArray);

                        intent.putExtra("jsonString", jsonObject1.toString());

                        startActivityForResult(intent, 1);

                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


}
