package com.redis.cache;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiryCache extends SimpleCache {
    private Map<String, Long> expiryTimes; // 存储键的过期时间戳
    private ScheduledExecutorService scheduler; // 定时清理任务

    public ExpiryCache(int capacity) {
        super(capacity);
        this.expiryTimes = new HashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        // 每10秒清理一次过期键
        scheduler.scheduleAtFixedRate(this::removeExpiredKeys, 10, 10, TimeUnit.SECONDS);
    }

    // 重载 put 方法，支持 TTL
    public void put(String key, String value, long ttlMillis) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key or value cannot be null");
        }
        if (ttlMillis <= 0) {
            throw new IllegalArgumentException("TTL must be positive");
        }

        // 检查缓存是否已满，尝试移除过期键
        if (getCache().size() >= super.size()) {
            removeRandomExpiredKey();
        }

        super.put(key, value);
        expiryTimes.put(key, System.currentTimeMillis() + ttlMillis);
    }

    // 重写 get 方法，实现惰性删除
    @Override
    public String get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        Long expiry = expiryTimes.get(key);
        if (expiry != null && System.currentTimeMillis() > expiry) {
            super.remove(key);
            expiryTimes.remove(key);
            return null;
        }
        return super.get(key);
    }

    // 定期清理过期键
    public void removeExpiredKeys() {
        expiryTimes.entrySet().removeIf(entry -> {
            if (System.currentTimeMillis() > entry.getValue()) {
                super.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    // 随机移除一个过期键
    private void removeRandomExpiredKey() {
        for (String key : expiryTimes.keySet()) {
            Long expiry = expiryTimes.get(key);
            if (expiry != null && System.currentTimeMillis() > expiry) {
                super.remove(key);
                expiryTimes.remove(key);
                return;
            }
        }
        // 若无过期键，调用父类的移除逻辑
        if (getCache().size() >= super.size()) {
            String firstKey = getCache().keySet().iterator().next();
            super.remove(firstKey);
            expiryTimes.remove(firstKey);
            System.out.println("No expired keys, removed key: " + firstKey);
        }
    }

    // 关闭定时任务（清理资源）
    public void shutdown() {
        scheduler.shutdown();
    }
}