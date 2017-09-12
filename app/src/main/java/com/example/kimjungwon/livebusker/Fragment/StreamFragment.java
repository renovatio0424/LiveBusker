package com.example.kimjungwon.livebusker.Fragment;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kimjungwon.livebusker.Adapter.Stream_Adapter;
import com.example.kimjungwon.livebusker.Data.Stream;
import com.example.kimjungwon.livebusker.Network.PHPRequest;
import com.example.kimjungwon.livebusker.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;

import static com.example.kimjungwon.livebusker.Config.URL.LiveStream_Addr;

/**
 * Created by kimjungwon on 2017-09-07.
 */

public class StreamFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = StreamFragment.class.getSimpleName();
    RecyclerView StreamListView;
    ArrayList<Stream> StreamList ;
    Stream_Adapter sa;
    View Rootview;

    SwipeRefreshLayout swipeRefreshLayout;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bun = msg.getData();
            String jsonString = bun.getString("StreamJson");
            JSONArray ja = null;
            try {
                ja = new JSONArray(jsonString);
                for(int i = 0 ; i < ja.length() ; i ++){
                    JSONObject jo = ja.getJSONObject(i);
                    int id = jo.getInt("id");
                    String title = jo.getString("title");
                    String stream_key = jo.getString("stream_key");
                    Stream st = new Stream(id,title,stream_key);
//                        Log.d(TAG,"id: " + id + "\ntitle: " + title);
                    StreamList.add(st);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            sa = new Stream_Adapter(StreamList,getContext());
            Log.d(TAG,"create stream adapter");
            StreamListView.setAdapter(sa);
            sa.notifyDataSetChanged();
            Log.d(TAG,"set stream adapter");
            Log.d(TAG,"setStreamList size: " + StreamList.size());
        }
    };

    public void setStreamList(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PHPRequest request = new PHPRequest(LiveStream_Addr);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("type",request.Select_stream);
                    String after = request.POSTJSON(jsonObject.toString());
//                    Log.d(TAG,"stream list: " + after);
                    Bundle bun = new Bundle();
                    bun.putString("StreamJson",after);
                    Message msg = handler.obtainMessage();
                    msg.setData(bun);
                    handler.sendMessage(msg);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG,"oncreateview");
        Rootview = inflater.inflate(R.layout.fragment_stream,container,false);

        return Rootview;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"oncreate");

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onviewcreated");
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG,"onactivitycreated");

    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG,"onresume");
        //리사이클러뷰
        StreamListView = (RecyclerView) Rootview.findViewById(R.id.stream_lv);
        StreamList = new ArrayList<>();
        setStreamList();

        Log.d(TAG,"list size : " + StreamList.size());
        for(int i = 0 ; i < StreamList.size() ; i++){
            Log.d(TAG,"stream " + i + ")id/ " + StreamList.get(i).getId() + " title/ " + StreamList.get(i).getTitle());
        }

        RecyclerView.LayoutManager lm = new LinearLayoutManager(getContext());
        StreamListView.setLayoutManager(lm);

        swipeRefreshLayout = (SwipeRefreshLayout) Rootview.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

    }

    @Override
    public void onRefresh() {
        setStreamList();
        swipeRefreshLayout.setRefreshing(false);
    }
}
