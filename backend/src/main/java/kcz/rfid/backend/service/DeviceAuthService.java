package kcz.rfid.backend.service;

import kcz.rfid.backend.model.dto.SignedNonceRequest;

public interface DeviceAuthService {
    String verifyAndIssueToken(SignedNonceRequest request);
}
