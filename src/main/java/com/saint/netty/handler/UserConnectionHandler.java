package com.saint.netty.handler;

import com.saint.netty.params.Msg;
import com.saint.netty.util.ConnectionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class UserConnectionHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object evt) throws Exception {
        Msg.NettyMsg msg = (Msg.NettyMsg)evt;
        System.out.println("UserConnectionHandler receiver: "+msg.toString());
        Channel channel = ctx.channel();
        ConnectionUtil.addChannel(msg.getUserId(), channel);
        Long toUserId = msg.getToUserId();

//        RespMsg.NettyRespMsg respMsg = RespMsg.NettyRespMsg.newBuilder().setCode(200).setMsg("消息接收成功").build();
        Msg.NettyMsg defaultMsg = Msg.NettyMsg.newBuilder().setMsgId(1).setUserId(0).setToUserId(msg.getUserId()).setContent("消息接收成功").build();
        if(toUserId==null || toUserId.equals(0L)){
            ConnectionUtil.addChannel(msg.getUserId(), channel);
            defaultMsg.toBuilder().setContent("连接建立成功");
        }else{
            Channel toChannel = ConnectionUtil.getChannel(msg.getToUserId());
            if(toChannel==null){
                defaultMsg.toBuilder().setContent("消息接收成功，但是对方不在线");
            }else{
                toChannel.writeAndFlush(msg);
            }
        }
        ctx.writeAndFlush(defaultMsg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
