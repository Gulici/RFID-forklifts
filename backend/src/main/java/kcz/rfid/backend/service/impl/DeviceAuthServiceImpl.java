package kcz.rfid.backend.service.impl;

import kcz.rfid.backend.config.security.JwtService;
import kcz.rfid.backend.model.dto.SignedNonceRequest;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.model.repository.DeviceRepository;
import kcz.rfid.backend.service.DeviceAuthService;
import kcz.rfid.backend.service.NonceService;
import kcz.rfid.backend.service.utils.PemUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

@Service
public class DeviceAuthServiceImpl implements DeviceAuthService {

    private final DeviceRepository deviceRepository;
    private final NonceService nonceService;
    private final JwtService jwtService;

    public DeviceAuthServiceImpl(DeviceRepository deviceRepository, NonceService nonceService, JwtService jwtService) {
        this.deviceRepository = deviceRepository;
        this.nonceService = nonceService;
        this.jwtService = jwtService;
    }

    @Override
    public String verifyAndIssueToken(SignedNonceRequest request) {
        String publicKeyPem = request.getPublicKeyPem();
        String signature = request.getSignatureBase64();

        String fingerprint = PemUtils.computeFingerprint(publicKeyPem);

        DeviceEntity device = deviceRepository.findByFingerprint(fingerprint)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Device not found"));
        String expectedNonce = nonceService.getStoredNonce(device.getId());
        if (expectedNonce == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nonce expired or missing");

        try {
            PublicKey publicKey = PemUtils.parsePublicKeyPem(publicKeyPem);
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(expectedNonce.getBytes(StandardCharsets.UTF_8));

            boolean verified = verifier.verify(Base64.getDecoder().decode(signature));
            if (!verified) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Signature verification failed");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid public key");
        }

        nonceService.deleteNonce(device.getId());
        return jwtService.generateDeviceToken(device.getId(), device.getFirm().getId());
    }
}
