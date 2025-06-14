package priv.ray.fetcher.fetchers.impl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import priv.ray.fetcher.fetchers.IFetchers;
import priv.ray.fetcher.handler.ProxyParseHandler;
import priv.ray.fetcher.model.Proxy;
import priv.ray.fetcher.util.LoggerUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Ray
 * @date: 2025/3/16 19:36
 * @desc:
 * @changes:
 */

public class FetcherA implements IFetchers {
    public LoggerUtil logger;

   private String url="https://freevpnshare.github.io/";
   private int timeout= 10000;

    @Override
    public List<Proxy> requestProxies() {
        List<String> subSites = getSubSites();
        System.out.println(subSites);
        List<String> allSub = getAllSub(subSites);
        System.out.println(allSub);
        List<Proxy> proxies = parseSubscription(allSub);

        return proxies;
    }

    public List<Proxy> parseSubscription(List<String> subUrl) {
        List<Proxy> proxies = new ArrayList<>();
        for (String url : subUrl
        ) {
            try {
                // 1. 访问订阅链接获取内容
                String content = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .timeout(timeout)
                        .execute()
                        .body()

                        ;

                // 2. 判断是否为Base64编码并解码
                String decodedContent = isBase64(content)
                        ? new String(Base64.getDecoder().decode(content))
                        : content;

                // 3. 按行分割解析代理
                for (String line : decodedContent.split("\\r?\\n")) {
                    if (line.trim().isEmpty()) continue;
                    try {
                        Proxy proxy = ProxyParseHandler.parseProxyLink(line.trim());
                        logger.logProxy(proxy);
                        proxies.add(proxy);
                    } catch (Exception e) {
                        logger.logErrorProxy(line.trim(), e);
                    }
                }
            } catch (IOException e) {
                logger.logErrorUrl(url, e);
            }

        }

        return proxies;
    }

    // 判断是否为Base64编码内容
    private boolean isBase64(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // 解析单行代理信息



    private List<String> getAllSub(List<String> subSites) {
        List<String> result = new ArrayList<>();
        for (String site : subSites) {
            try {
                // 使用 Jsoup 访问站点
                Document doc = Jsoup.connect(site).timeout(timeout).get();
                Elements ps = doc.select("p");
                for (Element p : ps) {
                    if (p.text().matches("^"+url+".+txt$" )) {
                        result.add(p.text());
                    }
                }
            } catch (IOException e) {
                // 处理异常（如站点无法访问）
                logger.logErrorUrl(site, e);
            }
        }
        return result;
    }

    private List<String> getSubSites() {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).timeout(timeout).get();
        } catch (IOException e) {
            logger.logErrorUrl(url, e);
            return new ArrayList<>();
        }
        return doc.select(".xcblog-blog-url")
                .stream()
                .limit(2)
                .map(e -> {
                    String href = e.attr("href");
                    // 处理相对路径（如 "/subsite" 或 "subsite"）
                    if (href.startsWith("/")) {
                        return url + href.substring(1);
                    } else {
                        return url + href;
                    }
                })
                .collect(Collectors.toList());
    }
}
