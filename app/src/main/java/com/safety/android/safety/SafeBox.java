package com.safety.android.safety;

import android.content.Context;

import com.safety.android.safety.SQLite3.SafeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WangJing on 2017/6/20.
 */

public class SafeBox {
    private static final String TAG="BeatBox";

    private List<SafeInfo> mSafeInfos=new ArrayList<>();


    public SafeBox(Context context){

        loadSounds();
    }



    private void loadSounds(){


        for (int i=0;i<9;i++){
                String name="name"+(i+1);
                SafeInfo mSafeInfo = new SafeInfo();
                mSafeInfo.setmName(name);
                mSafeInfo.setId(i);
                mSafeInfos.add(mSafeInfo);

        }
    }


    public List<SafeInfo> getmSafe(){
        return mSafeInfos;
    }

}
