package kcz.rfid.backend.service;

import kcz.rfid.backend.model.dto.UserDto;
import kcz.rfid.backend.model.entity.UserEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends EntityService<UserEntity>, UserDetailsService {
    UserEntity addUser(UserEntity user);
}
