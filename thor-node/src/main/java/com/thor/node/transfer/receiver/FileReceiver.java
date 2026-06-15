package com.thor.node.transfer.receiver;

import com.thor.node.transfer.cache.FileChannelCache;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FileReceiver {
    private static final Logger log = LoggerFactory.getLogger(FileReceiver.class);

    @Value("${thor.node.received-home}")
    private String receivedHome;

    private final Map<String, TransferContext> contextMap = new ConcurrentHashMap<>();

    public void initTransfer(String taskId, String fileName, long totalSize) {
        TransferContext ctx = new TransferContext();
        ctx.taskId = taskId;
        ctx.fileName = fileName;
        ctx.totalSize = totalSize;
        ctx.savePath = receivedHome + "/" + fileName;
        contextMap.put(taskId, ctx);

        if (totalSize == 0) {
            try {
                FileChannelCache.getWriteChannel(taskId, ctx.savePath);
                FileChannelCache.closeAndRemove(taskId);
                FileStateManager.clearState(taskId);
                contextMap.remove(taskId);
                generateIndFile(ctx.savePath, ctx.fileName, totalSize);
            } catch (Exception e) {
                log.error("处理0字节文件异常", e);
            }
        }
    }

    private void generateIndFile(String datFilePath, String fileName, long totalSize) {
        try {
            String indFilePath = datFilePath.substring(0, datFilePath.lastIndexOf('.')) + ".ind";
            try (PrintWriter writer = new PrintWriter(new FileWriter(indFilePath))) {
                writer.println("UTF-8");
                writer.println(fileName + " 1 " + totalSize);
            }
            log.info(">>> [系统协同] .ind 校验文件已生成: {}", indFilePath);
        } catch (Exception e) {
            log.error("生成 .ind 校验文件失败", e);
        }
    }

    public void processIncomingBlock(Channel channel, String taskId, byte[] payload) {
        TransferContext ctx = contextMap.get(taskId);
        if (ctx == null) return;

        try {
            FileChannel fileChannel = FileChannelCache.getWriteChannel(taskId, ctx.savePath);
            long offset = FileStateManager.getOffset(taskId);
            fileChannel.write(ByteBuffer.wrap(payload), offset);

            long newOffset = offset + payload.length;
            FileStateManager.saveOffset(taskId, newOffset);

            if (newOffset >= ctx.totalSize) {
                FileChannelCache.closeAndRemove(taskId);
                FileStateManager.clearState(taskId);
                contextMap.remove(taskId);
                generateIndFile(ctx.savePath, ctx.fileName, ctx.totalSize);
            }
        } catch (Exception e) {
            log.error("写入磁盘失败: {}", taskId, e);
        }
    }

    private static class TransferContext {
        String taskId, fileName, savePath;
        long totalSize;
    }
}