package com.saint.netty.handler;

import com.saint.netty.util.ConnectionUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class UserConnectionHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object evt) throws Exception {
        ByteBuf in = (ByteBuf)evt;
        String value = in.toString(CharsetUtil.UTF_8);
        System.out.println("UserConnectionHandler receiver: "+value);
        Channel channel = ctx.channel();
        String userId = value.split("#")[0];
        if(value.split("#").length==2){
            ConnectionUtil.addChannel(userId, channel);
        }
//        channelIdUserId.put(channel.id().asLongText(), userId);
        String otherUserId = "";
//        if(ConnectionUtil.getChannelMap().keySet().size()==2){
//            for(String user:ConnectionUtil.getChannelMap().keySet()){
//                if(user.equals(userId)){
//                    continue;
//                }
//                otherUserId = user;
//                break;
//            }
//        }
        if(StringUtils.isNotBlank(userId)){
            if(userId.equals("1")){
                otherUserId = "2";
            }else{
                otherUserId = "1";
            }
        }
        if(StringUtils.isNotBlank(otherUserId)){
            Channel otherChannel = ConnectionUtil.getChannel(otherUserId);
            if(value.split("#").length==2){
                otherChannel.writeAndFlush(Unpooled.copiedBuffer(value.split("#")[1], CharsetUtil.UTF_8));
            }
        }
        ctx.write(in);
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
