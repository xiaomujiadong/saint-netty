package com.saint.netty.handler.process;

import com.alibaba.fastjson.JSONObject;
import com.saint.netty.annotation.MsgType;
import com.saint.netty.constant.MsgTypeEnum;
import com.saint.netty.entity.ChatEntityInfo;
import com.saint.netty.entity.MsgReturn;
import com.saint.netty.params.Msg;
import com.saint.netty.util.ConnectionUtil;
import io.netty.channel.ChannelHandlerContext;

/**msgType = 1,==>MsgTypeEnum.CONNECTION_MSG_TYPE
 * @description:
 * @date: 2019/9/9 17:51
 */

@MsgType(msgType = 1)
public class ConnectionHandler extends AbstractProcessMsg {

    @Override
    public void process(Msg.NettyMsg msg, ChannelHandlerContext ctx) {
        ChatEntityInfo chatEntityInfo = JSONObject.parseObject(msg.getContent(), ChatEntityInfo.class);
        ConnectionUtil.addChannel(chatEntityInfo.getUserId(), ctx.channel());
        MsgReturn<String> msgReturn = MsgReturn.renderSuccess();
        msgReturn.setData("连接建立成功");

        ctx.writeAndFlush(Msg.NettyMsg.newBuilder()
                .setMsgId(msg.getMsgId())
                .setMsgType(MsgTypeEnum.RESPONSE_MSG_TYPE.getMsgType())
                .setContent(JSONObject.toJSONString(msgReturn)));
    }
}
