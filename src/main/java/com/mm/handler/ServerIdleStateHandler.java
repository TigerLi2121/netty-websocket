package com.mm.handler;

import com.mm.config.NettyConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 服务端空闲检测
 *
 * @author lwl
 */
@Slf4j
public class ServerIdleStateHandler extends IdleStateHandler {
    /**
     * 空闲检测时间
     */
    private static final int READER_IDLE_TIME = 600;

    public ServerIdleStateHandler() {
        super(READER_IDLE_TIME, 0, 0, TimeUnit.SECONDS);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        String channelId = ctx.channel().id().toString();
        String deviceId = NettyConfig.getDeviceId(channelId);
        log.debug("{} 秒内没有读取到数据,关闭连接 channelId:{} deviceId:{} 当前在线总数:{}", READER_IDLE_TIME,
                channelId, deviceId, NettyConfig.channelIdChannelMap.size());
        ctx.channel().close();

    }
}
