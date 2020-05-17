package com.safety.android.PhotoGallery;

import androidx.fragment.app.Fragment;

import com.safety.android.SingleFragmentActivity;


public class PhotoGalleryActivity extends SingleFragmentActivity {
    
    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }

}
