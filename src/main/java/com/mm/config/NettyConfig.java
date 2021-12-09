package com.mm.config;

import com.mm.listener.RedisListener;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
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

    public static Map<String, Channel> deviceIdChannelMap = new ConcurrentHashMap<>();

    static AttributeKey<String> deviceIdKey = AttributeKey.valueOf("deviceId");

    /**
     * 根据channel获取设备id
     *
     * @param channel
     * @return
     */
    public static String getDeviceId(Channel channel) {
        return channel.attr(deviceIdKey).get();
    }

    /**
     * add cache channel
     *
     * @param channel
     */
    public static void addChannel(Channel channel, String deviceId) {
        deviceIdChannelMap.put(deviceId, channel);
        channel.attr(deviceIdKey).set(deviceId);
    }

    /**
     * delete cache channel
     *
     * @param deviceId
     */
    public static void delChannel(String deviceId) {
        deviceIdChannelMap.remove(deviceId);
    }

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(new RedisListener(), new ChannelTopic(NETTY_TOPIC));
        return container;
    }
}
