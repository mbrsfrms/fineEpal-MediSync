package com.medisync.app.manager;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Salted PBKDF2 password hashing. Stored format:
 * {@code pbkdf2$<sha256|sha1>$<iterations>$<saltHex>$<hashHex>}.
 * SHA-256 PBKDF2 is not available on every Android API level (minSdk 23), so SHA-1 is used as a fallback.
 * Legacy hashes from the earlier app (unsalted SHA-256) are still verified so existing users can log in once,
 * after which AuthManager upgrades their stored hash.
 */
public final class PasswordHasher {
    private static final int ITERATIONS = 20000;
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final String PREFIX = "pbkdf2$";

    private PasswordHasher() { }

    public static String hash(String password) {
        try {
            byte[] salt = new byte[SALT_BYTES];
            new SecureRandom().nextBytes(salt);
            String tag = preferredTag();
            byte[] derived = derive(password, salt, ITERATIONS, tag);
            return PREFIX + tag + "$" + ITERATIONS + "$" + toHex(salt) + "$" + toHex(derived);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is not available on this device.", e);
        }
    }

    /** True when the stored value matches the password. Handles both current and legacy formats. */
    public static boolean verify(String password, String stored) {
        if (password == null || stored == null || stored.isEmpty()) return false;
        try {
            if (stored.startsWith(PREFIX)) {
                String[] parts = stored.split("\\$");
                if (parts.length != 5) return false;
                int iterations = Integer.parseInt(parts[2]);
                byte[] salt = fromHex(parts[3]);
                byte[] expected = fromHex(parts[4]);
                byte[] actual = derive(password, salt, iterations, parts[1]);
                return MessageDigest.isEqual(expected, actual);
            }
            return MessageDigest.isEqual(legacyHash(password).getBytes(StandardCharsets.UTF_8),
                    stored.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    /** True when the stored hash was created by the earlier unsalted scheme and should be upgraded. */
    public static boolean isLegacy(String stored) {
        return stored != null && !stored.startsWith(PREFIX);
    }

    /** The earlier unsalted SHA-256 scheme. Only used to verify and migrate old accounts. */
    public static String legacyHash(String password) throws GeneralSecurityException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(("medisync:" + password).getBytes(StandardCharsets.UTF_8));
        return toHex(digest);
    }

    private static String preferredTag() {
        try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return "sha256";
        } catch (GeneralSecurityException e) {
            return "sha1";
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations, String tag) throws GeneralSecurityException {
        String algorithm = "sha256".equals(tag) ? "PBKDF2WithHmacSHA256" : "PBKDF2WithHmacSHA1";
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(algorithm).generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    static byte[] fromHex(String hex) {
        if (hex.length() % 2 != 0) throw new IllegalArgumentException("Bad hex");
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++) out[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        return out;
    }
}