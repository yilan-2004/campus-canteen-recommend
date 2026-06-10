package com.example.shitang.controller;

import com.example.shitang.common.BizException;
import com.example.shitang.common.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @PostMapping("/image")
    public Result<UploadResult> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BizException("文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new BizException("文件名无效");
        }
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot > 0) {
            ext = originalName.substring(dot).toLowerCase();
        }
        if (!".jpg".equals(ext) && !".jpeg".equals(ext) && !".png".equals(ext) && !".webp".equals(ext) && !".gif".equals(ext)) {
            throw new BizException("只支持 jpg/jpeg/png/webp/gif 格式");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BizException("文件大小不能超过 5MB");
        }

        try {
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());

            UploadResult result = new UploadResult();
            result.setUrl(baseUrl + "/" + filename);
            result.setName(originalName);
            return Result.success(result);
        } catch (IOException e) {
            throw new BizException("文件上传失败: " + e.getMessage());
        }
    }

    @Data
    public static class UploadResult {
        private String url;
        private String name;
    }
}
