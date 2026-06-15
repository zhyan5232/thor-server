package com.thor.node.core.scanner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thor.common.entity.ThorNode;
import com.thor.common.entity.ThorTaskCfg;
import com.thor.common.entity.ThorTaskInstance;
import com.thor.common.mapper.ThorNodeMapper;
import com.thor.common.mapper.ThorTaskCfgMapper;
import com.thor.common.mapper.ThorTaskInstanceMapper;
import com.thor.node.core.QueueManager;
import com.thor.node.core.model.TaskNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.*;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ThorDirScanner {
    private static final Logger log = LoggerFactory.getLogger(ThorDirScanner.class);

    @Value("${thor.node.data-home}")
    private String dataHome;

    @Autowired private QueueManager queueManager;
    @Autowired private ThorTaskCfgMapper taskCfgMapper;
    @Autowired private ThorNodeMapper nodeMapper;
    @Autowired private ThorTaskInstanceMapper instanceMapper;

    private final ObjectMapper mapper = new ObjectMapper();
    private final java.util.Map<String, Long> processedFiles = new ConcurrentHashMap<>();

    private final ExecutorService workerPool = new ThreadPoolExecutor(
            5, 20, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(50000),
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "Thor-ScannerWorker-" + count.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy());

    @PostConstruct
    public void init() {
        File dir = new File(dataHome);
        if (!dir.exists() && dir.mkdirs()) {
            log.info("已自动创建数据监控目录: {}", dataHome);
        }
        Thread watcherThread = new Thread(this::startWatchService, "Thor-OS-Watcher");
        watcherThread.setDaemon(true);
        watcherThread.start();
        log.info(">>> [引信激活] Thor 自动发现引擎已挂载，异步并发处理池已就绪");
    }

    private void startWatchService() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path path = Paths.get(dataHome);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                if (key == null) continue;

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        log.warn(">>> [系统警告] OS文件事件队列溢出！触发全量扫盘补偿");
                        workerPool.submit(this::triggerFullCompensationScan);
                        continue;
                    }
                    Path changed = (Path) event.context();
                    String fileName = changed.toString();

                    if (fileName.endsWith(".ind")) {
                        File indFile = path.resolve(fileName).toFile();
                        workerPool.submit(() -> handleIndFileReady(indFile));
                    }
                }
                if (!key.reset()) break;
            }
        } catch (Exception e) {
            log.error("目录监听引擎发生致命异常", e);
        }
    }

    private void handleIndFileReady(File indFile) {
        try {
            Thread.sleep(500);
            if (indFile.length() == 0) return;

            String fileKey = indFile.getAbsolutePath();
            Long lastProcessTime = processedFiles.get(fileKey);
            if (lastProcessTime != null && (System.currentTimeMillis() - lastProcessTime < 3000)) return;
            processedFiles.put(fileKey, System.currentTimeMillis());

            String datFileName = indFile.getName().substring(0, indFile.getName().lastIndexOf('.')) + ".dat";
            File datFile = new File(indFile.getParentFile(), datFileName);

            if (!datFile.exists() || !validateIndFile(indFile, datFile)) return;

            ThorTaskCfg routeCfg = taskCfgMapper.selectOne(new QueryWrapper<ThorTaskCfg>()
                    .eq("file_pattern", datFileName).eq("is_active", 1).last("LIMIT 1"));
            if (routeCfg == null) return;

            ThorNode targetNode = nodeMapper.selectOne(new QueryWrapper<ThorNode>()
                    .eq("node_name", routeCfg.getDstNode()).eq("status", "ONLINE").last("LIMIT 1"));
            if (targetNode == null) return;

            String taskId = "TASK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            ThorTaskInstance instance = new ThorTaskInstance();
            instance.setTaskId(taskId);
            instance.setCfgId(routeCfg.getId());
            instance.setFileName(datFileName);
            instance.setTotalSize(datFile.length());
            instance.setStatus("PROCESSING");
            instance.setStartTime(new Date());
            instanceMapper.insert(instance);

            ObjectNode cfg = mapper.createObjectNode();
            cfg.put("src_file_name", datFile.getAbsolutePath());
            cfg.put("dst_address", targetNode.getIpAddress());
            cfg.put("dst_port", targetNode.getPort());
            cfg.put("process_type", routeCfg.getProcessType());
            cfg.put("from_charset", routeCfg.getFromCharset());
            cfg.put("to_charset", routeCfg.getToCharset());

            TaskNode task = new TaskNode();
            task.setTaskId(taskId);
            task.setProcCfg(cfg);

            queueManager.pushToProcessQueue(task);
            log.info(">>> [中心调度] 任务 {} 已生成，目标节点: {}", taskId, targetNode.getNodeName());

        } catch (Exception e) {
            log.error("处理 .ind 校验文件异常: {}", indFile.getName(), e);
        }
    }

    private void triggerFullCompensationScan() {
        log.info(">>> 开始执行全量目录对账补偿...");
        File dir = new File(dataHome);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".ind"));
        if (files == null) return;
        for (File indFile : files) {
            workerPool.submit(() -> handleIndFileReady(indFile));
        }
    }

    private boolean validateIndFile(File indFile, File datFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(indFile))) {
            String encoding = br.readLine();
            String metaLine = br.readLine();
            if (encoding == null || metaLine == null) return false;
            String expectedFileName = metaLine.trim().split("\\s+")[0];
            return expectedFileName.equals(datFile.getName());
        } catch (Exception e) {
            return false;
        }
    }
}