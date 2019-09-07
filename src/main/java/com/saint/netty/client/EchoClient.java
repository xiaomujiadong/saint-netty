package com.saint.netty.client;

import com.saint.netty.params.Msg;
import com.saint.netty.params.Msg.NettyMsg;
import com.saint.netty.params.RespMsg;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.util.CharsetUtil;
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
        int userId = 111;
        int toUserId = 222;
        Msg.NettyMsg msg = Msg.NettyMsg.newBuilder().setMsgId(1).setUserId(userId).setContent("测试").setToUserId(toUserId).build();
        NettyClient client = new NettyClient();
        client.connect(url, port);
        channel = client.getChannel();
        channel.writeAndFlush(msg);
//        System.out.println(Unpooled.copiedBuffer(msg.toByteArray()).readableBytes());
//        while (scan.hasNext()){
//            if(channel==null){
//                client.connect(url, port);
//                channel = client.getChannel();
//                channel.writeAndFlush(Unpooled.copiedBuffer(msg.toByteArray()));
//            }
//
//            String str1 = scan.next();
//            System.out.println(channel.isActive()+"输入的数据为：" + str1);
//            Msg.NettyMsg msg2 = NettyMsg.newBuilder().setMsgId(1).setUserId(userId).setContent(str1).setToUserId(toUserId).build();
//            channel.writeAndFlush(msg2);
//            channel.flush();
//        }
//        scan.close();
        channel.closeFuture().sync();
    }
}
@Data
class NettyClient{

    private Channel channel;

    public void connect(String url, int port) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(url, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("decoder", new ProtobufDecoder(Msg.NettyMsg.getDefaultInstance()));
                            socketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            socketChannel.pipeline().addLast("encoder", new ProtobufEncoder());
                            socketChannel.pipeline().addLast(new EchoClientHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            this.channel = channelFuture.channel();
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
