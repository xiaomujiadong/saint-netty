package com.saint.netty.init;

import com.saint.netty.handler.ServerHandler;
import com.saint.netty.params.Msg;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.nio.channels.spi.SelectorProvider;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class TcpServerInit {

    private volatile boolean inited;

    /**
     *  netty server run
     */
    public void run() {
//        NioEventLoopGroup bossLoopGroup = new NioEventLoopGroup(nettyProp.getTcp().getBoss().getThreadNum(), getBossThreadFactory(),
        NioEventLoopGroup bossLoopGroup = new NioEventLoopGroup(1, getBossThreadFactory(),
            getSelectorProvider());
//        bossLoopGroup.setIoRatio(nettyProp.getTcp().getBoss().getIoRatio());
//        NioEventLoopGroup workerLoopGroup = new NioEventLoopGroup(nettyProp.getTcp().getWorker().getThreadNum(), getWorkThreadFactory(),
        NioEventLoopGroup workerLoopGroup = new NioEventLoopGroup(0, getWorkThreadFactory(),
            getSelectorProvider());
//        workerLoopGroup.setIoRatio(nettyProp.getTcp().getWorker().getIoRatio());
        try {
            /*
             * ServerBootstrap 是一个启动NIO服务的辅助启动类
             * 你可以在这个服务中直接使用Channel
             */
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            /*
             * 这一步是必须的，如果没有设置group将会报java.lang.IllegalStateException: group not set异常
             */
            serverBootstrap.group(bossLoopGroup, workerLoopGroup);

            /*
             * ServerSocketChannel以NIO的selector为基础进行实现的，用来接收新的连接
             * 这里告诉Channel如何获取新的连接.
             */
            serverBootstrap.channelFactory(NioServerSocketChannel::new);
            /*
             * 这里的事件处理类经常会被用来处理一个最近的已经接收的Channel。
             * ChannelInitializer是一个特殊的处理类，
             * 他的目的是帮助使用者配置一个新的Channel。
             * 也许你想通过增加一些处理类比如NettyServerHandler来配置一个新的Channel
             * 或者其对应的ChannelPipeline来实现你的网络程序。
             * 当你的程序变的复杂时，可能你会增加更多的处理类到pipeline上，
             * 然后提取这些匿名类到最顶层的类上。
             */
            serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
                /**
                 * 每连上一个链接调用一次
                 */
                @Override
                public void initChannel(Channel ch) throws Exception {
                    initPipeline(ch.pipeline());
                }
            });
            /*
             * 在Netty 4中实现了一个新的ByteBuf内存池，它是一个纯Java版本的 jemalloc （Facebook也在用）。
             * 现在，Netty不会再因为用零填充缓冲区而浪费内存带宽了。不过，由于它不依赖于GC，开发人员需要小心内存泄漏。
             * 如果忘记在处理程序中释放缓冲区，那么内存使用率会无限地增长。
             * Netty默认不使用内存池，需要在创建客户端或者服务端的时候进行指定
             */
//            serverBootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
//            serverBootstrap.option(ChannelOption.SO_BACKLOG, nettyProp.getTcp().getSo().getBacklog());
//            serverBootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
//            serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
//            if (nettyProp.getTcp().getSendBuf().getConnectServer() > 0) {
//                serverBootstrap.option(ChannelOption.SO_SNDBUF, nettyProp.getTcp().getSendBuf().getConnectServer());
//            }
            /*
             * TCP层面的接收和发送缓冲区大小设置，
             * 在Netty中分别对应ChannelOption的SO_SNDBUF和SO_RCVBUF，
             * 需要根据推送消息的大小，合理设置，对于海量长连接，通常32K是个不错的选择。
             */
//            if (nettyProp.getTcp().getRcvBuf().getConnectServer() > 0) {
//                serverBootstrap.option(ChannelOption.SO_RCVBUF, nettyProp.getTcp().getRcvBuf().getConnectServer());
//            }

            /*
             * 这个坑其实也不算坑，只是因为懒，该做的事情没做。一般来讲我们的业务如果比较小的时候我们用同步处理，等业务到一定规模的时候，一个优化手段就是异步化。
             * 异步化是提高吞吐量的一个很好的手段。但是，与异步相比，同步有天然的负反馈机制，也就是如果后端慢了，前面也会跟着慢起来，可以自动的调节。
             * 但是异步就不同了，异步就像决堤的大坝一样，洪水是畅通无阻。如果这个时候没有进行有效的限流措施就很容易把后端冲垮。
             * 如果一下子把后端冲垮倒也不是最坏的情况，就怕把后端冲的要死不活。
             * 这个时候，后端就会变得特别缓慢，如果这个时候前面的应用使用了一些无界的资源等，就有可能把自己弄死。
             * 那么现在要介绍的这个坑就是关于Netty里的ChannelOutboundBuffer这个东西的。
             * 这个buffer是用在netty向channel write数据的时候，有个buffer缓冲，这样可以提高网络的吞吐量(每个channel有一个这样的buffer)。
             * 初始大小是32(32个元素，不是指字节)，但是如果超过32就会翻倍，一直增长。
             * 大部分时候是没有什么问题的，但是在碰到对端非常慢(对端慢指的是对端处理TCP包的速度变慢，比如对端负载特别高的时候就有可能是这个情况)的时候就有问题了，
             * 这个时候如果还是不断地写数据，这个buffer就会不断地增长，最后就有可能出问题了(我们的情况是开始吃swap，最后进程被linux killer干掉了)。
             * 为什么说这个地方是坑呢，因为大部分时候我们往一个channel写数据会判断channel是否active，但是往往忽略了这种慢的情况。
             *
             * 那这个问题怎么解决呢？其实ChannelOutboundBuffer虽然无界，但是可以给它配置一个高水位线和低水位线，
             * 当buffer的大小超过高水位线的时候对应channel的isWritable就会变成false，
             * 当buffer的大小低于低水位线的时候，isWritable就会变成true。所以应用应该判断isWritable，如果是false就不要再写数据了。
             * 高水位线和低水位线是字节数，默认高水位是64K，低水位是32K，我们可以根据我们的应用需要支持多少连接数和系统资源进行合理规划。
             */
//            serverBootstrap.childOption(
//                    ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
//                nettyProp.getTcp().getWriteBuf().getConnectServerLow(), nettyProp.getTcp().getWriteBuf().getConnectServerHigh()
//            ));
            //netty启动
//            logger.debug(DebugLogEnum.APPLICATION_INIT_LOG.code(), false, "netty tcp 启动成功;绑定端口:" + nettyProp.getTcp().getPort());
            ChannelFuture future = serverBootstrap.bind(8081).sync();
            inited = true;
            future.channel().closeFuture().sync();
            future.channel().closeFuture().addListener(
                (ChannelFutureListener) future1 -> {
                    bossLoopGroup.shutdownGracefully();
                    workerLoopGroup.shutdownGracefully();
                });

        } catch (Exception e) {
//            logger.error(AppLogEnum.NETTY_INTERNAL_ERROR.code(), "tcp启动是吧", e);
        }
    }

    /**
     * netty 默认的Executor为ThreadPerTaskExecutor
     * 线程池的使用在SingleThreadEventExecutor#doStartThread
     * <p>
     * eventLoop.execute(runnable);
     * 是比较重要的一个方法。在没有启动真正线程时，
     * 它会启动线程并将待执行任务放入执行队列里面。
     * 启动真正线程(startThread())会判断是否该线程已经启动，
     * 如果已经启动则会直接跳过，达到线程复用的目的
     *
     * @return java.util.concurrent.ThreadFactory
     */
    private ThreadFactory getBossThreadFactory() {
        return new DefaultThreadFactory("boss");
    }

    /**
     * Work线程池
     * @return java.util.concurrent.ThreadFactory
     */
    private ThreadFactory getWorkThreadFactory() {
        return new DefaultThreadFactory("worker");
    }

    /**
     *  IO多路复用,如果是linux机器,并且其内核版本大于2.6,创建的就是EPollSelectorProvider,否则的话,就创建PollSelectorProvider.
     * @return java.nio.channels.spi.SelectorProvider
     */
    private SelectorProvider getSelectorProvider() {
        return SelectorProvider.provider();
    }

    /**
     * 初始化pipeline
     * @param pipeline
     * @return void
     */
    private void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new ProtobufVarint32FrameDecoder());
        pipeline.addLast("decoder", new ProtobufDecoder(Msg.NettyMsg.getDefaultInstance()));
        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast("encoder", new ProtobufEncoder());
        pipeline.addLast("userHandler", new ServerHandler());
    }

    @Data
    public static class NettyProp{
        private Tcp tcp;

        private Kcp kcp;

        public Map<String, Integer> fixedChannel;

        private Connection connection;

        private Packet packet;

        private Decode decode;

        private boolean isDev;

        private Integer retryTimes;

        private Integer heartbeatOutTime;

        private Integer msgOutTime;

        @Data
        public static class Kcp {

            public Integer port;
            public Integer conv;
            public Integer kcpRetryNum;
            public Integer kcpRetryTimeMs;
        }

        @Data
        public static class Tcp {

            public Integer port;
            private Boss boss;
            private Worker worker;
            private So so;
            private SendBuf sendBuf;
            private RcvBuf rcvBuf;
            private WriteBuf writeBuf;

        }

        @Data
        public static class Boss {

            private Integer threadNum;
            private Integer ioRatio;
        }

        @Data
        public static class Worker {

            private Integer threadNum;

            private Integer ioRatio;
        }

        @Data
        public static class So {

            private boolean keepAlive;
            private Integer backlog;
        }

        @Data
        public static class SendBuf {

            private Integer connectServer;
        }

        @Data
        public static class RcvBuf {

            private Integer connectServer;

        }

        @Data
        public static class WriteBuf {

            private Integer connectServerLow;
            private Integer connectServerHigh;
        }

        @Data
        public static class Connection {

            private Integer minHeartbeat;
            private Integer maxHeartbeat;
            private Integer maxTimeoutTimes;
        }

        @Data
        public static class Packet {

            private Integer headerLen;
            private Integer maxPacketSize;
            private Integer sessionLen;
        }

        @Data
        public static class Decode {

            private String delimiter;
        }
    }

}


