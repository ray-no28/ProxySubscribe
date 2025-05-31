package priv.ray.fetcher;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import priv.ray.fetcher.fetchers.impl.FetcherA;
import priv.ray.fetcher.fetchers.impl.FetcherB;
import priv.ray.fetcher.handler.ProxyParseHandler;
import priv.ray.fetcher.handler.ProxyTestHandler;
import priv.ray.fetcher.model.Proxy;
import priv.ray.fetcher.util.LoggerUtil;
import priv.ray.fetcher.util.RedisUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
class FetcherApplicationTests {

    @Test
    void contextLoads() {
    }
    @Autowired
    LoggerUtil logger;

    @Autowired
    RedisUtil redisUtil;

    @Test
    public void test2(){
        FetcherB fetcherA = new FetcherB();
        List<Proxy> proxies = fetcherA.requestProxies();
        System.out.println(ProxyParseHandler.toProxyLink(proxies.get(0)));
    }
//    @Test
//    public void test3() throws Exception {
//        String[] split = ("trojan://2e126825-ef17-4548-892f-4299785ba728@f10011.flyflylflyflysbs.sbs:12768?security=tls&sni=sg03.ckcloud.info&type=tcp&headerType=none#%E6%96%B0%E5%8A%A0%E5%9D%A1%2001%E3%80%90vip2%E3%80%91\n" +
//                "trojan://2e126825-ef17-4548-892f-4299785ba728@f10021.flyflylflyflysbs.sbs:12768?security=tls&sni=sg03.ckcloud.info&type=tcp&headerType=none#%E6%96%B0%E5%8A%A0%E5%9D%A1%2001%E3%80%90vip3%E3%80%91\n" +
//                "trojan://2e126825-ef17-4548-892f-4299785ba728@f10011.flyflylflyflysbs.sbs:16706?security=tls&sni=hk04.ckcloud.info&type=tcp&headerType=none#%E9%A6%99%E6%B8%AF03%E3%80%90vip2%E3%80%91\n" +
//                "trojan://2e126825-ef17-4548-892f-4299785ba728@f10021.flyflylflyflysbs.sbs:16706?security=tls&sni=hk04.ckcloud.info&type=tcp&headerType=none#%E9%A6%99%E6%B8%AF03%E3%80%90vip3%E3%80%91\n" +
//                "trojan://2e126825-ef17-4548-892f-4299785ba728@f10011.flyflylflyflysbs.sbs:34664?security=tls&sni=jp04.ckcloud.info&type=tcp&headerType=none#%E6%97%A5%E6%9C%AC01%E3%80%90vip2%20%E3%80%91\n" +
//                "trojan://2e126825-ef17-4548-892f-4299785ba728@f10021.flyflylflyflysbs.sbs:34664?security=tls&sni=jp04.ckcloud.info&type=tcp&headerType=none#%E6%97%A5%E6%9C%AC01%E3%80%90vip3%E3%80%91\n" +
//                "trojan://2e126825-ef17-4548-892f-4299785ba728@f10011.flyflylflyflysbs.sbs:39520?security=tls&sni=jp03.ckcloud.info&type=tcp&headerType=none#%E6%97%A5%E6%9C%AC02%E3%80%90vip2%E3%80%91\n" +
//                "trojan://2e126825-ef17-4548-892f-4299785ba728@f10011.flyflylflyflysbs.sbs:19949?security=tls&sni=tw01.ckcloud.info&type=tcp&headerType=none#%E5%8F%B0%E6%B9%BE%2001%E3%80%90vip2%E3%80%91\n" +
//                "trojan://2e126825-ef17-4548-892f-4299785ba728@f10021.flyflylflyflysbs.sbs:19949?security=tls&sni=tw01.ckcloud.info&type=tcp&headerType=none#%E5%8F%B0%E6%B9%BE%2001%E3%80%90vip3%E3%80%91\n" +
//                "trojan://2e126825-ef17-4548-892f-4299785ba728@f10011.flyflylflyflysbs.sbs:49148?security=tls&sni=tw02.ckcloud.info&type=tcp&headerType=none#%E5%8F%B0%E6%B9%BE%2002%E3%80%90vip2%E3%80%91\n" +
//                "trojan://2e126825-ef17-4548-892f-4299785ba728@f10021.flyflylflyflysbs.sbs:49148?security=tls&sni=tw02.ckcloud.info&type=tcp&headerType=none#%E5%8F%B0%E6%B9%BE%2002%E3%80%90vip3%E3%80%91\n").split("\n");
//        ArrayList<Proxy> proxies = new ArrayList<>();
//        for (String s : split) {
//            proxies.add( ProxyParseHandler.parseProxyLink(s));
//        }
//        System.out.println(ProxyTestHandler.checkProxiesConcurrently(proxies,20));
//    }

}
