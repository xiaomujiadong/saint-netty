package com.saint.netty.util;

import com.saint.netty.params.Msg;

public class Test {
    public static void main(String[] args){
        Msg.NettyMsg nettyMsg = Msg.NettyMsg.newBuilder().setMsgId(10).setUserId(123).setContent("hello").build();

        byte[] data = nettyMsg.toByteArray();
        try{
            Msg.NettyMsg nettyMsg1 = Msg.NettyMsg.parseFrom(data);
            System.out.println(nettyMsg1);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
