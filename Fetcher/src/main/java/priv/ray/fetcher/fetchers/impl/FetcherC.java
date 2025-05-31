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
import java.util.*;

public class FetcherC implements IFetchers {
    public LoggerUtil logger;

    private String mainUrl = "https://nodefree.org/";
    private int timeout = 10000;

    @Override
    public List<Proxy> requestProxies() {
        List<String> subSites = getSubSites();
        logger.logInfo(subSites.toString());
        List<String> allSub = getAllSub(subSites);
        logger.logInfo(allSub.toString());
        List<Proxy> proxies = parseSubscription(allSub);
        return proxies;
    }

    private List<String> getSubSites() {
        Set<String> subUrls = new HashSet<>();
        try {
            Document doc = Jsoup.connect(mainUrl).timeout(timeout).get();
            // 直接筛选完整的子站点链接（以 https://nodefree.org/p 开头）
            Elements links = doc.select("a[href^='https://nodefree.org/p']");
            for (Element link : links) {
                if (subUrls.size() >= 2) break; // 达到数量后终止循环
                String href = link.attr("href");
                subUrls.add(href);
            }
        } catch (IOException e) {
            logger.logErrorUrl(mainUrl, e);
        }
        return subUrls.stream().toList();
    }

    private List<String> getAllSub(List<String> subSites) {
        List<String> result = new ArrayList<>();
        for (String site : subSites) {
            try {
                Document doc = Jsoup.connect(site).timeout(timeout).get();
                // 筛选以 https://nodefree 开头且以 .txt 结尾的完整链接
                Elements ps = doc.select("p");
                for (Element p : ps) {
                    String href = p.text();
                    if (href.startsWith("https://nodefree") && href.endsWith(".txt")) {
                        result.add(href);
                    }
                }
            } catch (IOException e) {
                logger.logErrorUrl(site, e);
            }
        }
        return result;
    }

    // 以下 parseSubscription 和 isBase64 方法与原代码一致
    private List<Proxy> parseSubscription(List<String> subUrls) {
        List<Proxy> proxies = new ArrayList<>();
        for (String url : subUrls) {
            try {
                String content = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .timeout(timeout)
                        .execute()
                        .body();

                String decodedContent = isBase64(content)
                        ? new String(Base64.getDecoder().decode(content))
                        : content;

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

    private boolean isBase64(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}