package com.saint.netty.handler.process;

import com.saint.netty.annotation.MsgType;
import com.saint.netty.constant.SaintNettyConstant;
import com.saint.netty.params.Msg.NettyMsg;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description:
 * @date: 2019/9/10 14:40
 */
@MsgType(msgType = SaintNettyConstant.HEART_BEAT)
public class HeartBeantHandler extends AbstractProcessMsg {

    public final static Logger logger = LoggerFactory.getLogger(HeartBeantHandler.class);

    @Override
    public void process(NettyMsg msg, ChannelHandlerContext ctx) {
        logger.info("服务端收到"+ctx.channel().id()+"心跳包");
    }
}
