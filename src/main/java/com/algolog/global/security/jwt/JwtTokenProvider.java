package com.algolog.global.security.jwt;

import com.algolog.domain.user.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Pattern SUBJECT_PATTERN = Pattern.compile("\"sub\"\\s*:\\s*\"(\\d+)\"");
    private static final Pattern EXPIRATION_PATTERN = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");

    private final String secret;
    private final long accessTokenExpirationMillis;
    private final Clock clock;

    public JwtTokenProvider(
        @Value("${algolog.jwt.secret}") String secret,
        @Value("${algolog.jwt.access-token-expiration-millis}") long accessTokenExpirationMillis
    ) {
        this.secret = secret;
        this.accessTokenExpirationMillis = accessTokenExpirationMillis;
        this.clock = Clock.systemUTC();
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plusMillis(accessTokenExpirationMillis);

        String payload = "{"
            + "\"sub\":\"" + user.getId() + "\","
            + "\"email\":\"" + escapeJson(user.getEmail()) + "\","
            + "\"iat\":" + now.getEpochSecond() + ","
            + "\"exp\":" + expiresAt.getEpochSecond()
            + "}";

        String encodedHeader = base64UrlEncode(JWT_HEADER.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));
        String unsignedToken = encodedHeader + "." + encodedPayload;
        String signature = base64UrlEncode(sign(unsignedToken));

        return unsignedToken + "." + signature;
    }

    public Optional<Long> getUserId(String token) {
        if (!isValid(token)) {
            return Optional.empty();
        }

        String payload = decodePayload(token);
        Matcher matcher = SUBJECT_PATTERN.matcher(payload);
        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(Long.parseLong(matcher.group(1)));
    }

    public boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = base64UrlEncode(sign(unsignedToken));
        if (!constantTimeEquals(expectedSignature, parts[2])) {
            return false;
        }

        return !isExpired(decodePayload(token));
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(keySpec);
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT 서명 생성에 실패했습니다.", exception);
        }
    }

    private String base64UrlEncode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private String decodePayload(String token) {
        String[] parts = token.split("\\.");
        byte[] decodedPayload = Base64.getUrlDecoder().decode(parts[1]);
        return new String(decodedPayload, StandardCharsets.UTF_8);
    }

    private boolean isExpired(String payload) {
        Matcher matcher = EXPIRATION_PATTERN.matcher(payload);
        if (!matcher.find()) {
            return true;
        }

        long expiresAt = Long.parseLong(matcher.group(1));
        return Instant.now(clock).getEpochSecond() >= expiresAt;
    }

    private boolean constantTimeEquals(String expected, String actual) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    private String escapeJson(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");
    }
}
