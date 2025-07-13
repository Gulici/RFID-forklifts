package kcz.rfid.backend.model.repository;

import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.ForkliftEntity;
import kcz.rfid.backend.model.entity.LocationHistoryEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocationHistoryRepository extends EntityRepository<LocationHistoryEntity> {
    @Query("SELECT lh FROM LocationHistoryEntity lh WHERE lh.location.firm = :firm")
    List<LocationHistoryEntity> findAllByFirm(@Param("firm") FirmEntity firm);

    List<LocationHistoryEntity> findAllByForklift(ForkliftEntity forklift);
}
