package com.redis.cache;

import java.util.HashMap;
import java.util.Map;

public class SimpleCache {
    private Map<String, String> cache;
    private int capacity;

    public SimpleCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.cache = new HashMap<>();
        this.capacity = capacity;
    }

    public String get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        return cache.get(key);
    }

    public void put(String key, String value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key or value cannot be null");
        }
        if (cache.size() >= capacity) {
            String firstKey = cache.keySet().iterator().next();
            cache.remove(firstKey);
            System.out.println("Cache full, removed key: " + firstKey);
        }
        cache.put(key, value);
    }

    public int size() {
        return cache.size();
    }

    // 供子类访问的受保护方法
    protected Map<String, String> getCache() {
        return cache;
    }

    protected void remove(String key) {
        cache.remove(key);
    }
}