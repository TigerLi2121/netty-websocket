package com.mm;

import com.mm.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * netty server
 * @author lwl
 */
@Slf4j
@SpringBootApplication
public class NettyApp implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(NettyApp.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                            //设置log监听器，并且日志级别为debug，方便观察运行流程
                            .addLast(new LoggingHandler("DEBUG"))
                            //编解码http请求
                            .addLast(new HttpServerCodec())
                            //聚合解码HttpRequest/HttpContent/LastHttpContent到FullHttpRequest
                            //保证接收的Http请求的完整性
                            .addLast(new HttpObjectAggregator(65536))
                            //用于大数据的分区传输
                            .addLast(new ChunkedWriteHandler())
                            //自定义的业务handler
                            .addLast(new ServerHandler())
                            //指定url
                            .addLast(new WebSocketServerProtocolHandler("/ws", null, true,
                                    65536 * 10));
                }
            });
            Channel channel = bootstrap.bind(8889).sync().channel();
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
