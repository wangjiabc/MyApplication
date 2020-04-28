package com.safety.android.safety.SQLite3.com.safety.android.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;


import com.example.myapplication.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by WangJing on 2017/5/31.
 */

public class DatePickerFragment extends DialogFragment {

    public static final String EXTRA_DATE="com.bignerdranch.android.criminalintent.date";

    private static final String ARG_DATE="date";
    private DatePicker mDatePicker;

    private Button mDateButton;

    public static DatePickerFragment newInstance(Date date){
        Bundle args=new Bundle();
        args.putSerializable(ARG_DATE,date);

        DatePickerFragment fragment=new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        Date date= (Date) getArguments().getSerializable(ARG_DATE);

        Calendar calendar= Calendar.getInstance();
        calendar.setTime(date);
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH);
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        final int hour=calendar.get(Calendar.HOUR_OF_DAY);
        final int minute=calendar.get(Calendar.MINUTE);

        View v=inflater.inflate(R.layout.dialog_date,container,false);

        mDatePicker= (DatePicker) v.findViewById(R.id.dialog_date_date_picker);
        mDatePicker.init(year,month,day,null);

        mDateButton= (Button) v.findViewById(R.id.dialog_data_confirm);

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year=mDatePicker.getYear();
                int month=mDatePicker.getMonth();
                int day=mDatePicker.getDayOfMonth();
                Date date=new GregorianCalendar(year,month,day,hour,minute).getTime();
                sendResult(Activity.RESULT_OK,date);
            }
        });

        return  v;
    }

    private void sendResult(int resultCode,Date date){
        if(getTargetFragment()==null){
            return;
        }

        Intent intent=new Intent();
        intent.putExtra(EXTRA_DATE,date);

        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
    }

}
