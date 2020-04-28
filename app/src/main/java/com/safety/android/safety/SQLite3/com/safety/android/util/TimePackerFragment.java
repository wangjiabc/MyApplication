package com.safety.android.safety.SQLite3.com.safety.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by WangJing on 2017/5/31.
 */

public class TimePackerFragment extends DialogFragment {
    public static final String EXTRA_TIME="com.bignerdranch.android.criminalintent.date";

    private static final String ARG_DATE="date";
    private TimePicker mTimePicker;

    public static TimePackerFragment newInstance(Date date){
        Bundle args=new Bundle();
        args.putSerializable(ARG_DATE,date);

        TimePackerFragment fragment=new TimePackerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Date date= (Date) getArguments().getSerializable(ARG_DATE);

        final Calendar calendar= Calendar.getInstance();
        calendar.setTime(date);

        final int hour=calendar.get(Calendar.HOUR);
        final int minute=calendar.get(Calendar.MINUTE);

        View v= LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_time,null);

        mTimePicker= (TimePicker) v.findViewById(R.id.dialog_time_picker);
        mTimePicker.setCurrentHour(hour);//6.0以下
        mTimePicker.setCurrentMinute(minute);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.time_picker)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                int year=calendar.get(Calendar.YEAR);
                                int month=calendar.get(Calendar.MONTH);
                                int day=calendar.get(Calendar.DAY_OF_MONTH);
                                int hour=mTimePicker.getCurrentHour();//6.0以下
                                int minute=mTimePicker.getCurrentMinute();

                                Date date=new GregorianCalendar(year,month,day,hour,minute).getTime();
                                sendResult(Activity.RESULT_OK,date);
                            }
                        })
                .create();
    }

    private void sendResult(int resultCode,Date date){
        if(getTargetFragment()==null){
            return;
        }

        Intent intent=new Intent();
        intent.putExtra(EXTRA_TIME,date);

        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
    }

}
