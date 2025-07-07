package kcz.rfid.backend.service.impl;

import jakarta.persistence.EntityNotFoundException;
import kcz.rfid.backend.config.security.UserDetailsImpl;
import kcz.rfid.backend.exeption.ResourceAlreadyExistsException;
import kcz.rfid.backend.model.dto.UserDto;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.RoleEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.mapper.UserMapper;
import kcz.rfid.backend.model.repository.EntityRepository;
import kcz.rfid.backend.model.repository.UserRepository;
import kcz.rfid.backend.service.FirmService;
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
    private final FirmService firmService;

    public UserServiceImpl(EntityRepository<UserEntity> repository, UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService, FirmService firmService) {
        super(repository);
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.firmService = firmService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> userDetail = userRepository.findByEmail(username);
        return userDetail.map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
    }

    @Override
    public UserEntity addUser(UserRegisterDto userDto, UUID firmId) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with email " + userDto.getEmail() + " already exists");
        }
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with username " + userDto.getUsername() + " already exists");
        }

        FirmEntity firm = firmService.getFirm(firmId);

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userDto.getUsername());
        userEntity.setEmail(userDto.getEmail());
        userEntity.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userEntity.setFirm(firm);
        setUserRole(userEntity);

        return userRepository.save(userEntity);
    }

    @Override
    public UserEntity getUserByEmail(String email) {
       return userRepository.findByEmail(email)
               .orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));
    }

    @Override
    public UserEntity getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
    }

    @Override
    public UserEntity updateUser(UserEntity user, UserRegisterDto userDto) {
        user = updateUserFromDto(userDto, user);
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserEntity setAdminRole(UserEntity user) {
        RoleEntity userRole = roleService.getRoleByName("ROLE_ADMIN");
        user.getRoles().add(userRole);
        return userRepository.save(user);
    }

    private void setUserRole(UserEntity user) {
        RoleEntity userRole = roleService.getRoleByName("ROLE_USER");
        user.getRoles().add(userRole);
    }
}
