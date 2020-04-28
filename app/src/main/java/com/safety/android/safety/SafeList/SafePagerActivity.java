package com.safety.android.safety.SafeList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.example.myapplication.R;
import com.safety.android.safety.Camera.CameraFragment;

import com.safety.android.safety.SQLite3.SafeInfo;
import com.safety.android.safety.SQLite3.SafeLab;

import java.util.List;
import java.util.UUID;

/**
 * Created by WangJing on 2017/5/30.
 */

public class SafePagerActivity extends AppCompatActivity
implements SafeListFragment.Callbacks{
    private static final String EXTRA_SAFE_ID=
            "com.safety.android.safety.safe_id";

    private ViewPager mViewPager;
    private List<SafeInfo> mSafeInfos;

    public static Intent newIntent(Context packageContext, UUID crimeId){
        Intent intent=new Intent(packageContext,SafePagerActivity.class);
        intent.putExtra(EXTRA_SAFE_ID,crimeId);
        Log.d("cccc",crimeId.toString());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_pager);

        UUID crimeId= (UUID) getIntent().getSerializableExtra(EXTRA_SAFE_ID);

        mViewPager= (ViewPager) findViewById(R.id.activity_crime_pager_view_pager);

        mSafeInfos= SafeLab.get(this).getSafeInfo();
        FragmentManager fragmentManager=getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                SafeInfo mSafeInfo=mSafeInfos.get(position);
                return CameraFragment.newInstance(mSafeInfo.getmId());
            }

            @Override
            public int getCount() {
                return mSafeInfos.size();
            }
        });

        for(int i=0;i<mSafeInfos.size();i++){
            if(mSafeInfos.get(i).getmId().equals(crimeId)){
                mViewPager.setCurrentItem(i);
                break;
            }
        }

    }


    @Override
    public void onSafeSelected(SafeInfo safeInfo) {
        if(findViewById(R.id.detail_fragment_container)==null){
            Intent intent=SafePagerActivity.newIntent(this,safeInfo.getmId());
            startActivity(intent);
        }else {
            Fragment newDetail=CameraFragment.newInstance(safeInfo.getmId());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container,newDetail)
                    .commit();
        }
    }


}
