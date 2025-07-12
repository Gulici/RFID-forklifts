package kcz.rfid.backend.service;

import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends EntityService<UserEntity>, UserDetailsService {
    UserEntity createUser(UserRegisterDto userDto, FirmEntity firmEntity);
    UserEntity createAdmin(UserRegisterDto userDto, FirmEntity firmEntity);
    UserEntity findUserByEmail(String email);
    UserEntity setAdminRole(UserEntity user);
    void setUserRole(UserEntity user);
}
