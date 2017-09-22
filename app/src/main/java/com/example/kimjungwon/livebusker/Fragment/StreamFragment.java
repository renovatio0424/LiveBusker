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
import com.example.kimjungwon.livebusker.Netty.ChatInitializer;
import com.example.kimjungwon.livebusker.Network.PHPRequest;
import com.example.kimjungwon.livebusker.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import static com.example.kimjungwon.livebusker.Config.URL.Chat_Port;
import static com.example.kimjungwon.livebusker.Config.URL.LiveStream_Addr;
import static com.example.kimjungwon.livebusker.Config.URL.Server_IP;

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

    String string_postgre,string_chatserver;

    NioEventLoopGroup group;
    Channel channel;
    ChannelFuture channelFuture;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x00:
                    JSONObject MessageOb = new JSONObject();
                    try {
                        MessageOb.put("Type","4");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String m = MessageOb.toString();
                    ByteBuf msgbuf = Unpooled.buffer();
                    msgbuf.writeBytes(m.getBytes());
                    channel.writeAndFlush(msgbuf.retain());
                    break;
//                채팅방 목록에 추가
                case 0x01:
//                    postgres
//                    try {
//                        JSONArray ja = new JSONArray(string_postgre);
//                        for(int i = 0 ; i < ja.length() ; i ++){
//                            JSONObject jo = ja.getJSONObject(i);
//                            int id = jo.getInt("id");
//                            String title = jo.getString("title");
//                            String stream_key = jo.getString("stream_key");
//                            Stream st = new Stream(id,title,stream_key);
////                        Log.d(TAG,"id: " + id + "\ntitle: " + title);
//                            StreamList.add(st);
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    chatserver
                    try {
                        JSONArray RoomArray = new JSONArray(msg.obj + "");
                        Log.d(TAG,"room list: " + msg.obj + "");
                        if(RoomArray.length() != 0){
                            for(int i = 0 ; i < RoomArray.length() ; i ++){
                                JSONObject Room = RoomArray.getJSONObject(i);
                                String title = Room.getString("Roomname");
                                String streamkey = Room.getString("Streamkey");
                                Log.d(TAG,"room name: " + title);
                                Log.d(TAG,"room streamkey: " + streamkey);
                                int usernum = Room.getInt("UserNum");

                                Stream stream = new Stream(0,title,streamkey);
                                StreamList.add(stream);
                            }
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
                    break;
//                채팅방 목록 요청
                case 0x04:

                    break;
            }
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
                    string_postgre = request.POSTJSON(jsonObject.toString());

//                    Log.d(TAG,"stream list: " + after);
//                    Bundle bun = new Bundle();
//                    bun.putString("StreamJson",after);
//                    Message msg = handler.obtainMessage();
//                    msg.setData(bun);
//                    handler.sendMessage(msg);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void connect(final Handler handler) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.d("Main","connect start");
                    group = new NioEventLoopGroup();

                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(group);
                    bootstrap.channel(NioSocketChannel.class);
                    bootstrap.handler(new ChatInitializer(handler));
                    bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                    bootstrap.option(ChannelOption.TCP_NODELAY, true);
                    Log.d("Main","bootstrap set option");

//                    channelFuture = bootstrap.connect(new InetSocketAddress(host, port));
                    channelFuture = bootstrap.connect(Server_IP,Chat_Port);
                    Log.d("Main","bootstrap connect");
                    channel = channelFuture.sync().channel();
                    channelFuture.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            //start 메시지 보내기
                            handler.obtainMessage(0x00).sendToTarget();
                        }
                    });

                    Log.d("Main","add Listner");
                    channel.closeFuture().sync();
                    Log.d("Main","channel.closeFuture().sync()");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
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
        connect(handler);


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
    public void onDestroy() {
        super.onDestroy();
//        채팅 서버 접속 종료
        group.shutdownGracefully();
    }

    @Override
    public void onRefresh() {
        handler.obtainMessage(0x00);
        swipeRefreshLayout.setRefreshing(false);
    }
}
