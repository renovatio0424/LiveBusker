package com.example.kimjungwon.livebusker.Activity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.kimjungwon.livebusker.Netty.ChatInitializer;
import com.example.kimjungwon.livebusker.Network.PHPRequest;
import com.example.kimjungwon.livebusker.R;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import me.lake.librestreaming.client.RESClient;
import me.lake.librestreaming.core.listener.RESConnectionListener;
import me.lake.librestreaming.core.listener.RESScreenShotListener;
import me.lake.librestreaming.core.listener.RESVideoChangeListener;
import me.lake.librestreaming.filter.hardvideofilter.BaseHardVideoFilter;
import me.lake.librestreaming.filter.hardvideofilter.HardVideoGroupFilter;
import me.lake.librestreaming.model.RESConfig;
import me.lake.librestreaming.model.Size;
import me.lake.librestreaming.sample.audiofilter.SetVolumeAudioFilter;
import me.lake.librestreaming.sample.hardfilter.IconHardFilter;
import me.lake.librestreaming.sample.ui.AspectTextureView;

import static com.example.kimjungwon.livebusker.Config.URL.Chat_Port;
import static com.example.kimjungwon.livebusker.Config.URL.LiveStream_Addr;
import static com.example.kimjungwon.livebusker.Config.URL.RTMP_Addr;
import static com.example.kimjungwon.livebusker.Config.URL.Server_IP;

