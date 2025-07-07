package kcz.rfid.backend.model.mapper;

import kcz.rfid.backend.model.dto.UserDto;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.UserEntity;

public class UserMapper {

    public static UserDto toDto(UserEntity user) {
        if (user == null) return null;
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setFirmName(user.getFirm().getFirmName());
        return userDto;
    }

   public static UserEntity updateUserFromDto(UserRegisterDto userDto, UserEntity user) {
        if (userDto == null) return null;
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        return user;
   }
}
