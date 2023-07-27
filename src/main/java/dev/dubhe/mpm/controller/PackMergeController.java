package dev.dubhe.mpm.controller;

import dev.dubhe.mpm.files.FileTreeNode;
import dev.dubhe.mpm.utils.FileUtil;
import dev.dubhe.mpm.utils.PackMerger;
import dev.dubhe.mpm.data.ResponseResult;
import dev.dubhe.mpm.data.meta.SubPackMeta;
import dev.dubhe.mpm.request.DownloadPackRequestBody;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class PackMergeController {
    @GetMapping("/main")
    public ResponseResult getMainPackInfo() {
        return ResponseResult.success(PackMerger.loadMainPackMeta());
    }

    @GetMapping("/adds")
    public ResponseResult getAddsPackInfo() {
        PackMerger merger = new PackMerger();
        List<SubPackMeta> list = new ArrayList<>();
        merger.adds.forEach(v -> list.add(v.getKey()));
        return ResponseResult.success(list);
    }

    @GetMapping("/mods")
    public ResponseResult getModsPackInfo() {
        PackMerger merger = new PackMerger();
        List<SubPackMeta> list = new ArrayList<>();
        merger.mods.forEach(v -> list.add(v.getKey()));
        return ResponseResult.success(list);
    }

    @PostMapping("/download")
    public void downloadPack(@RequestBody DownloadPackRequestBody body, HttpServletResponse response) throws IOException {
        File file = PackMerger.mergeAndCreate(body.getAdds(), body.getMods());
        response.reset();
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-Length", String.valueOf(file.length()));
        FileUtil.setAttachmentResponseHeader(response, file.getName());
        FileUtil.writeBytes(file, response.getOutputStream());
        File parentFile = file.getParentFile();
        if (!file.delete() || !parentFile.delete()) throw new RuntimeException();
    }

    @GetMapping("/icons/{id}")
    public void getIcon(@PathVariable String id, HttpServletResponse response) throws IOException {
        PackMerger merger = new PackMerger();
        File file = null;
        if ("main".equals(id)) {
            file = merger.mainDir.file.toPath().resolve("pack.png").toFile();
        } else {
            for (Map.Entry<SubPackMeta, FileTreeNode> entry : merger.adds) {
                if (!id.equals(entry.getKey().id)) continue;
                file = entry.getValue().file.toPath().resolve("pack.png").toFile();
            }
        }
        if (file == null || !file.isFile()) {
            return;
        }
        response.reset();
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-Length", String.valueOf(file.length()));
        FileUtil.setAttachmentResponseHeader(response, file.getName());
        FileUtil.writeBytes(file, response.getOutputStream());
    }
}
