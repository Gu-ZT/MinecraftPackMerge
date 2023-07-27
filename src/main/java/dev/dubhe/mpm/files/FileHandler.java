package dev.dubhe.mpm.files;

import java.io.File;

public class FileHandler {
    public static void handleFiles(FileTreeNode... files) {
        for (FileTreeNode file : files) {
            if (file.file.isFile()) {
                handleFile(file);
            } else if (file.file.isDirectory()) {
                handleDirectory(file);
            }
        }
    }

    public static void handleFile(FileTreeNode file) {
    }

    public static void handleDirectory(FileTreeNode dir) {
        if (dir.file.isDirectory()) {
            File[] files = dir.file.listFiles();
            if (files == null) return;
            for (File file : files) {
                dir.addChildren(file);
            }
            handleFiles(dir.children.values().toArray(FileTreeNode[]::new));
        }
    }
}
