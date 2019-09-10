package com.saint.netty.handler.process;

import com.alibaba.fastjson.JSONObject;
import com.saint.netty.annotation.MsgType;
import com.saint.netty.constant.SaintNettyConstant;
import com.saint.netty.entity.ChatEntityInfo;
import com.saint.netty.entity.MsgReturn;
import com.saint.netty.params.Msg;
import com.saint.netty.util.ConnectionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**msgType = 1,==>MsgTypeEnum.CHAT_MSG_TYPE
 * @description:
 * @date: 2019/9/9 20:05
 */
@MsgType(msgType = SaintNettyConstant.CHAT_MSG_TYPE)
public class ChatHandler extends AbstractProcessMsg  {

    @Override
    public void process(Msg.NettyMsg msg, ChannelHandlerContext ctx) {
        ChatEntityInfo chatEntityInfo = JSONObject.parseObject(msg.getContent(), ChatEntityInfo.class);
        Channel toChannel = ConnectionUtil.getChannel(chatEntityInfo.getToUserId());
        if(toChannel!=null){
            toChannel.writeAndFlush(msg);
            return;
        }

        MsgReturn<String> msgReturn = MsgReturn.renderSuccess();
        msgReturn.setData("消息接收成功，但是对方不在线");

        ctx.writeAndFlush(Msg.NettyMsg.newBuilder()
                .setMsgId(msg.getMsgId())
                .setMsgType(SaintNettyConstant.RESPONSE_MSG_TYPE)
                .setContent(JSONObject.toJSONString(msgReturn)));
    }
}
