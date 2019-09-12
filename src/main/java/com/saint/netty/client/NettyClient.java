package com.saint.netty.client;

import com.alibaba.fastjson.JSONObject;
import com.saint.netty.client.handler.ClientHandler;
import com.saint.netty.constant.SaintNettyConstant;
import com.saint.netty.entity.ChatEntityInfo;
import com.saint.netty.params.Msg;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
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
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import java.net.InetSocketAddress;
import lombok.Data;

/**
 * @description:
 * @date: 2019/9/10 14:10
 */
@Data
public class NettyClient{

    private Channel channel;

    protected final HashedWheelTimer timer = new HashedWheelTimer();

    public void connect(String url, int port, int userId) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .remoteAddress(new InetSocketAddress(url, port));

            final ConnectionWatchdog connectionWatchdog = new ConnectionWatchdog(bootstrap, timer, url, port, true) {
                @Override
                public ChannelHandler[] handlers() {
                    return new ChannelHandler[]{
                            new IdleStateHandler(0, 4, 0),
                            this,
                            new ProtobufVarint32FrameDecoder(),
                            new ProtobufDecoder(Msg.NettyMsg.getDefaultInstance()),
                            new ProtobufVarint32LengthFieldPrepender(),
                            new ProtobufEncoder(),
                            new ClientHandler()
                    };
                }
            };

            ChannelFuture channelFuture;

            synchronized (bootstrap){
                bootstrap.handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(connectionWatchdog.handlers());
                    }
                });
                channelFuture = bootstrap.connect().sync();
                this.channel = channelFuture.channel();
                ChatEntityInfo chatEntityInfo = ChatEntityInfo.builder().userId(userId).build();
                Msg.NettyMsg msg = Msg.NettyMsg.newBuilder()
                        .setMsgId(1)
                        .setMsgType(SaintNettyConstant.CONNECTION_MSG_TYPE)
                        .setContent(JSONObject.toJSONString(chatEntityInfo))
                        .build();
                channelFuture.channel().writeAndFlush(msg);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}