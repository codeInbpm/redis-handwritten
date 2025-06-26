package com;
import com.redis.cache.CustomHashMap;
import com.redis.cache.HashMapCache;
import com.redis.cache.MultiEvictionCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

public class HashMapCacheTest {
    private HashMapCache cache;
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
    public void testCustomHashMapBasic() {
        CustomHashMap<String, String> map = new CustomHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
        assertEquals(2, map.size());
        map.put("key1", "newValue");
        assertEquals("newValue", map.get("key1"));
        map.remove("key2");
        assertNull(map.get("key2"));
        assertEquals(1, map.size());
    }

    @Test
    public void testCustomHashMapResize() {
        CustomHashMap<String, String> map = new CustomHashMap<>();
        for (int i = 0; i < 20; i++) {
            map.put("key" + i, "value" + i);
        }
        assertEquals(20, map.size());
        assertEquals("value10", map.get("key10"));
    }

    @Test
    public void testHashMapCache() throws InterruptedException {
        cache = new HashMapCache(3, MultiEvictionCache.EvictionStrategy.LFU);
        cache.put("key1", "value1", 5000);
        cache.put("key2", "value2", 5000);
        cache.put("key3", "value3", 5000);
        for (int i = 0; i < 5; i++) cache.get("key1");
        cache.put("key4", "value4", 5000); // 移除低频 key2
        assertNull(cache.get("key2"));
        assertEquals("value1", cache.get("key1"));

        cache.put("key5", "value5", 1000);
        Thread.sleep(1500);
        assertNull(cache.get("key5")); // 过期

        cache = new HashMapCache(3, MultiEvictionCache.EvictionStrategy.LFU);
        assertEquals("value1", cache.get("key1")); // AOF 恢复
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInput() {
        cache = new HashMapCache(3, MultiEvictionCache.EvictionStrategy.LFU);
        cache.put(null, "value", 1000);
    }
}