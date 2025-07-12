package kcz.rfid.backend.service.impl;

import kcz.rfid.backend.model.entity.RoleEntity;
import kcz.rfid.backend.model.entity.util.RoleEnum;
import kcz.rfid.backend.model.repository.EntityRepository;
import kcz.rfid.backend.model.repository.RoleRepository;
import kcz.rfid.backend.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends EntityServiceBase<RoleEntity> implements RoleService {
    private final RoleRepository roleRepository;

    public RoleServiceImpl(EntityRepository<RoleEntity> repository, RoleRepository roleRepository) {
        super(repository);
        this.roleRepository = roleRepository;
    }

    @Override
    public RoleEntity getRoleByName(String roleName) {
        return roleRepository.findRoleEntityByName(RoleEnum.valueOf(roleName)).orElse(null);
    }

    @Override
    public RoleEntity getRoleByRole(RoleEnum role) {
        return roleRepository.findRoleEntityByName(role).orElse(null);
    }
}
