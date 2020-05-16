package com.safety.android.safety.Camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.myapplication.R;
import com.safety.android.safety.SQLite3.SafeInfo;
import com.safety.android.safety.SQLite3.SafeLab;
import com.safety.android.util.TimePackerFragment;
import com.safety.android.util.phone;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class CameraFragment extends Fragment {

    private static final String ARG_CRIME_ID="crime_id";
    private static final String DIALOG_DATE="DialogDate";

    private static final int REQUEST_DATE=0;
    private  static final int REQUEST_CONTACT=1;
    private static final int REQUEST_PHOTO=2;

    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mPickContactButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;

    private Button mTimePicker;
    private Button mReportButton;
    private Button mPhone;

    private SafeInfo mSafeInfo;

    public static CameraFragment newInstance(UUID crimeId){
        Bundle args=new Bundle();
        args.putSerializable(ARG_CRIME_ID,crimeId);

        CameraFragment fragment=new CameraFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID crimeId= (UUID) getArguments().getSerializable(ARG_CRIME_ID);

        if(crimeId==null){
            mSafeInfo=new SafeInfo();
            SafeLab.get(getActivity()).addSafeInfo(mSafeInfo);
        }else {
            mSafeInfo=SafeLab.get(getActivity()).getSafeInfo(crimeId);
        }

        mPhotoFile= SafeLab.get(getActivity()).getPhotoFile(mSafeInfo);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    onDialog ondialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_camera, container, false);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mSafeInfo.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSafeInfo.setTitle(s.toString());
                Log.d("tag","tttttt "+s.toString());
                updateSafeInfo();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton= (Button) v.findViewById(R.id.crime_date);
        updateDate();
        Calendar calendar=Calendar.getInstance();
        String week=swicthWeek(calendar.get(Calendar.DAY_OF_WEEK)-1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 星期"+week +" HH时mm分ss秒");


        mDateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                FragmentManager manager=getFragmentManager();
                ondialog=new onDialog();
              //  DatePickerFragment dialog=ondialog.getDialog();
              //  dialog.show(manager,DIALOG_DATE);
            }
        });


        mTimePicker= (Button) v.findViewById(R.id.time_picker);
        mTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager=getFragmentManager();
                TimePackerFragment dialog=TimePackerFragment.newInstance(new Date());
                dialog.show(manager,DIALOG_DATE);
            }
        });


        mReportButton= (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                ShareCompat.IntentBuilder i = ShareCompat.IntentBuilder.from(getActivity());
                i.setType("text/plain");
                i.setChooserTitle(getString(R.string.send_report));
                i.startChooser();
            }
        });

        mPhone= (Button) v.findViewById(R.id.crime_phone);
        mPhone.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                String phoneNumber=null;
                String name;
                name=mSafeInfo.getmSuspect();
                phoneNumber=mSafeInfo.getPhoneNumber();
                 Log.d("tag phoneNumber=", String.valueOf(phoneNumber)+"       naem="+name);
                if(phoneNumber!=null) {
                    Uri number = Uri.parse("tel:" + phoneNumber);
                    Intent i=new Intent(Intent.ACTION_DIAL,number);
                    startActivity(i);
                 }
            }
        });

        final Intent pickContact=new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mPickContactButton= (Button) v.findViewById(R.id.crime_suspect);
        mPickContactButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //检查SDK版本；如果它比Android 6.0更大,便向用户请求READ_CONTACTS权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 100);
                    //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
                    startActivityForResult(pickContact, REQUEST_CONTACT);
                }else {
                    startActivityForResult(pickContact, REQUEST_CONTACT);
                }
            }
        });



        PackageManager packageManager=getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY)==null){
            mPickContactButton.setEnabled(false);
        }

        mPhotoButton= (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto=mPhotoFile!=null&&
                captureImage.resolveActivity(packageManager)!=null;

        if(canTakePhoto){
          //  Uri uri=Uri.fromFile(mPhotoFile);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(this.getContext(), "com.safety.android.safety", mPhotoFile);
            } else {
                uri=Uri.fromFile(mPhotoFile);
            }
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);
            Log.d("tag uri mphotofile",mPhotoFile.toString());
        }

        mPhotoButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                captureImage.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 100);
                    startActivityForResult(captureImage, REQUEST_PHOTO);
                    updatePhotoView();
                }else{
                    startActivityForResult(captureImage, REQUEST_PHOTO);
                    updatePhotoView();
                }
            }
        });

        mPhotoView= (ImageView) v.findViewById(R.id.crime_photo);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap= PictureUtils.getScaledBitmap(mPhotoFile.getPath(),getActivity());

                FragmentManager manager=getFragmentManager();
                PhotoDialogFragment dialog=new PhotoDialogFragment();
                dialog.newInstance(bitmap);
                dialog.show(manager,"");

            }
        });

        updatePhotoView();
        return v;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.fragment_delete,menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode!= Activity.RESULT_OK){
            return;
        }

        if(requestCode==REQUEST_DATE){
            if(ondialog!=null) {
                //DatePickerFragment dialog = ondialog.getDialog();
                //dialog.dismiss();
            }
        }else if(requestCode==REQUEST_CONTACT&&data!=null){
            Uri contactUri=data.getData();

            String [] queryFields=new String []{
                    ContactsContract.Contacts.DISPLAY_NAME
            };

            Cursor cursor=getActivity().getContentResolver()
                    .query(contactUri,queryFields,null,null,null);

            try {
                if (cursor.getCount() == 0) {
                    return;
                }

                cursor.moveToFirst();
                String suspect = cursor.getString(0);



                mPickContactButton.setText(suspect);
                mSafeInfo.setmSuspect(suspect);

                Map a= phone.getContacts(this.getActivity());

                for(Object key:a.keySet()){
                   if(suspect.equals(key)) {
                       mSafeInfo.setPhoneNumber((String) a.get(key));
                   }
                }

                }finally {
                cursor.close();
               }
         }else if(requestCode==REQUEST_PHOTO){
            updatePhotoView();
            updateSafeInfo();
        }
    }

    private void updateSafeInfo(){
        SafeLab.get(getActivity()).updateCrime(mSafeInfo);
    }

    public class onDialog{
       /* DatePickerFragment dialog= DatePickerActivity.newInstance(new Date());


        DatePickerFragment getDialog(){
            dialog.setTargetFragment(CameraFragment.this, REQUEST_DATE);
            return dialog;
        }
*/
    }

    String swicthWeek(int i){
        String week="";
        switch (i){
            case 0:week="天";break;
            case 1:week="一";break;
            case 2:week="二";break;
            case 3:week="三";break;
            case 4:week="四";break;
            case 5:week="五";break;
            case 6:week="六";break;
        }
        return week;
    }


    private void updatePhotoView(){
        ViewTreeObserver observer=mPhotoView.getViewTreeObserver();
        Log.i("tag sdkint=", String.valueOf(Build.VERSION.SDK_INT));
        if(Build.VERSION.SDK_INT > 18) {

            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int w,h;
            @Override
            public void onGlobalLayout() {
                mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                w=mPhotoView.getMeasuredWidth();
                h=mPhotoView.getMeasuredHeight();
                if(mPhotoFile==null||!mPhotoFile.exists()){

                }else {
                    Bitmap bitmap=PictureUtils.getScaledBitmap(mPhotoFile.getPath(),getActivity());
                    int width=bitmap.getWidth();
                    int height=bitmap.getHeight()*w/width;
                    mPhotoView.setScaleType(ImageView.ScaleType.FIT_XY);
                    mPhotoView.setLayoutParams(new LinearLayout.LayoutParams(w,height));
                    mPhotoView.setImageBitmap(bitmap);

                }
            }
         });
       }else {
           if (mPhotoFile == null || !mPhotoFile.exists()) {

           } else {
               Bitmap bitmap = PictureUtils.getScaledBitmap(
                       mPhotoFile.getPath(), getActivity());
               mPhotoView.setImageBitmap(bitmap);
           }
       }
    }

    private void updateDate(){
        Date date=mSafeInfo.getmDate();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        String week=swicthWeek(calendar.get(Calendar.DAY_OF_WEEK)-1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 星期"+week +" HH时mm分ss秒");
        String mDate=sdf.format(date);
        mDateButton.setText(mDate);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DisplayMetrics dm =getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;

        switch (item.getItemId()) {
            case R.id.menu_item_del_crime:
                UUID id=mSafeInfo.getmId();
                SafeLab safeLab=SafeLab.get(getActivity());
                Log.d("tag del id=",id.toString());
                safeLab.delCrime(id);
                if(w_screen<h_screen) {
                    getActivity().finish();
                }else{
                    updateSafeInfo();
                }
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