/**
 * Created by kimjungwon on 2017-09-06.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class StreamingActivity extends AppCompatActivity implements RESConnectionListener
        , TextureView.SurfaceTextureListener
        , RESVideoChangeListener, View.OnClickListener, SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "StreamingActivity";
    protected RESClient resClient;
    protected AspectTextureView camera_preview;

    //    protected SeekBar sb_zoom;
    protected TextView tv_speed;
    protected TextView tv_rtmp;
    protected Handler mainHander;
    protected boolean started;

    String stream_key;
    protected String rtmpaddr;
    protected int filtermode = RESConfig.FilterMode.HARD;
    RESConfig resConfig;

    Button btn_start;
    ImageView btn_switch, btn_canel, stream_thumnail;
    TextView Room_name;

    EditText Stream_et;

    //필터 어뎁터
//    protected FilterAdapter filterAdapter;

    //준비 레이아웃
    FrameLayout readylayout;

    //채팅 변수
    NioEventLoopGroup group;
    Channel channel;
    ChannelFuture channelFuture;
    String name;

    TextView et_scroll;
    EditText et_msg;
    Button btn_send;
    RelativeLayout chat_layout, broadcast_layout;

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
//        System.loadLibrary("detection_based_tracker");
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    CascadeClassifier faceDetector;
    ImageReader previewReader;
    Bitmap bp2;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
//                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        faceDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (faceDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            faceDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());


                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    //메시지 핸들러
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String m = msg.obj + "";
            switch (msg.what) {
                case 0x00:
                    //채팅 서버 접속 -> 채팅방 생성
                    JSONObject MessageObject = new JSONObject();

                    try {
                        MessageObject.put("Streamkey", stream_key);
                        MessageObject.put("Roomname", Stream_et.getText().toString());

                        MessageObject.put("Userkey", name);
                        MessageObject.put("Username", "[User " + name + "]");
                        //방생성
                        MessageObject.put("Type", "0");
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
                    et_scroll.setText(et_scroll.getText() + m + "\r\n");
                    break;
                case 0x02:
                    //send complete
                    et_msg.setText("");
                    break;
                case 0x03:
                    //send txt
                    String et_m = et_msg.getText().toString();
                    if (et_m.length() == 0)
                        return;
                    String mmm = String.valueOf("[User " + name + "] " + et_m + "");

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("Type", "2");
                        jsonObject.put("Streamkey", stream_key);
                        jsonObject.put("Message", mmm);
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
                    Log.d("Main", "send Msg");
                    break;
                case 0x04:
                    //채팅 방 퇴장
                    JSONObject exitObject = new JSONObject();
                    try {
                        exitObject.put("Type", "3");
                        exitObject.put("Streamkey", stream_key);
                        exitObject.put("Userkey", name);
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
                    Log.d("Main", "send Msg");

                default:
                    Toast.makeText(StreamingActivity.this, "UNKNOWN MSG: " + m, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    public String getStreamkey() {
        long time = System.currentTimeMillis();
        SimpleDateFormat dayTime = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return dayTime.format(new Date(time));
    }

    public String getUserkey() {
        // 현재시간을 msec 으로 구한다.
        long now = System.currentTimeMillis();
        // 현재시간을 date 변수에 저장한다.
        Date date = new Date(now);
        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
        SimpleDateFormat sdfNow = new SimpleDateFormat("ss");
        return sdfNow.format(date);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Intent i = getIntent();
//        if (i.getBooleanExtra(DIRECTION, false)) {
        /////////////////////////////////전체 화면
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ////

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        } else {
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        }
//        if (i.getStringExtra(RTMPADDR) != null && !i.getStringExtra(RTMPADDR).isEmpty()) {
//            rtmpaddr = i.getStringExtra(RTMPADDR);
//        }

        //액션바 숨기기
//        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.hide();
//        }

        started = false;
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_streaming2);
        setContentView(R.layout.activity_streaming_land);
        readylayout = (FrameLayout) findViewById(R.id.ready_layout);

        //방송제목
        Room_name = (TextView) findViewById(R.id.broadcast_name);
//        Stream_et = (EditText) findViewById(R.id.stream_title);

        btn_start = (Button) findViewById(R.id.live_btn);
        btn_start.setOnClickListener(this);
        btn_switch = (ImageView) findViewById(R.id.switch_btn);
        btn_switch.setOnClickListener(this);
        btn_canel = (ImageView) findViewById(R.id.cancel_btn);
        btn_canel.setOnClickListener(this);

        //카메라 미리보기 뷰
        camera_preview = (AspectTextureView) findViewById(R.id.txv_preview);
//        camera_preview = (CameraBridgeViewBase) findViewById(R.id.txv_preview);
//        camera_preview = (JavaCameraView) findViewById(R.id.txv_preview);
        tv_speed = (TextView) findViewById(R.id.tv_speed);
        tv_rtmp = (TextView) findViewById(R.id.tv_rtmp);

        camera_preview.setKeepScreenOn(true);
        camera_preview.setSurfaceTextureListener(this);


        //스트림 객체
        resClient = new RESClient();
        resConfig = RESConfig.obtain();
        resConfig.setFilterMode(filtermode);
        resConfig.setTargetVideoSize(new Size(720, 480));

        //화질 설정

        resConfig.setBitRate(750 * 1024);
        resConfig.setVideoFPS(20);
        resConfig.setVideoGOP(1);
        resConfig.setRenderingMode(RESConfig.RenderingMode.OpenGLES);


        resConfig.setDefaultCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        int frontDirection, backDirection;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, cameraInfo);
        frontDirection = cameraInfo.orientation;
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo);
        backDirection = cameraInfo.orientation;

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            resConfig.setFrontCameraDirectionMode((frontDirection == 90 ? RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_270 : RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_90) | RESConfig.DirectionMode.FLAG_DIRECTION_FLIP_HORIZONTAL);
            resConfig.setBackCameraDirectionMode((backDirection == 90 ? RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_90 : RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_270));
        } else {
            resConfig.setBackCameraDirectionMode((backDirection == 90 ? RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_0 : RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_180));
            resConfig.setFrontCameraDirectionMode((frontDirection == 90 ? RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_180 : RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_0) | RESConfig.DirectionMode.FLAG_DIRECTION_FLIP_HORIZONTAL);
        }

        stream_key = getStreamkey();
        rtmpaddr = RTMP_Addr + stream_key;

        resConfig.setRtmpAddr(rtmpaddr);
        if (!resClient.prepare(resConfig)) {
            resClient = null;
            Log.e(TAG, "prepare,failed!!");
            Toast.makeText(this, "RESClient prepare failed", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Size s = resClient.getVideoSize();
        camera_preview.setAspectRatio(AspectTextureView.MODE_OUTSIDE, ((double) s.getWidth()) / s.getHeight());
//        camera_preview.setCvCameraViewListener(this);
        Log.d(TAG, "version=" + resClient.getVertion());

        resClient.setConnectionListener(this);
        resClient.setVideoChangeListener(this);

        mainHander = new Handler() {
            @Override
            public void handleMessage(Message msg) {
//                tv_speed.setText("byteSpeed=" + (resClient.getAVSpeed() / 1024) + ";drawFPS=" + resClient.getDrawFrameRate() + ";sendFPS=" + resClient.getSendFrameRate() + ";sendbufferfreepercent=" + resClient.getSendBufferFreePercent());
                sendEmptyMessageDelayed(0, 3000);
                if (resClient.getSendBufferFreePercent() <= 0.05) {
                    Toast.makeText(StreamingActivity.this, "sendbuffer is full,netspeed is low!", Toast.LENGTH_SHORT).show();
                }
            }
        };

        mainHander.sendEmptyMessage(0);

        resClient.setSoftAudioFilter(new SetVolumeAudioFilter());

        initView();
        name = getUserkey();

        //다이얼로그 띄우기
        MaterialDialog Start_dialog = new MaterialDialog.Builder(StreamingActivity.this)
                .customView(R.layout.dialog_stream, true)
                .autoDismiss(false)
                .cancelable(false)
//                .contentColorRes(R.color.White)
                .backgroundColorRes(R.color.White)
                .positiveColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorPrimary)
                .positiveText("시작하기")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        Toast.makeText(this, "live", Toast.LENGTH_SHORT).show();
                        if (!started) {
                            readylayout.setVisibility(View.GONE);
                            Room_name.setVisibility(View.VISIBLE);
                            Room_name.setText(Stream_et.getText().toString());

//                            채팅 서버 접속
                            connect(handler);
                            //                            final JSONObject json = new JSONObject();
//                            String title = Stream_et.getText().toString();
//
//                            try {
//                                json.put("title", title);
//                                json.put("stream_key",stream_key);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                            Log.d(TAG, "before stream:" + stream_key);
//
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    PHPRequest request = null;
//                                    try {
//                                        request = new PHPRequest(LiveStream_Addr);
//                                    } catch (MalformedURLException e) {
//                                        e.printStackTrace();
//                                    }
//                                    try {
//                                        json.put("type", request.Register_Stream);
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                    String before = json.toString();
//                                    String result = request.POSTJSON(before);
//                                    Log.d(TAG, "result:" + result);
//                                }
//                            }).start();
                            Log.d(TAG, "RTMP:" + rtmpaddr);

//                            스트리밍 시작
                            resClient.startStreaming();
                            started = !started;
                            Log.d(TAG, "is started? : " + (started ? "yes" : "no"));
                            dialog.dismiss();
                        } else {
                            readylayout.setVisibility(View.VISIBLE);
                            resClient.stopStreaming();
                        }
                    }
                })
                .negativeText("취소")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        //액티비티 종료
                        finish();
                    }
                })
                .show();

        View dialog_view = Start_dialog.getCustomView();
        if (dialog_view != null) {
            stream_thumnail = dialog_view.findViewById(R.id.stream_img);
            Stream_et = dialog_view.findViewById(R.id.stream_title);
        }

        //필터 적용 영상에 아이콘 띄우기
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        BaseHardVideoFilter filter = new IconHardFilter(bitmap, new Rect(100, 100, 400, 400));
        resClient.setHardVideoFilter(filter);
//        카메라 화면 폭 구하기
        Display display = StreamingActivity.this.getWindowManager().getDefaultDisplay();
        size = new android.graphics.Point();
        display.getSize(size);

        bp2 = BitmapFactory.decodeResource(getResources(), R.drawable.mask);


    }

    private void initView() {
        et_scroll = (TextView) findViewById(R.id.Chatview);

        chat_layout = (RelativeLayout) findViewById(R.id.chat_layout);
        broadcast_layout = (RelativeLayout) findViewById(R.id.broadcast_layout);


        broadcast_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chat_layout.getVisibility() == View.VISIBLE) {
                    chat_layout.setVisibility(View.GONE);
                } else {
                    chat_layout.setVisibility(View.VISIBLE);
                }

            }
        });
//        메시지 전송

        et_msg = (EditText) findViewById(R.id.msg_et_stream);
        btn_send = (Button) findViewById(R.id.send_btn_stream);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = String.valueOf(et_msg.getText()) + "\r\n";
                if (msg.length() != 0) {
                    handler.obtainMessage(0x03).sendToTarget();
                }
            }
        });

    }

    private void connect(final Handler handler) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.d("Main", "connect start");
                    group = new NioEventLoopGroup();

                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(group);
                    bootstrap.channel(NioSocketChannel.class);
                    bootstrap.handler(new ChatInitializer(handler));
                    bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                    bootstrap.option(ChannelOption.TCP_NODELAY, true);
                    Log.d("Main", "bootstrap set option");

//                    channelFuture = bootstrap.connect(new InetSocketAddress(host, port));
                    channelFuture = bootstrap.connect(Server_IP, Chat_Port);
                    Log.d("Main", "bootstrap connect");
                    channel = channelFuture.sync().channel();
                    channelFuture.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            //start 메시지 보내기
                            handler.obtainMessage(0x00).sendToTarget();
                        }
                    });

                    Log.d("Main", "add Listner");
                    channel.closeFuture().sync();
                    Log.d("Main", "channel.closeFuture().sync()");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //채팅 채널 끊기
        Log.d(TAG, "disconnect chat server");
        handler.obtainMessage(0x04).sendToTarget();

        if (mainHander != null) {
            mainHander.removeCallbacksAndMessages(null);
        }
        if (started) {
            resClient.stopStreaming();
        }
        if (resClient != null) {
            resClient.destroy();
        }
        super.onDestroy();
    }

    protected void reStartWithResolution(int w, int h) {
        /*
        //===========don`t interrupt streaming
        resClient.reSetVideoBitrate(1200*1024);
        resClient.reSetVideoSize(new Size(1280,720));
        //===or======interrupt streaming
        if (started) {
            resClient.stopStreaming();
        }
        resClient.stopPreview(false);
        resClient.destroy();
        resConfig.setTargetVideoSize(new Size(w, h));
        resClient.prepare(resConfig);
        resClient.startPreview(texture, sw, sh);
        Size s = resClient.getVideoSize();
        camera_preview.setAspectRatio(AspectTextureView.MODE_INSIDE, ((double) s.getWidth()) / s.getHeight());
        if (started) {
            resClient.startStreaming();
        }
        //===========
        */
    }

    @Override
    public void onOpenConnectionResult(int result) {
        if (result == 0) {
            Log.e(TAG, "server IP = " + resClient.getServerIpAddr());
        } else {
            Toast.makeText(this, "startfailed", Toast.LENGTH_SHORT).show();
        }
        /**
         * result==0 success
         * result!=0 failed
         */
//        tv_rtmp.setText("open=" + result);
        Log.d(TAG, "open=" + result);
    }

    @Override
    public void onWriteError(int errno) {
        if (errno == 9) {
            resClient.stopStreaming();
            resClient.startStreaming();
            Toast.makeText(this, "errno==9,restarting", Toast.LENGTH_SHORT).show();
        }
        /**
         * failed to write data,maybe restart.
         */
        Log.d(TAG, "writeError=" + errno);
//        tv_rtmp.setText("writeError=" + errno);
    }

    @Override
    public void onCloseConnectionResult(int result) {
        /**
         * result==0 success
         * result!=0 failed
         */
        Log.d(TAG, "close=" + result);
//        tv_rtmp.setText("close=" + result);
    }

    protected SurfaceTexture texture;
    protected int sw, sh;

    @Override
    public void onVideoSizeChanged(int width, int height) {
        camera_preview.setAspectRatio(AspectTextureView.MODE_INSIDE, ((double) width) / height);
    }

    private Mat grayscaleImage;
    private int absoluteFaceSize;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "on Surface Texture Available~!");
        if (resClient != null) {
            resClient.startPreview(surface, width, height);
        }
        texture = surface;
        sw = width;
        sh = height;

