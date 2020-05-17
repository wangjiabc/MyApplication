package com.safety.android.Camera;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.example.myapplication.R;


/**
 * Created by WangJing on 2017/6/16.
 */

public class PhotoDialogFragment extends DialogFragment {
    Bitmap bitmap;

    public PhotoDialogFragment newInstance(Bitmap bitmap){
        Bundle args=new Bundle();
        this.bitmap=bitmap;
        PhotoDialogFragment fragment=new PhotoDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        View view= LayoutInflater.from(getActivity()).inflate(R.layout.photo_dialog,null);
        ImageView v= (ImageView) view.findViewById(R.id.photo_dialog);
        v.setImageBitmap(bitmap);
        return new AlertDialog.Builder(getActivity())
                .setView(view).create();
    }

    public void show(FragmentManager manager, String s) {
    }
}
