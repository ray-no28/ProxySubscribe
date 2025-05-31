package priv.ray.fetcher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import priv.ray.fetcher.fetchers.IFetchers;
import priv.ray.fetcher.fetchers.impl.FetcherA;
import priv.ray.fetcher.fetchers.impl.FetcherB;
import priv.ray.fetcher.fetchers.impl.FetcherC;

@Configuration
public class FetcherConfig {

    /**
     * 注册 FetcherA
     *
     * @return FetcherA 实例
     */
    @Bean
    public IFetchers fetcherA() {
        return new FetcherA();
    }
    @Bean
    public IFetchers fetcherB() {
        return new FetcherB();
    }
    @Bean
    public IFetchers fetcherC() {
        return new FetcherC();
    }

    // 如果有其他 Fetcher，可以在这里继续注册
    // 例如：
    // @Bean
    // public IFetchers fetcherB() {
    //     return new FetcherB();
    // }
}