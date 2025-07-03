package kcz.rfid.backend.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "locations_history")
@Getter
@Setter
@ToString
public class LocationHistoryEntity extends EntityBase{

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "forklift_id", nullable = false)
    private ForkliftEntity forklift;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    private LocationEntity location;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
