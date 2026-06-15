package com.thor.node.core.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;

@Component
public class IconvProcessor {
    private static final Logger log = LoggerFactory.getLogger(IconvProcessor.class);

    public boolean convertEncoding(String sourcePath, String destPath, String fromCharset, String toCharset) {
        log.info(">>> [数据清洗] 启动 Iconv 转码: [{}] -> [{}], 目标: {}", fromCharset, toCharset, sourcePath);
        File srcFile = new File(sourcePath);
        File destFile = new File(destPath);

        if (!srcFile.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile), Charset.forName(fromCharset)));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile), Charset.forName(toCharset)))) {

            String line;
            long lineCount = 0;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (!isFirstLine) writer.write("\n");
                writer.write(line);
                isFirstLine = false;
                lineCount++;
            }
            log.info(">>> [数据清洗] 转码完成，共处理 {} 行", lineCount);
            return true;
        } catch (Exception e) {
            log.error(">>> Iconv 转码异常", e);
            return false;
        }
    }
}