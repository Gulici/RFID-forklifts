package kcz.rfid.backend.service;

import java.util.UUID;

public interface NonceService {
    String generateAndSaveNonce(UUID deviceId);
    void deleteNonce(UUID deviceId);
    String getStoredNonce(UUID deviceId);
}
