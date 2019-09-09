package com.saint.netty.handler.process;

import com.saint.netty.params.Msg;
import io.netty.channel.ChannelHandlerContext;

/**
 * @description:
 * @date: 2019/9/9 17:50
 */
public abstract class AbstractProcessMsg {

    public abstract void process(Msg.NettyMsg msg, ChannelHandlerContext ctx);
}
