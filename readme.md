
# Proxy Subscription Service

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![SpringBoot](https://img.shields.io/badge/SpringBoot-3.4.3-blue)
![License](https://img.shields.io/badge/License-MIT-green)

> 一个基于 SpringBoot 的代理服务器管理平台，自动采集、验证并对外提供订阅接口。

## ✨ 核心功能

- **代理采集**
  - 多源爬取（HTTP/SOCKS5/TROJAN 等协议支持）
  - 智能去重与质量过滤
  - 定时自动更新（可配置间隔）

- **代理订阅**
  - RESTful API 实时获取
  - 订阅链接生成（支持 V2ay 等客户端）

- **健康管理**
  - 实时连通性测试
  - 自动剔除失效节点

## 🚀 部署

### 环境要求
- JDK 17+
- Redis 6+（用于缓存）

### 部署步骤
```bash
git clone https://github.com/ray-no28/ProxySubscribe.git
```

## 📡 API 接口

### 获取全部代理
```http
GET /proxies
```

### 随机获取多个代理
```http
GET /proxies/random?count={数量} #默认数量为15
```

### 随机获取单个代理
```http
GET /proxy/random
```

## 🔧 扩展代理源

### 实现流程
1. **实现拉取器**  
   创建新类实现 `IFetchers` 接口，在 `requestProxies()` 方法中编写自定义代理拉取逻辑

2. **注册到配置**  
   在 `FetcherConfig` 配置类中将新拉取器注册为 Spring Bean

3. **自动集成**  
   `AutoFetcher` 组件会自动检测并启动定时拉取任务

### 特性优势
- 开箱即用的自动化集成
- 可配置的拉取频率
- 不影响现有功能的无缝扩展
