package priv.ray.fetcher.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import priv.ray.fetcher.service.ProxyService;

import java.util.List;

/**
 * @author: Ray
 * @date: 2025/3/19 23:26
 * @desc:
 * @changes:
 */
@RestController
public class ProxyController {

    private final ProxyService proxyService;

    @Autowired
    public ProxyController(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    /**
     * 获取全部代理
     *
     * @return 代理字符串列表
     */
    @GetMapping("/proxies")
    public String getAllProxies() {
        return String.join("\n",proxyService.getAllProxies());
    }

    /**
     * 随机获取指定数量的代理
     *
     * @param count 随机获取的数量
     * @return 随机代理字符串列表
     */
    @GetMapping("/proxies/random")
    public String getRandomProxies(@RequestParam(defaultValue = "15") int count) {
        return String.join("\n", proxyService.getRandomProxies(count));
    }

    /**
     * 随机获取一个代理
     *
     * @return 随机代理字符串
     */
    @GetMapping("/proxy/random")
    public String getRandomProxy() {
        return proxyService.getRandomProxy();
    }
}