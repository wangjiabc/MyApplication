package com.safety.android.HiddenNeaten;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class FoodActivity extends AppCompatActivity {

    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        view= LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_food,null);

        setContentView(view);

    }
}
