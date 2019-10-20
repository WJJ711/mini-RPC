package com.wjj.netty.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleServerHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger= LoggerFactory.getLogger(SimpleServerHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info(msg.toString());
        if ("pang".equals(msg.toString())){
            return;
        }
         ctx.channel().writeAndFlush("Server: ok\r\n");
         //服务器不要close
        // ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
       if (evt instanceof IdleStateEvent){
           IdleStateEvent event = (IdleStateEvent) evt;
           if (event.state().equals(IdleState.READER_IDLE)){
               logger.info("读空闲======");
               ctx.channel().close();
           }else if (event.state().equals(IdleState.WRITER_IDLE)){
               logger.info("写空闲=========");
           }else if (event.state().equals(IdleState.ALL_IDLE)){
               logger.info("读写空闲");
                ctx.channel().writeAndFlush("ping\r\n");
           }
       }
    }
}
