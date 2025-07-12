package kcz.rfid.backend.service.impl;

import jakarta.persistence.EntityNotFoundException;
import kcz.rfid.backend.config.security.UserDetailsImpl;
import kcz.rfid.backend.exeption.ResourceAlreadyExistsException;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.RoleEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.entity.util.RoleEnum;
import kcz.rfid.backend.model.repository.EntityRepository;
import kcz.rfid.backend.model.repository.UserRepository;
import kcz.rfid.backend.service.RoleService;
import kcz.rfid.backend.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static kcz.rfid.backend.model.mapper.UserMapper.updateUserFromDto;

@Service
public class UserServiceImpl extends EntityServiceBase<UserEntity> implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserServiceImpl(EntityRepository<UserEntity> repository, UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService) {
        super(repository);
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> userDetail = userRepository.findByUsername(username);
        return userDetail.map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
    }

    @Override
    public UserEntity createUser(UserRegisterDto userDto, FirmEntity firmEntity) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with email " + userDto.getEmail() + " already exists");
        }
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with username " + userDto.getUsername() + " already exists");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userDto.getUsername());
        userEntity.setEmail(userDto.getEmail());
        userEntity.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userEntity.setFirm(firmEntity);
        setUserRole(userEntity);

        return userRepository.save(userEntity);
    }

    @Override
    public UserEntity createAdmin(UserRegisterDto userDto, FirmEntity firmEntity) {
        UserEntity userEntity = createUser(userDto, firmEntity);
        this.setAdminRole(userEntity);
        return userRepository.save(userEntity);
    }

    @Override
    public UserEntity findUserByEmail(String email) {
       return userRepository.findByEmail(email)
               .orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));
    }

    @Override
    public UserEntity setAdminRole(UserEntity user) {
        RoleEntity userRole = roleService.getRoleByRole(RoleEnum.ROLE_ADMIN);
        user.getRoles().add(userRole);
        return userRepository.save(user);
    }

    @Override
    public void setUserRole(UserEntity user) {
        RoleEntity userRole = roleService.getRoleByRole(RoleEnum.ROLE_USER);
        user.getRoles().add(userRole);
    }
}
