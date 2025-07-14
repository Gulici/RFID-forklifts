package kcz.rfid.backend.model.mapper;

public interface Mapper<E, D> {
    D mapToDto(E e);
}
