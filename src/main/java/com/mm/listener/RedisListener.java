package com.mm.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * redis listener
 * @author lwl
 */
@Slf4j
@Component
public class RedisListener implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.debug("channel:{}", new String(message.getChannel()));
        log.debug("body:{}", new String(message.getBody()));
    }
}
