package dev.dubhe.mpm.files;

import jakarta.annotation.Nullable;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileTreeNode {
    public File file;
    @Nullable
    public final FileTreeNode parent;
    public final Map<String, FileTreeNode> children = new HashMap<>();
    public final List<String> path = new ArrayList<>();

    public FileTreeNode(File file, @Nullable FileTreeNode parent, String... ignore) {
        this.file = file;
        this.parent = parent;
        if (parent != null) path.addAll(parent.path);
        path.add(file.getName());
        if (file.isDirectory()) {
            handleDirectory(file, ignore);
        }
    }

    public FileTreeNode(File file, @Nullable FileTreeNode parent, boolean root, String... ignore) {
        this.file = file;
        this.parent = parent;
        if (parent != null) path.addAll(parent.path);
        if (!root) path.add(file.getName());
        if (file.isDirectory()) {
            handleDirectory(file, ignore);
        }
    }

    public void handleDirectory(File dir, String... ignore) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files == null) return;
            for (File file : files) {
                if (Arrays.stream(ignore).toList().contains(file.getName())) continue;
                this.addChildren(file);
            }
        }
    }

    @SuppressWarnings("all")
    public FileTreeNode addChildren(File file) {
        children.put(file.getName(), new FileTreeNode(file, this));
        return this;
    }

    public void print(int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("    ");
        }
        System.out.println("┗ " + file.getPath());
        for (int i = 0; i < indent; i++) {
            System.out.print("    ");
        }
        System.out.println("  " + file.getName());
        for (FileTreeNode child : children.values()) {
            child.print(indent + 1);
        }
        System.out.println("  " + this.path);
        for (FileTreeNode child : children.values()) {
            child.print(indent + 1);
        }
    }

    public String getName() {
        return file.getName();
    }

    public FileTreeNode merge(FileTreeNode node) {
        if (this.getName().equals(node.getName())) this.file = node.file;
        for (Map.Entry<String, FileTreeNode> entry : node.children.entrySet()) {
            if (this.children.containsKey(entry.getKey())) {
                this.children.get(entry.getKey()).merge(entry.getValue());
            } else {
                this.children.put(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FileTreeNode node && node.hashCode() == this.hashCode();
    }

    public boolean isFile() {
        return this.file.isFile();
    }

    public boolean isDirectory() {
        return this.file.isDirectory();
    }

    public void createZip(String fileName) {
        try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(fileName))) {
            this.compress(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createZip(OutputStream stream) {
        try (ZipOutputStream outputStream = new ZipOutputStream(stream)) {
            this.compress(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void compress(ZipOutputStream outputStream) throws IOException {
        for (FileTreeNode value : children.values()) {
            if (value.isDirectory()) value.compress(outputStream);
            else if (value.isFile()) {
                ZipEntry entry = new ZipEntry(value.getPath());
                outputStream.putNextEntry(entry);
                try (FileInputStream fileInputStream = new FileInputStream(value.file)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                outputStream.closeEntry();
            }
        }
    }

    public String getPath() {
        StringBuilder builder = new StringBuilder();
        for (String s : this.path) {
            builder.append(s).append(s.equals(this.path.get(this.path.size() - 1)) ? "" : "/");
        }
        return builder.toString();
    }
}
