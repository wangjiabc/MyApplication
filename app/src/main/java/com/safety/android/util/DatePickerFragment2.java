package com.safety.android.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import androidx.fragment.app.DialogFragment;

/**
 * Created by WangJing on 2017/5/31.
 */

public class DatePickerFragment2 extends DialogFragment {

    public static final String EXTRA_DATE="com.bignerdranch.android.criminalintent.date";

    private static final String ARG_DATE="date";
    private static final String  ARG_ID="id";
    private DatePicker mDatePicker;

    private Button mDateButton;

    private OnLoginInforCompleted mOnLoginInforCompleted;

    public void setOnLoginInforCompleted(OnLoginInforCompleted onLoginInforCompleted) {
        mOnLoginInforCompleted = onLoginInforCompleted;
    }

    public static DatePickerFragment2 newInstance(String id,Date date){
        Bundle args=new Bundle();
        args.putSerializable(ARG_DATE,date);
        args.putSerializable(ARG_ID,id);

        DatePickerFragment2 fragment=new DatePickerFragment2();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        Date date= (Date) getArguments().getSerializable(ARG_DATE);
        final String id= (String) getArguments().getSerializable(ARG_ID);

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

        ((ViewGroup)((ViewGroup)((ViewGroup)((ViewGroup) (((ViewGroup) mDatePicker.getChildAt(0)).getChildAt(1))).getChildAt(0))
                .getChildAt(0)).getChildAt(0)).setVisibility(View.GONE);
        mDateButton= (Button) v.findViewById(R.id.dialog_data_confirm);

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year=mDatePicker.getYear();
                int month=mDatePicker.getMonth();
                int day=mDatePicker.getDayOfMonth();
                Date date=new GregorianCalendar(year,month,day,hour,minute).getTime();
                System.out.println("click");
                sendResult(Activity.RESULT_OK,date);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = formatter.format(date);

                mOnLoginInforCompleted.inputLoginInforCompleted(id,dateString);

                dismiss();
            }
        });


        return  v;
    }

    private void sendResult(int resultCode,Date date){
        if(getTargetFragment()==null){
            System.out.println("getTargetFragment is null");
            return;
        }

        Intent intent=new Intent();
        intent.putExtra(EXTRA_DATE,date);

        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
    }

}
