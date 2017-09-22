package com.example.kimjungwon.livebusker.Netty;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by kimjungwon on 2017-09-20.
 */

public class ChatHandler extends ChannelInboundHandlerAdapter {
    //SimpleChannelInboundHandler<String>
    final Handler handler;

    public ChatHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Log.d("handler", "read msg");

        Message message = handler.obtainMessage(0x01);
        String s = ((ByteBuf)msg).toString(Charset.defaultCharset());
        Channel incoming = ctx.channel();
        message.obj = s;
        message.sendToTarget();
    }


    //    @Override
//    protected void channelRead(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
//        Message message = handler.obtainMessage(0x01);
//        message.obj = "[SYSTEM] - " + s;
//        message.sendToTarget();
//    }

//    @Override
//    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
//        Message message = handler.obtainMessage(0x01);
//        message.obj = "[SYSTEM] - " + s;
//        message.sendToTarget();
//    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        super.channelRead(ctx, msg);
//        String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset());
//
//        Log.d("ChatHandler","channel read");
//
//        Message message = handler.obtainMessage(0x01);
//        message.obj = "[System] - " + readMessage;
//        message.sendToTarget();
//    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        Message message = handler.obtainMessage(0x01);
//        message.obj = "\n[SYSTEM] - CLIENT ACTIVE";
//        message.sendToTarget();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        Message message = handler.obtainMessage(0x01);
//        message.obj = "[SYSTEM] - CLIENT INACTIVE";
//        message.sendToTarget();
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Log.d("Chat Handler", "exception Caught");

        cause.printStackTrace();
        ctx.close();
    }
}
