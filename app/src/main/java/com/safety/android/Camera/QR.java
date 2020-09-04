package com.safety.android.Camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.R;
import com.safety.android.tools.TakePictures;
import com.safety.android.zxinglib.CaptureActivity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class QR extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        Button startBtn=findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v)
            {
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
        }
    }

}
