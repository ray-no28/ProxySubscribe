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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FetcherB implements IFetchers {
    public LoggerUtil logger;

    private String url = "https://vpnjichang.github.io/";
    private int timeout = 10000;

    @Override
    public List<Proxy> requestProxies() {
        List<String> subSites = getSubSites();
        System.err.println(subSites);
        List<String> allSub = getAllSub(subSites);
        System.err.println(allSub);
        List<Proxy> proxies = parseSubscription(allSub);

        return proxies;
    }

    public List<Proxy> parseSubscription(List<String> subUrl) {
        List<Proxy> proxies = new ArrayList<>();
        for (String url : subUrl) {
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

    private List<String> getAllSub(List<String> subSites) {
        List<String> result = new ArrayList<>();
        String nodePattern = "^"+url+".+"+".txt$";
        Pattern pattern = Pattern.compile(nodePattern);

        for (String site : subSites) {
            try {
                Document doc = Jsoup.connect(site).timeout(timeout).get();
                Elements ps = doc.select("p");
                for (Element p : ps) {
                    if (pattern.matcher(p.text()).matches()) {
                        result.add(p.text());
                    }
                }
            } catch (IOException e) {
                logger.logErrorUrl(site, e);
            }
        }
        return result;
    }

    private List<String> getSubSites() {
        try {
            Document doc = Jsoup.connect(url).timeout(timeout).get();
            return doc.select(".xcblog-blog-url")
                    .stream()
                    .limit(2)
                    .map(e -> {
                        String href = e.attr("href");
                        if (href.startsWith("/")) {
                            return url + href.substring(1);
                        } else {
                            return url + href;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.logErrorUrl(url, e);
            return new ArrayList<>();
        }
    }
}