package com.emailagent.agent;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.emailagent.config.OpenAiConfig;
import com.emailagent.model.EmailTemplateRequest;
import com.emailagent.model.GeneratedEmailContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * LLM Agent - Supports Groq, Gemini, and Ollama (all free).
 * Controlled by llm.provider in application.properties.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmEmailGeneratorAgent {

    private final OpenAiConfig config;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public GeneratedEmailContent generateEmail(EmailTemplateRequest request) {
        log.info("[LLM Agent] Provider: {} | Topic: '{}'", config.getProvider(), request.getTopic());

        String prompt = buildPrompt(request);

        String rawResponse = switch (config.getProvider().toLowerCase()) {
            case "groq"   -> callGroq(prompt);
            case "gemini" -> callGemini(prompt);
            case "ollama" -> callOllama(prompt);
            default -> throw new RuntimeException("Unknown LLM provider: " + config.getProvider()
                    + ". Valid values: groq, gemini, ollama");
        };

        return parseEmailFromResponse(rawResponse, request);
    }

    // ─── Prompt ──────────────────────────────────────────────────────────────

    private String buildPrompt(EmailTemplateRequest request) {
        return String.format("""
                You are a professional email writer. Generate a complete, warm, and professional email.

                REQUEST DETAILS:
                - Topic / Purpose    : %s
                - User's Description : %s
                - Sender Name        : %s
                - Sender Phone       : %s

                INSTRUCTIONS:
                1. Generate a clear SUBJECT LINE matching the topic.
                2. Write a complete email BODY that:
                   - Opens with a proper salutation addressing by name-{{RECIPIENT_NAME}}
                   - Covers the intent described above naturally and warmly
                   - Uses a professional yet friendly tone
                   - Ends with a warm closing signed by %s
                3. Return ONLY in this exact format, nothing else:

                SUBJECT: <subject line here>

                BODY:
                <full email body here> but in RECIPIENT Name add place holder so that i can be swapper later in java code example - Dear {{RECIPIENT_NAME}},
                """,
                request.getTopic(), request.getDescription(),
                request.getMyName(),
                request.getMyNumber() != null ? request.getMyNumber() : "N/A",
                request.getMyName()
        );
    }

    // ─── Groq (OpenAI-compatible, free) ──────────────────────────────────────

    private String callGroq(String prompt) {
        log.info("[LLM Agent] Calling Groq | model: {}", config.getGroqModel());

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", config.getGroqModel());
        body.put("max_tokens", 1024);
        ArrayNode messages = body.putArray("messages");
        messages.addObject().put("role", "user").put("content", prompt);

        String responseBody = doPost(config.getGroqApiUrl(), body.toString(),
                "Authorization", "Bearer " + config.getGroqApiKey());

        try {
            return objectMapper.readTree(responseBody)
                    .path("choices").get(0)
                    .path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Groq response: " + responseBody, e);
        }
    }

    // ─── Gemini (Free tier - 1500 req/day) ───────────────────────────────────

    private String callGemini(String prompt) {
        log.info("[LLM Agent] Calling Gemini | model: {}", config.getGeminiModel());

        String url = config.getGeminiApiUrl() + "/" + config.getGeminiModel()
                + ":generateContent?key=" + config.getGeminiApiKey();

        ObjectNode body = objectMapper.createObjectNode();
        ArrayNode contents = body.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        String responseBody = doPost(url, body.toString(), null, null);

        try {
            return objectMapper.readTree(responseBody)
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + responseBody, e);
        }
    }

    // ─── Ollama (100% local, no key needed) ──────────────────────────────────

    private String callOllama(String prompt) {
        log.info("[LLM Agent] Calling Ollama (local) | model: {}", config.getOllamaModel());

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", config.getOllamaModel());
        body.put("stream", false);
        ArrayNode messages = body.putArray("messages");
        messages.addObject().put("role", "user").put("content", prompt);

        String responseBody = doPost(config.getOllamaApiUrl(), body.toString(), null, null);

        try {
            return objectMapper.readTree(responseBody)
                    .path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Ollama response: " + responseBody, e);
        }
    }

    // ─── HTTP Helper ─────────────────────────────────────────────────────────

    private String doPost(String url, String jsonBody, String authHeader, String authValue) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, JSON))
                .addHeader("Content-Type", "application/json");

        if (authHeader != null) builder.addHeader(authHeader, authValue);

        try (Response response = okHttpClient.newCall(builder.build()).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new RuntimeException("LLM API error " + response.code() + ": " + body);
            }
            return body;
        } catch (IOException e) {
            throw new RuntimeException("HTTP call failed: " + e.getMessage(), e);
        }
    }

    // ─── Parse Response ───────────────────────────────────────────────────────

    private GeneratedEmailContent parseEmailFromResponse(String llmText, EmailTemplateRequest request) {
        String subject = "";
        String body = "";
        try {
            String[] lines = llmText.split("\n");
            StringBuilder bodyBuilder = new StringBuilder();
            boolean inBody = false;
            for (String line : lines) {
                if (line.startsWith("SUBJECT:")) {
                    subject = line.substring("SUBJECT:".length()).trim();
                } else if (line.startsWith("BODY:")) {
                    inBody = true;
                } else if (inBody) {
                    bodyBuilder.append(line).append("\n");
                }
            }
            body = bodyBuilder.toString().trim();
            if (subject.isBlank()) subject = request.getTopic();
            if (body.isBlank())    body    = llmText.trim();
        } catch (Exception e) {
            subject = request.getTopic();
            body    = llmText.trim();
        }

        log.info("[LLM Agent] ✅ Email generated. Subject: '{}'", subject);
        return GeneratedEmailContent.builder()
                .subject(subject).body(body)
                .build();
    }
}
