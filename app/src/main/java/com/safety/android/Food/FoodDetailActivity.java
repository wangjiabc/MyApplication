package com.safety.android.Food;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

public class FoodDetailActivity extends AppCompatActivity {

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

        String jsonString=intent.getStringExtra("jsonString");

        if(jsonString!=null&&!jsonString.equals("")){

            try {

                JSONObject jsonObject = new JSONObject(jsonString);

                String name = jsonObject.getString("name");
                Double storage = jsonObject.getDouble("storage");
                Double cost = jsonObject.getDouble("cost");
                Double retailprice = jsonObject.getDouble("retailprice");

                editText1.setText(name);
                editText2.setText(String.valueOf(storage));
                editText3.setText(String.valueOf(cost));
                editText4.setText(String.valueOf(retailprice));

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        setContentView(view);

    }
}
