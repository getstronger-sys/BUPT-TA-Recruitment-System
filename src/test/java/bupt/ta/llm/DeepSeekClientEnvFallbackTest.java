package bupt.ta.llm;

import bupt.ta.model.AiApiSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeepSeekClientEnvFallbackTest {

    private static final String[] PROPS = {
            "TA_AI_ENABLED", "TA_AI_API_KEY", "TA_AI_BASE_URL", "TA_AI_MODEL", "TA_AI_PROVIDER",
            "LLM_ENABLED", "LLM_API_KEY", "LLM_BASE_URL", "LLM_MODEL", "LLM_PROVIDER",
            "MIMO_ENABLED", "MIMO_API_KEY", "MIMO_BASE_URL", "MIMO_MODEL", "MIMO_PROVIDER",
            "OPENAI_ENABLED", "OPENAI_API_KEY", "OPENAI_BASE_URL", "OPENAI_MODEL", "OPENAI_PROVIDER",
            "DEEPSEEK_ENABLED", "DEEPSEEK_API_KEY", "DEEPSEEK_API_BASE", "DEEPSEEK_MODEL",
            "llm.api.key", "llm.api.base", "llm.api.model",
            "mimo.api.key", "mimo.api.base", "mimo.api.model",
            "openai.api.key", "openai.api.base", "openai.api.model",
            "deepseek.api.key", "deepseek.api.base", "deepseek.api.model"
    };

    private final String[] saved = new String[PROPS.length];

    @Before
    public void saveProps() {
        for (int i = 0; i < PROPS.length; i++) {
            saved[i] = System.getProperty(PROPS[i]);
        }
    }

    @After
    public void restoreProps() {
        for (int i = 0; i < PROPS.length; i++) {
            if (saved[i] == null) {
                System.clearProperty(PROPS[i]);
            } else {
                System.setProperty(PROPS[i], saved[i]);
            }
        }
    }

    @Test
    public void usesAdminSettingsWhenKeyPresent() {
        clearProps();
        AiApiSettings admin = new AiApiSettings();
        admin.setApiEnabled(true);
        admin.setApiKey("sk-admin");
        admin.setBaseUrl("https://api.example.com");
        admin.setModel("admin-model");

        assertTrue(DeepSeekClient.fromRuntimeSettings(admin).isConfigured());
        assertEquals("sk-admin", DeepSeekClient.mergeWithEnvFallback(admin).getApiKey());
    }

    @Test
    public void fallsBackToEnvPropertiesWhenAdminKeyMissing() {
        clearProps();
        System.setProperty("TA_AI_ENABLED", "true");
        System.setProperty("TA_AI_API_KEY", "sk-from-env");
        System.setProperty("TA_AI_BASE_URL", "https://api.deepseek.com");
        System.setProperty("TA_AI_MODEL", "deepseek-chat");

        AiApiSettings admin = new AiApiSettings();
        assertFalse(admin.isEffectivelyConfigured());
        assertTrue(DeepSeekClient.isRuntimeConfigured(admin));

        AiApiSettings merged = DeepSeekClient.mergeWithEnvFallback(admin);
        assertEquals("sk-from-env", merged.getApiKey());
        assertEquals("deepseek-chat", merged.getModel());
    }

    @Test
    public void acceptsNonDeepSeekProviderWhenUsingOpenAiCompatibleEnv() {
        clearProps();
        System.setProperty("TA_AI_ENABLED", "true");
        System.setProperty("TA_AI_PROVIDER", "mimo");
        System.setProperty("TA_AI_API_KEY", "sk-mimo-through-ta");

        assertTrue(DeepSeekClient.isRuntimeConfigured(new AiApiSettings()));

        AiApiSettings merged = DeepSeekClient.mergeWithEnvFallback(new AiApiSettings());
        assertEquals("mimo", merged.getProvider());
        assertEquals("sk-mimo-through-ta", merged.getApiKey());
    }

    @Test
    public void fallsBackToMimoAndGenericModelProperties() {
        clearProps();
        System.setProperty("MIMO_API_KEY", "sk-from-mimo");
        System.setProperty("MIMO_BASE_URL", "https://token-plan-cn.xiaomimimo.com/v1");
        System.setProperty("LLM_MODEL", "mimo-v2.5-pro");

        AiApiSettings merged = DeepSeekClient.mergeWithEnvFallback(new AiApiSettings());

        assertTrue(DeepSeekClient.fromRuntimeSettings(merged).isConfigured());
        assertEquals("mimo", merged.getProvider());
        assertEquals("sk-from-mimo", merged.getApiKey());
        assertEquals("https://token-plan-cn.xiaomimimo.com/v1", merged.getBaseUrl());
        assertEquals("mimo-v2.5-pro", merged.getModel());
    }

    @Test
    public void genericDisabledFlagStopsEnvFallback() {
        clearProps();
        System.setProperty("LLM_ENABLED", "false");
        System.setProperty("LLM_API_KEY", "sk-disabled");

        assertFalse(DeepSeekClient.isRuntimeConfigured(new AiApiSettings()));
    }

    @Test
    public void remainsUnconfiguredWhenNeitherAdminNorEnv() {
        clearProps();
        assertFalse(DeepSeekClient.isRuntimeConfigured(new AiApiSettings()));
    }

    private static void clearProps() {
        for (String prop : PROPS) {
            System.clearProperty(prop);
        }
    }
}
