package com.example.kimjungwon.livebusker.Fragment;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.example.kimjungwon.livebusker.Config.MyApplication;
import com.example.kimjungwon.livebusker.CustomClass.EventLogger;
import com.example.kimjungwon.livebusker.Data.Stream;
import com.example.kimjungwon.livebusker.Network.PHPRequest;
import com.example.kimjungwon.livebusker.R;
import com.github.pedrovgs.DraggableListener;
import com.github.pedrovgs.DraggableView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;

import static com.example.kimjungwon.livebusker.Config.URL.HLS_Addr;
import static com.example.kimjungwon.livebusker.Config.URL.LiveStream_Addr;

/**
 * Created by kimjungwon on 2017-09-06.
 */

public class BuskingListFragment extends Fragment implements VideoRendererEventListener {
    public String TAG = WatchingFragment.class.getSimpleName();

    DraggableView draggableView;
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;

    String path =
            HLS_Addr + "test.m3u8"; //hls
//            "http://13.124.178.218/dash/test_dash.mpd"; //DASH
//    String path = "http://" + Server_IP + "/dog.mp4";

    ImageView thumbnailImageView;
    ImageView posterImageView;
    private DataSource.Factory mediaDataSourceFactory;
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private Handler mainHandler = new Handler();
    private EventLogger eventLogger;

    Context context;

    //리사이클러뷰
    RecyclerView StreamListView;

    public BuskingListFragment(){
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, Bundle savedInstanceState) {

        View Rootview = inflater.inflate(R.layout.draggable_layout,container,false);



        //비디오뷰 설정
        simpleExoPlayerView = (SimpleExoPlayerView) Rootview.findViewById(R.id.player_view);
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
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);
        simpleExoPlayerView = new SimpleExoPlayerView(context);
        simpleExoPlayerView = (SimpleExoPlayerView) Rootview.findViewById(R.id.player_view);

//Set media controller
        simpleExoPlayerView.setUseController(true);
        simpleExoPlayerView.requestFocus();

// Bind the player to the view.
        simpleExoPlayerView.setPlayer(player);
// Set Path
        Uri mp4videoUri = Uri.parse(path);
//Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
//Produces DataSource instances through which media data is loaded.
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getActivity(), Util.getUserAgent(context, "exoplayer2example"), bandwidthMeterA);
//Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();


        MediaSource videoSource = new HlsMediaSource(mp4videoUri,dataSourceFactory,1,null,null);
        final LoopingMediaSource loopingMediaSource =
//                new LoopingMediaSource(buildMediaSource(mp4videoUri,"extensions"));
                new LoopingMediaSource(videoSource);
        // Prepare the player with the source.
        player.prepare(loopingMediaSource);
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
                player.prepare(loopingMediaSource);
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


        draggableView = (DraggableView) Rootview.findViewById(R.id.draggable_view);
        thumbnailImageView = (ImageView) Rootview.findViewById(R.id.iv_thumbnail);
        posterImageView = (ImageView) Rootview.findViewById(R.id.iv_poster);

        //로딩 프로그레스 다이얼로그
        //다이얼로그
        final MaterialDialog MD = new MaterialDialog.Builder(context)
                .content("잠시만 기다려주세요 . . . ")
                .progress(true, 0)
                .build();
        //영상 로드 다이얼로그
//        simpleExoPlayerView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
//            @Override
//            public boolean onInfo(MediaPlayer mp, int what, int extra) {
//                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
//                    Log.d(TAG, "INFO: Buffering start");
////                    player.stop();
//                    MD.show();
//                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
//                    Log.d(TAG, "INFO: Buffering END");
//                    MD.dismiss();
////                    player.start();
//                }
//                return false;
//            }
//        });

        thumbnailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "thumbnail!", Toast.LENGTH_SHORT).show();
            }
        });

        posterImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                draggableView.maximize();
            }
        });

        initializePoster();
        hookDraggableViewListener();
        return Rootview;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void hookDraggableViewListener() {
        draggableView.setDraggableListener(new DraggableListener() {
            @Override
            public void onMaximized() {
                startVideo();
            }

            @Override
            public void onMinimized() {

            }

            @Override
            public void onClosedToLeft() {
//                draggableView.closeToLeft();
                player.release();
//                pauseVideo();
            }

            @Override
            public void onClosedToRight() {
//                draggableView.closeToRight();
                player.release();
//                pauseVideo();
            }
        });
    }

    private void pauseVideo() {
        if (player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
        }
    }

    private void startVideo() {
        if (!player.getPlayWhenReady()) {
            player.setPlayWhenReady(true);
        }
    }

    private void initializePoster() {
        Glide.with(this).load(R.drawable.busker).into(posterImageView);
        Glide.with(this).load(R.drawable.ic_home_black_24dp).into(thumbnailImageView);
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

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return ((MyApplication) getActivity().getApplication())
                .buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }


    @Override
    public void onVideoEnabled(DecoderCounters counters) {

    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

    }

    @Override
    public void onVideoInputFormatChanged(Format format) {

    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {

    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {

    }
}
