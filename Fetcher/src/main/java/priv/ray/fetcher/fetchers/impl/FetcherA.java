package priv.ray.fetcher.fetchers.impl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import priv.ray.fetcher.fetchers.AbstractFetchers;
import priv.ray.fetcher.model.Proxy;
import priv.ray.fetcher.util.LoggerUtil;

import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author: Ray
 * @date: 2025/3/16 19:36
 * @desc:
 * @changes:
 */

public class FetcherA extends AbstractFetchers {
    public LoggerUtil logger;

    private static final Pattern PROXY_URL_PATTERN = Pattern.compile(
            "https://freevpnshare\\.github\\.io/uploads/\\d{4}/\\d{2}/1-\\d+\\.txt"
    );

   private String url="https://freevpnshare.github.io/";

    @Override
    public List<Proxy> requestProxies() {
        List<String> subSites = getSubSites();
        logger.logError(subSites.toString());
        List<String> allSub = getAllSub(subSites);
        logger.logError(allSub.toString());
        List<Proxy> proxies = parseSubscription(allSub);
        System.err.println(proxies.size());
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
                        .execute()
                        .body();

                // 2. 判断是否为Base64编码并解码
                String decodedContent = isBase64(content)
                        ? new String(Base64.getDecoder().decode(content))
                        : content;

                // 3. 按行分割解析代理
                for (String line : decodedContent.split("\\r?\\n")) {
                    if (line.trim().isEmpty()) continue;
                    try {
                        Proxy proxy = parseProxyLine(line.trim());
                        logger.logProxy(proxy);
                        proxies.add(proxy);
                    } catch (Exception e) {
//                        logger.logErrorProxy(line.trim(), e);
                    }
                }
            } catch (IOException e) {
//                logger.logErrorUrl(url, e);
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
    private Proxy parseProxyLine(String line) {
        Proxy proxy = new Proxy();

        // 分割协议部分（如 ss://）
        String[] protocolSplit = line.split("://", 2);
        if (protocolSplit.length != 2) throw new IllegalArgumentException("协议格式错误");
        proxy.setProtocol(protocolSplit[0].toLowerCase());
        String rightPart = protocolSplit[1];

        // 提取备注并解码
        int hashIndex = rightPart.lastIndexOf('#');
        if (hashIndex != -1) {
            String encodedRemark = rightPart.substring(hashIndex + 1);
            proxy.setRemark(URLDecoder.decode(encodedRemark, StandardCharsets.UTF_8));
            rightPart = rightPart.substring(0, hashIndex);
        }

        // 分割认证信息@主机端口
        String[] authAndServer = rightPart.split("@", 2);
        if (authAndServer.length != 2) throw new IllegalArgumentException("缺少@分隔符");

        // 处理认证信息
        String authInfo = authAndServer[0];
        if ("ss".equals(proxy.getProtocol())) {
            authInfo = new String(Base64.getDecoder().decode(authInfo));
        }
        proxy.setPassword(authInfo);

        // 使用正则提取主机和端口
        String serverPart = authAndServer[1];
        Pattern hostPortPattern = Pattern.compile("^([^:]+):(\\d+)(?:[/?].*)?$");
        Matcher matcher = hostPortPattern.matcher(serverPart);
        if (matcher.find()) {
            proxy.setHost(matcher.group(1));
            proxy.setPort(Integer.parseInt(matcher.group(2)));
        } else {
            throw new IllegalArgumentException("主机/端口格式错误: " + serverPart);
        }

        return proxy;
    }


    private List<String> getAllSub(List<String> subSites) {
        List<String> result = new ArrayList<>();
        for (String site : subSites) {
            try {
                // 使用 Jsoup 访问站点
                Document doc = Jsoup.connect(site).get();
                Elements ps = doc.select("p");
                for (Element p : ps) {
                    if (PROXY_URL_PATTERN.matcher(p.text()).matches()) {
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
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            logger.logErrorUrl(url, e);
        }
        return doc.select(".xcblog-blog-url")
                .stream()
                .limit(3)
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
