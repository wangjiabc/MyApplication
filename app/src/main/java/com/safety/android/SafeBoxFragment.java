package com.safety.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.myapplication.R;
import com.safety.android.AccountheadList.AccountheadListActivity;
import com.safety.android.Camera.QR;
import com.safety.android.Food.FoodClassifyActivity;
import com.safety.android.Inoutitem.InoutitemActivity;
import com.safety.android.Management.ManageMainActivity;
import com.safety.android.ReportDetail.ReportDetailActivity;
import com.safety.android.SQLite3.PermissionInfo;
import com.safety.android.SQLite3.PermissionLab;
import com.safety.android.SQLite3.SafeInfo;
import com.safety.android.Sale.SaleClassifyActivity;
import com.safety.android.Storage.StorageClassActivity;
import com.safety.android.Storage.StorageLogListActivity;
import com.safety.android.util.phone;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Created by WangJing on 2017/6/19.
 */

public class SafeBoxFragment extends Fragment {
    private AlertDialog dialog;

    private  static final int REQUEST_CONTACT=1;
    private static final int REQUEST_DATE=0;

    private List<SafeInfo> mSafeInfos=new ArrayList<>();

    public static SafeBoxFragment newInstance(){
        return new SafeBoxFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        List<PermissionInfo> list= PermissionLab.get(getContext()).getPermissionInfo();

        Iterator<PermissionInfo> iterator=list.iterator();
/*
        String name0="name"+0;
        SafeInfo mSafeInfo0 = new SafeInfo();
        mSafeInfo0.setmName(name0);
        mSafeInfo0.setId(0);
        mSafeInfos.add(mSafeInfo0);
*/
        while (iterator.hasNext()){

            PermissionInfo permissionInfo=iterator.next();

            String action=permissionInfo.getAction();
            String component=permissionInfo.getComponent();

            if(component!=null) {
                System.out.println("component==="+component);
                if (component.equals("food/MaterialList")) {
                    String name = "商品管理";
                    SafeInfo mSafeInfo = new SafeInfo();
                    mSafeInfo.setmName(name);
                    mSafeInfo.setId(0);
                    mSafeInfos.add(mSafeInfo);
                }

                if (component.equals("sale/SaleList")) {
                    String name = "销售/开单";
                    SafeInfo mSafeInfo = new SafeInfo();
                    mSafeInfo.setmName(name);
                    mSafeInfo.setId(1);
                    mSafeInfos.add(mSafeInfo);
                }

                if (component.equals("accounthead/AccountheadList")) {
                    String name = "销售记录";
                    SafeInfo mSafeInfo = new SafeInfo();
                    mSafeInfo.setmName(name);
                    mSafeInfo.setId(2);
                    mSafeInfos.add(mSafeInfo);
                }

                if (component.equals("storage/StorageList")) {
                    String name = "实库";
                    SafeInfo mSafeInfo = new SafeInfo();
                    mSafeInfo.setmName(name);
                    mSafeInfo.setId(3);
                    mSafeInfos.add(mSafeInfo);
                }

                if (component.equals("storageLog/StorageLogList")) {
                    String name = "进货记录";
                    SafeInfo mSafeInfo = new SafeInfo();
                    mSafeInfo.setmName(name);
                    mSafeInfo.setId(4);
                    mSafeInfos.add(mSafeInfo);
                }

                if (component.equals("inoutitem/InoutitemList")) {
                    String name = "开支";
                    SafeInfo mSafeInfo = new SafeInfo();
                    mSafeInfo.setmName(name);
                    mSafeInfo.setId(5);
                    mSafeInfos.add(mSafeInfo);
                }

                if (component.equals("reportDetail/reportDetail")) {
                    String name = "统计";
                    SafeInfo mSafeInfo = new SafeInfo();
                    mSafeInfo.setmName(name);
                    mSafeInfo.setId(6);
                    mSafeInfos.add(mSafeInfo);
                }

            }
        }

      //  if(LunchActivity.username.equals("lubo")||LunchActivity.username.equals("admin")) {
            SafeInfo mSafeInfo = new SafeInfo();
            mSafeInfo.setmName("管理后台");
            mSafeInfo.setId(7);
            mSafeInfos.add(mSafeInfo);
      // }
/*
        SafeInfo mSafeInfo = new SafeInfo();
        mSafeInfo.setmName("QR");
        mSafeInfo.setId(8);
        mSafeInfos.add(mSafeInfo);
*/
        /*
        for (int i=0;i<9;i++){
            String name="name"+(i+1);
            SafeInfo mSafeInfo = new SafeInfo();
            mSafeInfo.setmName(name);
            mSafeInfo.setId(i);
            mSafeInfos.add(mSafeInfo);

        }
        */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.fragment_safe_box,container,false);

        RecyclerView recycleListView= (RecyclerView) view
                .findViewById(R.id.fragment_safe_box_recycler_view);
        recycleListView.setLayoutManager(new GridLayoutManager(null,3));
        recycleListView.setAdapter(new SafeAdapter(mSafeInfos));

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

                      //  Intent intent=new Intent(getActivity(), FoodListActivity.class);
                     //   startActivity(intent);

                        Intent intent=new Intent(getActivity(), FoodClassifyActivity.class);
                        startActivity(intent);

                    }
                };
            }else
            if(i==2) {
                mButton.setBackground(getResources().getDrawable(R.drawable.button_box2));
                onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent=new Intent(getActivity(), SaleClassifyActivity.class);
                        startActivity(intent);
                    }
                };
            }else
            if(i==3) {
                mButton.setBackground(getResources().getDrawable(R.drawable.button_box3));
                onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent=new Intent(getActivity(), AccountheadListActivity.class);
                        startActivity(intent);

                    }
                };
            }else
            if(i==4){
                mButton.setBackground(getResources().getDrawable(R.drawable.button_box4));
                onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(getActivity(), StorageClassActivity.class);
                        startActivity(intent);
                    }
                };
            }else
            if(i==5) {
                mButton.setBackground(getResources().getDrawable(R.drawable.button_box5));
                onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       /*
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
                        */
                        Intent intent=new Intent(getActivity(), StorageLogListActivity.class);
                        startActivity(intent);
                    }
                };
            }else
            if(i==6) {
                mButton.setBackground(getResources().getDrawable(R.drawable.button_box6));
                onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), InoutitemActivity.class);
                        startActivity(intent);
                    }
                };
            }else
            if(i==7) {
                mButton.setBackground(getResources().getDrawable(R.drawable.button_box7));
                onClickListener=new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(getActivity(), ReportDetailActivity.class);
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
                        Intent intent=new Intent(getActivity(), ManageMainActivity.class);
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
                        Intent intent=new Intent(getActivity(), QR.class);
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
