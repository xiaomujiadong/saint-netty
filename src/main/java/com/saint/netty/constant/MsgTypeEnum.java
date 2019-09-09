package com.saint.netty.constant;

/**
 * @description:
 * @date: 2019/9/9 20:07
 */
public enum MsgTypeEnum {
    CONNECTION_MSG_TYPE(1, "连接消息类型"),

    ACK(6, "ack"),

    CHAT_MSG_TYPE(100, "聊天消息类型"),

    RESPONSE_MSG_TYPE(1000, "返回消息类型"),
    ;

    private int msgType;
    private String desc;

    private MsgTypeEnum(int msgType, String desc) {
        this.msgType = msgType;
        this.desc = desc;
    }

    public int getMsgType() {
        return msgType;
    }

    public String getDesc() {
        return desc;
    }
}
