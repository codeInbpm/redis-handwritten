package com.redis.cache;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AofCache extends ExpiryCache {
    private File aofFile = new File("appendonly.aof");

    public AofCache(int capacity) {
        super(capacity);
        loadAof(); // 启动时加载 AOF 文件
    }

    // 重写 put 方法，追加日志
    @Override
    public void put(String key, String value, long ttlMillis) {
        super.put(key, value, ttlMillis);
        appendToAof("PUT", key, value, ttlMillis);
    }

    // 追加日志到 AOF 文件
    void appendToAof(String op, String key, String value, long ttl) {
        try (FileWriter writer = new FileWriter(aofFile, true);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            bufferedWriter.write(op + " " + key + " " + value + " " + ttl + "\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            System.err.println("Failed to append to AOF: " + e.getMessage());
        }
    }

    // 从 AOF 文件恢复数据
    void loadAof() {
        if (!aofFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(aofFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 4);
                if (parts.length == 4 && parts[0].equals("PUT")) {
                    String key = parts[1];
                    String value = parts[2];
                    long ttl = Long.parseLong(parts[3]);
                    // 只恢复未过期的键
                    if (System.currentTimeMillis() < ttl) {
                        super.put(key, value, ttl - System.currentTimeMillis());
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load AOF: " + e.getMessage());
        }
    }

    // 获取 AOF 文件大小（用于测试）
    public long getAofFileSize() {
        return aofFile.exists() ? aofFile.length() : 0;
    }
}