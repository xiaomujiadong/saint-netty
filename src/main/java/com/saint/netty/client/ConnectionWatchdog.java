package com.saint.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 重连步骤
 * 1）客户端连接服务端
 *
 * 2）在客户端的的ChannelPipeline中加入一个比较特殊的IdleStateHandler，设置一下客户端的写空闲时间，例如5s
 *
 * 3）当客户端的所有ChannelHandler中4s内没有write事件，则会触发userEventTriggered方法（上文介绍过）
 *
 * 4）我们在客户端的userEventTriggered中对应的触发事件下发送一个心跳包给服务端，检测服务端是否还存活，防止服务端已经宕机，客户端还不知道
 *
 * 5）同样，服务端要对心跳包做出响应，其实给客户端最好的回复就是“不回复”，这样可以服务端的压力，假如有10w个空闲Idle的连接，那么服务端光发送心跳回复，则也是费事的事情，那么怎么才能告诉客户端它还活着呢，其实很简单，因为5s服务端都会收到来自客户端的心跳信息，那么如果10秒内收不到，服务端可以认为客户端挂了，可以close链路
 *
 * 6）加入服务端因为什么因素导致宕机的话，就会关闭所有的链路链接，所以作为客户端要做的事情就是短线重连
 *
 *
 *要写工业级的Netty心跳重连的代码，需要解决一下几个问题：
 *
 * 1）ChannelPipeline中的ChannelHandlers的维护，首次连接和重连都需要对ChannelHandlers进行管理
 *
 * 2）重连对象的管理，也就是bootstrap对象的管理
 *
 * 3）重连机制编写
 *
 * 重连检测狗，当发现当前的链路不稳定关闭之后，进行12次重连
 *
 * 继承ChannelInboundHandlerAdapter，为了加入链路中，这样才能感知到链路状态
 *
 * 实现TimerTask，实现12次重连逻辑
 *
 * 实现ChannelHandlerHolder，获取链路的channelHandle，便于重连
 *
 * @date: 2019/9/11 17:08
 */
@Sharable
public abstract class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask,
        ChannelHandlerHolder {

    public final static Logger logger = LoggerFactory.getLogger(ConnectionWatchdog.class);

    private final Bootstrap bootstrap;
    private final Timer timer;

    private final int port;
    private final String host;

    private volatile boolean reconnect = true;
    private int attempts;

    public ConnectionWatchdog(Bootstrap bootstrap, Timer timer, String host, int port, boolean reconnect){
        this.bootstrap = bootstrap;
        this.timer = timer;
        this.host = host;
        this.port = port;
        this.reconnect = reconnect;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        logger.info("链接激活");
        attempts = 0;
        ctx.fireChannelInactive();
    }

    /**
     * 重连，意味着，链路断了，所以这个地方作为重连入口
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
        logger.warn("链接关闭");
        if(reconnect){
            logger.debug("链接关闭，将进行重连");
            if(attempts<12){
                attempts++;
                //重连时间越来越长
                int timeout = 2<<attempts;
                timer.newTimeout(this, timeout, TimeUnit.MILLISECONDS);
            }
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        ChannelFuture channelFuture ;
        //bootstrap初始化好了，只需将handler填充就好了
        //todo 会有问题，如果这个时候大量的连接在同一个时刻需要重连的话，那么这个时候性能并发会不会低很多呢？
        synchronized (bootstrap){
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    channel.pipeline().addLast(handlers());
                }
            });
            channelFuture = bootstrap.connect(host, port);
        }
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                boolean success = channelFuture.isSuccess();
                //如果重连失败，则调用ChannelInactive方法，再次出发重连事件，一直尝试12次，如果失败则不再重连
                if(success){
                    logger.info("链接重连成功");
                }else{
                    logger.warn("链接重连失败");
                    channelFuture.channel().pipeline().fireChannelInactive();
                }
            }
        });
    }
}
