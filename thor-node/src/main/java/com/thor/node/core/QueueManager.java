package com.thor.node.core;

import com.thor.node.core.model.TaskNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class QueueManager {
    private static final Logger log = LoggerFactory.getLogger(QueueManager.class);

    private final BlockingQueue<TaskNode> processQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<TaskNode> transferQueue = new LinkedBlockingQueue<>();

    public void pushToTransferQueue(TaskNode task) {
        if (task != null) {
            transferQueue.offer(task);
            log.debug("任务 [{}] 已进入传输队列", task.getTaskId());
        }
    }

    public TaskNode takeFromTransferQueue() throws InterruptedException {
        return transferQueue.take();
    }

    public void pushToProcessQueue(TaskNode task) {
        if (task != null) {
            processQueue.offer(task);
            log.debug("任务 [{}] 已进入处理队列", task.getTaskId());
        }
    }

    public TaskNode takeFromProcessQueue() throws InterruptedException {
        return processQueue.take();
    }
}