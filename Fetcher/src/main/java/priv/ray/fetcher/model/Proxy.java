package priv.ray.fetcher.model;

import lombok.Data;

import java.util.Map;

/**
 * @author: Ray
 * @date: 2025/3/16 19:37
 * @desc:
 * @changes:
 */
@Data
public class Proxy {
    private String protocol;       // 协议（如 ss、trojan、vless）
    private String username;      // 用户名（如果有）
    private String password;      // 密码（如果有）
    private String host;          // 主机地址
    private int port;             // 端口
    private String security;      // 安全协议（如 tls）
    private String sni;           // SNI（服务器名称指示）
    private String type;          // 类型（如 ws、grpc）
    private String path;          // 路径
    private String remark;        // 备注（如 #US_美国）
}
