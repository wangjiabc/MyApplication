package com.safety.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.myapplication.R;
import com.safety.android.Asset.AssetListActivity;
import com.safety.android.HiddenCheck.HiddenCheckListActivity;
import com.safety.android.HiddenNeaten.HiddenNeatenListActivity;
import com.safety.android.LocalFile.SdCard;
import com.safety.android.Message.Chat2Activity;
import com.safety.android.PhotoGallery.PhotoGalleryActivity;
import com.safety.android.SQLite3.SafeInfo;
import com.safety.android.SQLite3.SafeLab;
import com.safety.android.SafeList.SafeListActivity;
import com.safety.android.SafeList.SafePagerActivity;
import com.safety.android.util.phone;

import java.util.List;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Created by WangJing on 2017/6/19.
 */

public class SafeBoxFragment extends Fragment {
    private SafeBox mBeatBox;
    private AlertDialog dialog;

    private  static final int REQUEST_CONTACT=1;
    private static final int REQUEST_DATE=0;

    public static SafeBoxFragment newInstance(){
        return new SafeBoxFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mBeatBox=new SafeBox(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.fragment_safe_box,container,false);

        RecyclerView recycleListView= (RecyclerView) view
                .findViewById(R.id.fragment_safe_box_recycler_view);
        recycleListView.setLayoutManager(new GridLayoutManager(null,3));
        recycleListView.setAdapter(new SafeAdapter(mBeatBox.getmSafe()));

        return view;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private class SafeHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener{
        private ImageButton mButton;
        private TextView mTextView;
        private View.OnClickListener onClickListener=null;

        public SafeHolder(LayoutInflater inflater,ViewGroup container){
            super(inflater.inflate(R.layout.list_item_box,container,false));

           mButton= (ImageButton) itemView.findViewById(R.id.list_item_imageButton);
            mTextView= (TextView) itemView.findViewById(R.id.list_item_textView);
           mButton.setOnClickListener(this);
        }

        public void bindSafe(SafeInfo safe){
           int i=safe.getId();
            i++;
            if(i==1) {
                mButton.setBackground(getResources().getDrawable(R.drawable.button_box));
                onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*
                        dialog = new AlertDialog.Builder(v.getContext()).setTitle("多选对话框")
                                .setNegativeButton("取消", null).setPositiveButton("确定", null)
                                .create();
                        dialog.show();
                        */

                        Intent intent=new Intent(getActivity(), AssetListActivity.class);
                        startActivity(intent);


                    }
                };
            }else
            if(i==2) {
                mButton.setBackground(getResources().getDrawable(R.drawable.button_box2));
                onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*
                        dialog = new AlertDialog.Builder(v.getContext()).setTitle("多选对话框")
                                .setNegativeButton("取消", null).setPositiveButton("确定", null)
                                .create();
                        dialog.show();
                        */
                        Intent intent=new Intent(getActivity(), HiddenCheckListActivity.class);
                        startActivity(intent);
                    }
                };
            }else
            if(i==3) {
                mButton.setBackground(getResources().getDrawable(R.drawable.button_box3));
                onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(getActivity(), HiddenNeatenListActivity.class);
                        startActivity(intent);

                    }
                };
            }else
            if(i==4){
                mButton.setBackground(getResources().getDrawable(R.drawable.button_box4));
                onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SdCard.getFilePath();
                    }
                };
            }else
            if(i==5) {
                mButton.setBackground(getResources().getDrawable(R.drawable.button_box5));
                onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Intent pickContact=new Intent(Intent.ACTION_PICK,
                                ContactsContract.Contacts.CONTENT_URI);
                        //检查SDK版本；如果它比Android 6.0更大,便向用户请求READ_CONTACTS权限
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 100);
                            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
                            startActivityForResult(pickContact,REQUEST_CONTACT);
                        }else {
                            startActivityForResult(pickContact,REQUEST_CONTACT);
                        }
                    }
                };
            }else
            if(i==6) {
                mButton.setBackground(getResources().getDrawable(R.drawable.button_box6));
                onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), PhotoGalleryActivity.class);
                        startActivity(intent);
                    }
                };
            }else
            if(i==7) {
                mButton.setBackground(getResources().getDrawable(R.drawable.button_box7));
                onClickListener=new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(getActivity(), SafeListActivity.class);
                        startActivity(intent);
                    }
                };
            }else
            if(i==8) {
                mButton.setBackground(getResources().getDrawable(R.drawable.button_box8));

                onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                     //   startActivityForResult(captureImage, REQUEST_PHOTO);
                        SafeInfo mSafeInfo=new SafeInfo();
                        SafeLab.get(getActivity()).addSafeInfo(mSafeInfo);
                        Intent intent=SafePagerActivity.newIntent(getActivity(),mSafeInfo.getmId());
                        startActivity(intent);
                    }
                };
            }else
            if(i==9) {
                // mButton.setImageDrawable(getResources().getDrawable(R.drawable.menu9));
                // mButton.setBackground(getResources().getDrawable(R.drawable.button_box9));
                mButton.setBackground(getResources().getDrawable(R.drawable.button_box9));
                onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(getActivity(), Chat2Activity.class);
                        startActivity(intent);
                    }

                };
            }
            mTextView.setText(safe.getmName());

        }

        @Override
        public void onClick(View v) {
            if(onClickListener!=null)
               onClickListener.onClick(v);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode!= Activity.RESULT_OK){
            return;
        }

        if(requestCode==REQUEST_DATE){

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


                Map a= phone.getContacts(this.getActivity());

                String phoneNumber=null;
                for(Object key:a.keySet()){
                    if(suspect.equals(key)) {
                        phoneNumber=((String) a.get(key));
                    }
                }

                if(phoneNumber!=null) {
                    Uri number = Uri.parse("tel:" + phoneNumber);
                    Intent i=new Intent(Intent.ACTION_DIAL,number);
                    startActivity(i);
                }


            }finally {
                cursor.close();
            }
        }
    }

    private class SafeAdapter extends RecyclerView.Adapter<SafeHolder>{
        private List<SafeInfo> mSafeInfo;

        public SafeAdapter(List<SafeInfo> safeInfo){
            mSafeInfo=safeInfo;
        }

        @Override
        public SafeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater=LayoutInflater.from(getActivity());
            return new SafeHolder(inflater,parent);
        }

        @Override
        public void onBindViewHolder(SafeHolder holder, int position) {
            SafeInfo msafeInfo=mSafeInfo.get(position);
            holder.bindSafe(msafeInfo);
        }

        @Override
        public int getItemCount() {
            return mSafeInfo.size();
        }
    }
}
