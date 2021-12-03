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
        JSONObject wsBody = JSONUtil.parseObj(wsMsgDto.getBody());
        if (1 == wsBody.getInt("type")) {
            String toDeviceId = wsBody.getStr("to_device_id");
            String channelId = NettyConfig.deviceIdChannelIdMap.get(toDeviceId);
            if (channelId == null) {
                log.debug("toDeviceId[{}] not exist", toDeviceId);
                return;
            }
            Channel channel = NettyConfig.channelIdChannelMap.get(channelId);
            if (channel == null) {
                log.debug("client not exist");
                return;
            }
            channel.writeAndFlush(new TextWebSocketFrame(wsBody.getStr("msg")));
        } else {
            NettyConfig.channelIdChannelMap.forEach((k, v) -> {
                v.writeAndFlush(new TextWebSocketFrame(wsBody.getStr("msg")));
            });
//            NettyConfig.group.writeAndFlush(wsMsg);
        }
    }
}
