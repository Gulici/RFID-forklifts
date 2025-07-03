package kcz.rfid.backend.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "locations")
@Getter
@Setter
@ToString(exclude = {"forklifts", "locationHistoryList"})
public class LocationEntity extends EntityBase {

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "firm_id", nullable = false)
    private FirmEntity firm;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<ForkliftEntity> forklifts = new ArrayList<>();

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<LocationHistoryEntity> locationHistoryList = new ArrayList<>();

    @Column(nullable = false)
    private int x,y;
}
