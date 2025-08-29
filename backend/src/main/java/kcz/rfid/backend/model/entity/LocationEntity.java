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
@ToString(exclude = {"devices", "locationHistoryList"})
public class LocationEntity extends EntityBase {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int zoneId;

    @Column(nullable = false)
    private int x,y;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "firm_id", nullable = false)
    private FirmEntity firm;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<DeviceEntity> devices = new ArrayList<>();

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<LocationHistoryEntity> locationHistoryList = new ArrayList<>();
}
