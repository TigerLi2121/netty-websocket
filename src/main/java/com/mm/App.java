package com.mm;

import com.mm.handler.ChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

    public static void main(String[] args) {

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(eventLoopGroup, workGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelHandler());
            Channel channel = bootstrap.bind(8888).sync().channel();
            log.info("websocket服务器启动成功：{}", channel);
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("websocket服务器运行出错：", e);
        } finally {
            eventLoopGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            log.info("websocket服务器已关闭");
        }
    }

}
