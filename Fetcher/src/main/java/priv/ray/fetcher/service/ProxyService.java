package priv.ray.fetcher.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.ray.fetcher.util.RedisUtil;

import java.util.List;

/**
 * @author: Ray
 * @date: 2025/3/20 0:10
 * @desc:
 * @changes:
 */
@Service
public class ProxyService {

    private final RedisUtil redisUtil;

    @Autowired
    public ProxyService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /**
     * 获取全部代理字符串
     *
     * @return 代理字符串列表
     */
    public List<String> getAllProxies() {
        return redisUtil.getStringProxies("proxies");
    }

    /**
     * 随机获取指定数量的代理字符串
     *
     * @param count 随机获取的数量
     * @return 随机代理字符串列表
     */
    public List<String> getRandomProxies(int count) {
        return redisUtil.getRandomStringProxies("proxies", count);
    }

    /**
     * 随机获取一个代理字符串
     *
     * @return 随机代理字符串
     */
    public String getRandomProxy() {
        return redisUtil.getRandomStringProxy("proxies");
    }

}