package com.safety.android.HiddenNeaten;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

public class FoodActivity extends AppCompatActivity {

    private View view;

    private EditText editText1;

    private EditText editText2;

    private EditText editText3;

    private EditText editText4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        view= LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_food,null);

        editText1=view.findViewById(R.id.food1);
        editText2=view.findViewById(R.id.food2);
        editText3=view.findViewById(R.id.food3);
        editText4=view.findViewById(R.id.food4);

        Intent intent=getIntent();

        if(intent!=null){
            String jsonString=intent.getStringExtra("jsonString");

            try {

                JSONObject jsonObject = new JSONObject(jsonString);

                String name = jsonObject.getString("name");
                Integer storage = jsonObject.getInt("storage");
                Integer cost = jsonObject.getInt("cost");

                editText1.setText(name);
                editText2.setText(String.valueOf(storage));
                editText3.setText(String.valueOf(cost));

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        setContentView(view);

    }
}
