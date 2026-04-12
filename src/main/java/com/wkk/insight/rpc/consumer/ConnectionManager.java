package com.wkk.insight.rpc.consumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
@Slf4j
public class ConnectionManager {

    private final Map<String, ChannelWrapper> channelMap = new ConcurrentHashMap<>();

    private final Bootstrap bootstrap;

    public ConnectionManager(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Channel getChannel(String host, int port) {
        String key = host + ":" + port;
        ChannelWrapper channelWrapper = channelMap.computeIfAbsent(key, (k) -> {
            try {
                ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
                Channel channel = channelFuture.channel();
                channel.closeFuture().addListener((f) -> channelMap.remove(key));
                return new ChannelWrapper(channel);
            } catch (InterruptedException e) {
                log.error("链接超时{}, {}", host, port);
                return new ChannelWrapper(null);
            }
        });
        Channel channel = channelWrapper.channel;
        if (channel == null || !channel.isActive()) {
            channelMap.remove(key);
        }
        return channel;
    }

    private static class ChannelWrapper {

        private final Channel channel;

        public ChannelWrapper(Channel channel) {
            this.channel = channel;
        }
    }
}
