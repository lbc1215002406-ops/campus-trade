package com.example.demo.ai.provider;

public interface AiProvider {
    String chat(String systemPrompt, String userMessage);
    String chatJson(String systemPrompt, String userMessage);
}
