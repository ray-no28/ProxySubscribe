package priv.ray.fetcher.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import priv.ray.fetcher.model.Proxy;

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
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

            // 填充配置
            config.setAddress(jsonObject.get("add").getAsString());
            config.setPort(jsonObject.get("port").getAsInt());
            config.setUserInfo(jsonObject.get("id").getAsString()); // 使用 id 作为 userInfo
            config.setRemark(jsonObject.get("ps").getAsString());

            // 填充 VMess 专用字段
            config.setId(jsonObject.get("id").getAsString());
            config.setAid(jsonObject.get("aid").getAsInt());
            config.setNet(jsonObject.get("net").getAsString());
            config.setType(jsonObject.get("type").getAsString());
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
        String userInfo = proxy.getUserInfo();
        String address = proxy.getAddress();
        int port = proxy.getPort();
        Map<String, String> queryParams = proxy.getQueryParams();
        String remark = proxy.getRemark();

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
        json.addProperty("ps", proxy.getRemark()); // 备注
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
            link.append("#").append(proxy.getRemark()); // 备注
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
            link.append("#").append(proxy.getRemark()); // 备注
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
            link.append("#").append(proxy.getRemark()); // 备注
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


/**    public static void main(String[] args) {
 String s = "ss://YWVzLTEyOC1nY206NjYwMWZiOTBlOWIz@127.0.0.1:443#6%E5%85%83%E9%AB%98%E9%80%9F%E6%9C%BA%E5%9C%BA%EF%BC%9Acczzuu.top\n" +
 "ss://YWVzLTEyOC1nY206NjYwMWZiOTBlOWIz@127.0.0.1:443#%E9%87%8D%E5%BA%A6%E7%94%A8%E6%88%B7%E6%8E%A8%E8%8D%90%E4%BD%BF%E7%94%A8%E6%9C%BA%E5%9C%BA\n" +
 "ss://YWVzLTEyOC1nY206NjYwMWZiOTBlOWIz@127.0.0.1:443#%E5%85%8D%E8%B4%B9%E8%8A%82%E7%82%B9%E5%8F%AF%E8%83%BD%E4%BC%9A%E6%B3%84%E6%BC%8F%E8%AE%BF%E9%97%AE%E8%AE%B0%E5%BD%95\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.127:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_1\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.182:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_2\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.206:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD-%3E%F0%9F%87%BA%F0%9F%87%B8_US_%E7%BE%8E%E5%9B%BD_1\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.141:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_3\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.47:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_4\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.97:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_5\n" +
 "vless://27fdbffe-67a6-4012-fa3e-8ab349ec0a91@81.31.250.218:43851?security=none&type=ws&encryption=none&type=ws&path=%2F&headerType=none#%F0%9F%87%AE%F0%9F%87%B7_IR_%E4%BC%8A%E6%9C%97-%3E%F0%9F%87%BA%F0%9F%87%B8_US_%E7%BE%8E%E5%9B%BD\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.162:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_6\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.143:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD-%3E%F0%9F%87%BA%F0%9F%87%B8_US_%E7%BE%8E%E5%9B%BD_2\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.172:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_7\n" +
 "trojan://576c81b6-4976-4fe3-b1a9-05a9c302e98e@192.3.130.103:443?type=grpc&sni=us10-01.iran2030.ggff.net&alpn=h2&serviceName=i8oL7PsxV002zYFTmiIeg#%F0%9F%87%BA%F0%9F%87%B8_US_%E7%BE%8E%E5%9B%BD_1\n" +
 "ss://YWVzLTI1Ni1jZmI6YW1hem9uc2tyMDU=@34.211.230.161:443#%F0%9F%87%BA%F0%9F%87%B8_US_%E7%BE%8E%E5%9B%BD-%3E%F0%9F%87%A9%F0%9F%87%B0_DK_%E4%B8%B9%E9%BA%A6\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.125:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_8\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.71:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD-%3E%F0%9F%87%BA%F0%9F%87%B8_US_%E7%BE%8E%E5%9B%BD_3\n" +
 "ss://cmM0LW1kNToxNGZGUHJiZXpFM0hEWnpzTU9yNg==@193.108.119.230:8080#%F0%9F%87%A9%F0%9F%87%AA_DE_%E5%BE%B7%E5%9B%BD\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.185:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD-%3E%F0%9F%87%BA%F0%9F%87%B8_US_%E7%BE%8E%E5%9B%BD_4\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.2:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_9\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.117:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_10\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.159:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_11\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.78:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_12\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.201:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_13\n" +
 "ss://YWVzLTI1Ni1jZmI6YW1hem9uc2tyMDU=@18.236.137.219:443#%F0%9F%87%BA%F0%9F%87%B8_US_%E7%BE%8E%E5%9B%BD_2\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.135:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_14\n" +
 "ss://YWVzLTI1Ni1jZmI6YW1hem9uc2tyMDU=@34.210.253.95:443#%F0%9F%87%BA%F0%9F%87%B8_US_%E7%BE%8E%E5%9B%BD_3\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.102:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_15\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.114:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_16\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.156:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_17\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.157:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_18\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.61:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_19\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.255:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD-%3E%F0%9F%87%BA%F0%9F%87%B8_US_%E7%BE%8E%E5%9B%BD_5\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.137:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_20\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.138:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD_21\n" +
 "vless://568279ce-dd78-4f33-9e5c-64b18c5505db@151.101.123.177:80?security=none&type=ws&host=foffmelo.com&encryption=none&type=ws&path=%2Folem%2Fws%3Fed%3D2560&sni=foffmelo.com&headerType=none#%F0%9F%87%AB%F0%9F%87%B7_FR_%E6%B3%95%E5%9B%BD-%3E%F0%9F%87%BA%F0%9F%87%B8_US_%E7%BE%8E%E5%9B%BD_6";
 String[] split = s.split("\n");
 Arrays.stream(split).forEach(c-> {
 try {
 System.out.println(ProxyParseHandler.parseProxyLink(c));
 } catch (Exception e) {
 throw new RuntimeException(e);
 }
 });
 }
 */

}
