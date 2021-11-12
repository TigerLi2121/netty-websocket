package com.mm;

import com.mm.handler.ChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * netty server
 * @author lwl
 */
@Slf4j
public class NettyApp {

    public static void main(String[] args) {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelHandler());
            Channel channel = bootstrap.bind(8888).sync().channel();
            log.info("websocket服务器启动成功：{}", channel);
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("websocket服务器运行出错：", e);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            log.info("websocket服务器已关闭");
        }
    }

}
