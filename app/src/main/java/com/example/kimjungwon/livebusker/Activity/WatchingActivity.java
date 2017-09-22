package com.example.kimjungwon.livebusker.Activity;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.kimjungwon.livebusker.Config.MyApplication;
import com.example.kimjungwon.livebusker.CustomClass.EventLogger;
import com.example.kimjungwon.livebusker.Netty.ChatInitializer;
import com.example.kimjungwon.livebusker.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.UUID;

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
import static com.example.kimjungwon.livebusker.Config.URL.DASH_Addr;
import static com.example.kimjungwon.livebusker.Config.URL.HLS_Addr;
import static com.example.kimjungwon.livebusker.Config.URL.Server_IP;
import static com.example.kimjungwon.livebusker.R.id.et_scroll;

/**
 * Created by kimjungwon on 2017-09-06.
 */

public class WatchingActivity extends AppCompatActivity implements VideoRendererEventListener {


    private static final String TAG = "WatchingActivity";
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    String Stream_key;
    String path ;

    private DataSource.Factory mediaDataSourceFactory;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private Handler mainHandler = new Handler();
    private EventLogger eventLogger;
    //채팅
    TextView view_chat;
    EditText et_msg;
    Button send_btn;

    NioEventLoopGroup group;
    Channel channel;
    ChannelFuture channelFuture;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_watching);

//        액션바 숨기기
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Stream_key = getIntent().getStringExtra("stream_key");
        path
//                = HLS_Addr + Stream_key + ".m3u8";
                = DASH_Addr+ Stream_key +"_dash.mpd";

        Log.d(TAG,"path: " + path);

        name = getUserkey();

// 1. Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
//        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        TrackSelection.Factory adaptiveTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
        eventLogger = new EventLogger(trackSelector);

// 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

// 3. Create the player
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        simpleExoPlayerView = new SimpleExoPlayerView(this);
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);

// Bind the player to the view.
        simpleExoPlayerView.setPlayer(player);



// I. ADJUST HERE:
//CHOOSE CONTENT: LiveStream / SdCard

//LIVE STREAM SOURCE: * Livestream links may be out of date so find any m3u8 files online and replace:

//VIDEO FROM SD CARD: (2 steps. set up file and path, then change videoSource to get the file)
//        String urimp4 = "path/FileName.mp4"; //upload file to device and add path/name.mp4

//        Uri mp4VideoUri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()+urimp4);
        Uri mp4VideoUri = Uri.parse(path);
//        Uri mp4VideoUri = Uri.parse("http://13.124.178.218/hls/test.m3u8");

//Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
//Produces DataSource instances through which media data is loaded.
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), bandwidthMeterA);
//Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();


// II. ADJUST HERE:

//This is the MediaSource representing the media to be played:
//FOR SD CARD SOURCE:
//        MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);

//FOR LIVESTREAM LINK:
//        MediaSource videoSource = new HlsMediaSource(mp4VideoUri, dataSourceFactory, 1, null, null);

//        mediaDataSourceFactory = buildDataSourceFactory(true);
        DashMediaSource dashMediaSource = new DashMediaSource(mp4VideoUri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
//        MediaSource videoSource = new DashMediaSource(mp4VideoUri,
//                buildDataSourceFactory(false),
//                new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
//                null,
//                null);
//        MediaSource videoSource = new HlsMediaSource(mp4VideoUri,dataSourceFactory,1,null,null);
        final LoopingMediaSource loopingSource =
                new LoopingMediaSource(dashMediaSource);
//                new LoopingMediaSource(buildMediaSource(mp4VideoUri,"extensions"));
// Prepare the player with the source.
        player.prepare(loopingSource);
        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                Log.v(TAG, "Listener-onTimelineChanged...");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.v(TAG, "Listener-onTracksChanged...");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.v(TAG, "Listener-onLoadingChanged...isLoading:" + isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.v(TAG, "Listener-onPlayerStateChanged..." + playbackState);
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                Log.v(TAG, "Listener-onRepeatModeChanged...");
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.v(TAG, "Listener-onPlayerError...");
                player.stop();
                player.prepare(loopingSource);
                player.setPlayWhenReady(true);
            }

            @Override
            public void onPositionDiscontinuity() {
                Log.v(TAG, "Listener-onPositionDiscontinuity...");
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Log.v(TAG, "Listener-onPlaybackParametersChanged...");
            }
        });

        player.setPlayWhenReady(true); //run file/link when ready to play.
        player.setVideoDebugListener(this); //for listening to resolution change and  outputing the resolution

