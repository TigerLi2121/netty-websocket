package com.mm.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mm.config.NettyConfig;
import com.mm.dto.WsMsgDto;
import com.mm.util.RedisUtil;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理接收消息
 *
 * @author lwl
 */
@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler {
    private String websocketPath;
    private WebSocketServerHandshaker wsh;

    public ServerHandler(String websocketPath) {
        this.websocketPath = websocketPath;
    }

    private String getWebSocketLocation(ChannelPipeline cp, FullHttpRequest req) {
        String protocol = "ws";
        if (cp.get(SslHandler.class) != null) {
            protocol = "wss";
        }
        return protocol + "://" + req.headers().get(HttpHeaderNames.HOST) + websocketPath;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        String channelId = ctx.channel().id().toString();
        log.debug("channelId:{} deviceId:{} msg:{}", channelId, NettyConfig.getDeviceId(channelId), msg);
        // HTTP接入
        if (msg instanceof FullHttpRequest) {
            handHttpRequest(ctx, (FullHttpRequest) msg);
            // WebSocket接入
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketRequest(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * 客户端加入连接
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().toString();
        NettyConfig.addChannel(ctx.channel());
        log.debug("客户端加入连接 channelId{} 当前在线总数:{}", channelId, NettyConfig.channelIdChannelMap.size());
    }

    /**
     * 客户端断开连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().toString();
        String deviceId = NettyConfig.getDeviceId(channelId);
        NettyConfig.delChannel(channelId);
        log.debug("客户端断开连接 channelId{} deviceId:{} 当前在线总数:{}", channelId, deviceId, NettyConfig.channelIdChannelMap.size());
    }

    /**
     * 工程出现异常的时候调用
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String channelId = ctx.channel().id().toString();
        String deviceId = NettyConfig.getDeviceId(channelId);
        log.debug("客户端异常断开连接 channelId{} deviceId:{} 当前在线总数:{}", channelId, deviceId, NettyConfig.channelIdChannelMap.size());
        log.error("exceptionCaught e:", cause.getMessage());
        NettyConfig.delChannel(channelId);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    private void handleWebSocketRequest(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        //判断是否是关闭websocket的指令
        if (frame instanceof CloseWebSocketFrame) {
            wsh.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        //判断是否是ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        //判断是否是二进制消息
        if (!(frame instanceof TextWebSocketFrame)) {
            log.debug("不支持二进制消息");
            throw new UnsupportedOperationException(String.format("%s frame types not supported",
                    frame.getClass().getName()));
        }
        //文本接收和发送
        String msg = ((TextWebSocketFrame) frame).text();
        String channelId = ctx.channel().id().toString();
        String deviceId = NettyConfig.getDeviceId(channelId);
        log.debug("channelId:{} deviceId:{} json msg:{}", channelId, deviceId, msg);
        RedisUtil.redisTemplate.convertAndSend(NettyConfig.NETTY_TOPIC,
                JSONUtil.toJsonStr(new WsMsgDto(channelId, msg)));
    }


    /**
     * 处理客户端向服务端发起http握手请求业务
     *
     * @param ctx
     * @param req
     */
    private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        //判断是否http握手请求
        String upgrade = req.headers().get(StrUtil.upperFirst(HttpHeaderValues.UPGRADE.toString()));
        if (!req.decoderResult().isSuccess() || !(HttpHeaderValues.WEBSOCKET.toString().equals(upgrade))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(ctx.pipeline(), req), null, false);
        wsh = factory.newHandshaker(req);
        if (wsh == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            String url = req.uri();
            String deviceId = url.replace(websocketPath + "/", "");
            log.info("url:{} deviceId:{} channelId:{}", url, deviceId, ctx.channel().id());
            NettyConfig.deviceIdChannelIdMap.put(deviceId, ctx.channel().id().toString());
            wsh.handshake(ctx.channel(), req);
        }
    }

    /**
     * 服务端想客户端发送响应消息
     *
     * @param ctx
     * @param req
     * @param resp
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse resp) {
        HttpResponseStatus status = resp.status();
        if (status != HttpResponseStatus.OK) {
            ByteBufUtil.writeUtf8(resp.content(), status.toString());
            HttpUtil.setContentLength(req, resp.content().readableBytes());
        }
        boolean keepAlive = HttpUtil.isKeepAlive(req) && status == HttpResponseStatus.OK;
        HttpUtil.setKeepAlive(req, keepAlive);
        ChannelFuture future = ctx.write(resp);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
