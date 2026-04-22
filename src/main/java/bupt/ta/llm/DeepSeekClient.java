package bupt.ta.llm;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/**
 * DeepSeek Chat API (OpenAI-compatible: POST /v1/chat/completions).
 * <p>
 * Configure via environment (recommended) or JVM system properties:
 * <ul>
 *   <li>{@code DEEPSEEK_API_KEY} — required for real calls</li>
 *   <li>{@code DEEPSEEK_API_BASE} — default {@value #DEFAULT_BASE_URL}</li>
 *   <li>{@code DEEPSEEK_MODEL} — default {@value #DEFAULT_MODEL}</li>
 * </ul>
 * Never commit API keys; use env vars or CI secrets.
 */
public final class DeepSeekClient {

    public static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    public static final String DEFAULT_MODEL = "deepseek-chat";

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final HttpClient httpClient;

    public DeepSeekClient() {
        this(
                resolveApiKey(),
                firstNonBlank(System.getenv("TA_AI_BASE_URL"), System.getenv("DEEPSEEK_API_BASE"),
                        System.getProperty("deepseek.api.base"), DEFAULT_BASE_URL),
                firstNonBlank(System.getenv("TA_AI_MODEL"), System.getenv("DEEPSEEK_MODEL"),
                        System.getProperty("deepseek.api.model"), DEFAULT_MODEL)
        );
    }

    public DeepSeekClient(String apiKey, String baseUrl, String model) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.baseUrl = trimTrailingSlash(baseUrl != null && !baseUrl.isEmpty() ? baseUrl : DEFAULT_BASE_URL);
        this.model = model != null && !model.isEmpty() ? model : DEFAULT_MODEL;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * @return true if a non-blank API key is configured
     */
    public boolean isConfigured() {
        return isEnabled() && !apiKey.isEmpty();
    }

    private static boolean isEnabled() {
        String enabled = firstNonBlank(System.getenv("TA_AI_ENABLED"));
        if (!enabled.isEmpty()
                && ("false".equalsIgnoreCase(enabled) || "0".equals(enabled) || "off".equalsIgnoreCase(enabled))) {
            return false;
        }
        String provider = firstNonBlank(System.getenv("TA_AI_PROVIDER"));
        return provider.isEmpty() || "deepseek".equalsIgnoreCase(provider);
    }

    private static String resolveApiKey() {
        return firstNonBlank(
                System.getenv("TA_AI_API_KEY"),
                System.getenv("DEEPSEEK_API_KEY"),
                System.getProperty("deepseek.api.key")
        );
    }

    /**
     * Single user message, optional system prompt.
     *
     * @throws IllegalStateException if not configured
     * @throws IOException          HTTP or API errors
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
     * Full control over the messages array (e.g. multi-turn).
     */
    public String chatCompletions(JsonArray messages) throws IOException {
        if (!isConfigured()) {
            throw new IllegalStateException("DEEPSEEK_API_KEY is not set; cannot call DeepSeek API.");
        }
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.add("messages", messages);
        body.addProperty("temperature", 0.3);

        String url = baseUrl + "/v1/chat/completions";
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
            throw new IOException("DeepSeek request interrupted", e);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("DeepSeek HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
        if (root.has("error")) {
            throw new IOException("DeepSeek API error: " + root.get("error"));
        }
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.size() == 0) {
            throw new IOException("DeepSeek empty choices: " + response.body());
        }
        JsonObject first = choices.get(0).getAsJsonObject();
        JsonObject msg = first.getAsJsonObject("message");
        if (msg == null || !msg.has("content")) {
            throw new IOException("DeepSeek unexpected response: " + response.body());
        }
        return msg.get("content").getAsString();
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
