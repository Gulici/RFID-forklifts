package kcz.rfid.backend.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "firms")
@Getter
@Setter
@ToString(exclude = {"users", "locations", "forklifts"})
public class FirmEntity extends EntityBase {

    @Column(nullable = false, unique = true)
    private String firmName;

    @OneToMany(mappedBy = "firm", cascade = CascadeType.ALL)
    private List<UserEntity> users = new ArrayList<>();

    @OneToMany(mappedBy = "firm", cascade = CascadeType.ALL)
    private List<LocationEntity> locations = new ArrayList<>();

    @OneToMany(mappedBy = "firm", cascade = CascadeType.ALL)
    private List<ForkliftEntity> forklifts = new ArrayList<>();
}
