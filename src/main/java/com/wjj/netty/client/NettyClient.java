package com.wjj.netty.client;

import com.wjj.netty.constant.Const;
import com.wjj.netty.handler.SimpleClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClient {
    private static Logger logger= Logger.getLogger(NettyClient.class);

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
                    ch.pipeline().addLast(new StringDecoder());
                    ch.pipeline().addLast(new SimpleClientHandler());
                    ch.pipeline().addLast(new StringEncoder());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            f.channel().writeAndFlush("hello server");
            f.channel().writeAndFlush("\r\n");
            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
            Object result = f.channel().attr(AttributeKey.valueOf(Const.SERVER_MESSAGE_KEY)).get();
            logger.info("获取到服务器返回的数据====="+result);
        }catch (Exception e){
            logger.error("NettyClient错误"+e);
        }
        finally {
            workerGroup.shutdownGracefully();
        }
    }
}
