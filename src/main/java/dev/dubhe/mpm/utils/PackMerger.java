package dev.dubhe.mpm.utils;

import com.google.gson.*;
import dev.dubhe.mpm.data.Config;
import dev.dubhe.mpm.data.meta.PackMeta;
import dev.dubhe.mpm.data.meta.SubPackMeta;
import dev.dubhe.mpm.data.version.Version;
import dev.dubhe.mpm.data.version.Versions;
import dev.dubhe.mpm.files.FileTreeNode;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipOutputStream;

public class PackMerger {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Version.class, (JsonSerializer<Version>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(Version.class, (JsonDeserializer<Version>) (json, typeOfT, context) -> new Version(json.getAsString()))
            .registerTypeAdapter(Versions.class, (JsonSerializer<Versions>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(Versions.class, (JsonDeserializer<Versions>) (json, typeOfT, context) -> new Versions(json.getAsString()))
            .create();
    public static Config config = null;
    public final PackMeta mainPackMeta;
    public final FileTreeNode mainDir;
    public final List<Map.Entry<SubPackMeta, FileTreeNode>> adds;
    public final List<Map.Entry<SubPackMeta, FileTreeNode>> mods;

    static {
        PackMerger.reloadConfig();
    }

    public PackMerger() {
        this.mainDir = new FileTreeNode(config.getMainDir().toFile(), null, true, "meta.json");
        this.adds = walkAdds(config.getAddsDir());
        this.mods = walkAdds(config.getModsDir());
        this.mainPackMeta = loadMainPackMeta();
    }

    public static PackMeta loadMainPackMeta() {
        File meta = config.getMainDir().resolve("meta.json").toFile();
        try (FileReader reader = new FileReader(meta)) {
            return GSON.fromJson(reader, PackMeta.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reloadConfig() {
        Path configPath = Path.of(System.getProperty("user.dir")).resolve("config.json");
        try (FileReader reader = new FileReader(configPath.toFile())) {
            PackMerger.config = PackMerger.GSON.fromJson(reader, Config.class);
        } catch (FileNotFoundException e) {
            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                PackMerger.GSON.toJson(PackMerger.config = new Config(), writer);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void merge(String... ids) {
        for (Map.Entry<SubPackMeta, FileTreeNode> entry : this.adds) {
            if (!Arrays.stream(ids).toList().contains(entry.getKey().id)) continue;
            this.mainDir.merge(entry.getValue());
        }
        this.mainDir.createZip(getFileName(this.mainPackMeta));
    }

    public static File mergeAndCreate(List<String> addIds, List<String> modIds) {
        addIds.sort(Comparator.comparingInt(String::hashCode));
        modIds.sort(Comparator.comparingInt(String::hashCode));
        StringBuilder pathName = new StringBuilder("main");
        for (String s : addIds) {
            pathName.append("+").append(s);
        }
        for (String s : modIds) {
            pathName.append("+").append(s);
        }
        Path buildPath = PackMerger.config.getBuildDir().resolve(pathName.toString());
        File buildDir = buildPath.toFile();
        File file = buildPath.resolve(getFileName(loadMainPackMeta())).toFile();
        if (buildDir.isDirectory() && file.isFile()) return file;
        if (buildDir.isDirectory() || !buildDir.mkdirs()) throw new RuntimeException();
        PackMerger merge = new PackMerger();
        for (Map.Entry<SubPackMeta, FileTreeNode> entry : merge.adds) {
            if (!addIds.contains(entry.getKey().id)) continue;
            merge.mainDir.merge(entry.getValue());
        }
        for (Map.Entry<SubPackMeta, FileTreeNode> entry : merge.mods) {
            if (!modIds.contains(entry.getKey().id)) continue;
            merge.mainDir.merge(entry.getValue());
        }
        try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(file.getPath()))) {
            merge.mainDir.compress(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public void createZip() {
        this.mainDir.createZip(getFileName(this.mainPackMeta));
    }

    public static String getFileName(PackMeta mainPackMeta) {
        return "%s-v%s.zip".formatted(mainPackMeta.name, mainPackMeta.version);
    }

    public static List<Map.Entry<SubPackMeta, FileTreeNode>> walkAdds(Path addsPath) {
        List<Map.Entry<SubPackMeta, FileTreeNode>> list = new ArrayList<>();
        File addsDir = addsPath.toFile();
        if (addsDir.isDirectory()) {
            File[] subDirs = addsDir.listFiles();
            if (subDirs != null) for (File file : subDirs) {
                if (file.isDirectory()) {
                    File meta = file.toPath().resolve("meta.json").toFile();
                    try (FileReader reader = new FileReader(meta)) {
                        SubPackMeta packMeta = PackMerger.GSON.fromJson(reader, SubPackMeta.class);
                        list.add(new AbstractMap.SimpleEntry<>(packMeta, new FileTreeNode(file, null, true, "meta.json", "pack.png")));
                    } catch (FileNotFoundException ignored) {
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        list.sort(Comparator.comparingInt(v -> v.getKey().weight));
        return list;
    }

    public static void main(String[] args) {
        PackMerger merge = new PackMerger();
        merge.merge("detail_models", "lush_plants", "naturalize");
        merge.createZip();
    }
}
