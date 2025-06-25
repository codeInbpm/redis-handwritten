package com;
import com.redis.cache.LruCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

import java.io.File;

public class LruCacheTest {
    private LruCache cache;
    private File aofFile = new File("appendonly.aof");


    @Before
    public void setUp() {
        if (aofFile.exists()) {
            aofFile.delete();
        }
        cache = new LruCache(3);
    }

    @After
    public void tearDown() {
        cache.shutdown();
        if (aofFile.exists()) {
            aofFile.delete();
        }
    }

    @Test
    public void testLruCache() throws InterruptedException {
        // 测试 LRU 淘汰
        cache.put("key1", "value1", 5000);
        cache.put("key2", "value2", 5000);
        cache.put("key3", "value3", 5000);
        cache.get("key1"); // key1 移到头部
        cache.put("key4", "value4", 5000); // 移除 key2（最久未用）
        assertNull(cache.get("key2"));
        assertEquals("value1", cache.get("key1"));
        assertEquals("value4", cache.get("key4"));

        // 测试访问频率优化
        cache.put("key5", "value5", 5000);
        for (int i = 0; i < 10; i++) {
            cache.get("key1"); // key1 高频访问
        }
        cache.put("key6", "value6", 5000); // 移除低频键
        assertNotNull(cache.get("key1")); // key1 保留

        // 测试 TTL 兼容
        cache.put("key7", "value7", 1000);
        Thread.sleep(1500);
        assertNull(cache.get("key7"));

        // 测试 AOF 恢复
        cache = new LruCache(3);
        assertEquals("value1", cache.get("key1"));
    }
}