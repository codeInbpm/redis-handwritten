package com.redis.cache;
public class CustomHashMap<K, V> {
    private static class Entry<K, V> {
        K key;
        V value;
        int hash;
        Entry<K, V> next;
        Entry(K key, V value, int hash) {
            this.key = key;
            this.value = value;
            this.hash = hash;
        }
    }

    private Entry<K, V>[] table;
    private int size;
    private int capacity;
    private final float loadFactor;
    private static final int INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    @SuppressWarnings("unchecked")
    public CustomHashMap() {
        this.capacity = INITIAL_CAPACITY;
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.table = new Entry[INITIAL_CAPACITY];
    }

    public V get(K key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        int hash = hash(key);
        int index = hash & (capacity - 1);
        for (Entry<K, V> entry = table[index]; entry != null; entry = entry.next) {
            if (entry.hash == hash && (key.equals(entry.key))) {
                return entry.value;
            }
        }
        return null;
    }

    public void put(K key, V value) {
        if (key == null || value == null) throw new IllegalArgumentException("Key or value cannot be null");
        int hash = hash(key);
        int index = hash & (capacity - 1);

        for (Entry<K, V> entry = table[index]; entry != null; entry = entry.next) {
            if (entry.hash == hash && key.equals(entry.key)) {
                entry.value = value;
                return;
            }
        }

        if (size >= capacity * loadFactor) {
            resize();
            index = hash & (capacity - 1);
        }

        Entry<K, V> newEntry = new Entry<>(key, value, hash);
        newEntry.next = table[index];
        table[index] = newEntry;
        size++;
    }

    public V remove(K key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        int hash = hash(key);
        int index = hash & (capacity - 1);
        Entry<K, V> prev = null;
        for (Entry<K, V> entry = table[index]; entry != null; entry = entry.next) {
            if (entry.hash == hash && key.equals(entry.key)) {
                if (prev == null) {
                    table[index] = entry.next;
                } else {
                    prev.next = entry.next;
                }
                size--;
                return entry.value;
            }
            prev = entry;
        }
        return null;
    }

    public int size() {
        return size;
    }

    private int hash(K key) {
        int h = key.hashCode();
        return (h ^ (h >>> 16)) & 0x7fffffff; // 优化分散性，确保正数
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        try {
            Entry<K, V>[] oldTable = table;
            capacity *= 2;
            table = new Entry[capacity];
            size = 0;
            for (Entry<K, V> entry : oldTable) {
                while (entry != null) {
                    put(entry.key, entry.value);
                    entry = entry.next;
                }
            }
        } catch (OutOfMemoryError e) {
            throw new RuntimeException("Failed to resize hashmap due to insufficient memory", e);
        }
    }
}