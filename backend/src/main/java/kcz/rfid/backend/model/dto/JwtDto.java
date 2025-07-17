package kcz.rfid.backend.model.dto;

import lombok.Data;

@Data
public class JwtDto {
    String jwt;

    public JwtDto(String jwt) {
        this.jwt = jwt;
    }
}
