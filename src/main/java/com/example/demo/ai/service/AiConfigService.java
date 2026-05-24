package com.example.demo.ai.service;

import com.example.demo.ai.entity.AiConfig;
import java.util.List;

public interface AiConfigService {
    List<AiConfig> listAll();
    AiConfig add(AiConfig config);
    AiConfig update(AiConfig config);
    void remove(Long id);
    AiConfig activate(Long id);
    AiConfig getActive();
    boolean testConnection(AiConfig config);
}
