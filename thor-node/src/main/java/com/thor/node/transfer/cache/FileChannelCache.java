package com.thor.node.transfer.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileChannelCache {
    private static final Logger log = LoggerFactory.getLogger(FileChannelCache.class);
    private static final Map<String, FileChannel> cache = new ConcurrentHashMap<>();
    private static final Map<String, RandomAccessFile> rafCache = new ConcurrentHashMap<>();

    public static FileChannel getWriteChannel(String taskId, String fullPath) throws Exception {
        if (cache.containsKey(taskId)) return cache.get(taskId);

        File file = new File(fullPath);
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel channel = raf.getChannel();

        rafCache.put(taskId, raf);
        cache.put(taskId, channel);
        return channel;
    }

    public static void closeAndRemove(String taskId) {
        try {
            FileChannel channel = cache.remove(taskId);
            RandomAccessFile raf = rafCache.remove(taskId);
            if (channel != null) {
                channel.force(true);
                channel.close();
            }
            if (raf != null) raf.close();
        } catch (Exception e) {
            // ignore
        }
    }
}