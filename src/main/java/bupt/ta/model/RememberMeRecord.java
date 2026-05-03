package bupt.ta.model;

/**
 * Server-side record for persistent "remember me" login tokens.
 */
public class RememberMeRecord {
    private String tokenHash;
    private String userId;
    private long expiresAtMillis;

    public RememberMeRecord() {}

    public RememberMeRecord(String tokenHash, String userId, long expiresAtMillis) {
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.expiresAtMillis = expiresAtMillis;
    }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public long getExpiresAtMillis() { return expiresAtMillis; }
    public void setExpiresAtMillis(long expiresAtMillis) { this.expiresAtMillis = expiresAtMillis; }
}
