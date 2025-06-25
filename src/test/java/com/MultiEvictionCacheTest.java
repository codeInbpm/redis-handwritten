package com;

import com.redis.cache.MultiEvictionCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

public class MultiEvictionCacheTest {
    private MultiEvictionCache cache;
    private File aofFile = new File("appendonly.aof");

    @Before
    public void setUp() {
        if (aofFile.exists()) aofFile.delete();
    }

    @After
    public void tearDown() {
        if (cache != null) cache.shutdown();
        if (aofFile.exists()) aofFile.delete();
    }

    @Test
    public void testLfuEviction() throws InterruptedException {
        cache = new MultiEvictionCache(3, MultiEvictionCache.EvictionStrategy.LFU);
        cache.put("key1", "value1", 5000);
        cache.put("key2", "value2", 5000);
        cache.put("key3", "value3", 5000);
        for (int i = 0; i < 5; i++) cache.get("key1"); // key1 高频
        cache.put("key4", "value4", 5000); // 移除低频 key2
        assertNull(cache.get("key2"));
        assertEquals("value1", cache.get("key1"));
    }

    @Test
    public void testClockEviction() {
        cache = new MultiEvictionCache(3, MultiEvictionCache.EvictionStrategy.CLOCK);
        cache.put("key1", "value1", 5000);
        cache.put("key2", "value2", 5000);
        cache.put("key3", "value3", 5000);
        cache.get("key1"); // referenceBit = 1
        cache.put("key4", "value4", 5000); // 移除 referenceBit = 0 的键
        assertTrue(cache.size() <= 3);
    }

    @Test
    public void testFifoEviction() {
        cache = new MultiEvictionCache(3, MultiEvictionCache.EvictionStrategy.FIFO);
        cache.put("key1", "value1", 5000);
        cache.put("key2", "value2", 5000);
        cache.put("key3", "value3", 5000);
        cache.put("key4", "value4", 5000); // 移除 key1（最早插入）
        assertNull(cache.get("key1"));
        assertEquals("value4", cache.get("key4"));
    }

    @Test
    public void testTtlAndAof() throws InterruptedException {
        cache = new MultiEvictionCache(3, MultiEvictionCache.EvictionStrategy.LFU);
        cache.put("key1", "value1", 1000);
        Thread.sleep(1500);
        assertNull(cache.get("key1")); // 过期
        cache.put("key2", "value2", 5000);
        cache = new MultiEvictionCache(3, MultiEvictionCache.EvictionStrategy.LFU);
        assertEquals("value2", cache.get("key2")); // AOF 恢复
    }
}