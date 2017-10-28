package com.example.kimjungwon.livebusker.Fragment;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;

import com.example.kimjungwon.livebusker.Activity.ARActivity;
import com.example.kimjungwon.livebusker.Network.PathService;
import com.example.kimjungwon.livebusker.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.kimjungwon.livebusker.Config.URL.TMAP_API_URL;

/**
 * Created by kimjungwon on 2017-09-07.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = MapFragment.class.getSimpleName();
    private BottomSheetBehavior mBottomSheetBehavior;
    private MapView mapView;
    private GoogleMap mMap;
    private TableRow find_path_row;
    Marker dst_marker;
    LatLng src,dst;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragement_map, container, false);
        find_path_row = rootview.findViewById(R.id.find_path_row);
        find_path_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "click find path");

                if (src != null && dst != null) {
//                    dst = dst_marker.getPosition();
                    setPath(src, dst);
                    Log.d(TAG, "set Path");
                }
            }
        });

        Button AR_btn = rootview.findViewById(R.id.ar_btn);
        AR_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ARActivity.class);

                intent.putExtra("src_lat",src.latitude);
                intent.putExtra("src_lng",src.longitude);
                intent.putExtra("dst_lat",dst.latitude);
                intent.putExtra("dst_lng",dst.longitude);

                startActivity(intent);
            }
        });
        initMap(rootview);
        return rootview;
    }

    Polyline beforeRoute;
    ArrayList<LatLng> PathLine = new ArrayList<>();

    public void setPath(final LatLng start, final LatLng End) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
//                progressDialog.show();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(TMAP_API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                PathService pathService = retrofit.create(PathService.class);

                Call<ResponseBody> call = pathService.GetRoute(
                        "1",
                        "json",
                        PathService.App_Key,
                        String.valueOf(start.longitude),
                        String.valueOf(start.latitude),
                        String.valueOf(End.longitude),
                        String.valueOf(End.latitude),
                        "출발지",
                        "도착지",
                        "WGS84GEO",//response
                        "WGS84GEO"//request
                );

                try {
                    return call.execute().body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                PathLine.clear();
                if (beforeRoute != null){
                    beforeRoute.remove();
                    Log.d(TAG, "before Route is remove");
                }

                Log.d(TAG, "result: " + s);
                try {
                    JSONObject result = new JSONObject(s);
                    JSONArray features = result.getJSONArray("features");
                    for (int i = 0; i < features.length(); i++) {
                        JSONObject feature = (JSONObject) features.get(i);
                        JSONObject geometry = feature.getJSONObject("geometry");
                        String Type = geometry.getString("type");
                        Log.d(TAG, "type: " + Type);
                        if (Type.equals("Point")) {
//                            JSONArray coordinates = feature.getJSONArray("coordinates");
//                            PathLine.add(new LatLng(coordinates.get(0),coordinates.get(1)));
                            Log.d(TAG, (i + 1) + "] coordinate: " + geometry.get("coordinates") + "\n");
                            JSONArray coordinate = (JSONArray) geometry.get("coordinates");
                            PathLine.add(new LatLng((double) coordinate.get(1), (double) coordinate.get(0)));
                            Log.d(TAG, "coordinate1: " + coordinate.get(0) + " coordinate2: " + coordinate.get(1));

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "PathLine length: " + PathLine.size());
                PolylineOptions pathOption = new PolylineOptions();

                for (int i = 0; i < PathLine.size(); i++) {
                    pathOption.add(PathLine.get(i));
                }
                pathOption.width(5)
                        .color(Color.BLUE)
                        .geodesic(true);
                beforeRoute = mMap.addPolyline(pathOption);

//                progressDialog.dismiss();
            }
        }.execute();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void initMap(View ParentView) {
        Log.d(TAG, "init Map !");
        final Context mcontext = this.getContext();

        CoordinatorLayout MainLayout = (CoordinatorLayout) ParentView.findViewById(R.id.maplayout);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (supportMapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            supportMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map, supportMapFragment).commit();
        }

        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }
//        RelativeLayout relativeLayout = new RelativeLayout(mcontext);
//        tMapView = new TMapView(mcontext);

//        setGps();
//
//        tMapView.setSKPMapApiKey(TMap_Api_key);
//        tMapView.setCompassMode(false);
//        tMapView.setIconVisibility(true);
//        tMapView.setZoomLevel(15);
//        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
//        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
//        tMapView.setTrackingMode(false);
//        tMapView.setSightVisible(false);

        View bottomSheet = ParentView.findViewById(R.id.bottom_sheet);
        initBottomSheet(bottomSheet);

//        tMapView.setOnMarkerClickEvent(new TMapView.OnCalloutMarker2ClickCallback() {
//            @Override
//            public void onCalloutMarker2ClickEvent(String s, TMapMarkerItem2 tMapMarkerItem2) {
//                Toast.makeText(mcontext, "클릭!", Toast.LENGTH_SHORT).show();
//                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//                Log.d(TAG,"클릭");
//            }
//        });
//
//        tMapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
//            @Override
//            public void onCalloutRightButton(TMapMarkerItem tMapMarkerItem) {
//                Toast.makeText(mcontext, "클릭!", Toast.LENGTH_SHORT).show();
//                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//                Log.d(TAG,"클릭");
//            }
//        });
//        tMapData = new TMapData();
//        tMapGpsManager = new TMapGpsManager(mcontext);
//        tMapGpsManager.setMinTime(1000);
//        tMapGpsManager.setMinDistance(5);
//        tMapGpsManager.setProvider(TMapGpsManager.GPS_PROVIDER);
//        tMapGpsManager.OpenGps();

//        relativeLayout.addView(tMapView);
//        MainLayout.addView(relativeLayout);
    }

    private void initBottomSheet(View rootview) {
        mBottomSheetBehavior = BottomSheetBehavior.from(rootview);
        mBottomSheetBehavior.setPeekHeight(0);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                    mBottomSheetBehavior.setPeekHeight(0);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
    }
//
//    public void setGps() {
//        final LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//
//        Log.d(TAG,"set GPS()");
//        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
//                1000, // 통지사이의 최소 시간간격 (miliSecond)
//                1, // 통지사이의 최소 변경거리 (m)
//                mLocationListener);
//    }
//
//    public void setMarkerPoint(double lng,double lnt){
//        TMapPoint point = new TMapPoint(lnt,lng);
//
//        TMapMarkerItem2 MyItem = new TMapMarkerItem2();
//        MyItem.setTMapPoint(point);
////        MyItem.setName("현재 위치");
////        MyItem.setVisible(TMapMarkerItem.VISIBLE);
////        MyItem.setCalloutTitle("현재 위치");
////        MyItem.setCalloutSubTitle("");
////        MyItem.setCanShowCallout(true);
////        MyItem.setAutoCalloutVisible(true);
//
////        Bitmap right = BitmapFactory.decodeResource(getResources(),
////                R.mipmap.ic_right);
////        MyItem.setCalloutRightRect(new Rect(0,0,50,50));
////        MyItem.setCalloutRightButtonImage(right);
//        tMapView.addMarkerItem2("test1",MyItem);
//
//    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도

//            tMapView.setCenterPoint(longitude, latitude);
//            setMarkerPoint(longitude,latitude);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        LatLng SEOUL = new LatLng(37.56, 126.97);
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(SEOUL);
//        markerOptions.title("서울");
//        markerOptions.snippet("한국의 수도");
//        // Add a marker in Sydney and move the camera
//        mMap.addMarker(markerOptions);
//        setMarkerOption(SEOUL, "서울", "한국의 수도");

        LatLng Office1 = new LatLng(37.484125, 126.972603);
        LatLng Office2 = new LatLng(37.483892, 126.972228);
        LatLng Office3 = new LatLng(37.484137, 126.973596);
        LatLng Office5 = new LatLng(37.482961, 126.973926);

        setMarkerOption(Office1, "1사무실", null);
        setMarkerOption(Office2, "2사무실", null);
        setMarkerOption(Office3, "3사무실", null);
        setMarkerOption(Office5, "5사무실", null);


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                dst = new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
                return false;
            }
        });


        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                src = new LatLng(location.getLatitude(), location.getLongitude());
            }
        });

        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(src));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    public void setMarkerOption(LatLng latLng, String title, @Nullable String snippet) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(title);
        if (snippet != null)
            markerOptions.snippet(snippet);
        mMap.addMarker(markerOptions);
    }
}
