package com;

import com.redis.cache.SimpleCache;
import org.testng.annotations.Test;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

public class SimpleCacheTest {
    @Test
    public void testSimpleCache() {
        SimpleCache cache = new SimpleCache(3); // 容量为 3

        // 测试 put 和 get
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        assertEquals("value1", cache.get("key1"));
        assertEquals("value2", cache.get("key2"));
        assertEquals("value3", cache.get("key3"));
        assertEquals(3, cache.size());

        // 测试容量限制
        cache.put("key4", "value4"); // 超出容量，移除 key1
        assertNull(cache.get("key1")); // key1 已被移除
        assertEquals("value4", cache.get("key4"));
        assertEquals(3, cache.size());

        // 测试空键值
        try {
            cache.put(null, "value");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // 预期异常
        }

        try {
            cache.get(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // 预期异常
        }

        // 测试无效容量
        try {
            new SimpleCache(0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // 预期异常
        }
    }
}