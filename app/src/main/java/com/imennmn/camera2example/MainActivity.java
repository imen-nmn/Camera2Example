package com.imennmn.camera2example;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.imennmn.camera2example.recordVideo.VideoFragment.OnMediaFragmentInteraction;
import com.imennmn.camera2example.takePhoto.CameraFragment.OnFragmentInteractionListener;
import com.imennmn.camera2example.takePhoto.TakePhotoFragment;
import com.imennmn.camera2example.takePhoto.TakePhotoFragment.TakePhotoInteractionListener;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener,
        TakePhotoInteractionListener, OnMediaFragmentInteraction {

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        replaceFragment(new TakePhotoFragment(), false) ;
    }


    public void replaceFragment(Fragment fragment, boolean withBackStack) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        String fragmentTag = fragment.getClass().getName();
        fragmentTransaction.replace(R.id.frame, fragment, fragmentTag);
        if (withBackStack)
            fragmentTransaction.addToBackStack(fragmentTag);
        try {
            fragmentTransaction.commit();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void showLoading() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Loading ...");
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
    }

    @Override
    public void showMessage(String messageToShow) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.app_name);
        alertDialogBuilder.setMessage(messageToShow).setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
