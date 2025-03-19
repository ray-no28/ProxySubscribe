package priv.ray.fetcher.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import priv.ray.fetcher.fetchers.IFetchers;
import priv.ray.fetcher.model.Proxy;
import priv.ray.fetcher.util.RedisUtil;

import java.util.ArrayList;
import java.util.List;

@Service
public class AutoFetcher {

    private final List<IFetchers> fetchers; // 自动注入所有 IFetchers 实例
    private final RedisUtil redisUtil;

    @Autowired
    public AutoFetcher(List<IFetchers> fetchers, RedisUtil redisUtil) {
        this.fetchers = fetchers;
        this.redisUtil = redisUtil;
    }

    /**
     * 自动运行所有注册的 Fetcher，并将代理存储到 Redis 中
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 每 5 分钟运行一次
    public void run() {
        List<Proxy> allProxies = new ArrayList<>();

        // 运行所有 Fetcher
        for (IFetchers fetcher : fetchers) {
            List<Proxy> proxies = fetcher.requestProxies();
            allProxies.addAll(proxies);
        }

        // 将代理存储到 Redis 中
        if (!allProxies.isEmpty()) {
            redisUtil.saveProxies("proxies", allProxies, 3600); // 设置过期时间为 1 小时
        }
    }

    /**
     * 从 Redis 中获取代理列表
     *
     * @return 代理列表
     */
    public List<Proxy> getProxiesFromRedis() {
        return redisUtil.getProxies("proxies");
    }

    /**
     * 清空 Redis 中的代理数据
     */
    public void clearProxiesFromRedis() {
        redisUtil.deleteKey("proxies");
    }
}