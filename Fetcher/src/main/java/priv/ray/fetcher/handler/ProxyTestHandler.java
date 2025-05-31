package priv.ray.fetcher.handler;

import priv.ray.fetcher.model.Proxy;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ProxyTestHandler {

    /**
     * 测试代理节点的可用性
     *
     * @param proxy 代理节点
     * @return 是否可用
     */
    public static boolean checkProxy(Proxy proxy) {
        if (proxy.getProtocol() == null) {
            throw new IllegalArgumentException("Protocol cannot be null");
        }

        switch (proxy.getProtocol().toLowerCase()) {
            case "trojan":
                return checkTrojanProxy(proxy);
            default:
                throw new IllegalArgumentException("Unsupported protocol: " + proxy.getProtocol());
        }
    }

    /**
     * 测试 Trojan 代理
     */
    private static boolean checkTrojanProxy(Proxy proxy) {
        try {
            // 创建 SSL 上下文
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);

            // 创建 HttpClient
            HttpClient client = HttpClient.newBuilder()
                    .proxy(ProxySelector.of(new InetSocketAddress(proxy.getAddress(), proxy.getPort())))
                    .sslContext(sslContext) // 启用 TLS
                    .connectTimeout(Duration.ofSeconds(10)) // 设置超时时间
                    .build();

            // 测试目标
            String testUrl = "https://www.google.com/generate_204";

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(testUrl))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode() == 204; // 204 表示成功
            } catch (Exception e) {
                System.err.println("Proxy check failed for " + proxy.getAddress() + ":" + proxy.getPort() + " with URL " + testUrl + ": " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize SSL context: " + e.getMessage());
        }
        return false;
    }

    /**
     * 多线程测试代理节点
     *
     * @param proxies 代理节点列表
     * @param threadPoolSize 线程池大小
     * @return 可用代理节点列表
     */
    public static List<Proxy> checkProxiesConcurrently(List<Proxy> proxies, int threadPoolSize) {
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<Proxy>> futures = new ArrayList<>();
        List<Proxy> availableProxies = new ArrayList<>();

        // 提交任务
        for (Proxy proxy : proxies) {
            futures.add(executor.submit(() -> {
                if (checkProxy(proxy)) {
                    return proxy;
                }
                return null;
            }));
        }

        // 获取结果
        for (Future<Proxy> future : futures) {
            try {
                Proxy proxy = future.get();
                if (proxy != null) {
                    availableProxies.add(proxy);
                }
            } catch (Exception e) {
                System.err.println("Error while checking proxy: " + e.getMessage());
            }
        }

        // 关闭线程池
        executor.shutdown();
        return availableProxies;
    }
}