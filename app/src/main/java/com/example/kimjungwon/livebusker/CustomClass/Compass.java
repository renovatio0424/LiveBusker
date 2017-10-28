package com.example.kimjungwon.livebusker.CustomClass;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.kimjungwon.livebusker.R;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by kimjungwon on 2017-10-27.
 */

public class Compass implements SensorEventListener {
    private static final String TAG = "Compass";

    private SensorManager sensorManager;
    private Sensor gsensor;
    private Sensor msensor;
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private float azimuthY = 0f;
    private float currectAzimuth = 0;
    private TextView degree_view;
    private float horizontal_fov = 0.8f;
    private float vertical_fov = 1.1f;
    private Context mcontext;
    private RelativeLayout mCameraLayout;
    private ImageView PlaceMarker;
    private ImageView CompassPlane;
    private float CameraWidth;

    public LatLng src,dst;

    // compass arrow to rotate
    public ImageView arrowView = null;

    public Compass(Context context) {
        mcontext = context;
        sensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        degree_view = ((Activity) context).getWindow().getDecorView().findViewById(R.id.degree_tv);
        mCameraLayout = ((Activity) context).getWindow().getDecorView().findViewById(R.id.CameraLayout);
        CompassPlane = ((Activity) context).getWindow().getDecorView().findViewById(R.id.main_image_dial);
//        Log.d(TAG, "width: " + mCameraLayout.getWidth() + "\nheight: " + mCameraLayout.getHeight());
        CameraWidth = 720;
        setImage();
        Log.d(TAG, "horizontal fov: " + horizontal_fov);
    }

