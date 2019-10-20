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

/**
 * 长连接
 */
public class TCPClient {
    private static Logger logger = Logger.getLogger(TCPClient.class);

    private static final Bootstrap b = new Bootstrap();
    private static ChannelFuture f = null;

    static {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
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
        String host = "localhost";
        int port = 8080;
        try {
            // Start the client.
            f = b.connect(host, port).sync(); // (5)

        } catch (Exception e) {
            logger.error("NettyClient错误" + e);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 发送数据
     * 注意：1、每一个请求都是同一个连接，并发问题
     * 解决：1、每一次请求都有唯一的id来识别 2、请求内容
     * @param request
     * @return
     */

    public static Response send(ClientRequest request){
        f.channel().writeAndFlush(request);

        DefaultFuture df=new DefaultFuture(request);
        return df.get();
    }

    public static void main(String[] args) {

    }
}
