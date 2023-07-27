package dev.dubhe.mpm.data;

import com.google.gson.annotations.SerializedName;

import java.nio.file.Path;

public class Config {
    @SerializedName("work_dir")
    public String workDir = "run";
    @SerializedName("build_dir")
    public String buildDir = "build";

    public Path getRunDir() {
        return Path.of(System.getProperty("user.dir"));
    }

    public Path getWorkDir() {
        return this.getRunDir().resolve(workDir);
    }

    public Path getMainDir() {
        return this.getWorkDir().resolve("main");
    }

    public Path getAddsDir() {
        return this.getWorkDir().resolve("adds");
    }

    public Path getModsDir() {
        return this.getWorkDir().resolve("mods");
    }

    public Path getBuildDir() {
        return this.getRunDir().resolve(buildDir);
    }
}
