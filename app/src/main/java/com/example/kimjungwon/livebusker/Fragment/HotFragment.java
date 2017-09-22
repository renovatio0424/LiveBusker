package com.example.kimjungwon.livebusker.Fragment;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kimjungwon.livebusker.Netty.ChatInitializer;
import com.example.kimjungwon.livebusker.R;


import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by kimjungwon on 2017-09-07.
 */

public class HotFragment extends Fragment {

    TextView et_scroll;
    EditText et_msg;
    Button btn_send;

    Activity activity;

    String host = "52.78.246.168";
    int port = 8889;
// String host = "192.168.1.200";
// String host = "10.0.2.2";


    NioEventLoopGroup group;
    Channel channel;
    ChannelFuture channelFuture;
    String name;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.activity_chatting,container,false);
        initView(rootview);
        return rootview;
    }

    private void initView(View view) {
        connect(handler);

        et_scroll = (TextView) view.findViewById(R.id.et_scroll);
        et_msg = (EditText) view.findViewById(R.id.et_msg);
        btn_send = (Button) view.findViewById(R.id.btn_send);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = String.valueOf(et_msg.getText()) + "\r\n";
                if (msg.length() != 0) {
                    handler.obtainMessage(0x03).sendToTarget();
                }
            }
        });

//        btn_pic.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(intent, 1);
//            }
//        });
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
                    channelFuture = bootstrap.connect(host,port);
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
                    //online

                    String hello = new String("[" +name + "] joined!");

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
                    et_scroll.setText(et_scroll.getText() + m + "\r\n");
                    break;
                case 0x02:
                    //send complete
                    et_msg.setText("");
                    break;
                case 0x03:
                    //send txt
//                    String name = pref.getString("User","no");
                    String mmm = String.valueOf("[" + name + "] " + et_msg.getText() + "");
                    if (mmm.length() == 0)
                        return;

                    ByteBuf msgBuffer = Unpooled.buffer();
                    msgBuffer.writeBytes(mmm.getBytes());
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
                    //send pic

                default:
                    Toast.makeText(activity, "UNKNOWN MSG: " + m, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }
}
