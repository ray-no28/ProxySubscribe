package priv.ray.fetcher.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import priv.ray.fetcher.handler.ProxyParseHandler;
import priv.ray.fetcher.model.Proxy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class RedisUtil {

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RedisUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 将代理列表存储到 Redis 中
     *
     * @param key     Redis 键
     * @param proxies 代理列表
     * @param timeout 过期时间（单位：秒）
     */
    public void saveProxies(String key, List<Proxy> proxies, long timeout) {
        // 清空旧数据
        redisTemplate.delete(key);

        // 将代理列表存储为 Redis 的 Hash
        for (Proxy proxy : proxies) {
            String proxyLink = ProxyParseHandler.toProxyLink(proxy); // 使用 toProxyLink 方法
            redisTemplate.opsForHash().put(key, proxy.getAddress(), proxyLink);
        }

        // 设置过期时间
        if (timeout > 0) {
            redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
        }
    }

    /**
     * 从 Redis 中获取代理列表
     *
     * @param key Redis 键
     * @return 代理列表
     */
    public List<Proxy> getProxies(String key) {
        Map<Object, Object> proxyMap = redisTemplate.opsForHash().entries(key);
        return proxyMap.values().stream()
                .map(value -> {
                    try {
                        return ProxyParseHandler.parseProxyLink((String) value); // 使用 parseProxyLink 方法
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse proxy link", e);
                    }
                })
                .collect(Collectors.toList());
    }
    /**
     * 从 Redis 中随机获取指定数量的代理字符串
     *
     * @param key    Redis 键
     * @param count  随机获取的数量
     * @return 随机代理字符串列表
     */
    public List<String> getRandomStringProxies(String key, int count) {
        List<String> allProxies = getStringProxies(key);
        if (allProxies.isEmpty()) {
            return allProxies;
        }
        // 随机打乱列表
        Collections.shuffle(allProxies);
        // 返回前 count 个
        return allProxies.stream().limit(count).collect(Collectors.toList());
    }

    /**
     * 从 Redis 中随机获取一个代理字符串
     *
     * @param key Redis 键
     * @return 随机代理字符串
     */
    public String getRandomStringProxy(String key) {
        List<String> allProxies = getStringProxies(key);
        if (allProxies.isEmpty()) {
            return null;
        }
        // 随机返回一个
        return allProxies.get(new Random().nextInt(allProxies.size()));
    }

    /**
     * 从 Redis 中获取全部代理字符串
     *
     * @param key Redis 键
     * @return 代理字符串列表
     */
    public List<String> getStringProxies(String key) {
        Map<Object, Object> proxyMap = redisTemplate.opsForHash().entries(key);
        return proxyMap.values().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    /**
     * 删除指定键
     *
     * @param key Redis 键
     */
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 检查键是否存在
     *
     * @param key Redis 键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置键的过期时间
     *
     * @param key     Redis 键
     * @param timeout 过期时间（单位：秒）
     */
    public void expireKey(String key, long timeout) {
        if (timeout > 0) {
            redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
        }
    }
}