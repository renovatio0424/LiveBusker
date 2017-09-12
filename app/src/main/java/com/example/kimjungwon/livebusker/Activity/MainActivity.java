package com.example.kimjungwon.livebusker.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
//import android.support.v7.app.AlertController;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kimjungwon.livebusker.Fragment.FavoriteFragment;
import com.example.kimjungwon.livebusker.Fragment.HotFragment;
import com.example.kimjungwon.livebusker.Fragment.StreamFragment;
import com.example.kimjungwon.livebusker.R;

public class MainActivity extends AppCompatActivity {
    //권한 변수
    private static final int REQUEST_STREAM = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static String[] PERMISSIONS_STREAM = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    boolean authorized = false;

    private TextView mTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //<- 버튼 추가
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setOnNavigationItemReselectedListener(onNavigationItemReselectedListener);
        verifyPermissions();
        //첫화면 셀렉
        navigation.setSelectedItemId(R.id.navigation_home);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Log.d(TAG,"Select Listener");
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    ft.replace(R.id.content, new StreamFragment()).commit();
//                    mTextMessage.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
////                            Intent intent = new Intent(MainActivity.this, WatchingFragment.class);
////                            startActivity(intent);
//                            BuskingListFragment bf = new BuskingListFragment();
//                            bf.setContext(getApplicationContext());
//                        }
//                    });
                    return true;
                case R.id.navigation_dashboard:
                    ft.replace(R.id.content, new HotFragment()).commit();
                    return true;
                case R.id.navigation_notifications:
                    ft.replace(R.id.content, new FavoriteFragment()).commit();
                default:
                    return false;
            }
        }
    };

    BottomNavigationView.OnNavigationItemReselectedListener onNavigationItemReselectedListener
            = new BottomNavigationView.OnNavigationItemReselectedListener() {
        @Override
        public void onNavigationItemReselected(@NonNull MenuItem item) {
            Log.d(TAG,"Reselect Listener");
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    ft.replace(R.id.content, new StreamFragment()).commit();
//                    mTextMessage.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
////                            Intent intent = new Intent(MainActivity.this, WatchingFragment.class);
////                            startActivity(intent);
//                            BuskingListFragment bf = new BuskingListFragment();
//                            bf.setContext(getApplicationContext());
//                        }
//                    });
                    break;
                case R.id.navigation_dashboard:
                    ft.replace(R.id.content, new HotFragment()).commit();
                    break;
                case R.id.navigation_notifications:
                    ft.replace(R.id.content, new FavoriteFragment()).commit();
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_stream:
                Toast.makeText(this, "stream", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, StreamingActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void verifyPermissions() {
        int CAMERA_permission = ActivityCompat.checkSelfPermission(com.example.kimjungwon.livebusker.Activity.MainActivity.this, Manifest.permission.CAMERA);
        int RECORD_AUDIO_permission = ActivityCompat.checkSelfPermission(com.example.kimjungwon.livebusker.Activity.MainActivity.this, Manifest.permission.RECORD_AUDIO);
        int WRITE_EXTERNAL_STORAGE_permission = ActivityCompat.checkSelfPermission(com.example.kimjungwon.livebusker.Activity.MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (CAMERA_permission != PackageManager.PERMISSION_GRANTED ||
                RECORD_AUDIO_permission != PackageManager.PERMISSION_GRANTED ||
                WRITE_EXTERNAL_STORAGE_permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    com.example.kimjungwon.livebusker.Activity.MainActivity.this,
                    PERMISSIONS_STREAM,
                    REQUEST_STREAM
            );
            authorized = false;
        } else {
            authorized = true;
        }
    }
}
