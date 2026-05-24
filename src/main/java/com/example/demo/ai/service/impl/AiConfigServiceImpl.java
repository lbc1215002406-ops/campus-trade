package com.example.demo.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.ai.entity.AiConfig;
import com.example.demo.ai.mapper.AiConfigMapper;
import com.example.demo.ai.service.AiConfigService;
import com.example.demo.ai.util.AesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AiConfigServiceImpl implements AiConfigService {

    @Autowired
    private AiConfigMapper aiConfigMapper;

    @Override
    public List<AiConfig> listAll() {
        List<AiConfig> list = aiConfigMapper.selectList(null);
        list.forEach(c -> c.setApiKey(maskKey(c.getApiKey())));
        return list;
    }

    @Override
    public AiConfig add(AiConfig config) {
        config.setApiKey(AesUtil.encrypt(config.getApiKey()));
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        if (config.getIsActive() == null) config.setIsActive(0);
        if (config.getMaxTokens() == null) config.setMaxTokens(4096);
        if (config.getTemperature() == null) config.setTemperature(new java.math.BigDecimal("0.7"));
        aiConfigMapper.insert(config);
        config.setApiKey(maskKey(config.getApiKey()));
        return config;
    }

    @Override
    public AiConfig update(AiConfig config) {
        AiConfig existing = aiConfigMapper.selectById(config.getId());
        if (existing == null) throw new RuntimeException("配置不存在");
        if (config.getApiKey() != null && !config.getApiKey().startsWith("****")) {
            existing.setApiKey(AesUtil.encrypt(config.getApiKey()));
        }
        if (config.getModelName() != null) existing.setModelName(config.getModelName());
        if (config.getBaseUrl() != null) existing.setBaseUrl(config.getBaseUrl());
        if (config.getMaxTokens() != null) existing.setMaxTokens(config.getMaxTokens());
        if (config.getTemperature() != null) existing.setTemperature(config.getTemperature());
        if (config.getProvider() != null) existing.setProvider(config.getProvider());
        existing.setUpdateTime(LocalDateTime.now());
        aiConfigMapper.updateById(existing);
        existing.setApiKey(maskKey(existing.getApiKey()));
        return existing;
    }

    @Override
    public void remove(Long id) {
        aiConfigMapper.deleteById(id);
    }

    @Override
    public AiConfig activate(Long id) {
        LambdaQueryWrapper<AiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConfig::getIsActive, 1);
        List<AiConfig> activeList = aiConfigMapper.selectList(wrapper);
        for (AiConfig c : activeList) {
            c.setIsActive(0);
            aiConfigMapper.updateById(c);
        }
        AiConfig target = aiConfigMapper.selectById(id);
        if (target == null) throw new RuntimeException("配置不存在");
        target.setIsActive(1);
        target.setUpdateTime(LocalDateTime.now());
        aiConfigMapper.updateById(target);
        target.setApiKey(maskKey(target.getApiKey()));
        return target;
    }

    @Override
    public AiConfig getActive() {
        LambdaQueryWrapper<AiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConfig::getIsActive, 1);
        AiConfig config = aiConfigMapper.selectOne(wrapper);
        if (config != null) {
            config.setApiKey(AesUtil.decrypt(config.getApiKey()));
        }
        return config;
    }

    @Override
    public boolean testConnection(AiConfig config) {
        // 不存库，仅用传入的配置尝试一次简单对话
        try {
            String key = config.getApiKey();
            if (key != null && !key.startsWith("****")) {
                // 明文key直接使用
            } else if (key != null && key.startsWith("****")) {
                // 脱敏key，需要从数据库查真实值
                AiConfig db = aiConfigMapper.selectById(config.getId());
                if (db != null) key = AesUtil.decrypt(db.getApiKey());
            }
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build();
            String body = "{\"model\":\"" + config.getModelName() + "\",\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}],\"max_tokens\":10}";
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(config.getBaseUrl() + "/v1/chat/completions")
                    .header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json")
                    .post(okhttp3.RequestBody.create(body, okhttp3.MediaType.parse("application/json")))
                    .build();
            okhttp3.Response response = client.newCall(request).execute();
            return response.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    private String maskKey(String key) {
        if (key == null || key.length() <= 4) return "****";
        return "****" + key.substring(key.length() - 4);
    }
}
