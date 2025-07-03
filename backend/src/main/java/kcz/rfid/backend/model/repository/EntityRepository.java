package kcz.rfid.backend.model.repository;

import kcz.rfid.backend.model.entity.EntityBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

@NoRepositoryBean
public interface EntityRepository<T extends EntityBase> extends JpaRepository<T, UUID> {
}
