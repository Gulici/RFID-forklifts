package kcz.rfid.backend.model.repository;

import kcz.rfid.backend.model.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends EntityRepository<UserEntity> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
}
