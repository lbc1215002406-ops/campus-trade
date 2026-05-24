package com.example.demo.controller;

import com.example.demo.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result<List<String>> upload(@RequestParam("files") MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        String basePath = System.getProperty("user.dir") + File.separator + uploadDir;
        File dir = new File(basePath);
        if (!dir.exists()) dir.mkdirs();

        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".") ?
                    originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
            String newName = UUID.randomUUID().toString() + ext;
            try {
                File dest = new File(basePath, newName);
                file.transferTo(dest);
                urls.add("http://localhost:8080/uploads/" + newName);
            } catch (IOException e) {
                return Result.error("上传失败：" + e.getMessage());
            }
        }
        return Result.success(urls);
    }
}
