package com;

import com.redis.cache.ExpiryCache;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExpiryCacheTest {
    @Test
    public void testExpiryCache() throws InterruptedException {
        ExpiryCache cache = new ExpiryCache(3);

        // 测试 TTL 过期
        cache.put("key1", "value1", 1000); // 1秒后过期
        cache.put("key2", "value2", 5000); // 5秒后过期
        assertEquals("value1", cache.get("key1"));
        Thread.sleep(1500); // 等待 1.5秒
        assertNull(cache.get("key1")); // key1 已过期
        assertEquals("value2", cache.get("key2"));

        // 测试容量限制和随机淘汰
        cache.put("key3", "value3", 5000);
        cache.put("key4", "value4", 1000); // 触发淘汰
        assertTrue(cache.size() <= 3);

        // 测试定期删除
        cache.put("key5", "value5", 1000);
        Thread.sleep(1500); // 等待定期任务触发
        cache.removeExpiredKeys(); // 手动触发清理
        assertNull(cache.get("key5"));

        // 测试无效输入
        try {
            cache.put("key6", "value6", 0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // 预期异常
        }

        // 清理资源
        cache.shutdown();
    }
}