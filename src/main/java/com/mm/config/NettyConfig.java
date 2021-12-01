package com.mm.config;

import com.mm.listener.RedisListener;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * netty config
 *
 * @author lwl
 */
@Configuration
public class NettyConfig {

    public static final String NETTY_TOPIC = "netty_topic";

    public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static Map<String, Channel> channelIdChannelMap = new ConcurrentHashMap<>();
    /**
     * 设备id - channelId
     */
    public static Map<String, String> deviceIdChannelIdMap = new ConcurrentHashMap<>();

    /**
     * 根据channelId获取设备id
     *
     * @param channelId
     * @return
     */
    public static String getDeviceId(String channelId) {
        for (Map.Entry<String, String> entry : deviceIdChannelIdMap.entrySet()) {
            if (entry.getValue().equals(channelId)) {
                return entry.getKey();
            }
        }
        return null;
    }

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

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(new RedisListener(), new ChannelTopic(NETTY_TOPIC));
        return container;
    }
}