//        previewReader= ImageReader.newInstance(sw,sh,ImageFormat.YUV_420_888, 2);
//
//        previewReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
//            @Override
//            public void onImageAvailable(ImageReader imageReader) {
//
//            }
//        },backgroundHandler);


        grayscaleImage = new Mat(height, width, CvType.CV_8UC4);

        // The faces will be a 20% of the height of the screen
        absoluteFaceSize = (int) (height * 0.2);
        Log.d(TAG, "absoluteFaceSize: " + absoluteFaceSize);
//        texture.setOnFrameAvailableListener(this);
    }

    android.graphics.Point size;

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "frame captured!!");
//        //필터 적용 영상에 아이콘 띄우기
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
//        BaseHardVideoFilter filter = new IconHardFilter(bitmap,new Rect(100,100,400,400)) ;
//        resClient.setHardVideoFilter(filter);

//        Display display = StreamingActivity.this.getWindowManager().getDefaultDisplay();
//        size = new android.graphics.Point();
//        display.getSize(size);
    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (resClient != null) {
            resClient.updatePreview(width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (resClient != null) {
            resClient.stopPreview(true);
        }
        return false;
    }

    private int mFrameNum = 0;

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.d(TAG, "on surface texture updated");
        int second = Integer.parseInt(getUserkey());

        final Bitmap faceBP = camera_preview.getBitmap();

//        opencv 얼굴인식
//        for(int i = 0 ; i < 4 ; i ++){
//            Log.d(TAG,"face[" + i + "]: " + face[i] + "\n");
//        }

//        File cascadeDir = getDir("cascade" , Context.MODE_PRIVATE);
//        File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");


//        CascadeClassifier faceDetector
//                = new CascadeClassifier("android.resource://com.example.test/raw/haarcascade_frontalface_alt.xml");
////                = new CascadeClassifier(mCascadeFile.getAbsolutePath());
////                = new CascadeClassifier(StreamingActivity.this.getResources("haarcascade_frontalface_alt.xml").getPath());
////                = new CascadeClassifier("haarcascade_frontalface_alt.xml");
////                = new CascadeClassifier(FaceDetector.class.getResource("haarcascade_frontalface_alt.xml").getPath());
//        faceDetector.load("haarcascade_frontalface_alt");

        Mat image = new Mat();
//        Bitmap bp32 = bp.copy(Bitmap.Config.ARGB_8888, true);
//        Mat형태로 변환
        Utils.bitmapToMat(faceBP, image);
//        image.convertTo(image, CvType.CV_8U);
//        회색 필터를 입힌 이미지
        Imgproc.cvtColor(image, grayscaleImage, Imgproc.COLOR_RGBA2RGB);

        MatOfRect faceDetections = new MatOfRect();

        faceDetector.detectMultiScale(grayscaleImage,
                faceDetections, 1.1, 2, 2,
                new org.opencv.core.Size(absoluteFaceSize, absoluteFaceSize),
                new org.opencv.core.Size());

        Log.d(TAG, "face position start");
        if (faceDetections.toArray().length != 0) {
            for (org.opencv.core.Rect rect : faceDetections.toArray()) {
                int a,b,c,d ;
                a = (int) (rect.x/1.7);
                b = (int) (rect.y/1.7);
                c = (int) ((rect.x + rect.width)/1.7);
                d = (int) ((rect.y + rect.height)/1.7);

                Log.d(TAG, "face position \nx: " + rect.x
                        + " y: " + rect.y
                        + " width: " + rect.width
                        + " height: " + rect.height + "\n"
                        + "a: " + a
                        + "b: " + b
                        + "c: " + c
                        + "d: " + d + "\n"
                );

//            Point x = rt.tl();


                IconHardFilter filter = (IconHardFilter) resClient.acquireHardVideoFilter();
//                filter.updateIcon(bp2, new Rect(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height));
                filter.updateIcon(bp2, new Rect(a, b, c, d));
                resClient.releaseHardVideoFilter();
            }


        }

        //마스크 이미지

//
//        if (mFrameNum % 3 == 0) {
//            //얼굴 인식할 화면 이미지
//            final Bitmap faceBP = camera_preview.getBitmap();
//
//////                    라이브러리 얼굴 인식
////            FaceDet faceDet = new FaceDet(Constants.getFaceShapeModelPath());
////            List<VisionDetRet> results = faceDet.detect(faceBP);
////            if(results.size() > 0){
//////            for (VisionDetRet ret : results) {
////                VisionDetRet ret = results.get(0);
////                String label = ret.getLabel();
////                int rectLeft = ret.getLeft();
////                int rectTop = ret.getTop();
////                int rectRight = ret.getRight();
////                int rectBottom = ret.getBottom();
//
////            }
//            FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
//                    .setTrackingEnabled(false)
//                    .setLandmarkType(FaceDetector.ALL_LANDMARKS)
//                    .setMode(FaceDetector.FAST_MODE)
//                    .build();
//
//            if (!faceDetector.isOperational()) {
//                Toast.makeText(this, "Face detector가 작동하지 않습니다", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            Frame frame = new Frame.Builder().setBitmap(faceBP).build();
//            SparseArray<Face> sparseArray = faceDetector.detect(frame);
//
//            int rectLeft = 0, rectRight = 0, rectTop = 0, rectBottom = 0;
//            for (int i = 0; i < sparseArray.size(); i++) {
//                Face face = sparseArray.valueAt(0);
//                for(Landmark landmark : face.getLandmarks()){
//                    if(landmark.getType() == Landmark.LEFT_EYE){
//                        rectLeft = (int) landmark.getPosition().x;
//                        rectTop = (int) landmark.getPosition().y;
//                    }
//
//                    if(landmark.getType() == Landmark.RIGHT_EYE){
//                        rectRight = (int) landmark.getPosition().x;
//                    }
//
//                    if(landmark.getType() == Landmark.BOTTOM_MOUTH){
//                        rectBottom = (int) landmark.getPosition().y;
//                    }
//                }
//            }
//            IconHardFilter filter = (IconHardFilter) resClient.acquireHardVideoFilter();
////            filter.updateIcon(bp2, new Rect(second * 2 + 100, 100, second * 2 + 300, 300));
////            filter.updateIcon(bp2, new Rect(a, b, c, d));
////            filter.updateIcon(bp2, new Rect(rt.x, rt.y, size.x - (rt.x + rt.width), size.y - (rt.y + rt.height)));
////            filter.updateIcon(bp2, new Rect(100,100,200,200));
//            filter.updateIcon(bp2, new Rect(rectLeft, rectTop, rectRight, rectBottom));
//            resClient.releaseHardVideoFilter();
////            }
//        }
//
//        mFrameNum++;

//
//        FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
//                .setTrackingEnabled(false)
//                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
//                .setMode(FaceDetector.FAST_MODE)
//                .build();
//
//        if(!faceDetector.isOperational()){
//            Toast.makeText(this, "Face detector가 작동하지 않습니다", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Frame frame = new Frame.Builder().setBitmap(faceBP).build();
//        SparseArray<Face> sparseArray = faceDetector.detect(frame);
//        for(int i = 0 ; i < sparseArray.size() ; i ++){
//            Face face = sparseArray.valueAt(i);
//            float x = face.getPosition().x;
//            float y = face.getPosition().y;
////            for (Landmark landmark : face.getLandmarks()){
////                int cx = (int) landmark.getPosition().x;
////                int cy = (int) landmark.getPosition().y;
////
////                if(landmark.getType() == Landmark.NOSE_BASE){
////                    int scaleWidth =
////                }
////            }
//        }


//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                //        라이브러리 얼굴 인식
//                FaceDet faceDet = new FaceDet(Constants.getFaceShapeModelPath());
//                List<VisionDetRet> results = faceDet.detect(faceBP);
//
//                for (VisionDetRet ret : results) {
//                    String label = ret.getLabel();
//                    int rectLeft = ret.getLeft();
//                    int rectTop = ret.getTop();
//                    int rectRight = ret.getRight();
//                    int rectBottom = ret.getBottom();
////            ArrayList<android.graphics.Point> landmarks = ret.getFaceLandmarks();
////            for (android.graphics.Point point : landmarks) {
////                int pointX = point.x;
////                int pointY = point.y;
////                Log.d(TAG,"point x: " + pointX + "\npoint y: " + pointY);
////            }
//                    IconHardFilter filter = (IconHardFilter) resClient.acquireHardVideoFilter();
////            filter.updateIcon(bp2, new Rect(second * 2 + 100, 100, second * 2 + 300, 300));
////            filter.updateIcon(bp2, new Rect(a, b, c, d));
////            filter.updateIcon(bp2, new Rect(rt.x, rt.y, size.x - (rt.x + rt.width), size.y - (rt.y + rt.height)));
////            filter.updateIcon(bp2, new Rect(100,100,200,200));
//                    filter.updateIcon(bp2, new Rect(rectLeft, rectTop, rectRight, rectBottom));
//                    resClient.releaseHardVideoFilter();
//                }
//            }
//        });

//        opencv 얼굴인식
//        for(int i = 0 ; i < 4 ; i ++){
//            Log.d(TAG,"face[" + i + "]: " + face[i] + "\n");
//        }

//        File cascadeDir = getDir("cascade" , Context.MODE_PRIVATE);
//        File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");


//        CascadeClassifier faceDetector
//                = new CascadeClassifier("android.resource://com.example.test/raw/haarcascade_frontalface_alt.xml");
//                = new CascadeClassifier(mCascadeFile.getAbsolutePath());
//                = new CascadeClassifier(StreamingActivity.this.getResources("haarcascade_frontalface_alt.xml").getPath());
//                = new CascadeClassifier("haarcascade_frontalface_alt.xml");
//                = new CascadeClassifier(FaceDetector.class.getResource("haarcascade_frontalface_alt.xml").getPath());
//        faceDetector.load("haarcascade_frontalface_alt");
//
//        Mat image = new Mat();
////        Bitmap bp32 = bp.copy(Bitmap.Config.ARGB_8888, true);
////        Mat형태로 변환
//        Utils.bitmapToMat(bp,image);
////        image.convertTo(image, CvType.CV_8U);
////        회색 필터를 입힌 이미지
//        Imgproc.cvtColor(image,grayscaleImage,Imgproc.COLOR_RGBA2RGB);
//
//        MatOfRect faceDetections = new MatOfRect();
//
//        faceDetector.detectMultiScale(grayscaleImage,
//                faceDetections, 1.1, 2, 2,
//                new org.opencv.core.Size(absoluteFaceSize, absoluteFaceSize),
//                new org.opencv.core.Size());
//
//        Log.d(TAG,"face position start");
//        for (org.opencv.core.Rect rect : faceDetections.toArray()) {
//            Log.d(TAG,"face position \nx: " + rect.x
//                    + " y: " + rect.y
//                    + " width: " + rect.width
//                    + " height: " + rect.height + "\n");
//        }
//
//        if(faceDetections.toArray().length != 0){
//            org.opencv.core.Rect rt = faceDetections.toArray()[0];
////            Point x = rt.tl();
////            int a,b,c,d ;
////            a = (int) (rt.x/1.7);
////            b = (int) (rt.y/1.7);
////            c = (int) ((rt.x + rt.width)/1.7);
////            d = (int) ((rt.y + rt.height)/1.7);
//            IconHardFilter filter = (IconHardFilter) resClient.acquireHardVideoFilter();
////        filter.updateIcon(bp2, new Rect(second * 2 + 100, 100, second * 2 + 300, 300));
////            filter.updateIcon(bp2, new Rect(a, b, c, d));
//            filter.updateIcon(bp2, new Rect(rt.x, rt.y, size.x - (rt.x + rt.width), size.y - (rt.y + rt.height)));
////            filter.updateIcon(bp2, new Rect(100,100,200,200));
//            resClient.releaseHardVideoFilter();
//    }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.live_btn:
                Toast.makeText(this, "live", Toast.LENGTH_SHORT).show();
                if (!started) {
                    readylayout.setVisibility(View.GONE);
                    Room_name.setVisibility(View.VISIBLE);
                    //방송방만들기
                    final JSONObject json = new JSONObject();
                    String title = Stream_et.getText().toString();

                    try {
                        json.put("title", title);
                        json.put("stream_key", stream_key);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "before stream:" + stream_key);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            PHPRequest request = null;
                            try {
                                request = new PHPRequest(LiveStream_Addr);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                            try {
                                json.put("type", request.Register_Stream);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String before = json.toString();
                            String result = request.POSTJSON(before);
                            Log.d(TAG, "result:" + result);
                        }
                    }).start();

                    Log.d(TAG, "RTMP:" + rtmpaddr);

                    resClient.startStreaming();
                    started = !started;
                    Log.d(TAG, "is started? : " + (started ? "yes" : "no"));
                } else {
                    readylayout.setVisibility(View.VISIBLE);
                    resClient.stopStreaming();
                }

                break;

            case R.id.switch_btn:
                resClient.swapCamera();
                break;
            case R.id.btn_flash:
                resClient.toggleFlashLight();
                break;
            case R.id.btn_screenshot:
                resClient.takeScreenShot(new RESScreenShotListener() {
                    @Override
                    public void onScreenShotResult(Bitmap bitmap) {
                        File f = new File("/sdcard/" + System.currentTimeMillis() + "_libres.png");
                        try {
                            if (!f.exists()) {
                                f.createNewFile();
                            }
                            OutputStream outputStream = new FileOutputStream(f);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            outputStream.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
                break;
            case R.id.cancel_btn:
                if (started) {
                    readylayout.setVisibility(View.VISIBLE);
                    resClient.stopStreaming();
                    started = !started;
                }
                finish();
                break;
        }
    }

//    @Override
//    public void onCameraViewStarted(int width, int height) {
//        if (resClient != null) {
//            resClient.startPreview(surface, width, height);
//        }
//        texture = new SurfaceTexture(10);
//        texture = surface;
//        sw = width;
//        sh = height;
//    }
//
//    @Override
//    public void onCameraViewStopped() {
//
//    }
//
//    @Override
//    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        return null;
//    }


    class FilterItem {
        String name;
        HardVideoGroupFilter filter;

        public FilterItem(String name, HardVideoGroupFilter filter) {
            this.name = name;
            this.filter = filter;
        }
    }

    class FilterAdapter extends BaseAdapter {
        private List<FilterItem> filters;
        private int selectIndex = 0;

        FilterAdapter() {
            filters = new ArrayList<>(0);
        }

        public boolean selectItem(int index) {
            if (selectIndex == index) {
                return false;
            }
            selectIndex = index;
            notifyDataSetChanged();
            return true;
        }

        public void updateFilters(List<FilterItem> filters) {
            this.filters = filters == null ? new ArrayList<FilterItem>(0) : filters;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return filters.size();
        }

        @Override
        public Object getItem(int position) {
            return filters.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(StreamingActivity.this).inflate(me.lake.librestreaming.sample.R.layout.item_filter, parent, false);
                convertView.setTag(new FilterAdapter.ViewHolder(convertView));
            }
            if (selectIndex == position) {
                ((FilterAdapter.ViewHolder) convertView.getTag()).iv_star.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                ((FilterAdapter.ViewHolder) convertView.getTag()).iv_star.setImageResource(android.R.drawable.btn_star_big_off);
            }

            ((FilterAdapter.ViewHolder) convertView.getTag()).tv_name.setText(filters.get(position).name);
            return convertView;
        }

        class ViewHolder {
            TextView tv_name;
            ImageView iv_star;

            public ViewHolder(View v) {
                this.tv_name = (TextView) v.findViewById(me.lake.librestreaming.sample.R.id.tv_name);
                this.iv_star = (ImageView) v.findViewById(me.lake.librestreaming.sample.R.id.iv_star);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
