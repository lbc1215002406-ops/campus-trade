package com.example.demo.ai.provider;

import com.example.demo.ai.entity.AiConfig;
import com.example.demo.ai.service.AiConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProviderFactory {

    @Autowired
    private AiConfigService aiConfigService;

    public OpenAiCompatProvider createActive() {
        AiConfig config = aiConfigService.getActive();
        if (config == null) throw new RuntimeException("没有启用的 AI 配置，请先在管理后台添加");
        OpenAiCompatProvider provider = new OpenAiCompatProvider();
        provider.setConfig(config);
        return provider;
    }
}
