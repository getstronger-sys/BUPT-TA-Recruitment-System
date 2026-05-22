package bupt.ta.llm;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Unit tests for {@link DeepSeekClient} configuration helpers. */
public class DeepSeekClientTest {

    @Test
    public void unconfiguredClientHasNoKey() {
        DeepSeekClient c = new DeepSeekClient("", DeepSeekClient.DEFAULT_BASE_URL, DeepSeekClient.DEFAULT_MODEL);
        assertFalse(c.isConfigured());
    }

    @Test
    public void configuredWhenKeyPresent() {
        DeepSeekClient c = new DeepSeekClient("sk-test", DeepSeekClient.DEFAULT_BASE_URL, DeepSeekClient.DEFAULT_MODEL);
        assertTrue(c.isConfigured());
    }

    @Test
    public void normalizesBaseUrlThatAlreadyContainsVersionPath() {
        DeepSeekClient c = new DeepSeekClient(
                "sk-test",
                "https://token-plan-cn.xiaomimimo.com/v1",
                "mimo-v2.5-pro"
        );

        assertTrue(c.isConfigured());
        assertTrue(c.chatCompletionsUrl().endsWith("/v1/chat/completions"));
        assertFalse(c.chatCompletionsUrl().contains("/v1/v1/"));
    }

    @Test
    public void normalizesBaseUrlThatAlreadyContainsChatEndpoint() {
        DeepSeekClient c = new DeepSeekClient(
                "sk-test",
                "https://token-plan-cn.xiaomimimo.com/v1/chat/completions/",
                "mimo-v2.5-pro"
        );

        assertTrue(c.isConfigured());
        assertTrue(c.chatCompletionsUrl().endsWith("/v1/chat/completions"));
        assertFalse(c.chatCompletionsUrl().contains("/v1/chat/completions/v1/chat/completions"));
    }

    @Test
    public void blankBaseUrlStillUsesDefaultEndpoint() {
        DeepSeekClient c = new DeepSeekClient("sk-test", "   ", DeepSeekClient.DEFAULT_MODEL);

        assertTrue(c.isConfigured());
        assertTrue(c.chatCompletionsUrl().startsWith(DeepSeekClient.DEFAULT_BASE_URL));
        assertTrue(c.chatCompletionsUrl().endsWith("/v1/chat/completions"));
    }
}
