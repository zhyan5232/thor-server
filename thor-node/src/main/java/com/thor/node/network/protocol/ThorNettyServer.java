package com.thor.node.network.protocol;

import com.thor.node.network.ThorServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Component
public class ThorNettyServer {

    private static final Logger log = LoggerFactory.getLogger(ThorNettyServer.class);

    @Value("${thor.server.port:5599}")
    private int port;

    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final ThorServerHandler thorServerHandler;

    @Autowired
    public ThorNettyServer(ThorServerHandler thorServerHandler) {
        this.thorServerHandler = thorServerHandler;
    }

    @PostConstruct
    public void start() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(60, 60, 60, TimeUnit.SECONDS));
                        pipeline.addLast(new com.thor.node.network.codec.ThorDecoder());
                        pipeline.addLast(new com.thor.node.network.codec.ThorEncoder());
                        pipeline.addLast(thorServerHandler);
                    }
                });

        ChannelFuture future = bootstrap.bind(port).sync();
        if (future.isSuccess()) {
            log.info("Thor Netty Engine started on port: {}", port);
        }
    }

    @PreDestroy
    public void stop() {
        log.info("Shutting down Thor Netty Engine...");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}