package bupt.ta.model;

/**
 * Persisted email OTP record (for verification codes).
 */
public class EmailOtpRecord {
    private String id;
    private String email;
    private String purpose;
    private String salt;
    private String codeHash;
    private String createdAt;
    private String expiresAt;
    private String consumedAt;
    private int attempts;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public String getConsumedAt() { return consumedAt; }
    public void setConsumedAt(String consumedAt) { this.consumedAt = consumedAt; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
}

