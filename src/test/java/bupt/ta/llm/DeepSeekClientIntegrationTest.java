package bupt.ta.llm;

import org.junit.Assume;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * Run with {@code DEEPSEEK_API_KEY} set to verify network + key (optional).
 * If the key is not set, the test is skipped (does not fail the build).
 */
public class DeepSeekClientIntegrationTest {

    @Test
    public void chatReturnsNonEmptyWhenConfigured() throws Exception {
        DeepSeekClient client = new DeepSeekClient();
        Assume.assumeTrue("Set environment variable DEEPSEEK_API_KEY to run this test", client.isConfigured());

        String reply = client.chat(
                "Reply with exactly one word: OK",
                "ping"
        );
        assertFalse(reply == null || reply.trim().isEmpty());
    }
}
