package com.example.demo.ai.controller;

import com.example.demo.ai.entity.AiConfig;
import com.example.demo.ai.service.AiConfigService;
import com.example.demo.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ai-config")
public class AiConfigController {

    @Autowired
    private AiConfigService aiConfigService;

    @GetMapping
    public Result<List<AiConfig>> list() {
        return Result.success(aiConfigService.listAll());
    }

    @PostMapping
    public Result<AiConfig> add(@RequestBody AiConfig config) {
        try {
            return Result.success(aiConfigService.add(config));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<AiConfig> update(@PathVariable Long id, @RequestBody AiConfig config) {
        try {
            config.setId(id);
            return Result.success(aiConfigService.update(config));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> remove(@PathVariable Long id) {
        try {
            aiConfigService.remove(id);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/activate")
    public Result<AiConfig> activate(@PathVariable Long id) {
        try {
            return Result.success(aiConfigService.activate(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/test")
    public Result<String> testConnection(@RequestBody AiConfig config) {
        boolean ok = aiConfigService.testConnection(config);
        if (ok) return Result.success("连接成功");
        return Result.error("连接失败，请检查 API Key 和 Base URL");
    }
}
