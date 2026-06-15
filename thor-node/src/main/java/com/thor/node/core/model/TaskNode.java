package com.thor.node.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.thor.common.enums.TaskStatus;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskNode {
    private String taskId;
    private TaskStatus status;
    private JsonNode procCfg;
    private final AtomicInteger tryTimes = new AtomicInteger(0);

    public TaskNode() {
        this.status = TaskStatus.WAITING;
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public JsonNode getProcCfg() { return procCfg; }
    public void setProcCfg(JsonNode procCfg) { this.procCfg = procCfg; }
    public int getTryTimes() { return tryTimes.get(); }
    public int incrementAndGetTryTimes() { return tryTimes.incrementAndGet(); }
    public boolean canRetry() { return tryTimes.get() < 3; }
}