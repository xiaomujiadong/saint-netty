package com.saint.netty.client;

import com.alibaba.fastjson.JSONObject;
import com.saint.netty.constant.MsgTypeEnum;
import com.saint.netty.entity.ChatEntityInfo;
import com.saint.netty.params.Msg;
import com.saint.netty.params.Msg.NettyMsg;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import java.net.InetSocketAddress;
import java.util.Scanner;
import lombok.Data;

public class EchoClient {

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
        Scanner scan = new Scanner(System.in);
        // next方式接收字符串
        System.out.println("next方式接收：");
//        int userId = 111;
//        int toUserId = 222;
        int userId = 222;
        int toUserId = 111;

        NettyClient client = new NettyClient();
        client.connect(url, port, userId);
        channel = client.getChannel();
        while (scan.hasNext()){
            String str1 = scan.next();
            System.out.println(channel.isActive()+"输入的数据为：" + str1);
            ChatEntityInfo chatEntityInfoTemp = ChatEntityInfo.builder().userId(userId).toUserId(toUserId).content(str1).build();
            Msg.NettyMsg msg2 = NettyMsg.newBuilder().setMsgId(1).setMsgType(MsgTypeEnum.CHAT_MSG_TYPE.getMsgType()).setContent(
                    JSONObject.toJSONString(chatEntityInfoTemp)).build();
            channel.writeAndFlush(msg2);
            channel.flush();
        }
        scan.close();
        channel.closeFuture().sync();
    }
}
@Data
class NettyClient{

    private Channel channel;

    public void connect(String url, int port, int userId) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .remoteAddress(new InetSocketAddress(url, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            socketChannel.pipeline().addLast("decoder", new ProtobufDecoder(Msg.NettyMsg.getDefaultInstance()));
                            socketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            socketChannel.pipeline().addLast("encoder", new ProtobufEncoder());
                            socketChannel.pipeline().addLast(new EchoClientHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            this.channel = channelFuture.channel();
            ChatEntityInfo chatEntityInfo = ChatEntityInfo.builder().userId(userId).build();
            Msg.NettyMsg msg = Msg.NettyMsg.newBuilder()
                    .setMsgId(1)
                    .setMsgType(MsgTypeEnum.CONNECTION_MSG_TYPE.getMsgType())
                    .setContent(JSONObject.toJSONString(chatEntityInfo))
                    .build();
            channelFuture.channel().writeAndFlush(msg);

//            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
//            try {
//                group.shutdownGracefully().sync();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    }
}
