package kcz.rfid.backend.config.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.UUID;

@Getter
public class DeviceAuthenticationToken extends AbstractAuthenticationToken {

    private final UUID deviceId;
    private final UUID firmId;

    public DeviceAuthenticationToken(UUID deviceId, UUID firmId) {
        super(null);
        this.deviceId = deviceId;
        this.firmId = firmId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return deviceId;
    }
}
