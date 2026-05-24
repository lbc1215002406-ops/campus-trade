package com.example.demo.ai.service;

import com.example.demo.ai.provider.AiProvider;
import com.example.demo.ai.provider.ProviderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiGatewayService {

    @Autowired
    private ProviderFactory providerFactory;

    public String chat(String systemPrompt, String userMessage) {
        AiProvider provider = providerFactory.createActive();
        return provider.chat(systemPrompt, userMessage);
    }

    public String chatJson(String systemPrompt, String userMessage) {
        AiProvider provider = providerFactory.createActive();
        return provider.chatJson(systemPrompt, userMessage);
    }
}
