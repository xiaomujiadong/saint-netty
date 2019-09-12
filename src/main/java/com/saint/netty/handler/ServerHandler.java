package com.saint.netty.handler;

import com.alibaba.fastjson.JSONObject;
import com.saint.netty.constant.CodeStatusEnum;
import com.saint.netty.constant.SaintNettyConstant;
import com.saint.netty.entity.MsgReturn;
import com.saint.netty.handler.process.AbstractProcessMsg;
import com.saint.netty.params.Msg;
import com.saint.netty.util.AnnotionManagerUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    public final static Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleState idleState = ((IdleStateEvent)evt).state();
            if(idleState == IdleState.READER_IDLE){
                logger.warn("通道不活跃，即将关闭");
                ctx.channel().pipeline().close();
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object evt) throws Exception {
        Msg.NettyMsg msg = (Msg.NettyMsg)evt;

        ctx.writeAndFlush(Msg.NettyMsg.newBuilder()
                .setMsgId(msg.getMsgId())
                .setMsgType(SaintNettyConstant.ACK).build());

        AbstractProcessMsg processor = AnnotionManagerUtil.getProcessorByMsgType(msg.getMsgType());
        if(processor!=null){
            processor.process(msg, ctx);
            return;
        }
        MsgReturn<String> msgReturn = MsgReturn.renderFailure(CodeStatusEnum.PARAMS_ERROR);
        msgReturn.setData("传入的msgType="+msg.getMsgType()+"， 没有对应的处理器");

        ctx.writeAndFlush(Msg.NettyMsg.newBuilder()
                .setMsgId(msg.getMsgId())
                .setMsgType(SaintNettyConstant.RESPONSE_MSG_TYPE)
                .setContent(JSONObject.toJSONString(msgReturn)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
