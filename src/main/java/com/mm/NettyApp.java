package com.mm;

import com.mm.handler.ServerHandler;
import com.mm.handler.ServerIdleStateHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * netty server
 *
 * @author lwl
 */
@Slf4j
@SpringBootApplication
public class NettyApp implements ApplicationRunner {

    public static final String WEBSOCKET_PATH = "/ws";
    public static final Integer WEBSOCKET_PORT = 8888;

    public static void main(String[] args) {
        SpringApplication.run(NettyApp.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                            .addLast(new LoggingHandler("DEBUG"))
                            .addLast(new HttpServerCodec())
                            .addLast(new HttpObjectAggregator(65536))
                            .addLast(new ChunkedWriteHandler())
                            .addLast(new ServerIdleStateHandler())
                            .addLast(new ServerHandler(WEBSOCKET_PATH));
                }
            });
            Channel channel = bootstrap.bind(WEBSOCKET_PORT).sync().channel();
            log.info("websocket服务器启动成功：{}", channel);
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("websocket服务器运行出错：", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("websocket服务器已关闭");
        }
    }
}
