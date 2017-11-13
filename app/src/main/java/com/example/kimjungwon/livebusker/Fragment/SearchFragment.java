package com.example.kimjungwon.livebusker.Fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kimjungwon.livebusker.R;

/**
 * Created by kimjungwon on 2017-11-10.
 */

public class SearchFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_search,container,false);
        return RootView;
    }
}
