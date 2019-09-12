package com.saint.netty.client;

import com.alibaba.fastjson.JSONObject;
import com.saint.netty.constant.SaintNettyConstant;
import com.saint.netty.entity.ChatEntityInfo;
import com.saint.netty.params.Msg;
import com.saint.netty.params.Msg.NettyMsg;
import io.netty.channel.*;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoClient {

    private static final Logger logger = LoggerFactory.getLogger(EchoClient.class);

    private final String url;
    private final int port;

    public EchoClient(String url, int port){
        this.url = url;
        this.port = port;
    }

    private static Channel channel;

    public static void main(String[] args) throws Exception {
        int port = 8081;
        String url = "127.0.0.1";
//        int userId = 111;
//        int toUserId = 222;
        for(int i=1;i<10000;i++){
            int userId = i;
            int toUserId = i+10000;
            EchoClient echoClient = new EchoClient(url, port);
            echoClient.start(userId, toUserId);
            Thread thread = new Thread(){
                @Override
                public void run() {
                    EchoClient echoClient = new EchoClient(url, port);
                    try {
                        echoClient.start(userId, toUserId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();
        }

    }

    private void start(int userId, int toUserId) throws InterruptedException {
        NettyClient client = new NettyClient();
        client.connect(url, port, userId);
        channel = client.getChannel();
        Scanner scan = new Scanner(System.in);
        logger.info(userId+"开始连接");
        while (scan.hasNext()){
            String str1 = scan.next();
            System.out.println(channel.isActive()+"输入的数据为：" + str1);
            ChatEntityInfo chatEntityInfoTemp = ChatEntityInfo.builder().userId(userId).toUserId(toUserId).content(str1).build();
            Msg.NettyMsg msg2 = NettyMsg.newBuilder().setMsgId(1).setMsgType(SaintNettyConstant.CHAT_MSG_TYPE).setContent(
                    JSONObject.toJSONString(chatEntityInfoTemp)).build();
            channel.writeAndFlush(msg2);
            channel.flush();
        }
        scan.close();
        channel.closeFuture().sync();
    }
}