//        채팅 서버 접속
        view_chat = (TextView) findViewById(R.id.View_chat);
        et_msg = (EditText) findViewById(R.id.et_msg);
        send_btn = (Button) findViewById(R.id.btn_send);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = String.valueOf(et_msg.getText()) + "\r\n";
                if (msg.length() != 0) {
                    handler.obtainMessage(0x03).sendToTarget();
                }
            }
        });

        connect(handler);
    }//End of onCreate

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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String m = msg.obj + "";
            switch (msg.what) {
                case 0x00:
                    //채팅 서버 접속 -> 채팅방 생성
                    JSONObject MessageObject = new JSONObject();

                    try {
                        MessageObject.put("Streamkey",Stream_key);
                        MessageObject.put("Userkey", name);
                        MessageObject.put("Username","User[" + name +"]");
                        //방 참가
                        MessageObject.put("Type","1");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String hello = MessageObject.toString();

                    ByteBuf MessageBuffer = Unpooled.buffer();
                    MessageBuffer.writeBytes(hello.getBytes());
                    channel.writeAndFlush(MessageBuffer);
//                    ByteBuf buf = Unpooled.buffer(hello.length());
//                    buf.readBytes(hello.getBytes());
//                    channel.writeAndFlush(buf);
//                    channel.read();

//                    EchoMessage em = new EchoMessage();
//                    byte[] b = hello.getBytes();
//                    em.setBytes(b);
//                    em.setSumCountPackage(b.length);
//                    em.setCountPackage(1);
//                    em.setSend_time(System.currentTimeMillis());
//
//                    channel.writeAndFlush(em);
                    break;
                case 0x01:
                    //receive
                    Log.d(TAG,"receive msg: " + m);
                    if(m.equals("스트리머가 방송을 종료 했습니다")){
                        Log.d(TAG,"스트리머 방송 종료!");
                        new MaterialDialog.Builder(WatchingActivity.this)
                                .title("스트리머가 방송을 종료 했습니다")
                                .cancelable(false)
                                .positiveText("확인")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                })
                                .show();
                    }
                    view_chat.setText(view_chat.getText() + m + "\r\n");
                    break;
                case 0x02:
                    //send complete
                    et_msg.setText("");
                    break;
                case 0x03:
                    //send txt
//                    String name = pref.getString("User","no");
                    String et_m = et_msg.getText().toString();
                    if (et_m.length() == 0)
                        return;
                    String mmm = String.valueOf("[User " + name +"] " + et_m + "");

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("Type","2");
                        jsonObject.put("Streamkey",Stream_key);
                        jsonObject.put("Message",mmm);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    ByteBuf msgBuffer = Unpooled.buffer();
                    msgBuffer.writeBytes(jsonObject.toString().getBytes());
//                    ByteBuf buffer = Unpooled.buffer(mmm.length());
//                    buffer.readBytes(mmm.getBytes());

//                    EchoMessage emm = new EchoMessage();
//                    emm.setSend_time(System.currentTimeMillis());
//
//                    byte[] bb = mmm.getBytes();
//                    emm.setBytes(bb);
//                    emm.setSumCountPackage(bb.length);
//                    emm.setCountPackage(1);
//                    emm.setSend_time(System.currentTimeMillis());

                    channel.writeAndFlush(msgBuffer.retain()).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            handler.obtainMessage(0x02).sendToTarget();
                        }
                    });
                    Log.d("Main","send Msg");
                    break;
                case 0x04:
                    ///채팅 방 퇴장
                    JSONObject exitObject = new JSONObject();
                    try {
                        exitObject.put("Type","3");
                        exitObject.put("Streamkey",Stream_key);
                        exitObject.put("Userkey",name);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    ByteBuf exitBuffer = Unpooled.buffer();
                    exitBuffer.writeBytes(exitObject.toString().getBytes());
//                    ByteBuf buffer = Unpooled.buffer(mmm.length());
//                    buffer.readBytes(mmm.getBytes());

//                    EchoMessage emm = new EchoMessage();
//                    emm.setSend_time(System.currentTimeMillis());
//
//                    byte[] bb = mmm.getBytes();
//                    emm.setBytes(bb);
//                    emm.setSumCountPackage(bb.length);
//                    emm.setCountPackage(1);
//                    emm.setSend_time(System.currentTimeMillis());
                    channel.writeAndFlush(exitBuffer.retain());
                    group.shutdownGracefully();
                    Log.d("Main","send Msg");

                default:
                    Toast.makeText(WatchingActivity.this, "UNKNOWN MSG: " + m, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public String getUserkey(){
        // 현재시간을 msec 으로 구한다.
        long now = System.currentTimeMillis();
        // 현재시간을 date 변수에 저장한다.
        Date date = new Date(now);
        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
        SimpleDateFormat sdfNow = new SimpleDateFormat("ss");
        return sdfNow.format(date);
    }


    @Override
    public void onVideoEnabled(DecoderCounters counters) {

    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

    }

    @Override
    public void onVideoInputFormatChanged(com.google.android.exoplayer2.Format format) {

    }


    @Override
    public void onDroppedFrames(int count, long elapsedMs) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        Log.v(TAG, "onVideoSizeChanged [" + " width: " + width + " height: " + height + "]");
//        resolutionTextView.setText("RES:(WxH):" + width + "X" + height + "\n           "
//                + height + "p" + "\npath: " + path);
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {

    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {

    }


//-------------------------------------------------------ANDROID LIFECYCLE---------------------------------------------------------------------------------------------

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop()...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()...");
        player.release();
        handler.obtainMessage(0x04).sendToTarget();
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return ((MyApplication) getApplication())
                .buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    //프로토콜에 따른 MEDIASOURCE 생성
    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }


}