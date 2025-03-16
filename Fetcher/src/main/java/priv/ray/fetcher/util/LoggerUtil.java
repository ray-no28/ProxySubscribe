package priv.ray.fetcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import priv.ray.fetcher.model.Proxy;

/**
 * @author: Ray
 * @date: 2025/3/17 0:30
 * @desc: 代理日志工具类（注入方式）
 */
@Component // 关键：声明为 Spring Bean
public class LoggerUtil {
    private final static Logger logger = LoggerFactory.getLogger("ProxyLogger");


    // 记录代理信息
    public static void logProxy(Proxy proxy) {
        if (logger.isInfoEnabled()) {
            logger.info(proxy.toString());
        }
    }

    // 记录代理检查结果
    public static void logProxyCheck(Proxy proxy, boolean check) {
        if (logger.isInfoEnabled()) {
            logger.info(proxy.toString() + (check ? ": check pass" : ": not pass"));
        }
    }

    // 记录代理转化异常
    public static void logErrorProxy(String proxy, Throwable e) {
        logger.error(proxy + ": 转化异常，请检查数据", e);
    }

    // 记录网络请求异常
    public static void logErrorUrl(String url, Throwable e) {
        logger.error(url + ": 网络异常，请检查地址", e);
    }

    public static void logError(String s) {
        logger.error(s);
    }
}