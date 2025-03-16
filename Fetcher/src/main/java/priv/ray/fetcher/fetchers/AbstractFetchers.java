package priv.ray.fetcher.fetchers;

import priv.ray.fetcher.model.Proxy;

import java.util.List;

/**
 * @author: Ray
 * @date: 2025/3/16 19:39
 * @desc:
 * @changes:
 */
public abstract class AbstractFetchers {
    private String url;

    protected abstract List<Proxy> requestProxies();

    public String getUrl() {
        return url;
    }
}
