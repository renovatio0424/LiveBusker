package com.example.kimjungwon.livebusker.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.kimjungwon.livebusker.CustomClass.Compass;
import com.example.kimjungwon.livebusker.CustomClass.PreviewCam1;
import com.example.kimjungwon.livebusker.R;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by kimjungwon on 2017-10-27.
 */

public class ARActivity extends AppCompatActivity {
    private static final String TAG = "ARActivity";

    static final int REQUEST_CAMERA = 1;

    private Compass compass;

    private PreviewCam1 mPreviewCam1;
    private SurfaceView mCameraSurfaceView1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        mCameraSurfaceView1 = (SurfaceView) findViewById(R.id.cameraTextureView);

        int permissionCamera = 0;
        mPreviewCam1 = new PreviewCam1(mCameraSurfaceView1);
        float horizontal_angle = 0.86f;
        float vertical_angle = 1.1f;

        Log.d(TAG,"horizontal angle: " + horizontal_angle);

        compass = new Compass(this);
        if(getIntent().hasExtra("src_lat") &&
                getIntent().hasExtra("src_lng") &&
                getIntent().hasExtra("dst_lat") &&
                getIntent().hasExtra("dst_lng")){
            Intent intent = getIntent();
            compass.src = new LatLng(intent.getDoubleExtra("src_lat",0),intent.getDoubleExtra("src_lng",0));
            compass.dst = new LatLng(intent.getDoubleExtra("dst_lat",0),intent.getDoubleExtra("dst_lng",0));
        }

        compass.arrowView = (ImageView) findViewById(R.id.main_image_dial);

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            permissionCamera = checkSelfPermission(Manifest.permission.CAMERA);
//            if(permissionCamera == PackageManager.PERMISSION_DENIED) {
//                requestPermissions(new String[]{Manifest.permission.CAMERA}, MainActivity.REQUEST_CAMERA);
//            } else {
//
//            }
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.CAMERA)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            mPreviewCam1 = new PreviewCam1(mCameraSurfaceView1);
                            Log.d(TAG, "mPreview set");
                        } else {
                            Toast.makeText(this, "Should have camera permission to run", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "start compass");
        compass.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        compass.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        compass.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "stop compass");
        compass.stop();
    }

}
