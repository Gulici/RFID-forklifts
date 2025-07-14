package kcz.rfid.backend.service.impl;

import jakarta.persistence.EntityNotFoundException;
import kcz.rfid.backend.config.security.UserDetailsImpl;
import kcz.rfid.backend.exception.ResourceAlreadyExistsException;
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

import java.util.List;
import java.util.Optional;

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
        UserEntity userEntity = createSimpleUser(userDto, firmEntity);
        return userRepository.save(userEntity);
    }

    @Override
    public UserEntity createAdmin(UserRegisterDto userDto, FirmEntity firmEntity) {
        UserEntity userEntity = createSimpleUser(userDto, firmEntity);

        RoleEntity adminRole = roleService.getRoleByRole(RoleEnum.ROLE_ADMIN);
        userEntity.getRoles().add(adminRole);
        adminRole.getUsers().add(userEntity);

        return userRepository.save(userEntity);
    }

    private UserEntity createSimpleUser(UserRegisterDto userDto, FirmEntity firmEntity) {
        if (userDto.getUsername() == null || userDto.getUsername().isEmpty()
            || userDto.getPassword() == null || userDto.getPassword().isEmpty()
            || userDto.getEmail() == null || userDto.getEmail().isEmpty()
            || userDto.getFirmName() == null || userDto.getFirmName().isEmpty() ){
            throw new IllegalArgumentException("UserDto cannot have null or empty values");
        }

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

        RoleEntity userRole = roleService.getRoleByRole(RoleEnum.ROLE_USER);
        userRole.getUsers().add(userEntity);
        userEntity.getRoles().add(userRole);

        return userEntity;
    }

    @Override
    public UserEntity findUserByEmail(String email) {
       return userRepository.findByEmail(email)
               .orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));
    }

    @Override
    public UserEntity findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username " + username + " not found"));
    }

    @Override
    public UserEntity setAdminRole(UserEntity user) {
        RoleEntity adminRole = roleService.getRoleByRole(RoleEnum.ROLE_ADMIN);
        user.getRoles().add(adminRole);
        adminRole.getUsers().add(user);
        return userRepository.save(user);
    }

    @Override
    public void setUserRole(UserEntity user) {
        RoleEntity userRole = roleService.getRoleByRole(RoleEnum.ROLE_USER);
        user.getRoles().add(userRole);
    }

    @Override
    public List<UserEntity> findUsersByFirm(FirmEntity firmEntity) {
        return userRepository.findAllByFirm(firmEntity);
    }
}
