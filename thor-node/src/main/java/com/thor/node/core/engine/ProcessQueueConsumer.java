package com.thor.node.core.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thor.node.core.QueueManager;
import com.thor.node.core.model.TaskNode;
import com.thor.node.core.process.IconvProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ProcessQueueConsumer {
    private static final Logger log = LoggerFactory.getLogger(ProcessQueueConsumer.class);

    @Autowired private QueueManager queueManager;
    @Autowired private IconvProcessor iconvProcessor;

    @PostConstruct
    public void startConsumer() {
        for (int i = 0; i < 4; i++) {
            new Thread(this::runConsume, "Thor-ProcessWorker-" + i).start();
        }
        log.info("Thor 业务处理消费者线程池已激活，并行度: 4");
    }

    private void runConsume() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TaskNode task = queueManager.takeFromProcessQueue();
                if (task == null) continue;

                log.info(">>> [调度介入] 任务 ID: {} 进入本地数据加工车间", task.getTaskId());

                JsonNode cfg = task.getProcCfg();
                String srcFile = cfg.path("src_file_name").asText();
                String processType = cfg.path("process_type").asText("none");

                boolean success = true;

                if ("iconv".equals(processType)) {
                    String from = cfg.path("from_charset").asText("GBK");
                    String to = cfg.path("to_charset").asText("UTF-8");
                    String destFile = srcFile + ".utf8";
                    success = iconvProcessor.convertEncoding(srcFile, destFile, from, to);
                    if (success && cfg instanceof ObjectNode) {
                        ((ObjectNode) cfg).put("src_file_name", destFile);
                    }
                }

                if (success) {
                    queueManager.pushToTransferQueue(task);
                } else {
                    log.error(">>> 任务 {} 数据处理失败", task.getTaskId());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("业务处理异常", e);
            }
        }
    }
}