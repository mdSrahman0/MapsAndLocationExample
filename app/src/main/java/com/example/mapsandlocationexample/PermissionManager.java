package com.example.mapsandlocationexample;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.content.ContentValues.TAG;

public class PermissionManager {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 667;
    IPermissionManager iPermissionManager;
    Context context;

    public PermissionManager(Context context){
        this.iPermissionManager = (IPermissionManager)context;
        this.context = context;
    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)context,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                final AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("Need location Permission")
                        .setMessage("We need fine location permission for reasons...")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // if they accept, it just dismisses the dialog box. doesn't mean they accepted permission
                                dialogInterface.dismiss();
                            }
                        }).create();
                alertDialog.show();
            } else {
                requestPermission();

            }
        } else {
            iPermissionManager.onPermissionResult(true);
        }
    }

    // this generates a system dialog to request permission
    public void requestPermission() {
        Log.d("TAG", "onCreate: No explanation needed; request the permission");
        ActivityCompat.requestPermissions((Activity) context,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);
    }

    public void checkResult(int requestCode,
                            String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, "onRequestPermissionsResult: permission was granted");
                    iPermissionManager.onPermissionResult(true);

                } else {

                    iPermissionManager.onPermissionResult(false);

                    Log.d(TAG, "onRequestPermissionsResult: permission denied");

                }
                return;
            }
        }
    }

    public interface IPermissionManager{
        void onPermissionResult(boolean isGranted);
    }
}
