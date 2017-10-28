package com.example.kimjungwon.livebusker.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.kimjungwon.livebusker.R;

/**
 * Created by kimjungwon on 2017-09-07.
 */

public class FavoriteFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = FavoriteFragment.class.getSimpleName();
    private BottomSheetBehavior mBottomSheetBehavior;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_favorite,container,false);
        InitBottomSheet(RootView);
        return RootView;
    }


    PlaceSheetFragment placeSheetFragment;

    private void InitBottomSheet(View rootView) {
        View bottomSheet = rootView.findViewById( R.id.bottom_sheet );
        Button button1 = (Button) rootView.findViewById( R.id.button_1 );
        Button button2 = (Button) rootView.findViewById( R.id.button_2 );
        Button button3 = (Button) rootView.findViewById( R.id.button_3 );

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);

        placeSheetFragment = new PlaceSheetFragment();

        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setHideable(false);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    Log.d(TAG, "Bottom Sheet State: Collapsed");
                    mBottomSheetBehavior.setPeekHeight(0);
                }else if(newState == BottomSheetBehavior.STATE_DRAGGING){
                    Log.d(TAG, "Bottom Sheet State: Dragging");
                }else if(newState == BottomSheetBehavior.STATE_EXPANDED){
                    Log.d(TAG, "Bottom Sheet State: Expanded");
                }else if(newState == BottomSheetBehavior.STATE_HIDDEN){
                    Log.d(TAG, "Bottom Sheet State: Hidden");
                }else if(newState == BottomSheetBehavior.STATE_SETTLING){
                    Log.d(TAG, "Bottom Sheet State: Settling");
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button_1: {
//                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                mBottomSheetBehavior.setPeekHeight(0);
//                placeSheetFragment.show(getFragmentManager(), placeSheetFragment.getTag());
                break;
            }
            case R.id.button_2:
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            case R.id.button_3:
                break;
        }
    }
}
