package kcz.rfid.backend.config.security;

import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class DeviceAuthenticationToken extends AbstractAuthenticationToken {

    private final UUID deviceId;
    private final UUID firmId;
    private static final Collection<? extends GrantedAuthority> authorities =
            List.of(new SimpleGrantedAuthority("ROLE_DEVICE"));


    public DeviceAuthenticationToken(@NonNull UUID deviceId, @NonNull UUID firmId) {
        super(authorities);
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
