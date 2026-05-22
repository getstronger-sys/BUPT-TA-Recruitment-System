package bupt.ta.llm;

import bupt.ta.model.AiApiSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * OpenAI-compatible chat API client (POST /v1/chat/completions).
 * <p>
 * Configuration resolution (highest priority first):
 * <ol>
 *   <li>Admin-managed JSON ({@code data/ai-api-settings.json}) when enabled and a key is set.</li>
 *   <li>Environment variables / JVM properties ({@code TA_AI_*}, {@code LLM_*},
 *       {@code MIMO_*}, {@code OPENAI_*}, {@code DEEPSEEK_*}) via
 *       {@link #fromRuntimeSettings(AiApiSettings)} — used when admin settings are empty so
 *       local {@code ai.env} + {@code run-with-ai.ps1} work without re-entering the key in Admin UI.</li>
 *   <li>No-arg {@link #DeepSeekClient()} for tests and CLI (env only).</li>
 * </ol>
 * Never commit API keys; use env vars, CI secrets, or the admin settings file.
 */
public final class DeepSeekClient {

    public static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    public static final String DEFAULT_MODEL = "deepseek-chat";
    private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";

    private final boolean enabled;
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final HttpClient httpClient;

    /**
     * Environment-driven construction. Reads {@code TA_AI_*}, generic {@code LLM_*},
     * provider-specific env vars, and matching JVM system properties.
     * Used by integration tests and stand-alone CLIs.
     */
    public DeepSeekClient() {
        this(
                isEnabledFromEnv(),
                resolveApiKey(),
                resolveBaseUrl(),
                resolveModel()
        );
    }

    /**
     * Explicit configuration; the client is treated as enabled regardless of environment.
     */
    public DeepSeekClient(String apiKey, String baseUrl, String model) {
        this(true, apiKey, baseUrl, model);
    }

    /**
     * Full constructor allowing an explicit enabled flag, used by
     * {@link #fromAdminSettings(AiApiSettings)} so the admin toggle bypasses env-var checks.
     */
    public DeepSeekClient(boolean enabled, String apiKey, String baseUrl, String model) {
        this.enabled = enabled;
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.model = model != null && !model.trim().isEmpty() ? model.trim() : DEFAULT_MODEL;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Build a client from admin JSON only (no env fallback). Used by the admin settings form
     * when displaying what is stored on disk.
     */
    public static DeepSeekClient fromAdminSettings(AiApiSettings settings) {
        if (settings == null) {
            return new DeepSeekClient(false, "", "", "");
        }
        return new DeepSeekClient(
                settings.isApiEnabled(),
                settings.getApiKey(),
                settings.getBaseUrl(),
                settings.getModel()
        );
    }

    /**
     * Resolve admin settings, falling back to environment variables when admin has no API key.
     * This is the entry point for all servlet LLM calls.
     */
    public static DeepSeekClient fromRuntimeSettings(AiApiSettings stored) {
        return fromAdminSettings(mergeWithEnvFallback(stored));
    }

    /**
     * @return {@code true} when {@link #fromRuntimeSettings(AiApiSettings)} would be able to call the API
     */
    public static boolean isRuntimeConfigured(AiApiSettings stored) {
        return fromRuntimeSettings(stored).isConfigured();
    }

    /**
     * Copy admin settings and fill missing key/URL/model from the environment when allowed.
     */
    static AiApiSettings mergeWithEnvFallback(AiApiSettings stored) {
        if (stored != null && stored.isEffectivelyConfigured()) {
            return stored;
        }
        if (!isEnabledFromEnv()) {
            return stored != null ? stored : new AiApiSettings();
        }
        String envKey = resolveApiKey();
        if (envKey.isEmpty()) {
            return stored != null ? stored : new AiApiSettings();
        }
        AiApiSettings merged = stored != null ? copySettings(stored) : new AiApiSettings();
        merged.setApiEnabled(true);
        if (merged.getApiKey() == null || merged.getApiKey().trim().isEmpty()) {
            merged.setApiKey(envKey);
        }
        if (merged.getBaseUrl() == null || merged.getBaseUrl().trim().isEmpty()) {
            merged.setBaseUrl(resolveBaseUrl());
        }
        if (merged.getModel() == null || merged.getModel().trim().isEmpty()) {
            merged.setModel(resolveModel());
        }
        String envProvider = resolveProviderLabel();
        String currentProvider = merged.getProvider() != null ? merged.getProvider().trim() : "";
        if (currentProvider.isEmpty()
                || ("deepseek".equalsIgnoreCase(currentProvider) && !"deepseek".equalsIgnoreCase(envProvider))) {
            merged.setProvider(envProvider);
        }
        return merged;
    }

    private static AiApiSettings copySettings(AiApiSettings source) {
        AiApiSettings copy = new AiApiSettings();
        copy.setApiEnabled(source.isApiEnabled());
        copy.setStreamingEnabled(source.isStreamingEnabled());
        copy.setProvider(source.getProvider());
        copy.setBaseUrl(source.getBaseUrl());
        copy.setModel(source.getModel());
        copy.setApiKey(source.getApiKey());
        return copy;
    }

    /**
     * @return {@code true} when enabled and a non-blank API key is configured
     */
    public boolean isConfigured() {
        return enabled && !apiKey.isEmpty();
    }

    private static boolean isEnabledFromEnv() {
        String enabled = firstNonBlank(
                envOrProperty("TA_AI_ENABLED", "TA_AI_ENABLED"),
                envOrProperty("LLM_ENABLED", "LLM_ENABLED"),
                envOrProperty("MIMO_ENABLED", "MIMO_ENABLED"),
                envOrProperty("OPENAI_ENABLED", "OPENAI_ENABLED"),
                envOrProperty("DEEPSEEK_ENABLED", "DEEPSEEK_ENABLED")
        );
        if (!enabled.isEmpty()
                && ("false".equalsIgnoreCase(enabled) || "0".equals(enabled) || "off".equalsIgnoreCase(enabled))) {
            return false;
        }
        return true;
    }

    private static String resolveBaseUrl() {
        return firstNonBlank(
                envOrProperty("TA_AI_BASE_URL", "TA_AI_BASE_URL"),
                envOrProperty("LLM_BASE_URL", "LLM_BASE_URL"),
                envOrProperty("MIMO_BASE_URL", "MIMO_BASE_URL"),
                envOrProperty("OPENAI_BASE_URL", "OPENAI_BASE_URL"),
                envOrProperty("DEEPSEEK_API_BASE", "DEEPSEEK_API_BASE"),
                System.getProperty("llm.api.base"),
                System.getProperty("mimo.api.base"),
                System.getProperty("openai.api.base"),
                System.getProperty("deepseek.api.base"),
                DEFAULT_BASE_URL);
    }

    private static String resolveModel() {
        return firstNonBlank(
                envOrProperty("TA_AI_MODEL", "TA_AI_MODEL"),
                envOrProperty("LLM_MODEL", "LLM_MODEL"),
                envOrProperty("MIMO_MODEL", "MIMO_MODEL"),
                envOrProperty("OPENAI_MODEL", "OPENAI_MODEL"),
                envOrProperty("DEEPSEEK_MODEL", "DEEPSEEK_MODEL"),
                System.getProperty("llm.api.model"),
                System.getProperty("mimo.api.model"),
                System.getProperty("openai.api.model"),
                System.getProperty("deepseek.api.model"),
                DEFAULT_MODEL);
    }

    private static String resolveProviderLabel() {
        String explicit = firstNonBlank(
                envOrProperty("TA_AI_PROVIDER", "TA_AI_PROVIDER"),
                envOrProperty("LLM_PROVIDER", "LLM_PROVIDER"),
                envOrProperty("MIMO_PROVIDER", "MIMO_PROVIDER"),
                envOrProperty("OPENAI_PROVIDER", "OPENAI_PROVIDER"),
                envOrProperty("DEEPSEEK_PROVIDER", "DEEPSEEK_PROVIDER")
        );
        if (!explicit.isEmpty()) {
            return explicit;
        }
        if (hasAnySetting("MIMO_API_KEY", "MIMO_BASE_URL", "MIMO_MODEL",
                "mimo.api.key", "mimo.api.base", "mimo.api.model")) {
            return "mimo";
        }
        if (hasAnySetting("OPENAI_API_KEY", "OPENAI_BASE_URL", "OPENAI_MODEL",
                "openai.api.key", "openai.api.base", "openai.api.model")) {
            return "openai";
        }
        return "deepseek";
    }

    private static String resolveApiKey() {
        return firstNonBlank(
                envOrProperty("TA_AI_API_KEY", "TA_AI_API_KEY"),
                envOrProperty("LLM_API_KEY", "LLM_API_KEY"),
                envOrProperty("MIMO_API_KEY", "MIMO_API_KEY"),
                envOrProperty("OPENAI_API_KEY", "OPENAI_API_KEY"),
                envOrProperty("DEEPSEEK_API_KEY", "DEEPSEEK_API_KEY"),
                System.getProperty("llm.api.key"),
                System.getProperty("mimo.api.key"),
                System.getProperty("openai.api.key"),
                System.getProperty("deepseek.api.key")
        );
    }

    private static String envOrProperty(String envName, String propertyName) {
        return firstNonBlank(System.getenv(envName), System.getProperty(propertyName));
    }

    private static boolean hasAnySetting(String... names) {
        if (names == null) {
            return false;
        }
        for (String name : names) {
            if (name == null || name.trim().isEmpty()) {
                continue;
            }
            if (!firstNonBlank(System.getenv(name), System.getProperty(name)).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Single user message with optional system prompt.
     *
     * @param systemPrompt optional system role content (may be null)
     * @param userMessage  required user message
     * @return assistant reply text
     * @throws IllegalStateException if not configured
     * @throws IOException          on HTTP or API errors
     */
    public String chat(String systemPrompt, String userMessage) throws IOException {
        Objects.requireNonNull(userMessage, "userMessage");
        JsonArray messages = new JsonArray();
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            JsonObject sys = new JsonObject();
            sys.addProperty("role", "system");
            sys.addProperty("content", systemPrompt.trim());
            messages.add(sys);
        }
        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", userMessage);
        messages.add(user);
        return chatCompletions(messages);
    }

    /**
     * Sends a chat completion request with full control over the messages array.
     *
     * @param messages OpenAI-format message array (roles and content)
     * @return assistant reply text from the first choice
     * @throws IllegalStateException if not configured
     * @throws IOException          on HTTP or API errors
     */
    public String chatCompletions(JsonArray messages) throws IOException {
        if (!isConfigured()) {
            throw new IllegalStateException("LLM API key is not set; cannot call the chat API.");
        }
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.add("messages", messages);
        body.addProperty("temperature", 0.3);

        String url = chatCompletionsUrl();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("LLM request interrupted", e);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("LLM HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
        if (root.has("error")) {
            throw new IOException("LLM API error: " + root.get("error"));
        }
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.size() == 0) {
            throw new IOException("LLM empty choices: " + response.body());
        }
        JsonObject first = choices.get(0).getAsJsonObject();
        JsonObject msg = first.getAsJsonObject("message");
        if (msg == null || !msg.has("content")) {
            throw new IOException("LLM unexpected response: " + response.body());
        }
        return msg.get("content").getAsString();
    }

    /**
     * Streaming counterpart of {@link #chat(String, String)}. Pushes each non-empty content
     * delta from the OpenAI-compatible SSE response to {@code onChunk}. Returns the
     * concatenation of all delivered chunks so callers can also persist or log the full text.
     * The consumer is not called for the final {@code [DONE]} sentinel.
     *
     * @throws IllegalStateException if not configured
     * @throws IOException          HTTP, parsing, or upstream API errors
     */
    public String chatStream(String systemPrompt, String userMessage, Consumer<String> onChunk) throws IOException {
        Objects.requireNonNull(userMessage, "userMessage");
        Objects.requireNonNull(onChunk, "onChunk");
        if (!isConfigured()) {
            throw new IllegalStateException("LLM API key is not set; cannot call the chat API.");
        }

        JsonArray messages = new JsonArray();
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            JsonObject sys = new JsonObject();
            sys.addProperty("role", "system");
            sys.addProperty("content", systemPrompt.trim());
            messages.add(sys);
        }
        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", userMessage);
        messages.add(user);

        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.add("messages", messages);
        body.addProperty("temperature", 0.3);
        body.addProperty("stream", true);

        String url = chatCompletionsUrl();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "text/event-stream")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<InputStream> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("LLM streaming request interrupted", e);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("LLM HTTP " + response.statusCode() + " on streaming request");
        }

        StringBuilder fullText = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data:")) {
                    continue;
                }
                String payload = line.substring(5).trim();
                if (payload.isEmpty() || "[DONE]".equals(payload)) {
                    if ("[DONE]".equals(payload)) {
                        break;
                    }
                    continue;
                }
                String chunk = extractDeltaContent(payload);
                if (chunk != null && !chunk.isEmpty()) {
                    fullText.append(chunk);
                    onChunk.accept(chunk);
                }
            }
        }
        return fullText.toString();
    }

    private static String extractDeltaContent(String jsonPayload) throws IOException {
        try {
            JsonObject root = JsonParser.parseString(jsonPayload).getAsJsonObject();
            if (root.has("error")) {
                throw new IOException("LLM API error: " + root.get("error"));
            }
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) {
                return null;
            }
            JsonObject first = choices.get(0).getAsJsonObject();
            if (!first.has("delta")) {
                return null;
            }
            JsonObject delta = first.getAsJsonObject("delta");
            if (delta == null || !delta.has("content") || delta.get("content").isJsonNull()) {
                return null;
            }
            return delta.get("content").getAsString();
        } catch (IllegalStateException | com.google.gson.JsonSyntaxException e) {
            throw new IOException("Failed to parse LLM SSE payload: " + jsonPayload, e);
        }
    }

    static String normalizeBaseUrl(String rawBaseUrl) {
        String normalized = trimTrailingSlash(rawBaseUrl != null && !rawBaseUrl.trim().isEmpty()
                ? rawBaseUrl.trim() : DEFAULT_BASE_URL);
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (lower.endsWith(CHAT_COMPLETIONS_PATH)) {
            normalized = normalized.substring(0, normalized.length() - CHAT_COMPLETIONS_PATH.length());
        } else if (lower.endsWith("/v1")) {
            normalized = normalized.substring(0, normalized.length() - "/v1".length());
        }
        return trimTrailingSlash(normalized);
    }

    String chatCompletionsUrl() {
        return baseUrl + CHAT_COMPLETIONS_PATH;
    }

    private static String trimTrailingSlash(String u) {
        if (u == null || u.length() <= 1) {
            return u;
        }
        return u.endsWith("/") ? u.substring(0, u.length() - 1) : u;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
        }
        return "";
    }
}
