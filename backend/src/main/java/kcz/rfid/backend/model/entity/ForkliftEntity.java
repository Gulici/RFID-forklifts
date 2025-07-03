package kcz.rfid.backend.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "forklifts")
@Getter
@Setter
@ToString(exclude = {"locationHistoryList"})
public class ForkliftEntity extends EntityBase {

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "firm_id", nullable = false)
    private FirmEntity firm;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id")
    private LocationEntity location;

    @OneToMany(mappedBy = "forklift", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LocationHistoryEntity> locationHistoryList = new ArrayList<>();
}
