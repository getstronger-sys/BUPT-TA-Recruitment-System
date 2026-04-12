package bupt.ta.service;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Sends plain-text emails using SMTP settings from system properties or environment variables.
 * Disabled by default until host/from are configured.
 */
public class EmailNotificationService {

    private static final Path LOCAL_MAIL_PROPERTIES = Paths.get("data", "mail.properties");

    public static class EmailSettings {
        private final String host;
        private final int port;
        private final String username;
        private final String password;
        private final String from;
        private final boolean enabled;
        private final boolean auth;
        private final boolean startTls;
        private final boolean ssl;
        private final String appBaseUrl;

        EmailSettings(String host, int port, String username, String password, String from, boolean enabled,
                      boolean auth, boolean startTls, boolean ssl, String appBaseUrl) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this.from = from;
            this.enabled = enabled;
            this.auth = auth;
            this.startTls = startTls;
            this.ssl = ssl;
            this.appBaseUrl = appBaseUrl;
        }

        public String getHost() { return host; }
        public int getPort() { return port; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getFrom() { return from; }
        public boolean isEnabled() { return enabled; }
        public boolean isAuth() { return auth; }
        public boolean isStartTls() { return startTls; }
        public boolean isSsl() { return ssl; }
        public String getAppBaseUrl() { return appBaseUrl; }

        public boolean isConfigured() {
            return enabled && !isBlank(host) && !isBlank(from);
        }
    }

    public static class SendResult {
        private final boolean success;
        private final String detail;

        SendResult(boolean success, String detail) {
            this.success = success;
            this.detail = detail;
        }

        public boolean isSuccess() { return success; }
        public String getDetail() { return detail; }
    }

    public EmailSettings loadSettings() {
        Properties localProperties = loadLocalProperties();
        String host = firstNonBlank(
                property(localProperties, "ta.mail.host"),
                System.getProperty("ta.mail.host"),
                System.getenv("TA_MAIL_HOST"));
        int port = parseInt(firstNonBlank(
                property(localProperties, "ta.mail.port"),
                System.getProperty("ta.mail.port"),
                System.getenv("TA_MAIL_PORT")), 587);
        String username = firstNonBlank(
                property(localProperties, "ta.mail.username"),
                System.getProperty("ta.mail.username"),
                System.getenv("TA_MAIL_USERNAME"));
        String password = firstNonBlank(
                property(localProperties, "ta.mail.password"),
                System.getProperty("ta.mail.password"),
                System.getenv("TA_MAIL_PASSWORD"));
        String from = firstNonBlank(
                property(localProperties, "ta.mail.from"),
                System.getProperty("ta.mail.from"),
                System.getenv("TA_MAIL_FROM"));
        boolean enabled = parseBoolean(firstNonBlank(
                property(localProperties, "ta.mail.enabled"),
                System.getProperty("ta.mail.enabled"),
                System.getenv("TA_MAIL_ENABLED")), true);
        boolean auth = parseBoolean(firstNonBlank(
                property(localProperties, "ta.mail.auth"),
                System.getProperty("ta.mail.auth"),
                System.getenv("TA_MAIL_AUTH")), !isBlank(username));
        boolean startTls = parseBoolean(firstNonBlank(
                property(localProperties, "ta.mail.starttls"),
                System.getProperty("ta.mail.starttls"),
                System.getenv("TA_MAIL_STARTTLS")), true);
        boolean ssl = parseBoolean(firstNonBlank(
                property(localProperties, "ta.mail.ssl"),
                System.getProperty("ta.mail.ssl"),
                System.getenv("TA_MAIL_SSL")), false);
        String appBaseUrl = trimToNull(firstNonBlank(
                property(localProperties, "ta.mail.appBaseUrl"),
                System.getProperty("ta.mail.appBaseUrl"),
                System.getenv("TA_MAIL_APP_BASE_URL")));

        return new EmailSettings(host, port, username, password, from, enabled, auth, startTls, ssl, appBaseUrl);
    }

    public boolean isConfigured() {
        return loadSettings().isConfigured();
    }

    public SendResult sendPlainText(String to, String subject, String body) {
        if (isBlank(to)) {
            return new SendResult(false, "Missing recipient email.");
        }

        EmailSettings settings = loadSettings();
        if (!settings.isConfigured()) {
            return new SendResult(false, "SMTP is not configured.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", settings.getHost());
        props.put("mail.smtp.port", String.valueOf(settings.getPort()));
        props.put("mail.smtp.auth", String.valueOf(settings.isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(settings.isStartTls()));
        if (settings.isSsl()) {
            props.put("mail.smtp.ssl.enable", "true");
        }

        Authenticator authenticator = null;
        if (settings.isAuth()) {
            final String username = settings.getUsername();
            final String password = settings.getPassword();
            authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            };
        }

        try {
            Session session = Session.getInstance(props, authenticator);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(settings.getFrom()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            message.setSubject(subject != null ? subject : "", StandardCharsets.UTF_8.name());
            message.setText(body != null ? body : "", StandardCharsets.UTF_8.name());
            Transport.send(message);
            return new SendResult(true, "sent");
        } catch (MessagingException e) {
            return new SendResult(false, e.getMessage());
        }
    }

    public String maybeAppendPortalLink(String body, String relativePath) {
        EmailSettings settings = loadSettings();
        if (!settings.isConfigured() || isBlank(settings.getAppBaseUrl()) || isBlank(relativePath)) {
            return body;
        }
        String base = settings.getAppBaseUrl().endsWith("/")
                ? settings.getAppBaseUrl().substring(0, settings.getAppBaseUrl().length() - 1)
                : settings.getAppBaseUrl();
        String path = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
        return body + "\n\nPortal: " + base + path;
    }

    private static int parseInt(String raw, int defaultValue) {
        if (raw == null || raw.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean parseBoolean(String raw, boolean defaultValue) {
        if (raw == null || raw.trim().isEmpty()) {
            return defaultValue;
        }
        String normalized = raw.trim().toLowerCase();
        if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized)) {
            return false;
        }
        return defaultValue;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String property(Properties properties, String key) {
        return properties != null ? properties.getProperty(key) : null;
    }

    private static Properties loadLocalProperties() {
        if (!Files.exists(LOCAL_MAIL_PROPERTIES)) {
            return new Properties();
        }
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(LOCAL_MAIL_PROPERTIES)) {
            properties.load(in);
            return properties;
        } catch (IOException e) {
            return new Properties();
        }
    }
}
