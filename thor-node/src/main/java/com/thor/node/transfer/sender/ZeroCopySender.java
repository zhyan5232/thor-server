package com.thor.node.transfer.sender;

import com.thor.common.entity.ThorTaskInstance;
import com.thor.common.mapper.ThorTaskInstanceMapper;
import com.thor.node.network.codec.ThorEncoder;
import com.thor.node.network.protocol.ThorMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

@Component
public class ZeroCopySender {
    private static final Logger log = LoggerFactory.getLogger(ZeroCopySender.class);
    private final EventLoopGroup group = new NioEventLoopGroup();

    @Autowired
    private ThorTaskInstanceMapper instanceMapper;

    public void sendFile(String ip, int port, String taskId, String filePath, String logicalFileName) {
        File file = new File(filePath);
        if (!file.exists()) {
            log.error("待发送文件不存在: {}", filePath);
            return;
        }

        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline().addLast(new ThorEncoder());
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        String json = String.format(
                                "{\"code\":\"thor.node.transfer_start\",\"task_id\":\"%s\",\"file_name\":\"%s\",\"file_size\":%d}",
                                taskId, logicalFileName, file.length());
                        ctx.write(new ThorMessage(ThorMessage.TYPE_JSON_CMD, json.getBytes(StandardCharsets.UTF_8)));

                        ThorMessage streamHeader = new ThorMessage(ThorMessage.TYPE_FILE_STREAM, new byte[0]);
                        streamHeader.setLength((int) file.length());
                        ctx.write(streamHeader);

                        RandomAccessFile raf = new RandomAccessFile(file, "r");
                        FileRegion region = new DefaultFileRegion(raf.getChannel(), 0, file.length());

                        ctx.writeAndFlush(region).addListener((ChannelFutureListener) future -> {
                            ThorTaskInstance updateInstance = new ThorTaskInstance();
                            updateInstance.setTaskId(taskId);
                            updateInstance.setEndTime(new java.util.Date());

                            if (future.isSuccess()) {
                                log.info(">>> 任务 [{}] 物理传输成功落地！", taskId);
                                updateInstance.setStatus("SUCCESS");
                            } else {
                                log.error(">>> 任务 [{}] 物理传输失败", taskId, future.cause());
                                updateInstance.setStatus("FAILED");
                                updateInstance.setErrorMsg(future.cause().getMessage());
                            }
                            instanceMapper.updateById(updateInstance);
                            raf.close();
                            ctx.close();

                            if (file.getName().endsWith(".utf8")) {
                                file.delete();
                            }
                        });
                    }
                });
            }
        });

        b.connect(ip, port).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) log.error("无法连接到目标节点 {}:{}", ip, port);
        });
    }
}