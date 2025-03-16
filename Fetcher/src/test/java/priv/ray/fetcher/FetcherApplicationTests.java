package priv.ray.fetcher;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import priv.ray.fetcher.fetchers.impl.FetcherA;
import priv.ray.fetcher.model.Proxy;
import priv.ray.fetcher.util.LoggerUtil;

@SpringBootTest
class FetcherApplicationTests {

    @Test
    void contextLoads() {
    }
    @Autowired
    LoggerUtil logger;

    @Test
    public void test1(){
        Proxy proxy = new Proxy();
        proxy.setHost("11.22.33.44");
        logger.logProxy(proxy);
    }
    @Test
    public void test2(){
        FetcherA fetcherA = new FetcherA();
        fetcherA.requestProxies();
    }

}
