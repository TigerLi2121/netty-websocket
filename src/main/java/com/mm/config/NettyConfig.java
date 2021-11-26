package com.mm.config;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * netty config
 *
 * @author lwl
 */
@Component
public class NettyConfig {

    public static final String NETTY_TOPIC = "netty_topic";

    public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static Map<String, Channel> channelIdChannelMap = new ConcurrentHashMap<>();
    /**
     * 设备id - channelId
     */
    public static Map<String, String> deviceIdChannelIdMap = new ConcurrentHashMap<>();

    /**
     * add cache channel
     *
     * @param channel
     */
    public static void addChannel(Channel channel) {
        group.add(channel);
        channelIdChannelMap.put(channel.id().toString(), channel);
    }

    /**
     * delete cache channel
     *
     * @param channelId
     */
    public static void delChannel(String channelId) {
        group.remove(channelIdChannelMap.get(channelId));
        channelIdChannelMap.remove(channelId);
        deviceIdChannelIdMap.forEach((k, v) -> {
            if (v.equals(channelId)) {
                deviceIdChannelIdMap.remove(k);
            }
        });
    }

}
