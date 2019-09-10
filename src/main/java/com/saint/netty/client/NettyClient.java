package com.saint.netty.client;

import com.alibaba.fastjson.JSONObject;
import com.saint.netty.client.handler.ClientHandler;
import com.saint.netty.constant.SaintNettyConstant;
import com.saint.netty.entity.ChatEntityInfo;
import com.saint.netty.params.Msg;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import java.net.InetSocketAddress;
import lombok.Data;

/**
 * @description:
 * @date: 2019/9/10 14:10
 */
@Data
public class NettyClient{

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
                            socketChannel.pipeline().addLast(new ClientHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            this.channel = channelFuture.channel();
            ChatEntityInfo chatEntityInfo = ChatEntityInfo.builder().userId(userId).build();
            Msg.NettyMsg msg = Msg.NettyMsg.newBuilder()
                    .setMsgId(1)
                    .setMsgType(SaintNettyConstant.CONNECTION_MSG_TYPE)
                    .setContent(JSONObject.toJSONString(chatEntityInfo))
                    .build();
            channelFuture.channel().writeAndFlush(msg);
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