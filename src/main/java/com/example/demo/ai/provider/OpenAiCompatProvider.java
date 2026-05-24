package com.example.demo.ai.provider;

import com.example.demo.ai.entity.AiConfig;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class OpenAiCompatProvider implements AiProvider {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .build();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private AiConfig config;

    public void setConfig(AiConfig config) {
        this.config = config;
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        return callApi(systemPrompt, userMessage, false);
    }

    @Override
    public String chatJson(String systemPrompt, String userMessage) {
        return callApi(systemPrompt, userMessage, true);
    }

    private String callApi(String systemPrompt, String userMessage, boolean jsonMode) {
        if (config == null) throw new RuntimeException("AI 配置未初始化");

        // OpenAI 兼容 API 要求：使用 json_object 格式时，提示词必须包含 "json"
        if (jsonMode && !containsJsonKeyword(systemPrompt) && !containsJsonKeyword(userMessage)) {
            systemPrompt = systemPrompt + " 请返回JSON格式。";
        }

        StringBuilder messages = new StringBuilder();
        messages.append("{\"role\":\"system\",\"content\":").append(jsonEscape(systemPrompt)).append("},");
        messages.append("{\"role\":\"user\",\"content\":").append(jsonEscape(userMessage)).append("}");

        String reqJson = "{\"model\":\"" + config.getModelName() + "\"," +
                "\"messages\":[" + messages + "]," +
                "\"max_tokens\":" + config.getMaxTokens() + "," +
                "\"temperature\":" + config.getTemperature();

        if (jsonMode) {
            reqJson += ",\"response_format\":{\"type\":\"json_object\"}";
        }
        reqJson += "}";

        Request request = new Request.Builder()
                .url(config.getBaseUrl() + "/v1/chat/completions")
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .post(RequestBody.create(reqJson, JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errBody = response.body() != null ? response.body().string() : "";
                throw new RuntimeException("AI 调用失败: HTTP " + response.code() + " " + errBody);
            }
            String body = response.body().string();
            return extractContent(body);
        } catch (IOException e) {
            throw new RuntimeException("AI 调用异常: " + e.getMessage());
        }
    }

    private String extractContent(String responseBody) {
        try {
            int idx = responseBody.indexOf("\"content\"");
            if (idx < 0) throw new RuntimeException("AI 返回格式异常");

            // 找到 "content" 后面的冒号，再找 content 值的开头引号
            int colon = responseBody.indexOf(':', idx + 9);
            int openingQuote = responseBody.indexOf('"', colon + 1);
            if (openingQuote < 0) throw new RuntimeException("AI 返回格式异常");

            int start = openingQuote + 1;
            int end = start;
            while (end < responseBody.length()) {
                char c = responseBody.charAt(end);
                if (c == '\\') { end += 2; continue; }
                if (c == '"') break;
                end++;
            }
            String content = responseBody.substring(start, end);
            return content.replace("\\n", "\n").replace("\\t", "\t").replace("\\\"", "\"");
        } catch (Exception e) {
            throw new RuntimeException("AI 响应解析失败: " + e.getMessage());
        }
    }

    private boolean containsJsonKeyword(String s) {
        if (s == null) return false;
        String lower = s.toLowerCase();
        return lower.contains("json");
    }

    private String jsonEscape(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
