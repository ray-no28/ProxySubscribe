package priv.ray.fetcher;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import priv.ray.fetcher.fetchers.impl.FetcherA;
import priv.ray.fetcher.handler.ProxyParseHandler;
import priv.ray.fetcher.model.Proxy;
import priv.ray.fetcher.util.LoggerUtil;

import java.util.List;

@SpringBootTest
class FetcherApplicationTests {

    @Test
    void contextLoads() {
    }
    @Autowired
    LoggerUtil logger;

    @Test
    public void test2(){
        FetcherA fetcherA = new FetcherA();
        List<Proxy> proxies = fetcherA.requestProxies();
        System.out.println(ProxyParseHandler.toProxyLink(proxies.get(0)));
    }

}
