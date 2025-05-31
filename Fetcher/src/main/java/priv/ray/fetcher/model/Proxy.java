package priv.ray.fetcher.model;

/**
 * @author: Ray
 * @date: 2025/3/17 23:25
 * @desc:
 * @changes:
 */
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
public class Proxy {
    private String protocol; // 协议类型（trojan、vless、ss、xmess、）
    private String address;  // 服务器地址
    private int port;        // 端口
    private String userInfo; // 用户信息（如密码、UUID）
    private Map<String, String> queryParams = new HashMap<>(); // 查询参数
    private String remark;   // 备注

    // VMess 协议专用字段
    private String id;       // VMess 的用户 ID
    private int aid;         // VMess 的额外 ID
    private String net;      // VMess 的传输协议（如 tcp、ws）
    private String type;     // VMess 的伪装类型（如 none、http）
    private String path;     // VMess 的路径（用于 WebSocket）
    private String tls;      // VMess 的 TLS 配置（如 tls）
    private String sni;      // VMess 的 SNI 配置
    private String alpn;     // VMess 的 ALPN 配置

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Proxy{");

        if (protocol != null) {
            sb.append("protocol='").append(protocol).append("', ");
        }
        if (address != null) {
            sb.append("address='").append(address).append("', ");
        }
        if (port != 0) {
            sb.append("port=").append(port).append(", ");
        }
        if (userInfo != null) {
            sb.append("userInfo='").append(userInfo).append("', ");
        }
        if (!queryParams.isEmpty()) {
            sb.append("queryParams=").append(queryParams).append(", ");
        }
        if (remark != null) {
            sb.append("remark='").append(remark).append("', ");
        }
        if (id != null) {
            sb.append("id='").append(id).append("', ");
        }
        if (aid != 0) {
            sb.append("aid=").append(aid).append(", ");
        }
        if (net != null) {
            sb.append("net='").append(net).append("', ");
        }
        if (type != null) {
            sb.append("type='").append(type).append("', ");
        }
        if (path != null) {
            sb.append("path='").append(path).append("', ");
        }
        if (tls != null) {
            sb.append("tls='").append(tls).append("', ");
        }
        if (sni != null) {
            sb.append("sni='").append(sni).append("', ");
        }
        if (alpn != null) {
            sb.append("alpn='").append(alpn).append("', ");
        }

        // 去掉最后一个多余的逗号和空格
        if (sb.length() > 6 && sb.substring(sb.length() - 2).equals(", ")) {
            sb.delete(sb.length() - 2, sb.length());
        }

        sb.append("}");
        return sb.toString();
    }
    @Override
    public boolean equals(Object obj) {
        // 1. 检查是否是同一个对象
        if (this == obj) return true;
        // 2. 检查对象是否为 null 或者类型是否不匹配
        if (obj == null || getClass() != obj.getClass()) return false;
        // 3. 强制转换为 Proxy 类型
        Proxy proxy = (Proxy) obj;
        // 4. 比较 address 和 port 字段
        return port == proxy.port && Objects.equals(address, proxy.address);
    }
    @Override
    public int hashCode() {
        // 使用 Objects.hash() 方法生成哈希值
        return Objects.hash(address, port);
    }



}