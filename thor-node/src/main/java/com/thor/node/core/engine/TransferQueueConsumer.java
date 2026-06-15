package com.thor.node.core.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.thor.node.core.QueueManager;
import com.thor.node.core.model.TaskNode;
import com.thor.node.transfer.sender.ZeroCopySender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class TransferQueueConsumer {
    private static final Logger log = LoggerFactory.getLogger(TransferQueueConsumer.class);

    @Autowired private QueueManager queueManager;
    @Autowired private ZeroCopySender zeroCopySender;

    @PostConstruct
    public void startConsumer() {
        for (int i = 0; i < 8; i++) {
            new Thread(this::runConsume, "Thor-TransferWorker-" + i).start();
        }
        log.info("Thor 传输消费者线程池已激活，并行度: 8");
    }

    private void runConsume() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TaskNode task = queueManager.takeFromTransferQueue();
                if (task == null) continue;

                JsonNode cfg = task.getProcCfg();
                String ip = cfg.path("dst_address").asText();
                int port = cfg.path("dst_port").asInt();
                String srcPath = cfg.path("src_file_name").asText();

                String logicalFileName = srcPath;
                if (srcPath.endsWith(".utf8")) {
                    logicalFileName = srcPath.substring(0, srcPath.length() - 5);
                }
                logicalFileName = new java.io.File(logicalFileName).getName();

                zeroCopySender.sendFile(ip, port, task.getTaskId(), srcPath, logicalFileName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("传输异常", e);
            }
        }
    }
}