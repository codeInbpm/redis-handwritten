package com.redis.cache;

import java.io.*;
import java.util.*;

public class HashMapCache extends MultiEvictionCache {
    private static class Node {
        String key, value;
        long expiry;
        int accessCount; // LFU
        int referenceBit; // CLOCK
        long insertionOrder; // FIFO
        Node prev, next;
        Node(String key, String value, long expiry) {
            this.key = key;
            this.value = value;
            this.expiry = expiry;
            this.accessCount = 1;
            this.referenceBit = 1;
            this.insertionOrder = insertionCounter++;
        }
    }

    private CustomHashMap<String, Node> cache;
    private Node head, tail;
    private int capacity;
    private EvictionStrategy strategy;
    private TreeMap<Integer, List<Node>> freqMap;
    private Node clockHand;
    private static long insertionCounter;

    public HashMapCache(int capacity, EvictionStrategy strategy) {
        super(capacity, strategy);
        this.capacity = capacity;
        this.strategy = strategy;
        this.cache = new CustomHashMap<>();
        this.freqMap = new TreeMap<>();
        this.head = new Node(null, null, 0);
        this.tail = new Node(null, null, 0);
        head.next = tail;
        tail.prev = head;
        this.clockHand = head;
        loadAof();
    }

    @Override
    public String get(String key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        Node node = cache.get(key);
        if (node == null || System.currentTimeMillis() > node.expiry) {
            if (node != null) {
                removeNode(node);
                cache.remove(key);
                if (strategy == EvictionStrategy.LFU) updateFreqMap(node, -node.accessCount);
            }
            return null;
        }
        updateNodeAccess(node);
        return node.value;
    }

    @Override
    public void put(String key, String value, long ttlMillis) {
        if (key == null || value == null) throw new IllegalArgumentException("Key or value cannot be null");
        if (ttlMillis <= 0) throw new IllegalArgumentException("TTL must be positive");

        Node node = cache.get(key);
        if (node != null) {
            node.value = value;
            node.expiry = System.currentTimeMillis() + ttlMillis;
            updateNodeAccess(node);
        } else {
            if (cache.size() >= capacity) {
                removeEvictedNode();
            }
            node = new Node(key, value, System.currentTimeMillis() + ttlMillis);
            cache.put(key, node);
            addToHead(node);
            if (strategy == EvictionStrategy.LFU) {
                freqMap.computeIfAbsent(1, k -> new ArrayList<>()).add(node);
            }
        }
        appendToAof("PUT", key, value, ttlMillis);
    }

    private void updateNodeAccess(Node node) {
        if (strategy == EvictionStrategy.LFU) {
            updateFreqMap(node, -node.accessCount);
            node.accessCount++;
            updateFreqMap(node, node.accessCount);
            moveToHead(node);
        } else if (strategy == EvictionStrategy.CLOCK) {
            node.referenceBit = 1;
        }
    }

    private void removeEvictedNode() {
        if (strategy == EvictionStrategy.LFU) {
            Map.Entry<Integer, List<Node>> entry = freqMap.firstEntry();
            if (entry != null) {
                List<Node> nodes = entry.getValue();
                Node node = nodes.remove(nodes.size() - 1);
                if (nodes.isEmpty()) freqMap.remove(entry.getKey());
                removeNode(node);
                cache.remove(node.key);
            }
        } else if (strategy == EvictionStrategy.CLOCK) {
            while (true) {
                clockHand = clockHand.next == tail ? head.next : clockHand.next;
                if (clockHand == head) continue;
                if (System.currentTimeMillis() > clockHand.expiry) {
                    Node toRemove = clockHand;
                    clockHand = clockHand.prev;
                    removeNode(toRemove);
                    cache.remove(toRemove.key);
                    return;
                }
                if (clockHand.referenceBit == 0) {
                    removeNode(clockHand);
                    cache.remove(clockHand.key);
                    return;
                }
                clockHand.referenceBit = 0;
            }
        } else if (strategy == EvictionStrategy.FIFO) {
            Node toRemove = tail.prev;
            if (toRemove != head) {
                removeNode(toRemove);
                cache.remove(toRemove.key);
            }
        }
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

    private void updateFreqMap(Node node, int count) {
        if (strategy != EvictionStrategy.LFU) return;
        freqMap.computeIfAbsent(count, k -> new ArrayList<>()).remove(node);
        if (freqMap.get(count) != null && freqMap.get(count).isEmpty()) {
            freqMap.remove(count);
        }
    }
}