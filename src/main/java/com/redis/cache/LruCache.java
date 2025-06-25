package com.redis.cache;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LruCache extends AofCache {
    private class Node {
        String key, value;
        long expiry; // 过期时间戳
        int accessCount; // 访问频率
        Node prev, next;
        Node(String key, String value, long expiry) {
            this.key = key;
            this.value = value;
            this.expiry = expiry;
            this.accessCount = 1;
        }
    }

    private Map<String, Node> cache;
    private Node head, tail;
    private int capacity;

    public LruCache(int capacity) {
        super(capacity);
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.head = new Node(null, null, 0); // 哨兵节点
        this.tail = new Node(null, null, 0);
        head.next = tail;
        tail.prev = head;
        loadAof(); // 恢复 AOF 数据
    }

    @Override
    public String get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        Node node = cache.get(key);
        if (node == null || System.currentTimeMillis() > node.expiry) {
            if (node != null) {
                removeNode(node);
                cache.remove(key);
            }
            return null;
        }
        node.accessCount++; // 增加访问频率
        moveToHead(node); // 移到链表头部
        return node.value;
    }

    @Override
    public void put(String key, String value, long ttlMillis) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key or value cannot be null");
        }
        if (ttlMillis <= 0) {
            throw new IllegalArgumentException("TTL must be positive");
        }

        Node node = cache.get(key);
        if (node != null) {
            node.value = value;
            node.expiry = System.currentTimeMillis() + ttlMillis;
            node.accessCount++;
            moveToHead(node);
        } else {
            if (cache.size() >= capacity) {
                removeLruNode(); // 移除最近最少使用的节点
            }
            node = new Node(key, value, System.currentTimeMillis() + ttlMillis);
            cache.put(key, node);
            addToHead(node);
        }
        appendToAof("PUT", key, value, ttlMillis); // 记录到 AOF
    }

    private void addToHead(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void moveToHead(Node node) {
        removeNode(node);
        addToHead(node);
    }

    private void removeLruNode() {
        // 优先移除过期键
        for (Node node = tail.prev; node != head; node = node.prev) {
            if (System.currentTimeMillis() > node.expiry) {
                removeNode(node);
                cache.remove(node.key);
                return;
            }
        }
        // 若无过期键，检查访问频率
        Node lru = tail.prev;
        if (lru.accessCount > 5) { // 阈值示例
            // 寻找低频节点
            for (Node node = tail.prev; node != head; node = node.prev) {
                if (node.accessCount <= 5) {
                    removeNode(node);
                    cache.remove(node.key);
                    return;
                }
            }
        }
        // 默认移除尾部节点
        removeNode(lru);
        cache.remove(lru.key);
    }

    @Override
    protected void loadAof() {
        super.loadAof(); // 调用父类加载 AOF
        // 重建链表顺序（按插入顺序近似 LRU）
        for (String key : cache.keySet()) {
            Node node = cache.get(key);
            addToHead(node);
        }
    }
}