package com.mm.listener;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mm.config.NettyConfig;
import com.mm.dto.WsMsgDto;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * redis listener
 *
 * @author lwl
 */
@Slf4j
@Component
public class RedisListener implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody());
        log.debug("topic:{} body:{}", new String(message.getChannel()), body);
        WsMsgDto wsMsgDto = JSONUtil.toBean(body, WsMsgDto.class);
//        Channel channel = NettyConfig.group.get(wsMsgDto.getChannelId());
//        if (channel == null) {
//            log.debug("client not exist");
//            return;
//        }
        JSONObject wsBody = JSONUtil.parseObj(wsMsgDto.getBody());
        TextWebSocketFrame wsMsg = new TextWebSocketFrame(wsBody.getStr("msg"));
        if (1 == wsBody.getInt("type")) {
            String toDeviceId = wsBody.getStr("to_device_id");
            String channelId = NettyConfig.deviceIdChannelIdMap.get(toDeviceId);
            Channel channel = NettyConfig.channelIdChannelMap.get(channelId);
            if (channel == null) {
                log.debug("client not exist");
                return;
            }
            channel.writeAndFlush(wsMsg);
        } else {
            //服务端向每个连接上来的客户端发送消息
            NettyConfig.channelIdChannelMap.forEach((k, v) -> {
                v.writeAndFlush(wsMsg);
            });
        }
    }
}
