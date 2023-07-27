package dev.dubhe.mpm.data.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import dev.dubhe.mpm.data.version.Versions;
import lombok.Data;

@Data
public class SubPackMeta {
    public String id;
    public String name; // 名称
    public String desc; // 描述
    public int weight;
    @Versions.Formatter
    @SerializedName("main")
    @JsonProperty("main")
    public Versions mainVersions;
    @Versions.Formatter
    @SerializedName("mc")
    @JsonProperty("mc")
    public Versions mcVersions;
    @SerializedName("default")
    @JsonProperty("default")
    public boolean def;
    public boolean required;
}
