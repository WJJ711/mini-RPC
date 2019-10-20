package com.wjj.netty.handler;


import com.wjj.netty.constant.Const;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;


public class SimpleClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if ("ping".equals(msg.toString())){
            ctx.channel().writeAndFlush("pang\r\n");
            return;
        }

        //这个不是主线程，同步到主线程
        ctx.channel().attr(AttributeKey.valueOf(Const.SERVER_MESSAGE_KEY)). set(msg);
        //ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }
}
