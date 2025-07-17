package kcz.rfid.backend.config.security;

import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.service.UserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    private final UserService userService;

    public SecurityService(UserService userService) {
        this.userService = userService;
    }

    public boolean isRoot(UserDetails user) {
        return user.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_ROOT"));
    }

    public boolean isAdminOfFirm(UserDetails user, FirmEntity firm) {
        if (!user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return false;
        }
        var admin = userService.findUserByUsername(user.getUsername());
        return admin.getFirm().equals(firm);
    }

    public boolean isUserOfFirm(UserDetails user, FirmEntity firm) {
        var requestedUser = userService.findUserByUsername(user.getUsername());
        return requestedUser.getFirm().equals(firm);
    }
}
