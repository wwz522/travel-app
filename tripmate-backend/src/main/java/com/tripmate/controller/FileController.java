package com.tripmate.controller;

import com.tripmate.annotation.RequireAuth;
import com.tripmate.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @PostMapping("/upload")
    @RequireAuth
    public Result<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            
            String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String filename = UUID.randomUUID().toString() + extension;
            
            Path targetPath = Paths.get(uploadPath, dateFolder, filename);
            Files.createDirectories(targetPath.getParent());
            
            file.transferTo(targetPath.toFile());
            
            String fileUrl = urlPrefix + "/" + dateFolder + "/" + filename;
            
            Map<String, String> data = new HashMap<>();
            data.put("url", fileUrl);
            data.put("filename", filename);
            
            return Result.success(data);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @RequireAuth
    public Result<?> deleteFile(@RequestParam String url, HttpServletRequest request) {
        try {
            String relativePath = url.replace(urlPrefix, "");
            Path filePath = Paths.get(uploadPath, relativePath);
            
            File file = filePath.toFile();
            if (file.exists()) {
                file.delete();
                return Result.success("删除成功");
            } else {
                return Result.error("文件不存在");
            }
        } catch (Exception e) {
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }
}
