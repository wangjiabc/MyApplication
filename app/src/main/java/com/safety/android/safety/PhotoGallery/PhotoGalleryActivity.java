package com.safety.android.safety.PhotoGallery;

import androidx.fragment.app.Fragment;

import com.safety.android.safety.SingleFragmentActivity;


public class PhotoGalleryActivity extends SingleFragmentActivity {
    
    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }

}
