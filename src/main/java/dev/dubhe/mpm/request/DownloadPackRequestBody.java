package dev.dubhe.mpm.request;

import lombok.Data;

import java.beans.ConstructorProperties;
import java.util.List;

@Data
public class DownloadPackRequestBody {
    private final List<String> adds;
    private final List<String> mods;

    @ConstructorProperties({"adds", "mods"})
    public DownloadPackRequestBody(List<String> adds, List<String> mods) {
        this.adds = adds;
        this.mods = mods;
    }
}
