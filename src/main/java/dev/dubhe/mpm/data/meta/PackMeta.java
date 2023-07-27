package dev.dubhe.mpm.data.meta;

import dev.dubhe.mpm.data.version.Version;
import lombok.Data;

@Data
public class PackMeta {
    public String name; // 名称
    public String desc; // 描述
    @Version.Formatter
    public Version version; // 版本
}
