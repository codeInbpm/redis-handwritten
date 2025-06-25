# Java 从零手写 Redis

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-8%2B-blue)](https://www.oracle.com/java/)
[![Build Status](https://img.shields.io/badge/Build-Passing-green)](https://github.com/yourname/redis-handwritten/actions)

**一个从零开始用 Java 实现 Redis 核心功能的教学项目，通过 8 步打造高效键值缓存系统，助你深入理解 Redis 底层原理，轻松应对大厂面试！**

本项目配套 [**“Java 从零手写 Redis”博客系列**](#博客系列)，通过逐步实现 Redis 的核心功能（基础缓存、键过期、AOF 持久化、LRU 淘汰等），帮助开发者学习数据结构、算法和系统设计。代码简洁、可运行，包含全面的 JUnit 测试，适合初学者、进阶者和面试准备者。

## 项目背景
Redis 是一个高性能的内存键值数据库，广泛应用于缓存、会话管理和实时分析。本项目通过 Java 手写 Redis 的核心功能，深入剖析其底层实现，包括：
- 固定大小缓存
- 键过期机制（TTL）
- AOF 持久化
- LRU 缓存淘汰策略
- 其他淘汰策略（LFU、CLOCK、FIFO）
- 哈希表优化（HashMap、rehash）
- 代码重构与扩展

项目灵感来源于 [Juejin 博客系列](https://juejin.cn/post/7518707715130654720)，我们对其进行了优化：
- 将原 18 篇精简为 8 篇，逻辑更紧凑。
- 提供详细实现步骤、图表和面试要点。
- 增强代码健壮性，添加全面测试用例。

## 项目特点
- **教学导向**：每篇博客对应一个功能模块，循序渐进，适合系统学习。
- **可运行代码**：所有代码均可直接运行，附带 JUnit 测试用例。
- **面试准备**：涵盖大厂高频考题（如 LRU 实现、AOF vs RDB），提供针对性答案。
- **图文并茂**：UML 图、流程图、性能图表帮助理解复杂概念。
- **生产级健壮性**：异常处理、资源清理、代码注释完善。

## 目录结构
```plaintext
redis-handwritten/
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── SimpleCache.java       # 基础缓存（第1篇）
│   │       ├── ExpiryCache.java       # 键过期（第2篇）
│   │       ├── AofCache.java          # AOF 持久化（第3篇）
│   │       ├── LruCache.java          # LRU 淘汰（第4篇）
│   │       └── ...                    # 后续功能
│   ├── test/
│   │   └── java/
│   │       ├── SimpleCacheTest.java   # 测试用例
│   │       ├── ExpiryCacheTest.java
│   │       ├── AofCacheTest.java
│   │       ├── LruCacheTest.java
│   │       └── ...
├── pom.xml                                # Maven 配置文件
├── appendonly.aof                         # AOF 日志文件（运行时生成）
├── README.md                              # 项目说明
└── docs/                                  # 博客文档和图表
    ├── images/                            # UML、流程图等
    └── tutorials/                         # 博客 Markdown 文件
```



快速开始
环境要求
Java: 8 或以上

Maven: 3.6+

IDE: IntelliJ IDEA、Eclipse 或 VS Code（推荐）

Git: 用于克隆仓库


安装步骤
克隆仓库：
bash

git clone https://github.com/yourname/redis-handwritten.git
cd redis-handwritten

导入项目：
使用 IDE 打开项目根目录。

确保 Maven 自动导入依赖（pom.xml）。

运行测试：
bash

mvn test

或在 IDE 中运行 src/test/java 下的测试用例（如 SimpleCacheTest.java）。

查看 AOF 文件：
运行 AofCacheTest 或 LruCacheTest 后，检查 appendonly.aof 文件，验证持久化功能。

示例代码
实现一个固定大小缓存（第1篇）：
```java
SimpleCache cache = new SimpleCache(3);
cache.put("key1", "value1");
cache.put("key2", "value2");
cache.put("key3", "value3");
System.out.println(cache.get("key1")); // 输出: value1
cache.put("key4", "value4"); // 移除最早的键
System.out.println(cache.get("key2")); // 输出: null
```

博客系列
本项目配套 8 篇博客，每篇对应一个功能模块，详细讲解实现步骤、代码、图表和面试要点：
震惊！手写 Redis 第一步，打造超强缓存，面试官直呼过瘾！  
实现固定大小的键值缓存（SimpleCache）。

博客链接 (#) | 代码: SimpleCache.java (src/main/java/SimpleCache.java)

Redis 过期机制大揭秘！一招让键值自动消失，面试稳了！  
实现键过期（TTL）和随机淘汰（ExpiryCache）。

博客链接 (#) | 代码: ExpiryCache.java (src/main/java/ExpiryCache.java)

永不丢数据的黑科技！手写 Redis AOF 持久化，面试官直接跪了！  
实现 AOF 持久化和数据恢复（AofCache）。

博客链接 (#) | 代码: AofCache.java (src/main/java/AofCache.java)

**缓存淘汰黑科技！手写 LRU 算法...




