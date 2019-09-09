package com.saint.netty.handler;

import com.alibaba.fastjson.JSONObject;
import com.saint.netty.constant.CodeStatusEnum;
import com.saint.netty.constant.MsgTypeEnum;
import com.saint.netty.entity.MsgReturn;
import com.saint.netty.handler.process.AbstractProcessMsg;
import com.saint.netty.params.Msg;
import com.saint.netty.util.AnnotionManagerUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object evt) throws Exception {
        Msg.NettyMsg msg = (Msg.NettyMsg)evt;

        ctx.writeAndFlush(Msg.NettyMsg.newBuilder()
                .setMsgId(msg.getMsgId())
                .setMsgType(MsgTypeEnum.ACK.getMsgType()).build());

        AbstractProcessMsg processor = AnnotionManagerUtil.getProcessorByMsgType(msg.getMsgType());
        if(processor!=null){
            processor.process(msg, ctx);
            return;
        }
        MsgReturn<String> msgReturn = MsgReturn.renderFailure(CodeStatusEnum.PARAMS_ERROR);
        msgReturn.setData("传入的msgType="+msg.getMsgType()+"， 没有对应的处理器");

        ctx.writeAndFlush(Msg.NettyMsg.newBuilder()
                .setMsgId(msg.getMsgId())
                .setMsgType(MsgTypeEnum.RESPONSE_MSG_TYPE.getMsgType())
                .setContent(JSONObject.toJSONString(msgReturn)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
