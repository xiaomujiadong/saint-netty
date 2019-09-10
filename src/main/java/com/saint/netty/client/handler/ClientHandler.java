package com.saint.netty.client.handler;

import com.alibaba.fastjson.JSONObject;
import com.saint.netty.constant.SaintNettyConstant;
import com.saint.netty.entity.ChatEntityInfo;
import com.saint.netty.params.Msg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object evt) throws Exception {
        Msg.NettyMsg msg = (Msg.NettyMsg)evt;

        if(msg.getMsgType()== SaintNettyConstant.ACK){
            System.out.println("客户端收到的ack为： "+msg);
        }else if(msg.getMsgType()== SaintNettyConstant.CHAT_MSG_TYPE) {
            ChatEntityInfo chatEntityInfo = JSONObject
                    .parseObject(msg.getContent(), ChatEntityInfo.class);
            System.out.println(chatEntityInfo.getUserId() + ": " + chatEntityInfo.getContent());
        }else if(msg.getMsgType()==SaintNettyConstant.RESPONSE_MSG_TYPE){
            System.out.println("处理后返回结果为： "+msg.getContent());
        }else{
            System.out.println("无法解析"+msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) throws Exception {
        System.out.println("--------exception");
        cause.printStackTrace();
        channelHandlerContext.close();
    }
}
