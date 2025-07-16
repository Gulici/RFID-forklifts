package kcz.rfid.backend.service.impl;

import kcz.rfid.backend.service.NonceService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class NonceServiceImpl implements NonceService {

    private static final int NONCE_LENGTH = 24;
    private static final long NONCE_TTL_MINUTES = 5;

    private final SecureRandom secureRandom = new SecureRandom();
    private final StringRedisTemplate stringRedisTemplate;

    public NonceServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public String generateAndSaveNonce(UUID deviceId) {
        String nonce = generateNonce();
        String key = generateKey(deviceId);
        stringRedisTemplate.opsForValue().set(key, nonce,NONCE_TTL_MINUTES, TimeUnit.MINUTES);
        return nonce;
    }

    private String generateNonce() {
        byte[] randomBytes = new byte[NONCE_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @Override
    public boolean verifyNonce(String nonce, UUID deviceId) {
        String key = generateKey(deviceId);
        String value = stringRedisTemplate.opsForValue().get(key);
        return nonce.equals(value);
    }

    @Override
    public void deleteNonce(UUID deviceId) {
        String key = generateKey(deviceId);
        stringRedisTemplate.delete(key);
    }

    private String generateKey(UUID deviceId) {
        return "nonce:" + deviceId;
    }
}
