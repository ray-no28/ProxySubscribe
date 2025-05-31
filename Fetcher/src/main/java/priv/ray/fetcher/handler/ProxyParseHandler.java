package priv.ray.fetcher.handler;

import com.google.gson.*;
import priv.ray.fetcher.model.Proxy;

import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Ray
 * @date: 2025/3/17 23:24
 * @desc:
 * @changes:
 */
public class ProxyParseHandler {

    static Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(String.class, new JsonDeserializer<String>() {
                @Override
                public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                    return json.isJsonNull() ? "" : json.getAsString();
                }
            })
            .create();

    public static Proxy parseProxyLink(String link) throws Exception {
        Proxy config = new Proxy();

        // 解析协议
        if (link.startsWith("vmess://")) {
            config.setProtocol("vmess");
        } else if (link.startsWith("trojan://")) {
            config.setProtocol("trojan");
        } else if (link.startsWith("vless://")) {
            config.setProtocol("vless");
        } else if (link.startsWith("ss://")) {
            config.setProtocol("ss");
        } else {
            throw new IllegalArgumentException("未知协议");
        }

        // 解析 VMess 协议
        if (config.getProtocol().equals("vmess")) {
            // 去掉 "vmess://" 前缀
            String base64 = link.substring(8);

            // 解码 Base64
            String json = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);

            // 解析 JSON

            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

            // 填充配置（处理可能为空的字段）
            config.setAddress(jsonObject.has("add") ? jsonObject.get("add").getAsString() : "");
            config.setPort(jsonObject.has("port") ? jsonObject.get("port").getAsInt() : 0);
            config.setUserInfo(jsonObject.has("id") ? jsonObject.get("id").getAsString() : "");
            config.setRemark(jsonObject.has("ps") ? jsonObject.get("ps").getAsString() : "");

            // 填充 VMess 专用字段
            config.setId(jsonObject.has("id") ? jsonObject.get("id").getAsString() : "");
            config.setAid(jsonObject.has("aid") ? jsonObject.get("aid").getAsInt() : 0);
            config.setNet(jsonObject.has("net") ? jsonObject.get("net").getAsString() : "");
            config.setType(jsonObject.has("type") ? jsonObject.get("type").getAsString() : "");
            config.setPath(jsonObject.has("path") ? jsonObject.get("path").getAsString() : "");
            config.setTls(jsonObject.has("tls") ? jsonObject.get("tls").getAsString() : "");
            config.setSni(jsonObject.has("sni") ? jsonObject.get("sni").getAsString() : "");
            config.setAlpn(jsonObject.has("alpn") ? jsonObject.get("alpn").getAsString() : "");

            return config;
        }
        // 解析 VLESS 协议
        if (config.getProtocol().equals("vless")) {
            // 去掉 "vless://" 前缀
            String linkWithoutProtocol = link.substring(8);

            // 分割用户信息和地址部分
            String[] parts = linkWithoutProtocol.split("@");
            if (parts.length != 2) {
                throw new IllegalArgumentException("VLESS 链接格式错误");
            }

            // 设置用户信息
            config.setUserInfo(parts[0]);

            // 分割地址和查询参数
            String[] addressAndParams = parts[1].split("\\?");
            if (addressAndParams.length != 2) {
                throw new IllegalArgumentException("VLESS 链接格式错误");
            }

            // 解析地址和端口
            String addressWithPort = addressAndParams[0];
            String address;
            int port;

            // 处理 IPv6 地址
            if (addressWithPort.startsWith("[") && addressWithPort.contains("]:")) {
                // 提取 IPv6 地址部分
                int endIndex = addressWithPort.indexOf("]:");
                address = addressWithPort.substring(1, endIndex);
                // 提取端口部分
                port = Integer.parseInt(addressWithPort.substring(endIndex + 2));
            } else if (addressWithPort.contains(":")) {
                // 处理普通 IPv4 地址
                String[] addressParts = addressWithPort.split(":");
                if (addressParts.length != 2) {
                    throw new IllegalArgumentException("VLESS 链接地址格式错误");
                }
                address = addressParts[0];
                port = Integer.parseInt(addressParts[1]);
            } else {
                throw new IllegalArgumentException("VLESS 链接地址格式错误");
            }

            // 设置地址和端口
            config.setAddress(address);
            config.setPort(port);

            // 解析查询参数
            String queryParams = addressAndParams[1];
            String[] params = queryParams.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    config.getQueryParams().put(keyValue[0], keyValue[1]);
                }
            }

            return config;
        }

        // 解析其他协议
        String content = link.substring(link.indexOf("://") + 3);

        // 解析备注（# 后面的部分）
        int fragmentIndex = content.indexOf('#');
        if (fragmentIndex != -1) {
            config.setRemark(URLDecoder.decode(content.substring(fragmentIndex + 1), StandardCharsets.UTF_8));
            content = content.substring(0, fragmentIndex);
        }

        // 解析用户信息、地址和端口
        int atIndex = content.indexOf('@');
        if (atIndex == -1) {
            throw new IllegalArgumentException("Invalid link: missing user info");
        }
        String userInfo = content.substring(0, atIndex);
        String hostPort = content.substring(atIndex + 1);

        // 解析地址和端口
        int colonIndex = hostPort.indexOf(':');
        if (colonIndex == -1) {
            throw new IllegalArgumentException("Invalid link: missing port");
        }
        config.setAddress(hostPort.substring(0, colonIndex));
        config.setPort(Integer.parseInt(hostPort.substring(colonIndex + 1).split("[?/]")[0]));

        // 解析用户信息
        if (config.getProtocol().equals("ss")) {
            // Shadowsocks 的用户信息是 Base64 编码的
            userInfo = new String(Base64.getDecoder().decode(userInfo), StandardCharsets.UTF_8);
        }
        config.setUserInfo(userInfo);

        // 解析查询参数
        int queryIndex = hostPort.indexOf('?');
        if (queryIndex != -1) {
            String query = hostPort.substring(queryIndex + 1);
            Map<String, String> queryParams = new HashMap<>();
            for (String param : query.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
                }
            }
            config.setQueryParams(queryParams);
        }

        return config;
    }

    public static String toProxyLink(Proxy proxy) {
        if (proxy == null || proxy.getProtocol() == null) {
            throw new IllegalArgumentException("Proxy 对象或协议类型不能为空");
        }

        String protocol = proxy.getProtocol();

        // 根据协议生成链接
        switch (protocol) {
            case "vmess":
                return toVmessLink(proxy);
            case "vless":
                return toVlessLink(proxy);
            case "trojan":
                return toTrojanLink(proxy);
            case "ss":
                return toShadowsocksLink(proxy);
            default:
                throw new IllegalArgumentException("不支持的协议类型: " + protocol);
        }
    }

    /**
     * 生成 VMess 协议链接
     */
    private static String toVmessLink(Proxy proxy) {
        JsonObject json = new JsonObject();
        json.addProperty("v", "2"); // 版本
        json.addProperty("ps", processRemark(proxy.getRemark())); // 处理后的备注
        json.addProperty("add", proxy.getAddress()); // 地址
        json.addProperty("port", proxy.getPort()); // 端口
        json.addProperty("id", proxy.getUserInfo()); // 用户 ID
        json.addProperty("aid", proxy.getAid()); // 额外 ID
        json.addProperty("net", proxy.getNet()); // 传输协议
        json.addProperty("type", proxy.getType()); // 伪装类型
        json.addProperty("path", proxy.getPath()); // 路径
        json.addProperty("tls", proxy.getTls()); // TLS 配置
        json.addProperty("sni", proxy.getSni()); // SNI 配置
        json.addProperty("alpn", proxy.getAlpn()); // ALPN 配置

        // 移除空值字段
        json.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().isJsonNull());

        // 将 JSON 编码为 Base64
        String base64 = Base64.getEncoder().encodeToString(json.toString().getBytes(StandardCharsets.UTF_8));
        return "vmess://" + base64;
    }

    /**
     * 生成 VLESS 协议链接
     */
    private static String toVlessLink(Proxy proxy) {
        StringBuilder link = new StringBuilder("vless://");
        link.append(proxy.getUserInfo()).append("@"); // 用户信息
        link.append(formatAddress(proxy.getAddress())).append(":").append(proxy.getPort()); // 地址和端口
        link.append("?").append(buildQueryParams(proxy.getQueryParams())); // 查询参数
        if (proxy.getRemark() != null) {
            link.append("#").append(processRemark(proxy.getRemark())); // 处理后的备注
        }
        return link.toString();
    }

    /**
     * 生成 Trojan 协议链接
     */
    private static String toTrojanLink(Proxy proxy) {
        StringBuilder link = new StringBuilder("trojan://");
        link.append(proxy.getUserInfo()).append("@"); // 用户信息
        link.append(formatAddress(proxy.getAddress())).append(":").append(proxy.getPort()); // 地址和端口
        link.append("?").append(buildQueryParams(proxy.getQueryParams())); // 查询参数
        if (proxy.getRemark() != null) {
            link.append("#").append(processRemark(proxy.getRemark()));
        }
        return link.toString();
    }

    /**
     * 生成 Shadowsocks 协议链接
     */
    private static String toShadowsocksLink(Proxy proxy) {
        String userInfo = Base64.getEncoder().encodeToString(proxy.getUserInfo().getBytes(StandardCharsets.UTF_8));
        StringBuilder link = new StringBuilder("ss://");
        link.append(userInfo).append("@"); // 用户信息
        link.append(formatAddress(proxy.getAddress())).append(":").append(proxy.getPort()); // 地址和端口
        if (proxy.getRemark() != null) {
            link.append("#").append(processRemark(proxy.getRemark())); // 处理后的备注
        }
        return link.toString();
    }

    /**
     * 格式化地址（处理 IPv6）
     */
    private static String formatAddress(String address) {
        if (address.contains(":")) {
            return "[" + address + "]"; // IPv6 地址用 [] 包裹
        }
        return address;
    }

    /**
     * 构建查询参数
     */
    private static String buildQueryParams(Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return "";
        }
        StringBuilder params = new StringBuilder();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (params.length() > 0) {
                params.append("&");
            }
            params.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return params.toString();
    }

    /**
     * 处理备注：保留前两个汉字 + 括号内容（如果有）
     */
    private static String processRemark(String originalRemark) {
        if (originalRemark == null) return null;
        return originalRemark.replaceAll("^(..).*?(\\$[^)]*\\$)?.*$", "$1$2");
    }
}