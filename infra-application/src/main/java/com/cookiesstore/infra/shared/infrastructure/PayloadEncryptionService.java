package com.cookiesstore.infra.shared.infrastructure;

import com.cookiesstore.infra.config.InfraEncryptionProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(InfraEncryptionProperties.class)
public class PayloadEncryptionService {

    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;
    private final InfraEncryptionProperties encryptionProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public PayloadEncryptionService(InfraEncryptionProperties encryptionProperties) {
        this.encryptionProperties = encryptionProperties;
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(resolveKey(), "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] output = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, output, 0, iv.length);
            System.arraycopy(cipherText, 0, output, iv.length, cipherText.length);
            return Base64.getEncoder().encodeToString(output);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not encrypt provider credentials payload", exception);
        }
    }

    public String decrypt(String encryptedPayload) {
        try {
            byte[] input = Base64.getDecoder().decode(encryptedPayload);
            if (input.length <= GCM_IV_BYTES) {
                throw new IllegalArgumentException("Invalid encrypted payload");
            }
            byte[] iv = new byte[GCM_IV_BYTES];
            byte[] cipherText = new byte[input.length - GCM_IV_BYTES];
            System.arraycopy(input, 0, iv, 0, iv.length);
            System.arraycopy(input, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(resolveKey(), "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] clear = cipher.doFinal(cipherText);
            return new String(clear, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not decrypt provider credentials payload", exception);
        }
    }

    private byte[] resolveKey() throws Exception {
        String raw = encryptionProperties.encryptionKey();
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("infra.security.encryption-key is required");
        }
        if (raw.startsWith("base64:")) {
            byte[] decoded = Base64.getDecoder().decode(raw.substring("base64:".length()));
            if (decoded.length != 32) {
                throw new IllegalStateException("Base64 encryption key must decode to 32 bytes");
            }
            return decoded;
        }
        // Derive a stable 32-byte key from plaintext passphrase.
        return MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
    }
}
