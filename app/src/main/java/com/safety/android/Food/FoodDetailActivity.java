package com.safety.android.Food;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.safety.android.SQLite3.PermissionInfo;
import com.safety.android.SQLite3.PermissionLab;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch;
import com.safety.android.tools.TakePictures;
import com.safety.android.tools.UpFileToQiniu;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import okhttp3.FormBody;

import static com.safety.android.tools.TakePictures.REQUEST_PHOTO;

public class FoodDetailActivity extends AppCompatActivity {

    private View view;

    private TextView foodValidateinfo;

    private TextView textFood2;

    private EditText editText1;

    private EditText editText2;

    private EditText editText3;

    private EditText editText4;

    private Button foodButton;

    private int storage;

    private int id;

    private boolean isEdit=false;

    private int position=0;

    private boolean isCost=false;

    private ImageButton mPhotoButton;

    private ImageView mPhotoView;

    private String img=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        view= LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_food,null);

        foodValidateinfo=view.findViewById(R.id.food_validateinfo);
        textFood2=view.findViewById(R.id.text_food2);
        editText1=view.findViewById(R.id.food1);
        editText2=view.findViewById(R.id.food2);
        editText3=view.findViewById(R.id.food3);
        editText4=view.findViewById(R.id.food4);

        textFood2.setVisibility(View.GONE);
        editText2.setVisibility(View.GONE);

        foodButton=view.findViewById(R.id.food_button);
        mPhotoView=view.findViewById(R.id.food_photo);
        mPhotoButton=view.findViewById(R.id.food_camera);

        List<PermissionInfo> permissionInfos= PermissionLab.get(getApplicationContext()).getPermissionInfo();

        Iterator<PermissionInfo> iterator=permissionInfos.iterator();

        while (iterator.hasNext()){

            PermissionInfo permissionInfo=iterator.next();
            String action=permissionInfo.getAction();
            if(action!=null&&action.equals("materialList:cost")){
                isCost=true;
                textFood2.setVisibility(View.VISIBLE);
                editText2.setVisibility(View.VISIBLE);
                continue;
            }

        }


        mPhotoButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                int hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(),
                        Manifest.permission.CAMERA);
                if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
                    //有权限。
                } else {
                    //没有权限，申请权限。
                    requestPermissions(new String[]{Manifest.permission.CAMERA},100);
                }
                TakePictures takePictures=new TakePictures(getApplication());
                Intent captureImage=takePictures.getCaptureImage();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 100);
                    startActivityForResult(captureImage, REQUEST_PHOTO);
                }else{
                    startActivityForResult(captureImage, REQUEST_PHOTO);
                }

            }
        });

        Intent intent=getIntent();

        String jsonString=intent.getStringExtra("jsonString");

        if(jsonString!=null&&!jsonString.equals("")){
            foodValidateinfo.setText("编辑商品");
            isEdit=true;
            try {

                JSONObject jsonObject = new JSONObject(jsonString);

                id=jsonObject.getInt("id");
                position=jsonObject.getInt("position");
                String name = jsonObject.getString("name");
                Double cost = jsonObject.getDouble("cost");
                Double retailprice = jsonObject.getDouble("retailprice");
                String remark = jsonObject.getString("remark");

                editText1.setText(name);
                editText2.setText(String.valueOf(cost));
                editText3.setText(String.valueOf(retailprice));
                editText4.setText(remark);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else {
            foodValidateinfo.setText("新建商品");
        }

        foodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map map=new HashMap<String, String>();

                String text1=editText1.getText().toString();
                String text2=null;
                if(isCost)
                    text2=editText2.getText().toString();
                String text3=editText3.getText().toString();
                String text4=editText4.getText().toString();

                map.put("name",text1);
                if(isCost)
                    map.put("retailprice",text2);
                map.put("cost",text3);
                map.put("remark",text4);

                new FetchItemsTask().execute(map);

            }
        });

        setContentView(view);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("requestCode===="+requestCode+"         resultCode==="+resultCode);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }else if(requestCode==REQUEST_PHOTO){
            System.out.println("REQUEST_PHOTO");
            Bitmap bitmap=TakePictures.getScaledBitmap(null,null);
            updatePhotoView(bitmap);
            String key= UUID.randomUUID().toString()+".jpg";
            img=key;
            new UpFileToQiniu(key);

        }

    }

    private class FetchItemsTask extends AsyncTask<Map<String,String>,Void,String> {

        @Override
        protected String doInBackground(Map<String,String>... params) {

            FormBody.Builder builder = new FormBody.Builder();

            JSONObject jsonObject=new JSONObject();

            for(Map<String,String> map:params){

                for(Map.Entry<String, String> a:map.entrySet()){

                    System.out.println("键是"+a.getKey());

                    System.out.println("值是"+a.getValue());

                //    builder.addEncoded(a.getKey(),a.getValue());

                    try {
                        jsonObject.put(a.getKey(),a.getValue());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            String url;
            String type;
            if(isEdit){
                url = "/food/material/edit";
                type="put";
                try {
                    jsonObject.put("id",id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                url = "/food/material/add";
                type="post";
            }


            return new OKHttpFetch(getApplicationContext()).post(FlickrFetch.base + url,jsonObject,type);

        }

        @Override
        protected void onPostExecute(String json) {

            JSONObject jsonObject = null;

            try {

                jsonObject = new JSONObject(json);

                String success = jsonObject.optString("success", null);

                Toast.makeText(FoodDetailActivity.this, jsonObject.optString("message"), Toast.LENGTH_LONG).show();

                if (success.equals("true")) {

                    String text1=editText1.getText().toString();
                    String text2=editText2.getText().toString();
                    String text3=editText3.getText().toString();
                    String text4=editText4.getText().toString();

                    JSONObject jsonObject1 =new JSONObject();
                    if(isEdit) {
                        jsonObject1.put("type", 0);
                        jsonObject1.put("position",position);
                    }else {
                        jsonObject1.put("type",1);
                    }
                    jsonObject1.put("storage",storage);
                    jsonObject1.put("name",text1);
                    jsonObject1.put("retailprice",text2);
                    jsonObject1.put("cost",text3);
                    jsonObject1.put("remark",text4);

                    if(img!=null)
                        jsonObject1.put("img",img);

                    Intent intent = new Intent();
                    intent.putExtra("value", jsonObject1.toString());
                    setResult(Activity.RESULT_OK, intent);
                    finish();

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }


    private void updatePhotoView(Bitmap bitmap0){
        ViewTreeObserver observer=mPhotoView.getViewTreeObserver();
        Log.i("tag sdkint=", String.valueOf(Build.VERSION.SDK_INT));
        final Bitmap bitmap=bitmap0;
        if(Build.VERSION.SDK_INT > 18) {

            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                int w,h;
                @Override
                public void onGlobalLayout() {

                    mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    w=mPhotoView.getMeasuredWidth();
                    h=mPhotoView.getMeasuredHeight();
                    int width=bitmap.getWidth();
                    int height=bitmap.getHeight()*w/width;
                    mPhotoView.setScaleType(ImageView.ScaleType.FIT_XY);
                    mPhotoView.setLayoutParams(new LinearLayout.LayoutParams(w,height));
                    mPhotoView.setImageBitmap(bitmap);

                }
            });
        }else {
                mPhotoView.setImageBitmap(bitmap);
        }
    }

}