    public void start() {
        sensorManager.registerListener(this, gsensor,
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, msensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    private void adjustArrow() {
        if (arrowView == null) {
            Log.i(TAG, "arrow view is not set");
            return;
        }
        //currentAzimuth: 측정전 각도 / azimuth: 측정한 각도
        Log.i(TAG, "will set rotation from " + currectAzimuth + " to "
                + azimuth);

        Animation an = new RotateAnimation(-currectAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currectAzimuth = azimuth;
        degree_view.setText(String.valueOf("degreeX: " + (int) currectAzimuth) + "\ndegreeY: " + (int)azimuthY);

        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);

        arrowView.startAnimation(an);
    }

    private void setImage() {
        PlaceMarker = new ImageView(mcontext);
        PlaceMarker.setId(0);
        PlaceMarker.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        PlaceMarker.setBackgroundResource(R.drawable.redmarker);
        movefrom = -PlaceMarker.getHeight();
    }

    float movefrom = 0;
    long pasttime = 0;
    float movefromY = 0;

    public float bearingP1toP2(LatLng src, LatLng dst)
    {

        double P1_latitude = src.latitude;
        double P1_longitude = src.longitude;
        double P2_latitude = dst.latitude;
        double P2_longitude = dst.longitude;

        // 현재 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에 라디안 각도로 변환한다.
        double Cur_Lat_radian = P1_latitude * (3.141592 / 180);
        double Cur_Lon_radian = P1_longitude * (3.141592 / 180);


        // 목표 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에 라디안 각도로 변환한다.
        double Dest_Lat_radian = P2_latitude * (3.141592 / 180);
        double Dest_Lon_radian = P2_longitude * (3.141592 / 180);

        // radian distance
        double radian_distance = 0;
        radian_distance = Math.acos(Math.sin(Cur_Lat_radian) * Math.sin(Dest_Lat_radian) + Math.cos(Cur_Lat_radian) * Math.cos(Dest_Lat_radian) * Math.cos(Cur_Lon_radian - Dest_Lon_radian));

        // 목적지 이동 방향을 구한다.(현재 좌표에서 다음 좌표로 이동하기 위해서는 방향을 설정해야 한다. 라디안값이다.
        double radian_bearing = Math.acos((Math.sin(Dest_Lat_radian) - Math.sin(Cur_Lat_radian) * Math.cos(radian_distance)) / (Math.cos(Cur_Lat_radian) * Math.sin(radian_distance)));		// acos의 인수로 주어지는 x는 360분법의 각도가 아닌 radian(호도)값이다.

        double true_bearing = 0;
        if (Math.sin(Dest_Lon_radian - Cur_Lon_radian) < 0)
        {
            true_bearing = radian_bearing * (180 / 3.141592);
            true_bearing = 360 - true_bearing;
        }
        else
        {
            true_bearing = radian_bearing * (180 / 3.141592);
        }

        return (float) true_bearing;
    }


    private void adjustImage() {
        long currenttime = System.currentTimeMillis();


        float direction_image = 0;
//        270f
//        float direction_image = bearingP1toP2(Office2, Office3);
        if(src != null && dst != null){
            Log.d(TAG, "src,dst isn't null");
            direction_image = bearingP1toP2(src, dst);
        }else{
            Log.d(TAG, "src,dst is null");
            LatLng Office2 = new LatLng(37.483892, 126.972228);
            LatLng Office3 = new LatLng(37.484137, 126.973596);
            direction_image = bearingP1toP2(Office2, Office3);
        }

        direction_image = ((direction_image + 360) % 360);
        CompassPlane.setRotation(direction_image);
        Log.d(TAG, "direction: " + String.valueOf(direction_image));
        float direction_imageY = 0f;

        double fov_h = Math.toDegrees(horizontal_fov);
        float startX = azimuth - (float) fov_h / 2;
        startX = (float) ((startX + 360) % 360);
        float end = azimuth + (float) fov_h / 2;
        end = (float) ((end + 360) % 360);

        Log.d(TAG,"azimuth: " + azimuth +
                "\nstart:" + startX +
                "\nend:" + end);
        //이미지 숨어있는 각
        float deltaX = (float) (fov_h * PlaceMarker.getWidth() / 720);


        double fov_v = Math.toDegrees(vertical_fov);
        float startY = azimuthY - (float) fov_v / 2;
        float endY = azimuthY + (float) fov_v / 2;

        Log.d(TAG,"azimuthY: " + azimuthY +
                "\nstartY:" + startY +
                "\nendY:" + endY);

        float deltaY = (float) (fov_v * PlaceMarker.getHeight() / 1118);

        View view = mCameraLayout.findViewById(0);

        Log.d(TAG,"width: " + mCameraLayout.getWidth() +
                " height: " + mCameraLayout.getHeight());

        if (
                startX - deltaX < direction_image && direction_image < end
                        && startY - deltaY < direction_imageY && direction_imageY < endY
                ) {
            if (view == null){
                Log.d(TAG, "adjust Image!!");
                mCameraLayout.addView(PlaceMarker);
            }else{
                float a = direction_image - startX;
                double b = Math.toDegrees(horizontal_fov);
                float x = (float)((720 * a)/b);

                float aY = direction_imageY - startY;
                double bY = Math.toDegrees(vertical_fov);
                float y = (float) ((1118 * aY)/bY);

                Log.d(TAG,"aY: " + aY + "\nbY: " + bY + "\ny: " + y);

                Log.d(TAG,
//                        "X: " + x
                        "\nY: " + y
                );
                Animation move = new TranslateAnimation(movefrom,x,movefromY,y);

//                Animation move = new TranslateAnimation(360,360,movefromY,y);
                move.setDuration(50);
                move.setRepeatCount(0);
                move.setFillAfter(true);

                PlaceMarker.startAnimation(move);
                movefrom = x;
                movefromY = y;
            }
        } else {
//            if (view != null) {
//                Log.d(TAG, "delete Image!!");
//                mCameraLayout.removeView(PlaceMarker);
//            }else{
//                Log.d(TAG, "view is null");
//            }
        }

        Log.d(TAG, "refresh time : " + (currenttime - pasttime) + "ms" +
                "\ncurrent time : " + currenttime +
                "\npast time : " + pasttime);
        pasttime = currenttime;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //alpha????
        final float alpha = 0.97f;

        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                mGravity[0] = alpha * mGravity[0] + (1 - alpha)
                        * event.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha)
                        * event.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha)
                        * event.values[2];

                // mGravity = event.values;

                // Log.e(TAG, Float.toString(mGravity[0]));
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // mGeomagnetic = event.values;

                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha)
                        * event.values[0];
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha)
                        * event.values[1];
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha)
                        * event.values[2];
                // Log.e(TAG, Float.toString(event.values[0]));

            }

            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                    mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                float remapMatrix[] = new float[9];
                SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapMatrix);
                SensorManager.getOrientation(remapMatrix, orientation);
                //orientation value[0] : z 축에 대한 회전각 , value[1] : x 축에 대한 회전각 , value[2] : y 축에 대한 회전각
                // Log.d(TAG, "azimuth (rad): " + azimuth);

//                azimuth = (float) Math.toDegrees(orientation[0]); // orientation
                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;

                azimuthY = (float) Math.toDegrees(orientation[1]);

                // Log.d(TAG, "azimuth (deg): " + azimuth);
                adjustArrow();
                adjustImage();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
