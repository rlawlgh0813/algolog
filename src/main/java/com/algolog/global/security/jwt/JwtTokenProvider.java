package com.algolog.global.security.jwt;

import com.algolog.domain.user.User;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

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

    private String escapeJson(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");
    }
}
