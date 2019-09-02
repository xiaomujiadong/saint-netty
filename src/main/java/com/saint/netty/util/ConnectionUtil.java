package com.saint.netty.util;

import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @date: 2019/9/2 20:08
 */
public class ConnectionUtil {

    private static final Map<String, Channel> mapChannel = new HashMap<>();

    public static Channel getChannel(String userId){
        return mapChannel.get(userId);
    }

    public static void addChannel(String userId, Channel channel){
        mapChannel.put(userId, channel);
    }

    public static Map<String, Channel> getChannelMap(){
        return mapChannel;
    }
}
