package com.emailagent.config;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Holds all LLM provider config values.
 * Actual HTTP calls are made via OkHttpClient in LlmEmailGeneratorAgent.
 */
@Configuration
public class OpenAiConfig {

    @Value("${llm.provider}")
    private String provider;

    // Groq
    @Value("${groq.api.key}")
    private String groqApiKey;
    @Value("${groq.api.url}")
    private String groqApiUrl;
    @Value("${groq.model}")
    private String groqModel;

    // Gemini
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    @Value("${gemini.api.url}")
    private String geminiApiUrl;
    @Value("${gemini.model}")
    private String geminiModel;

    // Ollama
    @Value("${ollama.api.url}")
    private String ollamaApiUrl;
    @Value("${ollama.model}")
    private String ollamaModel;

    public String getProvider()     { return provider; }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    public String getGroqApiKey()   { return groqApiKey; }
    public String getGroqApiUrl()   { return groqApiUrl; }
    public String getGroqModel()    { return groqModel; }
    public String getGeminiApiKey() { return geminiApiKey; }
    public String getGeminiApiUrl() { return geminiApiUrl; }
    public String getGeminiModel()  { return geminiModel; }
    public String getOllamaApiUrl() { return ollamaApiUrl; }
    public String getOllamaModel()  { return ollamaModel; }
}
