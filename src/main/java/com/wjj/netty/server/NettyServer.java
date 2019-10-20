package com.wjj.netty.server;

import com.wjj.netty.constant.Const;
import com.wjj.netty.factory.ZookeeperFactory;
import com.wjj.netty.handler.SimpleServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class NettyServer {
    public static void main(String[] args) {
        EventLoopGroup parentGroup = new NioEventLoopGroup();
        EventLoopGroup childGroup = new NioEventLoopGroup();
        try {
            //启动引擎
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(parentGroup, childGroup);
            //允许有128个通道排队
            bootstrap.option(ChannelOption.SO_BACKLOG, 128)
                    //心跳包，设为false，自己做心跳检测
                    .childOption(ChannelOption.SO_KEEPALIVE, false)
                    //绑定通道
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            //解码器
                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(65535, Delimiters.lineDelimiter()[0]));
                            ch.pipeline().addLast(new StringDecoder());
                            //心跳检测
                            ch.pipeline().addLast(new IdleStateHandler(60,45,20, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new SimpleServerHandler());
                            //对字符串编码
                            ch.pipeline().addLast(new StringEncoder());
                        }
                    });
            //绑定端口
            ChannelFuture f =  bootstrap.bind(8080).sync();
            CuratorFramework client = ZookeeperFactory.create();
            InetAddress netAddress=InetAddress.getLocalHost();
            client.create().withMode(CreateMode.EPHEMERAL).forPath(Const.SERVER_PATH+netAddress.getHostAddress());

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            childGroup.shutdownGracefully();
            parentGroup.shutdownGracefully();

        }finally {
        }


    }
}
