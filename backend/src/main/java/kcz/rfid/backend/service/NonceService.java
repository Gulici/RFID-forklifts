package kcz.rfid.backend.service;

import java.util.UUID;

public interface NonceService {
    String generateAndSaveNonce(UUID deviceId);
    boolean verifyNonce(String nonce, UUID deviceId);
    void deleteNonce(UUID deviceId);
}
