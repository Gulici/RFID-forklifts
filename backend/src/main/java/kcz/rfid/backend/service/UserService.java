package kcz.rfid.backend.service;

import kcz.rfid.backend.model.dto.UserDto;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.UserEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface UserService extends EntityService<UserEntity>, UserDetailsService {
    UserEntity addUser(UserRegisterDto user, UUID firmId);
    UserEntity getUserByEmail(String email);
    UserEntity getUserById(UUID id);
    UserEntity updateUser(UserEntity user, UserRegisterDto userDto );
    UserEntity setAdminRole(UserEntity user);
    void deleteUser(UUID id);
}
